package com.example.kotlineatitv2client.Callback

import com.example.kotlineatitv2client.Model.Order

interface ILoadTimeFromFirebaseCallback {
    fun onLoadTimeSuccess(order: Order,estimatedTimeMs:Long)
    fun onLoadTimeFailed(message:String)
}