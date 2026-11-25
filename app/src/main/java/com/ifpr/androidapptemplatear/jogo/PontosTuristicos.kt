package com.ifpr.androidapptemplatear.jogo

/**
 * Ponto turístico que dá bônus de pontos na primeira visita da partida.
 *
 * @param raioMetros distância máxima para considerar que o jogador chegou ao local.
 */
data class PontoTuristico(
    val id: String,
    val nome: String,
    val latitude: Double,
    val longitude: Double,
    val raioMetros: Float
)

/**
 * Lista única de pontos turísticos do jogo (Ponta Grossa/PR).
 * Coordenadas obtidas do OpenStreetMap (Nominatim).
 */
object PontosTuristicos {

    val lista: List<PontoTuristico> = listOf(
        PontoTuristico(
            id = "catedral-santana",
            nome = "Catedral Sant'Ana",
            latitude = -25.0982041,
            longitude = -50.1589514,
            raioMetros = 60f
        ),
        PontoTuristico(
            id = "museu-campos-gerais",
            nome = "Museu Campos Gerais",
            latitude = -25.0970006,
            longitude = -50.1585938,
            raioMetros = 50f
        ),
        PontoTuristico(
            id = "estacao-saudade",
            nome = "Estação Saudade",
            latitude = -25.0933828,
            longitude = -50.1532606,
            raioMetros = 60f
        ),
        PontoTuristico(
            id = "parque-ambiental",
            nome = "Parque Ambiental Governador Manoel Ribas",
            latitude = -25.0957335,
            longitude = -50.1549645,
            raioMetros = 80f
        ),
        PontoTuristico(
            id = "mansao-vila-hilda",
            nome = "Mansão Vila Hilda",
            latitude = -25.0917531,
            longitude = -50.1620343,
            raioMetros = 60f
        ),
        PontoTuristico(
            id = "buraco-do-padre",
            nome = "Buraco do Padre",
            latitude = -25.1719374,
            longitude = -49.9688266,
            raioMetros = 150f
        ),
        PontoTuristico(
            id = "vila-velha",
            nome = "Parque Estadual de Vila Velha",
            latitude = -25.2329936,
            longitude = -50.0116978,
            raioMetros = 300f
        )
    )
}
