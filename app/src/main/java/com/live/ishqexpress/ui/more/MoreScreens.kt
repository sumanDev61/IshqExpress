package com.live.ishqexpress.ui.more

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.live.ishqexpress.ui.common.*
import com.live.ishqexpress.ui.components.BrandLogo
import com.live.ishqexpress.ui.components.PrimaryButton
import com.live.ishqexpress.ui.components.Wordmark
import com.live.ishqexpress.ui.home.compact
import com.live.ishqexpress.ui.home.sampleCreators
import com.live.ishqexpress.ui.live.ChatFeed
import com.live.ishqexpress.ui.nav.LocalNav
import com.live.ishqexpress.ui.nav.LocalToast
import com.live.ishqexpress.ui.nav.Screen
import com.live.ishqexpress.ui.theme.*
import kotlinx.coroutines.delay

/*
 * Screens that earlier taps led nowhere to: phone number, linked accounts, legal, about,
 * safety centre, payment methods, KYC capture, ranking rules, replay player, agency statement
 * and the official-talent application.
 */

/* ============================== Phone number ============================== */

@Composable
fun PhoneNumberScreen(onBack: () -> Unit) {
    val toast = LocalToast.current
    var step by rememberSaveable { mutableIntStateOf(0) } // 0 current · 1 new number · 2 otp
    var number by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    ScreenScaffold("Phone number", onBack) {
        Column(Modifier.padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            when (step) {
                0 -> {
                    IshqCard(Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconTile3D(Icons.Rounded.Phone, Success, size = 44.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(com.live.ishqexpress.data.Session.maskedPhone(), style = MaterialTheme.typography.titleMedium, color = Ink)
                                Text("Verified · used to sign in and for payouts", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                            }
                            Icon(Icons.Rounded.Verified, null, tint = Success)
                        }
                    }
                    Text("Changing your number signs you out on other devices. Your coins, diamonds and followers stay with your account.", style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
                    PrimaryButton("Change number", onClick = { step = 1 })
                }
                1 -> {
                    Text("Enter your new number", style = MaterialTheme.typography.titleLarge, color = Ink)
                    FieldBox("New mobile number", number, { if (it.length <= 10 && it.all(Char::isDigit)) number = it }, prefix = "🇮🇳 +91", keyboard = KeyboardType.Phone)
                    PrimaryButton("Send code", onClick = { step = 2; toast("Code sent to +91 $number") }, enabled = number.length == 10)
                }
                else -> {
                    Text("Enter the 6-digit code", style = MaterialTheme.typography.titleLarge, color = Ink)
                    Text("Sent to +91 $number", style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
                    FieldBox("Code", code, { if (it.length <= 6 && it.all(Char::isDigit)) code = it }, keyboard = KeyboardType.NumberPassword)
                    PrimaryButton("Verify & update", onClick = { com.live.ishqexpress.data.Session.setString("phone", "+91 $number"); toast("Phone number updated"); onBack() }, enabled = code.length == 6)
                }
            }
        }
    }
}

/* ============================== Linked accounts ============================== */

@Composable
fun LinkedAccountsScreen(onBack: () -> Unit) {
    val toast = LocalToast.current
    val linked = remember { mutableStateMapOf("Google" to true, "Apple" to false, "Facebook" to false, "Email" to false) }
    val meta = mapOf(
        "Google" to (Icons.Rounded.AlternateEmail to Color(0xFF4285F4)),
        "Apple" to (Icons.Rounded.PhoneIphone to Ink),
        "Facebook" to (Icons.Rounded.Public to Color(0xFF1877F2)),
        "Email" to (Icons.Rounded.Email to BrandPink),
    )
    ScreenScaffold("Linked accounts", onBack) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Link a backup sign-in so you never lose your account if you change phones.", style = MaterialTheme.typography.bodyMedium, color = InkSecondary, modifier = Modifier.padding(horizontal = 4.dp))
            IshqCard(Modifier.fillMaxWidth(), padding = 4.dp) {
                linked.keys.toList().forEach { k ->
                    val (icon, color) = meta.getValue(k)
                    MenuRow(icon, k, subtitle = if (linked.getValue(k)) "Linked" else "Not linked", iconTint = color, trailing = {
                        if (linked.getValue(k)) SmallOutlineButton("Unlink", {
                            if (linked.values.count { it } == 1) toast("Keep at least one backup sign-in") else { linked[k] = false; toast("$k unlinked") }
                        }, height = 32.dp)
                        else SmallGradientButton("Link", { linked[k] = true; toast("$k linked") }, height = 32.dp)
                    })
                }
            }
        }
    }
}

