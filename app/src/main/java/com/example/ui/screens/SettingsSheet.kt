package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CompanionSettings
import com.example.data.model.MemoryItem
import com.example.data.model.PRESET_LANGUAGES
import com.example.data.model.PRESET_MODELS
import com.example.ui.theme.DimText
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.InkBackground
import com.example.ui.theme.LineBorder
import com.example.ui.theme.MistText
import com.example.ui.theme.PanelSurface
import com.example.ui.theme.PanelSurfaceVariant
import com.example.ui.theme.RoseDark
import com.example.ui.theme.RosePrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsSheet(
    settings: CompanionSettings,
    memories: List<MemoryItem>,
    onSaveSettings: (CompanionSettings) -> Unit,
    onTestApiKey: (String, String) -> Unit,
    onAddMemory: (String) -> Unit,
    onRemoveMemory: (String) -> Unit,
    onClearChat: () -> Unit,
    onClearMemories: () -> Unit,
    onSignOut: () -> Unit = {},
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var apiKey by remember(settings.apiKey) { mutableStateOf(settings.apiKey) }
    var showApiKey by remember { mutableStateOf(false) }
    var selectedModel by remember(settings.model) { mutableStateOf(settings.model) }
    var companionName by remember(settings.companionName) { mutableStateOf(settings.companionName) }
    var userName by remember(settings.userName) { mutableStateOf(settings.userName) }
    var gender by remember(settings.gender) { mutableStateOf(settings.gender) }
    var relationship by remember(settings.relationship) { mutableStateOf(settings.relationship) }
    var languageCode by remember(settings.languageCode) { mutableStateOf(settings.languageCode) }
    var fastVoice by remember(settings.fastVoice) { mutableStateOf(settings.fastVoice) }
    var autoReadReplies by remember(settings.autoReadReplies) { mutableStateOf(settings.autoReadReplies) }

    var newMemoryInput by remember { mutableStateOf("") }
    var langDropdownExpanded by remember { mutableStateOf(false) }

    fun buildCurrentSettings() = CompanionSettings(
        apiKey = apiKey,
        model = selectedModel,
        companionName = companionName.ifBlank { "Kayara" },
        userName = userName,
        gender = gender,
        relationship = relationship,
        languageCode = languageCode,
        fastVoice = fastVoice,
        autoReadReplies = autoReadReplies
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PanelSurface)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Sheet Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Settings",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = MistText
            )

            Button(
                onClick = {
                    onSaveSettings(buildCurrentSettings())
                    onClose()
                },
                modifier = Modifier.testTag("settings_done_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RosePrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Done", fontWeight = FontWeight.SemiBold)
            }
        }

        HorizontalDivider(color = LineBorder, thickness = 1.dp)

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // --- SECTION 1: Gemini Connection ---
            Text(
                text = "Gemini Connection",
                style = MaterialTheme.typography.titleMedium,
                color = GoldAccent
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text("Gemini API Key", style = MaterialTheme.typography.bodySmall, color = DimText)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_api_key_input"),
                placeholder = { Text("Paste your API key (or uses injected key)", color = DimText) },
                singleLine = true,
                visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showApiKey = !showApiKey }) {
                        Icon(
                            imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle key visibility",
                            tint = DimText
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RosePrimary,
                    unfocusedBorderColor = LineBorder,
                    focusedTextColor = MistText,
                    unfocusedTextColor = MistText,
                    focusedContainerColor = InkBackground,
                    unfocusedContainerColor = InkBackground
                ),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Get a free key at aistudio.google.com/apikey. Saved only on this device.",
                style = MaterialTheme.typography.bodySmall,
                color = DimText
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text("Model", style = MaterialTheme.typography.bodySmall, color = DimText)
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PRESET_MODELS.forEach { modelName ->
                    val isSelected = selectedModel == modelName
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) RosePrimary else InkBackground)
                            .clickable { selectedModel = modelName }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = modelName,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) Color.White else MistText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    onSaveSettings(buildCurrentSettings())
                    onTestApiKey(apiKey, selectedModel)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("test_api_key_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PanelSurfaceVariant,
                    contentColor = MistText
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save and test key")
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = LineBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // --- SECTION 2: Companion Persona ---
            Text(
                text = "Your Companion",
                style = MaterialTheme.typography.titleMedium,
                color = GoldAccent
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text("Companion Name", style = MaterialTheme.typography.bodySmall, color = DimText)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = companionName,
                onValueChange = { companionName = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_companion_name_input"),
                placeholder = { Text("Kayara", color = DimText) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RosePrimary,
                    unfocusedBorderColor = LineBorder,
                    focusedTextColor = MistText,
                    unfocusedTextColor = MistText,
                    focusedContainerColor = InkBackground,
                    unfocusedContainerColor = InkBackground
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Your Name", style = MaterialTheme.typography.bodySmall, color = DimText)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = userName,
                onValueChange = { userName = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_user_name_input"),
                placeholder = { Text("What should they call you?", color = DimText) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RosePrimary,
                    unfocusedBorderColor = LineBorder,
                    focusedTextColor = MistText,
                    unfocusedTextColor = MistText,
                    focusedContainerColor = InkBackground,
                    unfocusedContainerColor = InkBackground
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text("Voice and Personality", style = MaterialTheme.typography.bodySmall, color = DimText)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (gender == "female") RosePrimary else InkBackground)
                        .clickable { gender = "female" }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Female (sweet voice)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (gender == "female") Color.White else MistText
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (gender == "male") RosePrimary else InkBackground)
                        .clickable { gender = "male" }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Male (calm voice)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (gender == "male") Color.White else MistText
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text("Relationship", style = MaterialTheme.typography.bodySmall, color = DimText)
            Spacer(modifier = Modifier.height(6.dp))
            listOf(
                "partner" to "Loving partner (girlfriend / boyfriend)",
                "friend" to "Best friend",
                "expert" to "Teacher and expert"
            ).forEach { (key, label) ->
                val isSel = relationship == key
                Card(
                    onClick = { relationship = key },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSel) RoseDark.copy(alpha = 0.4f) else InkBackground
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(if (isSel) RosePrimary else Color.Transparent)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MistText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = LineBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // --- SECTION 3: Language and Voice ---
            Text(
                text = "Language and Voice",
                style = MaterialTheme.typography.titleMedium,
                color = GoldAccent
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text("Speaking Language (for microphone & voice)", style = MaterialTheme.typography.bodySmall, color = DimText)
            Spacer(modifier = Modifier.height(6.dp))

            val currentLangName = PRESET_LANGUAGES.find { it.code == languageCode }?.displayName ?: languageCode
            Box {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(InkBackground)
                        .clickable { langDropdownExpanded = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = currentLangName, color = MistText)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = DimText)
                    }
                }

                DropdownMenu(
                    expanded = langDropdownExpanded,
                    onDismissRequest = { langDropdownExpanded = false },
                    modifier = Modifier.background(PanelSurfaceVariant)
                ) {
                    PRESET_LANGUAGES.forEach { lang ->
                        DropdownMenuItem(
                            text = { Text(lang.displayName, color = MistText) },
                            onClick = {
                                languageCode = lang.code
                                langDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Read every reply aloud", style = MaterialTheme.typography.bodyMedium, color = MistText)
                    Text("Automatic voice speech for all responses", style = MaterialTheme.typography.bodySmall, color = DimText)
                }
                Switch(
                    checked = autoReadReplies,
                    onCheckedChange = { autoReadReplies = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = RosePrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Fast voice response", style = MaterialTheme.typography.bodyMedium, color = MistText)
                    Text("Immediate audio synthesis on response arrival", style = MaterialTheme.typography.bodySmall, color = DimText)
                }
                Switch(
                    checked = fastVoice,
                    onCheckedChange = { fastVoice = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = RosePrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = LineBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // --- SECTION 4: Memories Learned ---
            Text(
                text = "What KAYARA Has Learned About You",
                style = MaterialTheme.typography.titleMedium,
                color = GoldAccent
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Over conversations, KAYARA extracts lasting personal details and saves them here.",
                style = MaterialTheme.typography.bodySmall,
                color = DimText
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (memories.isEmpty()) {
                Text(
                    text = "No saved memories yet. Chat with Kayara and she will remember facts about you!",
                    style = MaterialTheme.typography.bodySmall,
                    color = DimText,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                memories.forEach { mem ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = InkBackground),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "• ${mem.fact}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MistText,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { onRemoveMemory(mem.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete memory",
                                    tint = DimText,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Add manual memory
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newMemoryInput,
                    onValueChange = { newMemoryInput = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Add custom fact about you...", color = DimText) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RosePrimary,
                        unfocusedBorderColor = LineBorder,
                        focusedTextColor = MistText,
                        unfocusedTextColor = MistText,
                        focusedContainerColor = InkBackground,
                        unfocusedContainerColor = InkBackground
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (newMemoryInput.isNotBlank()) {
                            onAddMemory(newMemoryInput)
                            newMemoryInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(RosePrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add memory", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onClearChat,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PanelSurfaceVariant,
                        contentColor = MistText
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear chat")
                }
                Button(
                    onClick = onClearMemories,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PanelSurfaceVariant,
                        contentColor = Color(0xFFFFC9D6)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Forget everything")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Google Account & Cloud Sync Status ---
            val currentUser = Firebase.auth.currentUser
            Card(
                colors = CardDefaults.cardColors(containerColor = PanelSurfaceVariant),
                shape = RoundedCornerShape(14.dp),
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
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(RoseDark.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = currentUser?.displayName ?: "Google Account",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = currentUser?.email ?: "Cloud sync active",
                                style = MaterialTheme.typography.bodySmall,
                                color = DimText
                            )
                        }
                    }

                    Button(
                        onClick = onSignOut,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF381E2B),
                            contentColor = Color(0xFFFFC9D6)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("sign_out_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sign out", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
            HorizontalDivider(color = LineBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // --- Footer Credit ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "KAYARA AI",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MistText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Designed by Mr. Laxman Chhetri\nfrom Bardiya, Nepal",
                    style = MaterialTheme.typography.bodySmall,
                    color = DimText,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
