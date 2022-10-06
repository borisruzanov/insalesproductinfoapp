package com.mywebsite.insalesproductinfoapp.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.*
import com.google.gson.JsonObject
import com.mywebsite.insalesproductinfoapp.model.User
import com.mywebsite.insalesproductinfoapp.retrofit.ApiRepository
import com.mywebsite.insalesproductinfoapp.utils.AppSettings
import com.mywebsite.insalesproductinfoapp.utils.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(context: Context) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()
    val isLogged = MutableLiveData(false)
    val appSettings = AppSettings(context)
    var accountResponse = MutableLiveData<JsonObject?>()
    var isEmailFound = MutableLiveData(false)
    var isDeviceIdFound = MutableLiveData(false)
    var isStoreIdFound = MutableLiveData(false)

    init {
        viewModelScope.launch {
            delay(2000)
            checkUserLoginStatus()
        }
    }

    private fun checkUserLoginStatus() {
        val insalesStatus = appSettings.getString("INSALES_STATUS")
        if (insalesStatus!!.isNotEmpty() && insalesStatus == "logged") {
            val email = appSettings.getString("INSALES_EMAIL") as String
            Constants.firebaseUserId =
                email.replace(".", "_").replace("#", "_").replace("$", "_").replace("[", "_")
                    .replace("]", "_")
        }
//        val user = appSettings.getUser(Constants.user)
        _isLoading.value = false
//        isLogged.value = user != null && insalesStatus!!.isNotEmpty() && insalesStatus == "logged"
        isLogged.value = insalesStatus.isNotEmpty() && insalesStatus == "logged"
    }

    fun callSalesAccount(context: Context, shopName: String, email: String, password: String) {
        accountResponse = ApiRepository.getInstance(context).salesAccount(shopName, email, password)
    }

    fun getSalesAccountResponse(): LiveData<JsonObject?> {
        return accountResponse
    }

    fun checkEmailExist(email: String,reference: DatabaseReference){
        reference.child(email).get().addOnSuccessListener {
            if (it.value != null){
                isEmailFound.postValue(true)
            }
            else{
                isEmailFound.postValue(false)
            }

        }.addOnFailureListener{
            isEmailFound.postValue(false)
        }
//        reference.child(email).addListenerForSingleValueEvent(object :
//            ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (snapshot.hasChildren()) {
//                    var isFound = false
//                    for (item: DataSnapshot in snapshot.children) {
//                        if (item.hasChild("email")) {
//                            val value = item.child("email").getValue(String::class.java) as String
//                            if (value.isNotEmpty() && value == email) {
//                                isFound = true
//                                break
//                            }
//                            else{
//                                isFound = false
//                            }
//                        }
//                    }
//                    isEmailFound.postValue(isFound)
//                } else {
//                    isEmailFound.postValue(false)
//                }
//
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                isEmailFound.postValue(false)
//            }
//
//        })
    }

    fun checkDeviceIdExist(deviceId: String,reference: DatabaseReference){
        reference.child(deviceId).get().addOnSuccessListener {
            if (it.value != null){
                isDeviceIdFound.postValue(true)
            }
            else{
                isDeviceIdFound.postValue(false)
            }

        }.addOnFailureListener{
            isDeviceIdFound.postValue(false)
        }
//        reference.child(deviceId).addListenerForSingleValueEvent(object :
//            ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (snapshot.hasChildren()) {
//                    var isFound = false
//                    for (item: DataSnapshot in snapshot.children) {
//                        if (item.hasChild("deviceId")) {
//                            val value = item.child("deviceId").getValue(String::class.java) as String
//                            if (value.isNotEmpty() && value == deviceId) {
//                                isFound = true
//                                break
//                            }
//                            else{
//                                isFound = false
//                            }
//                        }
//                    }
//                    isDeviceIdFound.postValue(isFound)
//                } else {
//                    isDeviceIdFound.postValue(false)
//                }
//
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                isDeviceIdFound.postValue(false)
//            }
//
//        })
    }

    fun checkStoreIdExist(storeId: String,reference: DatabaseReference){
        reference.child(storeId).get().addOnSuccessListener {
            if (it.value != null){
                isStoreIdFound.postValue(true)
            }
            else{
                isStoreIdFound.postValue(false)
            }

        }.addOnFailureListener{
            isStoreIdFound.postValue(false)
        }
//        reference.child(storeId).addListenerForSingleValueEvent(object :
//            ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (snapshot.hasChildren()) {
//                    var isFound = false
//                    for (item: DataSnapshot in snapshot.children) {
//                        if (item.hasChild("storeId")) {
//                            val value = item.child("storeId").getValue(String::class.java) as String
//                            if (value.isNotEmpty() && value == storeId) {
//                                isFound = true
//                                break
//                            }
//                            else{
//                                isFound = false
//                            }
//                        }
//                    }
//                    isStoreIdFound.postValue(isFound)
//                } else {
//                    isStoreIdFound.postValue(false)
//                }
//
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                isStoreIdFound.postValue(false)
//            }
//
//        })
    }

}