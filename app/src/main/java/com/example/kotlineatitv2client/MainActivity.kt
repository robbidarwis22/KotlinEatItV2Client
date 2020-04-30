package com.example.kotlineatitv2client

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.Toast
//import androidx.appcompat.app.AlertDialog
import android.app.AlertDialog
import android.content.DialogInterface
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.EditText
import com.example.kotlineatitv2client.Common.Common
import com.example.kotlineatitv2client.Model.UserModel
import com.example.kotlineatitv2client.Remote.ICloudFunctions
import com.example.kotlineatitv2client.Remote.RetrofitCloudClient
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import dmax.dialog.SpotsDialog
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var listener:FirebaseAuth.AuthStateListener
    private lateinit var dialog: AlertDialog
    private val compositeDisposable = CompositeDisposable()
    private lateinit var cloudFunctions:ICloudFunctions

    private lateinit var userRef:DatabaseReference
    private var providers:List<AuthUI.IdpConfig>? = null

    companion object{
        private val APP_REQUEST_CODE = 7171 //NOMER BEBAS
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(listener)
    }

    override fun onStop() {
        if(listener != null)
            firebaseAuth.removeAuthStateListener(listener)
        compositeDisposable.clear()
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
    }

    private fun init() {
        providers = Arrays.asList<AuthUI.IdpConfig>(AuthUI.IdpConfig.PhoneBuilder().build())
        userRef = FirebaseDatabase.getInstance().getReference(Common.USER_REFERENCE)
        firebaseAuth = FirebaseAuth.getInstance()
        dialog = SpotsDialog.Builder().setContext(this).setCancelable(false).build()
        cloudFunctions = RetrofitCloudClient.getInstance().create(ICloudFunctions::class.java)
        listener = FirebaseAuth.AuthStateListener { firebaseAuth ->

            Dexter.withActivity(this@MainActivity)
                .withPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(object:PermissionListener{
                    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                        val user = firebaseAuth.currentUser
                        if(user != null)
                        {
                            checkUserFromFirebase(user!!)
                        }
                        else
                        {
                            phoneLogin()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permission: PermissionRequest?,
                        token: PermissionToken?
                    ) {

                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                        Toast.makeText(this@MainActivity,"You must accept this permission to use app",Toast.LENGTH_SHORT).show()
                    }

                }).check()

        }
    }

    private fun checkUserFromFirebase(user: FirebaseUser) {
        dialog!!.show()
        userRef!!.child(user!!.uid).addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(this@MainActivity,""+p0.message,Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists())
                {
                    var userModel = p0.getValue(UserModel::class.java)
                    goToHomeActivity(userModel)
                } else{
                    showRegisterDialog(user!!)
                }

                    dialog!!.dismiss()

            }
        })
    }


    //BRAINTREE
