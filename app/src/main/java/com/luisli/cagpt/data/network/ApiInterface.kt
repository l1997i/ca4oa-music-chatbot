package com.luisli.cagpt.data.network
import com.luisli.cagpt.data.models.ChatPostBody
import com.luisli.cagpt.data.models.ChatResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
interface ApiInterface {
        @POST("v1/chat/completions")
    suspend fun sendMessage(
        @Body chatPostBody: ChatPostBody
    ): Response<ChatResponseBody>
    }