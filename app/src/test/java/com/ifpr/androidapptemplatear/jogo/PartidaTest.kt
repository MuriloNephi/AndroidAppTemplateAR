package com.ifpr.androidapptemplatear.jogo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PartidaTest {

    @Test
    fun comecaComJogador1Escondendo() {
        val partida = Partida()

        assertEquals(Fase.ESCONDENDO, partida.fase)
        assertEquals(0, partida.escondedor)
        assertEquals(1, partida.procurador)
    }

    @Test
    fun toqueAntesDeTerminarDeEsconderNaoConta() {
        val partida = Partida()

        assertEquals(ResultadoToque.IGNORADO, partida.registrarToque(true))
        assertEquals(0, partida.pontosDe(1))
    }

    @Test
    fun acharOPersonagemPontuaEInverteOsPapeis() {
        val partida = Partida()

        partida.terminarDeEsconder()
        assertEquals(ResultadoToque.ACHOU, partida.registrarToque(true))

        assertEquals(Partida.PONTOS_POR_ACHAR, partida.pontosDe(1))
        assertEquals(0, partida.pontosDe(0))
        assertEquals(1, partida.escondedor)
        assertEquals(Fase.ESCONDENDO, partida.fase)
        assertEquals(2, partida.rodada)
    }

    @Test
    fun errarTiraUmaChance() {
        val partida = Partida()

        partida.terminarDeEsconder()
        partida.registrarToque(false)

        assertEquals(Partida.CHANCES_POR_RODADA - 1, partida.chances)
        assertFalse(partida.encerrada)
    }

    @Test
    fun semChancesEhGameOverEInterrompeOJogo() {
        val partida = Partida()

        partida.terminarDeEsconder()
        repeat(Partida.CHANCES_POR_RODADA - 1) {
            assertEquals(ResultadoToque.ERROU, partida.registrarToque(false))
        }
        assertEquals(ResultadoToque.GAME_OVER, partida.registrarToque(false))

        assertTrue(partida.encerrada)
        assertEquals(1, partida.perdedor)
        assertEquals(0, partida.chances)

        // Depois do Game Over nada mais conta.
        assertEquals(ResultadoToque.IGNORADO, partida.registrarToque(true))
        assertFalse(partida.registrarVisita("catedral"))
        assertFalse(partida.terminarDeEsconder())
        assertEquals(0, partida.pontosDe(1))
    }

    @Test
    fun chancesRecomecamACadaRodada() {
        val partida = Partida()

        partida.terminarDeEsconder()
        partida.registrarToque(false)
        partida.registrarToque(false)
        partida.registrarToque(true) // Jogador 2 achou

        partida.terminarDeEsconder() // Jogador 2 escondeu
        assertEquals(Partida.CHANCES_POR_RODADA, partida.chances)
        assertEquals(0, partida.procurador)
    }

    @Test
    fun vencedorEhQuemTemMaisPontos() {
        val partida = Partida()

        assertEquals(null, partida.vencedor)

        partida.terminarDeEsconder()
        partida.registrarToque(true)

        assertEquals(1, partida.vencedor)
    }

    @Test
    fun errarNaoPontua() {
        val partida = Partida()

        partida.terminarDeEsconder()
        assertEquals(ResultadoToque.ERROU, partida.registrarToque(false))

        assertEquals(0, partida.pontosDe(1))
        assertEquals(Fase.PROCURANDO, partida.fase)
    }

    @Test
    fun voltarAEsconderDesfazOPronto() {
        val partida = Partida()

        assertFalse(partida.voltarAEsconder())

        partida.terminarDeEsconder()
        assertTrue(partida.voltarAEsconder())
        assertEquals(Fase.ESCONDENDO, partida.fase)
        assertEquals(0, partida.escondedor)
    }

    @Test
    fun bonusDeLocalSoNaPrimeiraVisitaParaQuemEstaComOCelular() {
        val partida = Partida()

        assertTrue(partida.registrarVisita("catedral"))
        assertFalse(partida.registrarVisita("catedral"))
        assertEquals(Partida.BONUS_LOCAL, partida.pontosDe(0))

        partida.terminarDeEsconder()
        assertTrue(partida.registrarVisita("museu"))
        assertEquals(Partida.BONUS_LOCAL, partida.pontosDe(1))
        assertEquals(2, partida.quantidadeLocaisVisitados)
    }

    @Test
    fun listaTemPeloMenosCincoPontosComIdsUnicos() {
        val lista = PontosTuristicos.lista

        assertTrue(lista.size >= 5)
        assertEquals(lista.size, lista.map { it.id }.toSet().size)
        assertTrue(lista.all { it.raioMetros > 0f })
    }
}
