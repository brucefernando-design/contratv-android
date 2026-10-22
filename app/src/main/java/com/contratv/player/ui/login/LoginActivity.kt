package com.contratv.player.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.contratv.player.R
import com.contratv.player.api.ApiClient
import com.contratv.player.data.SessionManager
import com.contratv.player.databinding.ActivityLoginBinding
import com.contratv.player.ui.dashboard.DashboardActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        if (session.isLoggedIn && session.username.isNotEmpty() && session.password.isNotEmpty()) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        }

        binding.etServerUrl.setText(session.serverUrl)
        binding.etUsername.setText(session.username)

        binding.btnLogin.setOnClickListener {
            performLogin()
        }

        // Default focus for Android TV remotes
        binding.etUsername.requestFocus()
    }

    private fun performLogin() {
        val server = binding.etServerUrl.text.toString().trim()
        val user = binding.etUsername.text.toString().trim()
        val pass = binding.etPassword.text.toString().trim()

        if (server.isEmpty() || user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, R.string.login_error_empty, Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val service = ApiClient.getService(server)
                val response = service.login(user, pass)

                withContext(Dispatchers.Main) {
                    setLoading(false)

                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        val userInfo = body.userInfo

                        if (userInfo != null && userInfo.isAuthorized()) {
                            val status = userInfo.status ?: "Active"
                            val expDate = userInfo.expDate ?: ""
                            session.saveLogin(server, user, pass, status, expDate)

                            Toast.makeText(
                                this@LoginActivity,
                                "¡Bienvenido a ContraTV, ${userInfo.username ?: user}!",
                                Toast.LENGTH_SHORT
                            ).show()

                            startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                R.string.login_error_auth,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "Error del servidor (${response.code()})",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    setLoading(false)
                    Toast.makeText(
                        this@LoginActivity,
                        "${getString(R.string.login_error_network)}: ${e.localizedMessage ?: e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !loading
        binding.etServerUrl.isEnabled = !loading
        binding.etUsername.isEnabled = !loading
        binding.etPassword.isEnabled = !loading
    }
}
