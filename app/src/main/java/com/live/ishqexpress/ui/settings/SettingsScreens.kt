package com.live.ishqexpress.ui.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import com.live.ishqexpress.ui.common.*
import com.live.ishqexpress.ui.components.PrimaryButton
import com.live.ishqexpress.ui.home.LiveCreator
import com.live.ishqexpress.ui.home.compact
import com.live.ishqexpress.ui.home.sampleCreators
import com.live.ishqexpress.ui.nav.*
import com.live.ishqexpress.ui.theme.*
import com.live.ishqexpress.ui.home.LiveMode
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.delay

/* 67 Settings · 68 Help & support */

private enum class SettingsSheet { Language, Blocked, Delete, Logout }

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val nav = LocalNav.current
    val toast = LocalToast.current
    var sheet by remember { mutableStateOf<SettingsSheet?>(null) }
    var language by rememberSaveable { mutableStateOf("English") }
    var autoTranslate by rememberSaveable { mutableStateOf(true) }
    var privateAccount by rememberSaveable { mutableStateOf(false) }
    var hideLocation by rememberSaveable { mutableStateOf(false) }
    var invisible by rememberSaveable { mutableStateOf(false) }
    var liveAlerts by rememberSaveable { mutableStateOf(true) }
    var msgAlerts by rememberSaveable { mutableStateOf(true) }
    var giftAlerts by rememberSaveable { mutableStateOf(true) }
    var whoCanMessage by rememberSaveable { mutableIntStateOf(0) }

    ScreenScaffold("Settings", onBack) {
        LazyColumn(contentPadding = PaddingValues(bottom = 32.dp)) {
            item { Group("Account") }
            item { MenuRow(Icons.Rounded.Person, "Edit profile", onClick = { nav.push(Screen.EditProfile) }) }
            item { MenuRow(Icons.Rounded.Phone, "Phone number", trailingText = "+91 98••• ••210", onClick = {}) }
            item { MenuRow(Icons.Rounded.Link, "Linked accounts", trailingText = "Google", onClick = {}) }
            item { MenuRow(Icons.Rounded.Star, "My subscriptions", onClick = { nav.push(Screen.Subscriptions) }) }

            item { Group("Language & translation") }
            item { MenuRow(Icons.Rounded.Language, "App language", trailingText = language, onClick = { sheet = SettingsSheet.Language }) }
            item { ToggleRow(Icons.Rounded.Translate, "Auto-translate", autoTranslate, { autoTranslate = it }, subtitle = "Chats, live comments and calls") }

            item { Group("Privacy") }
            item { ToggleRow(Icons.Rounded.Lock, "Private account", privateAccount, { privateAccount = it }, subtitle = "Approve new followers") }
            item { ToggleRow(Icons.Rounded.LocationOff, "Hide my distance", hideLocation, { hideLocation = it }, subtitle = "Don't show me in Nearby") }
            item {
                ToggleRow(Icons.Rounded.VisibilityOff, "Invisible Mode", invisible, {
                    if (Me.vipTier >= 6) invisible = it else { toast("Invisible Mode unlocks at VIP 6"); nav.push(Screen.Vip) }
                }, subtitle = "Watch lives without appearing in viewer lists · VIP 6+")
            }
            item {
                MenuRow(Icons.Rounded.Chat, "Who can message me", trailingText = listOf("Everyone", "Followers", "Nobody")[whoCanMessage], onClick = { whoCanMessage = (whoCanMessage + 1) % 3 })
            }
            item { MenuRow(Icons.Rounded.Block, "Blocked users", trailingText = "3", onClick = { sheet = SettingsSheet.Blocked }) }

            item { Group("Notifications") }
            item { ToggleRow(Icons.Rounded.Videocam, "Creators going live", liveAlerts, { liveAlerts = it }) }
            item { ToggleRow(Icons.Rounded.ChatBubble, "Messages", msgAlerts, { msgAlerts = it }) }
            item { ToggleRow(Icons.Rounded.CardGiftcard, "Gifts and fans", giftAlerts, { giftAlerts = it }) }

            item { Group("Support") }
            item { MenuRow(Icons.Rounded.SupportAgent, "Help & support", onClick = { nav.push(Screen.Help) }) }
            item { MenuRow(Icons.Rounded.Shield, "Safety centre", onClick = { nav.push(Screen.Help) }) }
            item { MenuRow(Icons.Rounded.Description, "Terms & privacy policy", onClick = {}) }
            item { MenuRow(Icons.Rounded.Info, "About", trailingText = "v1.0 (1)") }

            item { Group("Testing") }
            item { MenuRow(Icons.Rounded.PhoneCallback, "Simulate incoming call", subtitle = "Preview screen 50", onClick = { nav.push(Screen.IncomingCall, 1) }) }

            item { Spacer(Modifier.height(16.dp)) }
            item { MenuRow(Icons.Rounded.Logout, "Log out", iconTint = InkSecondary, iconBg = SurfaceMuted, onClick = { sheet = SettingsSheet.Logout }) }
            item { MenuRow(Icons.Rounded.DeleteForever, "Delete account", iconTint = LiveRed, iconBg = LiveRed.copy(alpha = 0.1f), onClick = { sheet = SettingsSheet.Delete }) }
        }
    }

    when (sheet) {
        null -> {}
        SettingsSheet.Language -> IshqSheet({ sheet = null }, title = "App language") {
            listOf("English", "हिन्दी", "বাংলা", "தமிழ்", "తెలుగు", "मराठी", "ગુજરાતી", "اردو").forEach { l ->
                Row(Modifier.fillMaxWidth().clickable { language = l; sheet = null }.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(language == l, { language = l; sheet = null }, colors = RadioButtonDefaults.colors(selectedColor = BrandPink))
                    Text(l, style = MaterialTheme.typography.bodyLarge, color = Ink)
                }
            }
        }
        SettingsSheet.Blocked -> {
            val blocked = remember { mutableStateListOf(2, 8, 11) }
            IshqSheet({ sheet = null }, title = "Blocked users") {
                if (blocked.isEmpty()) EmptyState("🙌", "No one blocked", "People you block can't see your lives or message you.")
                blocked.toList().forEach { id ->
                    val c = creatorById(id)
                    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Avatar(c.imageUrl, 42.dp)
                        Spacer(Modifier.width(12.dp))
                        Text(c.name, style = MaterialTheme.typography.titleSmall, color = Ink, modifier = Modifier.weight(1f))
                        SmallOutlineButton("Unblock", { blocked.remove(id) }, height = 32.dp)
                    }
                }
            }
        }
        SettingsSheet.Logout -> IshqSheet({ sheet = null }) {
            Column(Modifier.padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Log out of ishqexpress?", style = MaterialTheme.typography.titleLarge, color = Ink)
                Spacer(Modifier.height(16.dp))
                PrimaryButton("Log out", { sheet = null; toast("Logged out (demo)") })
                SmallOutlineButton("Cancel", { sheet = null }, Modifier.fillMaxWidth().padding(top = 8.dp), height = 50.dp)
            }
        }
        SettingsSheet.Delete -> {
            var confirm by remember { mutableStateOf("") }
            IshqSheet({ sheet = null }, title = "Delete account") {
                Column(Modifier.padding(horizontal = 20.dp)) {
                    Text("This permanently deletes your profile, posts, chats and followers after 30 days. Unspent coins and unpaid diamonds are lost.", style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
                    Spacer(Modifier.height(12.dp))
                    IshqCard(Modifier.fillMaxWidth(), background = PinkTint) {
                        Row(verticalAlignment = Alignment.CenterVertically) { CoinAmount(Me.coins); Text("  coins   ", style = MaterialTheme.typography.bodySmall, color = InkSecondary); CoinAmount(Me.diamonds, diamond = true); Text("  diamonds", style = MaterialTheme.typography.bodySmall, color = InkSecondary) }
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(confirm, { confirm = it }, Modifier.fillMaxWidth(), label = { Text("Type DELETE to confirm") }, singleLine = true, shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LiveRed, unfocusedBorderColor = Hairline, focusedLabelColor = LiveRed))
                    Spacer(Modifier.height(14.dp))
                    Box(
                        Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(16.dp)).background(if (confirm == "DELETE") LiveRed else DisabledFill)
                            .clickable(enabled = confirm == "DELETE") { sheet = null; toast("Deletion scheduled (demo)") },
                        contentAlignment = Alignment.Center
                    ) { Text("Delete my account", style = MaterialTheme.typography.labelLarge, color = if (confirm == "DELETE") Color.White else InkTertiary) }
                }
            }
        }
    }
}