/* ============================== Legal ============================== */

@Composable
fun LegalScreen(startTab: Int, onBack: () -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(startTab) }
    val docs = listOf(
        "Terms" to listOf(
            "Who can use ishqexpress" to "You must be 18 or older. One account per person; accounts can't be sold or transferred.",
            "Coins and diamonds" to "Coins are bought in the app and spent on gifts, tickets and subscriptions. Creators receive diamonds, which can be withdrawn after verification. Coins are not refundable once spent.",
            "Live content" to "You are responsible for what you broadcast. Nudity, violence, hate, gambling promotion and sharing contact details to move payments off the app are not allowed.",
            "Suspension" to "We may limit or close accounts that break these terms. Withheld diamonds from fraud are not paid out.",
        ),
        "Privacy" to listOf(
            "What we collect" to "Phone number, profile details, the content you post and go live with, purchase history and device information for security.",
            "How it is used" to "To run your account, recommend lives, keep people safe, process payments and payouts, and meet legal requirements.",
            "Your controls" to "Hide your distance, go private, choose who can message you, download your data or delete your account from Settings.",
            "Payout documents" to "PAN and Aadhaar are stored encrypted and used only for identity and tax checks.",
        ),
        "Community" to listOf(
            "Be kind" to "No harassment, bullying, threats or hate speech in lives, comments or messages.",
            "Keep it safe" to "No sexual content, no minors on camera, no self-harm or dangerous challenges.",
            "Keep it real" to "No impersonation, fake giveaways or spam. Don't ask viewers for money outside the app.",
            "Report it" to "Long-press a message or use ⋯ on a profile or live to report. Our team reviews reports 24×7.",
        ),
    )
    ScreenScaffold("Terms & policies", onBack) {
        UnderlineTabs(docs.map { it.first }, tab, { tab = it })
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Text("Last updated 1 Nov 2026", style = MaterialTheme.typography.labelMedium, color = InkTertiary) }
            itemsIndexed(docs[tab].second) { i, (h, body) ->
                IshqCard(Modifier.fillMaxWidth()) {
                    Text("${i + 1}. $h", style = MaterialTheme.typography.titleSmall, color = Ink)
                    Spacer(Modifier.height(6.dp))
                    Text(body, style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
                }
            }
        }
    }
}

/* ============================== About ============================== */

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val nav = LocalNav.current
    val toast = LocalToast.current
    val context = androidx.compose.ui.platform.LocalContext.current
    var licences by remember { mutableStateOf(false) }
    if (licences) LicencesSheet { licences = false }
    ScreenScaffold("About", onBack) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(16.dp))
            BrandLogo(84.dp, elevation = 12.dp)
            Spacer(Modifier.height(12.dp))
            Wordmark(28.sp)
            Text("Version 1.0 (1)", style = MaterialTheme.typography.bodySmall, color = InkTertiary)
            Spacer(Modifier.height(20.dp))
            IshqCard(Modifier.fillMaxWidth(), padding = 4.dp) {
                MenuRow(Icons.Rounded.SystemUpdate, "Check for updates", subtitle = "You're on the latest version", iconTint = Success, onClick = { openPlayStore(context) })
                MenuRow(Icons.Rounded.StarRate, "Rate ishqexpress", iconTint = Gold, onClick = { openPlayStore(context) })
                MenuRow(Icons.Rounded.Description, "Terms of service", iconTint = BrandViolet, onClick = { nav.push(Screen.Legal, 0) })
                MenuRow(Icons.Rounded.PrivacyTip, "Privacy policy", iconTint = BrandOrchid, onClick = { nav.push(Screen.Legal, 1) })
                MenuRow(Icons.Rounded.Code, "Open-source licences", iconTint = InkSecondary, onClick = { licences = true })
            }
            Spacer(Modifier.height(20.dp))
            Text("Made with ❤️ in India", style = MaterialTheme.typography.bodySmall, color = InkTertiary)
        }
    }
}

