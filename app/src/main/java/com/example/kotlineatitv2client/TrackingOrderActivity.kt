package com.example.kotlineatitv2client

import android.Manifest
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.kotlineatitv2client.Common.Common
import com.example.kotlineatitv2client.Common.MyCustomInfoWindow
import com.example.kotlineatitv2client.Model.ShippingOrderModel
import com.example.kotlineatitv2client.Remote.IGoogleAPI
import com.example.kotlineatitv2client.Remote.RetrofitGoogleAPIClient
import com.google.android.gms.common.api.GoogleApi

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_tracking_order.*
import org.json.JSONObject

class TrackingOrderActivity : AppCompatActivity(), OnMapReadyCallback, ValueEventListener {

    private lateinit var mMap: GoogleMap

    private var shipperMarker:Marker?=null
    private var polylineOptions:PolylineOptions?=null
    private var blackPolylineOptions:PolylineOptions?=null
    private var blackPolyline:Polyline?=null
    private var grayPolyline:Polyline?=null
    private var redPolyline:Polyline?=null
    private var polylineList:List<LatLng> = ArrayList()

    private lateinit var iGoogleAPI: IGoogleAPI;
    private val compositeDisposable = CompositeDisposable()

    private lateinit var shipperRef:DatabaseReference
    private var isInit=false //first, isInit must be false
    //MoveMarker
    private var handler: Handler?=null
    private var index=0
    private var next:Int=0
    private var v = 0f
    private var lat = 0.0
    private var lng = 0.0
    private var startPosition=LatLng(0.0,0.0)
    private var endPosition=LatLng(0.0,0.0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking_order)

        iGoogleAPI = RetrofitGoogleAPIClient.instance!!.create(IGoogleAPI::class.java)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        subscribeShipperMove()

