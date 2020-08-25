package com.example.kotlineatitv2client.ui.cart

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kotlineatitv2client.Common.Common
import com.example.kotlineatitv2client.Database.CartDataSource
import com.example.kotlineatitv2client.Database.CartDatabase
import com.example.kotlineatitv2client.Database.CartItem
import com.example.kotlineatitv2client.Database.LocalCartDataSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class CartViewModel : ViewModel() {

    private val compositeDisposable:CompositeDisposable
    private var cartDataSource:CartDataSource?=null
    private var mutableLiveDataCartItem:MutableLiveData<List<CartItem>>?=null

    init {
        compositeDisposable = CompositeDisposable()
    }

    fun initCartdataSource(context: Context){
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(context).cartDAO())
    }

    fun getMutableLiveDataCartItem():MutableLiveData<List<CartItem>>{
        if(mutableLiveDataCartItem == null)
            mutableLiveDataCartItem = MutableLiveData()
        getCartItems()
        return mutableLiveDataCartItem!!
    }

    private fun getCartItems(){
        compositeDisposable.addAll(cartDataSource!!.getAllCart(Common.currentUser!!.uid!!,Common.currentRestaurant!!.uid)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({cartItems ->

                mutableLiveDataCartItem!!.value = cartItems
            },{t: Throwable? ->  mutableLiveDataCartItem!!.value = null }))
    }

    fun onStop(){
        compositeDisposable.clear()
    }
}