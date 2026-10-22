package com.contratv.player.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
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
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var session: SessionManager
    private var isPasswordVisible = false

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

        binding.etServerUrl.setText(session.serverUrl.ifEmpty { getString(R.string.default_server_url) })
        binding.etUsername.setText(session.username.ifEmpty { "casa" })
        binding.etPassword.setText(session.password.ifEmpty { "123456" })

        binding.btnLogin.setOnClickListener {
            performLogin()
        }

        binding.btnTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        binding.etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performLogin()
                true
            } else {
                false
            }
        }

        // Default focus for Android TV remotes
        binding.btnLogin.requestFocus()
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        if (isPasswordVisible) {
            binding.etPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            binding.etPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        binding.etPassword.setSelection(binding.etPassword.text.length)
    }

    private fun performLogin() {
        val server = binding.etServerUrl.text.toString().trim().trimEnd('/')
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
                            val msg = userInfo?.message ?: getString(R.string.login_error_auth)
                            Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_LONG).show()
                        }
                    } else {
                        // Extract error message from response body if possible
                        var errMsg = "Error del servidor (${response.code()})"
                        try {
                            val errBody = response.errorBody()?.string()
                            if (!errBody.isNullOrEmpty()) {
                                val json = JSONObject(errBody)
                                if (json.has("user_info")) {
                                    val uInfo = json.getJSONObject("user_info")
                                    if (uInfo.has("message")) {
                                        errMsg = uInfo.getString("message")
                                    }
                                }
                            }
                        } catch (_: Exception) {}

                        Toast.makeText(this@LoginActivity, errMsg, Toast.LENGTH_LONG).show()
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
