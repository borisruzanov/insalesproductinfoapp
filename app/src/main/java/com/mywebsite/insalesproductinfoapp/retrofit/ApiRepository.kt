package com.mywebsite.insalesproductinfoapp.retrofit

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.mywebsite.insalesproductinfoapp.utils.VolleySingleton
import com.google.gson.JsonObject
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ApiRepository {

    var apiInterface: ApiServices = RetrofitClientApi.createService(ApiServices::class.java)

    companion object {
        lateinit var context: Context

        private var apiRepository: ApiRepository? = null
        fun getInstance(mContext: Context): ApiRepository {
            context = mContext
            if (apiRepository == null) {
                apiRepository = ApiRepository()
            }
            return apiRepository!!
        }
    }

    // THIS FUNCTION WILL SEND THE POST REQUEST TO SERVER FOR CREATING DYNAMIC QR
//     fun createDynamicQrCode(body: HashMap<String, String>): MutableLiveData<JsonObject> {
//        val res = MutableLiveData<JsonObject>()
//        val bodyJson = Gson().toJsonTree(body).asJsonObject
//        Log.d("TEST199", bodyJson.toString())
//        apiInterface.createDynamicQrCode(bodyJson).enqueue(object:Callback<JsonObject>{
//            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
//                res.postValue(response.body())
//            }
//
//            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
//                res.postValue(null)
//            }
//        })
//
//        return res
//     }

    // THIS FUNCTION WILL SEND THE POST REQUEST TO SERVER FOR CREATING COUPON QR
//    fun createCouponQrCode(body: HashMap<String, String>): MutableLiveData<JsonObject> {
//        val res = MutableLiveData<JsonObject>()
//        val bodyJson = Gson().toJsonTree(body).asJsonObject
//        Log.d("TEST199", bodyJson.toString())
//        apiInterface.createCouponQrCode(bodyJson).enqueue(object:Callback<JsonObject>{
//            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
//                res.postValue(response.body())
//            }
//
//            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
//                res.postValue(null)
//            }
//        })
//
//        return res
//    }

    // THIS FUNCTION WILL SEND THE POST REQUEST TO SERVER FOR CREATING COUPON QR
//    fun createFeedbackQrCode(body: HashMap<String, String>): MutableLiveData<JsonObject> {
//        val res = MutableLiveData<JsonObject>()
//        val bodyJson = Gson().toJsonTree(body).asJsonObject
//        Log.d("TEST199", bodyJson.toString())
//        apiInterface.createFeedbackQrCode(bodyJson).enqueue(object:Callback<JsonObject>{
//            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
//                res.postValue(response.body())
//            }
//
//            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
//                res.postValue(null)
//            }
//        })
//
//        return res
//    }


//    fun signUp(body: HashMap<String, String>): MutableLiveData<JsonObject> {
//        val res = MutableLiveData<JsonObject>()
//        val bodyJson = Gson().toJsonTree(body).asJsonObject
//        Log.d("TEST199", bodyJson.toString())
//        apiInterface.signUp(bodyJson).enqueue(object:Callback<JsonObject>{
//            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
//                res.postValue(response.body())
//            }
//
//            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
//                res.postValue(null)
//            }
//        })
//
//        return res
//    }

//    fun signIn(email:String): MutableLiveData<JsonObject> {
//        val res = MutableLiveData<JsonObject>()
//        apiInterface.signIn(email).enqueue(object:Callback<JsonObject>{
//            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
//                res.postValue(response.body())
//            }
//
//            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
//                res.postValue(null)
//            }
//        })
//
//        return res
//    }

    // THIS FUNCTION WILL SEND THE POST REQUEST TO SERVER FOR CREATING SOCIAL NETWORK QR
//    fun createSnTemplate(body: SNPayload): MutableLiveData<JsonObject> {
//        val res = MutableLiveData<JsonObject>()
//        val bodyJson = Gson().toJsonTree(body).asJsonObject
//        apiInterface.createSnTemplate(bodyJson).enqueue(object:Callback<JsonObject>{
//            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
//                res.postValue(response.body())
//            }
//
//            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
//                res.postValue(null)
//            }
//        })
//
//        return res
//    }

//    fun getAllFeedbacks(id:String): MutableLiveData<FeedbackResponse> {
//        val res = MutableLiveData<FeedbackResponse>()
//        apiInterface.getAllFeedbacks(id).enqueue(object:Callback<FeedbackResponse>{
//            override fun onResponse(call: Call<FeedbackResponse>, response: Response<FeedbackResponse>) {
//                res.postValue(response.body())
//            }
//
//            override fun onFailure(call: Call<FeedbackResponse>, t: Throwable) {
//                res.postValue(null)
//            }
//        })
//
//        return res
//    }

    fun purchase(packageName:String,productId:String,token:String): MutableLiveData<JsonObject> {
        val res = MutableLiveData<JsonObject>()
        apiInterface.purchase(packageName,productId,token).enqueue(object:Callback<JsonObject>{
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                res.postValue(response.body())
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                res.postValue(null)
            }
        })

        return res
    }

    fun getUserPackageDetail(user_id:String):MutableLiveData<JSONObject?>{
        val packageResponse = MutableLiveData<JSONObject?>()

        val stringRequest = object : StringRequest(
            Method.POST, "https://itmagic.app/api/get_user_packages.php",
            com.android.volley.Response.Listener {
                val response = JSONObject(it)
                if (response.getInt("status") == 200){
//                  if (response.has("package") && !response.isNull("package")){
//                      val pkg = response.getJSONObject("package")
                      packageResponse.postValue(response)
                  }
                    else{
                      packageResponse.postValue(null)
                    }
//                }

            }, com.android.volley.Response.ErrorListener {
                packageResponse.postValue(null)
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["user_id"] = user_id
                return params
            }
        }

        stringRequest.retryPolicy = object : RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 50000
            }

            override fun getCurrentRetryCount(): Int {
                return 50000
            }

            @Throws(VolleyError::class)
            override fun retry(error: VolleyError) {
            }
        }

        VolleySingleton(context).addToRequestQueue(stringRequest)

        return packageResponse
    }


    fun salesAccount(shopName:String,email:String,password:String):MutableLiveData<JsonObject?>{
        val res = MutableLiveData<JsonObject?>()

        apiInterface.salesLoginAccount(email,password,shopName).enqueue(object:Callback<JsonObject>{
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                res.postValue(response.body())
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                res.postValue(null)
            }
        })

        return res
    }

    fun salesProducts(shopName:String,email:String,password:String,page:Int):MutableLiveData<JsonObject?>{
        val res = MutableLiveData<JsonObject?>()

        apiInterface.salesProducts(email,password,shopName,page).enqueue(object:Callback<JsonObject>{
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                res.postValue(response.body())
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                res.postValue(null)
            }
        })

        return res
    }

    fun salesProduct(shopName:String,email:String,password:String,pId:Int):MutableLiveData<JsonObject?>{
        val res = MutableLiveData<JsonObject?>()

        apiInterface.salesProduct(email,password,shopName,pId).enqueue(object:Callback<JsonObject>{
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                res.postValue(response.body())
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                res.postValue(null)
            }
        })

        return res
    }

    fun updateProductImage(shopName:String,email:String,password:String,image:String,pId:Int,position:Int,imageId:Int,fileName:String):MutableLiveData<JsonObject?>
    {
        val res = MutableLiveData<JsonObject?>()

        apiInterface.updateProductImage(email,password,shopName,image,pId,position,imageId,fileName).enqueue(object:Callback<JsonObject>{
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                res.postValue(response.body())
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                res.postValue(null)
            }
        })

        return res
    }

    fun updateProductImage(url:String,body: JSONObject):MutableLiveData<JsonObject?>
    {
        val res = MutableLiveData<JsonObject?>()

        apiInterface.updateProductImage(url,body).enqueue(object:Callback<JsonObject>{
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                res.postValue(response.body())
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                res.postValue(null)
            }
        })

        return res
    }

    fun addProductImage(shopName:String,email:String,password:String,image:String,pId:Int,fileName:String,src:String):MutableLiveData<JsonObject?>
    {
        val res = MutableLiveData<JsonObject?>()

        apiInterface.addProductImage(email,password,shopName,image,pId,fileName,src).enqueue(object:Callback<JsonObject>{
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                res.postValue(response.body())
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                res.postValue(null)
            }
        })

        return res
    }

    fun removeProductImage(shopName:String,email:String,password:String,pId:Int,imageId:Int):MutableLiveData<JsonObject?>
    {
        val res = MutableLiveData<JsonObject?>()

        apiInterface.removeProductImage(email,password,shopName,pId,imageId).enqueue(object:Callback<JsonObject>{
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                res.postValue(response.body())
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                res.postValue(null)
            }
        })

        return res
    }

    fun updateProductDetail(shopName:String,email:String,password:String,pId:Int,title:String,shortDesc:String,fullDesc:String):MutableLiveData<JsonObject?>
    {
        val res = MutableLiveData<JsonObject?>()

        apiInterface.updateProductDetail(email,password,shopName,pId,title,shortDesc,fullDesc).enqueue(object:Callback<JsonObject>{
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                res.postValue(response.body())
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                res.postValue(null)
            }
        })

        return res
    }

    fun categories(shopName:String,email:String,password:String):MutableLiveData<JsonObject?>{
        val res = MutableLiveData<JsonObject?>()

        apiInterface.categories(email,password,shopName).enqueue(object:Callback<JsonObject>{
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                res.postValue(response.body())
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                res.postValue(null)
            }
        })

        return res
    }

    fun addProduct(shopName:String,email:String,password:String,cId:Int,title:String,desc:String,quantity:String,price:String,barcodeId:String):MutableLiveData<JsonObject?>
    {
        val res = MutableLiveData<JsonObject?>()

        apiInterface.addProduct(email,password,shopName,cId,title,desc,quantity,price,barcodeId).enqueue(object:Callback<JsonObject>{
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                res.postValue(response.body())
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                res.postValue(null)
            }
        })

        return res
    }
}