//    private fun checkUserFromFirebase(user: FirebaseUser) {
//        dialog!!.show()
//        userRef!!.child(user!!.uid).addListenerForSingleValueEvent(object:ValueEventListener{
//            override fun onCancelled(p0: DatabaseError) {
//                Toast.makeText(this@MainActivity,""+p0.message,Toast.LENGTH_SHORT).show()
//            }
//
//            override fun onDataChange(p0: DataSnapshot) {
//                if (p0.exists())
//                {
//                    FirebaseAuth.getInstance().currentUser!!
//                        .getIdToken(true)
//                        .addOnFailureListener{ t->
//                            Toast.makeText(this@MainActivity,""+t.message,Toast.LENGTH_SHORT).show()
//                        }
//                        .addOnCompleteListener{
//                            Common.authorizeToken = it.result!!.token
//    val headers = HashMap<String,String>()
//    headers.put("Authorization",Common.buildToken(Common.authorizeToken!!))
//
//                                    compositeDisposable.add(cloudFunctions!!.getToken(headers)
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe({ braintreeToken ->
//
//                            dialog!!.dismiss()
//                            val userModel = p0.getValue(UserModel::class.java)
//                            goToHomeActivity(userModel,braintreeToken.token)
//
//                        },{ throwable ->
//                            dialog!!.dismiss()
//                            Toast.makeText(this@MainActivity,""+throwable.message,Toast.LENGTH_SHORT).show()
//                        }));
//                        }
//                } else
//                {
//                dialog!!.dismiss()
//                    showRegisterDialog(user!!)
//                }
//
//            }
//        })
//    }

    private fun showRegisterDialog(user:FirebaseUser) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("REGISTER")
        builder.setMessage("Please Fill Information")

        val itemView =
            LayoutInflater.from(this@MainActivity).inflate(R.layout.layout_register, null)

        val edt_name = itemView.findViewById<EditText>(R.id.edt_name)
        val edt_address = itemView.findViewById<EditText>(R.id.edt_address)
        val edt_phone = itemView.findViewById<EditText>(R.id.edt_phone)

        //set
        edt_phone.setText(user!!.phoneNumber)

        builder.setView(itemView)
        builder.setNegativeButton("CANCEL") { dialogInterface, i -> dialogInterface.dismiss() }
        builder.setPositiveButton("REGISTER") { dialogInterface, i ->
            if (TextUtils.isDigitsOnly(edt_name.text.toString())) {
                Toast.makeText(this@MainActivity, "Please Enter Your Name", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            else if (TextUtils.isDigitsOnly(edt_address.text.toString())) {
                Toast.makeText(this@MainActivity, "Please Enter Your Address", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            val userModel = UserModel()
            userModel.uid = user!!.uid
            userModel.name = edt_name.text.toString()
            userModel.address = edt_address.text.toString()
            userModel.phone = edt_phone.text.toString()

            userRef!!.child(user!!.uid).setValue(userModel).addOnCompleteListener{task ->
                if(task.isSuccessful)
                {
                    dialogInterface.dismiss()
                    Toast.makeText(this@MainActivity, "Congratulation! Register Successfully", Toast.LENGTH_SHORT).show()
                    goToHomeActivity(userModel)

                }
            }
        }

        //PENTING! TAMPILKAN PESAN DIALOG
        val dialog = builder.create()
        dialog.show()
    }


    //Braintree
//    private fun showRegisterDialog(user:FirebaseUser) {
//        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
//        builder.setTitle("REGISTER")
//        builder.setMessage("Please Fill Information")
//
//        val itemView =
//            LayoutInflater.from(this@MainActivity).inflate(R.layout.layout_register, null)
//
//        val edt_name = itemView.findViewById<EditText>(R.id.edt_name)
//        val edt_address = itemView.findViewById<EditText>(R.id.edt_address)
//        val edt_phone = itemView.findViewById<EditText>(R.id.edt_phone)
//
//        //set
//        edt_phone.setText(user!!.phoneNumber)
//
//        builder.setView(itemView)
//        builder.setNegativeButton("CANCEL") { dialogInterface, i -> dialogInterface.dismiss() }
//        builder.setPositiveButton("REGISTER") { dialogInterface, i ->
//            if (TextUtils.isDigitsOnly(edt_name.text.toString())) {
//                Toast.makeText(this@MainActivity, "Please Enter Your Name", Toast.LENGTH_SHORT).show()
//                return@setPositiveButton
//            }
//            else if (TextUtils.isDigitsOnly(edt_address.text.toString())) {
//                Toast.makeText(this@MainActivity, "Please Enter Your Address", Toast.LENGTH_SHORT).show()
//                return@setPositiveButton
//            }
//            val userModel = UserModel()
//            userModel.uid = user!!.uid
//            userModel.name = edt_name.text.toString()
//            userModel.address = edt_address.text.toString()
//            userModel.phone = edt_phone.text.toString()
//
//            userRef!!.child(user!!.uid).setValue(userModel).addOnCompleteListener{task ->
//                if(task.isSuccessful)
//                {
//                    FirebaseAuth.getInstance().currentUser!!
//                        .getIdToken(true)
//                        .addOnFailureListener { t ->
//                            Toast.makeText(this@MainActivity,""+t.message,Toast.LENGTH_SHORT).show()
//                        }
//                        .addOnCompleteListener {
//                            Common.authorizeToken = it!!.result!!.token
//    val headers = HashMap<String,String>()
//    headers.put("Authorization",Common.buildToken(Common.authorizeToken!!))
//                            compositeDisposable.add(cloudFunctions.getToken(headers)
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe({ braintreeToken ->
//                            dialogInterface.dismiss()
//                            Toast.makeText(this@MainActivity, "Congratulation! Register Successfully", Toast.LENGTH_SHORT).show()
////                            goToHomeActivity(userModel,braintreeToken.token)
//                            goToHomeActivity(userModel)
//                        },{t: Throwable? ->
//
//                            dialogInterface.dismiss()
//                            Toast.makeText(this@MainActivity, ""+t!!.message, Toast.LENGTH_SHORT).show()
//
//                        }))
//                        }
//                }
//            }
//        }
//
//        //PENTING! TAMPILKAN PESAN DIALOG
//        val dialog = builder.create()
//        dialog.show()
//    }


    private fun goToHomeActivity(userModel: UserModel?) {

        FirebaseInstanceId.getInstance()
            .instanceId
            .addOnFailureListener{ e -> Toast.makeText(this@MainActivity,""+e.message,Toast.LENGTH_SHORT).show()

                Common.currentUser = userModel!!

                startActivity(Intent(this@MainActivity,HomeActivity::class.java))
                finish()
            }
            .addOnCompleteListener{task ->
                if (task.isSuccessful)
                {

                    Common.currentUser = userModel!!
                    Common.updateToken(this@MainActivity,task.result!!.token)
                    //this function must be call after Common is assigned

                    startActivity(Intent(this@MainActivity,HomeActivity::class.java))
                    finish()
                }
            }
    }

    private fun phoneLogin() {

        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers!!).build(), APP_REQUEST_CODE)
    }

//    BRAINTREE
//    private fun goToHomeActivity(userModel: UserModel?,token:String?) {
//        Common.currentUser = userModel!!
//        Common.currenToken = token!!
//        startActivity(Intent(this@MainActivity,HomeActivity::class.java))
//        finish()
//    }


//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if(requestCode == APP_REQUEST_CODE)
//        {
//            val response = IdpResponse.fromResultIntent(data)
//            if(resultCode == Activity.RESULT_OK)
//            {
//                val user = FirebaseAuth.getInstance().currentUser
//            }
//            else
//            {
//                Toast.makeText(this,"Failed to Sign In",Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

}
