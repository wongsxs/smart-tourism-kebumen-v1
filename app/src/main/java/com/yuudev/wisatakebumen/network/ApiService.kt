package com.yuudev.wisatakebumen.network

import com.yuudev.wisatakebumen.model.Wisata
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import okhttp3.ResponseBody
import com.yuudev.wisatakebumen.model.Tiket
import com.yuudev.wisatakebumen.model.User
import com.yuudev.wisatakebumen.model.Statistik
import com.yuudev.wisatakebumen.model.ReviewResponse
import com.yuudev.wisatakebumen.model.ApiResponse
import com.yuudev.wisatakebumen.model.RiwayatValidasi
import com.yuudev.wisatakebumen.model.StatistikValidasi


interface ApiService {

    @GET("exec")
    suspend fun getWisata(
        @Query("action") action: String = "get"
    ): List<Wisata>

    @POST("exec")
    suspend fun postData(
        @Body body: RequestBody
    ): Response<Any>

    @POST("exec")
    suspend fun uploadImage(
        @Body body: RequestBody
    ): Response<ResponseBody>

    @GET("exec")
    suspend fun getPemesanan(
        @Query("action") action: String = "getPemesanan",
        @Query("username") username: String
    ): List<Tiket>

    @POST("exec")
    suspend fun login(
        @Body body: RequestBody
    ): User

    @GET("exec")
    suspend fun getStatistik(
        @Query("action") action: String = "getStatistik"
    ): Statistik

    @GET("exec")
    suspend fun getSemuaTiket(
        @Query("action")
        action: String = "getSemuaTiket"
    ): List<Tiket>

    @GET("exec")
    suspend fun getReview(
        @Query("action") action: String = "getReview",
        @Query("wisataId") wisataId: String
    ): ReviewResponse

    @POST("exec")
    suspend fun tambahReview(
        @Body body: RequestBody
    ): ApiResponse

    @GET("exec")
    suspend fun getDetailTiket(
        @Query("action") action: String = "getDetailTiket",
        @Query("bookingId") bookingId: String
    ): Tiket

    @POST("exec")
    suspend fun validasiTiket(
        @Body body: RequestBody
    ): ApiResponse

    @GET("exec")
    suspend fun getRiwayatValidasi(
        @Query("action") action: String = "getRiwayatValidasi"
    ): List<RiwayatValidasi>

    @GET("exec")
    suspend fun getStatistikValidasi(
        @Query("action") action: String = "getStatistikValidasi"
    ): StatistikValidasi

    @POST("exec")
    suspend fun getTripPlan(
        @Body body: com.yuudev.wisatakebumen.model.TripRequest
    ): com.yuudev.wisatakebumen.model.TripPlanResponse

}