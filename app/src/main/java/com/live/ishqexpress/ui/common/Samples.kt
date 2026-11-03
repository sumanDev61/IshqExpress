package com.live.ishqexpress.ui.common

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.live.ishqexpress.ui.home.LiveCreator
import com.live.ishqexpress.ui.home.sampleCreators

/*
 * Placeholder content for the UI build. Every list here is replaced by an API later.
 * Money follows the research doc: viewers buy Coins, creators earn Diamonds (~2 coins : 1 diamond).
 */

fun creatorById(id: Int): LiveCreator = sampleCreators.firstOrNull { it.id == id } ?: sampleCreators.first()

fun avatar(img: Int) = "https://i.pravatar.cc/300?img=$img"
fun photo(seed: Int, w: Int = 600, h: Int = 800) = "https://picsum.photos/seed/ishq$seed/$w/$h"

/* ---------- Me ---------- */

object Me {
    val name get() = com.live.ishqexpress.data.Session.name
    const val handle = "@suman.live"
    const val id = "ID 80412337"
    const val photo = 5
    const val coins = 1_240
    const val diamonds = 18_640
    const val level = 23
    const val levelXp = 0.62f
    const val vipTier = 3
    const val crowns = 0
    const val followers = 1_204
    const val following = 318
    const val fans = 46
    val avatarUrl get() = com.live.ishqexpress.data.Session.avatar ?: avatar(photo)
}

/* ---------- Gifts (13, 14) ---------- */

/** Drawer tabs, Tango-style. Bag holds free gifts (e.g. lucky-gift winnings). */
enum class GiftTab(val label: String) { Popular("Popular"), Lucky("Lucky"), Motion("Motion"), Ar("AR"), Exclusive("VIP"), Bag("Bag") }

/** How a gift plays on screen. */
enum class GiftKind {
    /** Side banner with combo counter. */
    Classic,
    /** Classic banner + a random luck multiplier for the sender. */
    Lucky,
    /** Full-screen motion effect (transparent video when an asset exists, else a drawn animation). */
    Motion,
    /** Effect anchored to the host's face. */
    Ar,
}

/** Drawn fallback / style for motion gifts. */
enum class MotionStyle { Car, Castle, Rocket, Dragon, Fireworks, Hearts, Portal }

data class Gift(
    val id: Int,
    val emoji: String,
    val name: String,
    val coins: Int,
    val tab: GiftTab,
    val kind: GiftKind = GiftKind.Classic,
    val motion: MotionStyle? = null,
    /** Side-by-side alpha MP4 under assets/, e.g. "gifts/love_storm.mp4". */
    val video: String? = null,
    /** Minimum VIP tier needed to send, 0 = everyone. */
    val vip: Int = 0,
    /** Counts double in PK battles. */
    val pkBoost: Boolean = false,
) {
    val fullScreen get() = kind == GiftKind.Motion
}

