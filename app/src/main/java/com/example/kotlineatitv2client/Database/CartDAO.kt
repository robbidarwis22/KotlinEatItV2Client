package com.example.kotlineatitv2client.Database

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface CartDAO {
    @Query("SELECT * FROM Cart WHERE uid=:uid AND restaurantId=:restaurantId")
    fun getAllCart(uid:String,restaurantId:String):Flowable<List<CartItem>>

    @Query("SELECT SUM(foodQuantity) FROM Cart WHERE uid=:uid AND restaurantId=:restaurantId")
    fun countItemInCart(uid:String,restaurantId: String):Single<Int>

    @Query("SELECT SUM((foodPrice + foodExtraPrice)*foodQuantity) FROM Cart WHERE uid=:uid AND restaurantId=:restaurantId")
    fun sumPrice(uid: String,restaurantId: String):Single<Double>

    @Query("SELECT * FROM Cart WHERE foodId=:foodId AND uid=:uid AND restaurantId=:restaurantId")
    fun getItemInCart(foodId:String,uid:String,restaurantId: String):Single<CartItem>

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplaceAll(vararg cartItem: CartItem) :Completable

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateCart(cart:CartItem):Single<Int>

    @Delete
    fun deleteCart(cart:CartItem):Single<Int>

    @Query("DELETE FROM Cart WHERE uid=:uid AND restaurantId=:restaurantId")
    fun cleanCart(uid:String,restaurantId: String):Single<Int>

    @Query("SELECT * FROM Cart WHERE categoryId=:categoryId AND foodId=:foodId AND uid=:uid AND foodSize=:foodSize AND foodAddon=:foodAddon AND restaurantId=:restaurantId")
    fun getItemWithAllOptionsInCart(uid:String,categoryId:String,foodId:String,foodSize:String,foodAddon:String,restaurantId: String): Single<CartItem>
}