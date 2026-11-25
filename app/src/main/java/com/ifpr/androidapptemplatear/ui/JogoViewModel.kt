package com.ifpr.androidapptemplatear.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ifpr.androidapptemplatear.jogo.Fase
import com.ifpr.androidapptemplatear.jogo.Partida
import com.ifpr.androidapptemplatear.jogo.PontoTuristico
import com.ifpr.androidapptemplatear.jogo.ResultadoToque

data class Placar(
    val pontosJogador1: Int,
    val pontosJogador2: Int,
    val fase: Fase,
    /** Número (1 ou 2) de quem esconde. */
    val escondedor: Int,
    /** Número (1 ou 2) de quem procura. */
    val procurador: Int,
    val rodada: Int,
    val locaisVisitados: Int,
    val totalLocais: Int,
    /** Chances restantes de quem procura. */
    val chances: Int,
    /** Número (1 ou 2) de quem perdeu; null enquanto a partida segue. */
    val perdedor: Int?,
    /** Número (1 ou 2) de quem tem mais pontos; null se empatado. */
    val vencedor: Int?
) {
    val gameOver: Boolean
        get() = perdedor != null
}

/**
 * Mantém a partida viva enquanto a Activity existir
 * (sobrevive à troca de abas e à rotação da tela).
 */
class JogoViewModel : ViewModel() {

    private var partida = Partida()

    private val _placar = MutableLiveData(criarPlacar())
    val placar: LiveData<Placar> = _placar

    fun terminarDeEsconder(): Boolean =
        partida.terminarDeEsconder().also { publicar() }

    fun voltarAEsconder(): Boolean =
        partida.voltarAEsconder().also { publicar() }

    fun registrarToque(acertouPersonagem: Boolean): ResultadoToque =
        partida.registrarToque(acertouPersonagem).also { publicar() }

    /** @return true se o local era novo e somou o bônus. */
    fun registrarVisita(ponto: PontoTuristico): Boolean =
        partida.registrarVisita(ponto.id).also { publicar() }

    /** Nova partida: zera pontos, chances, locais e papéis. */
    fun reiniciar() {
        partida = Partida()
        publicar()
    }

    private fun publicar() {
        _placar.value = criarPlacar()
    }

    private fun criarPlacar() = Placar(
        pontosJogador1 = partida.pontosDe(0),
        pontosJogador2 = partida.pontosDe(1),
        fase = partida.fase,
        escondedor = partida.escondedor + 1,
        procurador = partida.procurador + 1,
        rodada = partida.rodada,
        locaisVisitados = partida.quantidadeLocaisVisitados,
        totalLocais = partida.totalLocais,
        chances = partida.chances,
        perdedor = partida.perdedor?.plus(1),
        vencedor = partida.vencedor?.plus(1)
    )
}
