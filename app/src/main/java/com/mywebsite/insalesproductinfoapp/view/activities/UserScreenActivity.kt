package com.mywebsite.insalesproductinfoapp.view.activities

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.billingclient.api.*
import com.android.volley.VolleyError
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mywebsite.insalesproductinfoapp.R
import com.mywebsite.insalesproductinfoapp.databinding.ActivityUserScreenBinding
import com.mywebsite.insalesproductinfoapp.interfaces.APICallback
import com.mywebsite.insalesproductinfoapp.interfaces.ResponseListener
import com.mywebsite.insalesproductinfoapp.model.CouponCode
import com.mywebsite.insalesproductinfoapp.model.Feature
import com.mywebsite.insalesproductinfoapp.model.PurchaseDetail
import com.mywebsite.insalesproductinfoapp.utils.AppSettings
import com.mywebsite.insalesproductinfoapp.utils.Constants
import com.mywebsite.insalesproductinfoapp.utils.Security
import com.mywebsite.insalesproductinfoapp.viewmodel.UserScreenActivityViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class UserScreenActivity : BaseActivity(), View.OnClickListener, PurchasesUpdatedListener {

    private var purchaseDetail: PurchaseDetail? = null
    private lateinit var context: Context
    private lateinit var firebaseDatabase: DatabaseReference
    private lateinit var appSettings: AppSettings
//    private lateinit var auth: FirebaseAuth
    var userCurrentCreditsValue: Float = 0F
    private var userCurrentCredits = ""
    private var couponCodeCredits = 0
    private var isCouponCodeApplied = false
    private var billingClient: BillingClient? = null
    private var productId = ""
    private var productIdIndex = 0
    private var creditsValue: Float = 0F
    private var userId: String = ""
    private lateinit var viewModel: UserScreenActivityViewModel
    private var featuresKeys = mutableListOf<String>()
    private var featureCreditPrice = 0F
    private lateinit var binding:ActivityUserScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setUpToolbar()

    }

    private fun initViews() {
        context = this
//        auth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance().reference
        appSettings = AppSettings(context)
        binding.getCreditsBtn.setOnClickListener(this)
//        increaseStorageBtn = findViewById(R.id.increase_storage_btn)
        binding.increaseStorageBtn.setOnClickListener(this)
//        extendUsageBtn = findViewById(R.id.extend_usage_btn)
        binding.extendUsageBtn.setOnClickListener(this)
        userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String
//        viewModel = ViewModelProviders.of(
//            this,
//            ViewModelFactory(UserScreenActivityViewModel()).createFor()
//        )[UserScreenActivityViewModel::class.java]
        viewModel = ViewModelProvider(this).get(UserScreenActivityViewModel::class.java)
        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases().setListener(this).build()
        binding.applyCouponCodeBtn.setOnClickListener(this)
        binding.compressorFeatureBtn.setOnClickListener(this)
        binding.modesSwitcherFeatureBtn.setOnClickListener(this)
        binding.premiumSupportFeatureBtn.setOnClickListener(this)
        binding.unlimitedTablesFeatureBtn.setOnClickListener(this)


//        imagesSearchFeatureBtn.setOnClickListener(this)
        featuresKeys.addAll(resources.getStringArray(R.array.features_keys))
    }

    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.title = getString(R.string.user_screen)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }

    }

    override fun onResume() {
        super.onResume()
//        getUserCredit()
        //getUserSubscriptionDetails()
        //imagesSearchDetailView.text = "${Constants.searchImageCreditPrice} ${getString(R.string.credits_for_text)} ${Constants.searchImagesLimit} ${getString(R.string.images_per_api_request_text)}"
    }

    private fun getUserCredit() {
        if (Constants.firebaseUserId.isNotEmpty()) {

            val userId = Constants.firebaseUserId
            //Constants.firebaseUserId = userId
            firebaseDatabase.child(Constants.firebaseUserCredits)
                .child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        if (snapshot.hasChildren() && snapshot.hasChild("credits")) {
                            val previousCredits =
                                snapshot.child("credits").getValue(String::class.java)
                            userCurrentCreditsValue = if (previousCredits!!.isNotEmpty()) {
                                previousCredits.toFloat()
                            } else {
                                0F
                            }
                        }
                        val roundedCreditValues =
                            userCurrentCreditsValue.toBigDecimal().setScale(2, RoundingMode.UP)
                                .toDouble()
                        binding.usTotalCreditsView.text = "$roundedCreditValues"
                        appSettings.putString(Constants.userCreditsValue, "$roundedCreditValues")
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
        }
    }

    private fun getUserSubscriptionDetails() {
        if (Constants.firebaseUserId.isNotEmpty()) {

            val userId = Constants.firebaseUserId//auth.currentUser!!.uid

            startLoading(context)
            viewModel.callUserPackageDetail(context, userId)
            viewModel.getUserPackageResponse().observe(this) { response ->
                dismiss()
                startLoading(context)
                getUserFeatureDetails()
                if (response != null) {
                    if (response.has("package") && !response.isNull("package")) {

                        val packageDetail: JSONObject? = response.getJSONObject("package")
                        val startDate = packageDetail!!.getString("start_date")
                        val endDate = packageDetail.getString("end_date")
                        val expiredTimeMili = SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.ENGLISH
                        ).parse(endDate)!!.time

                        val diff1 = System.currentTimeMillis() - SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.ENGLISH
                        ).parse(startDate)!!.time

                        val goneDays = TimeUnit.DAYS.convert(diff1, TimeUnit.MILLISECONDS).toInt()

                        val remainingDay = Constants.calculateDays(
                            SimpleDateFormat(
                                "yyyy-MM-dd",
                                Locale.ENGLISH
                            ).parse(startDate)!!.time,
                            SimpleDateFormat(
                                "yyyy-MM-dd",
                                Locale.ENGLISH
                            ).parse(endDate)!!.time
                        )

                        val availableSize = packageDetail.getString("size")
                        val tSize = packageDetail.getInt("total_size")
//                        val availableDuration = packageDetail.getInt("duration")
                        val usage = tSize - availableSize.toDouble()
                        val formatted = String.format("%.2f", usage)
                        binding.usStorageSpaceView.text = "$formatted MB of $tSize MB"
                        binding.usDurationView.text = "in $remainingDay days | on ${
                            getDateFormateFromTimeStamp(expiredTimeMili)
                        }"
                    }

                }

            }

        }
    }

    private fun getUserFeatureDetails() {
        getUserFeaturesDetails(context, Constants.firebaseUserId, object : APICallback {
            override fun onSuccess(response: JSONObject) {
                dismiss()
                if (response.getInt("status") == 200) {
                    val features = response.getJSONArray("features")
                    if (features.length() > 0) {
                        for (i in 0 until features.length()) {
                            val feature = features.getJSONObject(i)
                            val status = feature.getInt("status")
                            when(feature.getString("feature")){
                                "compressor_feature"->{
                                    Constants.compressorFeatureStatus = status
                                }
                                "modes_switcher"->{
                                    Constants.modesSwitcherFeatureStatus = status
                                }
                                "premium_support"->{
                                    Constants.premiumSupportFeatureStatus = status
                                }
                                "unlimited_tables"->{
                                    Constants.unlimitedTablesFeatureStatus = status
                                }
                                else->{

                                }
                            }
                            if (feature.getString("feature") == featuresKeys[0] && feature.getInt("status") == 1) {
                                val id = feature.getInt("id")
                                val startDate = feature.getString("start_date")
                                val endDate = feature.getString("end_date")
//                                val type = feature.getString("type")
//                                val currentMiliSeconds = System.currentTimeMillis()
//                                val startMiliSeconds = getTimeStampFromStringDate(startDate)
//                                val endMiliSeconds = getTimeStampFromStringDate(endDate)
                                binding.cfExpiredTextView.text = "${getString(R.string.expired_at_text)} $endDate"

                            } else if (feature.getString("feature") == featuresKeys[1] && feature.getInt(
                                    "status"
                                ) == 1
                            ) {
                                val id = feature.getInt("id")
                                val startDate = feature.getString("start_date")
                                val endDate = feature.getString("end_date")
//                                val type = feature.getString("type")
//                                val currentMiliSeconds = System.currentTimeMillis()
//                                val startMiliSeconds = getTimeStampFromStringDate(startDate)
//                                val endMiliSeconds = getTimeStampFromStringDate(endDate)
                                binding.smsfExpiredTextView.text = "${getString(R.string.expired_at_text)} $endDate"

                            } else if (feature.getString("feature") == featuresKeys[2] && feature.getInt(
                                    "status"
                                ) == 1
                            ) {
                                val id = feature.getInt("id")
                                val startDate = feature.getString("start_date")
                                val endDate = feature.getString("end_date")
//                                val type = feature.getString("type")
//                                val currentMiliSeconds = System.currentTimeMillis()
//                                val startMiliSeconds = getTimeStampFromStringDate(startDate)
//                                val endMiliSeconds = getTimeStampFromStringDate(endDate)
                                binding.psfExpiredTextView.text = "${getString(R.string.expired_at_text)} $endDate"

                            } else if (feature.getString("feature") == featuresKeys[3] && feature.getInt(
                                    "status"
                                ) == 1
                            ) {
                                val id = feature.getInt("id")
                                val startDate = feature.getString("start_date")
                                val endDate = feature.getString("end_date")
//                                val type = feature.getString("type")
//                                val currentMiliSeconds = System.currentTimeMillis()
//                                val startMiliSeconds = getTimeStampFromStringDate(startDate)
//                                val endMiliSeconds = getTimeStampFromStringDate(endDate)
                                binding.utfExpiredTextView.text = "${getString(R.string.expired_at_text)} $endDate"
                            }

                        }
                    }
                }
            }

            override fun onError(error: VolleyError) {
                dismiss()
            }

        })
    }

    private fun calculateDays(createdAt: Long, expiredAtx: Long): Int {
        val diff: Long = expiredAtx - createdAt
        val totalDays = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt()
        val diff1 = System.currentTimeMillis() - createdAt
        val goneDays = TimeUnit.DAYS.convert(diff1, TimeUnit.MILLISECONDS).toInt()
        return totalDays - goneDays
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.apply_coupon_code_btn -> {
                val code = binding.couponCodeInputField.text.toString().trim()
                if (code.isNotEmpty()) {
//                    if (isCouponCodeApplied) {
//                        showAlert(context, getString(R.string.already_applied_coupon_error))
//                    } else {
                    getCouponsAndApply(code)
//                    }

                } else {
                    showAlert(context, getString(R.string.empty_text_error))
                }
            }
            R.id.get_credits_btn -> {
                getCredits()
            }
            R.id.increase_storage_btn -> {
                increaseStorage()
            }
            R.id.extend_usage_btn -> {
                extendUsage()
            }
            R.id.compressor_feature_btn -> {
                featureCreditPrice = 5F
                userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String
                if (userCurrentCredits.isNotEmpty() && (userCurrentCredits != "0" || userCurrentCredits != "0.0") && userCurrentCredits.toFloat() >= featureCreditPrice) {
                    showConfirmAlert(featuresKeys[0], binding.compressorFeatureBtn,binding.cfExpiredTextView.text.toString().split(" ")[2])
                }
                else{
                    showAlert(context,getString(R.string.low_credites_error_message))
                }
            }
            R.id.modes_switcher_feature_btn -> {
                featureCreditPrice = 5F
                userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String
                if (userCurrentCredits.isNotEmpty() && (userCurrentCredits != "0" || userCurrentCredits != "0.0") && userCurrentCredits.toFloat() >= featureCreditPrice) {

                    showConfirmAlert(featuresKeys[1], binding.modesSwitcherFeatureBtn,binding.smsfExpiredTextView.text.toString().split(" ")[2])
                }
                else{
                    showAlert(context,getString(R.string.low_credites_error_message))
                }
            }
            R.id.premium_support_feature_btn -> {
                featureCreditPrice = 7F
                userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String
                if (userCurrentCredits.isNotEmpty() && (userCurrentCredits != "0" || userCurrentCredits != "0.0") && userCurrentCredits.toFloat() >= featureCreditPrice) {

                    showConfirmAlert(featuresKeys[2], binding.premiumSupportFeatureBtn,binding.psfExpiredTextView.text.toString().split(" ")[2])
                }
                else{
                    showAlert(context,getString(R.string.low_credites_error_message))
                }
            }
            R.id.unlimited_tables_feature_btn -> {
                featureCreditPrice = 10F
                userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String
                if (userCurrentCredits.isNotEmpty() && (userCurrentCredits != "0" || userCurrentCredits != "0.0") && userCurrentCredits.toFloat() >= featureCreditPrice) {

                    showConfirmAlert(featuresKeys[3], binding.unlimitedTablesFeatureBtn,binding.utfExpiredTextView.text.toString().split(" ")[2])
                }
                else{
                    showAlert(context,getString(R.string.low_credites_error_message))
                }
            }
//            R.id.images_search_feature_btn -> {
//                featureCreditPrice = Constants.searchImageCreditPrice
//                showConfirmAlert(featuresKeys[4], imagesSearchFeatureBtn)
//            }
            else -> {

            }
        }
    }

    private fun showConfirmAlert(featureName: String, btn: MaterialButton,expiredAt:String) {
        MaterialAlertDialogBuilder(context)
            .setTitle(featureName.toUpperCase(Locale.ENGLISH).replace("_", " "))
            .setMessage(getString(R.string.extend_usage_warning_text))
            .setNegativeButton(getString(R.string.cancel_text)) { dialog, which ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.yes_text)) { dialog, which ->
                dialog.dismiss()
                updateUserFeatureDetails(featureName, btn,expiredAt)
            }
            .create().show()
    }

    private fun updateUserFeatureDetails(featureName: String, btn: MaterialButton,expiredAt:String) {

        val currentMiliSeconds = System.currentTimeMillis()
        val expiredAtMiliSeconds = getTimeStampFromStringDate(expiredAt)
        var newStartDate = ""
        var newExpiredDate = ""
        if (currentMiliSeconds > expiredAtMiliSeconds){
             newStartDate = getDateFromTimeStamp1(currentMiliSeconds)
             newExpiredDate = Constants.getDateFromDays1(30)
        }
        else{
            val remainingTrialDays = Constants.calculateDays(currentMiliSeconds,expiredAtMiliSeconds)
            newStartDate = getDateFromTimeStamp1(currentMiliSeconds)
            newExpiredDate = Constants.getDateFromDays1(30+remainingTrialDays)
        }

        startLoading(context)
        updateUserFeature(context, Constants.firebaseUserId,newStartDate,newExpiredDate, featureName,"update","premium",object : APICallback{
            override fun onSuccess(response: JSONObject) {
                dismiss()
                getUserFeatureDetails()
                userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String
                val hashMap = HashMap<String, Any>()
                val remaining = userCurrentCredits.toFloat() - featureCreditPrice
                Log.d("TEST199", "$remaining")
                hashMap["credits"] = remaining.toString()
                firebaseDatabase.child(Constants.firebaseUserCredits)
                    .child(Constants.firebaseUserId)
                    .updateChildren(hashMap)
                    .addOnSuccessListener {
                        getUserCredit()
                    }
                    .addOnFailureListener {

                    }
            }

            override fun onError(error: VolleyError) {
                dismiss()
            }

        })


//        getUserFeaturesDetails(context, Constants.firebaseUserId, object : APICallback {
//            override fun onSuccess(response: JSONObject) {
//                dismiss()
//                if (response.getInt("status") == 200) {
//                    val features = response.getJSONArray("features")
//                    var tempObject = JSONObject()
//                    var isFound = false
//                    if (features.length() > 0) {
//                        for (i in 0 until features.length()) {
//                            val feature = features.getJSONObject(i)
//                            if (feature.getString("feature")
//                                    .toLowerCase(Locale.ENGLISH) == featureName
//                            ) {
//                                isFound = true
//                                tempObject = feature
//                                break
//                            } else {
//                                isFound = false
//                            }
//                        }
//                        if (isFound) {
//                            val id = tempObject.getInt("id")
//                            val type = tempObject.getString("type")
//                            val start_date = getDateFromTimeStamp1(System.currentTimeMillis())
//                            var end_date = ""
//                            if (type == "trial"){
//                                end_date = Constants.getDateFromDays1(30)
//                            }else if (type == "premium"){
//                                end_date = Constants.getDateFromDays1(30)
//                            }
//                            startLoading(context)
//                            updateUserFeature(context, Constants.firebaseUserId,start_date,end_date, featureName,id,"update","premium",object : APICallback{
//                                override fun onSuccess(response: JSONObject) {
//                                    dismiss()
//                                    getUserFeatureDetails()
//                                    getUserFeaturesDetails1(context,Constants.firebaseUserId)
//                                }
//
//                                override fun onError(error: VolleyError) {
//                                    dismiss()
//                                }
//
//                            })
//                        } else {
//                            startLoading(context)
//                            val start_date = getDateFromTimeStamp1(System.currentTimeMillis())
//                            val end_date = Constants.getDateFromDays1(7)
//                            updateUserFeature(context, Constants.firebaseUserId,start_date,end_date, featureName,0,"new","trial",object : APICallback{
//                                override fun onSuccess(response: JSONObject) {
//                                    dismiss()
//                                    getUserFeatureDetails()
//                                    getUserFeaturesDetails1(context,Constants.firebaseUserId)
//                                }
//
//                                override fun onError(error: VolleyError) {
//                                   dismiss()
//                                }
//
//                            })
//                        }
//                    }
//                    else{
//                       startLoading(context)
//                            val start_date = getDateFromTimeStamp1(System.currentTimeMillis())
//                            val end_date = Constants.getDateFromDays1(7)
//                            updateUserFeature(context, Constants.firebaseUserId,start_date,end_date, featureName,0,"new","trial",object : APICallback{
//                                override fun onSuccess(response: JSONObject) {
//                                    dismiss()
//                                    getUserFeatureDetails()
//                                    getUserFeaturesDetails1(context,Constants.firebaseUserId)
//                                }
//
//                                override fun onError(error: VolleyError) {
//                                   dismiss()
//                                }
//
//                            })
//                    }
//                }
//
//            }
//
//            override fun onError(error: VolleyError) {
//                dismiss()
//            }
//
//        })
    }

    private fun getCredits() {

        val choosePackageLayout = LayoutInflater.from(context).inflate(R.layout.get_credits_dialog_layout, null)

        val oneCreditBtn =
            choosePackageLayout.findViewById<AppCompatButton>(R.id.get_credits_minimum_package_btn)
        val sixCreditBtn =
            choosePackageLayout.findViewById<AppCompatButton>(R.id.get_credits_regular_package_btn)
        val tenCreditBtn =
            choosePackageLayout.findViewById<AppCompatButton>(R.id.get_credits_premium_package_btn)
        val clickEnterCodeView =
            choosePackageLayout.findViewById<MaterialTextView>(R.id.click_enter_coupon_view)
        val couponCodeWrapperLayout =
            choosePackageLayout.findViewById<LinearLayout>(R.id.coupon_code_wrapper_layout)
        val couponCodeInputBox =
            choosePackageLayout.findViewById<TextInputEditText>(R.id.coupon_code_input_field)
        val applyCouponCodeBtn =
            choosePackageLayout.findViewById<MaterialButton>(R.id.apply_coupon_code_btn)

        clickEnterCodeView.setOnClickListener {
            if (couponCodeWrapperLayout.visibility == View.VISIBLE) {
                couponCodeWrapperLayout.visibility = View.GONE
            } else {
                couponCodeWrapperLayout.visibility = View.VISIBLE
            }
        }

//        applyCouponCodeBtn.setOnClickListener {
//            val code = couponCodeInputBox.text.toString().trim()
//            if (code.isNotEmpty()) {
////                if (isCouponCodeApplied) {
////                    showAlert(context, getString(R.string.already_applied_coupon_error))
////                } else {
//                    getCouponsAndApply(code)
////                }
//
//            } else {
//                showAlert(context, getString(R.string.empty_text_error))
//            }
//        }


        val builder = MaterialAlertDialogBuilder(context)
        builder.setCancelable(true)
        builder.setView(choosePackageLayout)
        val alert = builder.create()
        alert.show()

        oneCreditBtn.setOnClickListener {
            productId = "single_credit"
            productIdIndex = 0
            if (Constants.firebaseUserId.isNotEmpty()) {
                alert.dismiss()
                creditsValue = 1F
                userId = Constants.firebaseUserId//auth.currentUser!!.uid
                purchase()

            } else {
                showAlert(context, getString(R.string.user_session_expired))
            }
        }

        sixCreditBtn.setOnClickListener {
            productId = "six_credits"
            productIdIndex = 1
            if (Constants.firebaseUserId.isNotEmpty()) {
                alert.dismiss()
                creditsValue = 6F
                userId = Constants.firebaseUserId//auth.currentUser!!.uid
                purchase()

            } else {
                showAlert(context, getString(R.string.user_session_expired))
            }
        }

        tenCreditBtn.setOnClickListener {
            productId = "ten_credits"
            productIdIndex = 2
            if (Constants.firebaseUserId.isNotEmpty()) {
                alert.dismiss()
                creditsValue = 10F
                userId = Constants.firebaseUserId//auth.currentUser!!.uid
                purchase()

            } else {
                showAlert(context, getString(R.string.user_session_expired))
            }
        }

    }

    private fun getCouponsAndApply(code: String) {

        checkCouponAlreadyUsed(code, object : ResponseListener {
            override fun onSuccess(result: String) {
                if (result == "used") {
                    binding.couponCodeInputField.setText("")
                    showAlert(context, getString(R.string.coupon_already_used_error))
                } else {
                    startLoading(context)
                    val list = mutableListOf<CouponCode>()
                    var isFound = false
                    var tempObject: CouponCode? = null
                    firebaseDatabase.child(Constants.firebaseValidCouponCodes)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    list.clear()
                                    for (postSnapshot in dataSnapshot.children) {
                                        val item =
                                            postSnapshot.getValue(CouponCode::class.java) as CouponCode
                                        list.add(item)
                                    }
                                    dismiss()
                                    if (list.isNotEmpty()) {
                                        for (i in 0 until list.size) {
                                            val codeDetail = list[i]
                                            if (codeDetail.code.equals(code, ignoreCase = true)) {
                                                tempObject = codeDetail
                                                isFound = true
                                                break
                                            } else {
                                                isFound = false
                                            }
                                        }

                                        if (isFound) {
                                            val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                            userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String
                                            if (df.parse(tempObject!!.expired)!!.time > System.currentTimeMillis()) {
                                                couponCodeCredits = tempObject!!.credits
                                                isCouponCodeApplied = true

                                                val hashMap = HashMap<String, String>()
                                                userCurrentCreditsValue += couponCodeCredits
                                                hashMap["credits"] = (userCurrentCreditsValue + userCurrentCredits.toFloat()).toString()

                                                firebaseDatabase.child(Constants.firebaseUserCredits)
                                                    .child(Constants.firebaseUserId)
                                                    .setValue(hashMap)
                                                    .addOnSuccessListener {
                                                        Toast.makeText(
                                                            context,
                                                            getString(R.string.user_credits_update_success_text),
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        updateCouponsHistory(code, tempObject!!)
                                                        binding.couponCodeInputField.setText("")
                                                        getUserCredit()
                                                    }
                                                    .addOnFailureListener {

                                                    }


//                                                showAlert(
//                                                    context,
//                                                    getString(R.string.coupon_code_apply_success_message)
//                                                )
                                            } else {
                                                showAlert(
                                                    context,
                                                    getString(R.string.coupon_expired_error)
                                                )
                                            }

                                        } else {
                                            showAlert(
                                                context,
                                                getString(R.string.coupon_not_found_error)
                                            )
                                        }

                                    }
                                } else {
                                    showAlert(context, getString(R.string.coupon_not_found_error))
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                dismiss()
                                showAlert(context, databaseError.message)
                            }
                        })
                }
            }

        })
    }

    private fun updateCouponsHistory(code: String, tempObject: CouponCode) {
        tempObject.isUsed = 1
        tempObject.user_id = Constants.firebaseUserId
        firebaseDatabase.child(Constants.firebaseUsedCoupons)
            .child(code.lowercase())
            .push()
            .setValue(tempObject)
    }

    private fun checkCouponAlreadyUsed(code: String, listener: ResponseListener) {
        val list = mutableListOf<CouponCode>()
        var isFound = false
        var tempObject: CouponCode? = null

        startLoading(context)
        firebaseDatabase.child(Constants.firebaseUsedCoupons)
            .child(code.lowercase())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        list.clear()
                        for (postSnapshot in dataSnapshot.children) {
                            val item = postSnapshot.getValue(CouponCode::class.java) as CouponCode
                            list.add(item)
                        }
                        dismiss()
                        if (list.isNotEmpty()) {
                            for (i in 0 until list.size) {
                                val codeDetail = list[i]
                                if (codeDetail.code.equals(
                                        code,
                                        ignoreCase = true
                                    ) && codeDetail.user_id == Constants.firebaseUserId
                                ) {
                                    //tempObject = codeDetail
                                    isFound = true
                                    break
                                } else {
                                    isFound = false
                                }
                            }

                            if (isFound) {
                                listener.onSuccess("used")
                            } else {
                                listener.onSuccess("notused")
                            }
                        }
                    } else {
                        listener.onSuccess("notused")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    dismiss()
                    showAlert(context, databaseError.message)
                }
            })
    }

    private fun increaseStorage() {
        val increaseStorageLayout =
            LayoutInflater.from(context).inflate(R.layout.increase_storage_dialog_layout, null)
        val psPurchaseBtn =
            increaseStorageLayout.findViewById<AppCompatButton>(R.id.ps_purchase_package_btn)
        val pspPurchaseBtn =
            increaseStorageLayout.findViewById<AppCompatButton>(R.id.psp_purchase_package_btn)
        val builder = MaterialAlertDialogBuilder(context)
        builder.setCancelable(true)
        builder.setView(increaseStorageLayout)
        val alert = builder.create()
        alert.show()
        psPurchaseBtn.setOnClickListener {
            val feature = Feature(0, 1, 10, 30, 100.0f, 0, 0)
            purchaseFeature(alert, feature)
        }

        pspPurchaseBtn.setOnClickListener {
            val feature = Feature(0, 2, 2, 30, 500.0f, 0, 0)
            purchaseFeature(alert, feature)
        }
    }

    private fun purchaseFeature(alert: AlertDialog, feature: Feature) {
        startLoading(context)
        viewModel.callUserPackageDetail(context, Constants.firebaseUserId)
        viewModel.getUserPackageResponse().observe(this, Observer { response ->
            dismiss()
            if (response != null) {

                MaterialAlertDialogBuilder(context)
                    .setMessage(getString(R.string.purchase_feature_warning_text))
                    .setCancelable(false)
                    .setNegativeButton("No") { dialog, which ->
                        dialog.dismiss()
                    }
                    .setPositiveButton("Yes") { dialog, which ->
                        dialog.dismiss()
                        alert.dismiss()
                        if (response.has("package") && response.isNull("package")) {

                            if (feature.packageId == 3 || feature.packageId == 4) {
                                showAlert(
                                    context,
                                    getString(R.string.purchase_feature_already_active_text)
                                )
                            } else {
                                purchaseFeature(feature)
                            }
                        } else {
                            val packageDetail: JSONObject? = response.getJSONObject("package")
                            upgradeSubscription(feature, packageDetail!!)
                        }

                    }.create().show()
            }
        })
    }

    private fun upgradeSubscription(feature: Feature, packageDetail: JSONObject) {
        val startDate = packageDetail.getString("start_date")
        val endDate = packageDetail.getString("end_date")
        val expiredTimeMili = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(endDate)!!.time

        val diff1 = System.currentTimeMillis() - SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.ENGLISH
        ).parse(startDate)!!.time

        val goneDays = TimeUnit.DAYS.convert(diff1, TimeUnit.MILLISECONDS).toInt()

        val remainingDay = Constants.calculateDays(
            SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(startDate)!!.time,
            SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(endDate)!!.time
        )

        val currentPackageId = packageDetail.getInt("package")


        val availableSize = packageDetail.getDouble("size").toFloat()
        val totalSize = packageDetail.getInt("total_size")

        val currentMiliSeconds = System.currentTimeMillis()
        if ((feature.packageId == 1) && expiredTimeMili >= currentMiliSeconds) {

            var priceCharge: Float = 0F
            var updateDays = 0

            val minimumPackageCredit = totalSize / 100
            val perDayPrice = (minimumPackageCredit.toFloat() / feature.duration).toFloat()
            val packageRemainingBalance = (perDayPrice * remainingDay).toDouble()
            val previousPackageRemainingRoundBalance =
                BigDecimal(packageRemainingBalance).setScale(2, RoundingMode.HALF_EVEN)

            val newPackageCredit = (feature.memory + totalSize) / 100
            val newPackagePricePerDay = newPackageCredit / 30.toFloat()
            val requiredCreditForNewPackage =
                remainingDay * newPackagePricePerDay.toDouble()
            val requiredCreditForNewPackageRoundPrice =
                BigDecimal(requiredCreditForNewPackage).setScale(2, RoundingMode.HALF_EVEN)
            priceCharge =
                (requiredCreditForNewPackageRoundPrice - previousPackageRemainingRoundBalance).toFloat()
            updateDays = remainingDay //+ feature.duration

            val payedPriceCredits = (newPackagePricePerDay * feature.duration).toDouble()
            val priceChargeForExtendDays =
                BigDecimal(payedPriceCredits).setScale(1, RoundingMode.HALF_EVEN).toFloat()

            val totalPriceCharge = priceCharge //+ priceChargeForExtendDays


            userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String
            if (userCurrentCredits.isNotEmpty()) {
                if (userCurrentCredits.toFloat() >= totalPriceCharge) {
                    updateStorageSize(
                        feature,
                        totalPriceCharge,
                        availableSize,
                        totalSize,
                        Constants.getDateFromDays(updateDays)
                    )
                } else {
                    showAlert(
                        context,
                        getString(R.string.low_credites_error_message1)
                    )
                }
            } else {
                if (Constants.firebaseUserId.isNotEmpty()) {

                    val userId = Constants.firebaseUserId//auth.currentUser!!.uid
//                    Constants.firebaseUserId = userId
                    firebaseDatabase.child(Constants.firebaseUserCredits)
                        .child(userId).addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {

                                if (snapshot.hasChildren() && snapshot.hasChild("credits")) {
                                    val previousCredits =
                                        snapshot.child("credits")
                                            .getValue(String::class.java)
                                    userCurrentCreditsValue =
                                        if (previousCredits!!.isNotEmpty()) {
                                            previousCredits.toFloat()
                                        } else {
                                            0F
                                        }
                                }
                                appSettings.putString(
                                    Constants.userCreditsValue,
                                    "$userCurrentCreditsValue"
                                )
                                binding.usTotalCreditsView.text =
                                    "$userCurrentCreditsValue"
                                if (userCurrentCreditsValue >= totalPriceCharge) {
                                    updateStorageSize(
                                        feature,
                                        totalPriceCharge,
                                        availableSize,
                                        totalSize,
                                        Constants.getDateFromDays(updateDays)
                                    )
                                } else {
                                    showAlert(
                                        context,
                                        getString(R.string.low_credites_error_message1)
                                    )
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }

                        })
                }
            }

        } else if (feature.packageId == 2 && expiredTimeMili >= currentMiliSeconds) {

            var priceCharge: Float = 0F
            var updateDays = 0

            val minimumPackageCredit = totalSize.toFloat() / 250
            val perDayPrice = (minimumPackageCredit.toFloat() / feature.duration).toFloat()
            val packageRemainingBalance = (perDayPrice * remainingDay).toDouble()
            val previousPackageRemainingRoundBalance =
                BigDecimal(packageRemainingBalance).setScale(2, RoundingMode.HALF_EVEN)

            val newPackageCredit = (feature.memory + totalSize) / 250
            val newPackagePricePerDay = newPackageCredit / 30.toFloat()
            val requiredCreditForNewPackage =
                remainingDay * newPackagePricePerDay.toDouble()
            val requiredCreditForNewPackageRoundPrice =
                BigDecimal(requiredCreditForNewPackage).setScale(2, RoundingMode.HALF_EVEN)
            priceCharge =
                (requiredCreditForNewPackageRoundPrice - previousPackageRemainingRoundBalance).toFloat()
            updateDays = remainingDay //+ feature.duration

            val payedPriceCredits = (newPackagePricePerDay * feature.duration).toDouble()
            val priceChargeForExtendDays =
                BigDecimal(payedPriceCredits).setScale(1, RoundingMode.HALF_EVEN).toFloat()

            val totalPriceCharge = priceCharge //+ priceChargeForExtendDays

            userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String
            if (userCurrentCredits.isNotEmpty()) {
                if (userCurrentCredits.toFloat() >= totalPriceCharge) {
                    updateStorageSize(
                        feature,
                        totalPriceCharge,
                        availableSize,
                        totalSize,
                        Constants.getDateFromDays(updateDays)
                    )
                } else {
                    showAlert(
                        context,
                        getString(R.string.low_credites_error_message1)
                    )
                }
            } else {
                if (Constants.firebaseUserId.isNotEmpty()) {

                    val userId = Constants.firebaseUserId//auth.currentUser!!.uid
//                    Constants.firebaseUserId = userId
                    firebaseDatabase.child(Constants.firebaseUserCredits)
                        .child(userId).addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {

                                if (snapshot.hasChildren() && snapshot.hasChild("credits")) {
                                    val previousCredits =
                                        snapshot.child("credits")
                                            .getValue(String::class.java)
                                    userCurrentCreditsValue =
                                        if (previousCredits!!.isNotEmpty()) {
                                            previousCredits.toFloat()
                                        } else {
                                            0F
                                        }
                                }
                                appSettings.putString(
                                    Constants.userCreditsValue,
                                    "$userCurrentCreditsValue"
                                )
                                binding.usTotalCreditsView.text =
                                    "$userCurrentCreditsValue"
                                if (userCurrentCreditsValue >= totalPriceCharge) {
                                    updateStorageSize(
                                        feature,
                                        totalPriceCharge,
                                        availableSize,
                                        totalSize,
                                        Constants.getDateFromDays(updateDays)
                                    )
                                } else {
                                    showAlert(
                                        context,
                                        getString(R.string.low_credites_error_message1)
                                    )
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }

                        })
                }
            }
        } else if (feature.packageId == 3) {
            var totalDays = 0
            val minimumPackageCredit = totalSize / 250
            val perDayPrice = (minimumPackageCredit.toFloat() / feature.duration)
            val payedPriceCredits = (perDayPrice * feature.duration).toDouble()
            val priceChargeForExtendDays =
                BigDecimal(payedPriceCredits).setScale(1, RoundingMode.HALF_EVEN).toFloat()
            totalDays = remainingDay + feature.duration
            userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String
            if (userCurrentCredits.isNotEmpty()) {
                if (userCurrentCredits.toFloat() >= priceChargeForExtendDays) {
                    updateExtendUsage(priceChargeForExtendDays, totalDays)
                } else {
                    showAlert(
                        context,
                        getString(R.string.low_credites_error_message1)
                    )
                }
            } else {
                if (Constants.firebaseUserId.isNotEmpty()) {

                    val userId = Constants.firebaseUserId//auth.currentUser!!.uid
                    //Constants.firebaseUserId = userId
                    firebaseDatabase.child(Constants.firebaseUserCredits)
                        .child(userId).addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {

                                if (snapshot.hasChildren() && snapshot.hasChild("credits")) {
                                    val previousCredits =
                                        snapshot.child("credits")
                                            .getValue(String::class.java)
                                    userCurrentCreditsValue =
                                        if (previousCredits!!.isNotEmpty()) {
                                            previousCredits.toFloat()
                                        } else {
                                            0F
                                        }
                                }
                                appSettings.putString(
                                    Constants.userCreditsValue,
                                    "$userCurrentCreditsValue"
                                )
                                binding.usTotalCreditsView.text = "$userCurrentCreditsValue"
                                if (userCurrentCreditsValue >= priceChargeForExtendDays) {
                                    updateExtendUsage(priceChargeForExtendDays, totalDays)
                                } else {
                                    showAlert(
                                        context,
                                        getString(R.string.low_credites_error_message1)
                                    )
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }

                        })
                }
            }
        } else if (feature.packageId == 4) {
            var totalDays = 0
            val minimumPackageCredit = totalSize / 250
            val perDayPrice = (minimumPackageCredit.toFloat() / feature.duration)
            val payedPriceCredits = (perDayPrice * feature.duration).toDouble()
            val priceChargeForExtendDays =
                BigDecimal(payedPriceCredits).setScale(1, RoundingMode.HALF_EVEN).toFloat()
            totalDays = remainingDay + feature.duration

            userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String
            if (userCurrentCredits.isNotEmpty()) {
                if (userCurrentCredits.toFloat() >= priceChargeForExtendDays) {
                    updateExtendUsage(priceChargeForExtendDays, totalDays)
                } else {
                    showAlert(
                        context,
                        getString(R.string.low_credites_error_message1)
                    )
                }
            } else {
                if (Constants.firebaseUserId.isNotEmpty()) {

                    val userId = Constants.firebaseUserId//auth.currentUser!!.uid
                    //Constants.firebaseUserId = userId
                    firebaseDatabase.child(Constants.firebaseUserCredits)
                        .child(userId).addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {

                                if (snapshot.hasChildren() && snapshot.hasChild("credits")) {
                                    val previousCredits =
                                        snapshot.child("credits")
                                            .getValue(String::class.java)
                                    userCurrentCreditsValue =
                                        if (previousCredits!!.isNotEmpty()) {
                                            previousCredits.toFloat()
                                        } else {
                                            0F
                                        }
                                }
                                appSettings.putString(
                                    Constants.userCreditsValue,
                                    "$userCurrentCreditsValue"
                                )
                                binding.usTotalCreditsView.text = "$userCurrentCreditsValue"
                                if (userCurrentCreditsValue >= priceChargeForExtendDays) {
                                    updateExtendUsage(priceChargeForExtendDays, totalDays)
                                } else {
                                    showAlert(
                                        context,
                                        getString(R.string.low_credites_error_message1)
                                    )
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }

                        })
                }
            }

        }

    }

    private fun updateExtendUsage(priceChargeForExtendDays: Float, totalDays: Int) {
        startLoading(context)
        updateUsageTime(
            context,
            Constants.getDateFromDays(totalDays),
            Constants.firebaseUserId,
            object : APICallback {
                override fun onSuccess(response: JSONObject) {
                    dismiss()
                    if (response.getInt("status") == 200) {

                        val hashMap = HashMap<String, String>()
                        val remaining = userCurrentCredits.toFloat() - priceChargeForExtendDays
                        hashMap["credits"] = remaining.toString()
                        firebaseDatabase.child(Constants.firebaseUserCredits)
                            .child(Constants.firebaseUserId)
                            .setValue(hashMap)
                            .addOnSuccessListener {
                                getUserCredit()
                            }
                            .addOnFailureListener {

                            }
                        getUserSubscriptionDetails()
                    } else {
                        val message = response.getString("message")
                        showAlert(context, message)
                    }
                }

                override fun onError(error: VolleyError) {
                    dismiss()
                    Log.d("TEST199", "ERROR: ${error.localizedMessage}")
                }

            })
    }

    private fun updateStorageSize(
        feature: Feature,
        priceCharge: Float,
        availableSize: Float,
        totalSize: Int,
        endDate: String
    ) {
        var updatedStorageSize = 0F
        var updatedTotalSize = 0

        updatedStorageSize = availableSize + feature.memory
        updatedTotalSize = totalSize + feature.memory.toInt()

        startLoading(context)
        updateMemorySize(
            context,
            updatedStorageSize.toString(),
            updatedTotalSize,
            Constants.firebaseUserId,
            1,
            endDate,
            object : APICallback {
                override fun onSuccess(response: JSONObject) {
                    dismiss()
                    if (response.getInt("status") == 200) {

                        val hashMap = HashMap<String, Any>()
                        val remaining = userCurrentCredits.toFloat() - priceCharge
                        Log.d("TEST199", "$remaining")
                        hashMap["credits"] = remaining.toString()
                        firebaseDatabase.child(Constants.firebaseUserCredits)
                            .child(Constants.firebaseUserId)
                            .updateChildren(hashMap)
                            .addOnSuccessListener {
                                getUserCredit()
                            }
                            .addOnFailureListener {

                            }
                        getUserSubscriptionDetails()
                    } else {
                        val message = response.getString("message")
                        showAlert(context, message)
                    }
                }

                override fun onError(error: VolleyError) {
                    dismiss()
                }

            })

    }

    private fun extendUsage() {
        val extendUsageLayout =
            LayoutInflater.from(context).inflate(R.layout.extend_usage_dialog_layout, null)
        val utPurchaseBtn =
            extendUsageLayout.findViewById<AppCompatButton>(R.id.ut_purchase_package_btn)
        val utpPurchaseBtn =
            extendUsageLayout.findViewById<AppCompatButton>(R.id.utp_purchase_package_btn)
        val builder = MaterialAlertDialogBuilder(context)
        builder.setCancelable(true)
        builder.setView(extendUsageLayout)
        val alert = builder.create()
        alert.show()
        utPurchaseBtn.setOnClickListener {
            val feature = Feature(0, 3, 1, 30, 0.0f, 0, 0)
            purchaseFeature(alert, feature)
        }

        utpPurchaseBtn.setOnClickListener {
            val feature = Feature(0, 4, 2, 60, 0.0f, 0, 0)
            purchaseFeature(alert, feature)
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
//if item newly purchased

        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            val purchase: Purchase = purchases[0]
            if (Constants.firebaseUserId.isNotEmpty()) {
                val userId = Constants.firebaseUserId//auth.currentUser!!.uid
                purchaseDetail =
                    PurchaseDetail(
                        userId,
                        purchase.orderId,
                        purchase.packageName,
                        productId,
                        purchase.purchaseTime,
                        purchase.purchaseToken
                    )
                CoroutineScope(Dispatchers.IO).launch {
                    val token = purchase.purchaseToken
                    val consumeParams = ConsumeParams.newBuilder().setPurchaseToken(token).build()
                    billingClient!!.consumeAsync(consumeParams, consumeListener)
                }

                verifyPurchase()
            } else {
                showAlert(context, getString(R.string.user_session_expired))
            }
            //handlePurchases(purchases)
        }
        //if item already purchased then check and reflect changes
//        else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
//            billingClient!!.queryPurchasesAsync(INAPP) { billingResults, list ->
//                handlePurchases(list)
//            }
////            val queryAlreadyPurchasesResult = billingClient!!.queryPurchases(INAPP)
////            val alreadyPurchases: List<Purchase>? = queryAlreadyPurchasesResult.purchasesList
////            if (alreadyPurchases != null) {
////                handlePurchases(alreadyPurchases)
////            }
//        }
        //if purchase cancelled
        else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Toast.makeText(applicationContext, "Purchase Canceled", Toast.LENGTH_SHORT).show()
        }
        // Handle any other error msgs
        else {
//            Toast.makeText(
//                applicationContext,
//                "Error " + billingResult.debugMessage,
//                Toast.LENGTH_SHORT
//            ).show()

        }
    }

    var consumeListener =
        ConsumeResponseListener { billingResult, purchaseToken ->
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(
                    context,
                    getString(R.string.consumer_product_success_text),
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

    private fun verifyPurchase() {
        startLoading(context)
        viewModel.callPurchase(
            context,
            context.packageName,
            productId,
            purchaseDetail!!.purchaseToken
        )
        viewModel.getPurchaseResponse().observe(this) { response ->
            dismiss()
            if (response != null) {
                when (response.get("purchaseState").asInt) {
                    0 -> {
                        firebaseDatabase.child(Constants.firebasePurchaseHistory).push()
                            .setValue(purchaseDetail)
                        val hashMap = HashMap<String, String>()
//                        if (isCouponCodeApplied && couponCodeCredits != 0) {
//                            creditsValue += couponCodeCredits
//                            creditsValue += userCurrentCreditsValue
//                            hashMap["credits"] = creditsValue.toString()
//                        } else {
                        creditsValue += userCurrentCreditsValue
                        hashMap["credits"] = creditsValue.toString()
//                        }

                        firebaseDatabase.child(Constants.firebaseUserCredits)
                            .child(userId)
                            .setValue(hashMap)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    context,
                                    getString(R.string.user_credits_update_success_text),
                                    Toast.LENGTH_SHORT
                                ).show()
                                isCouponCodeApplied = false
                                couponCodeCredits = 0
                                getUserCredit()
                                Log.d("TEST199", "$userCurrentCreditsValue")
                            }
                            .addOnFailureListener {

                            }
                    }
                    else -> {
                        showRetryDialogBox(response.get("error").asString)
                    }
                }
            } else {
                showRetryDialogBox(getString(R.string.something_wrong_error))
            }
        }

    }


    private fun showRetryDialogBox(message: String) {
        MaterialAlertDialogBuilder(context).setCancelable(false)
            .setMessage(message)
            .setNegativeButton(getString(R.string.cancel_text)) { dialog, which ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.retry_text)) { dialog, which ->
                dialog.dismiss()
                verifyPurchase()
            }
            .create().show()
    }

    private fun purchase() {
        if (billingClient!!.isReady) {
            initiatePurchase()
        }
        //else reconnect service
        else {
            billingClient =
                BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build()
            billingClient!!.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        initiatePurchase()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Error " + billingResult.debugMessage,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onBillingServiceDisconnected() {}
            })
        }
    }

    private fun initiatePurchase() {
        val skuList: MutableList<String> = ArrayList()
        skuList.add("single_credit")
        skuList.add("six_credits")
        skuList.add("ten_credits")
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)

        billingClient!!.querySkuDetailsAsync(params.build())
        { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                if (skuDetailsList != null && skuDetailsList.size > 0) {
                    val flowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(skuDetailsList[productIdIndex])
                        .build()
                    billingClient!!.launchBillingFlow(this, flowParams)
                } else {
                    //try to add item/product id "purchase" inside managed product in google play console

                    Toast.makeText(
                        applicationContext,
                        "Purchase Item not Found",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    applicationContext,
                    " Error " + billingResult.debugMessage, Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    var ackPurchase = AcknowledgePurchaseResponseListener { billingResult ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {

            //if purchase is acknowledged
            // Grant entitlement to the user. and restart activity
//            savePurchaseValueToPref(true)
//            Toast.makeText(applicationContext, "Item Purchased", Toast.LENGTH_SHORT).show()
//            recreate()
        }
    }

    private fun verifyValidSignature(signedData: String, signature: String): Boolean {
        return try {
            // To get key go to Developer Console > Select your app > Development Tools > Services & APIs.

            Security.verifyPurchase(Constants.licenseKey, signedData, signature)
        } catch (e: IOException) {
            false
        }
    }


    private fun purchaseFeature(feature: Feature) {
        userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String
        startLoading(context)
        if (userCurrentCredits.isNotEmpty()) {
            if (userCurrentCredits.toFloat() >= feature.credit_price) {

                if (Constants.firebaseUserId.isNotEmpty()) {
                    val userId = Constants.firebaseUserId//auth.currentUser!!.uid

                    purchaseFeatures(context, feature, userId, object : APICallback {
                        override fun onSuccess(response: JSONObject) {
                            dismiss()
                            if (response.getInt("status") == 200) {

                                val hashMap = HashMap<String, String>()
                                val remaining =
                                    userCurrentCredits.toFloat() - feature.credit_price
                                hashMap["credits"] = remaining.toString()
                                firebaseDatabase.child(Constants.firebaseUserCredits)
                                    .child(userId)
                                    .setValue(hashMap)
                                    .addOnSuccessListener {

                                    }
                                    .addOnFailureListener {

                                    }

                                showAlert(context, getString(R.string.purchase_success_message))
                            } else {
                                val message = response.getString("message")
                                showAlert(context, message)
                            }
                        }

                        override fun onError(error: VolleyError) {
                            dismiss()
                        }

                    })
                }
            } else {
                dismiss()
                showAlert(context, getString(R.string.low_credites_error_message1))
            }
        }

    }

}