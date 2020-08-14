package com.example.kotlineatitv2client.Callback

import com.example.kotlineatitv2client.Database.CartItem
import com.example.kotlineatitv2client.Model.CategoryModel

interface ISearchCategoryCallbackListener {
    fun onSearchFound(category:CategoryModel,cartItem: CartItem)
    fun onSearchNotFound(message:String)
}