package com.example.kotlineatitv2client.Common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.kotlineatitv2client.Model.*
import com.example.kotlineatitv2client.R
import com.example.kotlineatitv2client.Services.MyFCMServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.FirebaseDatabase
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.random.Random
import kotlin.text.StringBuilder

object Common {
    fun formatPrice(price: Double): String {
        if (price != 0.toDouble())
        {
            val df = DecimalFormat("#,##0.00")
            df.roundingMode = RoundingMode.HALF_UP
            val finalPrice = StringBuilder(df.format(price)).toString()
            return finalPrice.replace(".",",")
        }
        else
            return "0,00"
    }

    fun calculateExtraPrice(userSelectedSize: SizeModel?, userSelectedAddon: MutableList<AddonModel>?): Double {
        var result:Double=0.0
        if(userSelectedSize == null && userSelectedAddon == null)
            return 0.0
        else if(userSelectedSize == null)
        {
            for(addonModel in userSelectedAddon!!)
                result += addonModel.price!!.toDouble()
            return result
        }
        else if(userSelectedAddon == null)
        {
            result = userSelectedSize!!.price.toDouble()
            return result
        }
        else
        {
            result = userSelectedSize!!.price.toDouble()
            for(addonModel in userSelectedAddon!!)
                result += addonModel.price!!.toDouble()
            return result
        }
    }

    fun setSpanString(welcome: String, name: String?, txtUser: TextView?) {
        val builder = SpannableStringBuilder()
        builder.append(welcome)
        val txtSpannable = SpannableString(name)
        val boldSpan = StyleSpan(Typeface.BOLD)
        txtSpannable.setSpan(boldSpan,0,name!!.length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.append(txtSpannable)
        txtUser!!.setText(builder,TextView.BufferType.SPANNABLE)
    }

    fun createOrderNumber(): String {
        return StringBuilder()
                .append(System.currentTimeMillis())
                .append(Math.abs(Random.nextInt()))
                .toString()
    }

    fun getDateOfWeek(i: Int): String {
        when(i){
            1 -> return "Monday"
            2 -> return "Tuesday"
            3 -> return "Wednesday"
            4 -> return "Thursday"
            5 -> return "Friday"
            6 -> return "Saturday"
            7 -> return "Sunday"
            else -> return "Unk"
        }
    }

    fun convertStatusToText(orderStatus: Int): String   {
        when(orderStatus){
            0 -> return "Placed"
            1 -> return "Shipping"
            2 -> return "Shipped"
            -1 -> return "Cancelled"
            else -> return "Unk"
        }
    }

    fun updateToken(context: Context, token: String) {
        //Fix error crash first time
        if (Common.currentUser != null)
            FirebaseDatabase.getInstance()
                    .getReference(Common.TOKEN_REF)
                    .child(Common.currentUser!!.uid!!)
                    .setValue(TokenModel(Common.currentUser!!.phone!!,token))
                    .addOnFailureListener{ e-> Toast.makeText(context,""+e.message,Toast.LENGTH_SHORT).show()}
    }

    fun showNotification(context: Context, id: Int, title: String?, content: String?, intent: Intent?) {
        var pendingIntent : PendingIntent?=null
        if (intent != null)
            pendingIntent = PendingIntent.getActivity(context,id,intent,PendingIntent.FLAG_UPDATE_CURRENT)
        val NOTIFICATION_CHANNEL_ID = "com.example.eatitv2"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "Eat It V2",NotificationManager.IMPORTANCE_DEFAULT)

            notificationChannel.description = "Eat It V2"
            notificationChannel.enableLights(true)
            notificationChannel.enableVibration(true)
            notificationChannel.lightColor = (Color.RED)
            notificationChannel.vibrationPattern = longArrayOf(0,1000,500,1000)

            notificationManager.createNotificationChannel(notificationChannel)
        }
        val builder = NotificationCompat.Builder(context,NOTIFICATION_CHANNEL_ID)