/* ============================== Safety centre ============================== */

@Composable
fun SafetyCentreScreen(onBack: () -> Unit) {
    val nav = LocalNav.current
    val toast = LocalToast.current
    var filterWords by com.live.ishqexpress.data.rememberFlag("filterWords", true)
    var limitStrangers by com.live.ishqexpress.data.rememberFlag("limitStrangers", false)
    var showBlocked by remember { mutableStateOf(false) }
    var showReports by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    if (showBlocked) com.live.ishqexpress.ui.settings.BlockedUsersSheet { showBlocked = false }
    if (showReports) MyReportsSheet { showReports = false }
    ScreenScaffold("Safety centre", onBack) {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                GradientCard(Modifier.fillMaxWidth(), brush = Brush.linearGradient(listOf(Color(0xFF12B76A), Color(0xFF0BA5A5)))) {
                    Column {
                        Text("You're in control", style = MaterialTheme.typography.titleLarge, color = Color.White)
                        Spacer(Modifier.height(4.dp))
                        Text("Our team reviews reports around the clock. Tools below help you decide who reaches you.", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))
                    }
                }
            }
            item {
                IshqCard(Modifier.fillMaxWidth(), padding = 4.dp) {
                    ToggleRow(Icons.Rounded.FilterAlt, "Filter offensive words", filterWords, { filterWords = it }, subtitle = "Hide them in your lives and messages")
                    ToggleRow(Icons.Rounded.PersonOff, "Limit strangers", limitStrangers, { limitStrangers = it }, subtitle = "Only followers can comment and message")
                    MenuRow(Icons.Rounded.Block, "Blocked users", trailingText = "${com.live.ishqexpress.data.Session.blocked.size}", iconTint = LiveRed, onClick = { showBlocked = true })
                    MenuRow(Icons.Rounded.Flag, "My reports", subtitle = "2 reviewed · 0 open", iconTint = Gold, onClick = { showReports = true })
                }
            }
            item { Text("Stay safe", style = MaterialTheme.typography.titleMedium, color = Ink, modifier = Modifier.padding(top = 6.dp, start = 4.dp)) }
            items(listOf(
                Triple(Icons.Rounded.Payments, "Never pay outside the app", "Anyone asking for UPI or gift cards outside ishqexpress is a scam."),
                Triple(Icons.Rounded.Lock, "Keep personal details private", "Don't share your address, school or phone number on live."),
                Triple(Icons.Rounded.Videocam, "Video calls are private", "Recording others without consent is not allowed and can be reported."),
                Triple(Icons.Rounded.Psychology, "Need to talk?", "Tele-MANAS 14416 · free, 24×7 mental-health support in India."),
            )) { (icon, t, b) ->
                IshqCard(Modifier.fillMaxWidth()) {
                    Row {
                        IconTile3D(icon, BrandPink, size = 38.dp)
                        Spacer(Modifier.width(12.dp))
                        Column { Text(t, style = MaterialTheme.typography.titleSmall, color = Ink); Text(b, style = MaterialTheme.typography.bodySmall, color = InkSecondary) }
                    }
                }
            }
            item { PrimaryButton("Contact the safety team", onClick = { nav.push(Screen.Help) }) }
        }
    }
}

/* ============================== Payment methods ============================== */

private data class PayMethod(val id: Int, val kind: String, val label: String, val detail: String)

