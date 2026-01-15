package com.pixnpunk.quasporium

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.core.app.ActivityCompat
import com.anggrayudi.storage.SimpleStorage.Companion.cleanupRedundantUriPermissions
import com.anggrayudi.storage.SimpleStorage.Companion.hasStoragePermission
import com.anggrayudi.storage.contract.FolderPickerResult
import com.anggrayudi.storage.contract.OpenFolderPickerContract
import com.anggrayudi.storage.contract.RequestStorageAccessContract
import com.anggrayudi.storage.contract.RequestStorageAccessResult
import com.anggrayudi.storage.contract.StoragePermissionContract
import com.anggrayudi.storage.extension.startActivitySafely
import com.anggrayudi.storage.extension.toDocumentFile
import com.anggrayudi.storage.file.MimeType
import com.arkivanov.decompose.defaultComponentContext
import com.pixnpunk.dto.OutputState
import com.pixnpunk.game.presentation.GameActivity
import com.pixnpunk.settings.SettingsRepository
import com.pixnpunk.utils.AddonsUtils
import com.pixnpunk.utils.FileUtil.isWritableDir
import com.pixnpunk.utils.FileUtil.isWritableFile
import org.koin.android.ext.android.get
import kotlin.concurrent.thread
import kotlin.contracts.ExperimentalContracts

class MainActivity : ComponentActivity() {

    private val settingsRepository: SettingsRepository = get()

    private lateinit var root: RealRootComponent

    @OptIn(ExperimentalContracts::class)
    private val dirPickerLauncher = runCatching {
        registerForActivityResult(OpenFolderPickerContract(get())) {
            when (it) {
                is FolderPickerResult.AccessDenied -> {
                    root.doShowDialog(
                        outputValue = """
                            We require full access to your device storage so that you can easily select and add games from any folder where they are stored.
                            This permission ensures stable file management on older Android versions (8, 9 and 10).
                            <br><br>
                            <b>For external storage:</b> tap "Allow access to..." when prompted.
                            <br>
                            <b>For SD cards:</b> select the main folder (root directory) of the SD card and tap "Allow access to..." when prompted.
                        """.trimIndent(),
                        outputState = OutputState.OUTPUT_REQUEST_PERM
                    )
                }

                is FolderPickerResult.CanceledByUser -> {

                }

                is FolderPickerResult.Picked -> {
                    var newGameDir = it.folder
                    if (newGameDir.isWritableDir()) {
                        val path = newGameDir.uri.toString()
                        val dirName = newGameDir.name

                        if (path.isNotEmpty() && dirName != null) {
                            root.processPickedDir(dirName, path)
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalContracts::class)
    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            if (it != null) {
                val contentResolver = applicationContext.contentResolver
                contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                thread { cleanupRedundantUriPermissions(applicationContext) }

                var newGameDir = it.toDocumentFile(this)
                if (newGameDir.isWritableFile()) {
                    val path = newGameDir.uri.toString()
                    if (path.isNotEmpty()) {
                        root.processPickedFile(path)
                    }
                }
            }
        }


    @OptIn(ExperimentalContracts::class)
    private val filesPickerLauncher =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) {
            if (it.isNotEmpty()) {
                var docGameFilesList = mutableListOf<String>()
                it.forEach { uri ->
                    val contentResolver = applicationContext.contentResolver
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    val file = uri.toDocumentFile(this)
                    if (file.isWritableFile()) {
                        docGameFilesList.add(file.uri.toString())
                    }
                }

                thread { cleanupRedundantUriPermissions(applicationContext) }

                if (docGameFilesList.isNotEmpty()) {
                    root.processPickedFiles(docGameFilesList)
                }
            }
        }

    private val storageAccessLauncher = runCatching {
        registerForActivityResult(RequestStorageAccessContract(get())) { result ->
            when (result) {
                is RequestStorageAccessResult.CanceledByUser -> {

                }

                is RequestStorageAccessResult.ExpectedStorageNotSelected -> {

                }

                is RequestStorageAccessResult.RootPathNotSelected -> {

                }

                is RequestStorageAccessResult.RootPathPermissionGranted -> {

                }

                is RequestStorageAccessResult.StoragePermissionDenied -> {

                }
            }
        }
    }

    private val storagePermissionLauncher =
        registerForActivityResult(StoragePermissionContract()) { _ ->

        }


    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q
            && !hasStoragePermission(this)
        ) {
            storagePermissionLauncher.launch()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val postNotification = ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (postNotification != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    200
                )
            }
        }

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        root = RealRootComponent(
            componentContext = defaultComponentContext(),
            launchDirPicker = {
                dirPickerLauncher.getOrNull()?.launch(OpenFolderPickerContract.Options())
            },
            launchFilePicker = {
                filePickerLauncher.launch(arrayOf(MimeType.IMAGE))
            },
            launchFilesPicker = {
                filesPickerLauncher.launch(arrayOf(MimeType.BINARY_FILE))
            },
            launchStorageAccess = {
                storageAccessLauncher.getOrNull()?.launch(RequestStorageAccessContract.Options())
            },
            launchEngineIntent = { (entry, i) ->
                startActivitySafely(
                    AddonsUtils.createEngineIntent(
                        clazz = GameActivity::class.java,
                        entry = entry,
                        fileSelIndex = i
                    )
                )
            }
        )

        setContent { RootContent(root, settingsRepository) }
    }
}