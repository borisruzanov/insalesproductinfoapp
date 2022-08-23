package com.mywebsite.insalesproductinfoapp.view.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.mywebsite.insalesproductinfoapp.R
import com.mywebsite.insalesproductinfoapp.databinding.ActivityLoginBinding
import com.mywebsite.insalesproductinfoapp.model.User
import com.mywebsite.insalesproductinfoapp.utils.AppSettings
import com.mywebsite.insalesproductinfoapp.utils.Constants
import com.mywebsite.insalesproductinfoapp.viewmodel.LoginViewModel
import com.mywebsite.insalesproductinfoapp.viewmodelfactory.LoginViewModelFactory
import java.math.RoundingMode
import java.util.regex.Pattern

class LoginActivity : BaseActivity() {

    private lateinit var context: Context
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var appSettings:AppSettings
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private var user: User? = null
    private lateinit var firebaseDatabase: DatabaseReference
    var userCurrentCreditsValue: Float = 0F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        viewModel = ViewModelProvider(
            this,LoginViewModelFactory(this)
        )[LoginViewModel::class.java]
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                viewModel.isLoading.value
            }
        }

        viewModel.isLogged.observe(this) { _isLogged ->
            if (_isLogged) {
                startActivity(Intent(context, MainActivity::class.java)).apply {
                    finish()
                }
            }
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

         initViews()

    }

    private fun initViews(){
       appSettings  = AppSettings(context)
        firebaseDatabase = FirebaseDatabase.getInstance().reference
        auth = Firebase.auth

        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
//            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
//            .requestScopes(Scope(SheetsScopes.SPREADSHEETS))
//            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, signInOptions)

        val acct: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(context)

        if (acct != null) {
            binding.googleLoginWrapperLayout.visibility = View.GONE
            binding.insalesLoginWrapperLayout.visibility = View.VISIBLE
        }

        binding.insalesLoginBtn.setOnClickListener {
            if (validation()) {
                val shopName = binding.insalesLoginShopNameBox.text.toString().trim()
                val email = binding.insalesLoginEmailBox.text.toString().trim()
                val password = binding.insalesLoginPasswordBox.text.toString().trim()
                inSalesLogin(shopName, email, password)
            }

        }

        binding.googleLoginBtn.setOnClickListener {
            startLogin()
        }

    }

    private fun startLogin() {
        val signInIntent = mGoogleSignInClient.signInIntent
        googleLauncher.launch(signInIntent)
    }

    // THIS GOOGLE LAUNCHER WILL HANDLE RESULT
    private var googleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {

                GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    .addOnSuccessListener { googleSignInAccount ->
                        firebaseAuthWithGoogle(googleSignInAccount!!.idToken!!)
                        saveUserUpdatedDetail(googleSignInAccount, "new")
                    }.addOnFailureListener { p0 ->
                        showAlert(
                            context,
                            p0.localizedMessage!!
                        )
                    }
            }
        }

    private fun saveUserUpdatedDetail(acct: GoogleSignInAccount?, isLastSignUser: String) {
        try {

            // IF PART WILL RUN IF USER LOGGED AND ACCOUNT DETAIL NOT EMPTY
            if (acct != null && acct.displayName.isNullOrEmpty()) {
                startLogin()
            } else if (acct != null) {
                val personName = acct.displayName
                val personGivenName = acct.givenName
                val personFamilyName = acct.familyName
                val personEmail = acct.email
                val personId = acct.id
                val personPhoto: Uri? = acct.photoUrl
                val user = User(
                    personName!!,
                    personGivenName!!,
                    personFamilyName!!,
                    personEmail!!,
                    personId!!,
                    personPhoto!!.toString()
                )
                appSettings.putUser(Constants.user, user)
                Constants.userData = user
                this.user = user
                if (isLastSignUser == "new") {
                    appSettings.putBoolean(Constants.isLogin, true)
                }
            }
        } catch (e: Exception) {

        }

    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    add50CreditsFree()
                }
            }
    }

    @SuppressLint("HardwareIds")
    private fun add50CreditsFree() {
        startLoading(context)
        val user = Firebase.auth.currentUser
        var freeCreditsValue = ""
        if (user != null) {
            val id = user.uid as String
            Constants.firebaseUserId = id
            val email = user.email.toString()
            val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

            val usedIdReference = firebaseDatabase.child("USERS_DEVICES_EMAILS")
            val params = HashMap<String, Any>()
            params["email"] = email
            params["deviceId"] = deviceId

            firebaseDatabase.child(Constants.firebaseFreeCredits).addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    freeCreditsValue = snapshot.child("value").getValue(String::class.java) as String
                    Log.d("TEST199FREECREDITS",freeCreditsValue)

                    usedIdReference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.hasChildren()) {
                                var isFound = false

                                for (item: DataSnapshot in snapshot.children) {
                                    if (item.child("deviceId").getValue(String::class.java) == deviceId &&
                                        item.child("email").getValue(String::class.java) == email) {
                                        isFound = true
                                        break
                                    }
                                }

                                if (!isFound) {
                                    firebaseDatabase.child(Constants.firebaseUserCredits)
                                        .child(id).addListenerForSingleValueEvent(object :
                                            ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {

                                                if (snapshot.hasChildren() && snapshot.hasChild("credits")) {
                                                    val previousCredits =
                                                        snapshot.child("credits").getValue(String::class.java)
                                                    userCurrentCreditsValue =
                                                        if (previousCredits!!.isNotEmpty()) {
                                                            previousCredits.toFloat()
                                                        } else {
                                                            0F
                                                        }
                                                }

                                                val roundedCreditValues =
                                                    userCurrentCreditsValue.toBigDecimal()
                                                        .setScale(2, RoundingMode.UP)
                                                        .toDouble()
                                                val totalCredits = roundedCreditValues + freeCreditsValue.toInt()
                                                val hashMap = HashMap<String, Any>()

                                                hashMap["credits"] = totalCredits.toString()
                                                firebaseDatabase.child(Constants.firebaseUserCredits)
                                                    .child(id)
                                                    .updateChildren(hashMap)
                                                    .addOnSuccessListener {
                                                        usedIdReference.push().setValue(params)
                                                        moveNext()
                                                    }
                                                    .addOnFailureListener {
                                                        moveNext()
                                                    }
                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                                moveNext()
                                            }

                                        })
                                }
                                else {
                                    moveNext()
                                }
                            } else {

                                firebaseDatabase.child(Constants.firebaseUserCredits)
                                    .child(id).addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {

                                            if (snapshot.hasChildren() && snapshot.hasChild("credits")) {
                                                val previousCredits =
                                                    snapshot.child("credits").getValue(String::class.java)
                                                userCurrentCreditsValue =
                                                    if (previousCredits!!.isNotEmpty()) {
                                                        previousCredits.toFloat()
                                                    } else {
                                                        0F
                                                    }
                                            }

                                            val roundedCreditValues =
                                                userCurrentCreditsValue.toBigDecimal()
                                                    .setScale(2, RoundingMode.UP)
                                                    .toDouble()
                                            val totalCredits = roundedCreditValues + freeCreditsValue.toInt()
                                            val hashMap = HashMap<String, Any>()

                                            hashMap["credits"] = totalCredits.toString()
                                            firebaseDatabase.child(Constants.firebaseUserCredits)
                                                .child(id)
                                                .updateChildren(hashMap)
                                                .addOnSuccessListener {
                                                    usedIdReference.push().setValue(params)
                                                    moveNext()
                                                }
                                                .addOnFailureListener {
//                                            moveNext()
                                                }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            moveNext()
                                        }

                                    })
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
//                    moveNext()
                        }

                    })

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }
        else{
            moveNext()
        }
    }

    private fun moveNext(){
        dismiss()
        binding.googleLoginWrapperLayout.visibility = View.GONE
        binding.insalesLoginWrapperLayout.visibility = View.VISIBLE
//        Handler(Looper.myLooper()!!).postDelayed({
//            dismiss()
//            startActivity(Intent(context,MainActivity::class.java)).apply {
//                finish()
//            }
//        },2000)

    }

    private fun validation(): Boolean {
        if (binding.insalesLoginShopNameBox.text.toString().trim().isEmpty()) {
            showAlert(context, getString(R.string.empty_text_error))
            return false
        } else if (binding.insalesLoginEmailBox.text.toString().trim().isEmpty()) {
            showAlert(context, getString(R.string.empty_text_error))
            return false
        } else if (!Pattern.compile(Constants.emailPattern)
                .matcher(binding.insalesLoginEmailBox.text.toString().trim())
                .matches()
        ) {
            showAlert(context, getString(R.string.email_valid_error))
            return false
        } else if (binding.insalesLoginPasswordBox.text.toString().trim().isEmpty()) {
            showAlert(context, getString(R.string.empty_text_error))
            return false
        }
        return true
    }

    private fun inSalesLogin(shopName: String, email: String, password: String) {

        startLoading(context, getString(R.string.please_wait_login_message))
        viewModel.callSalesAccount(context, shopName, email, password)
        viewModel.getSalesAccountResponse().observe(this, Observer { response ->
            dismiss()
            if (response != null) {
                if (response.get("status").asString == "200") {
                    appSettings.putString("INSALES_STATUS", "logged")
                    appSettings.putString("INSALES_SHOP_NAME", shopName)
                    appSettings.putString("INSALES_EMAIL", email)
                    appSettings.putString("INSALES_PASSWORD", password)
                    startActivity(Intent(context,MainActivity::class.java)).apply {
                        finish()
                    }
                } else {
                    showAlert(context, response.get("message").asString)
                }
            }
        })
    }
}