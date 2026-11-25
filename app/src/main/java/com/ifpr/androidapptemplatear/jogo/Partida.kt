package com.ifpr.androidapptemplatear.jogo

/** Motivos que encerram a partida com derrota. */
enum class MotivoDerrota(val descricao: String) {
    TEMPO_ESGOTADO(
        "O tempo acabou antes de você encontrar um novo ponto turístico " +
                "e suas vidas chegaram a zero."
    )
}

/**
 * Estado de uma partida. Classe pura (sem Android) para facilitar os testes.
 *
 * Regras:
 * - Cada ponto turístico novo soma [PONTOS_POR_DESCOBERTA] (uma vez por partida).
 * - O jogador tem [VIDAS_INICIAIS] vidas e [TEMPO_POR_RODADA_MS] para achar
 *   um ponto novo. Se o tempo zerar, perde 1 vida e o cronômetro reinicia.
 * - Encontrar um ponto novo também reinicia o cronômetro.
 * - Com 0 vidas, a partida termina (Game Over).
 */
class Partida(
    val totalPontos: Int = PontosTuristicos.lista.size,
    private val tempoPorRodadaMs: Long = TEMPO_POR_RODADA_MS
) {

    companion object {
        /** Pontos ganhos ao descobrir um ponto turístico novo. */
        const val PONTOS_POR_DESCOBERTA = 10

        const val VIDAS_INICIAIS = 3

        /** Tempo para encontrar um ponto novo antes de perder uma vida (3 min). */
        const val TEMPO_POR_RODADA_MS = 3 * 60_000L
    }

    private val encontrados = mutableSetOf<String>()

    var pontuacao = 0
        private set

    var vidas = VIDAS_INICIAIS
        private set

    var tempoRestanteMs = tempoPorRodadaMs
        private set

    var motivoDerrota: MotivoDerrota? = null
        private set

    val quantidadeEncontrados: Int
        get() = encontrados.size

    val encerrada: Boolean
        get() = motivoDerrota != null

    /** Objetivo concluído: todos os pontos foram encontrados. */
    val concluida: Boolean
        get() = encontrados.size >= totalPontos

    /** O cronômetro só corre enquanto a partida está em andamento. */
    val emAndamento: Boolean
        get() = !encerrada && !concluida

    /**
     * Registra a chegada a um ponto.
     * Só pontua na primeira vez que o ponto é encontrado na partida.
     *
     * @return true se o ponto era novo e pontuou.
     */
    fun registrarDescoberta(idPonto: String): Boolean {
        if (encerrada) return false
        if (!encontrados.add(idPonto)) return false

        pontuacao += PONTOS_POR_DESCOBERTA
        tempoRestanteMs = tempoPorRodadaMs
        return true
    }

    fun jaEncontrado(idPonto: String): Boolean = idPonto in encontrados

    /**
     * Desconta o tempo decorrido do cronômetro.
     *
     * @return true se o jogador perdeu uma vida neste intervalo.
     */
    fun avancarTempo(decorridoMs: Long): Boolean {
        if (!emAndamento || decorridoMs <= 0L) return false

        tempoRestanteMs -= decorridoMs

        if (tempoRestanteMs > 0L) return false

        vidas--

        if (vidas <= 0) {
            vidas = 0
            tempoRestanteMs = 0L
            motivoDerrota = MotivoDerrota.TEMPO_ESGOTADO
        } else {
            tempoRestanteMs = tempoPorRodadaMs
        }

        return true
    }
}
