package com.contratv.player.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.contratv.player.R
import com.contratv.player.data.SessionManager
import com.contratv.player.databinding.ActivityDashboardBinding
import com.contratv.player.ui.channels.ChannelsActivity
import com.contratv.player.ui.login.LoginActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        setupHeader()
        setupCards()

        // Focus Live TV card first for TV remote navigation
        binding.cardLiveTv.requestFocus()
    }

    private fun setupHeader() {
        binding.tvUserInfo.text = getString(R.string.logged_as, session.username)

        val expFormatted = formatExpDate(session.expDate)
        if (expFormatted.isNotEmpty()) {
            binding.tvStatus.text = "Estado: ACTIVO • $expFormatted"
        } else {
            binding.tvStatus.text = "Estado: ACTIVO"
        }

        binding.btnLogout.setOnClickListener {
            confirmLogout()
        }
    }

    private fun setupCards() {
        binding.cardLiveTv.setOnClickListener {
            startActivity(Intent(this, ChannelsActivity::class.java))
        }

        binding.cardMovies.setOnClickListener {
            Toast.makeText(this, "Películas próximamente disponibles en ContraTV", Toast.LENGTH_SHORT).show()
        }

        binding.cardSeries.setOnClickListener {
            Toast.makeText(this, "Series próximamente disponibles en ContraTV", Toast.LENGTH_SHORT).show()
        }

        binding.cardFavorites.setOnClickListener {
            val intent = Intent(this, ChannelsActivity::class.java).apply {
                putExtra("SHOW_FAVORITES", true)
            }
            startActivity(intent)
        }

        binding.cardSettings.setOnClickListener {
            confirmLogout()
        }
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle(R.string.logout)
            .setMessage(R.string.logout_confirm)
            .setPositiveButton(R.string.yes) { _, _ ->
                session.logout()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun formatExpDate(timestampStr: String): String {
        return try {
            val ts = timestampStr.toLongOrNull() ?: return ""
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            "Vence: " + sdf.format(Date(ts * 1000L))
        } catch (_: Exception) {
            ""
        }
    }
}
