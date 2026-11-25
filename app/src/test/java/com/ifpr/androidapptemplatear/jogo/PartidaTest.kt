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
    fun tempoEsgotadoTiraUmaVidaEReiniciaCronometro() {
        val partida = Partida(totalPontos = 5, tempoPorRodadaMs = 1_000L)

        assertFalse(partida.avancarTempo(999L))
        assertTrue(partida.avancarTempo(1L))

        assertEquals(Partida.VIDAS_INICIAIS - 1, partida.vidas)
        assertEquals(1_000L, partida.tempoRestanteMs)
        assertFalse(partida.encerrada)
    }

    @Test
    fun descobertaReiniciaCronometro() {
        val partida = Partida(totalPontos = 5, tempoPorRodadaMs = 1_000L)

        partida.avancarTempo(800L)
        partida.registrarDescoberta("a")

        assertEquals(1_000L, partida.tempoRestanteMs)
    }

    @Test
    fun semVidasEhGameOverEInterrompePontuacao() {
        val partida = Partida(totalPontos = 5, tempoPorRodadaMs = 1_000L)

        repeat(Partida.VIDAS_INICIAIS) { partida.avancarTempo(1_000L) }

        assertTrue(partida.encerrada)
        assertEquals(0, partida.vidas)
        assertEquals(MotivoDerrota.TEMPO_ESGOTADO, partida.motivoDerrota)

        // Depois do Game Over nada mais conta.
        assertFalse(partida.registrarDescoberta("a"))
        assertEquals(0, partida.pontuacao)
        assertFalse(partida.avancarTempo(1_000L))
    }

    @Test
    fun encontrarTodosOsPontosParaOCronometro() {
        val partida = Partida(totalPontos = 2, tempoPorRodadaMs = 1_000L)

        partida.registrarDescoberta("a")
        partida.registrarDescoberta("b")

        assertTrue(partida.concluida)
        assertFalse(partida.avancarTempo(5_000L))
        assertEquals(Partida.VIDAS_INICIAIS, partida.vidas)
    }

    @Test
    fun listaTemPeloMenosCincoPontosComIdsUnicos() {
        val lista = PontosTuristicos.lista

        assertTrue(lista.size >= 5)
        assertEquals(lista.size, lista.map { it.id }.toSet().size)
        assertTrue(lista.all { it.raioMetros > 0f && it.modelo.endsWith(".glb") })
    }
}