@Composable
private fun Group(title: String) {
    Text(title.uppercase(), style = MaterialTheme.typography.labelMedium, color = InkTertiary, modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 4.dp))
}

/* ============================== 68 · Help & support ============================== */

private data class SupportMsg(val mine: Boolean, val text: String, val options: List<String> = emptyList())

@Composable
fun HelpScreen(onBack: () -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    ScreenScaffold("Help & support", onBack) {
        UnderlineTabs(listOf("Chat with us", "FAQ"), tab, { tab = it })
        if (tab == 0) SupportChat() else Faq()
    }
}

@Composable
private fun ColumnScope.SupportChat() {
    val msgs = remember {
        mutableStateListOf(
            SupportMsg(false, "Hi ${Me.name} 👋 I'm Ishq Assistant. What do you need help with?", listOf("Coins not received", "Payout question", "Report a user", "Account & login"))
        )
    }
    var draft by remember { mutableStateOf("") }
    var typing by remember { mutableStateOf(false) }
    val state = rememberLazyListState()
    LaunchedEffect(msgs.size, typing) { state.animateScrollToItem((msgs.size + if (typing) 1 else 0).coerceAtLeast(1) - 1) }

    fun send(text: String) {
        msgs.add(SupportMsg(true, text))
        typing = true
    }
    LaunchedEffect(typing) {
        if (typing) {
            delay(1200)
            val last = msgs.last().text.lowercase()
            val reply = when {
                "coin" in last -> SupportMsg(false, "Sorry about that! Coins usually arrive within 5 minutes. If it's been longer, share your Google Play order ID (starts with GPA.) and we'll add them manually.", listOf("Find my order ID", "Talk to a person"))
                "payout" in last -> SupportMsg(false, "Payouts are processed on the 1st and arrive in 3–5 business days. Rejected payouts are usually a name mismatch with your KYC.", listOf("Check payout status", "Talk to a person"))
                "report" in last -> SupportMsg(false, "You can report anyone from their profile or the ⋯ menu in a live. For urgent safety issues we respond within 1 hour.", listOf("Open safety centre", "Talk to a person"))
                "person" in last -> SupportMsg(false, "Connecting you to our support team… Average wait is 4 minutes. You can leave this screen — we'll notify you.")
                else -> SupportMsg(false, "Got it. Can you tell me a bit more? A support agent will also see this conversation.", listOf("Talk to a person"))
            }
            msgs.add(reply)
            typing = false
        }
    }

    LazyColumn(Modifier.weight(1f).fillMaxWidth().background(SurfaceMuted.copy(alpha = 0.5f)), state = state, contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(msgs) { m ->
            Column(Modifier.fillMaxWidth(), horizontalAlignment = if (m.mine) Alignment.End else Alignment.Start) {
                Row(verticalAlignment = Alignment.Bottom) {
                    if (!m.mine) { Box(Modifier.size(30.dp).clip(CircleShape).background(BrandBrush.deep), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.SupportAgent, null, tint = Color.White, modifier = Modifier.size(18.dp)) }; Spacer(Modifier.width(6.dp)) }
                    Text(
                        m.text, style = MaterialTheme.typography.bodyMedium, color = if (m.mine) Color.White else Ink,
                        modifier = Modifier.widthIn(max = 280.dp).clip(RoundedCornerShape(18.dp)).then(if (m.mine) Modifier.background(BrandBrush.primary) else Modifier.background(Canvas)).padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
                if (m.options.isNotEmpty() && m == msgs.last()) {
                    Spacer(Modifier.height(8.dp))
                    FlowRow(Modifier.padding(start = 36.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        m.options.forEach { o ->
                            Text(o, style = MaterialTheme.typography.labelMedium, color = BrandPink, modifier = Modifier.clip(CircleShape).border(1.dp, BrandPink, CircleShape).clickable(enabled = !typing) { send(o) }.padding(horizontal = 12.dp, vertical = 7.dp))
                        }
                    }
                }
            }
        }
        if (typing) item {
            Text("Ishq Assistant is typing…", style = MaterialTheme.typography.bodySmall, color = InkTertiary, modifier = Modifier.padding(start = 36.dp))
        }
    }
    Row(Modifier.fillMaxWidth().imePadding().navigationBarsPadding().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Row(Modifier.weight(1f).height(46.dp).clip(CircleShape).background(SurfaceMuted).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) {
                if (draft.isEmpty()) Text("Describe your issue…", style = MaterialTheme.typography.bodyMedium, color = InkTertiary)
                BasicTextField(draft, { draft = it }, singleLine = true, textStyle = MaterialTheme.typography.bodyMedium.copy(color = Ink), cursorBrush = androidx.compose.ui.graphics.SolidColor(BrandPink), modifier = Modifier.fillMaxWidth())
            }
            Icon(Icons.Rounded.AttachFile, "Attach screenshot", tint = InkTertiary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier.size(46.dp).clip(CircleShape).background(if (draft.isBlank()) Brush.linearGradient(listOf(SurfaceMuted, SurfaceMuted)) else BrandBrush.primary)
                .clickable(enabled = draft.isNotBlank() && !typing) { send(draft.trim()); draft = "" },
            contentAlignment = Alignment.Center
        ) { Icon(Icons.AutoMirrored.Rounded.Send, "Send", tint = if (draft.isBlank()) InkTertiary else Color.White) }
    }
}

@Composable
private fun Faq() {
    val faqs = listOf(
        "How do I buy coins?" to "Open Me → Coins → Recharge, pick a pack and pay with Google Play. Coins on the web give about 20% more.",
        "What's the difference between coins and diamonds?" to "Viewers buy coins to send gifts. Creators receive diamonds — about 1 diamond for every 2 coins — and can withdraw them as money.",
        "How do I become a creator?" to "Tap the camera button and go live. To withdraw earnings you need to finish ID verification (KYC) once.",
        "When are payouts sent?" to "Withdrawal requests are processed on the 1st of each month and arrive in 3–5 business days.",
        "How does VIP work?" to "VIP is based on coins spent in the last 30 days. There are 13 tiers; your tier is recalculated every 30 days.",
        "How do I report someone?" to "Open their profile or tap ⋯ in a live and choose Report. Reports are anonymous.",
        "How do I delete my account?" to "Settings → Delete account. Deletion completes after 30 days; you can cancel by logging back in.",
    )
    var open by remember { mutableIntStateOf(-1) }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        itemsIndexed(faqs) { i, (q, a) ->
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(if (open == i) PinkTint else SurfaceMuted).clickable { open = if (open == i) -1 else i }.padding(16.dp).animateContentSize()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(q, style = MaterialTheme.typography.titleSmall, color = Ink, modifier = Modifier.weight(1f))
                    Icon(if (open == i) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, null, tint = InkSecondary)
                }
                if (open == i) { Spacer(Modifier.height(8.dp)); Text(a, style = MaterialTheme.typography.bodyMedium, color = InkSecondary) }
            }
        }
    }
}
