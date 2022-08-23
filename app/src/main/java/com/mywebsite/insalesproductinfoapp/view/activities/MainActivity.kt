package com.mywebsite.insalesproductinfoapp.view.activities

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.android.volley.VolleyError
import com.boris.expert.csvmagic.model.ProductImages
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.gson.JsonObject
import com.mywebsite.insalesproductinfoapp.R
import com.mywebsite.insalesproductinfoapp.adapters.BarcodeImageAdapter
import com.mywebsite.insalesproductinfoapp.adapters.InSalesProductsAdapter
import com.mywebsite.insalesproductinfoapp.adapters.InternetImageAdapter
import com.mywebsite.insalesproductinfoapp.databinding.ActivityMainBinding
import com.mywebsite.insalesproductinfoapp.databinding.InsalesProductImageUpdateDialogBinding
import com.mywebsite.insalesproductinfoapp.databinding.InsalesProductSearchDialogBinding
import com.mywebsite.insalesproductinfoapp.databinding.InternetImageSearchDialogLayoutBinding
import com.mywebsite.insalesproductinfoapp.interfaces.APICallback
import com.mywebsite.insalesproductinfoapp.interfaces.GrammarCallback
import com.mywebsite.insalesproductinfoapp.interfaces.ResponseListener
import com.mywebsite.insalesproductinfoapp.model.Category
import com.mywebsite.insalesproductinfoapp.model.Product
import com.mywebsite.insalesproductinfoapp.retrofit.ApiServices
import com.mywebsite.insalesproductinfoapp.retrofit.RetrofitClientApi
import com.mywebsite.insalesproductinfoapp.utils.*
import com.mywebsite.insalesproductinfoapp.view.fragments.AddProductCustomDialog
import com.mywebsite.insalesproductinfoapp.view.fragments.CustomDialog
import com.mywebsite.insalesproductinfoapp.view.fragments.FullImageFragment
import com.mywebsite.insalesproductinfoapp.viewmodel.MainActivityViewModel
import com.mywebsite.insalesproductinfoapp.viewmodel.SalesCustomersViewModel
import io.paperdb.Paper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.expandable.ExpandableTextView
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.RoundingMode
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : BaseActivity(), InSalesProductsAdapter.OnItemClickListener, NavigationView.OnNavigationItemSelectedListener {

    private lateinit var context: Context
    private lateinit var binding: ActivityMainBinding
    private lateinit var appSettings: AppSettings
    private var menu: Menu? = null
    private lateinit var productAdapter: InSalesProductsAdapter
    private var productViewListType = 0
    private var productsList = mutableListOf<Product>()
    //    private var originalProductsList = mutableListOf<Product>()
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private var dialogStatus = 0
    private var currentPage = 1
    private var currentTotalProducts = 0
    private var email = ""
    private var password = ""
    private var shopName = ""
    var userCurrentCreditsValue: Float = 0F
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDatabase: DatabaseReference
    private var productImagesChanges = false
    private var selectedImageBase64String: String = ""
    private var voiceLanguageCode = "en"
    var searchedImagesList = mutableListOf<String>()
    private var currentPhotoPath: String? = null
    private lateinit var searchDialog: InsalesProductSearchDialogBinding
    private lateinit var internetSearchBinding: InternetImageSearchDialogLayoutBinding
    private lateinit var insalesUpdateProductImageLayout: InsalesProductImageUpdateDialogBinding
    private lateinit var internetImageAdapter: InternetImageAdapter
    private var userCurrentCredits = ""
    var voiceSearchHint = "default"
    var barcodeSearchHint = "default"
    private var originalCategoriesList = mutableListOf<Category>()
    private var categoriesList = mutableListOf<Category>()
    private lateinit var salesViewModel: SalesCustomersViewModel
    private var intentType = 0
    private var barcodeImageList = mutableListOf<String>()
    var multiImagesList = mutableListOf<String>()
    private var adapter: BarcodeImageAdapter? = null
    private var characters = 0
    private var grammarPrice = 0F
    private var unitCharacterPrice = 0F
    private var howMuchChargeCredits = 0F
    private var selectedInternetImage = ""

    companion object {
        var originalProductsList = mutableListOf<Product>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setUpToolbar()
        getUserCredit()
    }

    private fun initViews() {
        context = this
        appSettings = AppSettings(context)
        auth = Firebase.auth
        firebaseDatabase = FirebaseDatabase.getInstance().reference
        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]
        salesViewModel = ViewModelProvider(this)[SalesCustomersViewModel::class.java]

        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, signInOptions)

        val acct: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(context)

        shopName = appSettings.getString("INSALES_SHOP_NAME") as String
        email = appSettings.getString("INSALES_EMAIL") as String
        password = appSettings.getString("INSALES_PASSWORD") as String

        productViewListType = appSettings.getInt("PRODUCT_VIEW_LIST_TYPE")
        binding.insalesProductsRecyclerview.layoutManager = WrapContentLinearLayoutManager(
            context,
            RecyclerView.VERTICAL,
            false
        )
        binding.insalesProductsRecyclerview.hasFixedSize()
        productAdapter = InSalesProductsAdapter(
            context, productsList, productViewListType
        )
        binding.insalesProductsRecyclerview.isNestedScrollingEnabled = false
        binding.insalesProductsRecyclerview.adapter = productAdapter
        productAdapter.setOnItemClickListener(this)

        val cacheList: ArrayList<Product>? = Paper.book().read(Constants.cacheProducts)
        if (Constants.listUpdateFlag == 0) {
            if (cacheList != null && cacheList.size > 0) {
                originalProductsList.clear()
                productsList.clear()
                originalProductsList.addAll(cacheList)
                originalProductsList.sortByDescending { it.id }
                productsList.addAll(originalProductsList)
                productAdapter.notifyItemRangeChanged(0, productsList.size)
            } else {
                dialogStatus = 1
                fetchProducts(currentPage)
            }
        }
        Constants.listUpdateFlag = 0

        binding.insalesProductsFilterBtn.setOnClickListener {

            if (originalCategoriesList.size == 0) {
                viewModel.callCategories(context, shopName, email, password)
                viewModel.getCategoriesResponse().observe(this) { response ->
                    if (response != null) {
                        if (response.get("status").asString == "200") {
                            val categories = response.get("categories").asJsonArray
                            if (categories.size() > 0) {
                                if (categoriesList.isNotEmpty()) {
                                    categoriesList.clear()
                                }
                                for (i in 0 until categories.size()) {
                                    val category = categories[i].asJsonObject
                                    originalCategoriesList.add(
                                        Category(
                                            category.get("title").asString,
                                            category.get("id").asInt
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                if (categoriesList.isNotEmpty()) {
                    categoriesList.clear()
                }
                categoriesList.addAll(originalCategoriesList)
            }

            val builder = MaterialAlertDialogBuilder(context)
            builder.setCancelable(false)
            builder.setTitle(getString(R.string.sorting_heading_text))
            builder.setNegativeButton(getString(R.string.cancel_text)) { dialog, which ->
                dialog.dismiss()
            }

            val arrayAdapter = ArrayAdapter(
                context,
                android.R.layout.select_dialog_item,
                getSortingList(context)
            )
            builder.setAdapter(
                arrayAdapter
            ) { dialog, which ->
                dialog!!.dismiss()
                if (which == 0) {
                    resetProductList()
                } else if (which == 3) {
                    displayCategoryFilterDialog(categoriesList)
                } else if (which == 4) {
                    displayErrorItems()
                } else {
                    sorting(which)
                }
            }
            val alert = builder.create()
            alert.show()

//                val query = searchBox.text.toString().trim()
//                if (query.isNotEmpty()) {
//                    BaseActivity.hideSoftKeyboard(requireActivity(), searchBox)
//                    searchBox.clearFocus()
//                    requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
//                    search(query, "default")
//                } else {
//                    BaseActivity.showAlert(requireActivity(), getString(R.string.empty_text_error))
//                }

        }
        binding.insalesProductsSearch.setOnClickListener {
            openSearchDialog()
        }

        binding.insalesProductsSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isEmpty()) {
                    resetProductList()
                }
            }

        })

        binding.fab.setOnClickListener {
            addProduct()
        }

        binding.privacyPolicyView.setOnClickListener {
            binding.drawer.closeDrawer(GravityCompat.START)
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://qrmagicapp.com/privacy-policy-CSV/")
            )
            startActivity(browserIntent)
        }

    }

    override fun onResume() {
        super.onResume()

        getSearchImageDetail()
        checkAndStartTrialPeriod()
        getPrices()
        val intentsFilter = IntentFilter()
        intentsFilter.addAction("update-products")
        intentsFilter.addAction("update-images")
        LocalBroadcastManager.getInstance(
            context
        ).registerReceiver(broadcastReceiver, intentsFilter)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver)
    }

    private fun openSearchDialog() {
        searchDialog = InsalesProductSearchDialogBinding.inflate(layoutInflater)//LayoutInflater.from(requireActivity()).inflate(R.layout.insales_product_search_dialog, null)
        val builder = MaterialAlertDialogBuilder(context)
        builder.setView(searchDialog.root)
        builder.setCancelable(false)
        val alert = builder.create()
        alert.show()

        //val dialogCloseBtn = searchDialog.findViewById<AppCompatImageView>(R.id.search_product_dialog_close)
        searchDialog.searchProductDialogClose.setOnClickListener {
            alert.dismiss()
        }

        searchDialog.insalesProductsSearchBtn.setOnClickListener {
            val query = searchDialog.insalesProductsSearchBox.text.toString().trim()
            if (query.isNotEmpty()) {
                hideSoftKeyboard(context, searchDialog.insalesProductsSearchBox)
                searchDialog.insalesProductsSearchBox.clearFocus()
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
                search(query, "default")
                binding.insalesProductsSearch.setText(query)
                alert.dismiss()
            } else {
                showAlert(context, getString(R.string.empty_text_error))
            }

        }

        searchDialog.insalesProductsSearchResetBtn.setOnClickListener {
            hideSoftKeyboard(context, searchDialog.insalesProductsSearchBox)
            searchDialog.insalesProductsSearchBox.clearFocus()
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            if (searchDialog.insalesProductsSearchBox.text.toString().trim().isNotEmpty()) {
                searchDialog.insalesProductsSearchBox.setText("")
            }
            resetProductList()
            alert.dismiss()
        }

        searchDialog.voiceSearchFragmentInsales.setOnClickListener {
            voiceSearchHint = "voice_mode"
            voiceLanguageCode = appSettings.getString("VOICE_LANGUAGE_CODE") as String
            val voiceLayout = LayoutInflater.from(context).inflate(
                R.layout.voice_language_setting_layout,
                null
            )
            val voiceLanguageSpinner =
                voiceLayout.findViewById<AppCompatSpinner>(R.id.voice_language_spinner)
            val voiceLanguageSaveBtn =
                voiceLayout.findViewById<MaterialButton>(R.id.voice_language_save_btn)

            if (voiceLanguageCode == "en" || voiceLanguageCode.isEmpty()) {
                voiceLanguageSpinner.setSelection(0, false)
            } else {
                voiceLanguageSpinner.setSelection(1, false)
            }

            voiceLanguageSpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        voiceLanguageCode =
                            if (parent!!.selectedItem.toString().toLowerCase(
                                    Locale.ENGLISH
                                ).contains("english")
                            ) {
                                "en"
                            } else {
                                "ru"
                            }
                        appSettings.putString("VOICE_LANGUAGE_CODE", voiceLanguageCode)

                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }

                }
            val builder = MaterialAlertDialogBuilder(context)
            builder.setView(voiceLayout)
            val alert = builder.create();
            alert.show()

            voiceLanguageSaveBtn.setOnClickListener {
                alert.dismiss()
                Constants.listUpdateFlag = 1
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, voiceLanguageCode)

                }
                voiceResultLauncher.launch(intent)
            }
        }

        if (binding.insalesProductsSearch.text.toString().isNotEmpty()) {
            searchDialog.insalesProductsSearchBox.setText(binding.insalesProductsSearch.text.toString())
            searchDialog.insalesProductsSearchBox.setSelection(searchDialog.insalesProductsSearchBox.text.toString().length)
            showSoftKeyboard(context, searchDialog.insalesProductsSearchBox)
        }

        searchDialog.barcodeImgFragmentInsales.setOnClickListener {
            barcodeSearchHint = "default"
            Constants.listUpdateFlag = 1
            val intent = Intent(context, BarcodeReaderActivity::class.java)
            barcodeImageResultLauncher.launch(intent)
        }

        searchDialog.insalesProductsSearchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                if (s.toString().isEmpty()) {
//                    resetProductList()
//                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        searchDialog.insalesProductsSearchBox.setOnEditorActionListener { v, actionId, event ->
            val query = searchDialog.insalesProductsSearchBox.text.toString()
            if (query.isNotEmpty()) {
                hideSoftKeyboard(context, searchDialog.insalesProductsSearchBox)
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
                search(query, "default")
                binding.insalesProductsSearch.setText(query)
                alert.dismiss()
            }

            false
        }
    }

    private var barcodeImageResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // THIS LINE OF CODE WILL CHECK THE IMAGE HAS BEEN SELECTED OR NOT
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data != null && result.data!!.hasExtra("SCANNED_BARCODE_VALUE")) {
                    val barcodeId = result.data!!.getStringExtra("SCANNED_BARCODE_VALUE") as String
                    if (barcodeId.isNotEmpty()) {
                        if (barcodeSearchHint == "default") {
                            searchDialog.insalesProductsSearchBox.setText(barcodeId)
                            binding.insalesProductsSearch.setText(barcodeId)
                            search(barcodeId, "sku")
                        } else {
                            internetSearchBinding.textInputField.setText(barcodeId)
                            Constants.hideKeyboar(context)
                            startSearch(
                                internetSearchBinding.textInputField,
                                internetSearchBinding.internetImageSearchBtn,
                                internetSearchBinding.imageLoaderView,
                                searchedImagesList as java.util.ArrayList<String>,
                                internetImageAdapter
                            )
                        }

                    }
                }


            } else {
                Constants.listUpdateFlag = 0
            }
        }

    private fun startSearch(
        searchBoxView: TextInputEditText,
        searchBtnView: ImageButton,
        loader: ProgressBar,
        searchedImagesList: java.util.ArrayList<String>,
        internetImageAdapter: InternetImageAdapter
    ) {
        var creditChargePrice: Float = 0F
        if (searchBoxView.text.toString().trim().isNotEmpty()) {

            val firebaseDatabase = FirebaseDatabase.getInstance().reference
            firebaseDatabase.child("SearchImagesLimit")
                .addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val creditPrice = snapshot.child("credits")
                            .getValue(Int::class.java) as Int
                        val images = snapshot.child("images")
                            .getValue(Int::class.java) as Int
                        creditChargePrice = creditPrice.toFloat() / images

                        userCurrentCredits =
                            appSettings.getString(Constants.userCreditsValue) as String

                        if (userCurrentCredits.isNotEmpty() && (userCurrentCredits != "0" || userCurrentCredits != "0.0") && userCurrentCredits.toFloat() >= creditChargePrice) {
                            hideSoftKeyboard(
                                context,
                                searchBtnView
                            )

                            val query = searchBoxView.text.toString().trim()
                            runOnUiThread {
                                loader.visibility = View.VISIBLE
                            }

                            searchInternetImages(
                                context,
                                query,
                                object : APICallback {
                                    override fun onSuccess(response: JSONObject) {
                                        if (loader.visibility == View.VISIBLE) {
                                            loader.visibility =
                                                View.INVISIBLE
                                        }

                                        val items =
                                            response.getJSONArray("items")
                                        if (items.length() > 0) {
                                            searchedImagesList.clear()
                                            for (i in 0 until items.length()) {
                                                val item =
                                                    items.getJSONObject(i)
                                                if (item.has("link")) {
                                                    searchedImagesList.add(
                                                        item.getString(
                                                            "link"
                                                        )
                                                    )
                                                }
                                            }

                                            internetImageAdapter.notifyItemRangeChanged(
                                                0,
                                                searchedImagesList.size
                                            )
                                            internetSearchBinding.iisdlDialogDoneBtn.visibility =
                                                View.VISIBLE
                                        }
                                        //userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String
                                        val hashMap = HashMap<String, Any>()
                                        val remaining =
                                            userCurrentCredits.toFloat() - creditChargePrice
                                        Log.d("TEST199", "$remaining")
                                        hashMap["credits"] =
                                            remaining.toString()
                                        firebaseDatabase.child(Constants.firebaseUserCredits)
                                            .child(Constants.firebaseUserId)
                                            .updateChildren(hashMap)
                                            .addOnSuccessListener {
                                                getUserCredits(
                                                    context
                                                )
                                            }
                                            .addOnFailureListener {

                                            }
                                    }

                                    override fun onError(error: VolleyError) {
                                        if (loader.visibility == View.VISIBLE) {
                                            loader.visibility =
                                                View.INVISIBLE
                                        }

                                        showAlert(
                                            context,
                                            error.localizedMessage!!
                                        )
                                    }

                                })
                        } else {
                            MaterialAlertDialogBuilder(context)
                                .setMessage(getString(R.string.low_credites_error_message))
                                .setCancelable(false)
                                .setNegativeButton(getString(R.string.no_text)) { dialog, which ->
                                    dialog.dismiss()
                                }
                                .setPositiveButton(getString(R.string.buy_credits)) { dialog, which ->
                                    dialog.dismiss()
                                    startActivity(Intent(context, UserScreenActivity::class.java))
                                }
                                .create().show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })


        } else {
            if (loader.visibility == View.VISIBLE) {
                loader.visibility = View.INVISIBLE
            }

            showAlert(
                context,
                getString(R.string.empty_text_error)
            )
        }
    }

    private var voiceResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // THIS LINE OF CODE WILL CHECK THE IMAGE HAS BEEN SELECTED OR NOT
            if (result.resultCode == Activity.RESULT_OK) {
                val spokenText: String =
                    result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        .let { results ->
                            results!![0]
                        }

                if (voiceSearchHint == "default") {
                    internetSearchBinding.textInputField.setText(spokenText)
                    Constants.hideKeyboar(context)
                    startSearch(
                        internetSearchBinding.textInputField,
                        internetSearchBinding.internetImageSearchBtn,
                        internetSearchBinding.imageLoaderView,
                        searchedImagesList as java.util.ArrayList<String>,
                        internetImageAdapter
                    )
                } else {
                    searchDialog.insalesProductsSearchBox.setText(spokenText)
                    binding.insalesProductsSearch.setText(spokenText)
                    search(spokenText, "default")
                    voiceSearchHint = "default"
                }
            }
        }

    private fun resetProductList() {
        productsList.clear()
        productsList.addAll(originalProductsList)
        productAdapter.notifyItemRangeChanged(0, productsList.size)
        binding.insalesProductsRecyclerview.smoothScrollToPosition(0)
    }

    private fun addProduct() {
        appSettings.putInt("CURRENT_PAGE",0)
        Constants.selectedRainForestProductImages = ""
        AddProductCustomDialog(
            originalCategoriesList,
            shopName,
            email,
            password,
            salesViewModel,
            object : ResponseListener {
                override fun onSuccess(result: String) {
                    dialogStatus = 1
                    fetchProducts()
                }

            }).show(supportFragmentManager, "add-dialog")

    }

    private fun search(text: String?, type: String) {

        val matchedProducts = mutableListOf<Product>()


        text?.let {

            if (type == "default") {
                productsList.forEach { item ->
                    if (item.title.contains(text, true) || item.fullDesc.contains(text, true)) {
                        matchedProducts.add(item)
                    }
                }
            } else {
                productsList.forEach { item ->
                    if (item.sku.contains(text, true)) {
                        matchedProducts.add(item)
                    }
                }
            }

            if (matchedProducts.isEmpty()) {
                Toast.makeText(
                    context,
                    getString(R.string.no_match_found_error),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                productsList.clear()
                productsList.addAll(matchedProducts)
                productAdapter.notifyItemRangeChanged(0, productsList.size)

            }
        }
    }

    private fun displayErrorItems() {
        val matchedProducts = mutableListOf<Product>()
        productsList.forEach { item ->
            if (item.title.length < 10 || item.fullDesc.length < 10 || item.productImages!!.size == 0) {
                matchedProducts.add(item)
            }
        }

        if (matchedProducts.isEmpty()) {
            Toast.makeText(
                context,
                getString(R.string.error_products_not_found),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            productsList.clear()
            productsList.addAll(matchedProducts)
            productAdapter.notifyDataSetChanged()
        }

    }

    private fun sorting(type: Int) {
        if (productsList.size > 0) {
            Collections.sort(productsList, object : Comparator<Product> {
                override fun compare(o1: Product?, o2: Product?): Int {
                    return if (type == 0) { // A-Z
                        o1!!.title.compareTo(o2!!.title, true)
                    } else if (type == 1) { // Z-A
                        o2!!.title.compareTo(o1!!.title, true)
                    } else {
                        -1
                    }
                }

            })
            productAdapter.notifyDataSetChanged()
        } else {
            showAlert(context, getString(R.string.empty_list_error_message))
        }
    }

    private fun displayCategoryFilterDialog(categoriesList: MutableList<Category>) {
        val builder = MaterialAlertDialogBuilder(context)
        builder.setCancelable(false)
        builder.setTitle(getString(R.string.filter_category_heading_text))
        builder.setNegativeButton(getString(R.string.cancel_text)) { dialog, which ->
            dialog.dismiss()
        }

        val arrayAdapter = ArrayAdapter(
            context,
            android.R.layout.select_dialog_singlechoice,
            categoriesList
        )
        builder.setAdapter(arrayAdapter, object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                dialog!!.dismiss()
                val id = categoriesList[which].id
                searchByCategory(id)
            }

        })
        val alert = builder.create()
        alert.show()
    }

    private fun searchByCategory(id: Int?) {
        val matchedProducts = mutableListOf<Product>()

        id?.let {
            productsList.forEach { item ->
                if (item.categoryId == id) {
                    matchedProducts.add(item)
                }
            }

            if (matchedProducts.isEmpty()) {
                Toast.makeText(
                    context,
                    getString(R.string.category_products_not_found),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                productsList.clear()
                productsList.addAll(matchedProducts)
                productAdapter.notifyItemRangeChanged(0, productsList.size)
            }
        }
    }

    private fun fetchProducts(page: Int) {
        if (dialogStatus == 1) {
            startLoading(
                context,
                getString(R.string.please_wait_products_message)
            )
        }

        viewModel.callProducts(context, shopName, email, password, page)
        viewModel.getSalesProductsResponse().observe(this) { response ->

            if (response != null) {
                if (response.get("status").asString == "200") {
                    dialogStatus = 0
                    if (menu != null) {
                        menu!!.findItem(R.id.insales_logout).isVisible = true
                        menu!!.findItem(R.id.insales_list_type).isVisible = true
                        if (productViewListType == 0) {
                            menu!!.findItem(R.id.insales_list_type)
                                .setIcon(R.drawable.ic_normal_list)
                        } else {
                            menu!!.findItem(R.id.insales_list_type)
                                .setIcon(R.drawable.ic_minimal_list)
                        }
                        menu!!.findItem(R.id.insales_data_sync).isVisible = true
                    }
                    val products = response.getAsJsonArray("products")
                    if (products.size() > 0) {

                        currentTotalProducts = products.size()

                        for (i in 0 until products.size()) {
                            val product = products.get(i).asJsonObject
                            val imagesArray = product.getAsJsonArray("images")
                            val variants = product.getAsJsonArray("variants")
                            val variantsItem = variants[0].asJsonObject
                            val imagesList = mutableListOf<ProductImages>()
                            if (imagesArray.size() > 0) {
                                for (j in 0 until imagesArray.size()) {
                                    val imageItem = imagesArray[j].asJsonObject
                                    imagesList.add(
                                        ProductImages(
                                            imageItem.get("id").asInt,
                                            imageItem.get("product_id").asInt,
                                            imageItem.get("url").asString,
                                            imageItem.get("position").asInt
                                        )
                                    )
                                }
                            }

                            if (Constants.imageLoadingStatus == 1 && product.get("id").asInt == Constants.productId) {
                                for (j in 0 until Constants.multiImagesSelectedListSize) {
                                    imagesList.add(
                                        ProductImages(
                                            0,
                                            Constants.productId,
                                            "",
                                            0
                                        )
                                    )
                                }
                            }

                            originalProductsList.add(
                                Product(
                                    product.get("id").asInt,
                                    product.get("category_id").asInt,
                                    product.get("title").asString,
                                    if (product.get("short_description").isJsonNull) {
                                        ""
                                    } else {
                                        product.get("short_description").asString
                                    },
                                    if (product.get("description").isJsonNull) {
                                        ""
                                    } else {
                                        product.get("description").asString
                                    },
                                    if (variantsItem.get("sku").isJsonNull) {
                                        ""
                                    } else {
                                        variantsItem.get("sku").asString
                                    },
                                    imagesList as ArrayList<ProductImages>
                                )
                            )

                        }

                        if (currentTotalProducts == 250) {
                            currentPage += 1
                            fetchProducts(currentPage)
                        } else {
                            Log.d("TEST199WRITE", "WRITE")
                            originalProductsList.sortByDescending { it.id }
                            Paper.book().write(Constants.cacheProducts, originalProductsList)

                            productsList.addAll(originalProductsList)
                            productAdapter.notifyDataSetChanged()
                            dismiss()
                        }
                    } else {
                        dismiss()
                    }
                } else {
                    dismiss()
                    showAlert(context, response.get("message").asString)
                }
            } else {
                dismiss()
            }
        }
    }

    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            it.apply {
                title = getString(R.string.app_name)
                setDisplayHomeAsUpEnabled(true)
            }
            binding.toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.white))
        }

        val toggle = ActionBarDrawerToggle(this, binding.drawer, binding.toolbar, 0, 0)
        binding.drawer.addDrawerListener(toggle)
        toggle.syncState()
        binding.navigation.setNavigationItemSelectedListener(this)

        binding.toolbar.setNavigationOnClickListener {
            hideSoftKeyboard(context, binding.drawer)
            if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
                binding.drawer.closeDrawer(GravityCompat.START)
            } else {
                binding.drawer.openDrawer(GravityCompat.START)
            }
        }

        binding.navigation.menu.findItem(R.id.login).isVisible = false
