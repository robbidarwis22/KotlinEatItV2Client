package com.example.kotlineatitv2client.Database

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface CartDataSource {

        fun getAllCart(uid:String,restaurantId:String): Flowable<List<CartItem>>

        fun countItemInCart(uid:String,restaurantId:String): Single<Int>

        fun sumPrice(uid: String,restaurantId:String): Single<Double>

        fun getItemInCart(foodId:String,uid:String,restaurantId:String): Single<CartItem>

        fun insertOrReplaceAll(vararg cartItem: CartItem) : Completable

        fun updateCart(cart:CartItem): Single<Int>

        fun deleteCart(cart:CartItem): Single<Int>

        fun cleanCart(uid:String,restaurantId:String): Single<Int>

        fun getItemWithAllOptionsInCart(uid:String,categoryId:String,foodId:String,foodSize:String,foodAddon:String,restaurantId:String): Single<CartItem>
}