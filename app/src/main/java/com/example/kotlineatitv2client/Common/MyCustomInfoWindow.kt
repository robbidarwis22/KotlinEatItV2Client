package com.example.kotlineatitv2client.Common

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.example.kotlineatitv2client.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class MyCustomInfoWindow(layoutInflater: LayoutInflater):GoogleMap.InfoWindowAdapter {

    private lateinit var itemView:View

    init {
        itemView = layoutInflater.inflate(R.layout.layout_marker_display,null)
    }

    override fun getInfoContents(p0: Marker?): View? {
        return null
    }

    override fun getInfoWindow(marker: Marker?): View {
        val txt_shipper_name= itemView!!.findViewById<View>(R.id.txt_shipper_name) as TextView
        val txt_shipper_info= itemView!!.findViewById<View>(R.id.txt_shipper_info) as TextView
        txt_shipper_name.text = marker!!.title
        txt_shipper_info.text = marker!!.snippet

        return itemView!!
    }

}