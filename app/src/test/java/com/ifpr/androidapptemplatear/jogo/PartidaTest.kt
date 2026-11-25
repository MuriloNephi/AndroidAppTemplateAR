package com.ifpr.androidapptemplatear.jogo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PartidaTest {

    @Test
    fun novoPontoSomaPontos() {
        val partida = Partida(totalPontos = 5)

        assertTrue(partida.registrarDescoberta("a"))
        assertEquals(Partida.PONTOS_POR_DESCOBERTA, partida.pontuacao)
        assertEquals(1, partida.quantidadeEncontrados)
    }

    @Test
    fun pontoRepetidoNaoPontuaDeNovo() {
        val partida = Partida(totalPontos = 5)

        partida.registrarDescoberta("a")
        assertFalse(partida.registrarDescoberta("a"))

        assertEquals(Partida.PONTOS_POR_DESCOBERTA, partida.pontuacao)
        assertEquals(1, partida.quantidadeEncontrados)
    }

    @Test
    fun pontosDiferentesAcumulam() {
        val partida = Partida(totalPontos = 5)

        partida.registrarDescoberta("a")
        partida.registrarDescoberta("b")
        partida.registrarDescoberta("c")

        assertEquals(3 * Partida.PONTOS_POR_DESCOBERTA, partida.pontuacao)
    }

    @Test
    fun listaTemPeloMenosCincoPontosComIdsUnicos() {
        val lista = PontosTuristicos.lista

        assertTrue(lista.size >= 5)
        assertEquals(lista.size, lista.map { it.id }.toSet().size)
        assertTrue(lista.all { it.raioMetros > 0f && it.modelo.endsWith(".glb") })
    }
}
