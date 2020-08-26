package com.example.kotlineatitv2client.ui.view_orders

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidwidgets.formatedittext.widgets.FormatEditText
import com.example.kotlineatitv2client.Adapter.MyOrderAdapter
import com.example.kotlineatitv2client.Callback.ILoadOrderCallbackListener
import com.example.kotlineatitv2client.Callback.IMyButtonCallback
import com.example.kotlineatitv2client.Common.Common
import com.example.kotlineatitv2client.Common.MySwipeHelper
import com.example.kotlineatitv2client.Database.CartDataSource
import com.example.kotlineatitv2client.Database.CartDatabase
import com.example.kotlineatitv2client.Database.LocalCartDataSource
import com.example.kotlineatitv2client.EventBus.CountCartEvent
import com.example.kotlineatitv2client.EventBus.MenuItemBack
import com.example.kotlineatitv2client.Model.OrderModel
import com.example.kotlineatitv2client.Model.RefundRequestModel
import com.example.kotlineatitv2client.Model.ShippingOrderModel
import com.example.kotlineatitv2client.R
import com.example.kotlineatitv2client.TrackingOrderActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dmax.dialog.SpotsDialog
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ViewOrderFragment : Fragment(), ILoadOrderCallbackListener {

    private var viewOrderModel : ViewOrderModel?=null

    lateinit var cartDataSource: CartDataSource
    var compositeDisposable = CompositeDisposable()

    internal lateinit var dialog:AlertDialog
    internal lateinit var recycler_order:RecyclerView
    internal lateinit var listener:ILoadOrderCallbackListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewOrderModel = ViewModelProviders.of(this).get(ViewOrderModel::class.java)
        val root = inflater.inflate(R.layout.fragment_view_orders,container,false)
        initViews(root)
        loadOrderFromFirebase()

        viewOrderModel!!.mutableLiveDataOrderList.observe(this, Observer {
            Collections.reverse(it!!)
            val adapter = MyOrderAdapter(context!!,it!!.toMutableList())
            recycler_order!!.adapter = adapter
        })

        return root
    }

    private fun loadOrderFromFirebase() {
        dialog.show()
        val orderList = ArrayList<OrderModel>()

        FirebaseDatabase.getInstance().getReference(Common.RESTAURANT_REF)
            .child(Common.currentRestaurant!!.uid)
            .child(Common.ORDER_REF)
            .orderByChild("userId")
            .equalTo(Common.currentUser!!.uid!!)
            .limitToLast(100)
            .addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    listener.onLoadOrderFailed(p0.message)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    for (orderSnapShot in p0.children)
                    {
                        val order = orderSnapShot.getValue(OrderModel::class.java)
                        order!!.orderNumber = orderSnapShot.key
                        orderList.add(order!!)
                    }
                    listener.onLoadOrderSuccess(orderList)
                }

            })
    }

    private fun initViews(root: View?) {

        cartDataSource = LocalCartDataSource(CartDatabase.getInstance(context!!).cartDAO())

        listener = this

        dialog = SpotsDialog.Builder().setContext(context!!).setCancelable(false).build()

        recycler_order = root!!.findViewById(R.id.recycler_order) as RecyclerView
        recycler_order.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context!!)
        recycler_order.layoutManager = layoutManager
        recycler_order.addItemDecoration(DividerItemDecoration(context!!,layoutManager.orientation))

        val swipe = object: MySwipeHelper(context!!,recycler_order!!,250)
        {
            override fun instantiateMyButton(
                viewHolder: RecyclerView.ViewHolder,
                buffer: MutableList<MyButton>
            ) {
                buffer.add(MyButton(context!!,
                    "Cancel Order",
                    30,
                    0,
                    Color.parseColor("#FF3C30"),
                    object: IMyButtonCallback {
                        override fun onClick(pos: Int) {
                            val orderModel = (recycler_order.adapter as MyOrderAdapter).getItemAtPosition(pos)
                            if (orderModel.orderStatus == 0)
                            {
                                if(orderModel.isCod)
                                {
                                    val builder = androidx.appcompat.app.AlertDialog.Builder(context!!)
                                    builder.setTitle("Cancel Order")
                                        .setMessage("Do you really want to cancel this order?")
                                        .setNegativeButton("NO"){dialogInterface, i ->
                                            dialogInterface.dismiss()
                                        }
                                        .setPositiveButton("YES"){dialogInterface, i->

                                            val update_data = HashMap<String,Any>()
                                            update_data.put("orderStatus",-1) //Cancel order
                                            FirebaseDatabase.getInstance()
                                                .getReference(Common.RESTAURANT_REF)
                                                .child(Common.currentRestaurant!!.uid!!)
                                                .child(Common.ORDER_REF)
                                                .child(orderModel.orderNumber!!)
                                                .updateChildren(update_data)
                                                .addOnFailureListener { e->
                                                    Toast.makeText(context!!,e.message,Toast.LENGTH_SHORT).show()
                                                }
                                                .addOnSuccessListener {
                                                    orderModel.orderStatus = -1 //Local update
                                                    (recycler_order.adapter as MyOrderAdapter).setItemAtPosition(pos,orderModel)
                                                    (recycler_order.adapter as MyOrderAdapter).notifyItemChanged(pos) //Update
                                                    Toast.makeText(context!!,"Cancel order successfully",Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                    val dialog = builder.create()
                                    dialog.show()
                                }
                                else //not COD
                                {
                                    val view = LayoutInflater.from(context!!)
                                        .inflate(R.layout.layout_refund_request,null)

                                    val edt_name = view.findViewById<EditText>(R.id.edt_card_name)
                                    val edt_card_number = view.findViewById<FormatEditText>(R.id.edt_card_number)
                                    val edt_card_exp = view.findViewById<FormatEditText>(R.id.edt_exp)

                                    //Set format
                                    edt_card_number.setFormat("---- ---- ---- ----")
                                    edt_card_exp.setFormat("--/--")


                                    val builder = androidx.appcompat.app.AlertDialog.Builder(context!!)
                                    builder.setTitle("Cancel Order")
                                        .setMessage("Do you really want to cancel this order?")
                                        .setView(view)
                                        .setNegativeButton("NO"){dialogInterface, i ->
                                            dialogInterface.dismiss()
                                        }
                                        .setPositiveButton("YES"){dialogInterface, i->

                                            val refundRequestModel = RefundRequestModel()
                                            refundRequestModel.name = Common.currentUser!!.name!!
                                            refundRequestModel.phone = Common.currentUser!!.phone!!
                                            refundRequestModel.cardNumber = edt_card_number.text.toString()
                                            refundRequestModel.cardExp = edt_card_exp.text.toString()
                                            refundRequestModel.amount = orderModel.finalPayment
                                            refundRequestModel.cardName = edt_name.text.toString()

                                            FirebaseDatabase.getInstance()
                                                .getReference(Common.RESTAURANT_REF)
                                                .child(Common.currentRestaurant!!.uid!!)
                                                .child(Common.REFUND_REQUEST_REF)
                                                .child(orderModel.orderNumber!!)
                                                .setValue(refundRequestModel)
                                                .addOnFailureListener { e->
                                                    Toast.makeText(context!!,e.message,Toast.LENGTH_SHORT).show()
                                                }
                                                .addOnSuccessListener {

                                                    //Update Data Firebase
                                                    val update_data = HashMap<String,Any>()
                                                    update_data.put("orderStatus",-1) //Cancel order
                                                    FirebaseDatabase.getInstance()
                                                        .getReference(Common.RESTAURANT_REF)
                                                        .child(Common.currentRestaurant!!.uid!!)
                                                        .child(Common.ORDER_REF)
                                                        .child(orderModel.orderNumber!!)
                                                        .updateChildren(update_data)
                                                        .addOnFailureListener { e->
                                                            Toast.makeText(context!!,e.message,Toast.LENGTH_SHORT).show()
                                                        }
                                                        .addOnSuccessListener {
                                                            orderModel.orderStatus = -1 //Local update
                                                            (recycler_order.adapter as MyOrderAdapter).setItemAtPosition(pos,orderModel)
                                                            (recycler_order.adapter as MyOrderAdapter).notifyItemChanged(pos) //Update
                                                            Toast.makeText(context!!,"Cancel order successfully",Toast.LENGTH_SHORT).show()
                                                        }
                                                }
                                        }
                                    val dialog = builder.create()
                                    dialog.show()
                                }
                            }
                            else
                            {
                                Toast.makeText(context!!,StringBuilder("Your order status was changed to ")
                                    .append(Common.convertStatusToText(orderModel.orderStatus))
                                    .append(", so you can't cancel it"),Toast.LENGTH_SHORT).show()
                            }
                        }

                    }))

                        //Tracking button
                    buffer.add(MyButton(context!!,
                    "Tracking Order",
                    30,
                    0,
                    Color.parseColor("#001970"),
                    object: IMyButtonCallback {
                        override fun onClick(pos: Int) {
                            val orderModel = (recycler_order.adapter as MyOrderAdapter).getItemAtPosition(pos)
                            //fetch from firebase
                            FirebaseDatabase.getInstance()
                                .getReference(Common.RESTAURANT_REF)
                                .child(Common.currentRestaurant!!.uid!!)
                                .child(Common.SHIPPING_ORDER_REF)
                                .child(orderModel.orderNumber!!)
                                .addListenerForSingleValueEvent(object:ValueEventListener{
                                    override fun onCancelled(p0: DatabaseError) {
                                        Toast.makeText(context!!,p0.message,Toast.LENGTH_SHORT).show()
                                    }

                                    override fun onDataChange(p0: DataSnapshot) {
                                        if (p0.exists())
                                        {
                                            Common.currentShippingOrder = p0.getValue(ShippingOrderModel::class.java)
                                            Common.currentShippingOrder!!.key = p0.key
                                            if (Common.currentShippingOrder!!.currentLat!! != -1.0 &&
                                                Common.currentShippingOrder!!.currentLng!! != -1.0)
                                            {
                                                startActivity(Intent(context!!,TrackingOrderActivity::class.java))
                                            }
                                            else
                                            {
                                                Toast.makeText(context!!,"Your order has not been ship, please wait",Toast.LENGTH_SHORT).show()
                                            
                                            }
                                        }
                                        else
                                        {
                                            Toast.makeText(context!!,"You just place your order, please wait it shipping",Toast.LENGTH_SHORT).show()
                                        }
                                    }

                                })
                        }

                    }))

                //Repeat Order
                buffer.add(MyButton(context!!,
                        "Repeat Order",
                        30,
                        0,
                        Color.parseColor("#5d4037"),
                        object: IMyButtonCallback {
                            override fun onClick(pos: Int) {
                                val orderModel = (recycler_order.adapter as MyOrderAdapter).getItemAtPosition(pos)

                                dialog.show()

                                //Clear all item in cart
                                cartDataSource.cleanCart(Common.currentUser!!.uid!!,Common.currentRestaurant!!.uid)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(object:SingleObserver<Int>{
                                            override fun onSuccess(t: Int) {
                                                //after clean cart, just add new
                                                val cartItems = orderModel.cartItemList!!.toTypedArray()

                                                compositeDisposable.add(
                                                        cartDataSource.insertOrReplaceAll(*cartItems) //*mean we insert varargs item
                                                                .subscribeOn(Schedulers.io())
                                                                .observeOn(AndroidSchedulers.mainThread())
                                                                .subscribe({
                                                                    dialog.dismiss()
                                                                    EventBus.getDefault().postSticky(CountCartEvent(true)) //Update fab count
                                                                    Toast.makeText(context!!,"Add all item to cart success",Toast.LENGTH_SHORT).show()
                                                                },{
                                                                    t: Throwable? ->
                                                                    dialog.dismiss()
                                                                    Toast.makeText(context!!,"Add all item to cart success",Toast.LENGTH_SHORT).show()

                                                                })
                                                )
                                            }

                                            override fun onSubscribe(d: Disposable) {

                                            }

                                            override fun onError(e: Throwable) {
                                                dialog.dismiss()
                                                Toast.makeText(context!!,e.message!!,Toast.LENGTH_SHORT).show()
                                            }

                                        })

                            }

                        }))

            }

        }
    }

    override fun onLoadOrderSuccess(orderList: List<OrderModel>) {
        //We need implement it
        dialog.dismiss()
        viewOrderModel!!.setMutableLiveDataOrderList(orderList)
    }

    override fun onLoadOrderFailed(message: String) {
        dialog.dismiss()
        Toast.makeText(context!!,message,Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        EventBus.getDefault().postSticky(MenuItemBack())
        compositeDisposable.clear()
        super.onDestroy()
    }
}