val sampleGifts = listOf(
    Gift(1, "🌹", "Rose", 1, GiftTab.Popular),
    Gift(2, "❤️", "Heart", 5, GiftTab.Popular),
    Gift(3, "🍫", "Chocolate", 10, GiftTab.Popular),
    Gift(4, "🧸", "Teddy", 49, GiftTab.Popular),
    Gift(5, "💐", "Bouquet", 99, GiftTab.Popular),
    Gift(6, "💍", "Ring", 299, GiftTab.Popular),
    Gift(16, "🎉", "Party popper", 29, GiftTab.Popular),
    Gift(19, "⚡", "PK Booster", 199, GiftTab.Popular, pkBoost = true),

    Gift(10, "🍀", "Lucky clover", 9, GiftTab.Lucky, GiftKind.Lucky),
    Gift(11, "🎲", "Lucky dice", 19, GiftTab.Lucky, GiftKind.Lucky),
    Gift(12, "🧧", "Red packet", 99, GiftTab.Lucky, GiftKind.Lucky),
    Gift(20, "🪙", "Gold coin", 49, GiftTab.Lucky, GiftKind.Lucky),
    Gift(21, "🐱", "Lucky cat", 199, GiftTab.Lucky, GiftKind.Lucky),

    Gift(22, "💗", "Love storm", 699, GiftTab.Motion, GiftKind.Motion, MotionStyle.Hearts, video = "gifts/love_storm.mp4"),
    Gift(23, "🌀", "Galaxy portal", 1_499, GiftTab.Motion, GiftKind.Motion, MotionStyle.Portal, video = "gifts/galaxy_portal.mp4"),
    Gift(29, "💏", "Love couple", 2_499, GiftTab.Motion, GiftKind.Motion, MotionStyle.Hearts, video = "gifts/love_couple.mp4"),
    Gift(7, "🏎️", "Sports car", 1_999, GiftTab.Motion, GiftKind.Motion, MotionStyle.Car),
    Gift(18, "🎆", "Fireworks", 999, GiftTab.Motion, GiftKind.Motion, MotionStyle.Fireworks),
    Gift(15, "🐉", "Dragon", 2_999, GiftTab.Motion, GiftKind.Motion, MotionStyle.Dragon),
    Gift(8, "🏰", "Castle", 4_999, GiftTab.Motion, GiftKind.Motion, MotionStyle.Castle),
    Gift(9, "🚀", "Rocket", 9_999, GiftTab.Motion, GiftKind.Motion, MotionStyle.Rocket),

    Gift(14, "👑", "Crown", 499, GiftTab.Ar, GiftKind.Ar),
    Gift(13, "🦋", "Butterflies", 199, GiftTab.Ar, GiftKind.Ar),
    Gift(24, "🐰", "Bunny ears", 99, GiftTab.Ar, GiftKind.Ar),
    Gift(25, "😎", "Cool shades", 149, GiftTab.Ar, GiftKind.Ar),

    Gift(26, "🦚", "Royal peacock", 3_999, GiftTab.Exclusive, GiftKind.Motion, MotionStyle.Fireworks, vip = 3),
    Gift(27, "💎", "Diamond throne", 7_999, GiftTab.Exclusive, GiftKind.Motion, MotionStyle.Castle, vip = 6),
    Gift(28, "🛥️", "Yacht", 14_999, GiftTab.Exclusive, GiftKind.Motion, MotionStyle.Car, vip = 9),
)

fun giftById(id: Int): Gift = sampleGifts.first { it.id == id }

/** Who a gift goes to: the host, a party guest, or a PK opponent. */
data class GiftTarget(val id: Int, val name: String, val avatarUrl: String)

/**
 * Demo wallet shared by every room so coins and Bag stay in sync while you move around the app.
 * Replace with the wallet API; the server is the only source of truth for balances.
 */
object DemoWallet {
    var coins by mutableIntStateOf(Me.coins)
    /** giftId → count of free gifts in the Bag. */
    val bag = mutableStateMapOf(1 to 30, 2 to 6, 4 to 1)
}

/**
 * Lucky-gift odds. Expected return is below 1 and wins are paid as free Roses into the Bag,
 * not coins — coin-in/coin-out would make this a real-money game (see the research doc).
 */
object LuckyDraw {
    /** (multiplier, probability). Remaining probability = no win. */
    val table = listOf(500 to 0.0001, 100 to 0.001, 50 to 0.003, 10 to 0.015, 5 to 0.03, 2 to 0.06, 1 to 0.10)

    /** [roll] in [0, 1). Returns 0 for no win. */
    fun multiplier(roll: Double): Int {
        var acc = 0.0
        for ((m, p) in table) { acc += p; if (roll < acc) return m }
        return 0
    }

    val expectedReturn: Double get() = table.sumOf { (m, p) -> m * p }
}

/* ---------- Live chat ---------- */