        initView()
    }

    private fun initView() {
        btn_call.setOnClickListener {
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse(StringBuilder("tel:").append(Common.currentShippingOrder!!.shipperPhone).toString())
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CALL_PHONE
                ) != PackageManager.PERMISSION_GRANTED)
            {
                Dexter.withActivity(this)
                    .withPermission(Manifest.permission.CALL_PHONE)
                    .withListener(object:PermissionListener{
                        override fun onPermissionGranted(response: PermissionGrantedResponse?) {

                        }

                        override fun onPermissionRationaleShouldBeShown(
                            permission: PermissionRequest?,
                            token: PermissionToken?
                        ) {

                        }

                        override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                            Toast.makeText(this@TrackingOrderActivity,"You must enable this permission to call",Toast.LENGTH_SHORT).show()
                        }

                    }).check()
                return@setOnClickListener
            }
            startActivity(intent)
        }
    }

    private fun subscribeShipperMove() {
        shipperRef = FirebaseDatabase.getInstance()
            .getReference(Common.RESTAURANT_REF)
            .child(Common.currentRestaurant!!.uid!!)
            .child(Common.SHIPPING_ORDER_REF)
            .child(Common.currentShippingOrder!!.key!!)
        shipperRef.addValueEventListener(this)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setInfoWindowAdapter(MyCustomInfoWindow(layoutInflater))

        mMap!!.uiSettings.isZoomControlsEnabled = true
        try {
            val success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.uber_light_with_label))
            if (!success)
                Log.d("Darwis","Failed to load map style")
        }catch (ex: Resources.NotFoundException)
        {
            Log.d("Darwis","Not found json string for map style")
        }

        drawRoutes()
    }

    private fun drawRoutes() {
        val locationOrder = LatLng(
            Common.currentShippingOrder!!.orderModel!!.lat,
            Common.currentShippingOrder!!.orderModel!!.lng)
        val locationShipper = LatLng(
            Common.currentShippingOrder!!.currentLat,
            Common.currentShippingOrder!!.currentLng)

        //Addbox
        mMap.addMarker(MarkerOptions()
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.box))
            .title(Common.currentShippingOrder!!.orderModel!!.userName)
            .snippet(Common.currentShippingOrder!!.orderModel!!.shippingAddress)
            .position(locationOrder))

        //add shipper
        if (shipperMarker == null)
        {
            val height = 80
            val width = 80
            val bitmapDrawable = ContextCompat.getDrawable(this@TrackingOrderActivity,R.drawable.shippernew)
            as BitmapDrawable
            val resized = Bitmap.createScaledBitmap(bitmapDrawable.bitmap,width,height,false)

            shipperMarker =  mMap.addMarker(MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(resized))
                .title(StringBuilder("shipper: ").append(Common.currentShippingOrder!!.shipperName).toString())
                .snippet(StringBuilder("Phone: ").append(Common.currentShippingOrder!!.shipperPhone)
                    .append("\n")
                    .append("Estimate Delivery Time: ")
                    .append(Common.currentShippingOrder!!.estimateTime).toString())
                .position(locationShipper))

            shipperMarker!!.showInfoWindow() //Always show windows

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationShipper,18.0f))
        }
        else
        {
            shipperMarker!!.position = locationShipper
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationShipper,18.0f))
        }

        //Draw route
        val to = StringBuilder().append(Common.currentShippingOrder!!.orderModel!!.lat)
            .append(",")
            .append(Common.currentShippingOrder!!.orderModel!!.lng)
            .toString()

        val from = StringBuilder().append(Common.currentShippingOrder!!.currentLat)
            .append(",")
            .append(Common.currentShippingOrder!!.currentLng)
            .toString()

        compositeDisposable.add(iGoogleAPI!!.getDirections("driving","less_driving",
            from,to,
            getString(R.string.google_maps_key))!!
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ s->
                try {
                    val jsonObject = JSONObject(s)
                    val jsonArray = jsonObject.getJSONArray("routes")
                    for (i in 0 until jsonArray.length())
                    {
                        val route = jsonArray.getJSONObject(i)
                        val poly = route.getJSONObject("overview_polyline")
                        val polyline = poly.getString("points")
                        polylineList = Common.decodePoly(polyline)
                    }

                    polylineOptions = PolylineOptions()
                    polylineOptions!!.color(Color.RED)
                    polylineOptions!!.width(12.0f)
                    polylineOptions!!.startCap(SquareCap())
                    polylineOptions!!.endCap(SquareCap())
                    polylineOptions!!.jointType(JointType.ROUND)
                    polylineOptions!!.addAll(polylineList)
                    redPolyline = mMap.addPolyline(polylineOptions)



                }catch (e:Exception)
                {
                    Log.d("DEBUG",e.message)
                }

            },{throwable ->
                Toast.makeText(this@TrackingOrderActivity,""+throwable.message,Toast.LENGTH_SHORT).show()
            }))
    }

    override fun onStop() {
        compositeDisposable.clear()
        super.onStop()
    }

    override fun onDestroy() {
        shipperRef.removeEventListener(this)
        isInit = false
        super.onDestroy()
    }

    override fun onCancelled(p0: DatabaseError) {

    }

    override fun onDataChange(dataSnapshot: DataSnapshot) {
        //Save old position
        val from = StringBuilder()
            .append(Common.currentShippingOrder!!.currentLat)
            .append(",")
            .append(Common.currentShippingOrder!!.currentLng)
            .toString()
        //Update Position
        Common.currentShippingOrder = dataSnapshot.getValue(ShippingOrderModel::class.java)
        Common.currentShippingOrder!!.key = dataSnapshot.key
        //Save new position
        val to = StringBuilder()
            .append(Common.currentShippingOrder!!.currentLat)
            .append(",")
            .append(Common.currentShippingOrder!!.currentLng)
            .toString()

        if (dataSnapshot.exists())
            if (isInit) moveMakerAnimation(shipperMarker,from,to) else isInit=true
    }

    private fun moveMakerAnimation(shipperMarker: Marker?, from: String, to: String) {
        compositeDisposable.add(iGoogleAPI!!.getDirections("driving",
            "less_driving",
            from,
            to,
            getString(R.string.google_maps_key))!!
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ s->
                Log.d("DEBUG",s)
                try {
                    val jsonObject = JSONObject(s)
                    val jsonArray = jsonObject.getJSONArray("routes")
                    for (i in 0 until jsonArray.length())
                    {
                        val route = jsonArray.getJSONObject(i)
                        val poly = route.getJSONObject("overview_polyline")
                        val polyline = poly.getString("points")
                        polylineList = Common.decodePoly(polyline)
                    }

                    polylineOptions = PolylineOptions()
                    polylineOptions!!.color(Color.GRAY)
                    polylineOptions!!.width(5.0f)
                    polylineOptions!!.startCap(SquareCap())
                    polylineOptions!!.endCap(SquareCap())
                    polylineOptions!!.jointType(JointType.ROUND)
                    polylineOptions!!.addAll(polylineList)
                    grayPolyline = mMap.addPolyline(polylineOptions)

                    blackPolylineOptions = PolylineOptions()
                    blackPolylineOptions!!.color(Color.BLACK)
                    blackPolylineOptions!!.width(5.0f)
                    blackPolylineOptions!!.startCap(SquareCap())
                    blackPolylineOptions!!.endCap(SquareCap())
                    blackPolylineOptions!!.jointType(JointType.ROUND)
                    blackPolylineOptions!!.addAll(polylineList)
                    blackPolyline = mMap.addPolyline(blackPolylineOptions)

                    //Animator
                    val polylineAnimator = ValueAnimator.ofInt(0,100)
                    polylineAnimator.setDuration(2000)
                    polylineAnimator.setInterpolator(LinearInterpolator())
                    polylineAnimator.addUpdateListener { valueAnimator ->
                        val points = grayPolyline!!.points
                        val percentValue = Integer.parseInt(valueAnimator.animatedValue.toString())
                        val size = points.size
                        val newPoints = (size*(percentValue / 100.0f)).toInt()
                        val p = points.subList(0,newPoints)
                        blackPolyline!!.points = p

                    }
                    polylineAnimator.start()

                    //Car moving
                    index = -1
                    next = 1
                    val r = object: Runnable {
                        override fun run() {
                            if (index < polylineList.size - 1)
                            {
                                index++
                                next = index + 1
                                startPosition = polylineList[index]
                                endPosition = polylineList[next]
                            }

                            val valueAnimator = ValueAnimator.ofInt(0,1)
                            valueAnimator.setDuration(1500)
                            valueAnimator.setInterpolator(LinearInterpolator())
                            valueAnimator.addUpdateListener { valueAnimator ->
                                v = valueAnimator.animatedFraction
                                lat = v * endPosition!!.latitude + (1-v) * startPosition!!.latitude
                                lng = v * endPosition!!.longitude + (1-v) * startPosition!!.longitude

                                val newPos = LatLng(lat,lng)
                                shipperMarker!!.position = newPos
                                shipperMarker!!.setAnchor(0.5f,0.5f)
                                shipperMarker!!.rotation = Common.getBearing(startPosition!!,newPos)

                                mMap.moveCamera(CameraUpdateFactory.newLatLng(shipperMarker.position)) //Fixed

                            }

                            valueAnimator.start()
                            if (index < polylineList.size - 2)
                                handler!!.postDelayed(this,1500)
                        }

                    }

                    handler = Handler()
                    handler!!.postDelayed(r,1500)



                }catch (e:Exception)
                {
                    Log.d("DEBUG",e.message)
                }

            },{throwable ->
                Toast.makeText(this@TrackingOrderActivity,""+throwable.message,Toast.LENGTH_SHORT).show()
            }))
    }
}