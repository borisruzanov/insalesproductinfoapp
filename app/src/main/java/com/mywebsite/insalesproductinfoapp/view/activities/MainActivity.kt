package com.mywebsite.insalesproductinfoapp.view.activities

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import com.boris.expert.csvmagic.model.ProductImages
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.mywebsite.insalesproductinfoapp.R
import com.mywebsite.insalesproductinfoapp.adapters.InSalesProductsAdapter
import com.mywebsite.insalesproductinfoapp.adapters.InternetImageAdapter
import com.mywebsite.insalesproductinfoapp.databinding.ActivityMainBinding
import com.mywebsite.insalesproductinfoapp.databinding.InsalesProductSearchDialogBinding
import com.mywebsite.insalesproductinfoapp.databinding.InternetImageSearchDialogLayoutBinding
import com.mywebsite.insalesproductinfoapp.interfaces.APICallback
import com.mywebsite.insalesproductinfoapp.model.Category
import com.mywebsite.insalesproductinfoapp.model.Product
import com.mywebsite.insalesproductinfoapp.utils.AppSettings
import com.mywebsite.insalesproductinfoapp.utils.Constants
import com.mywebsite.insalesproductinfoapp.utils.WrapContentLinearLayoutManager
import com.mywebsite.insalesproductinfoapp.viewmodel.MainActivityViewModel
import io.paperdb.Paper
import net.expandable.ExpandableTextView
import org.json.JSONObject
import java.math.RoundingMode
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : BaseActivity(), InSalesProductsAdapter.OnItemClickListener {

    private lateinit var context: Context
    private lateinit var binding: ActivityMainBinding
    private lateinit var appSettings: AppSettings
    private var menu: Menu? = null
    private lateinit var productAdapter: InSalesProductsAdapter
    private var productViewListType = 0
    private var productsList = mutableListOf<Product>()
    private var originalProductsList = mutableListOf<Product>()
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
    private var voiceSearchHint = "default"
    private var productImagesChanges = false
    private var selectedImageBase64String: String = ""
    private var voiceLanguageCode = "en"
    private var barcodeSearchHint = "default"
    var searchedImagesList = mutableListOf<String>()
    private var currentPhotoPath: String? = null
    private lateinit var searchDialog: InsalesProductSearchDialogBinding
    private lateinit var internetSearchBinding: InternetImageSearchDialogLayoutBinding
    private lateinit var internetImageAdapter: InternetImageAdapter
    private var userCurrentCredits = ""
    private var originalCategoriesList = mutableListOf<Category>()
    private var categoriesList = mutableListOf<Category>()

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

        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
//            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
//            .requestScopes(Scope(SheetsScopes.SPREADSHEETS))
//            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
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
            //addProduct()
        }

    }

    override fun onResume() {
        super.onResume()
        internetSearchBinding =
            InternetImageSearchDialogLayoutBinding.inflate(LayoutInflater.from(this))
    }

    private fun openSearchDialog() {
        val searchDialog =
            InsalesProductSearchDialogBinding.inflate(LayoutInflater.from(context))//LayoutInflater.from(requireActivity()).inflate(R.layout.insales_product_search_dialog, null)
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
            searchDialog.insalesProductsSearchBox.setSelection(binding.insalesProductsSearch.text.toString().length)
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
//                            searchBox.setText(spokenText)
//                            search(spokenText, "default")
//                            voiceSearchHint = "default"
                }
            }
        }

    private fun resetProductList() {
        productsList.clear()
        productsList.addAll(originalProductsList)
        productAdapter.notifyItemRangeChanged(0, productsList.size)
        binding.insalesProductsRecyclerview.smoothScrollToPosition(0)
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
            }
            binding.toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.white))
        }
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
                        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this){
                            mGoogleSignInClient.signOut().addOnCompleteListener(this){
                                FirebaseAuth.getInstance().signOut()

                                appSettings.remove(Constants.isLogin)
                                appSettings.remove(Constants.user)
                                Constants.userData = null
                                Toast.makeText(context, getString(R.string.logout_success_text), Toast.LENGTH_SHORT)
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

    }

    override fun onItemAddImageClick(position: Int) {

    }

    override fun onItemRemoveClick(position: Int, imagePosition: Int) {

    }

    override fun onItemEditImageClick(position: Int) {

    }

    override fun onItemGrammarCheckClick(
        position: Int,
        grammarCheckBtn: AppCompatImageView,
        title: ExpandableTextView,
        description: ExpandableTextView,
        grammarStatusView: MaterialTextView
    ) {

    }

    override fun onItemGetDescriptionClick(position: Int) {

    }

    override fun onItemCameraIconClick(
        position: Int,
        title: ExpandableTextView,
        description: ExpandableTextView
    ) {

    }

    override fun onItemImageIconClick(
        position: Int,
        title: ExpandableTextView,
        description: ExpandableTextView
    ) {

    }
}