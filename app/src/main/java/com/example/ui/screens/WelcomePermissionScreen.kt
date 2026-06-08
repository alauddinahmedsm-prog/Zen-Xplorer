package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.permission.PermissionManager
import com.example.viewmodel.MainViewModel

@Composable
fun WelcomePermissionScreen(
    viewModel: MainViewModel,
    onContinueClick: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Interactive permission toggles / states
    var coreImagesGranted by remember { mutableStateOf(false) }
    var coreVideoGranted by remember { mutableStateOf(false) }
    var coreAudioGranted by remember { mutableStateOf(false) }
    var legacyStorageGranted by remember { mutableStateOf(false) }
    var notificationGranted by remember { mutableStateOf(false) }
    var advancedFileMgmtGranted by remember { mutableStateOf(false) }
    var biometricsSupported by remember { mutableStateOf(false) }
    var apkInstallGranted by remember { mutableStateOf(false) }
    var cameraGranted by remember { mutableStateOf(false) }

    // Update status from permissions
    fun refreshPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            coreImagesGranted = PermissionManager.isPermissionGranted(context, Manifest.permission.READ_MEDIA_IMAGES)
            coreVideoGranted = PermissionManager.isPermissionGranted(context, Manifest.permission.READ_MEDIA_VIDEO)
            coreAudioGranted = PermissionManager.isPermissionGranted(context, Manifest.permission.READ_MEDIA_AUDIO)
            notificationGranted = PermissionManager.isPermissionGranted(context, Manifest.permission.POST_NOTIFICATIONS)
        } else {
            legacyStorageGranted = PermissionManager.isPermissionGranted(context, Manifest.permission.READ_EXTERNAL_STORAGE)
            notificationGranted = true
        }
        advancedFileMgmtGranted = PermissionManager.hasAdvancedFileManagementPermission(context)
        biometricsSupported = PermissionManager.isBiometricsAvailable(context)
        apkInstallGranted = PermissionManager.canInstallApks(context)
        cameraGranted = PermissionManager.isPermissionGranted(context, Manifest.permission.CAMERA)
    }

    LaunchedEffect(Unit) {
        refreshPermissions()
    }

    // Launchers
    val singlePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        refreshPermissions()
        val text = if (isGranted) "Permission granted!" else "Permission denied"
        viewModel.showMessage(text)
    }

    val multiplePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        refreshPermissions()
        val allGranted = permissions.values.all { it }
        viewModel.showMessage(if (allGranted) "Core permissions set up!" else "Some permissions were denied")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A1F44))
            .padding(24.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Welcome Header
        Icon(
            imageVector = Icons.Filled.VerifiedUser,
            contentDescription = "Welcome clearance shield",
            tint = Color(0xFF06B6D4),
            modifier = Modifier
                .size(56.dp)
                .padding(top = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Welcome to Zen Xplorer",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Fine-tune system authorizations to index and protect your digital assets securely.",
            color = Color(0xFF94A3B8),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        // The Required Informative Storage Notice Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF06B6D4).copy(alpha = 0.3f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Crucial Notice",
                    tint = Color(0xFF06B6D4),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "PERMISSION TRANSPARENCY NOTICE",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF06B6D4),
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Zen Xplorer requires storage access to browse, organize, move, copy, and manage your files across your device. Your data remains private and is never uploaded without your permission.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // List of Interactive Permissions Inside a scroll container
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "REQUEST CORE CLEARANCE",
                color = Color(0xFF3B82F6),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(start = 4.dp)
            )

            // Dynamic Core Media permission row depending on SDK version
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Photos & Videos Permission Check
                InteractivePermissionCard(
                    title = "Photos & Videos Access",
                    description = "Let Zen Xplorer view media files and display live image thumbnails in directories.",
                    icon = Icons.Filled.Image,
                    isGranted = coreImagesGranted && coreVideoGranted,
                    onRequestClick = {
                        multiplePermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.READ_MEDIA_IMAGES,
                                Manifest.permission.READ_MEDIA_VIDEO
                            )
                        )
                    }
                )

                // Audio Files Permission Check
                InteractivePermissionCard(
                    title = "Audio & Music Access",
                    description = "Required to preview MP3 recordings and list audio files inside the system music catalog.",
                    icon = Icons.Filled.AudioFile,
                    isGranted = coreAudioGranted,
                    onRequestClick = {
                        singlePermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
                    }
                )
            } else {
                // Pre-Android 13 Unified storage permissions
                InteractivePermissionCard(
                    title = "Workspace Core Storage Access",
                    description = "Grant standard permission to view documents, logs, downloads, and archives in storage routes.",
                    icon = Icons.Filled.Folder,
                    isGranted = legacyStorageGranted,
                    onRequestClick = {
                        val permissionsToRequest = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        } else {
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                        multiplePermissionLauncher.launch(permissionsToRequest)
                    }
                )
            }

            // Notifications
            InteractivePermissionCard(
                title = "Push status logs & notifications",
                description = "Keep active progress logs on backgrounds, zip conversions, or synchronization feedback visible.",
                icon = Icons.Filled.Notifications,
                isGranted = notificationGranted,
                onRequestClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        singlePermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            )

            Text(
                text = "ADVANCED SECTOR ENABLERS",
                color = Color(0xFF3B82F6),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )

            // Advanced file management (MANAGE_EXTERNAL_STORAGE)
            InteractivePermissionCard(
                title = "Advanced File Management",
                description = "Enables total sandbox access for moving files, recycling catalogs, or deleting system items recursive.",
                icon = Icons.Filled.SettingsSystemDaydream,
                isGranted = advancedFileMgmtGranted,
                onRequestClick = {
                    try {
                        val intent = PermissionManager.getManageFilesSettingIntent(context)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        viewModel.showMessage("Redirect error. Please open app details in Android Settings.")
                    }
                }
            )

            // Secure Vault (Biometrics)
            InteractivePermissionCard(
                title = "Secure Biometrics Lock",
                description = "Requires biometric fingerprint hardware clearance to authenticate your locked security vault.",
                icon = Icons.Filled.Security,
                isGranted = biometricsSupported,
                actionLabel = if (biometricsSupported) "Enrolled" else "Unsupported Device",
                onRequestClick = {
                    viewModel.showMessage("Biometric support is handled by the android physical system lock screen.")
                }
            )

            // Camera permission (QR scanning / vault features)
            InteractivePermissionCard(
                title = "Camera Authorization",
                description = "Used for document image uploads and scanning secure vault QR connection keys.",
                icon = Icons.Filled.Camera,
                isGranted = cameraGranted,
                onRequestClick = {
                    singlePermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            )

            // Unknown Sources / APK installs
            InteractivePermissionCard(
                title = "Package / APK Installer",
                description = "Allow Zen Xplorer to install, extract, and update compiled app packages directly from workspace catalogs.",
                icon = Icons.Filled.Android,
                isGranted = apkInstallGranted,
                onRequestClick = {
                    try {
                        val intent = PermissionManager.getInstallUnknownAppsSettingIntent(context)
                        context.startActivity(intent)
                    } catch (e: java.lang.Exception) {
                        viewModel.showMessage("Please set Install Unknown Apps in System App Preferences manually.")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Settings fallback link
        Row(
            modifier = Modifier
                .clickable {
                    try {
                        context.startActivity(PermissionManager.getAppSettingsIntent(context))
                    } catch (e: Exception) {
                        viewModel.showMessage("Could not open application settings")
                    }
                }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "System Preferences Link",
                tint = Color(0xFF3B82F6),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Alternative: Grant via Android Settings App",
                color = Color(0xFF3B82F6),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Primary Continue to Workspace
        Button(
            onClick = onContinueClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3B82F6)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("welcome_permission_continue")
        ) {
            Text(
                text = "Continue to Workspace",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun InteractivePermissionCard(
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    actionLabel: String? = null,
    onRequestClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) Color(0xFF0F172A).copy(alpha = 0.7f) else Color(0xFF1E293B)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (isGranted) Color(0xFF10B981).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (isGranted) Color(0xFF10B981).copy(alpha = 0.08f) else Color.White.copy(alpha = 0.03f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Permission category logo",
                        tint = if (isGranted) Color(0xFF10B981) else Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = description,
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            if (isGranted) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Permission OK",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = actionLabel ?: "Cleared",
                        color = Color(0xFF10B981),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = onRequestClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0F172A),
                        contentColor = Color(0xFF06B6D4)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF06B6D4).copy(alpha = 0.4f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = "Authorize",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
