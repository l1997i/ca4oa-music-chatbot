package com.luisli.cagpt.ui.activities
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
interface MusicInterface {
    @Headers("X-RapidAPI-Key: XXXX",
        "X-RapidAPI-Host: deezerdevs-deezer.p.rapidapi.com")
    @GET("search")
    fun getMusic(@Query("q") query: String) : Call<MusicData>
}