package com.example.kotlineatitv2client.Callback

import com.example.kotlineatitv2client.Model.Order

interface ILoadOrderCallbackListener {
    fun onLoadOrderSuccess(orderList: List<Order>)
    fun onLoadOrderFailed(message:String)

}