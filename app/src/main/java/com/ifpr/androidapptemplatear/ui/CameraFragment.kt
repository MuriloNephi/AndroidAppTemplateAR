package com.ifpr.androidapptemplatear.ui

import androidx.fragment.app.Fragment
import com.ifpr.androidapptemplatear.R
import com.ifpr.androidapptemplatear.SoundPlayer
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.ar.core.Config
import com.ifpr.androidapptemplatear.databinding.FragmentCameraBinding
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.math.Position
import okio.IOException
import java.io.File

class CameraFragment : Fragment() {

    private lateinit var sceneView: ArSceneView
    private lateinit var modelNode: ArModelNode
    private lateinit var placeButton: ExtendedFloatingActionButton

    private lateinit var sharedViewModel: SharedModelViewModel

    private val CAMERA_PERMISSION_CODE = 1001

    private var currentModelFile: String = "neutrofilo.glb"

    // ---- FASE 2: pontuação e som ----
    private var pontos = 0
    private var modeloColocado = false
    private lateinit var somPlayer: SoundPlayer
    // ---------------------------------

    private var _binding: FragmentCameraBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)

        somPlayer = SoundPlayer(requireContext())

        sharedViewModel = ViewModelProvider(requireActivity())[SharedModelViewModel::class.java]
        sharedViewModel.selectedModel.observe(viewLifecycleOwner) { path ->
            loadModel(path)
        }

        val view = inflater.inflate(R.layout.fragment_camera, container, false)

        sceneView = view.findViewById<ArSceneView?>(R.id.sceneView).apply {
            lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
        }

        // ---- FASE 2: detectar toque na tela ----
        val gestureDetector = GestureDetector(
            requireContext(),
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    registrarToque()
                    return false
                }
            }
        )
        sceneView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false // false = não bloqueia os gestos normais do SceneView
        }
        // ----------------------------------------

        placeButton = view.findViewById(R.id.place)

        placeButton.setOnClickListener {
            placeModel()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        somPlayer.liberar()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkCameraPermission()
    }

    private fun placeModel() {
        modelNode.anchor()
        modelNode.isEditable = true
        modelNode.isScaleEditable = false

        modeloColocado = true

        sceneView.planeRenderer.isVisible = false
    }

    // ---- FASE 2: "Toque para encontrar" (+10 pontos) ----
    private fun registrarToque() {
        if (!modeloColocado) return

        pontos += 10
        somPlayer.tocarParaModelo(currentModelFile)
        Toast.makeText(
            requireContext(),
            "Encontrou! Pontos: $pontos",
            Toast.LENGTH_SHORT
        ).show()
    }
    // -----------------------------------------------------

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else {
            initAR()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            initAR()
        } else {
            Toast.makeText(requireContext(), "Permissão de câmera negada", Toast.LENGTH_LONG).show()
        }
    }

    private fun initAR() {
        loadModel(currentModelFile)
    }

    private fun loadModel(fileName: String) {
        try {
            sceneView.removeChild(modelNode)
        } catch (e: Exception) {

        }

        // FASE 2: guarda o modelo atual e reinicia o estado
        currentModelFile = fileName
        modeloColocado = false

        modelNode = ArModelNode(sceneView.engine, PlacementMode.INSTANT).apply {
            loadModelGlbAsync(
                glbFileLocation = getFilePath(requireContext(),fileName),
                scaleToUnits = 0.4f,
                centerOrigin = Position(0f, -1f, 0f),
            ) {
                sceneView.planeRenderer.isVisible = true
            }
            // Permitir interação com o modelo
            isEditable = true
            isScaleEditable = false

        }

        sceneView.addChild(modelNode)
    }

    private fun getFilePath(context: Context?, fileName: String): String {
        try {
            context?.assets?.open(fileName)?.close() // Tenta abrir o arquivo dos assets
            return fileName // Se abrir sem erro, presume-se que está nos assets
        } catch (e: IOException) {
            // Arquivo não encontrado nos assets, agora vamos procurar no filesDir
            val internalFile = File(context?.filesDir, fileName)
            if (internalFile.exists()) {
                return  Uri.fromFile(internalFile).toString()
            } else {
                return "Arquivo não encontrado em assets nem em filesDir: $fileName"
            }
        }
    }
}