data class LiveComment(val user: String, val level: Int, val text: String, val gift: String? = null, val system: Boolean = false)

val sampleLiveComments = listOf(
    LiveComment("ishqexpress", 0, "Welcome! Be kind — follow the community rules.", system = true),
    LiveComment("Rahul_07", 12, "Hello from Kanpur 👋"),
    LiveComment("Neha", 31, "sent Teddy ×3", gift = "🧸"),
    LiveComment("Vikram", 8, "Please sing Tum Hi Ho 🙏"),
    LiveComment("Sana", 19, "Your voice is magic ✨"),
    LiveComment("Amit K", 44, "sent Sports car", gift = "🏎️"),
    LiveComment("Pooja", 5, "joined the live"),
    LiveComment("Karan", 27, "Goal almost done, let's go!"),
)

data class Viewer(val name: String, val img: Int, val level: Int, val coins: Int, val vip: Int = 0, val fan: Boolean = false)

val sampleViewers = listOf(
    Viewer("Amit K", 11, 44, 12_400, vip = 6, fan = true),
    Viewer("Neha", 23, 31, 6_210, vip = 4, fan = true),
    Viewer("Karan", 52, 27, 3_980, vip = 2),
    Viewer("Sana", 25, 19, 2_100, fan = true),
    Viewer("Rahul_07", 60, 12, 880),
    Viewer("Vikram", 53, 8, 420),
    Viewer("Pooja", 26, 5, 120),
    Viewer("Imran", 58, 3, 0),
    Viewer("Tanya", 24, 2, 0),
)

/* ---------- Explore (8) ---------- */

data class Category(val emoji: String, val label: String, val live: Int, val colors: List<Color>)

val sampleCategories = listOf(
    Category("🎤", "Music", 1_284, listOf(Color(0xFFFF6FA5), Color(0xFFC236D9))),
    Category("💃", "Dance", 932, listOf(Color(0xFFFF9A8B), Color(0xFFFF2E8B))),
    Category("💬", "Talk", 2_410, listOf(Color(0xFF8D5BEA), Color(0xFFC07BF0))),
    Category("🎮", "Gaming", 611, listOf(Color(0xFF5B7BEA), Color(0xFF8D5BEA))),
    Category("😂", "Comedy", 388, listOf(Color(0xFFFFB020), Color(0xFFFF6A88))),
    Category("📜", "Shayari", 702, listOf(Color(0xFFC236D9), Color(0xFF7A2BE8))),
    Category("🍳", "Cooking", 154, listOf(Color(0xFFFF8C5A), Color(0xFFFFB020))),
    Category("💪", "Fitness", 207, listOf(Color(0xFF12B76A), Color(0xFF0BA5A5))),
)

val trendingTags = listOf("#DiwaliLive", "#PKBattle", "#ShayariNight", "#Bollywood", "#LateNightTalk", "#Garba", "#Antakshari")

/* ---------- Audio rooms (34–36) ---------- */

data class AudioRoom(val id: Int, val title: String, val host: Int, val speakers: List<Int>, val listeners: Int, val tag: String)

val sampleRooms = listOf(
    AudioRoom(1, "Late night Shayari 🌙", 7, listOf(7, 2, 11, 4), 842, "Shayari"),
    AudioRoom(2, "Antakshari — join in!", 1, listOf(1, 6, 3), 1_530, "Music"),
    AudioRoom(3, "Startup talk: Jaipur founders", 5, listOf(5, 8), 214, "Talk"),
    AudioRoom(4, "Bollywood quiz night", 6, listOf(6, 10, 9, 12, 3), 977, "Games"),
    AudioRoom(5, "Dil ki baat — relationship advice", 9, listOf(9, 4), 1_102, "Talk"),
)

/* ---------- Match (37) ---------- */

data class MatchCard(val creator: Int, val age: Int, val bio: String, val interests: List<String>, val live: Boolean)

