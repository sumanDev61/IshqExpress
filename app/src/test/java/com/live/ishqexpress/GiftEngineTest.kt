package com.live.ishqexpress

import com.live.ishqexpress.ui.common.DemoWallet
import com.live.ishqexpress.ui.common.GiftKind
import com.live.ishqexpress.ui.common.GiftTab
import com.live.ishqexpress.ui.common.GiftTarget
import com.live.ishqexpress.ui.common.giftById
import com.live.ishqexpress.ui.common.sampleGifts
import com.live.ishqexpress.ui.gifts.GiftEffectsState
import com.live.ishqexpress.ui.gifts.GiftEvent
import com.live.ishqexpress.ui.gifts.GiftSender
import com.live.ishqexpress.ui.gifts.SendResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class GiftEngineTest {

    private val host = GiftTarget(1, "Aanya Kapoor", "a")
    private val guest = GiftTarget(3, "Simran Kaur", "b")
    private fun ev(giftId: Int, count: Int = 1, sender: String = "Neha", to: GiftTarget = host) =
        GiftEvent(giftById(giftId), count, sender, "x", 10, to)

    @Before fun resetWallet() {
        DemoWallet.coins = 1_000
        DemoWallet.bag.clear()
        DemoWallet.bag[1] = 5
    }

    /* ---------- combo banners ---------- */

    @Test fun repeatGiftsMergeIntoOneComboBanner() {
        val s = GiftEffectsState()
        s.post(ev(1, 1)); s.post(ev(1, 10)); s.post(ev(1, 1))
        assertEquals(1, s.banners.size)
        assertEquals(12, s.banners[0].combo)
        assertEquals(2, s.banners[0].version)
    }

    @Test fun differentReceiverIsADifferentBanner() {
        val s = GiftEffectsState()
        s.post(ev(1, to = host)); s.post(ev(1, to = guest))
        assertEquals(2, s.banners.size)
    }

    @Test fun lanesOverflowQueueAndRefillOnExpiry() {
        val s = GiftEffectsState(maxLanes = 2)
        s.post(ev(1, sender = "A")); s.post(ev(1, sender = "B")); s.post(ev(1, sender = "C")); s.post(ev(1, 3, sender = "C"))
        assertEquals(listOf("A", "B"), s.banners.map { it.event.senderName })
        val a = s.banners[0]
        s.bannerExpired(a.key, a.version)
        assertEquals(listOf("B", "C"), s.banners.map { it.event.senderName })
        assertEquals("queued repeats merge before showing", 4, s.banners[1].combo)
    }

    @Test fun staleExpiryIsIgnoredAfterAComboBump() {
        val s = GiftEffectsState()
        s.post(ev(1)); val old = s.banners[0]; s.post(ev(1))
        s.bannerExpired(old.key, old.version)
        assertEquals(1, s.banners.size)
    }

    /* ---------- motion queue ---------- */

    @Test fun biggerMotionGiftsJumpTheQueue() {
        val s = GiftEffectsState()
        val first = ev(22); s.post(first)                // Love storm 699, plays now
        s.post(ev(18)); s.post(ev(9)); s.post(ev(7))     // Fireworks 999, Rocket 9,999, Car 1,999
        assertEquals(first.id, s.motion!!.id)
        s.motionFinished(first.id); assertEquals("Rocket", s.motion!!.gift.name)
        s.motionFinished(s.motion!!.id); assertEquals("Sports car", s.motion!!.gift.name)
        s.motionFinished(s.motion!!.id); assertEquals("Fireworks", s.motion!!.gift.name)
        s.motionFinished(s.motion!!.id); assertNull(s.motion)
    }

    @Test fun motionAndArStillShowWhoSentThem() {
        val s = GiftEffectsState()
        s.post(ev(8)); s.post(ev(14, sender = "Sana"))
        assertEquals(2, s.banners.size)
        assertNotNull(s.ar)
    }

    @Test fun bigGiftsTriggerMarquee() {
        val s = GiftEffectsState()
        s.post(ev(4, 10)); assertNull("490 coins is below the marquee bar", s.marquee)
        s.post(ev(4, 21)); assertTrue(s.marquee!!.contains("Teddy ×21"))
    }

    /* ---------- sending ---------- */

    @Test fun sendChargesCoinsPerTarget() {
        val s = GiftEffectsState()
        val r = GiftSender.send(s, giftById(4), 2, listOf(host, guest), fromBag = false) // 49 × 2 × 2
        assertEquals(SendResult.Sent, r)
        assertEquals(1_000 - 196, DemoWallet.coins)
        assertEquals(2, s.banners.size)
    }

    @Test fun notEnoughCoinsChargesNothing() {
        val s = GiftEffectsState()
        assertEquals(SendResult.NotEnoughCoins, GiftSender.send(s, giftById(9), 1, listOf(host), fromBag = false))
        assertEquals(1_000, DemoWallet.coins)
        assertTrue(s.banners.isEmpty())
    }

    @Test fun bagGiftsAreFreeAndCounted() {
        val s = GiftEffectsState()
        assertEquals(SendResult.Sent, GiftSender.send(s, giftById(1), 3, listOf(host), fromBag = true))
        assertEquals(2, DemoWallet.bag[1]); assertEquals(1_000, DemoWallet.coins)
        assertEquals(SendResult.NotInBag, GiftSender.send(s, giftById(1), 3, listOf(host), fromBag = true))
        GiftSender.send(s, giftById(1), 2, listOf(host), fromBag = true)
        assertNull("empty stacks leave the bag", DemoWallet.bag[1])
    }

    @Test fun vipGiftsAreLockedBelowTier() {
        val s = GiftEffectsState()
        assertEquals(SendResult.Locked, GiftSender.send(s, giftById(27), 1, listOf(host), fromBag = false, vipTier = 3))
    }

    /* ---------- catalog ---------- */

    @Test fun catalogIsConsistent() {
        assertEquals(sampleGifts.size, sampleGifts.map { it.id }.toSet().size)
        sampleGifts.filter { it.kind == GiftKind.Motion }.forEach { assertNotNull("${it.name} needs a style", it.motion) }
        assertTrue(sampleGifts.none { it.tab == GiftTab.Bag })
        sampleGifts.filter { it.vip > 0 }.forEach { assertEquals(GiftTab.Exclusive, it.tab) }
    }
}
