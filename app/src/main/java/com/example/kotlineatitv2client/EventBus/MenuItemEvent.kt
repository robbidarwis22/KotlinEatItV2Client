package com.example.kotlineatitv2client.EventBus

import com.example.kotlineatitv2client.Model.CategoryModel
import com.example.kotlineatitv2client.Model.RestaurantModel

class MenuItemEvent (var isSuccess:Boolean, var restaurantModel: RestaurantModel)
