package grapes.microservices.utils

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LanguageUtils {

    private const val PREFS_NAME = "language_preferences"
    private const val KEY_LANGUAGE_CODE = "language_code"

    /**
     * Retrieves the saved language from SharedPreferences (SharedPreferences is used to store
     * simple key-value pairs persistently.)
     * @param context The context used to access SharedPreferences.
     * @return The saved language code, or null if not found.
     */
    fun getSavedLanguage(context: Context): String {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_LANGUAGE_CODE, null)?:"en"
    }

    /**
     * Save the selected language in SharedPreferences (Percistent storage)
     * @param context The context used to access SharedPreferences.
     * @param languageCode The language to save.
     */
    fun setLanguage(context: Context, languageCode: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java)
                .applicationLocales = LocaleList.forLanguageTags(languageCode)
        }else{
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageCode))
        }
        saveLanguage(context, languageCode)
    }

    private fun saveLanguage(context: Context, languageCode: String) {
        val pref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        pref.edit().putString(KEY_LANGUAGE_CODE, languageCode).apply()
    }
}