val sampleMatches = listOf(
    MatchCard(3, 24, "Bhangra on weekends, chai always.", listOf("💃 Dance", "☕ Chai", "✈️ Travel"), live = true),
    MatchCard(4, 26, "Product designer. Ask me about plants.", listOf("🎨 Art", "🌿 Plants", "📚 Books"), live = false),
    MatchCard(10, 23, "Stand-up comic in training 🎤", listOf("😂 Comedy", "🎬 Movies"), live = true),
    MatchCard(12, 27, "I will cook for you. Probably.", listOf("🍳 Cooking", "🎵 Music"), live = false),
    MatchCard(7, 25, "Ghazals and long drives.", listOf("🎶 Ghazal", "🚗 Drives"), live = false),
)

/* ---------- Feed (39–42) ---------- */

data class Post(
    val id: Int, val creator: Int, val text: String, val image: Int?, val likes: Int, val comments: Int,
    val time: String, val locked: Boolean = false, val video: Boolean = false,
    /** Local or remote media url for posts you made (creator id 0 = you). */
    val media: String? = null,
)

/** Posts you published this session. In production these come back from the posts API. */
object MyPosts {
    val items = androidx.compose.runtime.mutableStateListOf<Post>()
    fun all() = items + samplePosts
    fun byId(id: Int) = all().firstOrNull { it.id == id }
}

val samplePosts = listOf(
    Post(1, 1, "Rehearsal for tonight's live 🎶 9 PM, don't miss it!", 101, 4_812, 213, "2h"),
    Post(2, 6, "Exclusive: behind the scenes of my new cover", 102, 1_920, 88, "5h", locked = true),
    Post(3, 3, "Garba practice with the squad 💃", 103, 7_301, 402, "8h", video = true),
    Post(4, 9, "Thank you for 100K diamonds this month 🥹 You all are family.", null, 2_244, 190, "1d"),
    Post(5, 4, "Sunday mood", 104, 980, 41, "1d"),
    Post(6, 6, "Super Fans only — the full unplugged set", 105, 1_120, 64, "2d", locked = true, video = true),
)

data class Comment(val user: String, val img: Int, val text: String, val time: String, val likes: Int)

val sampleComments = listOf(
    Comment("Neha", 23, "Can't wait!! Setting a reminder ⏰", "1h", 42),
    Comment("Amit K", 11, "Bringing the sports car tonight 🏎️", "1h", 31),
    Comment("Sana", 25, "Please sing Kesariya", "52m", 18),
    Comment("Rahul_07", 60, "Love from Kanpur ❤️", "40m", 9),
    Comment("Pooja", 26, "First time here, already a fan", "12m", 3),
)

/* ---------- News & notifications (43, 44) ---------- */

data class NewsItem(val title: String, val body: String, val tag: String, val image: Int, val time: String)

val sampleNews = listOf(
    NewsItem("Diwali Gift Fest is live", "Send festive gifts this week and creators earn a 20% diamond bonus.", "Event", 201, "Today"),
    NewsItem("PK Battle League — Season 3", "Top 16 creators battle for the trophy and 1M coins prize pool.", "Event", 202, "Yesterday"),
    NewsItem("New: auto-translate in chats", "Messages now translate into your app language automatically.", "Update", 203, "2 days ago"),
    NewsItem("Weekly top gifters", "See who topped the Rich List this week.", "Trending", 204, "3 days ago"),
)

enum class NotifKind { Follow, Gift, Live, Like, Comment, System }

data class Notif(val kind: NotifKind, val who: Int, val text: String, val time: String, val unread: Boolean = false)

