package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.draw.rotate
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonCyan
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CompanionSettings
import com.example.data.model.MEMORY_CATEGORIES
import com.example.data.model.MemoryItem
import com.example.data.model.PRESET_LANGUAGES
import com.example.data.model.PRESET_PERSONALITIES
import com.example.data.model.PersonalityPreset
import com.example.ui.theme.DeepSpace
import com.example.ui.theme.DimText
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.InkBackground
import com.example.ui.theme.LineBorder
import com.example.ui.theme.MistText
import com.example.ui.theme.MutedText
import com.example.ui.theme.PanelSurface
import com.example.ui.theme.PanelSurfaceVariant
import com.example.ui.theme.RoseDark
import com.example.ui.theme.RosePrimary
import com.example.ui.theme.RoseSubtle
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileSettingsScreen(
    settings: CompanionSettings,
    memories: List<MemoryItem>,
    detectedModels: List<com.example.data.model.DiscoveredModel> = emptyList(),
    isDetectingModels: Boolean = false,
    modelDetectionStatus: String? = null,
    onDetectModels: (apiKey: String) -> Unit = {},
    onSaveSettings: (CompanionSettings) -> Unit,
    onTestApiKey: (key: String, model: String) -> Unit,
    onAddMemory: (fact: String, category: String) -> Unit,
    onUpdateMemory: (id: String, fact: String, category: String) -> Unit,
    onRemoveMemory: (id: String) -> Unit,
    onClearChat: () -> Unit,
    onClearMemories: () -> Unit,
    onSpeakTest: (text: String) -> Unit,
    onSignOut: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentSettings by remember(settings) { mutableStateOf(settings) }
    var selectedTab by remember { mutableIntStateOf(0) }

    // Dialog states
    var showAddMemoryDialog by remember { mutableStateOf(false) }
    var memoryToEdit by remember { mutableStateOf<MemoryItem?>(null) }
    var showClearChatDialog by remember { mutableStateOf(false) }
    var showClearMemoriesDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    // Memory filter & search
    var memorySearchQuery by remember { mutableStateOf("") }
    var selectedMemoryCategory by remember { mutableStateOf("All") }

    // API Key visibility toggle
    var showApiKeyText by remember { mutableStateOf(false) }

    // Helper to persist and sync changes immediately so state is never lost
    fun commitUpdate(updated: CompanionSettings) {
        currentSettings = updated
        onSaveSettings(updated)
    }

    // Intercept hardware/system back button to guarantee settings are saved
    BackHandler {
        onSaveSettings(currentSettings)
        onClose()
    }

    val tabTitles = listOf("Soul & Presets", "Memory Brain", "Voice & Lang", "Account & AI")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpace)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // --- Top Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(InkBackground)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    onSaveSettings(currentSettings)
                    onClose()
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(PanelSurface)
                    .testTag("settings_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back and Save",
                    tint = MistText
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Profile & Brain Settings",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MistText,
                        fontSize = 18.sp
                    )
                )
                Text(
                    text = "Auto-saved to cloud & device",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = GoldAccent,
                        fontSize = 11.sp
                    )
                )
            }

            // Save CTA badge button
            Surface(
                onClick = {
                    onSaveSettings(currentSettings)
                    Toast.makeText(context, "All settings saved!", Toast.LENGTH_SHORT).show()
                },
                color = RosePrimary,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("save_settings_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Saved",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Saved",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        // --- Tab Row ---
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = InkBackground,
            contentColor = MistText,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = GoldAccent,
                    height = 3.dp
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    modifier = Modifier.testTag("tab_$index"),
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == index) GoldAccent else DimText
                            )
                        )
                    }
                )
            }
        }

        // --- Tab Contents ---
        when (selectedTab) {
            0 -> SoulPresetsTab(
                settings = currentSettings,
                onUpdate = { commitUpdate(it) }
            )

            1 -> MemoryBrainTab(
                memories = memories,
                autoMemoryEnabled = currentSettings.autoMemoryEnabled,
                onToggleAutoMemory = { enabled ->
                    commitUpdate(currentSettings.copy(autoMemoryEnabled = enabled))
                },
                searchQuery = memorySearchQuery,
                onSearchChange = { memorySearchQuery = it },
                selectedCategory = selectedMemoryCategory,
                onCategoryChange = { selectedMemoryCategory = it },
                onAddNewClick = { showAddMemoryDialog = true },
                onEditClick = { memoryToEdit = it },
                onDeleteClick = { onRemoveMemory(it.id) },
                onClearAllClick = { showClearMemoriesDialog = true }
            )

            2 -> VoiceLanguageTab(
                settings = currentSettings,
                onUpdate = { commitUpdate(it) },
                onSpeakTest = onSpeakTest
            )

            3 -> AccountAiTab(
                settings = currentSettings,
                detectedModels = detectedModels,
                isDetectingModels = isDetectingModels,
                modelDetectionStatus = modelDetectionStatus,
                onDetectModels = onDetectModels,
                showApiKey = showApiKeyText,
                onToggleShowApiKey = { showApiKeyText = !showApiKeyText },
                onUpdate = { commitUpdate(it) },
                onTestApiKey = { key, model -> onTestApiKey(key, model) },
                onClearChatClick = { showClearChatDialog = true },
                onSignOutClick = { showSignOutDialog = true }
            )
        }
    }

    // --- Dialog: Add New Memory ---
    if (showAddMemoryDialog) {
        MemoryEditorDialog(
            title = "Add Memory to Brain",
            initialFact = "",
            initialCategory = if (selectedMemoryCategory == "All") "General" else selectedMemoryCategory,
            onConfirm = { fact, category ->
                onAddMemory(fact, category)
                showAddMemoryDialog = false
                Toast.makeText(context, "Memory encoded into brain", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showAddMemoryDialog = false }
        )
    }

    // --- Dialog: Edit Existing Memory ---
    memoryToEdit?.let { item ->
        MemoryEditorDialog(
            title = "Edit Brain Memory",
            initialFact = item.fact,
            initialCategory = item.category,
            onConfirm = { fact, category ->
                onUpdateMemory(item.id, fact, category)
                memoryToEdit = null
                Toast.makeText(context, "Memory updated", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { memoryToEdit = null }
        )
    }

    // --- Dialog: Clear Memories ---
    if (showClearMemoriesDialog) {
        AlertDialog(
            onDismissRequest = { showClearMemoriesDialog = false },
            containerColor = PanelSurface,
            title = {
                Text(
                    text = "Clear Memory Brain?",
                    style = MaterialTheme.typography.titleMedium.copy(color = MistText, fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "This will erase all stored long-term memories from both this device and cloud Firestore. Kayara will forget everything you shared.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = DimText)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearMemories()
                        showClearMemoriesDialog = false
                    }
                ) {
                    Text("Clear All", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearMemoriesDialog = false }) {
                    Text("Cancel", color = MistText)
                }
            }
        )
    }

    // --- Dialog: Clear Chat ---
    if (showClearChatDialog) {
        AlertDialog(
            onDismissRequest = { showClearChatDialog = false },
            containerColor = PanelSurface,
            title = {
                Text(
                    text = "Clear Chat History?",
                    style = MaterialTheme.typography.titleMedium.copy(color = MistText, fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "This will wipe the current chat timeline and reset with a fresh greeting. Memories stored in the brain will NOT be affected.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = DimText)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearChat()
                        showClearChatDialog = false
                    }
                ) {
                    Text("Clear Chat", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearChatDialog = false }) {
                    Text("Cancel", color = MistText)
                }
            }
        )
    }

    // --- Dialog: Sign Out ---
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            containerColor = PanelSurface,
            title = {
                Text(
                    text = "Sign Out?",
                    style = MaterialTheme.typography.titleMedium.copy(color = MistText, fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "You will be returned to the Google Sign-In screen. Your synchronized data remains safely in Google Cloud Firestore.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = DimText)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutDialog = false
                        onSignOut()
                    }
                ) {
                    Text("Sign Out", color = RosePrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel", color = MistText)
                }
            }
        )
    }
}

// -------------------------------------------------------------
// TAB 0: SOUL & PERSONALITY PRESETS
// -------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SoulPresetsTab(
    settings: CompanionSettings,
    onUpdate: (CompanionSettings) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section header
        item {
            SectionHeader(
                icon = Icons.Default.AutoAwesome,
                title = "AI Personality Presets",
                subtitle = "Choose the soul and conversational energy of your companion"
            )
        }

        // Preset cards
        items(PRESET_PERSONALITIES) { preset ->
            val isSelected = settings.personalityPreset == preset.id ||
                    (settings.personalityPreset.isBlank() && preset.id == "partner")
            PersonalityCard(
                preset = preset,
                isSelected = isSelected,
                onClick = {
                    onUpdate(
                        settings.copy(
                            personalityPreset = preset.id,
                            relationship = preset.id
                        )
                    )
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(
                icon = Icons.Default.Tune,
                title = "Identity & Names",
                subtitle = "Customize how you and your companion address each other"
            )
        }

        // Companion Name
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PanelSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Companion Name",
                        style = MaterialTheme.typography.labelLarge.copy(color = GoldAccent, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = settings.companionName,
                        onValueChange = { onUpdate(settings.copy(companionName = it)) },
                        placeholder = { Text("e.g. Kayara", color = DimText) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_companion_name"),
                        colors = outlinedFieldColors(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Your Name / Nickname",
                        style = MaterialTheme.typography.labelLarge.copy(color = GoldAccent, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = settings.userName,
                        onValueChange = { onUpdate(settings.copy(userName = it)) },
                        placeholder = { Text("e.g. Laxman", color = DimText) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_user_name"),
                        colors = outlinedFieldColors(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Companion Gender",
                        style = MaterialTheme.typography.labelLarge.copy(color = GoldAccent, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("female" to "Female", "male" to "Male").forEach { (key, label) ->
                            val isChosen = settings.gender.lowercase() == key
                            Surface(
                                onClick = { onUpdate(settings.copy(gender = key)) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isChosen) RosePrimary else PanelSurfaceVariant,
                                border = if (isChosen) null else androidx.compose.foundation.BorderStroke(1.dp, LineBorder)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            color = if (isChosen) Color.White else MistText,
                                            fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Custom Prompt Directives
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PanelSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Custom Instructions & Secrets",
                        style = MaterialTheme.typography.labelLarge.copy(color = GoldAccent, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Special behaviors, tone quirks, or rules for Kayara (e.g. 'Use cute pet names', 'Focus on daily fitness')",
                        style = MaterialTheme.typography.bodySmall.copy(color = DimText)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = settings.customInstructions,
                        onValueChange = { onUpdate(settings.copy(customInstructions = it)) },
                        placeholder = { Text("e.g. Always be supportive, keep answers concise during voice calls...", color = DimText) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .testTag("input_custom_instructions"),
                        colors = outlinedFieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PersonalityCard(
    preset: PersonalityPreset,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) GoldAccent else LineBorder,
        label = "preset_border"
    )
    val containerColor = if (isSelected) PanelSurfaceVariant else PanelSurface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
            .testTag("preset_${preset.id}"),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) RosePrimary else DeepSpace),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = preset.emoji, fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = preset.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) GoldAccent else MistText
                            )
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = GoldAccent,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    color = DeepSpace,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = preset.subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(color = DimText),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Trait pills
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                preset.traits.forEach { trait ->
                    Surface(
                        color = if (isSelected) RoseSubtle else DeepSpace,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder)
                    ) {
                        Text(
                            text = trait,
                            color = if (isSelected) GoldAccent else MistText,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sample greeting preview
            Text(
                text = "\"${preset.sampleGreeting}\"",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = DimText,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontSize = 12.sp
                )
            )
        }
    }
}

// -------------------------------------------------------------
// TAB 1: MEMORY BRAIN MANAGEMENT
// -------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MemoryBrainTab(
    memories: List<MemoryItem>,
    autoMemoryEnabled: Boolean,
    onToggleAutoMemory: (Boolean) -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    onAddNewClick: () -> Unit,
    onEditClick: (MemoryItem) -> Unit,
    onDeleteClick: (MemoryItem) -> Unit,
    onClearAllClick: () -> Unit
) {
    val filteredMemories = remember(memories, searchQuery, selectedCategory) {
        memories.filter { item ->
            val matchCat = selectedCategory == "All" || item.category.equals(selectedCategory, ignoreCase = true)
            val matchSearch = searchQuery.isBlank() || item.fact.contains(searchQuery, ignoreCase = true)
            matchCat && matchSearch
        }.sortedByDescending { it.timestamp }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Card: Brain Knowledge Bank
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PanelSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(RoseSubtle),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Persistent Brain Memory",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MistText
                                )
                            )
                            Text(
                                text = "${memories.size} facts encoded across chat and live calls",
                                style = MaterialTheme.typography.bodySmall.copy(color = GoldAccent)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Auto-Learn switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DeepSpace)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-Learn in Conversations",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MistText
                                )
                            )
                            Text(
                                text = "Kayara extracts personal preferences and facts automatically",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = DimText,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Switch(
                            checked = autoMemoryEnabled,
                            onCheckedChange = onToggleAutoMemory,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = RosePrimary,
                                uncheckedThumbColor = DimText,
                                uncheckedTrackColor = PanelSurfaceVariant
                            )
                        )
                    }
                }
            }
        }

        // Add Memory Button & Clear All
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    onClick = onAddNewClick,
                    modifier = Modifier.weight(1f),
                    color = RosePrimary,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add Fact",
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                if (memories.isNotEmpty()) {
                    Surface(
                        onClick = onClearAllClick,
                        color = PanelSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Clear All",
                                color = ErrorRed,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search brain memories...", color = DimText) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = DimText
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = DimText)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("memory_search_input"),
                colors = outlinedFieldColors(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Category Filter Chips
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val categories = listOf("All") + MEMORY_CATEGORIES
                categories.forEach { cat ->
                    val isSelected = selectedCategory.equals(cat, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategoryChange(cat) },
                        label = { Text(cat, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldAccent,
                            selectedLabelColor = DeepSpace,
                            containerColor = PanelSurface,
                            labelColor = MistText
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) GoldAccent else LineBorder
                        )
                    )
                }
            }
        }

        // Memory List Items
        if (filteredMemories.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No memories match '$searchQuery'" else "Brain memory is currently clear",
                            style = MaterialTheme.typography.bodyMedium.copy(color = DimText)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add facts above or chat with Kayara to auto-learn",
                            style = MaterialTheme.typography.bodySmall.copy(color = GoldAccent)
                        )
                    }
                }
            }
        } else {
            items(filteredMemories, key = { it.id }) { memory ->
                MemoryItemCard(
                    item = memory,
                    onEdit = { onEditClick(memory) },
                    onDelete = { onDeleteClick(memory) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }
    }
}

@Composable
private fun MemoryItemCard(
    item: MemoryItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryBadgeColor = when (item.category.lowercase()) {
        "goal" -> GoldAccent
        "personal" -> RosePrimary
        "preference" -> Color(0xFF8E7CFF)
        "work" -> Color(0xFF4AC4E2)
        else -> DimText
    }

    val formattedDate = remember(item.timestamp) {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        sdf.format(Date(item.timestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PanelSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Category badge
                Surface(
                    color = categoryBadgeColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, categoryBadgeColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = item.category.uppercase(),
                        color = categoryBadgeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall.copy(color = DimText, fontSize = 11.sp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Memory",
                            tint = MistText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Memory",
                            tint = ErrorRed.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.fact,
                style = MaterialTheme.typography.bodyMedium.copy(color = MistText, fontSize = 14.sp)
            )
        }
    }
}

// -------------------------------------------------------------
// TAB 2: VOICE & LANGUAGE
// -------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VoiceLanguageTab(
    settings: CompanionSettings,
    onUpdate: (CompanionSettings) -> Unit,
    onSpeakTest: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader(
                icon = Icons.Default.RecordVoiceOver,
                title = "Voice & Spoken Audio",
                subtitle = "Configure live call synthesis and speech-to-text recognition"
            )
        }

        // Test Voice Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PanelSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Voice Synthesis Audio",
                        style = MaterialTheme.typography.labelLarge.copy(color = GoldAccent, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Kayara uses Android's native Text-to-Speech engine with natural intonation in ${settings.languageCode}.",
                        style = MaterialTheme.typography.bodySmall.copy(color = DimText)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        onClick = {
                            val testSentence = if (settings.languageCode.startsWith("ne")) {
                                "नमस्ते! म तपाइँको साथी कायरा हुँ।"
                            } else if (settings.languageCode.startsWith("hi")) {
                                "नमस्ते! मैं आपकी दोस्त कायरा हूँ।"
                            } else {
                                "Hello! I am ${settings.companionName}, your companion. How can I brighten your day?"
                            }
                            onSpeakTest(testSentence)
                        },
                        color = RosePrimary,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Test Voice Sample",
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }

        // Language Selection
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PanelSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Preferred Language",
                        style = MaterialTheme.typography.labelLarge.copy(color = GoldAccent, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Speech recognition and voice response will adapt to this language",
                        style = MaterialTheme.typography.bodySmall.copy(color = DimText)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PRESET_LANGUAGES.forEach { lang ->
                            val isChosen = settings.languageCode.equals(lang.code, ignoreCase = true)
                            FilterChip(
                                selected = isChosen,
                                onClick = { onUpdate(settings.copy(languageCode = lang.code)) },
                                label = { Text(lang.displayName, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldAccent,
                                    selectedLabelColor = DeepSpace,
                                    containerColor = DeepSpace,
                                    labelColor = MistText
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isChosen,
                                    borderColor = if (isChosen) GoldAccent else LineBorder
                                )
                            )
                        }
                    }
                }
            }
        }

        // Switches: Auto-read replies
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PanelSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-Read Chat Replies",
                                style = MaterialTheme.typography.labelLarge.copy(color = MistText, fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Automatically speak incoming text messages aloud",
                                style = MaterialTheme.typography.bodySmall.copy(color = DimText)
                            )
                        }
                        Switch(
                            checked = settings.autoReadReplies,
                            onCheckedChange = { onUpdate(settings.copy(autoReadReplies = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = RosePrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Fast Voice Synthesis",
                                style = MaterialTheme.typography.labelLarge.copy(color = MistText, fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Slightly accelerated playback cadence for responsive voice calls",
                                style = MaterialTheme.typography.bodySmall.copy(color = DimText)
                            )
                        }
                        Switch(
                            checked = settings.fastVoice,
                            onCheckedChange = { onUpdate(settings.copy(fastVoice = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = RosePrimary
                            )
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }
    }
}

// -------------------------------------------------------------
// TAB 3: ACCOUNT & AI MODEL
// -------------------------------------------------------------

@Composable
private fun AccountAiTab(
    settings: CompanionSettings,
    detectedModels: List<com.example.data.model.DiscoveredModel>,
    isDetectingModels: Boolean,
    modelDetectionStatus: String?,
    onDetectModels: (String) -> Unit,
    showApiKey: Boolean,
    onToggleShowApiKey: () -> Unit,
    onUpdate: (CompanionSettings) -> Unit,
    onTestApiKey: (String, String) -> Unit,
    onClearChatClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    val firebaseUser = Firebase.auth.currentUser

    val rotateTransition = rememberInfiniteTransition(label = "detectRotate")
    val detectRotation by rotateTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing)
        ),
        label = "rotation"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PanelSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(RosePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (firebaseUser?.displayName?.take(1)
                                    ?: firebaseUser?.email?.take(1)
                                    ?: "U").uppercase(),
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = firebaseUser?.displayName ?: "Authenticated User",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MistText
                                )
                            )
                            Text(
                                text = firebaseUser?.email ?: "Google Account Linked",
                                style = MaterialTheme.typography.bodySmall.copy(color = DimText)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cloud Sync Active • Google Cloud Firestore",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = GoldAccent,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        // Custom API Key Card (Key Option)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PanelSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gemini API Key",
                            style = MaterialTheme.typography.labelLarge.copy(color = GoldAccent, fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Enter your personal Gemini API key, or leave blank to use the pre-configured project credentials.",
                        style = MaterialTheme.typography.bodySmall.copy(color = DimText)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = settings.apiKey,
                        onValueChange = { onUpdate(settings.copy(apiKey = it)) },
                        placeholder = { Text("AIzaSy... (leave blank for default key)", color = DimText) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_api_key"),
                        colors = outlinedFieldColors(),
                        singleLine = true,
                        visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = onToggleShowApiKey) {
                                Icon(
                                    imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Key Visibility",
                                    tint = DimText
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Auto-Detect Models button
                        Surface(
                            onClick = { onDetectModels(settings.apiKey) },
                            color = RosePrimary,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("detect_models_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Detect Models",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .rotate(if (isDetectingModels) detectRotation else 0f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isDetectingModels) "Connecting Google..." else "Auto-Detect Models",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        // Test API button
                        Surface(
                            onClick = { onTestApiKey(settings.apiKey, settings.model) },
                            color = PanelSurfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Ping Live",
                                    color = MistText,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }

                    // Live Google Connectivity status feedback
                    if (modelDetectionStatus != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DeepSpace)
                                .border(1.dp, if (detectedModels.isNotEmpty()) NeonEmerald.copy(alpha = 0.5f) else LineBorder, RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (detectedModels.isNotEmpty()) NeonEmerald else GoldAccent)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = modelDetectionStatus,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (detectedModels.isNotEmpty()) NeonEmerald else MistText,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Auto-Shift Fallback Protection Switch
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PanelSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Auto-Shift on Model Unavailability",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MistText
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "If selected model fails or becomes unavailable, Kayara auto-shifts to the next working Gemini model seamlessly.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DimText,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Switch(
                        checked = settings.autoModelFallback,
                        onCheckedChange = { onUpdate(settings.copy(autoModelFallback = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = RosePrimary,
                            uncheckedThumbColor = DimText,
                            uncheckedTrackColor = PanelSurfaceVariant
                        )
                    )
                }
            }
        }

        // Gemini AI Model Selection List
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PanelSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Gemini Models",
                                style = MaterialTheme.typography.labelLarge.copy(color = GoldAccent, fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (detectedModels.isNotEmpty()) "${detectedModels.size} models verified live via Google" else "Tap 'Auto-Detect Models' to scan Google live",
                                style = MaterialTheme.typography.bodySmall.copy(color = DimText, fontSize = 11.sp)
                            )
                        }

                        if (detectedModels.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NeonEmerald.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "● Live Google API",
                                    color = NeonEmerald,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Model list: Use detected models if present, else fallback candidates
                    val displayList = if (detectedModels.isNotEmpty()) {
                        detectedModels.map { it.id to (it.displayName.ifBlank { it.id } + if (it.latencyMs > 0) " (${it.latencyMs}ms)" else "") }
                    } else {
                        listOf(
                            "gemini-2.5-flash" to "Gemini 2.5 Flash (Ultra Fast & Adaptive)",
                            "gemini-2.0-flash" to "Gemini 2.0 Flash (Stable & Low Latency)",
                            "gemini-1.5-flash" to "Gemini 1.5 Flash (Lightweight & Efficient)",
                            "gemini-1.5-pro" to "Gemini 1.5 Pro (Deep Reasoning & Thought)"
                        )
                    }

                    displayList.forEach { (modelId, desc) ->
                        val isChosen = settings.model == modelId
                        Surface(
                            onClick = { onUpdate(settings.copy(model = modelId)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isChosen) PanelSurfaceVariant else DeepSpace,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isChosen) GoldAccent else LineBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, if (isChosen) GoldAccent else DimText, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isChosen) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(GoldAccent)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = modelId,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = if (isChosen) MistText else DimText,
                                            fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (isChosen) GoldAccent else MutedText,
                                            fontSize = 11.sp
                                        )
                                    )
                                }

                                if (isChosen) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(GoldAccent.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Active",
                                            color = GoldAccent,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Danger actions
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PanelSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Data & Account Actions",
                        style = MaterialTheme.typography.labelLarge.copy(color = GoldAccent, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        onClick = onClearChatClick,
                        color = DeepSpace,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = ErrorRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Reset Chat Timeline",
                                    style = MaterialTheme.typography.labelLarge.copy(color = ErrorRed, fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Erases conversation history, keeping brain memories safe",
                                    style = MaterialTheme.typography.bodySmall.copy(color = DimText, fontSize = 11.sp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        onClick = onSignOutClick,
                        color = DeepSpace,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                tint = RosePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Sign Out Account",
                                    style = MaterialTheme.typography.labelLarge.copy(color = RosePrimary, fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Safely sign out from Google Identity",
                                    style = MaterialTheme.typography.bodySmall.copy(color = DimText, fontSize = 11.sp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(40.dp)) }
    }
}

// -------------------------------------------------------------
// HELPER COMPOSABLES & DIALOGS
// -------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MemoryEditorDialog(
    title: String,
    initialFact: String,
    initialCategory: String,
    onConfirm: (fact: String, category: String) -> Unit,
    onDismiss: () -> Unit
) {
    var factText by remember { mutableStateOf(initialFact) }
    var selectedCategory by remember { mutableStateOf(initialCategory) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PanelSurface,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(color = MistText, fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column {
                Text(
                    text = "Fact or Preference:",
                    style = MaterialTheme.typography.labelMedium.copy(color = GoldAccent)
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = factText,
                    onValueChange = { factText = it },
                    placeholder = { Text("e.g. Loves black coffee without sugar", color = DimText) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("memory_dialog_input"),
                    colors = outlinedFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Category:",
                    style = MaterialTheme.typography.labelMedium.copy(color = GoldAccent)
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MEMORY_CATEGORIES.forEach { cat ->
                        val isChosen = selectedCategory.equals(cat, ignoreCase = true)
                        FilterChip(
                            selected = isChosen,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldAccent,
                                selectedLabelColor = DeepSpace,
                                containerColor = DeepSpace,
                                labelColor = MistText
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isChosen,
                                borderColor = if (isChosen) GoldAccent else LineBorder
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (factText.isNotBlank()) {
                        onConfirm(factText.trim(), selectedCategory)
                    }
                },
                enabled = factText.isNotBlank()
            ) {
                Text("Save", color = if (factText.isNotBlank()) RosePrimary else DimText, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MistText)
            }
        }
    )
}

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(RoseSubtle),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MistText
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(color = DimText)
            )
        }
    }
}

@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = DeepSpace,
    unfocusedContainerColor = DeepSpace,
    focusedBorderColor = GoldAccent,
    unfocusedBorderColor = LineBorder,
    focusedTextColor = MistText,
    unfocusedTextColor = MistText,
    cursorColor = GoldAccent
)
