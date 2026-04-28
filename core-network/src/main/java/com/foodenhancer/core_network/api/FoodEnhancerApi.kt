package com.foodenhancer.core_network.api

import com.foodenhancer.core_network.dto.EnhanceResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface FoodEnhancerApi {

    @Multipart
    @POST("api/v1/enhance")
    suspend fun enhance(
        @Part image: MultipartBody.Part,
        @Part mask: MultipartBody.Part,
        @Part("style") style: RequestBody
    ): Response<EnhanceResponse>
}
