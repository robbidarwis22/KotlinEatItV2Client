package com.example.kotlineatitv2client.Callback

import com.example.kotlineatitv2client.Model.RestaurantModel

interface IRestaurantCallbackListener {
    fun onRestaurantLoadSuccess(restaurantList: List<RestaurantModel>)
    fun onRestaurantLoadFailed(message:String)
}