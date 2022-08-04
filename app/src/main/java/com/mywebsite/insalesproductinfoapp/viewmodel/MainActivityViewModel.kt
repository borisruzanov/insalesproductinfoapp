package com.mywebsite.insalesproductinfoapp.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.mywebsite.insalesproductinfoapp.retrofit.ApiRepository

class MainActivityViewModel : ViewModel() {

    var productsResponse = MutableLiveData<JsonObject?>()
    var categoriesResponse = MutableLiveData<JsonObject?>()

    fun callProducts(context: Context, shopName:String, email:String, password:String, page:Int){
        productsResponse = ApiRepository.getInstance(context).salesProducts(shopName,email,password,page)
    }

    fun getSalesProductsResponse(): LiveData<JsonObject?> {
        return productsResponse
    }

    fun callCategories(context: Context,shopName:String,email:String,password:String){
        categoriesResponse = ApiRepository.getInstance(context).categories(shopName,email,password)
    }

    fun getCategoriesResponse():LiveData<JsonObject?>{
        return categoriesResponse
    }

}