package com.live.ishqexpress.ui.gifts

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.live.ishqexpress.ui.common.DemoWallet
import com.live.ishqexpress.ui.common.Gift
import com.live.ishqexpress.ui.common.GiftKind
import com.live.ishqexpress.ui.common.GiftTarget
import com.live.ishqexpress.ui.common.Me
import kotlin.random.Random

/**
 * One gift as it arrives in a room. In production this comes from the room's real-time channel
 * after the server has charged the sender — every phone in the room renders the same event.
 */
data class GiftEvent(
    val gift: Gift,
    val count: Int,
    val senderName: String,
    val senderAvatar: String,
    val senderLevel: Int,
    val receiver: GiftTarget,
    val id: Long = nextId(),
) {
    val totalCoins get() = gift.coins * count

    companion object {
        private var counter = 0L
        fun nextId(): Long = ++counter
    }
}

/** Side banner (combo) — merges repeat gifts from the same sender to the same receiver. */
data class ComboBanner(
    val key: String,
    val event: GiftEvent,
    val combo: Int,
    /** Bumps on every merge so the banner restarts its timer and re-animates the count. */
    val version: Int,
)

data class Entrance(val name: String, val avatar: String, val level: Int, val vip: Int, val ride: String, val id: Long = GiftEvent.nextId())

/**
 * Everything animating in one room. Pure state + queue logic (no Compose UI), so it is unit-tested.
 */
@Stable
class GiftEffectsState(private val maxLanes: Int = 2) {
    val banners = mutableStateListOf<ComboBanner>()
    private val bannerQueue = ArrayDeque<GiftEvent>()

    var motion by mutableStateOf<GiftEvent?>(null)
        private set
    private val motionQueue = mutableListOf<GiftEvent>()

    var ar by mutableStateOf<GiftEvent?>(null)
        private set
    var marquee by mutableStateOf<String?>(null)
        private set
    var entrance by mutableStateOf<Entrance?>(null)
        private set
    private val entranceQueue = ArrayDeque<Entrance>()

    /** Last time anything happened; lets idle rooms skip work. */
    var lastEventAt by mutableLongStateOf(0L)
        private set

    fun post(e: GiftEvent) {
        lastEventAt = System.currentTimeMillis()
        when (e.gift.kind) {
            GiftKind.Motion -> {
                // Bigger gifts jump the queue; equal value keeps arrival order.
                if (motion == null) motion = e
                else {
                    val at = motionQueue.indexOfFirst { it.totalCoins < e.totalCoins }
                    if (at < 0) motionQueue.add(e) else motionQueue.add(at, e)
                }
                // Motion gifts still show who sent them in a side banner.
                addBanner(e)
            }
            GiftKind.Ar -> { ar = e; addBanner(e) }
            GiftKind.Classic -> addBanner(e)
        }
        if (e.totalCoins >= MARQUEE_MIN_COINS) {
            marquee = "${e.senderName} sent ${e.gift.emoji} ${e.gift.name}${if (e.count > 1) " ×${e.count}" else ""} to ${e.receiver.name}"
        }
    }

    private fun addBanner(e: GiftEvent) {
        val key = "${e.senderName}|${e.gift.id}|${e.receiver.id}"
        val i = banners.indexOfFirst { it.key == key }
        if (i >= 0) {
            val b = banners[i]
            banners[i] = b.copy(event = e, combo = b.combo + e.count, version = b.version + 1)
        } else if (banners.size < maxLanes) {
            banners.add(ComboBanner(key, e, e.count, 0))
        } else {
            // Lanes full: merge into a queued event for the same key, else wait in line.
            val q = bannerQueue.indexOfFirst { "${it.senderName}|${it.gift.id}|${it.receiver.id}" == key }
            if (q >= 0) bannerQueue[q] = bannerQueue[q].copy(count = bannerQueue[q].count + e.count) else bannerQueue.addLast(e)
        }
    }

    fun bannerExpired(key: String, version: Int) {
        val i = banners.indexOfFirst { it.key == key && it.version == version }
        if (i < 0) return
        banners.removeAt(i)
        bannerQueue.removeFirstOrNull()?.let { addBanner(it) }
    }

    fun motionFinished(id: Long) {
        if (motion?.id != id) return
        motion = if (motionQueue.isEmpty()) null else motionQueue.removeAt(0)
    }

    val pendingMotion: Int get() = motionQueue.size

    fun arFinished(id: Long) { if (ar?.id == id) ar = null }
    fun marqueeFinished() { marquee = null }
    fun showMarquee(text: String) { marquee = text }

    fun enter(e: Entrance) { if (entrance == null) entrance = e else entranceQueue.addLast(e) }
    fun entranceFinished(id: Long) { if (entrance?.id == id) entrance = entranceQueue.removeFirstOrNull() }

    companion object {
        const val MARQUEE_MIN_COINS = 999
    }
}

enum class SendResult { Sent, NotEnoughCoins, NotInBag, Locked }

/**
 * Client-side stand-in for the "send gift" API. The real flow: POST /gifts with an idempotency key,
 * server charges coins (or the Bag), then broadcasts a GiftEvent to the room.
 */
object GiftSender {
    fun send(
        effects: GiftEffectsState,
        gift: Gift,
        count: Int,
        targets: List<GiftTarget>,
        fromBag: Boolean,
        vipTier: Int = Me.vipTier,
        random: Random = Random.Default,
    ): SendResult {
        if (targets.isEmpty()) return SendResult.Sent
        if (gift.vip > vipTier) return SendResult.Locked
        val units = count * targets.size
        if (fromBag) {
            val have = DemoWallet.bag[gift.id] ?: 0
            if (have < units) return SendResult.NotInBag
            if (have == units) DemoWallet.bag.remove(gift.id) else DemoWallet.bag[gift.id] = have - units
        } else {
            val cost = gift.coins * units
            if (cost > DemoWallet.coins) return SendResult.NotEnoughCoins
            DemoWallet.coins -= cost
        }
        targets.forEach { t ->
            effects.post(GiftEvent(gift, count, Me.name, Me.avatarUrl, Me.level, t))
        }
        return SendResult.Sent
    }
}
