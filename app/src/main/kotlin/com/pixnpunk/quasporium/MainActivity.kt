package com.pixnpunk.quasporium

import android.Manifest
import android.content.ActivityNotFoundException
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
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withResumed
import com.anggrayudi.storage.contract.RequestStorageAccessContract
import com.anggrayudi.storage.contract.RequestStorageAccessResult
import com.anggrayudi.storage.contract.StoragePermissionContract
import com.anggrayudi.storage.extension.startActivitySafely
import com.anggrayudi.storage.extension.toDocumentFile
import com.anggrayudi.storage.file.MimeType
import com.anggrayudi.storage.file.openInputStream
import com.arkivanov.decompose.defaultComponentContext
import com.pixnpunk.dto.OutputState
import com.pixnpunk.game.presentation.GameActivity
import com.pixnpunk.storage.AddonsUtils
import com.pixnpunk.storage.FileUtil.isWritableDir
import com.pixnpunk.storage.FileUtil.isWritableFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.get
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.contracts.ExperimentalContracts

class MainActivity : ComponentActivity() {

    private lateinit var root: RealRootComponent

    @OptIn(ExperimentalContracts::class)
    private val dirPickerLauncher =
        registerForActivityResult(
            ActivityResultContracts.OpenDocumentTree()
        ) {
            if (it != null) {
                contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                var newGameDir = it.toDocumentFile(get())
                if (newGameDir.isWritableDir()) {
                    val path = newGameDir.uri.toString()
                    val dirName = newGameDir.name

                    if (path.isNotEmpty() && dirName != null) {
                        root.processPickedDir(dirName, path)
                    }
                } else {
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
            }
        }

    @OptIn(ExperimentalContracts::class)
    private val filePickerLauncher =
        registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) {
            if (it != null) {
                contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                var newGameDir = it.toDocumentFile(this)
                if (newGameDir.isWritableFile()) {
                    val path = newGameDir.uri.toString()
                    if (path.isNotEmpty()) {
                        root.processPickedFile(path)
                    }
                }
            }
        }

    private val fontPickerLauncher =
        registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri != null) {
                val customFont = uri.toDocumentFile(this) ?: return@registerForActivityResult
                if (customFont.isFile && customFont.canRead()) {
                    lifecycleScope.launch {
                        val cachedFontFile = withContext(Dispatchers.IO) {
                            val extension = customFont.name
                                ?.substringAfterLast('.', "")
                                ?.lowercase()
                                ?.takeIf { it in FONT_EXTENSIONS }
                                ?: "ttf"
                            val target = File(
                                File(filesDir, "custom_fonts").apply { mkdirs() },
                                "custom_font_${UUID.randomUUID()}.$extension"
                            )
                            val isCopied = runCatching {
                                customFont.openInputStream(this@MainActivity)!!.use { inputStream ->
                                    FileOutputStream(target).use { outputStream ->
                                        inputStream.copyTo(outputStream)
                                    }
                                }
                            }.isSuccess

                            if (isCopied && isFontFile(target)) {
                                target
                            } else {
                                target.delete()
                                null
                            }
                        }

                        if (cachedFontFile != null) {
                            root.processPickedFont(cachedFontFile.absolutePath)
                        } else {
                            root.doShowDialog(
                                outputValue = "The selected file is not a TrueType or OpenType font.",
                                outputState = OutputState.OUTPUT_SIMPLE_MSG
                            )
                        }
                    }
                }
            }
        }

    private val storageAccessLauncher =
        registerForActivityResult(
            RequestStorageAccessContract(get())
        ) { result ->
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

    private val storagePermissionLauncher =
        registerForActivityResult(StoragePermissionContract()) { _ ->

        }

    private companion object {
        val FONT_EXTENSIONS = setOf("ttf", "otf", "ttc")

        fun isFontFile(file: File): Boolean {
            val header = runCatching {
                file.inputStream().use {
                    input -> ByteArray(4)
                        .also { input.read(it) }
                }
            }.getOrNull() ?: return false
            val tag = String(header, Charsets.ISO_8859_1)
            return header.contentEquals(byteArrayOf(0, 1, 0, 0)) ||
                    tag == "OTTO" || tag == "true" || tag == "ttcf"
        }
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            val hasWrite = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            val hasRead = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasWrite || !hasRead) {
                storagePermissionLauncher.launch()
            }
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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        root = RealRootComponent(
            componentContext = defaultComponentContext(),
            launchDirPicker = {
                try {
                    dirPickerLauncher.launch(null)
                } catch (_: ActivityNotFoundException) {
                    root.doShowDialog(
                        outputValue = """
                            No app was found on your device to choose folders. Please make sure the system Files app is enabled in your device settings.
                        """.trimIndent(),
                        outputState = OutputState.OUTPUT_SIMPLE_MSG
                    )
                }
            },
            launchFilePicker = {
                try {
                    filePickerLauncher.launch(arrayOf(MimeType.IMAGE))
                } catch (_: ActivityNotFoundException) {
                    root.doShowDialog(
                        outputValue = """
                            No app was found on your device to choose folders. Please make sure the system Files app is enabled in your device settings.
                        """.trimIndent(),
                        outputState = OutputState.OUTPUT_SIMPLE_MSG
                    )
                }
            },
            launchFontPicker = {
                try {
                    fontPickerLauncher.launch(arrayOf(MimeType.FONT))
                } catch (_: ActivityNotFoundException) {
                    root.doShowDialog(
                        outputValue = """
                            No app was found on your device to choose folders. Please make sure the system Files app is enabled in your device settings.
                        """.trimIndent(),
                        outputState = OutputState.OUTPUT_SIMPLE_MSG
                    )
                }
            },
            launchStorageAccess = {
                runCatching {
                    storageAccessLauncher.launch(
                        RequestStorageAccessContract.Options()
                    )
                }.onFailure { }
            },
            launchEngineIntent = {
                startActivitySafely(
                    AddonsUtils.createEngineIntent(
                        clazz = GameActivity::class.java,
                        entity = it
                    )
                )
            }
        )

        splashScreen.setKeepOnScreenCondition {
            !root.model.value.isMigrationComplete
        }

        lifecycleScope.launch {
            root.model.first { it.isMigrationComplete }

            withResumed { checkAndRequestPermissions() }
        }

        setContent { RootContent(root) }
    }
}