val sampleNotifs = listOf(
    Notif(NotifKind.Live, 1, "Aanya Kapoor is live: Sunday unplugged 🎶", "2m", unread = true),
    Notif(NotifKind.Gift, 9, "Priya Sharma thanked you for the Teddy 🧸", "18m", unread = true),
    Notif(NotifKind.Follow, 4, "Ishita Rao started following you", "1h", unread = true),
    Notif(NotifKind.Like, 3, "Simran Kaur liked your comment", "3h"),
    Notif(NotifKind.Comment, 6, "Meera Joshi replied: \"Thank you so much!\"", "5h"),
    Notif(NotifKind.System, 0, "You reached Level 23 — new entry effect unlocked", "1d"),
    Notif(NotifKind.Live, 5, "Kabir Singh started an audio room", "1d"),
    Notif(NotifKind.Follow, 10, "Naina Gupta started following you", "2d"),
)

/* ---------- Chats (45–47) ---------- */

data class Conversation(val id: Int, val creator: Int, val last: String, val time: String, val unread: Int, val online: Boolean, val request: Boolean = false)

val sampleConversations = listOf(
    Conversation(1, 1, "See you at the live tonight 😊", "9:41", 2, online = true),
    Conversation(2, 9, "🎁 Sent you a Rose", "9:02", 1, online = true),
    Conversation(3, 4, "Haha that's so true", "Yesterday", 0, online = false),
    Conversation(4, 6, "📷 Photo", "Yesterday", 0, online = true),
    Conversation(5, 3, "Thanks for the gift!", "Mon", 0, online = false),
    Conversation(6, 12, "Recipe kal bhejti hoon", "Sun", 0, online = false),
    Conversation(7, 2, "Hey! Loved your PK battle", "Sat", 0, online = false, request = true),
    Conversation(8, 11, "Can we collab on a live?", "Fri", 0, online = true, request = true),
)

data class Message(val mine: Boolean, val text: String, val time: String, val translated: String? = null, val gift: String? = null, val image: Int? = null, val imageUrl: String? = null)

val sampleMessages = listOf(
    Message(false, "Hiii! Thanks for joining my live yesterday 💕", "9:30"),
    Message(true, "Your voice was amazing. When's the next one?", "9:31"),
    Message(false, "Aaj raat 9 baje! Special Diwali songs", "9:33", translated = "Tonight at 9! Special Diwali songs"),
    Message(true, "", "9:34", gift = "🌹"),
    Message(false, "Aww thank you 🥹", "9:35"),
    Message(false, "", "9:36", image = 301),
    Message(true, "Looks beautiful! See you tonight", "9:40"),
    Message(false, "See you at the live tonight 😊", "9:41"),
)

data class MediaPack(val title: String, val count: Int, val cover: Int, val price: Int)

val sampleMediaPacks = listOf(
    MediaPack("Diwali shoot", 12, 401, 199),
    MediaPack("Studio session", 8, 402, 149),
    MediaPack("Goa trip", 24, 403, 299),
    MediaPack("Behind the scenes", 6, 404, 0),
    MediaPack("Dance reels", 10, 405, 99),
    MediaPack("Selfies", 18, 406, 0),
)

/* ---------- Money (58–66) ---------- */

data class CoinPack(val coins: Int, val bonus: Int, val price: String, val tag: String? = null)

val sampleCoinPacks = listOf(
    CoinPack(70, 0, "₹89"),
    CoinPack(350, 35, "₹449", "First purchase ×2"),
    CoinPack(700, 84, "₹899"),
    CoinPack(1_400, 210, "₹1,799", "Most popular"),
    CoinPack(3_500, 630, "₹4,499"),
    CoinPack(7_000, 1_400, "₹8,999", "Best value"),
)

enum class TxType { Purchase, Gift, Subscription, Reward, Ticket, Refund }

data class Tx(val type: TxType, val title: String, val amount: Int, val time: String)

