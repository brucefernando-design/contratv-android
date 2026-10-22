package com.contratv.player.api.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AuthResponse(
    @SerializedName("user_info") val userInfo: UserInfo?,
    @SerializedName("server_info") val serverInfo: ServerInfo?
) : Serializable

data class UserInfo(
    @SerializedName("username") val username: String?,
    @SerializedName("password") val password: String?,
    @SerializedName("message") val message: String? = null,
    @SerializedName("auth") val auth: Any?, // Can be Int (1), Double (1.0), Boolean, or String ("1")
    @SerializedName("status") val status: String?,
    @SerializedName("exp_date") val expDate: String?,
    @SerializedName("is_trial") val isTrial: String?,
    @SerializedName("active_cons") val activeCons: Any?,
    @SerializedName("max_connections") val maxConnections: String?
) : Serializable {
    fun isAuthorized(): Boolean {
        val isStatusActive = status?.equals("Active", ignoreCase = true) == true
        if (auth == null) return isStatusActive

        val isAuthValid = when (auth) {
            is Number -> auth.toInt() == 1
            is Boolean -> auth
            is String -> auth == "1" || auth == "1.0" || auth.equals("true", ignoreCase = true)
            else -> auth.toString().startsWith("1") || auth.toString().equals("true", ignoreCase = true)
        }

        return isAuthValid || isStatusActive
    }
}

data class ServerInfo(
    @SerializedName("url") val url: String?,
    @SerializedName("port") val port: String?,
    @SerializedName("server_protocol") val serverProtocol: String?
) : Serializable

data class Category(
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("category_name") val categoryName: String,
    @SerializedName("parent_id") val parentId: Int = 0
) : Serializable

data class Channel(
    @SerializedName("num") val num: Int?,
    @SerializedName("name") val name: String,
    @SerializedName("stream_type") val streamType: String?,
    @SerializedName("stream_id") val streamId: Int,
    @SerializedName("stream_icon") val streamIcon: String?,
    @SerializedName("epg_channel_id") val epgChannelId: String?,
    @SerializedName("category_id") val categoryId: String?,
    @SerializedName("direct_source") val directSource: String?
) : Serializable
