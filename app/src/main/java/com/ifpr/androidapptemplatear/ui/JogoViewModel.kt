package com.ifpr.androidapptemplatear.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ifpr.androidapptemplatear.jogo.Partida
import com.ifpr.androidapptemplatear.jogo.PontoTuristico

data class Placar(
    val pontuacao: Int,
    val encontrados: Int,
    val total: Int
)

/**
 * Mantém a partida viva enquanto a Activity existir
 * (sobrevive à troca de abas e à rotação da tela).
 */
class JogoViewModel : ViewModel() {

    private val partida = Partida()

    private val _placar = MutableLiveData(criarPlacar())
    val placar: LiveData<Placar> = _placar

    /** @return true se o ponto era novo e somou pontos. */
    fun registrarDescoberta(ponto: PontoTuristico): Boolean {
        val novo = partida.registrarDescoberta(ponto.id)

        if (novo) {
            _placar.value = criarPlacar()
        }

        return novo
    }

    private fun criarPlacar() = Placar(
        pontuacao = partida.pontuacao,
        encontrados = partida.quantidadeEncontrados,
        total = partida.totalPontos
    )
}
