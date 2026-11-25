package com.ifpr.androidapptemplatear.ui

import android.os.SystemClock
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ifpr.androidapptemplatear.jogo.MotivoDerrota
import com.ifpr.androidapptemplatear.jogo.Partida
import com.ifpr.androidapptemplatear.jogo.PontoTuristico
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class Placar(
    val pontuacao: Int,
    val encontrados: Int,
    val total: Int,
    val vidas: Int,
    val tempoRestanteMs: Long,
    val concluida: Boolean,
    val motivoDerrota: MotivoDerrota?
) {
    val gameOver: Boolean
        get() = motivoDerrota != null
}

/**
 * Mantém a partida viva enquanto a Activity existir
 * (sobrevive à troca de abas e à rotação da tela).
 */
class JogoViewModel : ViewModel() {

    private var partida = Partida()

    private val _placar = MutableLiveData(criarPlacar())
    val placar: LiveData<Placar> = _placar

    private var cronometroJob: Job? = null

    /** @return true se o ponto era novo e somou pontos. */
    fun registrarDescoberta(ponto: PontoTuristico): Boolean {
        val novo = partida.registrarDescoberta(ponto.id)

        if (novo) {
            publicar()
        }

        return novo
    }

    /** Inicia (ou retoma) o cronômetro enquanto a tela do jogo está visível. */
    fun retomarCronometro() {
        if (cronometroJob?.isActive == true || !partida.emAndamento) return

        cronometroJob = viewModelScope.launch {
            var anterior = SystemClock.elapsedRealtime()

            while (isActive && partida.emAndamento) {
                delay(1_000L)

                val agora = SystemClock.elapsedRealtime()
                partida.avancarTempo(agora - anterior)
                anterior = agora

                publicar()
            }
        }
    }

    fun pausarCronometro() {
        cronometroJob?.cancel()
        cronometroJob = null
    }

    /** Começa uma partida nova: zera pontos, vidas, locais e cronômetro. */
    fun reiniciar() {
        pausarCronometro()
        partida = Partida()
        publicar()
    }

    private fun publicar() {
        _placar.value = criarPlacar()
    }

    private fun criarPlacar() = Placar(
        pontuacao = partida.pontuacao,
        encontrados = partida.quantidadeEncontrados,
        total = partida.totalPontos,
        vidas = partida.vidas,
        tempoRestanteMs = partida.tempoRestanteMs,
        concluida = partida.concluida,
        motivoDerrota = partida.motivoDerrota
    )
}
