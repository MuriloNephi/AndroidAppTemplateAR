package com.ifpr.androidapptemplatear.jogo

/**
 * Estado de uma partida. Classe pura (sem Android) para facilitar os testes.
 */
class Partida(
    val totalPontos: Int = PontosTuristicos.lista.size
) {

    companion object {
        /** Pontos ganhos ao descobrir um ponto turístico novo. */
        const val PONTOS_POR_DESCOBERTA = 10
    }

    private val encontrados = mutableSetOf<String>()

    var pontuacao = 0
        private set

    val quantidadeEncontrados: Int
        get() = encontrados.size

    /**
     * Registra a chegada a um ponto.
     * Só pontua na primeira vez que o ponto é encontrado na partida.
     *
     * @return true se o ponto era novo e pontuou.
     */
    fun registrarDescoberta(idPonto: String): Boolean {
        if (!encontrados.add(idPonto)) return false

        pontuacao += PONTOS_POR_DESCOBERTA
        return true
    }

    fun jaEncontrado(idPonto: String): Boolean = idPonto in encontrados
}
