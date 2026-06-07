package com.example.ui.screens

import android.text.format.Formatter
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.database.FileMetaEntity
import com.example.data.file.ZenFileManager
import com.example.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val recycleBinItems by viewModel.recycleBin.collectAsState()
    val vaultItems by viewModel.vaultFiles.collectAsState()

    val context = LocalContext.current
    val isVaultLocked by viewModel.isVaultLocked.collectAsState()
    var showVaultUnlockPinDialog by remember { mutableStateOf(false) }
    var vaultPinInput by remember { mutableStateOf("") }
    var vaultPinError by remember { mutableStateOf<String?>(null) }

    var showPinChangeDialog by remember { mutableStateOf(false) }
    var oldPinInput by remember { mutableStateOf("") }
    var newPinInput by remember { mutableStateOf("") }

    var showCloudConnectDialog by remember { mutableStateOf<String?>(null) } // "Drive", "DropBox", "SFTP"
    var cloudHostInput by remember { mutableStateOf("") }
    var cloudUserInput by remember { mutableStateOf("") }
    var cloudPassInput by remember { mutableStateOf("") }

    val isGoogleDriveConnected by viewModel.isGoogleDriveConnected.collectAsState()
    val isOneDriveConnected by viewModel.isOneDriveConnected.collectAsState()
    val isDropboxConnected by viewModel.isDropboxConnected.collectAsState()
    val isSftpConnected by viewModel.isSftpConnected.collectAsState()

    val googleDriveSyncFolders by viewModel.googleDriveSyncFolders.collectAsState()
    val oneDriveSyncFolders by viewModel.oneDriveSyncFolders.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Settings Slogan
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Text(
                text = "Workspace Settings",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Personalize files, locks, and databases.",
                fontSize = 14.sp,
                color = Color(0xFF06B6D4)
            )
        }

        // SECTION: THEME & STORAGE ENGINE BOUNDS
        SettingsCategoryTitle("Appearance & Theme")
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(16.dp)
            ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsSwitchRow(
                    title = "Premium Deep Blue Canvas",
                    description = "Locks the application inside the high-contrast aesthetic deep blue theme.",
                    checked = isDarkTheme,
                    onCheckedChange = { viewModel.toggleTheme() }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION: CLOUD ACCOUNT INTEGRATION
        SettingsCategoryTitle("Cloud Storage & SFTP Networks")
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Google Drive Row
                CloudConnectionRow(
                    name = "Google Drive Backups",
                    isConnected = isGoogleDriveConnected,
                    icon = Icons.Filled.Cloud
                ) {
                    if (isGoogleDriveConnected) {
                        viewModel.setGoogleDriveConnected(false)
                        viewModel.showMessage("Separated Google Drive account.")
                    } else {
                        cloudHostInput = ""
                        cloudUserInput = ""
                        cloudPassInput = ""
                        showCloudConnectDialog = "Google Drive"
                    }
                }

                if (isGoogleDriveConnected) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF06B6D4).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "GOOGLE DRIVE SYNC FOLDERS",
                            color = Color(0xFF06B6D4),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                        )
                        googleDriveSyncFolders.forEach { (folder, isSynced) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Folder,
                                        contentDescription = "Sync Folder",
                                        tint = Color(0xFF06B6D4).copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "$folder Folder",
                                        color = Color.White.copy(alpha = 0.82f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Switch(
                                    checked = isSynced,
                                    onCheckedChange = { viewModel.toggleGoogleDriveSyncFolder(folder) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF06B6D4),
                                        uncheckedThumbColor = Color(0xFF64748B),
                                        uncheckedTrackColor = Color(0xFF1E293B)
                                    ),
                                    modifier = Modifier.testTag("sync_gdrive_$folder")
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF1E3A8A))

                // OneDrive Row
                CloudConnectionRow(
                    name = "OneDrive Cloud Sync",
                    isConnected = isOneDriveConnected,
                    icon = Icons.Filled.Cloud
                ) {
                    if (isOneDriveConnected) {
                        viewModel.setOneDriveConnected(false)
                        viewModel.showMessage("Separated OneDrive directory connection.")
                    } else {
                        cloudHostInput = ""
                        cloudUserInput = ""
                        cloudPassInput = ""
                        showCloudConnectDialog = "OneDrive"
                    }
                }

                if (isOneDriveConnected) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "ONEDRIVE SYNC FOLDERS",
                            color = Color(0xFF3B82F6),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                        )
                        oneDriveSyncFolders.forEach { (folder, isSynced) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Folder,
                                        contentDescription = "Sync Folder",
                                        tint = Color(0xFF3B82F6).copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "$folder Folder",
                                        color = Color.White.copy(alpha = 0.82f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Switch(
                                    checked = isSynced,
                                    onCheckedChange = { viewModel.toggleOneDriveSyncFolder(folder) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF3B82F6),
                                        uncheckedThumbColor = Color(0xFF64748B),
                                        uncheckedTrackColor = Color(0xFF1E293B)
                                    ),
                                    modifier = Modifier.testTag("sync_onedrive_$folder")
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF1E3A8A))

                // Dropbox Row
                CloudConnectionRow(
                    name = "Dropbox Personal Account",
                    isConnected = isDropboxConnected,
                    icon = Icons.Filled.CloudQueue
                ) {
                    if (isDropboxConnected) {
                        viewModel.setDropboxConnected(false)
                        viewModel.showMessage("Separated Dropbox account.")
                    } else {
                        cloudHostInput = ""
                        cloudUserInput = ""
                        cloudPassInput = ""
                        showCloudConnectDialog = "Dropbox"
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF1E3A8A))

                // SFTP Row
                CloudConnectionRow(
                    name = "Secure SFTP/FTP Host",
                    isConnected = isSftpConnected,
                    icon = Icons.Filled.Storage
                ) {
                    if (isSftpConnected) {
                        viewModel.setSftpConnected(false)
                        viewModel.showMessage("Deactivated SFTP transmission port link.")
                    } else {
                        cloudHostInput = "sftp.alauddin-architect.com"
                        cloudUserInput = "alauddin_guest"
                        cloudPassInput = ""
                        showCloudConnectDialog = "SFTP"
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION: FILE SECURITY LOCKS
        SettingsCategoryTitle("Security Protocols")
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsActionRow(
                    title = "Setup Global PIN Applock",
                    description = "Change the 4-digit pass key that protects Zen Explorer vault containers.",
                    icon = Icons.Filled.VpnKey
                ) {
                    oldPinInput = ""
                    newPinInput = ""
                    showPinChangeDialog = true
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF1E3A8A))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isVaultLocked) {
                                val activity = context as? FragmentActivity
                                if (activity != null) {
                                    val biometricManager = BiometricManager.from(context)
                                    val canAuthenticate = biometricManager.canAuthenticate(
                                        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
                                    )
                                    if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
                                        showBiometricPrompt(
                                            activity = activity,
                                            onSuccess = {
                                                viewModel.unlockVaultWithoutPin()
                                            },
                                            onError = { err ->
                                                viewModel.showMessage("Biometric feedback: $err")
                                                vaultPinInput = ""
                                                vaultPinError = null
                                                showVaultUnlockPinDialog = true
                                            }
                                        )
                                    } else {
                                        vaultPinInput = ""
                                        vaultPinError = null
                                        showVaultUnlockPinDialog = true
                                    }
                                } else {
                                    vaultPinInput = ""
                                    vaultPinError = null
                                    showVaultUnlockPinDialog = true
                                }
                            } else {
                                viewModel.lockVault()
                            }
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isVaultLocked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                        contentDescription = if (isVaultLocked) "Vault Locked" else "Vault Unlocked",
                        tint = if (isVaultLocked) Color(0xFF14B8A6) else Color(0xFFF59E0B),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Active Private Vault Records", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(
                            text = if (isVaultLocked) "${vaultItems.size} files obfuscated & locked" else "UNLOCKED • Tap to lock container", 
                            fontSize = 12.sp, 
                            color = if (isVaultLocked) Color(0xFF94A3B8) else Color(0xFF10B981)
                        )
                    }
                    if (!isVaultLocked) {
                        IconButton(onClick = { viewModel.lockVault() }) {
                            Icon(Icons.Filled.LockOpen, contentDescription = "Unlock Status Action", tint = Color(0xFF10B981))
                        }
                    }
                }

                if (!isVaultLocked) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Zen Secure Vault Storage Viewer",
                        color = Color(0xFF14B8A6),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (vaultItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No secured items recorded.\nNavigate to Workspace and long-press a file, then click 'Add to Secure Hidden Vault' to protect items.",
                                color = Color(0xFF64748B),
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(12.dp))
                                .padding(8.dp)
                        ) {
                            vaultItems.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (item.isFolder) Icons.Filled.Folder else Icons.Filled.Description,
                                        contentDescription = "Vault Item Icon",
                                        tint = if (item.isFolder) Color(0xFF3B82F6) else Color(0xFF94A3B8),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.name,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = Formatter.formatShortFileSize(context, item.size),
                                            color = Color(0xFF64748B),
                                            fontSize = 11.sp
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.restoreFromVault(item) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Restore,
                                            contentDescription = "Restore File",
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteFromVaultPermanently(item) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Delete File Permanently",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION: RETRIVAL UTILITY CLEARANCES
        SettingsCategoryTitle("Retrieval & Maintenance")
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (recycleBinItems.isNotEmpty()) {
                                viewModel.clearRecycleBin()
                            } else {
                                viewModel.showMessage("Recycle Bin is already empty!")
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFFEF4444).copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear Recycle", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Empty Recycle Bin Log", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("${recycleBinItems.size} items pending permanent erasure", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    }
                }
            }
        }
    }

    // PIN CHALLENGE UNLOCK MODAL
    if (showVaultUnlockPinDialog) {
        Dialog(onDismissRequest = { showVaultUnlockPinDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF14B8A6).copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = "PIN Security Check",
                        tint = Color(0xFF14B8A6),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Enter Vault Security PIN",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Enter your 4-digit workspace pass-key passcode.",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = vaultPinInput,
                        onValueChange = { 
                            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                vaultPinInput = it
                                vaultPinError = null
                            }
                        },
                        label = { Text("4-Digit PIN Code", color = Color(0xFF64748B)) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF14B8A6),
                            unfocusedBorderColor = Color(0xFF1E3A8A)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("vault_pin_input")
                    )

                    if (vaultPinError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = vaultPinError!!,
                            color = Color(0xFFEF4444),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { showVaultUnlockPinDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Cancel", color = Color(0xFF94A3B8))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (viewModel.unlockVault(vaultPinInput)) {
                                    showVaultUnlockPinDialog = false
                                } else {
                                    vaultPinError = "Incorrect security PIN. Please try again."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF14B8A6)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("vault_pin_submit")
                        ) {
                            Text("Access", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // CLOUD CONNECTION ENTRY MODALS
    if (showCloudConnectDialog != null) {
        val target = showCloudConnectDialog!!
        Dialog(onDismissRequest = { showCloudConnectDialog = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1F44)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Sign In: $target",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (target == "SFTP") {
                        OutlinedTextField(
                            value = cloudHostInput,
                            onValueChange = { cloudHostInput = it },
                            label = { Text("Server Host Address", color = Color(0xFF64748B)) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    OutlinedTextField(
                        value = cloudUserInput,
                        onValueChange = { cloudUserInput = it },
                        label = { Text("Username / Account ID", color = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = cloudPassInput,
                        onValueChange = { cloudPassInput = it },
                        label = { Text("Access Password / Secret Token", color = Color(0xFF64748B)) },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Row {
                        Button(
                            onClick = { showCloudConnectDialog = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                        ) {
                            Text("Cancel", color = Color(0xFF94A3B8))
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = {
                                if (target == "Google Drive") viewModel.setGoogleDriveConnected(true)
                                if (target == "OneDrive") viewModel.setOneDriveConnected(true)
                                if (target == "Dropbox") viewModel.setDropboxConnected(true)
                                if (target == "SFTP") viewModel.setSftpConnected(true)
                                viewModel.showMessage("Authentication link to $target active.")
                                showCloudConnectDialog = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                        ) {
                            Text("Link Account", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // PIN CHANGE PASSCODE CONTROL DIALOG
    if (showPinChangeDialog) {
        Dialog(onDismissRequest = { showPinChangeDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1F44)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Modify Core Applock PIN",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = oldPinInput,
                        onValueChange = { oldPinInput = it },
                        label = { Text("Verify Old PIN (Default '1234')", color = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = { newPinInput = it },
                        label = { Text("Enter New 4-Digit PIN", color = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Row {
                        Button(
                            onClick = { showPinChangeDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                        ) {
                            Text("Dismiss", color = Color(0xFF94A3B8))
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = {
                                val ok = viewModel.changeAppPin(oldPinInput, newPinInput)
                                if (ok) {
                                    viewModel.showMessage("App lock PIN security updated.")
                                    showPinChangeDialog = false
                                } else {
                                    viewModel.showMessage("Old security PIN is invalid!")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                        ) {
                            Text("Confirm PIN", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsCategoryTitle(name: String) {
    Text(
        text = name,
        color = Color(0xFF06B6D4),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 10.dp)
    )
}

@Composable
fun SettingsSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(description, color = Color(0xFF94A3B8), fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF3B82F6),
                uncheckedThumbColor = Color(0xFF64748B),
                uncheckedTrackColor = Color(0xFF1E293B)
            )
        )
    }
}

@Composable
fun CloudConnectionRow(
    name: String,
    isConnected: Boolean,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = name, tint = if (isConnected) Color(0xFF10B981) else Color(0xFF94A3B8), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(if (isConnected) "Fully Connected & Syncing" else "Offline • Disconnected", color = if (isConnected) Color(0xFF10B981) else Color(0xFF64748B), fontSize = 11.sp)
        }
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isConnected) Color(0xFFEF4444).copy(alpha = 0.15f) else Color(0xFF3B82F6)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = if (isConnected) "Unlink" else "Link",
                color = if (isConnected) Color(0xFFEF4444) else Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SettingsActionRow(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, tint = Color(0xFF3B82F6), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(description, color = Color(0xFF94A3B8), fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

fun showBiometricPrompt(
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val executor = ContextCompat.getMainExecutor(activity)
    val biometricPrompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                activity.runOnUiThread { onSuccess() }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                activity.runOnUiThread { onError(errString.toString()) }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
            }
        }
    )

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Biometric Key Clearance")
        .setSubtitle("Authenticate via fingerprint or face to unlock secure vault records")
        .setNegativeButtonText("Use PIN Instead")
        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)
        .build()

    try {
        biometricPrompt.authenticate(promptInfo)
    } catch (e: Exception) {
        onError(e.message ?: "Launch failed")
    }
}

