package com.live.ishqexpress.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.io.File

/**
 * The signed-in user, kept on the device so the app opens straight to Home after the first
 * sign-up. Compose observes every field, so editing the profile updates the whole app at once.
 *
 * In production the auth token from the backend is stored here too (in EncryptedSharedPreferences
 * or DataStore) and the profile is refreshed from the server on launch.
 */
object Session {
    private var prefs: SharedPreferences? = null

    var signedIn by mutableStateOf(false); private set
    var name by mutableStateOf("Suman"); private set
    var bio by mutableStateOf("Music lover 🎶 Night owl 🌙"); private set
    var city by mutableStateOf("Jaipur"); private set
    /** A local file:// uri, or null to use the sample avatar. */
    var avatar by mutableStateOf<String?>(null); private set
    var photos by mutableStateOf<List<String>>(emptyList()); private set
    var languages by mutableStateOf(listOf("Hindi", "English")); private set
    var interests by mutableStateOf<Set<String>>(emptySet()); private set

    /** Creator ids you blocked. Hidden from feeds; they can't message you. */
    val blocked = androidx.compose.runtime.mutableStateListOf<Int>()

    fun block(id: Int) { if (id !in blocked) { blocked.add(id); saveBlocked() } }
    fun unblock(id: Int) { blocked.remove(id); saveBlocked() }
    private fun saveBlocked() { prefs?.edit()?.putString("blocked", blocked.joinToString(","))?.apply() }

    /** Finished KYC steps: "doc", "selfie". Server-verified in production. */
    var kyc by mutableStateOf<Set<String>>(emptySet()); private set
    fun completeKyc(step: String) { kyc = kyc + step; prefs?.edit()?.putStringSet("kyc", kyc)?.apply() }

    fun init(context: Context) {
        if (prefs != null) return
        val p = context.applicationContext.getSharedPreferences("session", Context.MODE_PRIVATE)
        prefs = p
        signedIn = p.getBoolean("signedIn", false)
        name = p.getString("name", null) ?: name
        bio = p.getString("bio", null) ?: bio
        city = p.getString("city", null) ?: city
        avatar = p.getString("avatar", null)
        photos = p.getString("photos", null)?.split('\n')?.filter { it.isNotBlank() } ?: emptyList()
        languages = p.getString("languages", null)?.split(',')?.filter { it.isNotBlank() } ?: languages
        interests = p.getStringSet("interests", null) ?: emptySet()
        kyc = p.getStringSet("kyc", null) ?: emptySet()
        blocked.clear()
        blocked.addAll((p.getString("blocked", null) ?: "2,8,11").split(',').mapNotNull { it.toIntOrNull() })
    }

    fun signIn(name: String, avatar: String?, interests: Set<String>) {
        if (name.isNotBlank()) this.name = name.trim()
        this.avatar = avatar
        this.photos = listOfNotNull(avatar)
        this.interests = interests
        signedIn = true
        save()
    }

    fun updateProfile(name: String, bio: String, city: String, photos: List<String>, languages: List<String>) {
        if (name.isNotBlank()) this.name = name.trim()
        this.bio = bio.trim()
        this.city = city.trim()
        this.photos = photos
        this.avatar = photos.firstOrNull()
        this.languages = languages
        save()
    }

    /** Clears the account from this device. The next screen is Welcome. */
    fun signOut() {
        prefs?.edit()?.clear()?.apply()
        name = "Suman"; bio = "Music lover 🎶 Night owl 🌙"; city = "Jaipur"
        avatar = null; photos = emptyList(); interests = emptySet(); blocked.clear(); kyc = emptySet()
        signedIn = false
    }

    private fun save() {
        prefs?.edit()
            ?.putBoolean("signedIn", signedIn)
            ?.putString("name", name)
            ?.putString("bio", bio)
            ?.putString("city", city)
            ?.putString("avatar", avatar)
            ?.putString("photos", photos.joinToString("\n"))
            ?.putString("languages", languages.joinToString(","))
            ?.putStringSet("interests", interests)
            ?.apply()
    }

    /** Verified phone as "+91 9876543210"; shown masked in settings. */
    fun maskedPhone(): String {
        val p = string("phone", "+91 9876543210")
        val dial = p.substringBefore(' '); val n = p.substringAfter(' ')
        return if (n.length < 5) p else "$dial ${n.take(2)}••• ••${n.takeLast(3)}"
    }

    /** A boolean setting that survives restarts (falls back to memory in previews and tests). */
    fun flag(key: String, default: Boolean): Boolean = prefs?.getBoolean("flag.$key", default) ?: default
    fun setFlag(key: String, value: Boolean) { prefs?.edit()?.putBoolean("flag.$key", value)?.apply() }
    fun int(key: String, default: Int): Int = prefs?.getInt("int.$key", default) ?: default
    fun setInt(key: String, value: Int) { prefs?.edit()?.putInt("int.$key", value)?.apply() }
    fun string(key: String, default: String): String = prefs?.getString("str.$key", default) ?: default
    fun setString(key: String, value: String) { prefs?.edit()?.putString("str.$key", value)?.apply() }

    /**
     * Copies a picked image (content:// from the photo picker, which can expire) into app storage
     * and returns a stable file:// uri. Returns the original uri if copying fails.
     */
    fun importImage(context: Context, uri: Uri): String = runCatching {
        val dir = File(context.filesDir, "images").apply { mkdirs() }
        val out = File(dir, "img_${System.currentTimeMillis()}_${(0..9999).random()}.jpg")
        context.contentResolver.openInputStream(uri)!!.use { input -> out.outputStream().use { input.copyTo(it) } }
        Uri.fromFile(out).toString()
    }.getOrElse { uri.toString() }
}

/** `var x by rememberFlag("key", true)` — a Compose state saved to the device. */
@Composable
fun rememberFlag(key: String, default: Boolean): MutableState<Boolean> = remember(key) {
    object : MutableState<Boolean> {
        private val state = mutableStateOf(Session.flag(key, default))
        override var value: Boolean
            get() = state.value
            set(v) { state.value = v; Session.setFlag(key, v) }
        override fun component1() = value
        override fun component2(): (Boolean) -> Unit = { value = it }
    }
}

@Composable
fun rememberIntPref(key: String, default: Int): MutableState<Int> = remember(key) {
    object : MutableState<Int> {
        private val state = mutableStateOf(Session.int(key, default))
        override var value: Int
            get() = state.value
            set(v) { state.value = v; Session.setInt(key, v) }
        override fun component1() = value
        override fun component2(): (Int) -> Unit = { value = it }
    }
}

@Composable
fun rememberStringPref(key: String, default: String): MutableState<String> = remember(key) {
    object : MutableState<String> {
        private val state = mutableStateOf(Session.string(key, default))
        override var value: String
            get() = state.value
            set(v) { state.value = v; Session.setString(key, v) }
        override fun component1() = value
        override fun component2(): (String) -> Unit = { value = it }
    }
}
