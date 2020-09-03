package com.example.kotlineatitv2client.Callback

import com.example.kotlineatitv2client.Model.OrderModel

interface ILoadTimeFromFirebaseCallback {
    fun onLoadTimeSuccess(order: OrderModel, estimatedTimeMs:Long)
    fun onLoadOnlyTimeSuccess(estimatedTimeMs:Long)
    fun onLoadTimeFailed(message:String)
}