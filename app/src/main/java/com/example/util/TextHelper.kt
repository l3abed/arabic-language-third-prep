package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import java.util.Locale

object TextHelper {
    private val TASHKEEL_REGEX = Regex("[\\u064B-\\u0652\\u0670]")

    fun formatArabic(text: String, showTashkeel: Boolean = true): String {
        val cleaned = text.replace("_", " ")
        return if (showTashkeel) {
            cleaned
        } else {
            cleaned.replace(TASHKEEL_REGEX, "")
        }
    }
}

class ArabicAudioPlayer(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private fun ensureTtsInitialized() {
        if (tts == null) {
            try {
                tts = TextToSpeech(context.applicationContext, this)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val arLocale = Locale("ar")
            val result = tts?.setLanguage(arLocale)
            isInitialized = (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED)
        }
    }

    fun speak(text: String, speechRate: Float = 0.9f) {
        val cleanText = TextHelper.formatArabic(text, showTashkeel = true)
        ensureTtsInitialized()
        if (isInitialized && tts != null) {
            try {
                tts?.setSpeechRate(speechRate)
                tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "ArabicLessonUtterance")
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "الصوت: $cleanText", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "الصوت: $cleanText", Toast.LENGTH_SHORT).show()
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
        tts = null
        isInitialized = false
    }
}
