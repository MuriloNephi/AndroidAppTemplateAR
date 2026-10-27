package com.ifpr.androidapptemplatear

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper

class SoundPlayer(private val context: Context) {

    private var player: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())

    // Duração máxima do som: 2 segundos
    private val duracaoMaxima = 2000L

    fun tocarParaModelo(nomeModelo: String) {
        val nome = nomeModelo.substringAfterLast('/').substringBeforeLast('.')
        val resId = context.resources.getIdentifier(nome, "raw", context.packageName)
        if (resId == 0) return

        handler.removeCallbacksAndMessages(null)
        player?.release()
        player = MediaPlayer.create(context, resId)?.apply { start() }

        handler.postDelayed({ parar() }, duracaoMaxima)
    }

    private fun parar() {
        player?.release()
        player = null
    }

    fun liberar() {
        handler.removeCallbacksAndMessages(null)
        parar()
    }
}