package com.example.kotlineatitv2client.ui.restaurant

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kotlineatitv2client.Callback.IRestaurantCallbackListener
import com.example.kotlineatitv2client.Common.Common
import com.example.kotlineatitv2client.Model.CategoryModel
import com.example.kotlineatitv2client.Model.RestaurantModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RestaurantViewModel : ViewModel(), IRestaurantCallbackListener {
    private var restaurantListMutable : MutableLiveData<List<RestaurantModel>>?=null
    private var messageError:MutableLiveData<String> = MutableLiveData()
    private var restaurantCallBackListener: IRestaurantCallbackListener

    init {
        restaurantCallBackListener = this
    }

    fun getMessageError():MutableLiveData<String>{
        return messageError
    }

    fun getRestaurantList() :MutableLiveData<List<RestaurantModel>>{
        if(restaurantListMutable == null)
        {
            restaurantListMutable = MutableLiveData()
            loadRestaurantFromFirebase()
        }
        return restaurantListMutable!!
    }

    private fun loadRestaurantFromFirebase() {
        val tempList = ArrayList<RestaurantModel>()
        val restaurantRef = FirebaseDatabase.getInstance().getReference(Common.RESTAURANT_REF)
        restaurantRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                restaurantCallBackListener.onRestaurantLoadFailed((p0.message!!))
            }
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    for (itemSnapshot in p0!!.children) {
                        val model =
                            itemSnapshot.getValue<RestaurantModel>(RestaurantModel::class.java)
                        model!!.uid = itemSnapshot.key!!
                        tempList.add(model!!)
                    }
                    if (tempList.size > 0)
                        restaurantCallBackListener.onRestaurantLoadSuccess(tempList)
                    else
                        restaurantCallBackListener.onRestaurantLoadFailed("Restaurant List empty")
                }
                else
                    restaurantCallBackListener.onRestaurantLoadFailed("Restaurant List doesn't exists ")
            }

        })
    }

    override fun onRestaurantLoadSuccess(restaurantList: List<RestaurantModel>) {
        restaurantListMutable!!.value = restaurantList
    }

    override fun onRestaurantLoadFailed(message: String) {
        messageError.value = message
    }

}