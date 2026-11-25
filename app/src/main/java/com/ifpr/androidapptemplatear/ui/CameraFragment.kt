package com.ifpr.androidapptemplatear.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.android.filament.Colors
import com.ifpr.androidapptemplatear.R
import com.ifpr.androidapptemplatear.SoundPlayer
import com.ifpr.androidapptemplatear.jogo.Fase
import com.ifpr.androidapptemplatear.jogo.Partida
import com.ifpr.androidapptemplatear.jogo.ResultadoToque
import io.github.sceneview.SceneView
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class CameraFragment : Fragment() {

    private enum class Mode {
        CHECKING,
        AR,
        CAMERA,
        STATIC
    }

    companion object {
        private const val AVAILABILITY_TIMEOUT_MS = 4000L
        private const val AR_TIMEOUT_MS = 15000L
        private const val CAMERA_TIMEOUT_MS = 8000L
        private const val MODELO_INICIAL = "camaleao_pose.glb"

        /** Cores da paleta para disfarçar o personagem (null = cor original). */
        private val PALETA: List<Int?> = listOf(
            null,
            Color.rgb(46, 125, 50),   // verde
            Color.rgb(121, 85, 72),   // marrom
            Color.rgb(158, 158, 158), // cinza
            Color.rgb(245, 245, 245), // branco
            Color.rgb(33, 33, 33),    // preto
            Color.rgb(25, 118, 210),  // azul
            Color.rgb(229, 57, 53),   // vermelho
            Color.rgb(253, 216, 53)   // amarelo
        )
    }

    private val localizacaoController = LocalizacaoController(this)

    private var mode = Mode.CHECKING

    private var container: FrameLayout? = null
    private var placeButton: ExtendedFloatingActionButton? = null
    private var statusText: TextView? = null
    private var overlay: View? = null

    private var sceneView: SceneView? = null
    private var arView: ARSceneView? = null

    private var modelNode: ModelNode? = null
    private var anchorNode: AnchorNode? = null
    private var placementHit: HitResult? = null

    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraPreview: Preview? = null

    private var loadJob: Job? = null
    private var availabilityJob: Job? = null
    private var arWatchdogJob: Job? = null
    private var cameraWatchdogJob: Job? = null

    private var generation = 0
    private var modelGeneration = 0
    private var pendingAr = false

    private var arTracking = false
    private var cameraStreaming = false

    private var currentModelFile = MODELO_INICIAL

    private lateinit var sharedViewModel: SharedModelViewModel
    private lateinit var jogoViewModel: JogoViewModel

    // Controles de quem esconde.
    private var paletaView: View? = null
    private var prontoButton: Button? = null
    private var passarOverlay: View? = null
    private var passarTexto: TextView? = null

    /** Cor aplicada ao personagem nesta rodada (null = original). */
    private var corAtual: Int? = null

    private var somPlayer: SoundPlayer? = null

    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (container != null) {
                when {
                    !granted -> startPreview(useCamera = false)
                    pendingAr -> startAr()
                    else -> startPreview(useCamera = true)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_camera,
            parent,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        container = view.findViewById(R.id.sceneContainer)
        placeButton = view.findViewById(R.id.place)

        createControls()

        sharedViewModel =
            ViewModelProvider(requireActivity())[
                SharedModelViewModel::class.java
            ]

        val selectedModel = sharedViewModel.selectedModel.value

        if (!selectedModel.isNullOrBlank()) {
            currentModelFile = selectedModel
        } else {
            sharedViewModel.selectModel(currentModelFile)
        }

        sharedViewModel.selectedModel.observe(viewLifecycleOwner) { path ->
            if (!path.isNullOrBlank() && path != currentModelFile) {
                currentModelFile = path
                loadModel()
            }
        }

        placeButton?.setOnClickListener {
            when (mode) {
                Mode.AR -> placeModel()

                Mode.CAMERA,
                Mode.STATIC -> {
                    startPreview(useCamera = mode == Mode.CAMERA)
                }

                Mode.CHECKING -> Unit
            }
        }

        jogoViewModel =
            ViewModelProvider(requireActivity())[
                JogoViewModel::class.java
            ]

        somPlayer = SoundPlayer(requireContext())

        passarOverlay = view.findViewById(R.id.passarOverlay)
        passarTexto = view.findViewById(R.id.passarTexto)

        view.findViewById<View>(R.id.comecarProcura).setOnClickListener {
            passarOverlay?.visibility = View.GONE
            statusText?.text = "Toque onde você acha que o personagem está."
        }

        // A tela foi recriada no meio da procura: o personagem posicionado
        // se perdeu, então quem escondeu precisa esconder de novo.
        voltarAEsconderSeNecessario()

        checkAvailability()

        val parent = container?.parent as? ViewGroup

        if (parent != null) {
            localizacaoController.conectar(parent) { ponto ->
                val jogador =
                    (jogoViewModel.placar.value?.let { placar ->
                        if (placar.fase == Fase.ESCONDENDO) {
                            placar.escondedor
                        } else {
                            placar.procurador
                        }
                    }) ?: 1

                if (jogoViewModel.registrarVisita(ponto)) {
                    statusText?.text =
                        "+${Partida.BONUS_LOCAL} pontos para o Jogador " +
                                "$jogador: você visitou ${ponto.nome}!"
                }
            }

            placeButton?.bringToFront()
        }

        jogoViewModel.placar.observe(viewLifecycleOwner) { placar ->
            localizacaoController.exibirPlacar(placar)
            atualizarControles(placar)
        }
    }

    /** Mostra os controles de quem esconde só na fase de esconder. */
    private fun atualizarControles(placar: Placar) {
        val escondendo = placar.fase == Fase.ESCONDENDO
        val visibilidade = if (escondendo) View.VISIBLE else View.GONE

        paletaView?.visibility = visibilidade
        prontoButton?.visibility = visibilidade
        placeButton?.visibility = visibilidade

        // Quem procura não pode arrastar o personagem.
        modelNode?.isEditable = escondendo
    }

    private fun voltarAEsconderSeNecessario() {
        if (jogoViewModel.voltarAEsconder()) {
            passarOverlay?.visibility = View.GONE
            corAtual = null

            val jogador = jogoViewModel.placar.value?.escondedor ?: 1
            statusText?.text =
                "A cena foi reiniciada. Jogador $jogador, esconda de novo."
        }
    }

    /** Quem esconde terminou: cobre a tela para passar o celular. */
    private fun terminarDeEsconder() {
        val posicionado = when (mode) {
            Mode.AR -> anchorNode != null
            Mode.CAMERA, Mode.STATIC -> modelNode != null
            Mode.CHECKING -> false
        }

        if (!posicionado) {
            statusText?.text = "Posicione o personagem antes de passar o celular."
            return
        }

        if (!jogoViewModel.terminarDeEsconder()) return

        val procurador = jogoViewModel.placar.value?.procurador ?: 2

        passarTexto?.text =
            "Passe o celular para o Jogador $procurador.\n\n" +
                    "Jogador $procurador: encontre o personagem e toque nele!"

        passarOverlay?.apply {
            visibility = View.VISIBLE
            bringToFront()
        }
    }

    /** Toque na cena: só conta durante a procura. */
    private fun aoTocarNaCena(node: Node?) {
        val placar = jogoViewModel.placar.value ?: return
        if (placar.fase != Fase.PROCURANDO) return
        if (passarOverlay?.visibility == View.VISIBLE) return

        val acertou = node != null && pertenceAoPersonagem(node)

        when (jogoViewModel.registrarToque(acertou)) {
            ResultadoToque.ACHOU -> {
                somPlayer?.tocarParaModelo(currentModelFile)
                corAtual = null

                statusText?.text =
                    "Jogador ${placar.procurador} achou! " +
                            "+${Partida.PONTOS_POR_ACHAR} pontos. " +
                            "Agora é a sua vez de esconder."

                // Nova rodada: o personagem volta a ser posicionado.
                loadModel()
            }

            ResultadoToque.ERROU -> {
                statusText?.text = "Errou! O personagem não está aí."
            }

            ResultadoToque.IGNORADO -> Unit
        }
    }

    private fun pertenceAoPersonagem(node: Node): Boolean {
        val personagem = modelNode ?: return false
        var atual: Node? = node

        while (atual != null) {
            if (atual === personagem) return true
            atual = atual.parent
        }

        return false
    }

    private fun configurarToque(scene: SceneView) {
        scene.setOnGestureListener(
            onSingleTapConfirmed = { _, node -> aoTocarNaCena(node) }
        )
    }

    /** Pinta o personagem (null = cor original do modelo). */
    private fun aplicarCor(cor: Int?) {
        corAtual = cor

        val node = modelNode ?: return

        val r = if (cor == null) 1f else Color.red(cor) / 255f
        val g = if (cor == null) 1f else Color.green(cor) / 255f
        val b = if (cor == null) 1f else Color.blue(cor) / 255f

        node.materialInstances.flatten().forEach { material ->
            runCatching {
                material.setParameter(
                    "baseColorFactor",
                    Colors.RgbaType.SRGB,
                    r, g, b, 1f
                )
            }
        }
    }

    private fun createControls() {
        val parent = container?.parent as? ViewGroup ?: return

        val layer = FrameLayout(requireContext())

        val panel = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(10), dp(12), dp(10))
            setBackgroundColor(Color.argb(190, 25, 30, 35))
        }

        statusText = TextView(requireContext()).apply {
            text = "Preparando visualização…"
            textSize = 13f
            setTextColor(Color.WHITE)
        }

        panel.addView(statusText)

        // Paleta: quem esconde pinta o personagem para disfarçá-lo.
        val paleta = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(8), 0, 0)
        }

        PALETA.forEach { cor ->
            val amostra = TextView(requireContext()).apply {
                gravity = Gravity.CENTER
                textSize = 11f
                text = if (cor == null) "orig." else ""
                setTextColor(Color.BLACK)

                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(cor ?: Color.rgb(220, 220, 220))
                    setStroke(dp(2), Color.WHITE)
                }

                contentDescription =
                    if (cor == null) "Cor original" else "Pintar o personagem"

                setOnClickListener { aplicarCor(cor) }
            }

            paleta.addView(
                amostra,
                LinearLayout.LayoutParams(0, dp(32), 1f).apply {
                    marginEnd = dp(4)
                }
            )
        }

        panel.addView(paleta)
        paletaView = paleta

        prontoButton = Button(requireContext()).apply {
            text = "Pronto! Passar o celular"
            setOnClickListener { terminarDeEsconder() }
        }

        panel.addView(prontoButton)

        layer.addView(
            panel,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
            ).apply {
                leftMargin = dp(12)
                rightMargin = dp(12)
                bottomMargin = dp(85)
            }
        )

        parent.addView(
            layer,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        overlay = layer

        parent.findViewById<View>(R.id.titleImage)?.bringToFront()
        placeButton?.bringToFront()
    }

    /**
     * Identificação por características comuns de emuladores.
     * Por escolha deste aplicativo, eles iniciam sem ARCore.
     */
    private fun isEmulator(): Boolean {
        return Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.startsWith("unknown") ||
                Build.MODEL.contains("google_sdk", ignoreCase = true) ||
                Build.MODEL.contains("Emulator", ignoreCase = true) ||
                Build.MODEL.contains(
                    "Android SDK built for",
                    ignoreCase = true
                ) ||
                Build.PRODUCT.contains("sdk", ignoreCase = true) ||
                Build.HARDWARE.contains("goldfish", ignoreCase = true) ||
                Build.HARDWARE.contains("ranchu", ignoreCase = true)
    }

    private fun checkAvailability() {
        availabilityJob?.cancel()

        // Não tenta iniciar ARCore no emulador.
        if (isEmulator()) {
            requestMode(useAr = false)
            return
        }

        val expected = generation
        var resolved = false

        availabilityJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(AVAILABILITY_TIMEOUT_MS)

            if (
                !resolved &&
                container != null &&
                expected == generation
            ) {
                resolved = true
                requestMode(useAr = false)
            }
        }

        try {
            ArCoreApk.getInstance()
                .checkAvailabilityAsync(requireContext()) { availability ->
                    activity?.runOnUiThread {
                        if (
                            !resolved &&
                            container != null &&
                            expected == generation
                        ) {
                            resolved = true
                            availabilityJob?.cancel()
                            availabilityJob = null

                            val installed =
                                availability ==
                                        ArCoreApk.Availability.SUPPORTED_INSTALLED

                            requestMode(useAr = installed)
                        }
                    }
                }
        } catch (_: Exception) {
            if (
                !resolved &&
                container != null &&
                expected == generation
            ) {
                resolved = true
                availabilityJob?.cancel()
                availabilityJob = null

                requestMode(useAr = false)
            }
        }
    }

    private fun requestMode(useAr: Boolean) {
        pendingAr = useAr

        val permitted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        when {
            !permitted -> {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }

            useAr -> startAr()

            else -> startPreview(useCamera = true)
        }
    }

    private fun startAr() {
        val host = container ?: return

        clearScene()
        voltarAEsconderSeNecessario()
        mode = Mode.AR

        val expected = generation

        statusText?.text = "Aponte para uma mesa ou para o chão."

        placeButton?.apply {
            text = "Posicionar"
            isEnabled = false
        }

        try {
            val ar = ARSceneView(requireContext())

            arView = ar
            sceneView = ar

            ar.configureSession { _, config ->
                config.planeFindingMode =
                    Config.PlaneFindingMode.HORIZONTAL

                config.lightEstimationMode =
                    Config.LightEstimationMode.DISABLED
            }

            ar.onSessionFailed = {
                host.post {
                    if (
                        container != null &&
                        expected == generation &&
                        mode == Mode.AR
                    ) {
                        startPreview(useCamera = true)
                    }
                }
            }

            ar.onSessionUpdated = { _, frame ->
                if (
                    expected == generation &&
                    mode == Mode.AR
                ) {
                    arTracking =
                        frame.camera.trackingState ==
                                TrackingState.TRACKING

                    if (anchorNode == null) {
                        placementHit =
                            if (
                                arTracking &&
                                ar.width > 0 &&
                                ar.height > 0
                            ) {
                                frame.hitTest(
                                    ar.width / 2f,
                                    ar.height / 2f
                                ).firstOrNull { hit ->
                                    val plane = hit.trackable as? Plane

                                    plane != null &&
                                            plane.type ==
                                            Plane.Type.HORIZONTAL_UPWARD_FACING &&
                                            plane.isPoseInPolygon(hit.hitPose)
                                }
                            } else {
                                null
                            }

                        val ready =
                            placementHit != null &&
                                    modelNode != null

                        placeButton?.isEnabled = ready

                        statusText?.text =
                            if (ready) {
                                "Superfície encontrada. Toque em Posicionar."
                            } else {
                                "Mova o aparelho devagar sobre uma superfície."
                            }
                    }
                }
            }

            ar.lifecycle = viewLifecycleOwner.lifecycle
            configurarToque(ar)

            host.addView(ar, fullSize())

            startArWatchdog(expected)
            loadModel()

        } catch (_: Exception) {
            startPreview(useCamera = true)
        }
    }

    /**
     * Troca automaticamente quando o ARCore não consegue rastrear.
     * Conta somente o tempo em que a tela está em primeiro plano.
     * A ausência de uma superfície, sozinha, não provoca a troca.
     */
    private fun startArWatchdog(expected: Int) {
        arWatchdogJob?.cancel()

        arWatchdogJob = viewLifecycleOwner.lifecycleScope.launch {
            var withoutTrackingMs = 0L
            var lastTime = SystemClock.elapsedRealtime()

            while (isActive) {
                delay(500L)

                if (
                    container == null ||
                    expected != generation ||
                    mode != Mode.AR
                ) {
                    return@launch
                }

                val now = SystemClock.elapsedRealtime()
                val elapsed = now - lastTime
                lastTime = now

                val resumed = viewLifecycleOwner.lifecycle
                    .currentState.isAtLeast(Lifecycle.State.RESUMED)

                if (!resumed || arTracking) {
                    withoutTrackingMs = 0L
                } else {
                    withoutTrackingMs += elapsed
                }

                if (withoutTrackingMs >= AR_TIMEOUT_MS) {
                    startPreview(useCamera = true)
                    return@launch
                }
            }
        }
    }

    private fun startPreview(useCamera: Boolean) {
        val host = container ?: return

        clearScene()
        voltarAEsconderSeNecessario()

        mode = if (useCamera) Mode.CAMERA else Mode.STATIC

        placeButton?.apply {
            text = "Centralizar"
            isEnabled = false
        }

        statusText?.text = if (useCamera) {
            "Modelo sobre a câmera, sem fixação no ambiente."
        } else {
            "Visualização 3D sem câmera."
        }

        host.setBackgroundColor(Color.rgb(45, 55, 65))

        val cameraView = if (useCamera) {
            PreviewView(requireContext()).apply {
                implementationMode =
                    PreviewView.ImplementationMode.COMPATIBLE

                scaleType = PreviewView.ScaleType.FILL_CENTER

                host.addView(this, fullSize())
            }
        } else {
            null
        }

        try {
            val scene = SceneView(
                context = requireContext(),
                isOpaque = !useCamera
            )

            scene.lifecycle = viewLifecycleOwner.lifecycle
            configurarToque(scene)
            scene.cameraNode.position = Position(0f, 0f, 3f)
            scene.cameraNode.lookAt(Position(0f, 0f, 0f))

            sceneView = scene

            host.addView(scene, fullSize())

            loadModel()

            if (cameraView != null) {
                openCamera(cameraView)
            }
        } catch (_: Exception) {
            statusText?.text = "Não foi possível iniciar a cena 3D."
        }
    }

    private fun openCamera(previewView: PreviewView) {
        val context = context ?: return

        val expected = generation
        val owner = viewLifecycleOwner

        // O prazo também cobre a obtenção do CameraProvider.
        startCameraWatchdog(expected)

        try {
            val future = ProcessCameraProvider.getInstance(context)

            future.addListener({
                if (
                    container == null ||
                    expected != generation ||
                    mode != Mode.CAMERA
                ) {
                    return@addListener
                }

                try {
                    val provider = future.get()

                    val selector = when {
                        provider.hasCamera(
                            CameraSelector.DEFAULT_BACK_CAMERA
                        ) -> CameraSelector.DEFAULT_BACK_CAMERA

                        provider.hasCamera(
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        ) -> CameraSelector.DEFAULT_FRONT_CAMERA

                        else -> {
                            startPreview(useCamera = false)
                            return@addListener
                        }
                    }

                    val preview = Preview.Builder().build()

                    preview.setSurfaceProvider(
                        previewView.surfaceProvider
                    )

                    cameraProvider = provider
                    cameraPreview = preview

                    previewView.previewStreamState.observe(owner) { state ->
                        if (
                            expected == generation &&
                            mode == Mode.CAMERA
                        ) {
                            cameraStreaming =
                                state == PreviewView.StreamState.STREAMING
                        }
                    }

                    provider.bindToLifecycle(
                        owner,
                        selector,
                        preview
                    )

                } catch (_: Exception) {
                    if (
                        expected == generation &&
                        container != null
                    ) {
                        startPreview(useCamera = false)
                    }
                }
            }, ContextCompat.getMainExecutor(context))

        } catch (_: Exception) {
            if (
                expected == generation &&
                container != null
            ) {
                startPreview(useCamera = false)
            }
        }
    }

    private fun startCameraWatchdog(expected: Int) {
        cameraWatchdogJob?.cancel()

        cameraWatchdogJob = viewLifecycleOwner.lifecycleScope.launch {
            var withoutCameraMs = 0L
            var lastTime = SystemClock.elapsedRealtime()

            while (isActive) {
                delay(500L)

                if (
                    container == null ||
                    expected != generation ||
                    mode != Mode.CAMERA
                ) {
                    return@launch
                }

                val now = SystemClock.elapsedRealtime()
                val elapsed = now - lastTime
                lastTime = now

                val resumed = viewLifecycleOwner.lifecycle
                    .currentState.isAtLeast(Lifecycle.State.RESUMED)

                if (!resumed || cameraStreaming) {
                    withoutCameraMs = 0L
                } else {
                    withoutCameraMs += elapsed
                }

                if (withoutCameraMs >= CAMERA_TIMEOUT_MS) {
                    startPreview(useCamera = false)
                    return@launch
                }
            }
        }
    }

    private fun loadModel() {
        val scene = sceneView ?: return
        val location = modelLocation(currentModelFile)

        if (location == null) {
            statusText?.text =
                "Modelo não encontrado: $currentModelFile"

            placeButton?.isEnabled = false
            return
        }

        loadJob?.cancel()
        removeModel()

        val expected = generation
        val expectedModel = ++modelGeneration
        val isAr = mode == Mode.AR
        val fileName = currentModelFile

        placeButton?.isEnabled = false

        loadJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                val instance =
                    scene.modelLoader.loadModelInstance(location)

                if (
                    expected != generation ||
                    expectedModel != modelGeneration ||
                    sceneView !== scene
                ) {
                    return@launch
                }

                if (instance == null) {
                    statusText?.text = "Não foi possível abrir o modelo."
                    return@launch
                }

                val node = ModelNode(
                    modelInstance = instance,
                    scaleToUnits = if (isAr) 0.5f else 1.5f,
                    centerOrigin = Position(0f, 0f, 0f)
                )

                node.isEditable =
                    jogoViewModel.placar.value?.fase != Fase.PROCURANDO
                modelNode = node

                // Mantém a cor escolhida se o modelo for recarregado.
                aplicarCor(corAtual)

                if (!isAr) {
                    scene.addChildNode(node)
                    placeButton?.isEnabled = true
                }

            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                if (
                    expected == generation &&
                    expectedModel == modelGeneration
                ) {
                    statusText?.text =
                        "Erro ao carregar: $fileName"
                }
            }
        }
    }

    private fun placeModel() {
        val ar = arView ?: return

        if (anchorNode != null) {
            loadModel()
            return
        }

        val hit = placementHit ?: return
        val model = modelNode ?: return

        try {
            val anchor = AnchorNode(
                engine = ar.engine,
                anchor = hit.createAnchor()
            )

            anchor.addChildNode(model)
            ar.addChildNode(anchor)

            anchorNode = anchor
            placementHit = null

            placeButton?.apply {
                text = "Reposicionar"
                isEnabled = true
            }

            statusText?.text =
                "Personagem escondido! Escolha uma cor para disfarçá-lo " +
                        "e toque em Pronto."

        } catch (_: Exception) {
            statusText?.text =
                "Não foi possível posicionar. Tente outra superfície."
        }
    }

    private fun modelLocation(fileName: String): String? {
        val context = context ?: return null

        try {
            context.assets.open(fileName).use { }
            return fileName
        } catch (_: Exception) {
            // Procura no armazenamento interno.
        }

        val file = File(context.filesDir, fileName)

        return if (file.isFile) {
            Uri.fromFile(file).toString()
        } else {
            null
        }
    }

    private fun removeModel() {
        val scene = sceneView

        modelNode?.let { model ->
            runCatching {
                anchorNode?.removeChildNode(model)
                scene?.removeChildNode(model)
                model.destroy()
            }
        }

        modelNode = null

        anchorNode?.let { anchor ->
            runCatching {
                scene?.removeChildNode(anchor)
                anchor.anchor.detach()
                anchor.destroy()
            }
        }

        anchorNode = null
        placementHit = null

        if (mode == Mode.AR) {
            placeButton?.text = "Posicionar"
        }
    }

    private fun clearScene() {
        generation++
        modelGeneration++

        arWatchdogJob?.cancel()
        arWatchdogJob = null

        cameraWatchdogJob?.cancel()
        cameraWatchdogJob = null

        loadJob?.cancel()
        loadJob = null

        cameraPreview?.let { preview ->
            runCatching {
                cameraProvider?.unbind(preview)
            }
        }

        cameraPreview = null
        cameraProvider = null

        arTracking = false
        cameraStreaming = false

        removeModel()

        sceneView?.let { scene ->
            runCatching {
                scene.lifecycle = null
                scene.destroy()
            }
        }

        sceneView = null
        arView = null

        container?.removeAllViews()
    }

    private fun fullSize() = FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    override fun onDestroyView() {
        availabilityJob?.cancel()
        availabilityJob = null

        clearScene()

        overlay?.let {
            (it.parent as? ViewGroup)?.removeView(it)
        }

        somPlayer?.liberar()
        somPlayer = null

        overlay = null
        paletaView = null
        prontoButton = null
        passarOverlay = null
        passarTexto = null
        statusText = null
        placeButton = null
        container = null

        pendingAr = false
        mode = Mode.CHECKING

        super.onDestroyView()
    }
}