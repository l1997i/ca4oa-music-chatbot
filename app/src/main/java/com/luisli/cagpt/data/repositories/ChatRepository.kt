package com.luisli.cagpt.data.repositories
import com.luisli.cagpt.data.models.ChatPostBody
import com.luisli.cagpt.data.models.ChatResponseBody
import com.luisli.cagpt.data.network.ApiInterface
import com.luisli.cagpt.data.network.SafeApiRequest
import javax.inject.Inject
class ChatRepository @Inject constructor(
    private val api: ApiInterface
) : SafeApiRequest() {
    suspend fun sendMessage(
        chatPostBody: ChatPostBody
    ): ChatResponseBody = apiRequest {
        api.sendMessage(chatPostBody)
    }
}