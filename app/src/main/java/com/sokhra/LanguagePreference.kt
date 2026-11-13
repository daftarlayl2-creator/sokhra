package com.sokhra

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

class LanguageManager(private val ctx: Context) {
    private val prefs: SharedPreferences = ctx.getSharedPreferences("sokhra_prefs", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = prefs.edit()

    enum class Language { ARABIC, DARIJA, FRENCH }

    private val langKey = "app_language"
    private val themeKey = "app_theme"

    fun getLanguage(): Language {
        val value = prefs.getString(langKey, "ARABIC") ?: "ARABIC"
        return when (value) {
            "DARIJA" -> Language.DARIJA
            "FRENCH" -> Language.FRENCH
            else -> Language.ARABIC
        }
    }

    fun setLanguage(language: Language) {
        val value = when (language) {
            Language.ARABIC -> "ARABIC"
            Language.DARIJA -> "DARIJA"
            Language.FRENCH -> "FRENCH"
        }
        editor.putString(langKey, value).apply()
        // Reset to force reload with new locale
        val ctx = ctx ?: return
        val pm = ctx.packageManager
        val intent = ctx.packageManager.getLaunchIntentForPackage(ctx.packageName)
        intent?.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        ctx.startActivity(intent)
    }

    fun getTheme(): Theme { // no-op, handled in MainActivity
        return Theme.SYSTEM
    }

    enum class Theme { LIGHT, DARK, SYSTEM }

    fun getStoredTheme(): Theme {
        val value = prefs.getString(themeKey, "SYSTEM") ?: "SYSTEM"
        return when (value) {
            "LIGHT" -> Theme.LIGHT
            "DARK" -> Theme.DARK
            else -> Theme.SYSTEM
        }
    }

    fun setTheme(theme: Theme) {
        val value = when (theme) {
            Theme.LIGHT -> "LIGHT"
            Theme.DARK -> "DARK"
            Theme.SYSTEM -> "SYSTEM"
        }
        editor.putString(themeKey, value).apply()
    }
}
