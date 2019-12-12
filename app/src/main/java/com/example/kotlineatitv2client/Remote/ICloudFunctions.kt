package com.example.kotlineatitv2client.Remote

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface ICloudFunctions {
    @GET("")
    fun getCustomToken(@Query("access_token") accessToken: String): Observable<ResponseBody>
}