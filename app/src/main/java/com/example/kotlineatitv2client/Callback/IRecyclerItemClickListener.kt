package com.example.kotlineatitv2client.Callback

import android.view.View

interface IRecyclerItemClickListener {
    fun onItemClick(view:View,pos:Int)
}