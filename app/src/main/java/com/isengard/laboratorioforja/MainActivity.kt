package com.isengard.laboratorioforja

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.common.Player
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    //Instancia de Expplayer, es la encargada de conectarse a la url
    private var player: ExoPlayer? = null

    // Instancia de la máquina de estados multimedia
    private var mediaPlayer: MediaPlayer? = null

    //Elemento playerView del xml (Es la pantalla)
    private lateinit var playerView: PlayerView

    //Elemento textview del xml, mostrara los estados del video
    private lateinit var tvEstadoConexion: TextView

    private var url = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        playerView = findViewById(R.id.playerView)

        //Mapa de los canales del spinner que contendra cada elemento un url de video
        val canales = mapOf(
            "Minas Tirith" to "https://raw.githubusercontent.com/mediaelement/mediaelement-files/master/big_buck_bunny.mp4",
            "Isengard" to "https://raw.githubusercontent.com/mediaelement/mediaelement-files/master/echo-hereweare.mp4",
            "Rivendell" to "https://raw.githubusercontent.com/intel-iot-devkit/sample-videos/master/person-bicycle-car-detection.mp4"
        )


        //Creo una lista con las claves del mapa
        val listaCanales = canales.keys.toList()

        //Con este adapter escribo los datos de la lista de claves de canales para que se vean en el spinner
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listaCanales
        )
        tvEstadoConexion = findViewById<TextView>(R.id.tvEstadoConexion)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val spinnerCanales =  findViewById<Spinner>(R.id.spinnerCanales)
        spinnerCanales.adapter = adapter

        //Este evento captura el cambio de elemento del spinner
        spinnerCanales.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected( parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val itemSeleccionado = parent?.getItemAtPosition(position).toString()
                //Obtenemos la url del item y reproducimos el video
                url = canales[itemSeleccionado] ?: ""
                if(url.isNotEmpty()){
                    if (player == null) {
                        initializePlayer()
                    } else{
                        player?.setMediaItem(MediaItem.fromUri(Uri.parse(url)))
                        player?.prepare()
                        player?.playWhenReady = true
                    }
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun initializePlayer(){
        // Instanciamos el motor del reproductor ExoPlayer
        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            playerView.player = exoPlayer

            // Creamos el item multimedia a partir de la URL de Red
            val mediaItem = MediaItem.fromUri(Uri.parse(url))
            exoPlayer.setMediaItem(mediaItem)

            //Este evento captura los estados del playerview, para poder mostrar un texto de cada estado en el textview de estados
            exoPlayer.addListener(object : Player.Listener{
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> tvEstadoConexion.text = "Cargando Búfer..."
                        Player.STATE_READY -> tvEstadoConexion.text = "Transmitiendo en vivo"
                        Player.STATE_ENDED -> {
                            tvEstadoConexion.text = "Conexión terminada"
                            sonarAlarma()
                        }

                    }
                }
            })

            // Preparamos el búfer y configuramos el auto-play
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }



    //Este funcion es para hacer sonar un audio cuando un video acabe
    private fun sonarAlarma(){
        // Liberamos la instancia previa si ya existía para evitar acumulación en memoria
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this, R.raw.campanas_gondor)
        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start() // Estado Started
            Toast.makeText(this, "Sonando: Las campanas estan sonando", Toast.LENGTH_SHORT).show()
        }
    }






    //Funcion para eliminar el playerView
    private fun releasePlayer() {
        // Liberación segura de recursos para evitar fugas de memoria
        player?.let { exoPlayer ->
            exoPlayer.release()
        }
        player = null

        // Liberación segura de recursos del audio
        mediaPlayer?.release()
        mediaPlayer = null
    }

    //Ciclos de vida de la aplicacion
    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        if (player == null) {
            initializePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
        mediaPlayer?.release()
        mediaPlayer = null
    }


}