package com.contratv.player.ui.channels

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.contratv.player.api.ApiClient
import com.contratv.player.api.models.Category
import com.contratv.player.api.models.Channel
import com.contratv.player.data.SessionManager
import com.contratv.player.databinding.ActivityChannelsBinding
import com.contratv.player.ui.player.PlayerActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChannelsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChannelsBinding
    private lateinit var session: SessionManager

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var channelAdapter: ChannelAdapter

    private var allChannels: List<Channel> = emptyList()
    private var displayedChannels: List<Channel> = emptyList()
    private var currentCategoryId: String = "ALL"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChannelsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        setupUI()
        loadData()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        // Category list
        categoryAdapter = CategoryAdapter(emptyList()) { category ->
            currentCategoryId = category.categoryId
            binding.tvCurrentCategory.text = category.categoryName
            filterChannels()
        }
        binding.rvCategories.layoutManager = LinearLayoutManager(this)
        binding.rvCategories.adapter = categoryAdapter

        // Channel grid (4 columns for TV widescreen)
        channelAdapter = ChannelAdapter(emptyList()) { channel, _ ->
            // Pass the currently filtered list and selected channel to player
            val channelIndex = displayedChannels.indexOf(channel)
            PlayerActivity.channelList = displayedChannels
            val intent = Intent(this, PlayerActivity::class.java).apply {
                putExtra(PlayerActivity.EXTRA_CHANNEL_INDEX, channelIndex)
            }
            startActivity(intent)
        }
        binding.rvChannels.layoutManager = GridLayoutManager(this, 4)
        binding.rvChannels.adapter = channelAdapter

        // Search filter
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterChannels()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadData() {
        binding.pbLoadingChannels.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val service = ApiClient.getService(session.serverUrl)
                val catResponse = service.getLiveCategories(session.username, session.password)
                val chanResponse = service.getLiveStreams(session.username, session.password)

                withContext(Dispatchers.Main) {
                    binding.pbLoadingChannels.visibility = View.GONE

                    if (catResponse.isSuccessful && catResponse.body() != null) {
                        val rawCats = catResponse.body()!!
                        val fullCats = mutableListOf(
                            Category(categoryId = "ALL", categoryName = "Todos los Canales")
                        )
                        fullCats.addAll(rawCats)
                        categoryAdapter.updateData(fullCats)
                    }

                    if (chanResponse.isSuccessful && chanResponse.body() != null) {
                        allChannels = chanResponse.body()!!
                        filterChannels()
                    } else {
                        Toast.makeText(
                            this@ChannelsActivity,
                            "No se pudieron cargar los canales",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.pbLoadingChannels.visibility = View.GONE
                    Toast.makeText(
                        this@ChannelsActivity,
                        "Error de red: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun filterChannels() {
        val query = binding.etSearch.text.toString().trim().lowercase()

        displayedChannels = allChannels.filter { channel ->
            val matchesCategory = (currentCategoryId == "ALL" || channel.categoryId == currentCategoryId)
            val matchesQuery = query.isEmpty() || channel.name.lowercase().contains(query)
            matchesCategory && matchesQuery
        }

        channelAdapter.updateData(displayedChannels)
        binding.tvEmptyChannels.visibility = if (displayedChannels.isEmpty()) View.VISIBLE else View.GONE
    }
}
