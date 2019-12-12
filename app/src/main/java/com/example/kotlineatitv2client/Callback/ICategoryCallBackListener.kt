package com.example.kotlineatitv2client.Callback

import com.example.kotlineatitv2client.Model.CategoryModel

interface ICategoryCallBackListener {
    fun onCategoryLoadSuccess(categoriesList:List<CategoryModel>)
    fun onCategoryLoadFailed(message:String)
}