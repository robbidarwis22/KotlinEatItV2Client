package com.example.kotlineatitv2client.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kotlineatitv2client.Callback.IRecyclerItemClickListener
import com.example.kotlineatitv2client.Model.CategoryModel
import com.example.kotlineatitv2client.Model.RestaurantModel
import com.example.kotlineatitv2client.R

class MyRestaurantAdapter (internal var context: Context,
                           internal var restaurantList: List<RestaurantModel>) :
    RecyclerView.Adapter<MyRestaurantAdapter.MyViewHolder>()  {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        override fun onClick(view: View?) {
            listener!!.onItemClick(view!!,adapterPosition)
        }

        var txt_restaurant_name: TextView?=null
        var txt_restaurant_address: TextView?=null
        var img_restaurant: ImageView?=null

        internal var listener: IRecyclerItemClickListener?=null

        fun setListener(listener: IRecyclerItemClickListener)
        {
            this.listener = listener
        }

        init{
            txt_restaurant_name = itemView.findViewById(R.id.txt_restaurant_name) as TextView
            txt_restaurant_address = itemView.findViewById(R.id.txt_restaurant_address) as TextView
            img_restaurant = itemView.findViewById(R.id.img_restaurant) as ImageView
            itemView.setOnClickListener(this)
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyRestaurantAdapter.MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_restaurant,parent,false))
    }

    override fun onBindViewHolder(holder: MyRestaurantAdapter.MyViewHolder, position: Int) {
        Glide.with(context).load(restaurantList[position].imageUrl)
            .into(holder.img_restaurant!!)
        holder.txt_restaurant_name!!.setText(restaurantList[position].name)
        holder.txt_restaurant_address!!.setText(restaurantList[position].address)

        holder.setListener(object : IRecyclerItemClickListener{
            override fun onItemClick(view: View, pos: Int) {
                //Code late
            }

        })
    }

    override fun getItemCount(): Int {
        return restaurantList.size
    }

}