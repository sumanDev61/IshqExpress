package com.live.ishqexpress.ui.onboarding

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.live.ishqexpress.ui.components.OnboardingFrame
import com.live.ishqexpress.ui.components.PrimaryButton
import com.live.ishqexpress.ui.components.SecondaryButton
import com.live.ishqexpress.ui.components.SelectableCard
import com.live.ishqexpress.ui.theme.BrandBrush
import com.live.ishqexpress.ui.theme.BrandPink
import com.live.ishqexpress.ui.theme.BrandViolet
import com.live.ishqexpress.ui.theme.Canvas
import com.live.ishqexpress.ui.theme.Hairline
import com.live.ishqexpress.ui.theme.Ink
import com.live.ishqexpress.ui.theme.InkSecondary
import com.live.ishqexpress.ui.theme.InkTertiary
import com.live.ishqexpress.ui.theme.LiveRed
import com.live.ishqexpress.ui.theme.PinkTint
import com.live.ishqexpress.ui.theme.SurfaceMuted
import com.live.ishqexpress.ui.theme.VioletTint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/* ============================== 4. Profile setup ============================== */

enum class Gender(val label: String, val emoji: String) { Female("Woman", "👩"), Male("Man", "👨"), Other("Other", "🌈") }

data class ProfileDraft(
    val photo: Uri? = null,
    val name: String = "",
    val gender: Gender? = null,
    val birthdayMillis: Long? = null,
)

private fun ageFrom(millis: Long): Int {
    val dob = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = millis }
    val now = Calendar.getInstance()
    var age = now.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
    if (now.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) age--
    return age
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    draft: ProfileDraft,
    onChange: (ProfileDraft) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    // Copy the picked photo into app storage: picker uris can stop working after a restart.
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onChange(draft.copy(photo = Uri.parse(com.live.ishqexpress.data.Session.importImage(context, uri))))
    }
    val age = draft.birthdayMillis?.let(::ageFrom)
    val underAge = age != null && age < 18
    val valid = draft.name.trim().length >= 2 && draft.gender != null && age != null && !underAge

    OnboardingFrame(
        title = "Create your profile",
        subtitle = "This is how people will see you on ishqexpress.",
        onBack = onBack,
        step = 1,
        footer = { PrimaryButton("Continue", onContinue, enabled = valid) }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(28.dp))

            // Photo
            Box(Modifier.align(Alignment.CenterHorizontally)) {
                Box(
                    Modifier
                        .size(116.dp)
                        .clip(CircleShape)
                        .background(PinkTint)
                        .drawBehind {
                            if (draft.photo == null) drawCircle(
                                color = BrandPink,
                                radius = size.minDimension / 2 - 2.dp.toPx(),
                                style = Stroke(1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f)))
                            )
                        }
                        .clickable { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    contentAlignment = Alignment.Center
                ) {
                    if (draft.photo != null) {
                        AsyncImage(draft.photo, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Icon(Icons.Rounded.Person, null, tint = BrandPink.copy(alpha = 0.45f), modifier = Modifier.size(56.dp))
                    }
                }
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Canvas)
                        .padding(3.dp)
                        .clip(CircleShape)
                        .background(BrandBrush.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.CameraAlt, "Add photo", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                if (draft.photo == null) "Add a photo — profiles with photos get 5× more views" else "Looking good!",
                style = MaterialTheme.typography.bodySmall,
                color = InkTertiary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(28.dp))
            FieldLabel("Display name")
            OutlinedTextField(
                value = draft.name,
                onValueChange = { onChange(draft.copy(name = it.take(24))) },
                placeholder = { Text("e.g. Aanya", color = InkTertiary) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                supportingText = { Text("${draft.name.length}/24", color = InkTertiary, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.End) },
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))
            FieldLabel("I am a")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Gender.entries.forEach { g ->
                    SelectableCard(
                        selected = draft.gender == g,
                        onClick = { onChange(draft.copy(gender = g)) },
                        modifier = Modifier
                            .weight(1f)
                            .height(84.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(g.emoji, fontSize = 24.sp)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                g.label,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (draft.gender == g) BrandPink else Ink
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            FieldLabel("Birthday")
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, if (underAge) LiveRed else Hairline, RoundedCornerShape(16.dp))
                    .clickable { showPicker = true }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    draft.birthdayMillis?.let {
                        SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(it)
                    } ?: "Select your date of birth",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (draft.birthdayMillis == null) InkTertiary else Ink,
                    modifier = Modifier.weight(1f)
                )
                if (age != null && !underAge) {
                    Text(
                        "$age yrs",
                        style = MaterialTheme.typography.labelMedium,
                        color = BrandPink,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(PinkTint)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                }
                Icon(Icons.Rounded.CalendarMonth, null, tint = InkTertiary)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                if (underAge) "You must be 18 or older to use ishqexpress."
                else "Only your age is shown — never your full birthday.",
                style = MaterialTheme.typography.bodySmall,
                color = if (underAge) LiveRed else InkTertiary
            )
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showPicker) {
        val defaultMonth = remember {
            Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { add(Calendar.YEAR, -22) }.timeInMillis
        }
        val state = rememberDatePickerState(
            initialSelectedDateMillis = draft.birthdayMillis,
            initialDisplayedMonthMillis = draft.birthdayMillis ?: defaultMonth,
            yearRange = 1940..Calendar.getInstance().get(Calendar.YEAR)
        )
        val pickerColors = DatePickerDefaults.colors(
            containerColor = Canvas,
            selectedDayContainerColor = BrandPink,
            todayDateBorderColor = BrandPink,
            todayContentColor = BrandPink,
            selectedYearContainerColor = BrandPink,
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onChange(draft.copy(birthdayMillis = state.selectedDateMillis))
                    showPicker = false
                }) { Text("Done", color = BrandPink, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel", color = InkSecondary) }
            },
            colors = pickerColors
        ) {
            DatePicker(state, colors = pickerColors, showModeToggle = true)
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, color = Ink, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = BrandPink,
    unfocusedBorderColor = Hairline,
    cursorColor = BrandPink,
    focusedTextColor = Ink,
    unfocusedTextColor = Ink,
)