@Composable
fun PaymentMethodsScreen(onBack: () -> Unit) {
    val toast = LocalToast.current
    val methods = remember { mutableStateListOf(PayMethod(1, "UPI", "suman@okhdfc", "Verified name: Suman"), PayMethod(2, "Bank", "HDFC Bank ••4821", "IFSC HDFC0001234")) }
    var default by rememberSaveable { mutableIntStateOf(1) }
    var adding by remember { mutableStateOf<String?>(null) }
    ScreenScaffold("Payout methods", onBack) {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(methods, key = { it.id }) { m ->
                val on = m.id == default
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(SurfaceMuted.copy(alpha = 0.8f))
                        .border(if (on) 2.dp else 1.dp, if (on) BrandPink else Hairline, RoundedCornerShape(18.dp))
                        .clickable { default = m.id }.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconTile3D(if (m.kind == "UPI") Icons.Rounded.QrCode2 else Icons.Rounded.AccountBalance, if (m.kind == "UPI") BrandViolet else BattleBlue, size = 42.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(m.label, style = MaterialTheme.typography.titleSmall, color = Ink)
                        Text("${m.kind} · ${m.detail}", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                    }
                    if (on) Text("Default", style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.clip(CircleShape).background(BrandBrush.primary).padding(horizontal = 8.dp, vertical = 3.dp))
                    else Icon(Icons.Rounded.DeleteOutline, "Remove", tint = InkTertiary, modifier = Modifier.clickable { methods.remove(m); toast("Removed") })
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SmallOutlineButton("Add UPI", { adding = "UPI" }, Modifier.weight(1f), icon = Icons.Rounded.Add, height = 46.dp, color = BrandPink)
                    SmallOutlineButton("Add bank", { adding = "Bank" }, Modifier.weight(1f), icon = Icons.Rounded.Add, height = 46.dp, color = BrandPink)
                }
            }
            item { Text("Payouts go to your default method within 2 working days. The account name must match your verified ID.", style = MaterialTheme.typography.bodySmall, color = InkTertiary) }
        }
    }
    adding?.let { kind ->
        var a by remember { mutableStateOf("") }
        var b by remember { mutableStateOf("") }
        IshqSheet(onDismiss = { adding = null }, title = if (kind == "UPI") "Add UPI ID" else "Add bank account") {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FieldBox(if (kind == "UPI") "UPI ID (name@bank)" else "Account number", a, { a = it }, keyboard = if (kind == "UPI") KeyboardType.Email else KeyboardType.Number)
                if (kind == "Bank") FieldBox("IFSC code", b, { b = it.uppercase() })
                PrimaryButton("Verify & save", onClick = {
                    methods.add(PayMethod((methods.maxOfOrNull { it.id } ?: 0) + 1, kind, if (kind == "UPI") a else "Bank ••${a.takeLast(4)}", if (kind == "UPI") "Verifying name…" else "IFSC $b"))
                    adding = null; toast("$kind added")
                }, enabled = a.length >= 4 && (kind == "UPI" && "@" in a || kind == "Bank" && b.length == 11))
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

/* ============================== KYC capture ============================== */

@Composable
fun KycCaptureScreen(selfie: Boolean, onBack: () -> Unit) {
    DarkSystemBars()
    val toast = LocalToast.current
    var side by rememberSaveable { mutableIntStateOf(0) } // document: 0 front · 1 back
    var captured by rememberSaveable { mutableStateOf(false) }
    var checking by remember { mutableStateOf(false) }
    LaunchedEffect(checking) { if (checking) { delay(1500); checking = false; com.live.ishqexpress.data.Session.completeKyc(if (selfie) "selfie" else "doc"); toast(if (selfie) "Selfie verified" else "Document submitted for review"); onBack() } }
    val title = if (selfie) "Liveness check" else if (side == 0) "Front of Aadhaar / passport" else "Back of Aadhaar / passport"
    // The captured frame (camera snapshot or a gallery photo) is frozen on screen until Retake.
    // In production these images are uploaded to the KYC provider, not kept on the phone.
    val camera = remember { CameraHandle() }
    var shot by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    var picked by remember { mutableStateOf<String?>(null) }
    val pickDoc = rememberImagePicker { picked = it; captured = true }
    RequestOnEnter(listOf(Perms.camera)) { toast("Allow the camera, or upload a photo from your gallery") }
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        when {
            captured && picked != null -> AsyncImage(picked, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            captured && shot != null -> androidx.compose.foundation.Image(shot!!, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            else -> CameraBackdrop(front = selfie, tint = violetPair, fallbackUrl = photo(if (selfie) 77 else 91, 900, 1600), handle = camera, fx = false)
        }
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                GlassButton(Icons.AutoMirrored.Rounded.ArrowBack, "Back", onBack)
                Spacer(Modifier.width(10.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White, modifier = Modifier.weight(1f))
            }
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                // Guide frame: oval for a face, card for a document.
                Box(
                    Modifier.then(if (selfie) Modifier.size(240.dp, 310.dp) else Modifier.fillMaxWidth(0.86f).aspectRatio(1.586f))
                        .drawBehind {
                            val stroke = Stroke(4.dp.toPx(), pathEffect = if (captured) null else PathEffect.dashPathEffect(floatArrayOf(24f, 14f)))
                            val c = if (captured) Success else Color.White
                            if (selfie) drawOval(c, style = stroke) else drawRoundRect(c, cornerRadius = CornerRadius(18.dp.toPx()), style = stroke)
                        }
                )
            }
            Text(
                if (selfie) "Keep your face inside the oval, then blink slowly" else "Place the card inside the frame · avoid glare",
                style = MaterialTheme.typography.bodyMedium, color = Color.White, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            )
            Spacer(Modifier.height(20.dp))
            if (!captured) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(76.dp).clip(CircleShape).border(4.dp, Color.White, CircleShape).padding(6.dp).clip(CircleShape).background(Color.White).clickable {
                        shot = camera.snapshot()?.asImageBitmap(); picked = null; captured = true
                    })
                    if (!selfie) Column(Modifier.align(Alignment.CenterEnd).padding(end = 28.dp).clip(RoundedCornerShape(12.dp)).clickable { pickDoc() }.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.PhotoLibrary, "Upload from gallery", tint = Color.White, modifier = Modifier.size(28.dp))
                        Text("Gallery", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                }
            } else {
                Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SmallOutlineButton("Retake", { captured = false; shot = null; picked = null }, Modifier.weight(1f), height = 50.dp, color = Color.White)
                    SmallGradientButton(
                        if (checking) "Checking…" else if (!selfie && side == 0) "Next: back side" else "Submit",
                        { if (!selfie && side == 0) { side = 1; captured = false; shot = null; picked = null } else checking = true }, Modifier.weight(1f), height = 50.dp
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

/* ============================== Ranking rules ============================== */

@Composable
fun RankingRulesScreen(onBack: () -> Unit) {
    ScreenScaffold("Ranking rules", onBack) {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                IshqCard(Modifier.fillMaxWidth()) {
                    Text("How it's counted", style = MaterialTheme.typography.titleMedium, color = Ink)
                    Spacer(Modifier.height(8.dp))
                    listOf(
                        "Top creators" to "Diamonds received from gifts, tickets and subscriptions.",
                        "Top gifters" to "Coins spent on gifts. Free Bag gifts don't count.",
                        "Periods" to "Hourly resets on the hour, daily at midnight IST, weekly on Monday, monthly on the 1st.",
                        "Fair play" to "Gifts between linked or agency-owned accounts are removed from rankings.",
                    ).forEach { (h, b) ->
                        Row(Modifier.padding(vertical = 5.dp)) {
                            Box(Modifier.padding(top = 6.dp).size(6.dp).clip(CircleShape).background(BrandPink))
                            Spacer(Modifier.width(10.dp))
                            Column { Text(h, style = MaterialTheme.typography.titleSmall, color = Ink); Text(b, style = MaterialTheme.typography.bodySmall, color = InkSecondary) }
                        }
                    }
                }
            }
            item { Text("Weekly rewards", style = MaterialTheme.typography.titleMedium, color = Ink) }
            items(listOf(
                Triple("🥇", "Rank 1", "50,000 coins · golden frame 7 days · home banner"),
                Triple("🥈", "Rank 2", "25,000 coins · silver frame 7 days"),
                Triple("🥉", "Rank 3", "10,000 coins · bronze frame 7 days"),
                Triple("🏅", "Rank 4–10", "2,000 coins · ranking badge"),
                Triple("✨", "Rank 11–50", "Entry effect for 3 days"),
            )) { (e, r, p) ->
                IshqCard(Modifier.fillMaxWidth(), padding = 14.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(e, fontSize = 26.sp)
                        Spacer(Modifier.width(12.dp))
                        Column { Text(r, style = MaterialTheme.typography.titleSmall, color = Ink); Text(p, style = MaterialTheme.typography.bodySmall, color = InkSecondary) }
                    }
                }
            }
        }
    }
}

/* ============================== Replay player ============================== */

@Composable
fun ReplayPlayerScreen(creatorId: Int, onBack: () -> Unit) {
    DarkSystemBars()
    val toast = LocalToast.current
    val c = sampleCreators.firstOrNull { it.id == creatorId } ?: sampleCreators.first()
    val context = androidx.compose.ui.platform.LocalContext.current
    val total = 4_368 // 1:12:48
    var pos by rememberSaveable { mutableIntStateOf(0) }
    var playing by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(playing) { while (playing && pos < total) { delay(1000); pos += 1 } }
    val comments = remember {
        listOf(LiveComment("Neha", 21, "This song 😍"), LiveComment("Amit K", 44, "sent Rose ×10", gift = "🌹"), LiveComment("Vikram", 12, "Please sing Tum Hi Ho"), LiveComment("Sana", 19, "Voice is magic ✨"))
    }
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        VideoBackdrop(c.imageUrl, c.tint)
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                GlassButton(Icons.AutoMirrored.Rounded.ArrowBack, "Back", onBack)
                Spacer(Modifier.width(10.dp))
                Avatar(c.imageUrl, 36.dp)
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(c.name, style = MaterialTheme.typography.titleSmall, color = Color.White)
                    Text("Replay · Sunday unplugged · 2 days ago", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                }
                GlassButton(Icons.Rounded.Share, "Share", { shareText(context, "Watch ${c.name}'s replay on ishqexpress ${AppLinks.replay(c.id)}") })
            }
            Spacer(Modifier.weight(1f))
            ChatFeed(comments, Modifier.height(170.dp).padding(start = 12.dp, end = 80.dp))
            Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Slider(
                    pos.toFloat(), { pos = it.toInt() }, valueRange = 0f..total.toFloat(),
                    colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = BrandPink, inactiveTrackColor = Color.White.copy(alpha = 0.25f))
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(com.live.ishqexpress.ui.golive.formatClock(pos), style = MaterialTheme.typography.labelMedium, color = Color.White)
                    Spacer(Modifier.weight(1f))
                    GlassButton(Icons.Rounded.Replay10, "Back 10 s", { pos = (pos - 10).coerceAtLeast(0) })
                    Spacer(Modifier.width(12.dp))
                    Bubble3D(Ramps.live, Modifier.size(58.dp).clickable { playing = !playing }, CircleShape) {
                        BubbleGlyph(if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, 30.dp, if (playing) "Pause" else "Play")
                    }
                    Spacer(Modifier.width(12.dp))
                    GlassButton(Icons.Rounded.Forward10, "Forward 10 s", { pos = (pos + 10).coerceAtMost(total) })
                    Spacer(Modifier.weight(1f))
                    Text(com.live.ishqexpress.ui.golive.formatClock(total), style = MaterialTheme.typography.labelMedium, color = Color.White)
                }
            }
        }
    }
}

