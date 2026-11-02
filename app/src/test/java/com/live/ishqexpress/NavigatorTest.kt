package com.live.ishqexpress

import com.live.ishqexpress.ui.common.creatorById
import com.live.ishqexpress.ui.common.fanTiers
import com.live.ishqexpress.ui.common.grouped
import com.live.ishqexpress.ui.common.vipTiers
import com.live.ishqexpress.ui.golive.formatClock
import com.live.ishqexpress.ui.nav.Dest
import com.live.ishqexpress.ui.nav.Navigator
import com.live.ishqexpress.ui.nav.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigatorTest {

    private fun nav() = Navigator(listOf(Dest(Screen.Main)))

    @Test fun pushAndPop() {
        val n = nav()
        n.push(Screen.LiveViewer, 3)
        assertEquals(Dest(Screen.LiveViewer, 3), n.current)
        assertTrue(n.canPop)
        n.pop()
        assertEquals(Screen.Main, n.current.screen)
        assertFalse(n.canPop)
    }

    @Test fun popNeverRemovesRoot() {
        val n = nav()
        n.pop(); n.pop()
        assertEquals(1, n.stack.size)
    }

    @Test fun replaceSwapsTopOnly() {
        val n = nav()
        n.push(Screen.GoLiveSetup)
        n.replace(Screen.Broadcaster, 1)
        assertEquals(listOf(Dest(Screen.Main), Dest(Screen.Broadcaster, 1)), n.stack.toList())
    }

    @Test fun popToRoot() {
        val n = nav()
        n.push(Screen.Broadcaster); n.push(Screen.Battle, 6); n.replace(Screen.LiveSummary)
        n.popToRoot()
        assertEquals(listOf(Dest(Screen.Main)), n.stack.toList())
    }

    @Test fun saverRoundTrip() {
        val n = nav()
        n.push(Screen.OtherProfile, 9); n.push(Screen.ChatThread, 2)
        val saved = Navigator.saver.run { androidx.compose.runtime.saveable.SaverScope { true }.save(n) }!!
        val restored = Navigator.saver.restore(saved)!!
        assertEquals(n.stack.toList(), restored.stack.toList())
    }

    @Test fun everyScreenSurvivesSaver() {
        Screen.entries.forEach { s ->
            val n = Navigator(listOf(Dest(s, 42)))
            val saved = Navigator.saver.run { androidx.compose.runtime.saveable.SaverScope { true }.save(n) }!!
            assertEquals(Dest(s, 42), Navigator.saver.restore(saved)!!.current)
        }
    }
}

class SampleDataTest {
    @Test fun unknownCreatorFallsBack() = assertEquals(1, creatorById(999).id)
    @Test fun thirteenVipTiersAscending() {
        assertEquals(13, vipTiers.size)
        assertEquals(vipTiers.sortedBy { it.monthlyCoins }, vipTiers)
    }
    @Test fun fanTierDefaultsMatchDoc() = assertEquals(listOf(499, 999, 4_999), fanTiers.map { it.coins })
    @Test fun clockFormat() {
        assertEquals("00:05", formatClock(5))
        assertEquals("12:34", formatClock(754))
        assertEquals("1:02:03", formatClock(3723))
    }
    @Test fun grouping() = assertEquals("18,640", 18_640.grouped())
}
