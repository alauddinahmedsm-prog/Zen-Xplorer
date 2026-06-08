package com.example.data.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.biometric.BiometricManager

object PermissionManager {

    // --- Permissions Definitions ---
    
    val REQUIRED_CORE_PERMISSIONS: Array<String>
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            val list = mutableListOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                list.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            list.toTypedArray()
        }

    // --- Permission Checks ---

    fun hasStoragePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Checks if the core media permissions are granted.
     * On Android 13+ (API 33+), checks READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, and READ_MEDIA_AUDIO.
     * On older versions, checks READ_EXTERNAL_STORAGE.
     */
    fun hasCoreStoragePermissions(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            isPermissionGranted(context, Manifest.permission.READ_MEDIA_IMAGES) &&
                    isPermissionGranted(context, Manifest.permission.READ_MEDIA_VIDEO) &&
                    isPermissionGranted(context, Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            isPermissionGranted(context, Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    /**
     * Checks if notification permission is granted (Android 13+).
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            isPermissionGranted(context, Manifest.permission.POST_NOTIFICATIONS)
        } else {
            true
        }
    }

    /**
     * Checks if MANAGE_EXTERNAL_STORAGE is granted (Android 11+).
     */
    fun hasAdvancedFileManagementPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            // Falls back to standard storage checking on older versions
            hasCoreStoragePermissions(context)
        }
    }

    /**
     * Checks if camera is granted.
     */
    fun hasCameraPermission(context: Context): Boolean {
        return isPermissionGranted(context, Manifest.permission.CAMERA)
    }

    /**
     * Checks if Biometrics is available on hardware.
     */
    fun isBiometricsAvailable(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        val result = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        )
        return result == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Checks if APK unknown source installation is cleared (Android 8.0+).
     */
    fun canInstallApks(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    // --- Redirection Builders ---

    /**
     * Direct user to the primary app details settings page.
     */
    fun getAppSettingsIntent(context: Context): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Direct user to Advanced File Management permission request (Android 11+).
     */
    fun getManageFilesSettingIntent(context: Context): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            getAppSettingsIntent(context)
        }
    }

    /**
     * Redirect user to request package install package clearance.
     */
    fun getInstallUnknownAppsSettingIntent(context: Context): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            getAppSettingsIntent(context)
        }
    }
}
