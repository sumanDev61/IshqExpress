package com.live.ishqexpress.ui.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.live.ishqexpress.ui.components.OnboardingFrame
import com.live.ishqexpress.ui.components.PrimaryButton
import com.live.ishqexpress.ui.theme.BrandPink
import com.live.ishqexpress.ui.theme.Canvas
import com.live.ishqexpress.ui.theme.Hairline
import com.live.ishqexpress.ui.theme.Ink
import com.live.ishqexpress.ui.theme.InkSecondary
import com.live.ishqexpress.ui.theme.InkTertiary
import com.live.ishqexpress.ui.theme.PinkTint
import com.live.ishqexpress.ui.theme.SurfaceMuted
import kotlinx.coroutines.delay

/* ============================== 3a. Phone number ============================== */

@Composable
fun PhoneEntryScreen(
    phone: String,
    onPhoneChange: (String) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    val focus = remember { FocusRequester() }
    var focused by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { focus.requestFocus() }
    val valid = phone.length == 10

    OnboardingFrame(
        title = "What's your number?",
        subtitle = "We'll send a 6-digit code to verify it's really you.",
        onBack = onBack,
        footer = { PrimaryButton("Send code", onContinue, enabled = valid) }
    ) {
        Spacer(Modifier.height(32.dp))
        Row(
            Modifier.padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Country picker (UI only)
            Row(
                Modifier
                    .height(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceMuted)
                    .clickable { }
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🇮🇳", fontSize = 20.sp)
                Spacer(Modifier.width(6.dp))
                Text("+91", style = MaterialTheme.typography.titleMedium, color = Ink)
                Icon(Icons.Rounded.KeyboardArrowDown, null, tint = InkTertiary, modifier = Modifier.size(20.dp))
            }
            val border by animateColorAsState(if (focused) BrandPink else Hairline, label = "b")
            Box(
                Modifier
                    .weight(1f)
                    .height(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (focused) Canvas else SurfaceMuted)
                    .border(if (focused) 1.5.dp else 0.dp, if (focused) border else SurfaceMuted, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = phone,
                    onValueChange = { v -> onPhoneChange(v.filter(Char::isDigit).take(10)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    textStyle = MaterialTheme.typography.titleMedium.copy(color = Ink, fontSize = 18.sp, letterSpacing = 1.sp),
                    cursorBrush = SolidColor(BrandPink),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focus)
                        .onFocusChanged { focused = it.isFocused },
                    decorationBox = { inner ->
                        if (phone.isEmpty()) {
                            Text("98765 43210", style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp), color = InkTertiary)
                        }
                        inner()
                    }
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(
            Modifier.padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Lock, null, tint = InkTertiary, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                "Your number stays private — it's never shown on your profile.",
                style = MaterialTheme.typography.bodySmall,
                color = InkTertiary
            )
        }
    }
}

/* ============================== 3b. OTP ============================== */

@Composable
fun OtpScreen(
    phone: String,
    onBack: () -> Unit,
    onVerified: () -> Unit,
) {
    var code by remember { mutableStateOf("") }
    var seconds by remember { mutableIntStateOf(30) }
    var resendKey by remember { mutableIntStateOf(0) }
    val focus = remember { FocusRequester() }

    LaunchedEffect(Unit) { focus.requestFocus() }
    LaunchedEffect(resendKey) {
        seconds = 30
        while (seconds > 0) { delay(1000); seconds-- }
    }

    val pretty = "+91 ${phone.take(5)} ${phone.drop(5)}"

    OnboardingFrame(
        title = "Enter the code",
        subtitle = null,
        onBack = onBack,
        footer = { PrimaryButton("Verify & continue", onVerified, enabled = code.length == 6) }
    ) {
        Text(
            buildAnnotatedString {
                append("Sent via SMS to ")
                withStyle(SpanStyle(color = Ink, fontWeight = FontWeight.SemiBold)) { append(pretty) }
                append("  ·  ")
                withStyle(SpanStyle(color = BrandPink, fontWeight = FontWeight.SemiBold)) { append("Edit") }
            },
            style = MaterialTheme.typography.bodyLarge,
            color = InkSecondary,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp)
                .clickable(onClick = onBack)
        )
        Spacer(Modifier.height(32.dp))

        BasicTextField(
            value = code,
            onValueChange = { v -> code = v.filter(Char::isDigit).take(6) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .focusRequester(focus),
            decorationBox = {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    repeat(6) { i ->
                        val char = code.getOrNull(i)
                        val isActive = i == code.length
                        val borderColor by animateColorAsState(
                            when {
                                isActive -> BrandPink
                                char != null -> PinkTint
                                else -> Hairline
                            }, label = "otp$i"
                        )
                        Box(
                            Modifier
                                .weight(1f)
                                .height(60.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (char != null) PinkTint else SurfaceMuted)
                                .border(if (isActive) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                char?.toString() ?: "",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Ink
                            )
                        }
                    }
                }
            }
        )

        Spacer(Modifier.height(24.dp))
        Row(Modifier.padding(horizontal = 24.dp)) {
            Text("Didn't get it? ", style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
            if (seconds > 0) {
                Text(
                    "Resend in 00:%02d".format(seconds),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = InkTertiary
                )
            } else {
                Text(
                    "Resend code",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = BrandPink,
                    modifier = Modifier.clickable { resendKey++ }
                )
            }
        }
    }
}