        builder.setContentTitle(title!!).setContentText(content!!).setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources,R.drawable.ic_restaurant_menu_black_24dp))
        if(pendingIntent != null)
            builder.setContentIntent(pendingIntent)

        val notification = builder.build()

        notificationManager.notify(id,notification)
    }

    fun showNotification(context: Context, id: Int, title: String?, content: String?, bitmap: Bitmap, intent:Intent?) {
        var pendingIntent : PendingIntent?=null
        if (intent != null)
            pendingIntent = PendingIntent.getActivity(context,id,intent,PendingIntent.FLAG_UPDATE_CURRENT)
        val NOTIFICATION_CHANNEL_ID = "com.example.eatitv2"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID,
                "Eat It V2",NotificationManager.IMPORTANCE_DEFAULT)

            notificationChannel.description = "Eat It V2"
            notificationChannel.enableLights(true)
            notificationChannel.enableVibration(true)
            notificationChannel.lightColor = (Color.RED)
            notificationChannel.vibrationPattern = longArrayOf(0,1000,500,1000)

            notificationManager.createNotificationChannel(notificationChannel)
        }
        val builder = NotificationCompat.Builder(context,NOTIFICATION_CHANNEL_ID)

        builder.setContentTitle(title!!).setContentText(content!!).setAutoCancel(true)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setLargeIcon(bitmap)
            .setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap))
        if(pendingIntent != null)
            builder.setContentIntent(pendingIntent)

        val notification = builder.build()

        notificationManager.notify(id,notification)
    }

    fun getNewOrderTopic(): String? {
        return StringBuilder("/topics/new_order").toString()
    }

    fun decodePoly(encoded: String): List<LatLng> {
        val poly:MutableList<LatLng> = ArrayList<LatLng>()
        var index = 0
        var len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len)
        {
            var b:Int
            var shift=0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift +=5

            }while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift +=5
            }while (b >= 0x20)
            val dlng = if(result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val p = LatLng(lat.toDouble() / 1E5,lng.toDouble()/1E5)
            poly.add(p)
        }
        return poly
    }

//    fun buildToken(authorizeToken: String): String{
//        return StringBuilder("Bearer").append("").append(authorizeToken).toString()
//    }

    fun getBearing(begin: LatLng, end: LatLng): Float {
        val lat = Math.abs(begin.latitude - end.latitude)
        val lng = Math.abs(begin.longitude - end.longitude)
        if (begin.latitude < end.latitude && begin.longitude < end.longitude) return Math.toDegrees(
                Math.atan(lng / lat)
        )
                .toFloat() else if (begin.latitude >= end.latitude && begin.longitude < end.longitude) return (90 - Math.toDegrees(
                Math.atan(lng / lat)
        ) + 90).toFloat() else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude) return (Math.toDegrees(
                Math.atan(lng / lat)
        ) + 180).toFloat() else if (begin.latitude < end.latitude && begin.longitude >= end.longitude) return (90 - Math.toDegrees(
                Math.atan(lng / lat)
        ) + 270).toFloat()
        return (-1).toFloat()
    }

    fun findFoodInListById(category: CategoryModel, foodId: String): FoodModel? {
        return if (category!!.foods != null && category.foods!!.size > 0)
        {
            for(foodModel in category!!.foods!!) if (foodModel.id.equals(foodId))
                return foodModel
            null
        }else null
    }

    fun getNewsTopic(): String {
        //restore something like: restaurantid_news
        return StringBuilder("/topics/")
            .append(Common.currentRestaurant!!.uid)
            .append("_")
            .append("news")
            .toString()
    }

    fun getListAddon(addonModels: List<AddonModel>): String {
        val result = StringBuilder()
        for (addonModel in addonModels)
            result.append(addonModel.name).append(",")
        if (!result.isEmpty())
            return result.substring(0,result.length-1) //Remove last ","
        else
            return "Default"
    }

    var currentRestaurant: RestaurantModel?=null
    val RESTAURANT_REF: String = "Restaurant"
    val IMAGE_URL: String="IMAGE_URL"
    val IS_SEND_IMAGE: String="IS_SEND_IMAGE"

    var currentShippingOrder: ShippingOrderModel?=null
    val SHIPPING_ORDER_REF: String="ShippingOrder" //same as server app
    val REFUND_REQUEST_REF: String="RefundRequest"
    const val NOTI_TITLE = "title"
    const val NOTI_CONTENT = "content"
    var authorizeToken: String?=null
    var currenToken: String = ""
    const val ORDER_REF: String = "Order"
    const val COMMENT_REF: String = "Comment"
    var foodSelected: FoodModel?=null
    var categorySelected: CategoryModel?=null
    const val CATEGORY_REF: String = "Category"
    val FULL_WIDTH_COLUMN: Int=1
    val DEFAULT_COLUMN_COUNT: Int=0
    const val BEST_DEAL_REF: String="BestDeals"
    const val POPULAR_REF: String="MostPopular"
    val USER_REFERENCE="Users"
    var currentUser:UserModel?=null

    val TOKEN_REF = "Tokens"
}