/* ============================== 5. Interests ============================== */

data class Interest(val emoji: String, val label: String)

private val watchInterests = listOf(
    Interest("🎤", "Singing"), Interest("💃", "Dance"), Interest("🎵", "Music"), Interest("🎙️", "Talk shows"),
    Interest("😂", "Comedy"), Interest("🎮", "Gaming"), Interest("✍️", "Shayari"), Interest("🎬", "Bollywood"),
    Interest("🏏", "Cricket"), Interest("💪", "Fitness"), Interest("🍳", "Cooking"), Interest("🎨", "Art"),
)
private val meetInterests = listOf(
    Interest("💘", "Dating"), Interest("🤝", "Friendship"), Interest("💬", "Late-night chats"), Interest("✈️", "Travel"),
    Interest("👗", "Fashion"), Interest("🐶", "Pets"), Interest("🧘", "Spirituality"), Interest("🍿", "Movies"),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InterestsScreen(
    selected: Set<String>,
    onToggle: (String) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    val min = 3
    OnboardingFrame(
        title = "What are you into?",
        subtitle = "Pick at least $min — we'll tailor your live feed around them.",
        onBack = onBack,
        step = 2,
        footer = {
            PrimaryButton(
                if (selected.size >= min) "Continue  ·  ${selected.size} selected" else "Pick ${min - selected.size} more",
                onContinue,
                enabled = selected.size >= min
            )
        }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            SectionHeader("Watch", "What you love to watch live")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                watchInterests.forEach { InterestChip(it, it.label in selected) { onToggle(it.label) } }
            }
            Spacer(Modifier.height(28.dp))
            SectionHeader("Meet", "The kind of connections you want")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                meetInterests.forEach { InterestChip(it, it.label in selected) { onToggle(it.label) } }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String, caption: String) {
    Row(Modifier.padding(bottom = 12.dp), verticalAlignment = Alignment.Bottom) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = Ink)
        Spacer(Modifier.width(8.dp))
        Text(caption, style = MaterialTheme.typography.bodySmall, color = InkTertiary)
    }
}

