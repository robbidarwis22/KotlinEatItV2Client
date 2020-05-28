package com.example.kotlineatitv2client.ui.view_orders

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kotlineatitv2client.Model.OrderModel

class ViewOrderModel : ViewModel() {
    val mutableLiveDataOrderList:MutableLiveData<List<OrderModel>>
    init {
        mutableLiveDataOrderList = MutableLiveData()
    }
    fun setMutableLiveDataOrderList(orderList: List<OrderModel>)
    {
        mutableLiveDataOrderList.value = orderList
    }
}