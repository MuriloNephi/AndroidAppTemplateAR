package com.ifpr.androidapptemplatear.jogo

/** Etapa da rodada de esconde-esconde. */
enum class Fase {
    /** Um jogador posiciona e pinta o personagem. */
    ESCONDENDO,

    /** O outro jogador toca na tela tentando achar o personagem. */
    PROCURANDO
}

/** Resultado de um toque na tela durante a partida. */
enum class ResultadoToque {
    /** Tocou no personagem: pontua e os papéis se invertem. */
    ACHOU,

    /** Tocou fora do personagem: perde uma chance. */
    ERROU,

    /** Errou e as chances acabaram: fim de jogo. */
    GAME_OVER,

    /** Toque fora da fase de procura (não conta). */
    IGNORADO
}

/**
 * Estado de uma partida de Hide and Seek para dois jogadores no mesmo celular.
 * Classe pura (sem Android) para facilitar os testes.
 *
 * Regras:
 * - Quem esconde posiciona e pinta o personagem e passa o celular.
 * - Quem procura toca na tela. Se tocar no personagem, ganha
 *   [PONTOS_POR_ACHAR] e passa a ser quem esconde na próxima rodada.
 * - Cada ponto turístico visitado pela primeira vez na partida dá
 *   [BONUS_LOCAL] ao jogador que está com o celular.
 * - Quem procura tem [CHANCES_POR_RODADA] chances. Cada toque fora do
 *   personagem tira uma; sem chances, a partida termina (Game Over).
 */
class Partida(
    val totalLocais: Int = PontosTuristicos.lista.size
) {

    companion object {
        const val JOGADORES = 2

        /** Pontos ganhos ao encontrar o personagem escondido. */
        const val PONTOS_POR_ACHAR = 10

        /** Pontos ganhos ao visitar um ponto turístico novo. */
        const val BONUS_LOCAL = 5

        /** Toques errados permitidos a quem procura, por rodada. */
        const val CHANCES_POR_RODADA = 3
    }

    /** Chances (vidas) restantes de quem procura. */
    var chances = CHANCES_POR_RODADA
        private set

    /** Jogador (0 ou 1) que ficou sem chances; null enquanto a partida segue. */
    var perdedor: Int? = null
        private set

    val encerrada: Boolean
        get() = perdedor != null

    /** Quem tem mais pontos (null em caso de empate). */
    val vencedor: Int?
        get() = when {
            pontos[0] > pontos[1] -> 0
            pontos[1] > pontos[0] -> 1
            else -> null
        }

    private val pontos = IntArray(JOGADORES)
    private val locaisVisitados = mutableSetOf<String>()

    /** Índice (0 ou 1) de quem esconde nesta rodada. */
    var escondedor = 0
        private set

    val procurador: Int
        get() = (escondedor + 1) % JOGADORES

    var fase = Fase.ESCONDENDO
        private set

    var rodada = 1
        private set

    /** Quem está segurando o celular agora. */
    val jogadorComCelular: Int
        get() = if (fase == Fase.ESCONDENDO) escondedor else procurador

    val quantidadeLocaisVisitados: Int
        get() = locaisVisitados.size

    fun pontosDe(jogador: Int): Int = pontos[jogador]

    /** Quem esconde terminou: o celular passa para quem procura. */
    fun terminarDeEsconder(): Boolean {
        if (encerrada || fase != Fase.ESCONDENDO) return false

        fase = Fase.PROCURANDO
        chances = CHANCES_POR_RODADA
        return true
    }

    /**
     * Desfaz o "Pronto" quando a cena 3D precisa ser recriada
     * (o personagem posicionado se perde): quem escondeu esconde de novo.
     *
     * @return true se estava procurando e voltou a esconder.
     */
    fun voltarAEsconder(): Boolean {
        if (encerrada || fase != Fase.PROCURANDO) return false

        fase = Fase.ESCONDENDO
        return true
    }

    /** Registra um toque de quem procura. */
    fun registrarToque(acertouPersonagem: Boolean): ResultadoToque {
        if (encerrada || fase != Fase.PROCURANDO) return ResultadoToque.IGNORADO

        if (!acertouPersonagem) {
            chances--

            if (chances > 0) return ResultadoToque.ERROU

            chances = 0
            perdedor = procurador
            return ResultadoToque.GAME_OVER
        }

        pontos[procurador] += PONTOS_POR_ACHAR

        // Quem achou passa a esconder.
        escondedor = procurador
        fase = Fase.ESCONDENDO
        rodada++

        return ResultadoToque.ACHOU
    }

    /**
     * Registra a chegada a um ponto turístico.
     * Só dá bônus na primeira visita da partida.
     *
     * @return true se o local era novo e somou o bônus.
     */
    fun registrarVisita(idPonto: String): Boolean {
        if (encerrada) return false
        if (!locaisVisitados.add(idPonto)) return false

        pontos[jogadorComCelular] += BONUS_LOCAL
        return true
    }
}
