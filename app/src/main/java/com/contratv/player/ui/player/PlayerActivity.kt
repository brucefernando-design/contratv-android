package com.contratv.player.ui.player

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import com.contratv.player.api.models.Channel
import com.contratv.player.data.SessionManager
import com.contratv.player.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private lateinit var session: SessionManager
    private var exoPlayer: ExoPlayer? = null

    private var currentIndex: Int = 0
    private val handler = Handler(Looper.getMainLooper())
    private val hideOsdRunnable = Runnable { hideOsd() }
    private var isOsdVisible = true

    private var currentAspectRatioIndex = 0
    private val aspectRatios = listOf(
        Pair("FIT", AspectRatioFrameLayout.RESIZE_MODE_FIT),
        Pair("FILL", AspectRatioFrameLayout.RESIZE_MODE_FILL),
        Pair("ZOOM", AspectRatioFrameLayout.RESIZE_MODE_ZOOM)
    )

    companion object {
        const val EXTRA_CHANNEL_INDEX = "EXTRA_CHANNEL_INDEX"
        var channelList: List<Channel> = emptyList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        currentIndex = intent.getIntExtra(EXTRA_CHANNEL_INDEX, 0)

        setupListeners()
        initPlayer()
        playCurrentChannel()
    }

    private fun setupListeners() {
        binding.btnPlayerBack.setOnClickListener { finish() }

        binding.root.setOnClickListener {
            toggleOsd()
        }

        binding.btnRetry.setOnClickListener {
            playCurrentChannel()
        }

        binding.tvAspectRatio.setOnClickListener {
            cycleAspectRatio()
        }
    }

    private fun initPlayer() {
        exoPlayer = ExoPlayer.Builder(this)
            .build()
            .apply {
                playWhenReady = true
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        when (state) {
                            Player.STATE_BUFFERING -> {
                                binding.pbBuffering.visibility = View.VISIBLE
                                binding.layoutError.visibility = View.GONE
                            }
                            Player.STATE_READY -> {
                                binding.pbBuffering.visibility = View.GONE
                                binding.layoutError.visibility = View.GONE
                            }
                            Player.STATE_ENDED -> {
                                binding.pbBuffering.visibility = View.GONE
                            }
                            Player.STATE_IDLE -> {}
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        binding.pbBuffering.visibility = View.GONE
                        binding.layoutError.visibility = View.VISIBLE
                        binding.tvErrorMessage.text = "Error al conectar con la señal del canal: ${error.errorCodeName}"
                    }
                })
            }

        binding.playerView.player = exoPlayer
    }

    private fun playCurrentChannel() {
        if (channelList.isEmpty() || currentIndex < 0 || currentIndex >= channelList.size) {
            Toast.makeText(this, "Canal no disponible", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val channel = channelList[currentIndex]
        binding.tvPlayerChannelName.text = channel.name
        binding.tvPlayerChannelCategory.text = "Canal #${channel.num ?: (currentIndex + 1)}"
        binding.layoutError.visibility = View.GONE
        binding.pbBuffering.visibility = View.VISIBLE

        val streamUrl = if (!channel.directSource.isNullOrEmpty()) {
            channel.directSource
        } else {
            val server = session.serverUrl.trimEnd('/')
            "$server/live/${session.username}/${session.password}/${channel.streamId}.m3u8"
        }

        val mediaItem = MediaItem.fromUri(Uri.parse(streamUrl))
        exoPlayer?.setMediaItem(mediaItem)
        exoPlayer?.prepare()
        exoPlayer?.play()

        showOsd()
    }

    private fun cycleAspectRatio() {
        currentAspectRatioIndex = (currentAspectRatioIndex + 1) % aspectRatios.size
        val (name, mode) = aspectRatios[currentAspectRatioIndex]
        binding.playerView.resizeMode = mode
        binding.tvAspectRatio.text = "AJUSTE: $name"
    }

    private fun toggleOsd() {
        if (isOsdVisible) {
            hideOsd()
        } else {
            showOsd()
        }
    }

    private fun showOsd() {
        binding.osdOverlay.visibility = View.VISIBLE
        isOsdVisible = true
        handler.removeCallbacks(hideOsdRunnable)
        handler.postDelayed(hideOsdRunnable, 5000)
    }

    private fun hideOsd() {
        binding.osdOverlay.visibility = View.GONE
        isOsdVisible = false
        handler.removeCallbacks(hideOsdRunnable)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_CHANNEL_UP -> {
                if (channelList.isNotEmpty()) {
                    currentIndex = if (currentIndex + 1 < channelList.size) currentIndex + 1 else 0
                    playCurrentChannel()
                }
                return true
            }
            KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_CHANNEL_DOWN -> {
                if (channelList.isNotEmpty()) {
                    currentIndex = if (currentIndex - 1 >= 0) currentIndex - 1 else channelList.size - 1
                    playCurrentChannel()
                }
                return true
            }
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                toggleOsd()
                return true
            }
            KeyEvent.KEYCODE_BACK -> {
                if (isOsdVisible) {
                    hideOsd()
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onStop() {
        super.onStop()
        exoPlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(hideOsdRunnable)
        exoPlayer?.release()
        exoPlayer = null
    }
}
