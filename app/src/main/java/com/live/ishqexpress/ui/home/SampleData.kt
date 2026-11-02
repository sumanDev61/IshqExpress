package com.live.ishqexpress.ui.home

import androidx.compose.ui.graphics.Color

/** Placeholder content for the UI build. Replace with the feed API later. */
enum class LiveMode(val label: String) { Solo("Live"), Pk("PK"), Party("Party"), Audio("Audio") }

data class LiveCreator(
    val id: Int,
    val name: String,
    val city: String,
    val flag: String,
    val tag: String,
    val viewers: Int,
    val photo: Int, // pravatar index
    val mode: LiveMode = LiveMode.Solo,
    val verified: Boolean = false,
    val distanceKm: Double = 0.0,
    val hot: Boolean = false,
    val tint: List<Color>,
) {
    val imageUrl get() = "https://i.pravatar.cc/600?img=$photo"
}

private val tints = listOf(
    listOf(Color(0xFFFFB3D1), Color(0xFFC07BF0)),
    listOf(Color(0xFFFFC9A8), Color(0xFFFF6FA5)),
    listOf(Color(0xFFB9C8FF), Color(0xFFB07BF0)),
    listOf(Color(0xFFFFD6E8), Color(0xFFFF8CB8)),
    listOf(Color(0xFFD8C2FF), Color(0xFF8D5BEA)),
    listOf(Color(0xFFFFE0B5), Color(0xFFFF9A8B)),
)

val sampleCreators: List<LiveCreator> = listOf(
    LiveCreator(1, "Aanya Kapoor", "Mumbai", "🇮🇳", "#Singing", 12_480, 47, LiveMode.Solo, verified = true, distanceKm = 2.4, hot = true, tint = tints[0]),
    LiveCreator(2, "Rohan Mehra", "Delhi", "🇮🇳", "#Shayari", 3_215, 12, LiveMode.Pk, distanceKm = 5.1, tint = tints[2]),
    LiveCreator(3, "Simran Kaur", "Amritsar", "🇮🇳", "#Dance", 8_930, 45, LiveMode.Party, verified = true, distanceKm = 1.2, tint = tints[1]),
    LiveCreator(4, "Ishita Rao", "Bengaluru", "🇮🇳", "#Talk", 1_042, 44, LiveMode.Solo, distanceKm = 0.8, tint = tints[3]),
    LiveCreator(5, "Kabir Singh", "Jaipur", "🇮🇳", "#Gaming", 6_771, 13, LiveMode.Audio, verified = true, distanceKm = 3.6, tint = tints[4]),
    LiveCreator(6, "Meera Joshi", "Pune", "🇮🇳", "#Bollywood", 22_310, 49, LiveMode.Solo, verified = true, distanceKm = 7.9, hot = true, tint = tints[5]),
    LiveCreator(7, "Zoya Khan", "Lucknow", "🇮🇳", "#Ghazal", 4_508, 41, LiveMode.Party, distanceKm = 4.3, tint = tints[0]),
    LiveCreator(8, "Arjun Nair", "Kochi", "🇮🇳", "#Fitness", 2_190, 15, LiveMode.Solo, distanceKm = 9.2, tint = tints[2]),
    LiveCreator(9, "Priya Sharma", "Jaipur", "🇮🇳", "#Chat", 9_874, 32, LiveMode.Pk, verified = true, distanceKm = 1.9, tint = tints[1]),
    LiveCreator(10, "Naina Gupta", "Indore", "🇮🇳", "#Comedy", 5_633, 29, LiveMode.Solo, distanceKm = 6.4, tint = tints[3]),
    LiveCreator(11, "Dev Malhotra", "Chandigarh", "🇮🇳", "#Music", 1_388, 33, LiveMode.Audio, distanceKm = 11.0, tint = tints[4]),
    LiveCreator(12, "Riya Das", "Kolkata", "🇮🇳", "#Cooking", 3_902, 20, LiveMode.Solo, distanceKm = 12.7, tint = tints[5]),
)

data class Promo(val title: String, val subtitle: String, val cta: String, val emoji: String, val colors: List<Color>)

val samplePromos = listOf(
    Promo("Diwali Gift Fest", "Send gifts, creators earn 20% bonus diamonds", "Join now", "🪔", listOf(Color(0xFFFF2E8B), Color(0xFFC236D9))),
    Promo("Become a creator", "Go live today and start earning", "Go live", "🎙️", listOf(Color(0xFF7A2BE8), Color(0xFFC236D9))),
    Promo("Daily check-in", "Claim free coins every day", "Claim", "💎", listOf(Color(0xFFFF6A88), Color(0xFFFF2E8B))),
)

fun Int.compact(): String = when {
    this >= 1_000_000 -> String.format("%.1fM", this / 1_000_000f)
    this >= 1_000 -> String.format("%.1fK", this / 1_000f)
    else -> toString()
}.replace(".0", "")
