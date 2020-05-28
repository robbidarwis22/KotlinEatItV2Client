package com.example.kotlineatitv2client.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kotlineatitv2client.Common.Common
import com.example.kotlineatitv2client.Model.OrderModel
import com.example.kotlineatitv2client.R
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*

class MyOrderAdapter (private val context:Context,
private val orderList:List<OrderModel>):
    RecyclerView.Adapter<MyOrderAdapter.MyViewHolder>(){

    internal var calendar : Calendar
    internal var simpleDateFormat : SimpleDateFormat

    init {
        calendar = Calendar.getInstance()
        simpleDateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        internal var img_order:ImageView?=null
        internal var txt_order_date:TextView?=null
        internal var txt_order_status:TextView?=null
        internal var txt_order_number:TextView?=null
        internal var txt_order_comment:TextView?=null

        init {
            img_order = itemView.findViewById(R.id.img_order) as ImageView
            txt_order_date = itemView.findViewById(R.id.txt_order_date) as TextView
            txt_order_status = itemView.findViewById(R.id.txt_order_status) as TextView
            txt_order_number = itemView.findViewById(R.id.txt_order_number) as TextView
            txt_order_comment = itemView.findViewById(R.id.txt_order_comment) as TextView
        }
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context!!)
            .inflate(R.layout.layout_order_item,parent,false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context!!)
            .load(orderList[position].cartItemList!![0].foodImage)
            .into(holder.img_order!!)
        calendar.timeInMillis = orderList[position].createDate
        val date = Date(orderList[position].createDate)
        holder.txt_order_date!!.text = StringBuilder(Common.getDateOfWeek(calendar.get(Calendar.DAY_OF_WEEK)))
            .append("")
            .append(simpleDateFormat.format(date))
        holder.txt_order_number!!.text = StringBuilder("Order number: ").append(orderList[position].orderNumber)
        holder.txt_order_comment!!.text = StringBuilder("Comment: ").append(orderList[position].comment)
        holder.txt_order_status!!.text = StringBuilder("Status: ").append(Common.convertStatusToText(orderList[position].orderStatus))

    }


}