package com.contratv.player.api

import com.contratv.player.api.models.AuthResponse
import com.contratv.player.api.models.Category
import com.contratv.player.api.models.Channel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface XtreamService {

    @POST("player_api.php")
    suspend fun login(
        @Query("username") user: String,
        @Query("password") pass: String
    ): Response<AuthResponse>

    @GET("player_api.php")
    suspend fun getLiveCategories(
        @Query("username") user: String,
        @Query("password") pass: String,
        @Query("action") action: String = "get_live_categories"
    ): Response<List<Category>>

    @GET("player_api.php")
    suspend fun getLiveStreams(
        @Query("username") user: String,
        @Query("password") pass: String,
        @Query("action") action: String = "get_live_streams",
        @Query("category_id") categoryId: String? = null
    ): Response<List<Channel>>
}