@Composable
private fun InterestChip(interest: Interest, selected: Boolean, onClick: () -> Unit) {
    val textColor by animateColorAsState(if (selected) Color.White else Ink, label = "chipText")
    Row(
        Modifier
            .height(42.dp)
            .clip(CircleShape)
            .then(
                if (selected) Modifier.background(BrandBrush.primary)
                else Modifier
                    .background(Canvas)
                    .border(1.dp, Hairline, CircleShape)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(interest.emoji, fontSize = 16.sp)
        Spacer(Modifier.width(6.dp))
        Text(interest.label, style = MaterialTheme.typography.titleSmall, color = textColor)
        if (selected) {
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}

/* ============================== 6. Permissions primer ============================== */

private data class PermissionItem(
    val icon: ImageVector,
    val title: String,
    val body: String,
    val tint: Color,
    val bg: Color,
)

private val permissionItems = listOf(
    PermissionItem(Icons.Rounded.Videocam, "Camera", "Go live and join video calls with creators.", BrandPink, PinkTint),
    PermissionItem(Icons.Rounded.Mic, "Microphone", "Talk in audio rooms and live streams.", BrandViolet, VioletTint),
    PermissionItem(Icons.Rounded.NotificationsActive, "Notifications", "Know when people you follow go live.", Color(0xFFF79009), Color(0xFFFFF4E5)),
)

@Composable
fun PermissionsScreen(
    onBack: () -> Unit,
    onAllow: () -> Unit,
    onSkip: () -> Unit,
) {
    var granted by remember { mutableStateOf(setOf("Camera", "Microphone", "Notifications")) }
    val gate = com.live.ishqexpress.ui.common.rememberPermissionGate()
    val P = com.live.ishqexpress.ui.common.Perms
    // Ask Android for the switched-on ones; continue whatever the user answers.
    val allow = {
        val wanted = buildList {
            if ("Camera" in granted) add(P.camera)
            if ("Microphone" in granted) add(P.mic)
            if ("Notifications" in granted) P.notifications?.let { add(it) }
        }
        gate.request(wanted) { onAllow() }
    }

    OnboardingFrame(
        title = "A few quick permissions",
        subtitle = "So you can go live, chat on video and never miss a moment.",
        onBack = onBack,
        step = 3,
        footer = {
            PrimaryButton("Allow & start exploring", allow)
            SecondaryButton("Not now", onSkip)
        }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(28.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                permissionItems.forEach { item ->
                    val on = item.title in granted
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, Hairline, RoundedCornerShape(20.dp))
                            .clickable { granted = if (on) granted - item.title else granted + item.title }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(item.bg),
                            contentAlignment = Alignment.Center
                        ) { Icon(item.icon, null, tint = item.tint, modifier = Modifier.size(24.dp)) }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(item.title, style = MaterialTheme.typography.titleMedium, color = Ink)
                            Spacer(Modifier.height(2.dp))
                            Text(item.body, style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                        }
                        Spacer(Modifier.width(8.dp))
                        Switch(
                            checked = on,
                            onCheckedChange = { granted = if (it) granted + item.title else granted - item.title },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = BrandPink,
                                checkedThumbColor = Color.White,
                                uncheckedTrackColor = SurfaceMuted,
                                uncheckedBorderColor = Hairline,
                                uncheckedThumbColor = InkTertiary,
                            )
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceMuted)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Shield, null, tint = BrandPink, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    "We only use these while you're using a feature. You can change them anytime in Settings.",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
