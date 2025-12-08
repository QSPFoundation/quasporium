package com.pixnpunk.quasporium

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import com.anggrayudi.storage.SimpleStorage.Companion.cleanupRedundantUriPermissions
import com.anggrayudi.storage.contract.FolderPickerResult
import com.anggrayudi.storage.contract.OpenFolderPickerContract
import com.anggrayudi.storage.contract.RequestStorageAccessContract
import com.anggrayudi.storage.contract.RequestStorageAccessResult
import com.anggrayudi.storage.contract.StoragePermissionContract
import com.anggrayudi.storage.contract.StoragePermissionDeniedException
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
                    root.showMessageDialog(
                        message = """
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
    private val filePickerLauncher = runCatching {
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
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        dirPickerLauncher.onFailure {
            when (it) {
                is StoragePermissionDeniedException -> {
                    storagePermissionLauncher.launch()
                }
            }
        }

        filePickerLauncher.onFailure {
            when (it) {
                is StoragePermissionDeniedException -> {
                    storagePermissionLauncher.launch()
                }
            }
        }

        storageAccessLauncher.onFailure {
            when (it) {
                is StoragePermissionDeniedException -> {
                    storagePermissionLauncher.launch()
                }
            }
        }

        root = RealRootComponent(
            componentContext = defaultComponentContext(),
            launchDirPicker = {
                dirPickerLauncher.getOrNull()?.launch(OpenFolderPickerContract.Options())
            },
            launchFilePicker = {
                filePickerLauncher.getOrNull()?.launch(arrayOf(MimeType.IMAGE))
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