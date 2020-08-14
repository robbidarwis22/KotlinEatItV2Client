package com.example.kotlineatitv2client.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton
import com.example.kotlineatitv2client.Common.Common
import com.example.kotlineatitv2client.Database.CartDataSource
import com.example.kotlineatitv2client.Database.CartDatabase
import com.example.kotlineatitv2client.Database.CartItem
import com.example.kotlineatitv2client.Database.LocalCartDataSource
import com.example.kotlineatitv2client.EventBus.UpdateItemInCart
import com.example.kotlineatitv2client.Model.AddonModel
import com.example.kotlineatitv2client.Model.FoodModel
import com.example.kotlineatitv2client.Model.SizeModel
import com.example.kotlineatitv2client.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.disposables.CompositeDisposable
import org.greenrobot.eventbus.EventBus

class MyCartAdapter (internal var context: Context,
                     internal var cartItems: List<CartItem>) :
    RecyclerView.Adapter<MyCartAdapter.MyViewHolder>()  {

    internal var compositeDisposable:CompositeDisposable
    internal var cartDataSource:CartDataSource
    val gson: Gson

    init {
        compositeDisposable = CompositeDisposable()
        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(context).cartDAO())
            gson=Gson()
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        lateinit var img_cart:ImageView
        lateinit var txt_food_name:TextView
        lateinit var txt_food_price:TextView
        lateinit var txt_food_size:TextView
        lateinit var txt_food_addon:TextView
        lateinit var number_button:ElegantNumberButton

        init{
            img_cart = itemView.findViewById(R.id.img_cart) as ImageView
            txt_food_name = itemView.findViewById(R.id.txt_food_name) as TextView
            txt_food_price = itemView.findViewById(R.id.txt_food_price) as TextView
            txt_food_size = itemView.findViewById(R.id.txt_food_size) as TextView
            txt_food_addon = itemView.findViewById(R.id.txt_food_addon) as TextView
            number_button = itemView.findViewById(R.id.number_button) as ElegantNumberButton
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_cart_item,parent,false))
    }

    override fun getItemCount(): Int {
        return cartItems.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(cartItems[position].foodImage)
            .into(holder.img_cart)
        holder.txt_food_name.text = StringBuilder(cartItems[position].foodName!!)
        holder.txt_food_price.text = StringBuilder("").append(cartItems[position].foodPrice + cartItems[position].foodExtraPrice)

        if (cartItems[position].foodSize != null)
        {
            if (cartItems[position].foodSize.equals("Default")) holder.txt_food_price.text = StringBuilder("Size: Default")
            else{
                val sizeModel = gson.fromJson<SizeModel>(cartItems[position].foodSize,object : TypeToken<SizeModel>() {}.type)
                holder.txt_food_size.text = StringBuilder("Size: ").append(sizeModel.name)
            }
        }

        if (cartItems[position].foodAddon != null)
        {
            if (cartItems[position].foodAddon.equals("Default")) holder.txt_food_addon.text = StringBuilder("Addon: Default")
            else{
                val addonModels = gson.fromJson<List<AddonModel>>(cartItems[position].foodAddon,object :TypeToken<List<AddonModel>>(){}.type)
                holder.txt_food_addon.text = StringBuilder("Addon: ").append(Common.getListAddon(addonModels))
            }
        }

        holder.number_button.number = cartItems[position].foodQuantity.toString()

        //Event
        holder.number_button.setOnValueChangeListener { view, oldValue, newValue ->
            cartItems[position].foodQuantity = newValue
            EventBus.getDefault().postSticky(UpdateItemInCart(cartItems[position]))
        }
    }

    fun getItemAtPosition(pos: Int): CartItem {
        return cartItems[pos]
    }
}