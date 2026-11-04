package com.live.ishqexpress.ui.shorts

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import com.live.ishqexpress.ui.common.Me
import com.live.ishqexpress.ui.common.creatorById
import com.live.ishqexpress.ui.common.photo

/**
 * A short video. [creator] 0 = you. In production the feed, counts and comments come from the
 * backend and [url] is a CDN (HLS) link; here they are mock data with public sample clips.
 */
data class ShortVideo(
    val id: Int,
    val creator: Int,
    val caption: String,
    val music: String,
    val url: String,
    val thumb: String,
    val likes: Int,
    val comments: Int,
    val shares: Int,
    val views: Int,
)

data class ShortComment(val user: String, val avatar: String, val text: String, val time: String, val likes: Int = 0)

private const val clips = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/"
private val sampleClips = listOf("ForBiggerBlazes.mp4", "ForBiggerEscapes.mp4", "ForBiggerFun.mp4", "ForBiggerJoyrides.mp4", "ForBiggerMeltdowns.mp4")

private val captions = listOf(
    "Diwali vibes ✨🪔 #diwali #festive #ishqexpress",
    "Practice session before tonight's live 🎤 #singing #arijit",
    "POV: your crush joins your live 🙈 #pov #romance",
    "New dance trend, try it! 💃 #dance #trending",
    "Shayari for the night owls 🌙 #shayari #feelings",
    "Behind the scenes of my studio 🎧 #bts #music",
    "Morning chai and good vibes ☕ #morning #vlog",
    "Duet with me? Comment your song 🎶 #duet",
    "When the PK battle goes to the last second 😱 #pkbattle",
    "Goa sunsets hit different 🌅 #travel #goa",
)
private val songs = listOf(
    "Kesariya — Arijit Singh", "Tum Hi Ho — Arijit Singh", "Apna Bana Le — Arijit Singh", "Chaleya — Arijit Singh",
    "Pasoori — Ali Sethi", "Heeriye — Arijit Singh", "Original sound", "Raataan Lambiyan — Jubin Nautiyal", "Lo-fi Chai Beats", "Original sound",
)

object ShortsStore {
    val videos = mutableStateListOf<ShortVideo>().apply {
        addAll(List(10) { i ->
            ShortVideo(
                id = 100 + i, creator = (i % 12) + 1, caption = captions[i], music = songs[i],
                url = clips + sampleClips[i % sampleClips.size], thumb = photo(600 + i, 720, 1280),
                likes = 4_200 + i * 3_917 % 90_000, comments = 120 + i * 37, shares = 40 + i * 11, views = 52_000 + i * 41_337,
            )
        })
    }
    val liked = mutableStateListOf<Int>()
    val followed = mutableStateListOf(1, 4)
    val saved = mutableStateListOf<Int>()
    val comments = mutableStateMapOf<Int, List<ShortComment>>()

    fun commentsFor(v: ShortVideo): List<ShortComment> = comments[v.id] ?: listOf(
        ShortComment("Neha", photo(23, 100, 100), "This is so beautiful 😍", "2h", 48),
        ShortComment("Amit K", photo(11, 100, 100), "Waiting for tonight's live 🔥", "1h", 21),
        ShortComment("Riya", photo(31, 100, 100), "Song name please?", "45m", 9),
        ShortComment("Karan", photo(41, 100, 100), "Best creator on ishqexpress ❤️", "12m", 3),
    ).also { comments[v.id] = it }

    fun addComment(v: ShortVideo, text: String) {
        comments[v.id] = listOf(ShortComment(Me.name, Me.avatarUrl, text, "now")) + commentsFor(v)
    }

    fun likes(v: ShortVideo) = v.likes + if (v.id in liked) 1 else 0
    fun toggleLike(v: ShortVideo) { if (v.id in liked) liked.remove(v.id) else liked.add(v.id) }

    /** Adds your own upload at the top of the feed. */
    fun post(uri: String, caption: String) {
        val id = (videos.maxOfOrNull { it.id } ?: 100) + 1
        videos.add(0, ShortVideo(id, 0, caption.ifBlank { "My new short ✨" }, "Original sound — ${Me.name}", uri, Me.avatarUrl, 0, 0, 0, 1))
    }
}

/** Name / avatar for a short's creator (0 = you). */
fun shortAuthor(v: ShortVideo): Pair<String, String> =
    if (v.creator == 0) Me.name to Me.avatarUrl else creatorById(v.creator).let { it.name to it.imageUrl }