//        binding.navigation.menu.findItem(R.id.logout).isVisible = true
        binding.navigation.menu.findItem(R.id.profile).isVisible = false
        binding.navigation.menu.findItem(R.id.tables).isVisible = false
        binding.navigation.menu.findItem(R.id.credit).isVisible = false
        binding.navigation.menu.findItem(R.id.user_screen).isVisible = true
//            mNavigation.menu.findItem(R.id.tickets).isVisible = Constants.premiumSupportFeatureStatus == 1
//        binding.navigation.menu.findItem(R.id.purchase_feature).isVisible = false
        binding.navigation.menu.findItem(R.id.field_list).isVisible = false
//        binding.navigation.menu.findItem(R.id.insales).isVisible = false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.insales_logout -> {
                MaterialAlertDialogBuilder(context)
                    .setTitle(getString(R.string.logout))
                    .setMessage(getString(R.string.logout_insales_warning_text))
                    .setNegativeButton(getString(R.string.cancel_text)) { dialog, which ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(getString(R.string.logout)) { dialog, which ->
                        dialog.dismiss()
                        startLoading(context)
                        appSettings.remove("INSALES_STATUS")
                        appSettings.remove("INSALES_SHOP_NAME")
                        appSettings.remove("INSALES_EMAIL")
                        appSettings.remove("INSALES_PASSWORD")
                        Paper.book().destroy()
                        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this) {
                            mGoogleSignInClient.signOut().addOnCompleteListener(this) {
                                FirebaseAuth.getInstance().signOut()

                                appSettings.remove(Constants.isLogin)
                                appSettings.remove(Constants.user)
                                Constants.userData = null
                                Toast.makeText(
                                    context,
                                    getString(R.string.logout_success_text),
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                dismiss()
                                startActivity(Intent(context, LoginActivity::class.java)).apply {
                                    finish()
                                }
                            }
                        }
                    }
                    .create().show()
            }
            R.id.insales_list_type -> {
                productViewListType = appSettings.getInt("PRODUCT_VIEW_LIST_TYPE")
                if (productViewListType == 0) {
                    menu!!.findItem(R.id.insales_list_type).setIcon(R.drawable.ic_minimal_list)
                    productViewListType = 1
                    productAdapter.updateListType(productViewListType)
                    productAdapter.notifyDataSetChanged()
                    appSettings.putInt("PRODUCT_VIEW_LIST_TYPE", 1)

                } else {
                    menu!!.findItem(R.id.insales_list_type).setIcon(R.drawable.ic_normal_list)
                    productViewListType = 0
                    productAdapter.updateListType(productViewListType)
                    productAdapter.notifyDataSetChanged()
                    appSettings.putInt("PRODUCT_VIEW_LIST_TYPE", 0)
                }
            }
            R.id.insales_data_sync -> {
                dialogStatus = 1
                fetchProducts()
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
        return true
    }

    private fun fetchProducts() {
        productsList.clear()
        originalProductsList.clear()
        Paper.book().destroy()
        currentPage = 1
        fetchProducts(currentPage)
    }

    private fun getUserCredit() {
        if (auth.currentUser != null) {

            val userId = auth.currentUser!!.uid
            Constants.firebaseUserId = userId
            firebaseDatabase.child(Constants.firebaseUserCredits)
                .child(userId).addValueEventListener(object : ValueEventListener {
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
                        binding.homeCurrentCreditsView.text = "$roundedCreditValues"
                        appSettings.putString(Constants.userCreditsValue, "$roundedCreditValues")
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
        }
    }

    override fun onItemClick(position: Int) {

    }

    override fun onItemEditClick(position: Int, imagePosition: Int) {
        val imageItem = productsList[position].productImages!![imagePosition]
        insalesUpdateProductImageLayout =
            InsalesProductImageUpdateDialogBinding.inflate(layoutInflater)
        insalesUpdateProductImageLayout.cameraImageView.setOnClickListener {
            intentType = 1
            if (RuntimePermissionHelper.checkCameraPermission(
                    this,
                    Constants.CAMERA_PERMISSION
                )
            ) {
                //dispatchTakePictureIntent()
                val cameraIntent =
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraResultLauncher.launch(cameraIntent)
            }
        }

        insalesUpdateProductImageLayout.imagesImageView.setOnClickListener {
            intentType = 2
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {

                getImageFromGallery()
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_REQUEST_CODE
                )
            }
        }

        insalesUpdateProductImageLayout.internetImageView.setOnClickListener {
            internetSearchBinding = InternetImageSearchDialogLayoutBinding.inflate(layoutInflater)
            intentType = 3
            val tempImageList = mutableListOf<String>()
            searchedImagesList.clear()
            val builder = MaterialAlertDialogBuilder(context)
            builder.setCancelable(false)
            builder.setView(internetSearchBinding.root)
            val iAlert = builder.create()
            iAlert.show()

            internetSearchBinding.iisdlDialogDoneBtn.setOnClickListener {
                iAlert.dismiss()
            }

            internetSearchBinding.barcodeImgSearchInternetImages.setOnClickListener {
                barcodeSearchHint = "image"
                val intent = Intent(context, BarcodeReaderActivity::class.java)
                barcodeImageResultLauncher.launch(intent)
            }

            internetSearchBinding.searchImageDialogClose.setOnClickListener {
                iAlert.dismiss()
            }

            internetSearchBinding.internetSearchImageRecyclerview.layoutManager =
                StaggeredGridLayoutManager(
                    2,
                    LinearLayoutManager.VERTICAL
                )//GridLayoutManager(context, 2)
            internetSearchBinding.internetSearchImageRecyclerview.hasFixedSize()
            internetImageAdapter = InternetImageAdapter(
                context,
                searchedImagesList as java.util.ArrayList<String>
            )
            internetSearchBinding.internetSearchImageRecyclerview.adapter = internetImageAdapter
            internetImageAdapter.setOnItemClickListener(object :
                InternetImageAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    val selectedImage = searchedImagesList[position]
                    FullImageFragment(selectedImage).show(
                        supportFragmentManager,
                        "full-image-dialog"
                    )
                }

                override fun onItemAttachClick(btn: MaterialButton, position: Int) {
                    btn.text = getString(R.string.please_wait)

                    val selectedImage = searchedImagesList[position]
                    val bitmap: Bitmap? = ImageManager.getBitmapFromURL(
                        context,
                        selectedImage
                    )
                    if (bitmap != null) {
                        ImageManager.saveMediaToStorage(
                            context,
                            bitmap,
                            object : ResponseListener {
                                override fun onSuccess(result: String) {
                                    if (internetSearchBinding.imageLoaderView.visibility == View.VISIBLE) {
                                        internetSearchBinding.imageLoaderView.visibility =
                                            View.INVISIBLE
                                    }

                                    if (result.isNotEmpty()) {
                                        currentPhotoPath = ImageManager.getRealPathFromUri(
                                            context,
                                            Uri.parse(result)
                                        )!!
                                        Glide.with(context)
                                            .load(currentPhotoPath)
                                            .placeholder(R.drawable.placeholder)
                                            .centerInside()
                                            .into(insalesUpdateProductImageLayout.selectedInsalesProductImageView)
                                        selectedImageBase64String =
                                            ImageManager.convertImageToBase64(
                                                context,
                                                currentPhotoPath!!
                                            )
                                        iAlert.dismiss()
                                    } else {
                                        showAlert(
                                            context,
                                            getString(R.string.something_wrong_error)
                                        )
                                    }
                                }

                            })
                    } else {
                        if (internetSearchBinding.imageLoaderView.visibility == View.VISIBLE) {
                            internetSearchBinding.imageLoaderView.visibility = View.INVISIBLE
                        }
                        showAlert(
                            context,
                            getString(R.string.something_wrong_error)
                        )
                    }
                }

            })
            internetSearchBinding.voiceSearchInternetImages.setOnClickListener {
                voiceLanguageCode = appSettings.getString("VOICE_LANGUAGE_CODE") as String
                val voiceLayout = LayoutInflater.from(context).inflate(
                    R.layout.voice_language_setting_layout,
                    null
                )
                val voiceLanguageSpinner =
                    voiceLayout.findViewById<AppCompatSpinner>(R.id.voice_language_spinner)
                val voiceLanguageSaveBtn =
                    voiceLayout.findViewById<MaterialButton>(R.id.voice_language_save_btn)

                if (voiceLanguageCode == "en" || voiceLanguageCode.isEmpty()) {
                    voiceLanguageSpinner.setSelection(0, false)
                } else {
                    voiceLanguageSpinner.setSelection(1, false)
                }

                voiceLanguageSpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            voiceLanguageCode =
                                if (parent!!.selectedItem.toString().toLowerCase(
                                        Locale.ENGLISH
                                    ).contains("english")
                                ) {
                                    "en"
                                } else {
                                    "ru"
                                }
                            appSettings.putString("VOICE_LANGUAGE_CODE", voiceLanguageCode)

                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {

                        }

                    }
                val builder = MaterialAlertDialogBuilder(context)
                builder.setView(voiceLayout)
                val alert = builder.create();
                alert.show()
                voiceLanguageSaveBtn.setOnClickListener {
                    alert.dismiss()
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                        )
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, voiceLanguageCode)

                    }
                    voiceResultLauncher.launch(intent)
                }
            }

            internetSearchBinding.internetImageSearchBtn.setOnClickListener {
                startSearch(
                    internetSearchBinding.textInputField,
                    internetSearchBinding.internetImageSearchBtn,
                    internetSearchBinding.imageLoaderView,
                    searchedImagesList as java.util.ArrayList<String>,
                    internetImageAdapter
                )
            }

            internetSearchBinding.textInputField.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    startSearch(
                        internetSearchBinding.textInputField,
                        internetSearchBinding.internetImageSearchBtn,
                        internetSearchBinding.imageLoaderView,
                        searchedImagesList as java.util.ArrayList<String>,
                        internetImageAdapter
                    )
                }
                false
            }
        }


        Glide.with(context)
            .load(imageItem.imageUrl)
            .thumbnail(Glide.with(context).load(R.drawable.loader))
            .fitCenter()
            .into(insalesUpdateProductImageLayout.selectedInsalesProductImageView)
        val builder = MaterialAlertDialogBuilder(context)
        builder.setCancelable(false)
        builder.setView(insalesUpdateProductImageLayout.root)

        val alert = builder.create()
        alert.show()

        insalesUpdateProductImageLayout.insalesProductDialogCancelBtn.setOnClickListener {
            alert.dismiss()
        }

        insalesUpdateProductImageLayout.insalesProductDialogUpdateBtn.setOnClickListener {

            if (selectedImageBase64String.isNotEmpty()) {
                alert.dismiss()
                startLoading(context)

                salesViewModel.callUpdateProductImage(
                    context,
                    shopName,
                    email,
                    password,
                    selectedImageBase64String,
                    imageItem.productId,
                    imageItem.position,
                    imageItem.id,
                    "${System.currentTimeMillis()}.jpg"
                )
                salesViewModel.getUpdateProductImageResponse().observe(this) { response ->
                    if (response != null) {
                        if (response.get("status").asString == "200") {
                            selectedImageBase64String = ""
                            Handler(Looper.myLooper()!!).postDelayed({
                                dismiss()
                                fetchProducts()
                            }, 3000)
                        } else {
                            dismiss()
                            showAlert(
                                context,
                                response.get("message").asString
                            )
                        }
                    } else {
                        dismiss()
                    }
                }
            } else {
                showAlert(
                    context,
                    getString(R.string.image_attach_error)
                )
            }
        }

    }

    override fun onItemAddImageClick(position: Int) {
        val pItem = productsList[position]
        multiImagesList.clear()
        barcodeImageList.clear()
        insalesUpdateProductImageLayout =
            InsalesProductImageUpdateDialogBinding.inflate(LayoutInflater.from(context))
        insalesUpdateProductImageLayout.insalesProductImagesRecyclerview.layoutManager =
            LinearLayoutManager(
                context, RecyclerView.HORIZONTAL,
                false
            )

        insalesUpdateProductImageLayout.insalesProductImagesRecyclerview.hasFixedSize()
        adapter = BarcodeImageAdapter(
            context,
            barcodeImageList as java.util.ArrayList<String>
        )
        insalesUpdateProductImageLayout.insalesProductImagesRecyclerview.adapter = adapter
        adapter!!.setOnItemClickListener(object :
            BarcodeImageAdapter.OnItemClickListener {
            override fun onItemDeleteClick(position: Int) {
//                            val image = barcodeImageList[position]
                val builder = MaterialAlertDialogBuilder(context)
                builder.setMessage(getString(R.string.delete_barcode_image_message))
                builder.setCancelable(false)
                builder.setNegativeButton(getString(R.string.no_text)) { dialog, which ->
                    dialog.dismiss()
                }
                builder.setPositiveButton(getString(R.string.yes_text)) { dialog, which ->
                    dialog.dismiss()
                    barcodeImageList.removeAt(position)
                    multiImagesList.removeAt(position)
                    adapter!!.notifyItemRemoved(position)
                    if (barcodeImageList.size == 0) {
                        Glide.with(context)
                            .load("")
                            .placeholder(R.drawable.placeholder)
                            .centerInside()
                            .into(insalesUpdateProductImageLayout.selectedInsalesProductImageView)
                    }
                }
                val alert = builder.create()
                alert.show()

            }

            override fun onAddItemEditClick(position: Int) {

            }

            override fun onImageClick(position: Int) {

            }

        })

        insalesUpdateProductImageLayout.cameraImageView.setOnClickListener {
            intentType = 1
            if (RuntimePermissionHelper.checkCameraPermission(
                    context,
                    Constants.CAMERA_PERMISSION
                )
            ) {
                //dispatchTakePictureIntent()
                val cameraIntent =
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraResultLauncher.launch(cameraIntent)
            }
        }

        insalesUpdateProductImageLayout.imagesImageView.setOnClickListener {
            intentType = 2
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {

                getImageFromGallery()
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_REQUEST_CODE
                )
            }
        }

        insalesUpdateProductImageLayout.internetImageView.setOnClickListener {
            internetSearchBinding = InternetImageSearchDialogLayoutBinding.inflate(layoutInflater)
            intentType = 3
            val tempImageList = mutableListOf<String>()
            searchedImagesList.clear()
            val builder = MaterialAlertDialogBuilder(context)
            builder.setCancelable(false)
            builder.setView(internetSearchBinding.root)
            val iAlert = builder.create()
            iAlert.show()

            internetSearchBinding.iisdlDialogDoneBtn.setOnClickListener {
                iAlert.dismiss()
            }

            internetSearchBinding.barcodeImgSearchInternetImages.setOnClickListener {
               barcodeSearchHint = "image"
                val intent = Intent(context, BarcodeReaderActivity::class.java)
                barcodeImageResultLauncher.launch(intent)
            }

            internetSearchBinding.searchImageDialogClose.setOnClickListener {
                iAlert.dismiss()
            }

            internetSearchBinding.internetSearchImageRecyclerview.layoutManager =
                StaggeredGridLayoutManager(
                    2,
                    LinearLayoutManager.VERTICAL
                )//GridLayoutManager(context, 2)
            internetSearchBinding.internetSearchImageRecyclerview.hasFixedSize()
            internetImageAdapter = InternetImageAdapter(
                context,
                searchedImagesList as java.util.ArrayList<String>
            )
            internetSearchBinding.internetSearchImageRecyclerview.adapter = internetImageAdapter
            internetImageAdapter.setOnItemClickListener(object :
                InternetImageAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    val selectedImage = searchedImagesList[position]
                    FullImageFragment(selectedImage).show(
                        supportFragmentManager,
                        "full-image-dialog"
                    )
                }

                override fun onItemAttachClick(btn: MaterialButton, position: Int) {
                    //iAlert.dismiss()
                    selectedInternetImage = searchedImagesList[position]
                    Glide.with(context)
                        .load(selectedInternetImage)
                        .thumbnail(
                            Glide.with(context).load(R.drawable.placeholder)
                        )
                        .fitCenter()
                        .into(insalesUpdateProductImageLayout.selectedInsalesProductImageView)
                    if (btn.tag.toString().lowercase() == "attach") {
                        barcodeImageList.add(selectedInternetImage)
                        multiImagesList.add(selectedInternetImage)
                        btn.text = getString(R.string.attached_text)
                        btn.tag = "attached"
                        btn.setBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                R.color.dark_gray
                            )
                        )
                    } else {
                        btn.text = getString(R.string.attach_text)
                        btn.tag = "attach"
                        btn.setBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                R.color.primary_positive_color
                            )
                        )
                        barcodeImageList.remove(selectedInternetImage)
                        multiImagesList.remove(selectedInternetImage)
                    }
                    adapter!!.notifyDataSetChanged()
                    if (multiImagesList.isEmpty()){
                        Glide.with(context)
                            .load("")
                            .thumbnail(
                                Glide.with(context).load(R.drawable.placeholder)
                            )
                            .fitCenter()
                            .into(insalesUpdateProductImageLayout.selectedInsalesProductImageView)
                    }
                }

            })
            internetSearchBinding.voiceSearchInternetImages.setOnClickListener {
                voiceLanguageCode = appSettings.getString("VOICE_LANGUAGE_CODE") as String
                val voiceLayout = LayoutInflater.from(context).inflate(
                    R.layout.voice_language_setting_layout,
                    null
                )
                val voiceLanguageSpinner =
                    voiceLayout.findViewById<AppCompatSpinner>(R.id.voice_language_spinner)
                val voiceLanguageSaveBtn =
                    voiceLayout.findViewById<MaterialButton>(R.id.voice_language_save_btn)

                if (voiceLanguageCode == "en" || voiceLanguageCode.isEmpty()) {
                    voiceLanguageSpinner.setSelection(0, false)
                } else {
                    voiceLanguageSpinner.setSelection(1, false)
                }

                voiceLanguageSpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            voiceLanguageCode =
                                if (parent!!.selectedItem.toString().toLowerCase(
                                        Locale.ENGLISH
                                    ).contains("english")
                                ) {
                                    "en"
                                } else {
                                    "ru"
                                }
                            appSettings.putString("VOICE_LANGUAGE_CODE", voiceLanguageCode)

                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {

                        }

                    }
                val builder = MaterialAlertDialogBuilder(context)
                builder.setView(voiceLayout)
                val alert = builder.create();
                alert.show()
                voiceLanguageSaveBtn.setOnClickListener {
                    alert.dismiss()
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                        )
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, voiceLanguageCode)

                    }
                    voiceResultLauncher.launch(intent)
                }
            }

            internetSearchBinding.internetImageSearchBtn.setOnClickListener {
                startSearch(
                    internetSearchBinding.textInputField,
                    internetSearchBinding.internetImageSearchBtn,
                    internetSearchBinding.imageLoaderView,
                    searchedImagesList as java.util.ArrayList<String>,
                    internetImageAdapter
                )
            }

            internetSearchBinding.textInputField.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    startSearch(
                        internetSearchBinding.textInputField,
                        internetSearchBinding.internetImageSearchBtn,
                        internetSearchBinding.imageLoaderView,
                        searchedImagesList as java.util.ArrayList<String>,
                        internetImageAdapter
                    )
                }
                false
            }
        }



        Glide.with(context)
            .load("")
            .thumbnail(Glide.with(context).load(R.drawable.placeholder))
            .fitCenter()
            .into(insalesUpdateProductImageLayout.selectedInsalesProductImageView)
        val builder = MaterialAlertDialogBuilder(context)
        builder.setCancelable(false)
        builder.setView(insalesUpdateProductImageLayout.root)

        val alert = builder.create()
        alert.show()

        insalesUpdateProductImageLayout.insalesProductDialogCancelBtn.setOnClickListener {
            alert.dismiss()
        }

        insalesUpdateProductImageLayout.insalesProductDialogUpdateBtn.setOnClickListener {

            if (multiImagesList.isNotEmpty()) {
                val tempProduct = pItem
                for (i in 0 until multiImagesList.size) {
                    tempProduct.productImages!!.add(
                        ProductImages(
                            0,
                            pItem.id,
                            "",
                            0
                        )
                    )
                }
                productsList.removeAt(position)
                productsList.add(position, tempProduct)
                productAdapter.notifyDataSetChanged()
                Constants.startImageUploadService(
                    pItem.id,
                    multiImagesList.joinToString(","),
                    "add_image",
                    true
                )
                Constants.multiImagesSelectedListSize = multiImagesList.size
                multiImagesList.clear()
                alert.dismiss()
            } else {
                Constants.multiImagesSelectedListSize = 0
                showAlert(
                    context,
                    getString(R.string.image_attach_error)
                )
            }
        }


    }

    override fun onItemRemoveClick(position: Int, imagePosition: Int) {
        val imageItem = productsList[position].productImages!![imagePosition]
        MaterialAlertDialogBuilder(context)
            .setTitle(getString(R.string.remove_text))
            .setMessage(getString(R.string.image_remove_warning_message))
            .setCancelable(false)
            .setNegativeButton(getString(R.string.cancel_text)) { dialog, which ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.remove_text)) { dialog, which ->
                dialog.dismiss()
                startLoading(context)
                salesViewModel.callRemoveProductImage(
                    context,
                    shopName,
                    email,
                    password,
                    imageItem.productId,
                    imageItem.id
                )
                salesViewModel.getRemoveProductImageResponse().observe(this) { response ->
                    if (response != null) {
                        if (response.get("status").asString == "200") {
//                                                Handler(Looper.myLooper()!!).postDelayed({
                            dismiss()
                            productsList[position].productImages!!.removeAt(imagePosition)
                            productAdapter.notifyDataSetChanged()
                            updateMainProductList(productsList[position])
//                                                    dialogStatus = 1
//                                                    fetchProducts()//showProducts()
//                                                }, 3000)
                        } else {
                            dismiss()
                            showAlert(
                                context,
                                response.get("message").asString
                            )
                        }
                    } else {
//                                            Handler(Looper.myLooper()!!).postDelayed({
                        dismiss()
                        productsList[position].productImages!!.removeAt(imagePosition)
                        productAdapter.notifyDataSetChanged()
                        updateMainProductList(productsList[position])
//                                                dialogStatus = 1
//                                                fetchProducts()//showProducts()
//                                            }, 3000)
                    }
                }

            }.create().show()
    }

    override fun onItemEditImageClick(position: Int) {
        val pItem = productsList[position]
        CustomDialog(
            shopName,
            email,
            password,
            pItem,
            position,
            null,
            productAdapter,
            salesViewModel,
            object : ResponseListener {
                override fun onSuccess(result: String) {
                    if (result.contains("image_changes")) {

                        if (Constants.productId != 0) {
                            addLoadingImages(Constants.productId)
                        } else {
                            dialogStatus = 0
                            fetchProducts()
                        }
                    } else {
                        dialogStatus = 1
                        fetchProducts()
                    }
                }

            }).show(
            supportFragmentManager,
            "dialog"
        )
    }

    override fun onItemGrammarCheckClick(
        position: Int,
        grammarCheckBtn: AppCompatImageView,
        title: ExpandableTextView,
        description: ExpandableTextView,
        grammarStatusView: MaterialTextView
    ) {
        val item = productsList[position]
        characters = appSettings.getInt("GRAMMAR_CHARACTERS_LIMIT")
        grammarPrice = appSettings.getString("GRAMMAR_CHARACTERS_PRICE")!!.toFloat()
        unitCharacterPrice = grammarPrice / characters
        userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String

        val totalCharacters = item.title.length + item.fullDesc.length
        val totalCreditPrice = unitCharacterPrice * totalCharacters
        howMuchChargeCredits = totalCreditPrice

        if (userCurrentCredits.isNotEmpty() && (userCurrentCredits != "0" || userCurrentCredits != "0.0") && userCurrentCredits.toFloat() >= totalCreditPrice) {
            startLoading(context)
            GrammarCheck.check(
                context,
                item.title,
                title,
                1,
                grammarStatusView,
                object : GrammarCallback {
                    override fun onSuccess(
                        response: SpannableStringBuilder?,
                        errors: Boolean
                    ) {
                        GrammarCheck.check(
                            context,
                            item.fullDesc,
                            description,
                            0,
                            grammarStatusView,
                            object : GrammarCallback {
                                override fun onSuccess(
                                    response: SpannableStringBuilder?,
                                    errors: Boolean
                                ) {
                                    dismiss()
                                    chargeCreditsPrice()
                                    if (errors) {
                                        grammarStatusView.setTextColor(Color.RED)
                                        grammarStatusView.text =
                                            getString(
                                                R.string.error_found_text
                                            )
                                        //grammarCheckBtn.setImageResource(R.drawable.red_cross)
                                        grammarCheckBtn.setColorFilter(
                                            ContextCompat.getColor(
                                                context,
                                                R.color.red
                                            ),
                                            android.graphics.PorterDuff.Mode.MULTIPLY
                                        )
                                    } else {
                                        grammarStatusView.setTextColor(Color.GREEN)
                                        grammarStatusView.text =
                                            getString(
                                                R.string.no_erros_text
                                            )
                                        // grammarCheckBtn.setImageResource(R.drawable.green_check_48)
                                        grammarCheckBtn.setColorFilter(
                                            ContextCompat.getColor(
                                                context,
                                                R.color.green
                                            ),
                                            android.graphics.PorterDuff.Mode.MULTIPLY
                                        )
                                    }
                                }

                            })
                    }

                })
        } else {
            MaterialAlertDialogBuilder(context)
                .setMessage(getString(R.string.low_credites_error_message))
                .setCancelable(false)
                .setNegativeButton(getString(R.string.no_text)) { dialog, which ->
                    dialog.dismiss()
                }
                .setPositiveButton(getString(R.string.buy_credits)) { dialog, which ->
                    dialog.dismiss()
                    startActivity(
                        Intent(
                            context,
                            UserScreenActivity::class.java
                        )
                    )
                }
                .create().show()
        }
    }

    override fun onItemGetDescriptionClick(position: Int) {
        val pItem = productsList[position]
        userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String

        if (userCurrentCredits.toFloat() >= 1.0) {
            Constants.pItemPosition = position
            Constants.pItem = pItem

            launchActivity.launch(
                Intent(
                    context,
                    RainForestApiActivity::class.java
                )
            )
        } else {
            MaterialAlertDialogBuilder(context)
                .setMessage(getString(R.string.low_credites_error_message2))
                .setCancelable(false)
                .setNegativeButton(getString(R.string.no_text)) { dialog, which ->
                    dialog.dismiss()
                }
                .setPositiveButton(getString(R.string.buy_credits)) { dialog, which ->
                    dialog.dismiss()
                    startActivity(Intent(context, UserScreenActivity::class.java))
                }
                .create().show()
        }
    }

    override fun onItemCameraIconClick(
        position: Int,
        title: ExpandableTextView,
        description: ExpandableTextView
    ) {
        val item = productsList[position]
        Constants.pItemPosition = position
        Constants.pItem = item
        Constants.pTitle = title
        Constants.pDescription = description
        //BaseActivity.showAlert(requireActivity(),item.title)
        if (RuntimePermissionHelper.checkCameraPermission(
                context, Constants.CAMERA_PERMISSION
            )
        ) {
            // BaseActivity.hideSoftKeyboard(requireActivity())
//            pickImageFromCamera()
        }
    }

    override fun onItemImageIconClick(
        position: Int,
        title: ExpandableTextView,
        description: ExpandableTextView
    ) {
        val item = productsList[position]
        Constants.pItemPosition = position
        Constants.pItem = item
        Constants.pTitle = title
        Constants.pDescription = description
        //BaseActivity.showAlert(requireActivity(),item.fullDesc)
        if (RuntimePermissionHelper.checkCameraPermission(
                context,
                Constants.READ_STORAGE_PERMISSION
            )
        ) {
            //BaseActivity.hideSoftKeyboard(requireActivity())
//            pickImageFromGallery()
        }
    }

    var launchActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && Constants.pItem != null) {
                val data: Intent? = result.data

                if (data != null && data.hasExtra("TITLE")) {
                    val title = data.getStringExtra("TITLE") as String
                    if (title.isNotEmpty()) {
                        val currentPItemTitle = Constants.pItem!!.title
                        val stringBuilder = java.lang.StringBuilder()
                        stringBuilder.append(currentPItemTitle)
                        stringBuilder.append(title)
                        Constants.pItem!!.title = stringBuilder.toString()
                    }
                }

                if (data != null && data.hasExtra("DESCRIPTION")) {
                    val description = data.getStringExtra("DESCRIPTION") as String
                    if (description.isNotEmpty()) {

                        val currentPItemDescription = Constants.pItem!!.fullDesc
                        val stringBuilder = java.lang.StringBuilder()
                        stringBuilder.append(currentPItemDescription)
                        stringBuilder.append(description)
                        Constants.pItem!!.fullDesc = stringBuilder.toString()

                    }
                }
                val tempImageList = mutableListOf<String>()
                val images = Constants.selectedRainForestProductImages
                if (images.isNotEmpty()) {

                    if (images.contains(",")) {
                        tempImageList.addAll(images.split(","))
                    } else {
                        tempImageList.add(images)
                    }
                    if (tempImageList.isNotEmpty()) {
                        for (i in 0 until tempImageList.size) {
                            Constants.pItem!!.productImages!!.add(
                                ProductImages(
                                    0,
                                    Constants.pItem!!.id,
                                    "",
                                    0
                                )
                            )
                        }
                    }
                }

                CustomDialog(
                    shopName,
                    email,
                    password,
                    Constants.pItem!!,
                    Constants.pItemPosition!!,
                    tempImageList,
                    productAdapter,
                    salesViewModel, object : ResponseListener {
                        override fun onSuccess(result: String) {
                            if (result.contains("image_changes")) {
//                                dialogStatus = 0
//                                fetchProducts()
                                if (Constants.productId != 0) {
                                    addLoadingImages(Constants.productId)
                                } else {
                                    dialogStatus = 0
                                    fetchProducts()
                                }
                            } else {
                                dialogStatus = 1
                                fetchProducts()
                            }
                        }

                    }
                ).show(supportFragmentManager, "dialog")
                productAdapter.notifyItemChanged(Constants.pItemPosition!!)

            }
        }

    private fun chargeCreditsPrice() {
        val firebaseDatabase = FirebaseDatabase.getInstance().reference
        val hashMap = HashMap<String, Any>()
        val remaining = userCurrentCredits.toFloat() - howMuchChargeCredits
        userCurrentCredits = remaining.toString()
        hashMap["credits"] = userCurrentCredits
        firebaseDatabase.child(Constants.firebaseUserCredits)
            .child(Constants.firebaseUserId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                howMuchChargeCredits = 0F
                getUserCredits(
                    context
                )
            }
            .addOnFailureListener {

            }
    }

    var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != null && intent.action == "update-products") {
                dialogStatus = 1
                fetchProducts()
            } else if (intent.action != null && intent.action == "update-images") {
                val apiInterface: ApiServices =
                    RetrofitClientApi.createService(ApiServices::class.java)
                val productId = intent.getIntExtra("PID", 0)

                apiInterface.salesProduct(email, password, shopName, productId)
                    .enqueue(object : Callback<JsonObject> {
                        override fun onResponse(
                            call: Call<JsonObject>,
                            response: Response<JsonObject>?
                        ) {
                            val result = response!!.body()
                            if (result != null) {
                                if (result.get("status").asString == "200") {

                                    val product = result.getAsJsonObject("product")
                                    val imagesArray = product.getAsJsonArray("images")
                                    val variants = product.getAsJsonArray("variants")
                                    val variantsItem = variants[0].asJsonObject
                                    val imagesList = mutableListOf<ProductImages>()

                                    if (imagesArray.size() > 0) {
                                        for (j in 0 until imagesArray.size()) {
                                            val imageItem = imagesArray[j].asJsonObject
                                            imagesList.add(
                                                ProductImages(
                                                    imageItem.get("id").asInt,
                                                    imageItem.get("product_id").asInt,
                                                    imageItem.get("url").asString,
                                                    imageItem.get("position").asInt
                                                )
                                            )
                                        }
                                    }

                                    val updatedProduct = Product(
                                        product.get("id").asInt,
                                        product.get("category_id").asInt,
                                        product.get("title").asString,
                                        if (product.get("short_description").isJsonNull) {
                                            ""
                                        } else {
                                            product.get("short_description").asString
                                        },
                                        if (product.get("description").isJsonNull) {
                                            ""
                                        } else {
                                            product.get("description").asString
                                        },
                                        if (variantsItem.get("sku").isJsonNull) {
                                            ""
                                        } else {
                                            variantsItem.get("sku").asString
                                        },
                                        imagesList as ArrayList<ProductImages>
                                    )
                                    var foundPosition = -1
                                    val cacheList: ArrayList<Product>? =
                                        Paper.book().read(Constants.cacheProducts)
                                    if (cacheList!!.isNotEmpty()) {
                                        for (i in 0 until cacheList.size) {
                                            val item = cacheList[i]
                                            if (item.id == productId) {
                                                foundPosition = i
                                                break
                                            }
                                        }

                                        if (foundPosition != -1) {
                                            cacheList.removeAt(foundPosition)
                                            cacheList.add(foundPosition, updatedProduct)
                                            Paper.book().destroy()
                                            Paper.book().write(Constants.cacheProducts, cacheList)
                                            originalProductsList.clear()
                                            productsList.clear()
                                            originalProductsList.addAll(cacheList)
                                            productsList.addAll(originalProductsList)
                                            productAdapter.notifyDataSetChanged()
                                        }
                                    }

                                }
                            }
                        }

                        override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                            Log.d("TEST199", "t!!.localizedMessage")
                        }

                    })
            }
        }
    }

    fun addLoadingImages(productId: Int) {
        var foundPosition = -1
        var foundItem: Product? = null
        val cacheList: ArrayList<Product>? = Paper.book().read(Constants.cacheProducts)
        if (cacheList!!.isNotEmpty()) {
            for (i in 0 until cacheList.size) {
                val item = cacheList[i]
                if (item.id == productId) {
                    foundPosition = i
                    foundItem = item
                    break
                }
            }

            if (foundPosition != -1 && foundItem != null) {

                for (j in 0 until Constants.multiImagesSelectedListSize) {
                    foundItem.productImages!!.add(
                        ProductImages(
                            0,
                            productId,
                            "",
                            0
                        )
                    )
                }

                cacheList.removeAt(foundPosition)
                cacheList.add(foundPosition, foundItem)
                Paper.book().destroy()
                Paper.book().write(Constants.cacheProducts, cacheList)
                originalProductsList.clear()
                productsList.clear()
                originalProductsList.addAll(cacheList)
                productsList.addAll(originalProductsList)

                productAdapter.notifyItemChanged(0, productsList.size)

            }
        }
    }

    private fun updateMainProductList(changeItem: Product) {
        CoroutineScope(Dispatchers.IO).launch {

            if (originalProductsList.isNotEmpty()) {
                var isFound = false
                var foundPosition = -1
                for (i in 0 until originalProductsList.size) {
                    if (originalProductsList[i].id == changeItem.id) {
                        isFound = true
                        foundPosition = i
                        break
                    } else {
                        isFound = false
                    }
                }
                if (isFound && foundPosition != -1) {
                    originalProductsList.removeAt(foundPosition)
                    originalProductsList.add(foundPosition, changeItem)
                    Paper.book().destroy()
                    Paper.book().write(Constants.cacheProducts, originalProductsList)
                }
            }

        }
    }

    private fun getPrices() {
        firebaseDatabase.child(Constants.firebasePrices).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pDetailsPrice: String =
                    snapshot.child("rainforest").child("p_details_price").getValue(
                        Float::class.java
                    ).toString()
                val pListPrice: String =
                    snapshot.child("rainforest").child("p_list_price").getValue(
                        Float::class.java
                    ).toString()
                val characters =
                    snapshot.child("translator").child("characters").getValue(Int::class.java)
                val translatorPrice: String = snapshot.child("translator").child("price").getValue(
                    Float::class.java
                ).toString()

                val grammarCharacters =
                    snapshot.child("grammar").child("characters").getValue(Int::class.java)
                val grammarPrice: String = snapshot.child("grammar").child("price").getValue(
                    Float::class.java
                ).toString()

                appSettings.putString("P_DETAILS_PRICE", pDetailsPrice)
                appSettings.putString("P_LIST_PRICE", pListPrice)
                appSettings.putInt("TRANSLATOR_CHARACTERS_LIMIT", characters!!)
                appSettings.putString("TRANSLATOR_CHARACTERS_PRICE", translatorPrice)
                appSettings.putInt("GRAMMAR_CHARACTERS_LIMIT", grammarCharacters!!)
                appSettings.putString("GRAMMAR_CHARACTERS_PRICE", grammarPrice)
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun checkAndStartTrialPeriod() {
        if (auth.currentUser != null) {
            val id = auth.uid as String
            Constants.firebaseUserId = id
//            startLoading(context)
            activeTrialFeatures(context, Constants.firebaseUserId, object : APICallback {
                override fun onSuccess(response: JSONObject) {
//                    dismiss()
//                    getUserCurrentFeatures()
                }

                override fun onError(error: VolleyError) {
//                    dismiss()
                }

            })
        }
    }

    private fun getImageFromGallery() {
        val fileIntent = Intent(Intent.ACTION_PICK)
        fileIntent.type = "image/*"
        resultLauncher.launch(fileIntent)
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // THIS LINE OF CODE WILL CHECK THE IMAGE HAS BEEN SELECTED OR NOT
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data != null) {
                    val imageUri = result.data!!
                    currentPhotoPath = ImageManager.getRealPathFromUri(
                        context,
                        imageUri.data
                    )

//                    Log.d("TEST199", selectedImageBase64String)
                    Glide.with(context)
                        .load(currentPhotoPath)
                        .placeholder(R.drawable.placeholder)
                        .centerInside()
                        .into(insalesUpdateProductImageLayout.selectedInsalesProductImageView)
                    if (adapter != null) {
                        barcodeImageList.add(currentPhotoPath!!)
                        multiImagesList.add(currentPhotoPath!!)
                        adapter!!.notifyDataSetChanged()
                    } else {
                        selectedImageBase64String =
                            ImageManager.convertImageToBase64(
                                context,
                                currentPhotoPath!!
                            )
                    }
                }

            }
        }

    private var cameraResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // THIS LINE OF CODE WILL CHECK THE IMAGE HAS BEEN SELECTED OR NOT
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val bitmap = data!!.extras!!.get("data") as Bitmap
                createImageFile(bitmap)