/* ============================== Agency statement ============================== */

@Composable
fun AgencyStatementScreen(month: Int, onBack: () -> Unit) {
    val toast = LocalToast.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val months = listOf("November 2026", "October 2026", "September 2026", "August 2026")
    val m = months[month.coerceIn(0, months.lastIndex)]
    val rows = sampleCreators.take(7).mapIndexed { i, c -> Triple(c, (c.viewers * (9 - i)) * 3, 10 + (i % 3)) }
    val totalDiamonds = rows.sumOf { it.second }
    val commission = totalDiamonds / 100 * 10 / 10 // ₹ at 1 diamond = ₹0.1, 10%
    ScreenScaffold(m, onBack, actions = { TopAction(Icons.Rounded.Download, "Download") {
        val ok = savePdfToDownloads(
            context, "ishqexpress-agency-${m.replace(' ', '-').lowercase()}", "Agency statement · $m",
            "Commission ₹${commission.grouped()} · ${totalDiamonds.grouped()} diamonds · ${rows.size} creators",
            rows.map { (c, d, hrs) -> "${c.name} · ${hrs * 3} h live" to "${d.grouped()} 💎 · ₹${(d / 100).grouped()}" }
        )
        toast(if (ok) "Statement PDF saved to Downloads" else "Couldn't save the PDF")
    } }) {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                GradientCard(Modifier.fillMaxWidth(), brush = Brush.linearGradient(listOf(Color(0xFF5B7BEA), Color(0xFF7A2BE8)))) {
                    Row {
                        Stat("₹${commission.grouped()}", "Commission", Modifier.weight(1f), color = Color.White, labelColor = Color.White.copy(alpha = 0.8f))
                        Stat(totalDiamonds.compact(), "Diamonds", Modifier.weight(1f), color = Color.White, labelColor = Color.White.copy(alpha = 0.8f))
                        Stat("${rows.size}", "Creators", Modifier.weight(1f), color = Color.White, labelColor = Color.White.copy(alpha = 0.8f))
                    }
                }
            }
            item {
                Text(if (month == 0) "In progress · pays 5 Dec" else "Paid to HDFC ••4821", style = MaterialTheme.typography.labelMedium, color = if (month == 0) Gold else Success)
            }
            items(rows) { (c, d, hrs) ->
                IshqCard(Modifier.fillMaxWidth(), padding = 12.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Avatar(c.imageUrl, 40.dp)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(c.name, style = MaterialTheme.typography.titleSmall, color = Ink)
                            Text("${hrs * 3} h live · ${hrs} valid days", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            CoinAmount(d, diamond = true)
                            Text("₹${(d / 100).grouped()}", style = MaterialTheme.typography.labelSmall, color = Success)
                        }
                    }
                }
            }
        }
    }
}

