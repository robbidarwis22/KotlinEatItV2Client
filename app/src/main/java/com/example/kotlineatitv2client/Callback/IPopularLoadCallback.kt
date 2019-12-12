package com.example.kotlineatitv2client.Callback

import com.example.kotlineatitv2client.Model.PopularCategoryModel

interface IPopularLoadCallback {
    fun onPopularLoadSuccess(popularModelList:List<PopularCategoryModel>)
    fun onPopularLoadFailed(message:String)
}