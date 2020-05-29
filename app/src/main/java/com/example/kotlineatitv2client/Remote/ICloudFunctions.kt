package com.example.kotlineatitv2client.Remote

import com.example.kotlineatitv2client.Model.BraintreeToken
import com.example.kotlineatitv2client.Model.BraintreeTransaction
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.*
import java.time.temporal.TemporalAmount

interface ICloudFunctions {
//    @GET("token")
//    fun getToken(@HeaderMap headers:Map<String,String>): Observable<BraintreeToken>
    @GET("getCustomToken")
    fun getCustomToken(@Query("access_token") accessToken: String):Observable<ResponseBody>
//    @POST("checkout")
//    @FormUrlEncoded
//    fun submitPayment(
//    @HeaderMap header:Map<String>,
//    @Field("amount") amount: Double,
//    @Field("payment_method_nonce") nonce:String): Observable<BraintreeTransaction>
}