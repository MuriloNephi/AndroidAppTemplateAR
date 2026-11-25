package com.ifpr.androidapptemplatear.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.os.SystemClock
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.ifpr.androidapptemplatear.jogo.Fase
import com.ifpr.androidapptemplatear.jogo.Partida
import com.ifpr.androidapptemplatear.jogo.PontoTuristico
import com.ifpr.androidapptemplatear.jogo.PontosTuristicos
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class LocalizacaoController(
    private val fragment: Fragment
) : DefaultLifecycleObserver, LocationListener {

    companion object {
        // Margem extra para sair do ponto; evita repetições por oscilação do GPS.
        private const val MARGEM_SAIDA = 25f

        private const val PRECISAO_MAXIMA = 50f
        private const val INTERVALO_ENDERECO = 30_000L
        private const val IDADE_MAXIMA_LOCALIZACAO = 30_000L
    }

    /** Pontos turísticos definidos em [PontosTuristicos]. */
    private val lugares = PontosTuristicos.lista

    private var manager: LocationManager? = null
    private var owner: LifecycleOwner? = null

    private var camada: FrameLayout? = null
    private var enderecoView: TextView? = null
    private var placarView: TextView? = null
    private var botao: Button? = null

    private var aoEncontrarPonto: ((PontoTuristico) -> Unit)? = null

    private var lugarAtual: PontoTuristico? = null

    private var monitorando = false

    // Falso após o Game Over: impede reativar o GPS até a nova partida.
    private var jogoAtivo = true

    private var enderecoAtual: String? = null

    private var enderecoJob: Job? = null
    private var ultimaConsultaEndereco: Location? = null
    private var instanteConsultaEndereco = 0L

    /*
     * Instancie este controlador como propriedade da CameraFragment,
     * antes de seus métodos de ciclo de vida.
     */
    private val permissoes = fragment.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        val telaVisivel = owner
            ?.lifecycle
            ?.currentState
            ?.isAtLeast(Lifecycle.State.STARTED) == true

        if (telaVisivel) {
            iniciar()
        }
    }

    fun conectar(
        parent: ViewGroup,
        aoEncontrar: (PontoTuristico) -> Unit
    ) {
        check(owner == null) {
            "O controlador já está conectado a uma tela."
        }

        val context = fragment.requireContext()

        owner = fragment.viewLifecycleOwner
        aoEncontrarPonto = aoEncontrar

        manager = context.getSystemService(
            Context.LOCATION_SERVICE
        ) as LocationManager

        criarPainel(parent)

        owner?.lifecycle?.addObserver(this)
    }

    /**
     * Cria o painel por código, sem alterar os layouts XML.
     */
    private fun criarPainel(parent: ViewGroup) {
        val context = fragment.requireContext()

        val painel = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL

            setPadding(
                dp(12),
                dp(10),
                dp(12),
                dp(10)
            )

            setBackgroundColor(
                Color.argb(205, 25, 30, 35)
            )
        }

        placarView = TextView(context).apply {
            text = "J1: 0 pts  •  J2: 0 pts"
            textSize = 16f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(Color.rgb(255, 214, 79))
        }

        enderecoView = TextView(context).apply {
            text = "Ative a localização para exibir o endereço atual."
            textSize = 14f
            setTextColor(Color.WHITE)
        }

        botao = Button(context).apply {
            text = "Ativar localização"

            setOnClickListener {
                if (temPermissaoPrecisa()) {
                    iniciar()
                } else {
                    permissoes.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }
        }

        painel.addView(placarView)
        painel.addView(enderecoView)
        painel.addView(botao)

        val layer = FrameLayout(context).apply {
            id = View.generateViewId()
        }

        layer.addView(
            painel,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.TOP
            ).apply {
                leftMargin = dp(12)
                rightMargin = dp(12)

                // Ajuste para posicionar abaixo do logotipo.
                topMargin = dp(170)
            }
        )

        val parametros = if (parent is ConstraintLayout) {
            ConstraintLayout.LayoutParams(0, 0).apply {
                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            }
        } else {
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        parent.addView(layer, parametros)
        camada = layer
    }

    override fun onStart(owner: LifecycleOwner) {
        if (!jogoAtivo) return

        if (temPermissaoPrecisa()) {
            iniciar()
        } else {
            mostrarAcao(
                mensagem = "Permita a localização precisa para continuar.",
                textoBotao = "Ativar localização"
            )
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        parar()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        parar()

        camada?.let { layer ->
            (layer.parent as? ViewGroup)?.removeView(layer)
        }

        owner.lifecycle.removeObserver(this)

        camada = null
        enderecoView = null
        placarView = null
        botao = null

        aoEncontrarPonto = null
        manager = null
        this.owner = null

        enderecoAtual = null
    }

    private fun temPermissaoPrecisa(): Boolean {
        val context = fragment.context ?: return false

        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun iniciar() {
        if (monitorando || !jogoAtivo) return

        if (!temPermissaoPrecisa()) {
            mostrarAcao(
                mensagem = "Permita a localização precisa para continuar.",
                textoBotao = "Ativar localização"
            )
            return
        }

        val locationManager = manager ?: return

        val provedores = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER
        ).filter { provedor ->
            runCatching {
                locationManager.isProviderEnabled(provedor)
            }.getOrDefault(false)
        }

        if (provedores.isEmpty()) {
            mostrarAcao(
                mensagem = "Ative a localização do aparelho.",
                textoBotao = "Tentar novamente"
            )
            return
        }

        var registrado = false

        for (provedor in provedores) {
            try {
                locationManager.requestLocationUpdates(
                    provedor,
                    5_000L,
                    5f,
                    this,
                    Looper.getMainLooper()
                )

                registrado = true

            } catch (_: SecurityException) {
                // A permissão pode ter sido revogada.

            } catch (_: IllegalArgumentException) {
                // O provedor pode ter ficado indisponível.
            }
        }

        monitorando = registrado

        if (registrado) {
            // Após ativar, o painel mostra somente o endereço.
            botao?.visibility = View.GONE
            enderecoView?.text = "Obtendo endereço atual…"

        } else {
            mostrarAcao(
                mensagem = "Não foi possível iniciar a localização.",
                textoBotao = "Tentar novamente"
            )
        }
    }

    private fun parar() {
        monitorando = false

        runCatching {
            manager?.removeUpdates(this)
        }

        enderecoJob?.cancel()
        enderecoJob = null

        ultimaConsultaEndereco = null
        instanteConsultaEndereco = 0L
    }

    private fun mostrarAcao(
        mensagem: String,
        textoBotao: String
    ) {
        enderecoView?.text = mensagem

        botao?.apply {
            text = textoBotao
            visibility = View.VISIBLE
            isEnabled = true
        }
    }

    override fun onLocationChanged(location: Location) {
        if (!monitorando) return

        val idadeMs = (
                SystemClock.elapsedRealtimeNanos() -
                        location.elapsedRealtimeNanos
                ) / 1_000_000L

        if (idadeMs !in 0L..IDADE_MAXIMA_LOCALIZACAO) {
            return
        }

        // O endereço é atualizado independentemente dos encontros.
        atualizarEndereco(location)

        // Exige boa precisão para detectar o raio de cada ponto.
        if (
            !location.hasAccuracy() ||
            location.accuracy > PRECISAO_MAXIMA
        ) {
            return
        }

        verificarLugares(location)
    }

    /**
     * Utiliza as coordenadas internamente, sem exibi-las na tela.
     */
    private fun verificarLugares(location: Location) {
        lugarAtual?.let { lugar ->
            // Mantém o mesmo encontro enquanto não sair do raio + margem.
            if (distancia(location, lugar) <= lugar.raioMetros + MARGEM_SAIDA) {
                return
            }

            lugarAtual = null
        }

        val lugarEncontrado = lugares
            .filter { lugar ->
                distancia(location, lugar) <= lugar.raioMetros
            }
            .minByOrNull { lugar ->
                distancia(location, lugar)
            }
            ?: return

        lugarAtual = lugarEncontrado

        aoEncontrarPonto?.invoke(lugarEncontrado)
    }

    /** Atualiza a HUD: pontos, vez, chances de quem procura e locais. */
    fun exibirPlacar(placar: Placar) {
        val vez = when {
            placar.gameOver -> "Fim de jogo"
            placar.fase == Fase.ESCONDENDO ->
                "Jogador ${placar.escondedor} esconde"
            else -> "Jogador ${placar.procurador} procura"
        }

        val chances =
            if (placar.fase == Fase.PROCURANDO || placar.gameOver) {
                "\nChances: " +
                        "♥".repeat(placar.chances) +
                        "♡".repeat(Partida.CHANCES_POR_RODADA - placar.chances)
            } else {
                ""
            }

        placarView?.text =
            "J1: ${placar.pontosJogador1} pts  •  " +
                    "J2: ${placar.pontosJogador2} pts\n" +
                    "Rodada ${placar.rodada}: $vez  •  " +
                    "Locais: ${placar.locaisVisitados}/${placar.totalLocais}" +
                    chances
    }

    /** Game Over: para o GPS e não aceita novos encontros. */
    fun pausarJogo() {
        jogoAtivo = false
        parar()
        lugarAtual = null
        botao?.visibility = View.GONE
    }

    /** Nova partida: libera os encontros e reativa o GPS. */
    fun retomarJogo() {
        jogoAtivo = true
        lugarAtual = null

        if (temPermissaoPrecisa()) {
            iniciar()
        } else {
            mostrarAcao(
                mensagem = "Permita a localização precisa para continuar.",
                textoBotao = "Ativar localização"
            )
        }
    }

    private fun distancia(
        location: Location,
        lugar: PontoTuristico
    ): Float {
        val resultado = FloatArray(1)

        Location.distanceBetween(
            location.latitude,
            location.longitude,
            lugar.latitude,
            lugar.longitude,
            resultado
        )

        return resultado[0]
    }

    /**
     * Consulta o endereço sem bloquear a interface.
     * Nunca exibe latitude ou longitude.
     */
    private fun atualizarEndereco(location: Location) {
        val agora = SystemClock.elapsedRealtime()
        val anterior = ultimaConsultaEndereco

        /*
         * Atualiza após 30 segundos ou deslocamento de 50 metros.
         * Evita consultar o serviço a cada atualização do GPS.
         */
        if (
            anterior != null &&
            agora - instanteConsultaEndereco < INTERVALO_ENDERECO &&
            anterior.distanceTo(location) < 50f
        ) {
            return
        }

        if (enderecoJob?.isActive == true) return

        ultimaConsultaEndereco = Location(location)
        instanteConsultaEndereco = agora

        val context = fragment.requireContext().applicationContext

        enderecoJob = owner?.lifecycleScope?.launch {
            val endereco = withContext(Dispatchers.IO) {
                try {
                    if (!Geocoder.isPresent()) {
                        null
                    } else {
                        /*
                         * A chamada síncrona roda na thread de IO
                         * para atender também Androids anteriores à API 33.
                         */
                        @Suppress("DEPRECATION")
                        val resultados = Geocoder(
                            context,
                            Locale("pt", "BR")
                        ).getFromLocation(
                            location.latitude,
                            location.longitude,
                            1
                        )

                        resultados
                            ?.firstOrNull()
                            ?.getAddressLine(0)
                            ?.takeIf { it.isNotBlank() }
                    }

                } catch (e: CancellationException) {
                    throw e

                } catch (_: Exception) {
                    null
                }
            }

            enderecoAtual = endereco

            enderecoView?.text = enderecoAtual
                ?: "Endereço atual indisponível."
        }
    }

    override fun onProviderDisabled(provider: String) {
        if (!monitorando) return

        val algumProvedorAtivo = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER
        ).any { nome ->
            runCatching {
                manager?.isProviderEnabled(nome) == true
            }.getOrDefault(false)
        }

        if (!algumProvedorAtivo) {
            parar()

            mostrarAcao(
                mensagem = "Localização desativada. Ative o GPS.",
                textoBotao = "Tentar novamente"
            )
        }
    }

    override fun onProviderEnabled(provider: String) = Unit

    @Deprecated("Callback utilizado em versões antigas do Android")
    override fun onStatusChanged(
        provider: String?,
        status: Int,
        extras: Bundle?
    ) = Unit

    private fun dp(valor: Int): Int {
        return (
                valor * fragment.resources.displayMetrics.density
                ).toInt()
    }
}