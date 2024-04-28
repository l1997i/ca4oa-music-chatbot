package com.luisli.cagpt.utils
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import timber.log.Timber
import java.util.Locale
import java.util.UUID
import java.util.concurrent.CountDownLatch
class TTSHelper(private val context: Context, private val onInitialized: Runnable?) {
    private var tts: TextToSpeech? = null
    init {
        initializeTTS()
    }
    fun isSpeaking(): Boolean {
        return tts != null && tts!!.isSpeaking
    }
        fun stopSpeaking() {
        if (tts != null) {
            tts!!.stop()
                                }
    }
    private fun initializeTTS() {
        tts = TextToSpeech(context) { status: Int ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts!!.setLanguage(Locale.UK)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Timber.tag("TTS").e("This Language is not supported")
                } else {
                    onInitialized?.run()
                }
            } else {
                Timber.tag("TTS").e("Initialization Failed!")
            }
        }
    }
    fun speakOut(text: String, latch: CountDownLatch) {
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "")
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, params, text.hashCode().toString())
        tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) {
                            }
            override fun onDone(utteranceId: String) {
                                latch.countDown()             }
            override fun onError(utteranceId: String) {
                                latch.countDown()             }
        })
    }
    fun speakOut_simple(text: String?) {
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }
    fun speakOut_Last(text: String?, onComplete: Runnable?) {
        if (tts != null) {
            val utteranceId = UUID.randomUUID().toString()
            val params = Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String) {
                    Timber.tag("TTS").d("Start Speaking")
                }
                override fun onDone(utteranceId: String) {
                    Timber.tag("TTS").d("TTS Finished Speaking")
                    Handler(Looper.getMainLooper()).post(onComplete!!)
                }
                override fun onError(utteranceId: String) {
                    Timber.tag("TTS").d("Error Speaking")
                }
            })
            tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        }
    }
    fun shutdown() {
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
    }
}
