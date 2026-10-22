package com.contratv.player.ui.channels

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.contratv.player.R
import com.contratv.player.api.models.Channel
import com.contratv.player.databinding.ItemChannelBinding

class ChannelAdapter(
    private var channels: List<Channel>,
    private val onChannelClick: (Channel, Int) -> Unit
) : RecyclerView.Adapter<ChannelAdapter.ViewHolder>() {

    fun updateData(newChannels: List<Channel>) {
        channels = newChannels
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChannelBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(channels[position], position)
    }

    override fun getItemCount(): Int = channels.size

    inner class ViewHolder(private val binding: ItemChannelBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(channel: Channel, position: Int) {
            binding.tvChannelNum.text = channel.num?.toString() ?: "${position + 1}"
            binding.tvChannelName.text = channel.name

            val iconUrl = channel.streamIcon
            if (!iconUrl.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(iconUrl)
                    .placeholder(R.drawable.logo_contratv)
                    .error(R.drawable.logo_contratv)
                    .into(binding.ivChannelLogo)
            } else {
                binding.ivChannelLogo.setImageResource(R.drawable.logo_contratv)
            }

            binding.root.setOnClickListener {
                onChannelClick(channel, position)
            }
        }
    }
}
