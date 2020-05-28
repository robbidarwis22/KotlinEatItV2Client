package com.example.kotlineatitv2client.Callback

import com.example.kotlineatitv2client.Model.OrderModel

interface ILoadOrderCallbackListener {
    fun onLoadOrderSuccess(orderList: List<OrderModel>)
    fun onLoadOrderFailed(message:String)

}