//                selectedImageBase64String =
//                    ImageManager.convertImageToBase64(requireActivity(), currentPhotoPath!!)
//                Log.d("TEST199", selectedImageBase64String)
                Glide.with(context)
                    .load(currentPhotoPath)
                    .placeholder(R.drawable.placeholder)
                    .centerInside()
                    .into(insalesUpdateProductImageLayout.selectedInsalesProductImageView)
                if (adapter != null) {
                    barcodeImageList.add(currentPhotoPath!!)
                    multiImagesList.add(currentPhotoPath!!)
                    adapter!!.notifyDataSetChanged()
                } else {
                    selectedImageBase64String =
                        ImageManager.convertImageToBase64(
                            context,
                            currentPhotoPath!!
                        )
                }
            }
        }

    private fun createImageFile(bitmap: Bitmap) {
        currentPhotoPath = ImageManager.readWriteImage(context, bitmap).absolutePath
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.user_screen -> {
                getSearchImageDetail()
                startActivity(Intent(context, UserScreenActivity::class.java))
            }
            R.id.nav_setting -> {
                startActivity(Intent(context, SettingsActivity::class.java))
            }
            R.id.nav_rateUs -> {
                rateUs(this)
            }
            R.id.nav_recommend -> {
                shareApp()
            }
            R.id.nav_contact_support -> {
                contactSupport(this)
            }
            else->{

            }
        }
        binding.drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(
            Intent.EXTRA_TEXT,
            getString(R.string.share_app_message) + "https://play.google.com/store/apps/details?id=" + packageName
        )
        startActivity(shareIntent)

    }


}