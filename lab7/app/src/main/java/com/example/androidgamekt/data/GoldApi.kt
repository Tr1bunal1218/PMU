package com.example.androidgamekt.data

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface GoldApi {
    @GET("/scripts/xml_metall.asp")
    suspend fun getMetalsXml(
        @Query("date_req1") from: String,
        @Query("date_req2") to: String
    ): String

    companion object {
        fun create(): GoldApi {
            val client = OkHttpClient.Builder().build()
            val retrofit = Retrofit.Builder()
                .baseUrl("https://www.cbr.ru")
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(client)
                .build()
            return retrofit.create(GoldApi::class.java)
        }
    }
}
