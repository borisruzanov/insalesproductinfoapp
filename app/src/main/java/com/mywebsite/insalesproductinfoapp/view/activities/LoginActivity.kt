package com.mywebsite.insalesproductinfoapp.view.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mywebsite.insalesproductinfoapp.R
import com.mywebsite.insalesproductinfoapp.databinding.ActivityLoginBinding
import com.mywebsite.insalesproductinfoapp.utils.AppSettings
import com.mywebsite.insalesproductinfoapp.utils.Constants
import com.mywebsite.insalesproductinfoapp.viewmodel.LoginViewModel
import com.mywebsite.insalesproductinfoapp.viewmodelfactory.LoginViewModelFactory
import java.util.regex.Pattern

class LoginActivity : BaseActivity() {

    private lateinit var context: Context
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var appSettings:AppSettings

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

        binding.insalesLoginBtn.setOnClickListener {

            if (validation()) {
                val shopName = binding.insalesLoginShopNameBox.text.toString().trim()
                val email = binding.insalesLoginEmailBox.text.toString().trim()
                val password = binding.insalesLoginPasswordBox.text.toString().trim()
                inSalesLogin(shopName, email, password)
            }

        }

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