/* ============================== Official talent ============================== */

@Composable
fun OfficialTalentScreen(onBack: () -> Unit) {
    val toast = LocalToast.current
    var submitted by rememberSaveable { mutableStateOf(false) }
    var category by rememberSaveable { mutableIntStateOf(0) }
    var link by rememberSaveable { mutableStateOf("") }
    val requirements = listOf(
        "Level 10 or above" to (Me.level >= 10),
        "Verified phone number" to true,
        "20 hours live in the last 30 days" to false,
        "No safety strikes" to true,
    )
    ScreenScaffold("Official talent", onBack) {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                GradientCard(Modifier.fillMaxWidth(), brush = Brush.linearGradient(listOf(Color(0xFFFF4FB8), Color(0xFFB45CFF), Color(0xFF39C5FF)))) {
                    Column {
                        Text("🌟 Become an official talent", style = MaterialTheme.typography.titleLarge, color = Color.White)
                        Spacer(Modifier.height(4.dp))
                        Text("Get a verified badge, priority on Home, a fixed hourly bonus and a personal manager.", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.92f))
                    }
                }
            }
            item {
                IshqCard(Modifier.fillMaxWidth()) {
                    Text("Requirements", style = MaterialTheme.typography.titleMedium, color = Ink)
                    Spacer(Modifier.height(8.dp))
                    requirements.forEach { (r, ok) ->
                        Row(Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (ok) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked, null, tint = if (ok) Success else InkTertiary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(r, style = MaterialTheme.typography.bodyMedium, color = if (ok) Ink else InkSecondary)
                        }
                    }
                }
            }
            if (!submitted) {
                item {
                    IshqCard(Modifier.fillMaxWidth()) {
                        Text("Your talent", style = MaterialTheme.typography.titleMedium, color = Ink)
                        Spacer(Modifier.height(10.dp))
                        sampleCategories.chunked(3).forEach { row ->
                            Row(Modifier.padding(vertical = 3.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                row.forEach { c ->
                                    val i = sampleCategories.indexOf(c)
                                    PillChip("${c.emoji} ${c.label}", i == category, { category = i }, Modifier.weight(1f))
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        FieldBox("Link to a sample (Instagram, YouTube…)", link, { link = it }, keyboard = KeyboardType.Uri)
                    }
                }
                item { PrimaryButton("Submit application", onClick = { submitted = true; toast("Application sent") }, enabled = link.length > 5) }
            } else {
                item {
                    IshqCard(Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconTile3D(Icons.Rounded.HourglassTop, Gold, size = 44.dp)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Under review", style = MaterialTheme.typography.titleMedium, color = Ink)
                                Text("We reply within 3 working days in Messages.", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ============================== Location picker (sheet) ============================== */

@Composable
fun LocationPickerSheet(current: String?, onDismiss: () -> Unit, onPick: (String?) -> Unit) {
    var q by remember { mutableStateOf("") }
    val places = listOf("Jaipur, Rajasthan", "Hawa Mahal, Jaipur", "C-Scheme, Jaipur", "Mumbai, Maharashtra", "Delhi", "Bengaluru, Karnataka", "Goa", "Udaipur, Rajasthan")
        .filter { q.isBlank() || it.contains(q, ignoreCase = true) }
    IshqSheet(onDismiss, title = "Add location") {
        Column(Modifier.padding(horizontal = 16.dp)) {
            FieldBox("Search places", q, { q = it }, leading = Icons.Rounded.Search)
            Spacer(Modifier.height(6.dp))
            MenuRow(Icons.Rounded.MyLocation, "Use current location", subtitle = "Jaipur, Rajasthan", iconTint = BattleBlue, onClick = { onPick("Jaipur, Rajasthan") })
            places.forEach { p -> MenuRow(Icons.Rounded.Place, p, iconTint = BrandPink, trailing = { if (p == current) Icon(Icons.Rounded.Check, null, tint = BrandPink) }, onClick = { onPick(p) }) }
            if (current != null) TextButton(onClick = { onPick(null) }, modifier = Modifier.fillMaxWidth()) { Text("Remove location", color = LiveRed) }
            Spacer(Modifier.height(8.dp))
        }
    }
}

/* ============================== Shared field ============================== */

@Composable
private fun FieldBox(
    hint: String,
    value: String,
    onChange: (String) -> Unit,
    prefix: String? = null,
    leading: androidx.compose.ui.graphics.vector.ImageVector? = null,
    keyboard: KeyboardType = KeyboardType.Text,
) {
    Row(
        Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(16.dp)).background(SurfaceMuted.copy(alpha = 0.8f)).border(1.dp, Hairline, RoundedCornerShape(16.dp)).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) { Icon(leading, null, tint = InkTertiary, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)) }
        if (prefix != null) { Text(prefix, style = MaterialTheme.typography.titleSmall, color = Ink); Spacer(Modifier.width(10.dp)) }
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Text(hint, style = MaterialTheme.typography.bodyLarge, color = InkTertiary, maxLines = 1)
            BasicTextField(value, onChange, singleLine = true, textStyle = MaterialTheme.typography.bodyLarge.copy(color = Ink), keyboardOptions = KeyboardOptions(keyboardType = keyboard), modifier = Modifier.fillMaxWidth())
        }
    }
}

/* ============================== Sheets: licences, my reports ============================== */

@Composable
private fun LicencesSheet(onDismiss: () -> Unit) {
    val libs = listOf(
        "Kotlin" to "Apache 2.0 · JetBrains",
        "Jetpack Compose & AndroidX" to "Apache 2.0 · Google",
        "CameraX" to "Apache 2.0 · Google",
        "Material icons" to "Apache 2.0 · Google",
        "Coil" to "Apache 2.0 · Coil contributors",
    )
    IshqSheet(onDismiss, title = "Open-source licences") {
        libs.forEach { (n, l) ->
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(n, style = MaterialTheme.typography.titleSmall, color = Ink)
                Text(l, style = MaterialTheme.typography.bodySmall, color = InkSecondary)
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun MyReportsSheet(onDismiss: () -> Unit) {
    val reports = listOf(
        Triple(creatorById(8), "Spam in comments · 12 Oct", "Action taken · user muted for 7 days"),
        Triple(creatorById(11), "Asked for UPI outside the app · 2 Sep", "Action taken · account banned"),
    )
    IshqSheet(onDismiss, title = "My reports") {
        reports.forEach { (c, what, result) ->
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Avatar(c.imageUrl, 42.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(c.name, style = MaterialTheme.typography.titleSmall, color = Ink)
                    Text(what, style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                    Text(result, style = MaterialTheme.typography.labelSmall, color = Success)
                }
            }
        }
        Text("Reports are anonymous. The person is never told who reported them.", style = MaterialTheme.typography.bodySmall, color = InkTertiary, modifier = Modifier.padding(20.dp))
    }
}
