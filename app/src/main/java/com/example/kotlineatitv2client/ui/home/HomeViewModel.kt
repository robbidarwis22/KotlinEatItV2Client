package com.example.kotlineatitv2client.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kotlineatitv2client.Callback.IBestDealLoadCallback
import com.example.kotlineatitv2client.Callback.IPopularLoadCallback
import com.example.kotlineatitv2client.Common.Common
import com.example.kotlineatitv2client.Model.BestDealModel
import com.example.kotlineatitv2client.Model.PopularCategoryModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeViewModel : ViewModel(), IPopularLoadCallback, IBestDealLoadCallback {
    override fun onBestDealLoadSuccess(bestDealList: List<BestDealModel>) {
        bestDealListMutableLiveData!!.value = bestDealList
    }

    override fun onBestDealLoadFailed(message: String) {
        messageError.value = message
    }

    override fun onPopularLoadSuccess(popularModelList: List<PopularCategoryModel>) {
        popularListMutableLiveData!!.value = popularModelList
    }

    override fun onPopularLoadFailed(message: String) {
        messageError.value = message
    }

    private var popularListMutableLiveData: MutableLiveData<List<PopularCategoryModel>>?=null
    private var bestDealListMutableLiveData: MutableLiveData<List<BestDealModel>>?=null
    private lateinit var messageError: MutableLiveData<String>
    private lateinit var popularLoadCallbackListener:IPopularLoadCallback
    private lateinit var bestDealLoadCallbackListener: IBestDealLoadCallback

    fun getBestDealList(key: String):LiveData<List<BestDealModel>>
    {
            if(bestDealListMutableLiveData == null)
            {
                bestDealListMutableLiveData = MutableLiveData()
                messageError = MutableLiveData()
                loadBestDealList(key)
            }
            return bestDealListMutableLiveData!!
        }

    private fun loadBestDealList(key: String) {
        val tempList = ArrayList<BestDealModel>()
        val bestDealRef = FirebaseDatabase.getInstance().getReference(Common.RESTAURANT_REF)
            .child(key)
            .child(Common.BEST_DEAL_REF)
        bestDealRef.addListenerForSingleValueEvent(object:ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                bestDealLoadCallbackListener.onBestDealLoadFailed((p0.message!!))
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (itemSnapshot in p0!!.children)
                {
                    val model = itemSnapshot.getValue<BestDealModel>(BestDealModel::class.java)
                    tempList.add(model!!)
                }
                bestDealLoadCallbackListener.onBestDealLoadSuccess(tempList)
            }

        })
    }

    fun getPopularList(key:String):LiveData<List<PopularCategoryModel>>
    {
        if(popularListMutableLiveData == null)
        {
            popularListMutableLiveData = MutableLiveData()
            messageError = MutableLiveData()
            loadPopularList(key)
        }
        return popularListMutableLiveData!!
    }

    private fun loadPopularList(key:String) {
        val tempList = ArrayList<PopularCategoryModel>()
        val popularRef = FirebaseDatabase.getInstance().getReference(Common.RESTAURANT_REF)
            .child(key)
            .child(Common.POPULAR_REF)
        popularRef.addListenerForSingleValueEvent(object:ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                popularLoadCallbackListener.onPopularLoadFailed((p0.message!!))
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (itemSnapshot in p0!!.children)
                {
                    val model = itemSnapshot.getValue<PopularCategoryModel>(PopularCategoryModel::class.java)
                    tempList.add(model!!)
                }
                popularLoadCallbackListener.onPopularLoadSuccess(tempList)
            }

        })
    }

    init {
        popularLoadCallbackListener = this
        bestDealLoadCallbackListener = this
    }
}