package com.example.campushive.util

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object FavoritesStore {
    private const val PREFS_NAME = "campushive_favorites"

    private val _savedIds = MutableStateFlow<Set<String>>(emptySet())
    val savedIds: StateFlow<Set<String>> = _savedIds.asStateFlow()

    private var loadedUid: String? = null

    private fun keyFor(uid: String): String =
        if (uid.isBlank()) "saved_ids_guest" else "saved_ids_$uid"

    fun load(context: Context, uid: String) {
        if (loadedUid == uid) return
        loadedUid = uid
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val stored = prefs.getStringSet(keyFor(uid), emptySet()) ?: emptySet()
        _savedIds.value = stored.toSet()
    }

    fun toggle(context: Context, uid: String, listingId: String): Boolean {
        if (listingId.isBlank()) return false
        if (loadedUid != uid) {
            load(context, uid)
        }
        val current = _savedIds.value
        val nowSaved = !current.contains(listingId)
        val updated = if (nowSaved) current + listingId else current - listingId
        _savedIds.value = updated
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(keyFor(uid), updated)
            .apply()
        return nowSaved
    }

    fun isSaved(listingId: String): Boolean = _savedIds.value.contains(listingId)
}
