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

    init {
        tts = TextToSpeech(context.applicationContext, this)
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
        if (isInitialized && tts != null) {
            tts?.setSpeechRate(speechRate)
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "ArabicLessonUtterance")
        } else {
            Toast.makeText(context, "الصوت: $cleanText", Toast.LENGTH_SHORT).show()
        }
    }

    fun stop() {
        tts?.stop()
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