val sampleTxs = listOf(
    Tx(TxType.Gift, "Teddy ×3 to Priya Sharma", -147, "Today, 9:12 PM"),
    Tx(TxType.Reward, "Daily check-in · day 5", 20, "Today, 8:00 AM"),
    Tx(TxType.Purchase, "Bought 700 + 84 coins", 784, "Yesterday, 10:41 PM"),
    Tx(TxType.Ticket, "Premium live · Meera Joshi", -500, "Yesterday, 9:02 PM"),
    Tx(TxType.Subscription, "Super Fan · Aanya Kapoor", -999, "1 Nov"),
    Tx(TxType.Gift, "Sports car to Aanya Kapoor", -1_999, "31 Oct"),
    Tx(TxType.Refund, "Private show ended early", 120, "30 Oct"),
    Tx(TxType.Purchase, "Bought 1,400 + 210 coins", 1_610, "28 Oct"),
)

data class Payout(val amount: String, val diamonds: Int, val status: String, val date: String, val method: String)

val samplePayouts = listOf(
    Payout("₹8,240", 10_300, "Processing", "1 Nov 2026", "HDFC ••4821"),
    Payout("₹12,600", 15_750, "Paid", "1 Oct 2026", "HDFC ••4821"),
    Payout("₹9,120", 11_400, "Paid", "1 Sep 2026", "HDFC ••4821"),
    Payout("₹4,000", 5_000, "Rejected", "18 Aug 2026", "UPI suman@okhdfc"),
    Payout("₹6,880", 8_600, "Paid", "1 Aug 2026", "UPI suman@okhdfc"),
)

/* VIP — 13 tiers per the doc; monthly spend resets every 30 days. */
data class VipTier(val level: Int, val name: String, val monthlyCoins: Int, val color: Color)

val vipTiers = listOf(
    VipTier(1, "Bronze", 800, Color(0xFFC98B5B)),
    VipTier(2, "Silver", 2_000, Color(0xFF9AA4B2)),
    VipTier(3, "Gold", 4_000, Color(0xFFFFB020)),
    VipTier(4, "Platinum", 8_000, Color(0xFF5BC0BE)),
    VipTier(5, "Emerald", 16_000, Color(0xFF12B76A)),
    VipTier(6, "Sapphire", 30_000, Color(0xFF3B6FE0)),
    VipTier(7, "Ruby", 50_000, Color(0xFFE0245E)),
    VipTier(8, "Amethyst", 80_000, Color(0xFF8D5BEA)),
    VipTier(9, "Diamond", 120_000, Color(0xFF6EC6FF)),
    VipTier(10, "Royal", 200_000, Color(0xFFC236D9)),
    VipTier(11, "Imperial", 400_000, Color(0xFF7A2BE8)),
    VipTier(12, "Legend", 800_000, Color(0xFFFF2E8B)),
    VipTier(13, "Mythic", 2_400_000, Color(0xFF2A0B4A)),
)

data class SubTier(val name: String, val coins: Int, val perks: List<String>, val colors: List<Color>)

/* Fan tiers with the doc's default prices (creator can change them). */
val fanTiers = listOf(
    SubTier("Fan", 499, listOf("Fan badge in chat", "Fan-only posts", "Priority in live chat"), listOf(Color(0xFFFF8CB8), Color(0xFFFF2E8B))),
    SubTier("Super Fan", 999, listOf("Everything in Fan", "Free entry to premium lives", "Exclusive emojis", "Monthly shout-out"), listOf(Color(0xFFC236D9), Color(0xFF7A2BE8))),
    SubTier("Exclusive Fan", 4_999, listOf("Everything in Super Fan", "1 private show per month", "Direct chat priority", "Name in creator's profile"), listOf(Color(0xFF7A2BE8), Color(0xFF2A0B4A))),
)

data class LeaderEntry(val creator: Int, val score: Int)

val sampleLeaders = listOf(6, 1, 9, 3, 5, 10, 7, 2, 12, 4, 8, 11).mapIndexed { i, c -> LeaderEntry(c, 980_000 / (i + 1) + 1_200 * (12 - i)) }
