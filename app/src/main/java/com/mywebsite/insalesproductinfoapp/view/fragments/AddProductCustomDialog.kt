package com.mywebsite.insalesproductinfoapp.view.fragments

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.content.ContextCompat
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager.widget.ViewPager
import com.android.volley.VolleyError
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mywebsite.insalesproductinfoapp.R
import com.mywebsite.insalesproductinfoapp.adapters.BarcodeImageAdapter
import com.mywebsite.insalesproductinfoapp.adapters.FieldListsAdapter
import com.mywebsite.insalesproductinfoapp.adapters.InternetImageAdapter
import com.mywebsite.insalesproductinfoapp.interfaces.APICallback
import com.mywebsite.insalesproductinfoapp.interfaces.ResponseListener
import com.mywebsite.insalesproductinfoapp.model.Category
import com.mywebsite.insalesproductinfoapp.model.ListItem
import com.mywebsite.insalesproductinfoapp.utils.*
import com.mywebsite.insalesproductinfoapp.view.activities.*
import com.mywebsite.insalesproductinfoapp.view.activities.BaseActivity.Companion.capitalized
import com.mywebsite.insalesproductinfoapp.viewmodel.AddProductViewModel
import com.mywebsite.insalesproductinfoapp.viewmodel.SalesCustomersViewModel
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import org.json.JSONObject
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class AddProductCustomDialog(
    private val originalCategoriesList: MutableList<Category>,
    private val shopName: String,
    private val email: String,
    private val password: String,
    private val viewModel: SalesCustomersViewModel,
    private val listener: ResponseListener
) : DialogFragment() {

    private lateinit var apQuantityView: TextInputEditText
    private lateinit var apPriceView: TextInputEditText
    private lateinit var cameraImageView: AppCompatImageView
    private lateinit var imagesImageView: AppCompatImageView
    private lateinit var internetImageView: AppCompatImageView
    private lateinit var apPriceDefaultInputBox: TextInputEditText
    private lateinit var apQuantityDefaultInputBox: TextInputEditText
    private lateinit var apPriceListBtn: MaterialButton
    private lateinit var apQuantityListBtn: MaterialButton
    private lateinit var apPriceSpinner: AppCompatSpinner
    private lateinit var apQuantitySpinner: AppCompatSpinner
    private lateinit var apCancelBtn: MaterialButton
    private lateinit var apSubmitBtn: MaterialButton
    private lateinit var apAddDescriptionView: MaterialTextView
    private lateinit var apPriceDefaultInputWrapper: TextInputLayout
    private lateinit var apPriceViewWrapper: TextInputLayout
    private lateinit var apPriceVoiceRecView: LinearLayout
    private lateinit var apQuantityDefaultInputWrapper: TextInputLayout
    private lateinit var apQuantityViewWrapper: TextInputLayout
    private lateinit var apQuantityVoiceRecView: LinearLayout
    private lateinit var apTestDataView: MaterialTextView
    private lateinit var tableGenerator: TableGenerator
    private lateinit var internetImageRecyclerView: RecyclerView
    private lateinit var internetImageDoneBtn: MaterialButton
    private lateinit var apTitleActiveListNameView: MaterialTextView
    private lateinit var apDescriptionActiveListNameView: MaterialTextView
    private lateinit var apQuantityActiveListNameView: MaterialTextView
    private lateinit var apPriceActiveListNameView: MaterialTextView
//    private var insalesFragment: InsalesFragment? = null
    private var selectedCategoryId = 0
    private var selectedInternetImage = ""
    private var userCurrentCredits = ""
    private lateinit var appSettings: AppSettings
    private lateinit var selectedImageView: AppCompatImageView
    private var currentPhotoPath: String? = null
    private var selectedImageBase64String: String = ""
    private var intentType = 0
    private lateinit var categoriesSpinner: AppCompatSpinner
    private lateinit var apTitleView: TextInputEditText
    private lateinit var apTitleDefaultInputBox: TextInputEditText
    private lateinit var apDescriptionView: TextInputEditText
    private lateinit var apDescriptionDefaultInputBox: TextInputEditText
    private var finalTitleText = ""
    private var finalDescriptionText = ""
    private var finalQuantityText = ""
    private var finalPriceText = ""

    //private var CIVType = ""
    private lateinit var quickModeCheckBox: MaterialCheckBox
    private lateinit var apViewPager: MyViewPager
    private lateinit var apFirstLayout: LinearLayout
    private lateinit var apSecondLayout: LinearLayout
    private lateinit var apNextPreviousButtons: LinearLayout
    private lateinit var apPreviousBtn: MaterialTextView
    private lateinit var apNextBtn: MaterialTextView
    private lateinit var apBackArrowBtn: AppCompatImageView
    private lateinit var internetImageAdapter: InternetImageAdapter
    private lateinit var searchBtnView: ImageButton
    private lateinit var searchBoxView: TextInputEditText
    private lateinit var loader: ProgressBar
    private lateinit var voiceSearchIcon: AppCompatImageView
    private var voiceLanguageCode = "en"
    val searchedImagesList = mutableListOf<String>()
    private lateinit var addProdcutViewModel: AddProductViewModel
    private lateinit var testDataBtn: MaterialTextView
    private lateinit var getTitleBtn: MaterialTextView
    private lateinit var imagesRecyclerView: RecyclerView
    private var barcodeImageList = mutableListOf<String>()
    var multiImagesList = mutableListOf<String>()
    private lateinit var imagesAdapter: BarcodeImageAdapter
    private lateinit var apTitleListSpinner: AppCompatSpinner
    private lateinit var apDescriptionListSpinner:AppCompatSpinner
    private lateinit var apQuantityListSpinner:AppCompatSpinner
    private lateinit var apPriceListSpinner:AppCompatSpinner

    private lateinit var apDescriptionSpinner: AppCompatSpinner
    private lateinit var apDescriptionVoiceRecView: LinearLayout
    private lateinit var apDescriptionImageRecView: LinearLayout
    private lateinit var apDescriptionCameraRecView: LinearLayout
    private lateinit var apDescriptionDefaultValueMessage: MaterialTextView
    private lateinit var apDescriptionDefaultInputWrapper: TextInputLayout
    private lateinit var apDescriptionListBtn: MaterialButton
    private lateinit var apDescriptionViewWrapper: TextInputLayout

    private lateinit var apTitleVoiceRecView: LinearLayout
    private lateinit var apTitleImageRecView: LinearLayout
    private lateinit var apTitleCameraRecView: LinearLayout
    private lateinit var apTitleDefaultValueMessage: MaterialTextView
    private lateinit var apTitleDefaultInputWrapper: TextInputLayout
    private lateinit var apTitleListBtn: MaterialButton
    private lateinit var apTitleSpinner: AppCompatSpinner
    private lateinit var apTitleViewWrapper: TextInputLayout

    var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != null && intent.action == "dialog-dismiss") {
                dismiss()
                listener.onSuccess("")
            } else if (intent.action != null && intent.action == "move-next") {
                apViewPager.currentItem = apViewPager.currentItem + 1
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction("dialog-dismiss")
        intentFilter.addAction("move-next")
        LocalBroadcastManager.getInstance(
            requireActivity()
        ).registerReceiver(broadcastReceiver, intentFilter)

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
                    if (!checkImageAlreadyExist(multiImagesList, tempImageList[i])) {
                        multiImagesList.add(tempImageList[i])
                        barcodeImageList.add(tempImageList[i])
                    }
                }
                Glide.with(requireActivity())
                    .load(barcodeImageList[barcodeImageList.size-1])
                    .placeholder(R.drawable.placeholder)
                    .centerInside()
                    .into(selectedImageView)
                imagesAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(
            broadcastReceiver
        );
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            R.style.FullScreenDialogStyle
        )
        appSettings = AppSettings(requireActivity())
//        insalesFragment = InsalesFragment()
        tableGenerator = TableGenerator(requireActivity())
        addProdcutViewModel = ViewModelProvider(this)[AddProductViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v =
            inflater.inflate(R.layout.insales_add_product_dialog, container)

        initViews(v)

        return v
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    private fun initViews(view: View) {
        categoriesSpinner =
            view.findViewById(R.id.ap_cate_spinner)
        apTitleView = view.findViewById(R.id.ap_title)
        apTestDataView = view.findViewById<MaterialTextView>(R.id.test_data_button)
        apDescriptionView =
            view.findViewById(R.id.ap_description)
        testDataBtn = view.findViewById(R.id.test_data_button1)
        quickModeCheckBox = view.findViewById(R.id.ap_quick_product_mode)
        apViewPager = view.findViewById(R.id.ap_viewpager)
        apFirstLayout = view.findViewById(R.id.ap_first_layout)
        apSecondLayout = view.findViewById(R.id.ap_second_layout)
        apPreviousBtn = view.findViewById(R.id.ap_previous_btn)
        apNextBtn = view.findViewById(R.id.ap_next_btn)
        apBackArrowBtn = view.findViewById(R.id.ap_back_arrow)
        apNextPreviousButtons = view.findViewById(R.id.ap_next_previous_buttons)
        getTitleBtn = view.findViewById(R.id.get_title_text_view)
        apTitleViewWrapper = view.findViewById<TextInputLayout>(R.id.ap_title_wrapper)
        apTitleDefaultInputWrapper =
            view.findViewById<TextInputLayout>(R.id.ap_title_non_changeable_default_text_input_wrapper)
        apDescriptionViewWrapper =
            view.findViewById<TextInputLayout>(R.id.ap_description_wrapper)
        apDescriptionDefaultInputWrapper =
            view.findViewById<TextInputLayout>(R.id.ap_description_non_changeable_default_text_input_wrapper)
        apQuantityViewWrapper = view.findViewById<TextInputLayout>(R.id.ap_quantity_wrapper)
        apQuantityVoiceRecView = view.findViewById<LinearLayout>(R.id.ap_quantity_voice_layout)
        apPriceVoiceRecView = view.findViewById<LinearLayout>(R.id.ap_price_voice_layout)
        apQuantityDefaultInputWrapper =
            view.findViewById<TextInputLayout>(R.id.ap_quantity_non_changeable_default_text_input_wrapper)
        apPriceViewWrapper = view.findViewById<TextInputLayout>(R.id.ap_price_wrapper)
        apPriceDefaultInputWrapper =
            view.findViewById<TextInputLayout>(R.id.ap_price_non_changeable_default_text_input_wrapper)


        apAddDescriptionView =
            view.findViewById<MaterialTextView>(R.id.ap_add_description_text_view)
        apQuantityView = view.findViewById<TextInputEditText>(R.id.ap_quantity)
        apPriceView = view.findViewById<TextInputEditText>(R.id.ap_price)
        apSubmitBtn = view.findViewById<MaterialButton>(R.id.ap_dialog_submit_btn)
        apCancelBtn = view.findViewById<MaterialButton>(R.id.ap_dialog_cancel_btn)

        apTitleSpinner = view.findViewById<AppCompatSpinner>(R.id.ap_title_options_spinner)
        apDescriptionSpinner =
            view.findViewById<AppCompatSpinner>(R.id.ap_description_options_spinner)
        apQuantitySpinner =
            view.findViewById<AppCompatSpinner>(R.id.ap_quantity_options_spinner)
        apPriceSpinner = view.findViewById<AppCompatSpinner>(R.id.ap_price_options_spinner)

        apTitleListBtn =
            view.findViewById<MaterialButton>(R.id.ap_title_list_with_fields_btn)
        apDescriptionListBtn =
            view.findViewById<MaterialButton>(R.id.ap_description_list_with_fields_btn)
        apQuantityListBtn =
            view.findViewById<MaterialButton>(R.id.ap_quantity_list_with_fields_btn)
        apPriceListBtn =
            view.findViewById<MaterialButton>(R.id.ap_price_list_with_fields_btn)

        apTitleDefaultInputBox =
            view.findViewById<TextInputEditText>(R.id.ap_title_non_changeable_default_text_input)
        apDescriptionDefaultInputBox =
            view.findViewById<TextInputEditText>(R.id.ap_description_non_changeable_default_text_input)
        apQuantityDefaultInputBox =
            view.findViewById<TextInputEditText>(R.id.ap_quantity_non_changeable_default_text_input)
        apPriceDefaultInputBox =
            view.findViewById<TextInputEditText>(R.id.ap_price_non_changeable_default_text_input)


        apTitleListSpinner = view.findViewById<AppCompatSpinner>(R.id.ap_title_list_spinner)
        apDescriptionListSpinner =
            view.findViewById<AppCompatSpinner>(R.id.ap_description_list_spinner)
        apQuantityListSpinner =
            view.findViewById<AppCompatSpinner>(R.id.ap_quantity_list_spinner)
        apPriceListSpinner = view.findViewById<AppCompatSpinner>(R.id.ap_price_list_spinner)

        apTitleActiveListNameView =
            view.findViewById<MaterialTextView>(R.id.ap_title_active_list_name)
        apDescriptionActiveListNameView =
            view.findViewById<MaterialTextView>(R.id.ap_description_active_list_name)
        apQuantityActiveListNameView =
            view.findViewById<MaterialTextView>(R.id.ap_quantity_active_list_name)
        apPriceActiveListNameView =
            view.findViewById<MaterialTextView>(R.id.ap_price_active_list_name)


        apTitleCameraRecView = view.findViewById<LinearLayout>(R.id.ap_title_camera_layout)
        apTitleImageRecView = view.findViewById<LinearLayout>(R.id.ap_title_images_layout)
        apTitleVoiceRecView = view.findViewById<LinearLayout>(R.id.ap_title_voice_layout)

        apDescriptionCameraRecView =
            view.findViewById<LinearLayout>(R.id.ap_description_camera_layout)
        apDescriptionImageRecView =
            view.findViewById<LinearLayout>(R.id.ap_description_images_layout)
        apDescriptionVoiceRecView =
            view.findViewById<LinearLayout>(R.id.ap_description_voice_layout)

        selectedImageView =
            view.findViewById(R.id.selected_insales_add_product_image_view)
        cameraImageView =
            view.findViewById<AppCompatImageView>(R.id.camera_image_view)
        imagesImageView =
            view.findViewById<AppCompatImageView>(R.id.images_image_view)
        internetImageView =
            view.findViewById<AppCompatImageView>(R.id.internet_image_view)


        imagesRecyclerView = view.findViewById(R.id.ap_images_recyclerview)
        imagesRecyclerView.layoutManager = LinearLayoutManager(
            requireActivity(), RecyclerView.HORIZONTAL,
            false
        )
        imagesRecyclerView.hasFixedSize()
        imagesAdapter = BarcodeImageAdapter(
            requireContext(),
            barcodeImageList as java.util.ArrayList<String>
        )
        imagesRecyclerView.adapter = imagesAdapter
        imagesAdapter.setOnItemClickListener(object :
            BarcodeImageAdapter.OnItemClickListener {
            override fun onItemDeleteClick(position: Int) {
//                            val image = barcodeImageList[position]
                val builder = MaterialAlertDialogBuilder(requireActivity())
                builder.setMessage(getString(R.string.delete_barcode_image_message))
                builder.setCancelable(false)
                builder.setNegativeButton(getString(R.string.no_text)) { dialog, which ->
                    dialog.dismiss()
                }
                builder.setPositiveButton(getString(R.string.yes_text)) { dialog, which ->
                    dialog.dismiss()
                    barcodeImageList.removeAt(position)
                    multiImagesList.removeAt(position)
                    imagesAdapter.notifyItemRemoved(position)
                    if (barcodeImageList.size == 0) {
                        Glide.with(requireActivity())
                            .load("")
                            .placeholder(R.drawable.placeholder)
                            .centerInside()
                            .into(selectedImageView)
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

        apTitleView.setOnTouchListener(View.OnTouchListener { v, event ->
            if (v.id == R.id.ap_title) {
                v.parent.requestDisallowInterceptTouchEvent(true)
                when (event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_UP -> v.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        })

        apTitleDefaultInputBox.setOnTouchListener(View.OnTouchListener { v, event ->
            if (v.id == R.id.ap_title_non_changeable_default_text_input) {
                v.parent.requestDisallowInterceptTouchEvent(true)
                when (event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_UP -> v.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        })

        apDescriptionView.setOnTouchListener(View.OnTouchListener { v, event ->
            if (v.id == R.id.ap_description) {
                v.parent.requestDisallowInterceptTouchEvent(true)
                when (event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_UP -> v.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        })

        apDescriptionDefaultInputBox.setOnTouchListener(View.OnTouchListener { v, event ->
            if (v.id == R.id.ap_description_non_changeable_default_text_input) {
                v.parent.requestDisallowInterceptTouchEvent(true)
                when (event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_UP -> v.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        })

        getTitleBtn.setOnClickListener {
            userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String

            if (userCurrentCredits.toFloat() >= 1.0) {

                launchActivity1.launch(
                    Intent(
                        requireActivity(),
                        RainForestApiActivity::class.java
                    )
                )
            } else {
                MaterialAlertDialogBuilder(requireActivity())
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


        val quickModeStatus = appSettings.getInt("QUICK_MODE_STATUS")
        if (quickModeStatus == 1) {
            quickModeCheckBox.isChecked = true
            apFirstLayout.visibility = View.GONE
            apSecondLayout.visibility = View.VISIBLE
            apNextPreviousButtons.visibility = View.VISIBLE
        } else {
            apSecondLayout.visibility = View.GONE
            apNextPreviousButtons.visibility = View.GONE
            apFirstLayout.visibility = View.VISIBLE
        }
        renderView()
        quickModeCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String
            if (isChecked) {

                if (userCurrentCredits.toFloat() > 0) {
                    appSettings.putInt("QUICK_MODE_STATUS", 1)
                    quickModeCheckBox.isChecked = true
                    apFirstLayout.visibility = View.GONE
                    apSecondLayout.visibility = View.VISIBLE
                    apNextPreviousButtons.visibility = View.VISIBLE
                    renderView()
                } else {
                    quickModeCheckBox.isChecked = false
                    MaterialAlertDialogBuilder(requireActivity())
                        .setMessage(getString(R.string.low_credites_error_message))
                        .setCancelable(false)
                        .setNegativeButton(getString(R.string.no_text)) { dialog, which ->
                            dialog.dismiss()
                        }
                        .setPositiveButton(getString(R.string.buy_credits)) { dialog, which ->
                            dialog.dismiss()
                            startActivity(
                                Intent(
                                    requireActivity(),
                                    UserScreenActivity::class.java
                                )
                            )
                        }
                        .create().show()
                }

            } else {
                appSettings.putInt("QUICK_MODE_STATUS", 0)
                apSecondLayout.visibility = View.GONE
                apNextPreviousButtons.visibility = View.GONE
                apFirstLayout.visibility = View.VISIBLE
                apTitleView.setText(appSettings.getString("AP_PRODUCT_TITLE"))
                apDescriptionView.setText(appSettings.getString("AP_PRODUCT_DESCRIPTION"))
                apQuantityView.setText(appSettings.getString("AP_PRODUCT_QUANTITY"))
                apPriceView.setText(appSettings.getString("AP_PRODUCT_PRICE"))
                renderView()
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
                            if (!checkImageAlreadyExist(multiImagesList, tempImageList[i])) {
                                multiImagesList.add(tempImageList[i])
                                barcodeImageList.add(tempImageList[i])
                            }
                        }
                        Glide.with(requireActivity())
                            .load(barcodeImageList[barcodeImageList.size-1])
                            .placeholder(R.drawable.placeholder)
                            .centerInside()
                            .into(selectedImageView)
                        imagesAdapter.notifyDataSetChanged()
                    }
                }
            }
        }

        apBackArrowBtn.setOnClickListener {
            dismiss()
        }


    }

    private fun renderView(){

        apViewPager.offscreenPageLimit = 7
        val fragmentAdapter = ViewPagerAdapter(childFragmentManager)
        fragmentAdapter.addFragment(ApScannerFragment(), "ap_scanner_fr")
        fragmentAdapter.addFragment(ApCategoryInputFragment(), "ap_category_fr")
        fragmentAdapter.addFragment(ApTitleInputFragment(), "ap_title_fr")
        fragmentAdapter.addFragment(ApDescriptionInputFragment(), "ap_description_fr")
        fragmentAdapter.addFragment(ApQuantityInputFragment(), "ap_quantity_fr")
        fragmentAdapter.addFragment(ApPriceInputFragment(), "ap_price_fr")
        fragmentAdapter.addFragment(ApImageUploadFragment(), "ap_image_fr")
        apViewPager.adapter = fragmentAdapter
        val currentPage = appSettings.getInt("CURRENT_PAGE")
        apViewPager.currentItem = currentPage

        testDataBtn.setOnClickListener {
            val currentFragment =
                childFragmentManager.getFragments().get(apViewPager.getCurrentItem())
            if (currentFragment is ApTitleInputFragment) {
                currentFragment.updateTestData("Test Title")
            } else if (currentFragment is ApDescriptionInputFragment) {
                currentFragment.updateTestData("Test Description")
            } else if (currentFragment is ApQuantityInputFragment) {
                currentFragment.updateTestData("1")
            } else if (currentFragment is ApPriceInputFragment) {
                currentFragment.updateTestData("1")
            }
        }

        apViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                appSettings.putInt("CURRENT_PAGE",position)
                if (position == 2 || position == 3) {
                    getTitleBtn.visibility = View.VISIBLE
                } else {
                    getTitleBtn.visibility = View.GONE
                }

                if (position == 0 || position == 1 || position == 6) {
                    testDataBtn.visibility = View.GONE
                } else {
                    testDataBtn.visibility = View.VISIBLE
                }
                if (position == 0) {
                    apPreviousBtn.visibility = View.INVISIBLE
                } else {
                    apPreviousBtn.visibility = View.VISIBLE
                }
                if (position < apViewPager.adapter!!.count - 1) {
                    apNextBtn.visibility = View.VISIBLE
                } else {
                    apNextBtn.visibility = View.INVISIBLE
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }

        })

        apPreviousBtn.setOnClickListener {
            apViewPager.setCurrentItem(apViewPager.currentItem - 1, true)
        }

        apNextBtn.setOnClickListener {
            apViewPager.setCurrentItem(apViewPager.currentItem + 1, true)
        }


        apTitleCameraRecView.setOnClickListener {
            Constants.CIVType = "ap_title"
            if (RuntimePermissionHelper.checkCameraPermission(
                    requireActivity(), Constants.CAMERA_PERMISSION
                )
            ) {
                BaseActivity.hideSoftKeyboard(requireActivity(), apTitleCameraRecView)
                pickImageFromCamera()
            }
        }
        apTitleImageRecView.setOnClickListener {
            Constants.CIVType = "ap_title"
            Constants.hint = "ap"
            if (RuntimePermissionHelper.checkCameraPermission(
                    requireActivity(),
                    Constants.READ_STORAGE_PERMISSION
                )
            ) {
                BaseActivity.hideSoftKeyboard(requireActivity(), apTitleImageRecView)
                pickImageFromGallery()
            }
        }
        apTitleVoiceRecView.setOnClickListener {
            Constants.CIVType = "ap_title"
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
                        voiceLanguageCode = if (parent!!.selectedItem.toString().toLowerCase(
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
            val builder = MaterialAlertDialogBuilder(requireActivity())
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


        apDescriptionCameraRecView.setOnClickListener {
            Constants.CIVType = "ap_description"

            if (RuntimePermissionHelper.checkCameraPermission(
                    requireActivity(), Constants.CAMERA_PERMISSION
                )
            ) {
                BaseActivity.hideSoftKeyboard(requireActivity(), apDescriptionCameraRecView)
                pickImageFromCamera()
            }
        }
        apDescriptionImageRecView.setOnClickListener {
            Constants.CIVType = "ap_description"
            Constants.hint = "ap"
            if (RuntimePermissionHelper.checkCameraPermission(
                    requireActivity(),
                    Constants.READ_STORAGE_PERMISSION
                )
            ) {
                BaseActivity.hideSoftKeyboard(requireActivity(), apDescriptionImageRecView)
                pickImageFromGallery()
            }
        }
        apDescriptionVoiceRecView.setOnClickListener {
            Constants.CIVType = "ap_description"
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
                        voiceLanguageCode = if (parent!!.selectedItem.toString().toLowerCase(
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
            val builder = MaterialAlertDialogBuilder(requireActivity())
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

        val apTitleSpinnerSelectedPosition =
            appSettings.getInt("AP_TITLE_SPINNER_SELECTED_POSITION")
        val apTitleDefaultValue = appSettings.getString("AP_TITLE_DEFAULT_VALUE")
        val apTitleListId = appSettings.getInt("AP_TITLE_LIST_ID")
        val apTitleActiveListName = appSettings.getString("AP_TITLE_LIST_NAME")
        if (apTitleActiveListName!!.isEmpty()) {
            apTitleActiveListNameView.text = "${resources.getString(R.string.active_list_text)}: None"
        } else {
            apTitleActiveListNameView.text = "${resources.getString(R.string.active_list_text)}: $apTitleActiveListName"
        }
        apTitleSpinner.setSelection(apTitleSpinnerSelectedPosition)


        apTitleListBtn.setOnClickListener {
            openListWithFieldsDialog("ap_title")
        }

        when (apTitleSpinnerSelectedPosition)
        {
            1 -> {
//                apTitleVoiceRecView.visibility = View.GONE
//                apTitleCameraRecView.visibility = View.GONE
//                apTitleImageRecView.visibility = View.GONE
//                apTitleListBtn.visibility = View.GONE
//                apTitleActiveListNameView.visibility = View.GONE
//                apTitleListSpinner.visibility = View.GONE
//                apTitleDefaultInputWrapper.visibility = View.VISIBLE
//                apTitleViewWrapper.visibility = View.VISIBLE
//                apTitleDefaultInputBox.setText(apTitleDefaultValue)
//                apTitleView.setText(apTitleDefaultValue)

                apTitleListBtn.visibility = View.GONE
                apTitleActiveListNameView.visibility = View.GONE
                apTitleListSpinner.visibility = View.GONE
                apTitleDefaultInputWrapper.visibility = View.GONE
                apTitleCameraRecView.visibility = View.GONE
                apTitleImageRecView.visibility = View.GONE
                apTitleViewWrapper.visibility = View.VISIBLE
                apTitleVoiceRecView.visibility = View.VISIBLE
            }
            2 -> {
                apTitleVoiceRecView.visibility = View.GONE
                apTitleCameraRecView.visibility = View.GONE
                apTitleImageRecView.visibility = View.GONE
                apTitleDefaultInputWrapper.visibility = View.GONE
                apTitleListBtn.visibility = View.VISIBLE
                apTitleActiveListNameView.visibility = View.VISIBLE
                apTitleViewWrapper.visibility = View.GONE
                apTitleListSpinner.visibility = View.VISIBLE
                val listOptions: String = tableGenerator.getListValues(apTitleListId)
                val listValues = listOptions.split(",")
                if (listValues.isNotEmpty()) {
                    appSettings.putString("AP_PRODUCT_TITLE", listValues[0])
                }
                val apTitleSpinnerAdapter = ArrayAdapter(
                    requireActivity(),
                    android.R.layout.simple_spinner_item,
                    listValues
                )
                apTitleSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                apTitleListSpinner.adapter = apTitleSpinnerAdapter

                apTitleListSpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {

                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {

                        }

                    }

            }
            3 -> {
                apTitleListBtn.visibility = View.GONE
                apTitleActiveListNameView.visibility = View.GONE
                apTitleListSpinner.visibility = View.GONE
                apTitleDefaultInputWrapper.visibility = View.GONE
                apTitleCameraRecView.visibility = View.GONE
                apTitleImageRecView.visibility = View.GONE
                apTitleViewWrapper.visibility = View.VISIBLE
                apTitleVoiceRecView.visibility = View.VISIBLE
            }
            4 -> {
                apTitleListBtn.visibility = View.GONE
                apTitleActiveListNameView.visibility = View.GONE
                apTitleListSpinner.visibility = View.GONE
                apTitleDefaultInputWrapper.visibility = View.GONE
                apTitleVoiceRecView.visibility = View.GONE
                apTitleImageRecView.visibility = View.GONE
                apTitleViewWrapper.visibility = View.VISIBLE
                apTitleCameraRecView.visibility = View.VISIBLE
            }
            5 -> {
                apTitleListBtn.visibility = View.GONE
                apTitleActiveListNameView.visibility = View.GONE
                apTitleListSpinner.visibility = View.GONE
                apTitleDefaultInputWrapper.visibility = View.GONE
                apTitleVoiceRecView.visibility = View.GONE
                apTitleCameraRecView.visibility = View.GONE
                apTitleViewWrapper.visibility = View.VISIBLE
                apTitleImageRecView.visibility = View.VISIBLE
            }
            else -> {
                apTitleVoiceRecView.visibility = View.GONE
                apTitleCameraRecView.visibility = View.GONE
                apTitleImageRecView.visibility = View.GONE
                apTitleListBtn.visibility = View.GONE
                apTitleActiveListNameView.visibility = View.GONE
                apTitleDefaultInputWrapper.visibility = View.GONE
                apTitleListSpinner.visibility = View.GONE
                apTitleViewWrapper.visibility = View.VISIBLE
            }
        }



        apTitleDefaultInputBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                apTitleView.setText(s.toString())
                appSettings.putString("AP_TITLE_DEFAULT_VALUE", s.toString())
                //appSettings.putString("AP_PRODUCT_TITLE", s.toString())
            }

        })

        apTitleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                appSettings.putInt("AP_TITLE_SPINNER_SELECTED_POSITION", position)

                if (position == 1) {
//                    apTitleVoiceRecView.visibility = View.GONE
//                    apTitleCameraRecView.visibility = View.GONE
//                    apTitleImageRecView.visibility = View.GONE
//                    apTitleListBtn.visibility = View.GONE
//                    apTitleActiveListNameView.visibility = View.GONE
//                    apTitleListSpinner.visibility = View.GONE
//                    apTitleDefaultInputWrapper.visibility = View.VISIBLE
//                    apTitleViewWrapper.visibility = View.VISIBLE
//                    if (apTitleDefaultValue!!.isNotEmpty()) {
//                        apTitleDefaultInputBox.setText(apTitleDefaultValue)
//                        apTitleView.setText(apTitleDefaultValue)
//                    } else {
//                        apTitleView.setText(appSettings.getString("AP_PRODUCT_TITLE"))
//                        apTitleView.setSelection(apTitleView.text.toString().length)
//                    }

                    apTitleListBtn.visibility = View.GONE
                    apTitleActiveListNameView.visibility = View.GONE
                    apTitleListSpinner.visibility = View.GONE
                    apTitleDefaultInputWrapper.visibility = View.GONE
                    apTitleCameraRecView.visibility = View.GONE
                    apTitleImageRecView.visibility = View.GONE
                    apTitleViewWrapper.visibility = View.VISIBLE
                    apTitleView.setText(appSettings.getString("AP_PRODUCT_TITLE"))
                    apTitleView.setSelection(apTitleView.text.toString().length)
                    apTitleVoiceRecView.visibility = View.VISIBLE
                } else if (position == 2) {
                    apTitleVoiceRecView.visibility = View.GONE
                    apTitleCameraRecView.visibility = View.GONE
                    apTitleImageRecView.visibility = View.GONE
                    apTitleDefaultInputWrapper.visibility = View.GONE
                    apTitleListBtn.visibility = View.VISIBLE
                    apTitleActiveListNameView.visibility = View.VISIBLE
                    apTitleViewWrapper.visibility = View.GONE
                    apTitleView.setText(appSettings.getString("AP_PRODUCT_TITLE"))
                    apTitleView.setSelection(apTitleView.text.toString().length)
                    apTitleListSpinner.visibility = View.VISIBLE
                    val listOptions: String = tableGenerator.getListValues(apTitleListId)
                    val listValues = listOptions.split(",")
                    if (listValues.isNotEmpty()) {
                        appSettings.putString("AP_PRODUCT_TITLE", listValues[0])
                    }
                    val apTitleSpinnerAdapter = ArrayAdapter(
                        requireActivity(),
                        android.R.layout.simple_spinner_item,
                        listValues
                    )
                    apTitleSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    apTitleListSpinner.adapter = apTitleSpinnerAdapter

                    apTitleListSpinner.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {

                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {

                            }

                        }

                } else if (position == 3) {
                    apTitleListBtn.visibility = View.GONE
                    apTitleActiveListNameView.visibility = View.GONE
                    apTitleListSpinner.visibility = View.GONE
                    apTitleDefaultInputWrapper.visibility = View.GONE
                    apTitleCameraRecView.visibility = View.GONE
                    apTitleImageRecView.visibility = View.GONE
                    apTitleViewWrapper.visibility = View.VISIBLE
                    apTitleView.setText(appSettings.getString("AP_PRODUCT_TITLE"))
                    apTitleView.setSelection(apTitleView.text.toString().length)
                    apTitleVoiceRecView.visibility = View.VISIBLE
                } else if (position == 4) {
                    apTitleListBtn.visibility = View.GONE
                    apTitleActiveListNameView.visibility = View.GONE
                    apTitleListSpinner.visibility = View.GONE
                    apTitleDefaultInputWrapper.visibility = View.GONE
                    apTitleVoiceRecView.visibility = View.GONE
                    apTitleImageRecView.visibility = View.GONE
                    apTitleViewWrapper.visibility = View.VISIBLE
                    apTitleView.setText(appSettings.getString("AP_PRODUCT_TITLE"))
                    apTitleView.setSelection(apTitleView.text.toString().length)
                    apTitleCameraRecView.visibility = View.VISIBLE
                } else if (position == 5) {
                    apTitleListBtn.visibility = View.GONE
                    apTitleActiveListNameView.visibility = View.GONE
                    apTitleListSpinner.visibility = View.GONE
                    apTitleDefaultInputWrapper.visibility = View.GONE
                    apTitleVoiceRecView.visibility = View.GONE
                    apTitleCameraRecView.visibility = View.GONE
                    apTitleViewWrapper.visibility = View.VISIBLE
                    apTitleView.setText(appSettings.getString("AP_PRODUCT_TITLE"))
                    apTitleView.setSelection(apTitleView.text.toString().length)
                    apTitleImageRecView.visibility = View.VISIBLE
                } else {
                    apTitleVoiceRecView.visibility = View.GONE
                    apTitleCameraRecView.visibility = View.GONE
                    apTitleImageRecView.visibility = View.GONE
                    apTitleListBtn.visibility = View.GONE
                    apTitleActiveListNameView.visibility = View.GONE
                    apTitleDefaultInputWrapper.visibility = View.GONE
                    apTitleListSpinner.visibility = View.GONE
                    apTitleViewWrapper.visibility = View.VISIBLE
                    apTitleView.setText(appSettings.getString("AP_PRODUCT_TITLE"))
                    apTitleView.setSelection(apTitleView.text.toString().length)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }

        apTitleView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                appSettings.putString("AP_PRODUCT_TITLE", s.toString())
            }

        })

        val apDescriptionSpinnerSelectedPosition =
            appSettings.getInt("AP_DESCRIPTION_SPINNER_SELECTED_POSITION")
        val apDescriptionDefaultValue = appSettings.getString("AP_DESCRIPTION_DEFAULT_VALUE")
        val apDescriptionListId = appSettings.getInt("AP_DESCRIPTION_LIST_ID")
        val apDescriptionActiveListName = appSettings.getString("AP_DESCRIPTION_LIST_NAME")
        if (apDescriptionActiveListName!!.isEmpty()) {
            apDescriptionActiveListNameView.text = "${resources.getString(R.string.active_list_text)}: None"
        } else {
            apDescriptionActiveListNameView.text = "${resources.getString(R.string.active_list_text)}: $apDescriptionActiveListName"
        }
        apDescriptionSpinner.setSelection(apDescriptionSpinnerSelectedPosition)
        apDescriptionListBtn.setOnClickListener {
            openListWithFieldsDialog("ap_description")
        }
        when (apDescriptionSpinnerSelectedPosition) {
            1 -> {
//                apDescriptionVoiceRecView.visibility = View.GONE
//                apDescriptionCameraRecView.visibility = View.GONE
//                apDescriptionImageRecView.visibility = View.GONE
//                apDescriptionListBtn.visibility = View.GONE
//                apDescriptionActiveListNameView.visibility = View.GONE
//                apDescriptionListSpinner.visibility = View.GONE
//                apDescriptionDefaultInputWrapper.visibility = View.VISIBLE
//                apDescriptionViewWrapper.visibility = View.VISIBLE
//                apDescriptionDefaultInputBox.setText(apDescriptionDefaultValue)
//                apDescriptionView.setText(apDescriptionDefaultValue)

                apDescriptionListBtn.visibility = View.GONE
                apDescriptionActiveListNameView.visibility = View.GONE
                apDescriptionListSpinner.visibility = View.GONE
                apDescriptionDefaultInputWrapper.visibility = View.GONE
                apDescriptionCameraRecView.visibility = View.GONE
                apDescriptionImageRecView.visibility = View.GONE
                apDescriptionViewWrapper.visibility = View.VISIBLE
                apDescriptionVoiceRecView.visibility = View.VISIBLE
            }
            2 -> {
                apDescriptionVoiceRecView.visibility = View.GONE
                apDescriptionCameraRecView.visibility = View.GONE
                apDescriptionImageRecView.visibility = View.GONE
                apDescriptionDefaultInputWrapper.visibility = View.GONE
                apDescriptionListBtn.visibility = View.VISIBLE
                apDescriptionActiveListNameView.visibility = View.VISIBLE
                apDescriptionViewWrapper.visibility = View.GONE
                apDescriptionListSpinner.visibility = View.VISIBLE
                val listOptions: String = tableGenerator.getListValues(apDescriptionListId)
                val listValues = listOptions.split(",")
                if (listValues.isNotEmpty()){
                    appSettings.putString("AP_PRODUCT_DESCRIPTION",listValues[0])
                }
                val apDescriptionSpinnerAdapter = ArrayAdapter(
                    requireActivity(),
                    android.R.layout.simple_spinner_item,
                    listValues
                )
                apDescriptionSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                apDescriptionListSpinner.adapter = apDescriptionSpinnerAdapter

                apDescriptionListSpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {

                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {

                        }

                    }

            }
            3 -> {
                apDescriptionListBtn.visibility = View.GONE
                apDescriptionActiveListNameView.visibility = View.GONE
                apDescriptionListSpinner.visibility = View.GONE
                apDescriptionDefaultInputWrapper.visibility = View.GONE
                apDescriptionCameraRecView.visibility = View.GONE
                apDescriptionImageRecView.visibility = View.GONE
                apDescriptionViewWrapper.visibility = View.VISIBLE
                apDescriptionVoiceRecView.visibility = View.VISIBLE
            }
            4 -> {
                apDescriptionListBtn.visibility = View.GONE
                apDescriptionActiveListNameView.visibility = View.GONE
                apDescriptionListSpinner.visibility = View.GONE
                apDescriptionDefaultInputWrapper.visibility = View.GONE
                apDescriptionVoiceRecView.visibility = View.GONE
                apDescriptionImageRecView.visibility = View.GONE
                apDescriptionViewWrapper.visibility = View.VISIBLE
                apDescriptionCameraRecView.visibility = View.VISIBLE
            }
            5 -> {
                apDescriptionListBtn.visibility = View.GONE
                apDescriptionActiveListNameView.visibility = View.GONE
                apDescriptionListSpinner.visibility = View.GONE
                apDescriptionDefaultInputWrapper.visibility = View.GONE
                apDescriptionVoiceRecView.visibility = View.GONE
                apDescriptionCameraRecView.visibility = View.GONE
                apDescriptionViewWrapper.visibility = View.VISIBLE
                apDescriptionImageRecView.visibility = View.VISIBLE
            }
            else -> {
                apDescriptionVoiceRecView.visibility = View.GONE
                apDescriptionCameraRecView.visibility = View.GONE
                apDescriptionImageRecView.visibility = View.GONE
                apDescriptionListBtn.visibility = View.GONE
                apDescriptionActiveListNameView.visibility = View.GONE
                apDescriptionDefaultInputWrapper.visibility = View.GONE
                apDescriptionListSpinner.visibility = View.GONE
                apDescriptionViewWrapper.visibility = View.VISIBLE
            }
        }

        apDescriptionDefaultInputBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                apDescriptionView.setText(s.toString())
                appSettings.putString("AP_DESCRIPTION_DEFAULT_VALUE", s.toString())
                //appSettings.putString("AP_PRODUCT_DESCRIPTION", s.toString())
            }

        })

        apDescriptionSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    appSettings.putInt("AP_DESCRIPTION_SPINNER_SELECTED_POSITION", position)
                    if (position == 1) {
//                        apDescriptionVoiceRecView.visibility = View.GONE
//                        apDescriptionCameraRecView.visibility = View.GONE
//                        apDescriptionImageRecView.visibility = View.GONE
//                        apDescriptionListBtn.visibility = View.GONE
//                        apDescriptionActiveListNameView.visibility = View.GONE
//                        apDescriptionListSpinner.visibility = View.GONE
//                        apDescriptionDefaultInputWrapper.visibility = View.VISIBLE
//                        apDescriptionViewWrapper.visibility = View.VISIBLE
//                        if (apDescriptionDefaultValue!!.isNotEmpty()) {
//                            apDescriptionDefaultInputBox.setText(apDescriptionDefaultValue)
//                            apDescriptionView.setText(apDescriptionDefaultValue)
//                        } else {
//                            apDescriptionView.setText(appSettings.getString("AP_PRODUCT_DESCRIPTION"))
//                            apDescriptionView.setSelection(apDescriptionView.text.toString().length)
//                        }

                        apDescriptionListBtn.visibility = View.GONE
                        apDescriptionActiveListNameView.visibility = View.GONE
                        apDescriptionListSpinner.visibility = View.GONE
                        apDescriptionDefaultInputWrapper.visibility = View.GONE
                        apDescriptionCameraRecView.visibility = View.GONE
                        apDescriptionImageRecView.visibility = View.GONE
                        apDescriptionViewWrapper.visibility = View.VISIBLE
                        apDescriptionView.setText(appSettings.getString("AP_PRODUCT_DESCRIPTION"))
                        apDescriptionView.setSelection(apDescriptionView.text.toString().length)
                        apDescriptionVoiceRecView.visibility = View.VISIBLE
                    } else if (position == 2) {
                        apDescriptionVoiceRecView.visibility = View.GONE
                        apDescriptionCameraRecView.visibility = View.GONE
                        apDescriptionImageRecView.visibility = View.GONE
                        apDescriptionDefaultInputWrapper.visibility = View.GONE
                        apDescriptionListBtn.visibility = View.VISIBLE
                        apDescriptionActiveListNameView.visibility = View.VISIBLE
                        apDescriptionViewWrapper.visibility = View.GONE
                        apDescriptionListSpinner.visibility = View.VISIBLE
                        apDescriptionView.setText(appSettings.getString("AP_PRODUCT_DESCRIPTION"))
                        apDescriptionView.setSelection(apDescriptionView.text.toString().length)
                        val listOptions: String = tableGenerator.getListValues(apDescriptionListId)
                        val listValues = listOptions.split(",")
                        if (listValues.isNotEmpty()){
                            appSettings.putString("AP_PRODUCT_DESCRIPTION",listValues[0])
                        }
                        val apDescriptionSpinnerAdapter = ArrayAdapter(
                            requireActivity(),
                            android.R.layout.simple_spinner_item,
                            listValues
                        )
                        apDescriptionSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        apDescriptionListSpinner.adapter = apDescriptionSpinnerAdapter

                        apDescriptionListSpinner.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(
                                    parent: AdapterView<*>?,
                                    view: View?,
                                    position: Int,
                                    id: Long
                                ) {

                                }

                                override fun onNothingSelected(parent: AdapterView<*>?) {

                                }

                            }

                    } else if (position == 3) {
                        apDescriptionListBtn.visibility = View.GONE
                        apDescriptionActiveListNameView.visibility = View.GONE
                        apDescriptionListSpinner.visibility = View.GONE
                        apDescriptionDefaultInputWrapper.visibility = View.GONE
                        apDescriptionCameraRecView.visibility = View.GONE
                        apDescriptionImageRecView.visibility = View.GONE
                        apDescriptionViewWrapper.visibility = View.VISIBLE
                        apDescriptionView.setText(appSettings.getString("AP_PRODUCT_DESCRIPTION"))
                        apDescriptionView.setSelection(apDescriptionView.text.toString().length)
                        apDescriptionVoiceRecView.visibility = View.VISIBLE
                    } else if (position == 4) {
                        apDescriptionListBtn.visibility = View.GONE
                        apDescriptionActiveListNameView.visibility = View.GONE
                        apDescriptionListSpinner.visibility = View.GONE
                        apDescriptionDefaultInputWrapper.visibility = View.GONE
                        apDescriptionVoiceRecView.visibility = View.GONE
                        apDescriptionImageRecView.visibility = View.GONE
                        apDescriptionViewWrapper.visibility = View.VISIBLE
                        apDescriptionView.setText(appSettings.getString("AP_PRODUCT_DESCRIPTION"))
                        apDescriptionView.setSelection(apDescriptionView.text.toString().length)
                        apDescriptionCameraRecView.visibility = View.VISIBLE
                    } else if (position == 5) {
                        apDescriptionListBtn.visibility = View.GONE
                        apDescriptionActiveListNameView.visibility = View.GONE
                        apDescriptionListSpinner.visibility = View.GONE
                        apDescriptionDefaultInputWrapper.visibility = View.GONE
                        apDescriptionVoiceRecView.visibility = View.GONE
                        apDescriptionCameraRecView.visibility = View.GONE
                        apDescriptionViewWrapper.visibility = View.VISIBLE
                        apDescriptionView.setText(appSettings.getString("AP_PRODUCT_DESCRIPTION"))
                        apDescriptionView.setSelection(apDescriptionView.text.toString().length)
                        apDescriptionImageRecView.visibility = View.VISIBLE
                    } else {
                        apDescriptionVoiceRecView.visibility = View.GONE
                        apDescriptionCameraRecView.visibility = View.GONE
                        apDescriptionImageRecView.visibility = View.GONE
                        apDescriptionListBtn.visibility = View.GONE
                        apDescriptionActiveListNameView.visibility = View.GONE
                        apDescriptionDefaultInputWrapper.visibility = View.GONE
                        apDescriptionListSpinner.visibility = View.GONE
                        apDescriptionViewWrapper.visibility = View.VISIBLE
                        apDescriptionView.setText(appSettings.getString("AP_PRODUCT_DESCRIPTION"))
                        apDescriptionView.setSelection(apDescriptionView.text.toString().length)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

            }

        apDescriptionView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                appSettings.putString("AP_PRODUCT_DESCRIPTION", s.toString())
            }

        })

        val apQuantitySpinnerSelectedPosition =
            appSettings.getInt("AP_QUANTITY_SPINNER_SELECTED_POSITION")
        var apQuantityDefaultValue = appSettings.getString("AP_QUANTITY_DEFAULT_VALUE")
        val apQuantityListId = appSettings.getInt("AP_QUANTITY_LIST_ID")
        val apQuantityActiveListName = appSettings.getString("AP_QUANTITY_LIST_NAME")
        if (apQuantityActiveListName!!.isEmpty()) {
            apQuantityActiveListNameView.text = "${resources.getString(R.string.active_list_text)}: None"
        } else {
            apQuantityActiveListNameView.text = "${resources.getString(R.string.active_list_text)}: $apQuantityActiveListName"
        }
        apQuantitySpinner.setSelection(apQuantitySpinnerSelectedPosition)
        apQuantityListBtn.setOnClickListener {
            openListWithFieldsDialog("ap_quantity")
        }


        apQuantityVoiceRecView.setOnClickListener {
            Constants.CIVType = "ap_quantity"
            voiceLanguageCode = appSettings.getString("VOICE_LANGUAGE_CODE") as String
            val voiceLayout = LayoutInflater.from(requireActivity()).inflate(R.layout.voice_language_setting_layout, null)
            val voiceLanguageSpinner = voiceLayout.findViewById<AppCompatSpinner>(R.id.voice_language_spinner)
            val voiceLanguageSaveBtn = voiceLayout.findViewById<MaterialButton>(R.id.voice_language_save_btn)

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
                        voiceLanguageCode = if (parent!!.selectedItem.toString().toLowerCase(Locale.ENGLISH).contains("english")){"en"}else{"ru"}
                        appSettings.putString("VOICE_LANGUAGE_CODE", voiceLanguageCode)

                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }

                }
            val builder = MaterialAlertDialogBuilder(requireActivity())
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

        if (apQuantitySpinnerSelectedPosition == 1) {
            apQuantityListSpinner.visibility = View.GONE
            apQuantityListBtn.visibility = View.GONE
            apQuantityActiveListNameView.visibility = View.GONE
            apQuantityDefaultInputWrapper.visibility = View.VISIBLE
            apQuantityViewWrapper.visibility = View.VISIBLE
            apQuantityVoiceRecView.visibility = View.GONE
            if (apQuantityDefaultValue!!.isNotEmpty()) {
                apQuantityDefaultInputBox.setText(apQuantityDefaultValue)
                apQuantityView.setText(apQuantityDefaultValue)
            } else {
                apQuantityView.setText(appSettings.getString("AP_PRODUCT_QUANTITY"))
                apQuantityView.setSelection(apQuantityView.text.toString().length)
            }
        }
        else if (apQuantitySpinnerSelectedPosition == 2)
        {
            apQuantityDefaultInputWrapper.visibility = View.GONE
            apQuantityListBtn.visibility = View.VISIBLE
            apQuantityActiveListNameView.visibility = View.VISIBLE
            apQuantityViewWrapper.visibility = View.GONE
            apQuantityListSpinner.visibility = View.VISIBLE
            apQuantityVoiceRecView.visibility = View.GONE
//            val listOptions: String = tableGenerator.getListValues(apQuantityListId)
//            val listValues = listOptions.split(",")
//            val apQuantitySpinnerAdapter = ArrayAdapter(
//                requireActivity(),
//                android.R.layout.simple_spinner_item,
//                listValues
//            )
            val listOptions: String = tableGenerator.getListValues(apQuantityListId)
            val listValues = listOptions.split(",")
            if (listValues.isNotEmpty()){
                appSettings.putString("AP_PRODUCT_QUANTITY",listValues[0])
            }
            val apQuantitySpinnerAdapter = ArrayAdapter(
                requireActivity(),
                android.R.layout.simple_spinner_item,
                listValues
            )
            apQuantitySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            apQuantityListSpinner.adapter = apQuantitySpinnerAdapter

            apQuantityListSpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {

                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }

                }
        }
        else if (apQuantitySpinnerSelectedPosition == 3){
            apQuantityViewWrapper.visibility = View.VISIBLE
            apQuantityListBtn.visibility = View.GONE
            apQuantityActiveListNameView.visibility = View.GONE
            apQuantityListSpinner.visibility = View.GONE
            apQuantityDefaultInputWrapper.visibility = View.GONE
            apQuantityVoiceRecView.visibility = View.VISIBLE
        }
        else {
            apQuantityViewWrapper.visibility = View.VISIBLE
            apQuantityListBtn.visibility = View.GONE
            apQuantityActiveListNameView.visibility = View.GONE
            apQuantityDefaultInputWrapper.visibility = View.GONE
            apQuantityListSpinner.visibility = View.GONE
            apQuantityVoiceRecView.visibility = View.GONE
        }

        apQuantityDefaultInputBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().substring(0) == "0") {
                    BaseActivity.showAlert(requireActivity(), "Default Quantity value should start from greater then 0!")
                    apQuantityDefaultInputBox.setText(s.toString().substring(1))
                } else {
                    apQuantityView.setText(s.toString())
                    appSettings.putString("AP_QUANTITY_DEFAULT_VALUE", s.toString())
                    appSettings.putString("AP_PRODUCT_QUANTITY", s.toString())
                }
            }

        })

        apQuantitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                appSettings.putInt("AP_QUANTITY_SPINNER_SELECTED_POSITION", position)
                if (position == 1) {
                    apQuantityListSpinner.visibility = View.GONE
                    apQuantityListBtn.visibility = View.GONE
                    apQuantityActiveListNameView.visibility = View.GONE
                    apQuantityDefaultInputWrapper.visibility = View.VISIBLE
                    apQuantityViewWrapper.visibility = View.VISIBLE
                    apQuantityVoiceRecView.visibility = View.GONE
                    apQuantityDefaultValue = appSettings.getString("AP_QUANTITY_DEFAULT_VALUE")
                    if (apQuantityDefaultValue!!.isNotEmpty()) {
                        apQuantityDefaultInputBox.setText(apQuantityDefaultValue)
                        apQuantityView.setText(apQuantityDefaultValue)
                    } else {
                        apQuantityView.setText(appSettings.getString("AP_PRODUCT_QUANTITY"))
                        apQuantityView.setSelection(apQuantityView.text.toString().length)
                    }
                }
                else if (position == 2)
                {
                    apQuantityDefaultInputWrapper.visibility = View.GONE
                    apQuantityListBtn.visibility = View.VISIBLE
                    apQuantityActiveListNameView.visibility = View.VISIBLE
                    apQuantityViewWrapper.visibility = View.GONE
                    apQuantityView.setText(appSettings.getString("AP_PRODUCT_QUANTITY"))
                    apQuantityView.setSelection(apQuantityView.text.toString().length)
                    apQuantityListSpinner.visibility = View.VISIBLE
                    apQuantityVoiceRecView.visibility = View.GONE
//                    val listOptions: String = tableGenerator.getListValues(apQuantityListId)
//                    val listValues = listOptions.split(",")
//                    val apQuantitySpinnerAdapter = ArrayAdapter(
//                        requireActivity(),
//                        android.R.layout.simple_spinner_item,
//                        listValues
//                    )
                    val listOptions: String = tableGenerator.getListValues(apQuantityListId)
                    val listValues = listOptions.split(",")
                    if (listValues.isNotEmpty()){
                        appSettings.putString("AP_PRODUCT_QUANTITY",listValues[0])
                    }
                    val apQuantitySpinnerAdapter = ArrayAdapter(
                        requireActivity(),
                        android.R.layout.simple_spinner_item,
                        listValues
                    )
                    apQuantitySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    apQuantityListSpinner.adapter = apQuantitySpinnerAdapter

                    apQuantityListSpinner.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {

                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {

                            }

                        }
                }
                else if (position == 3){
                    apQuantityViewWrapper.visibility = View.VISIBLE
                    apQuantityListBtn.visibility = View.GONE
                    apQuantityActiveListNameView.visibility = View.GONE
                    apQuantityListSpinner.visibility = View.GONE
                    apQuantityDefaultInputWrapper.visibility = View.GONE
                    apQuantityVoiceRecView.visibility = View.VISIBLE
                }
                else {
                    apQuantityViewWrapper.visibility = View.VISIBLE
                    apQuantityView.setText(appSettings.getString("AP_PRODUCT_QUANTITY"))
                    apQuantityView.setSelection(apQuantityView.text.toString().length)
                    apQuantityListBtn.visibility = View.GONE
                    apQuantityActiveListNameView.visibility = View.GONE
                    apQuantityDefaultInputWrapper.visibility = View.GONE
                    apQuantityListSpinner.visibility = View.GONE
                    apQuantityVoiceRecView.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }

        apQuantityView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().substring(0) == "0") {
                    BaseActivity.showAlert(requireActivity(), "Quantity value should start from greater then 0!")
                    apQuantityView.setText(s.toString().substring(1))
                } else {
                    appSettings.putString("AP_PRODUCT_QUANTITY", s.toString())
                }
            }

        })

        val apPriceSpinnerSelectedPosition =
            appSettings.getInt("AP_PRICE_SPINNER_SELECTED_POSITION")
        var apPriceDefaultValue = appSettings.getString("AP_PRICE_DEFAULT_VALUE")
        val apPriceListId = appSettings.getInt("AP_PRICE_LIST_ID")
        val apPriceActiveListName = appSettings.getString("AP_PRICE_LIST_NAME")
        if (apPriceActiveListName!!.isEmpty()) {
            apPriceActiveListNameView.text = "${resources.getString(R.string.active_list_text)}: None"
        } else {
            apPriceActiveListNameView.text = "${resources.getString(R.string.active_list_text)}: $apPriceActiveListName"
        }
        apPriceSpinner.setSelection(apPriceSpinnerSelectedPosition)
        apPriceListBtn.setOnClickListener {
            openListWithFieldsDialog("ap_price")
        }

        apPriceVoiceRecView.setOnClickListener {
            Constants.CIVType = "ap_price"
            voiceLanguageCode = appSettings.getString("VOICE_LANGUAGE_CODE") as String
            val voiceLayout = LayoutInflater.from(requireActivity()).inflate(R.layout.voice_language_setting_layout, null)
            val voiceLanguageSpinner = voiceLayout.findViewById<AppCompatSpinner>(R.id.voice_language_spinner)
            val voiceLanguageSaveBtn = voiceLayout.findViewById<MaterialButton>(R.id.voice_language_save_btn)

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
                        voiceLanguageCode = if (parent!!.selectedItem.toString().toLowerCase(Locale.ENGLISH).contains("english")){"en"}else{"ru"}
                        appSettings.putString("VOICE_LANGUAGE_CODE", voiceLanguageCode)

                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }

                }
            val builder = MaterialAlertDialogBuilder(requireActivity())
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

        if (apPriceSpinnerSelectedPosition == 1) {
            apPriceListSpinner.visibility = View.GONE
            apPriceListBtn.visibility = View.GONE
            apPriceActiveListNameView.visibility = View.GONE
            apPriceDefaultInputWrapper.visibility = View.VISIBLE
            apPriceViewWrapper.visibility = View.VISIBLE
            apPriceVoiceRecView.visibility = View.GONE
            if (apPriceDefaultValue!!.isNotEmpty()) {
                apPriceDefaultInputBox.setText(apPriceDefaultValue)
                apPriceView.setText(apPriceDefaultValue)
            } else {
                apPriceView.setText(appSettings.getString("AP_PRODUCT_PRICE"))
                apPriceView.setSelection(apPriceView.text.toString().length)
            }
        }
        else if (apPriceSpinnerSelectedPosition == 2)
        {
            apPriceDefaultInputWrapper.visibility = View.GONE
            apPriceListBtn.visibility = View.VISIBLE
            apPriceActiveListNameView.visibility = View.VISIBLE
            apPriceViewWrapper.visibility = View.GONE
            apPriceListSpinner.visibility = View.VISIBLE
            apPriceVoiceRecView.visibility = View.GONE
//            val listOptions: String = tableGenerator.getListValues(apPriceListId)
//            val listValues = listOptions.split(",")
//            val apPriceSpinnerAdapter = ArrayAdapter(
//                requireActivity(),
//                android.R.layout.simple_spinner_item,
//                listValues
//            )
            val listOptions: String = tableGenerator.getListValues(apPriceListId)
            val listValues = listOptions.split(",")
            if (listValues.isNotEmpty()){
                appSettings.putString("AP_PRODUCT_PRICE",listValues[0])
            }
            val apPriceSpinnerAdapter = ArrayAdapter(
                requireActivity(),
                android.R.layout.simple_spinner_item,
                listValues
            )
            apPriceSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            apPriceListSpinner.adapter = apPriceSpinnerAdapter

            apPriceListSpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {

                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }

                }
        }
        else if (apPriceSpinnerSelectedPosition == 3){
            apPriceViewWrapper.visibility = View.VISIBLE
            apPriceListBtn.visibility = View.GONE
            apPriceActiveListNameView.visibility = View.GONE
            apPriceListSpinner.visibility = View.GONE
            apPriceDefaultInputWrapper.visibility = View.GONE
            apPriceVoiceRecView.visibility = View.VISIBLE
        }
        else {
            apPriceViewWrapper.visibility = View.VISIBLE
            apPriceListBtn.visibility = View.GONE
            apPriceActiveListNameView.visibility = View.GONE
            apPriceDefaultInputWrapper.visibility = View.GONE
            apPriceListSpinner.visibility = View.GONE
            apPriceVoiceRecView.visibility = View.GONE
        }

        apPriceDefaultInputBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                apPriceView.setText(s.toString())
                appSettings.putString("AP_PRICE_DEFAULT_VALUE", s.toString())
                appSettings.putString("AP_PRODUCT_PRICE", s.toString())
            }

        })

        apPriceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                appSettings.putInt("AP_PRICE_SPINNER_SELECTED_POSITION", position)
                if (position == 1) {
                    apPriceListSpinner.visibility = View.GONE
                    apPriceListBtn.visibility = View.GONE
                    apPriceDefaultInputWrapper.visibility = View.VISIBLE
                    apPriceActiveListNameView.visibility = View.GONE
                    apPriceViewWrapper.visibility = View.VISIBLE
                    apPriceVoiceRecView.visibility = View.GONE
                    apPriceDefaultValue = appSettings.getString("AP_PRICE_DEFAULT_VALUE")
                    if (apPriceDefaultValue!!.isNotEmpty()) {
                        apPriceDefaultInputBox.setText(apPriceDefaultValue)
                        apPriceView.setText(apPriceDefaultValue)
                    } else {
                        apPriceView.setText(appSettings.getString("AP_PRODUCT_PRICE"))
                        apPriceView.setSelection(apPriceView.text.toString().length)
                    }
                }
                else if (position == 2) {
                    apPriceDefaultInputWrapper.visibility = View.GONE
                    apPriceListBtn.visibility = View.VISIBLE
                    apPriceActiveListNameView.visibility = View.VISIBLE
                    apPriceViewWrapper.visibility = View.GONE
                    apPriceView.setText(appSettings.getString("AP_PRODUCT_PRICE"))
                    apPriceView.setSelection(apPriceView.text.toString().length)
                    apPriceListSpinner.visibility = View.VISIBLE
                    apPriceVoiceRecView.visibility = View.GONE
//                    val listOptions: String = tableGenerator.getListValues(apPriceListId)
//                    val listValues = listOptions.split(",")
//                    val apPriceSpinnerAdapter = ArrayAdapter(
//                        requireActivity(),
//                        android.R.layout.simple_spinner_item,
//                        listValues
//                    )
                    val listOptions: String = tableGenerator.getListValues(apPriceListId)
                    val listValues = listOptions.split(",")
                    if (listValues.isNotEmpty()){
                        appSettings.putString("AP_PRODUCT_PRICE",listValues[0])
                    }
                    val apPriceSpinnerAdapter = ArrayAdapter(
                        requireActivity(),
                        android.R.layout.simple_spinner_item,
                        listValues
                    )
                    apPriceSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    apPriceListSpinner.adapter = apPriceSpinnerAdapter

                    apPriceListSpinner.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {

                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {

                            }

                        }
                }
                else if (position == 3){
                    apPriceViewWrapper.visibility = View.VISIBLE
                    apPriceListBtn.visibility = View.GONE
                    apPriceActiveListNameView.visibility = View.GONE
                    apPriceListSpinner.visibility = View.GONE
                    apPriceDefaultInputWrapper.visibility = View.GONE
                    apPriceVoiceRecView.visibility = View.VISIBLE
                }
                else {
                    apPriceViewWrapper.visibility = View.VISIBLE
                    apPriceView.setText(appSettings.getString("AP_PRODUCT_PRICE"))
                    apPriceView.setSelection(apPriceView.text.toString().length)
                    apPriceListBtn.visibility = View.GONE
                    apPriceActiveListNameView.visibility = View.GONE
                    apPriceDefaultInputWrapper.visibility = View.GONE
                    apPriceListSpinner.visibility = View.GONE
                    apPriceVoiceRecView.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }

        apPriceView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                appSettings.putString("AP_PRODUCT_PRICE", s.toString())
            }

        })


        cameraImageView.setOnClickListener {
            intentType = 1
            if (RuntimePermissionHelper.checkCameraPermission(
                    requireActivity(),
                    Constants.CAMERA_PERMISSION
                )
            ) {
                //dispatchTakePictureIntent()
                val cameraIntent =
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraResultLauncher.launch(cameraIntent)
            }
        }

        imagesImageView.setOnClickListener {
            intentType = 2
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
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

        internetImageView.setOnClickListener {
            intentType = 3
            val tempImageList = mutableListOf<String>()
            searchedImagesList.clear()
            val internetSearchLayout = LayoutInflater.from(context)
                .inflate(R.layout.internet_image_search_dialog_layout, null)
            loader =
                internetSearchLayout.findViewById<ProgressBar>(R.id.image_loader_view)
            searchBoxView =
                internetSearchLayout.findViewById<TextInputEditText>(R.id.text_input_field)
            searchBtnView =
                internetSearchLayout.findViewById<ImageButton>(R.id.internet_image_search_btn)
            internetImageRecyclerView =
                internetSearchLayout.findViewById<RecyclerView>(R.id.internet_search_image_recyclerview)
            val closeBtn =
                internetSearchLayout.findViewById<AppCompatImageView>(R.id.search_image_dialog_close)
            voiceSearchIcon =
                internetSearchLayout.findViewById(R.id.voice_search_internet_images)
            val barcodeImage = internetSearchLayout.findViewById<AppCompatImageView>(
                R.id
                    .barcode_img_search_internet_images
            )
            internetImageDoneBtn = internetSearchLayout.findViewById(R.id.iisdl_dialog_done_btn)
            val builder = MaterialAlertDialogBuilder(requireActivity())
            builder.setCancelable(false)
            builder.setView(internetSearchLayout)
            val iAlert = builder.create()
            iAlert.show()

            internetImageDoneBtn.setOnClickListener {
                iAlert.dismiss()
            }

            barcodeImage.setOnClickListener {
                val intent = Intent(requireActivity(), BarcodeReaderActivity::class.java)
                barcodeImageResultLauncher.launch(intent)
            }

            closeBtn.setOnClickListener {
                iAlert.dismiss()
            }

            internetImageRecyclerView.layoutManager = StaggeredGridLayoutManager(
                2,
                LinearLayoutManager.VERTICAL
            )//GridLayoutManager(context, 2)
            internetImageRecyclerView.hasFixedSize()
            internetImageAdapter = InternetImageAdapter(
                requireActivity(),
                searchedImagesList as java.util.ArrayList<String>
            )
            internetImageRecyclerView.adapter = internetImageAdapter
            internetImageAdapter.setOnItemClickListener(object :
                InternetImageAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    val selectedImage = searchedImagesList[position]
                    FullImageFragment(selectedImage).show(
                        childFragmentManager,
                        "full-image-dialog"
                    )
                }

                override fun onItemAttachClick(btn: MaterialButton, position: Int) {
                    //iAlert.dismiss()
                    selectedInternetImage = searchedImagesList[position]
                    Glide.with(requireActivity())
                        .load(selectedInternetImage)
                        .thumbnail(
                            Glide.with(requireActivity()).load(R.drawable.placeholder)
                        )
                        .fitCenter()
                        .into(selectedImageView)
                    if (btn.tag.toString().lowercase() == "attach") {
                        barcodeImageList.add(selectedInternetImage)
                        multiImagesList.add(selectedInternetImage)
                        btn.text = requireActivity().resources.getString(R.string.attached_text)
                        btn.tag = "attached"
                        btn.setBackgroundColor(
                            ContextCompat.getColor(
                                requireActivity(),
                                R.color.dark_gray
                            )
                        )
                    } else {
                        btn.text = requireActivity().resources.getString(R.string.attach_text)
                        btn.tag = "attach"
                        btn.setBackgroundColor(
                            ContextCompat.getColor(
                                requireActivity(),
                                R.color.primary_positive_color
                            )
                        )
                        barcodeImageList.remove(selectedInternetImage)
                        multiImagesList.remove(selectedInternetImage)
                    }
                    imagesAdapter.notifyDataSetChanged()
                    if (multiImagesList.isEmpty()){
                        Glide.with(requireActivity())
                            .load("")
                            .thumbnail(
                                Glide.with(requireActivity()).load(R.drawable.placeholder)
                            )
                            .fitCenter()
                            .into(selectedImageView)
                    }
                }

            })

            voiceSearchIcon.setOnClickListener {
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
                val builder = MaterialAlertDialogBuilder(requireActivity())
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
                    voiceResultLauncher1.launch(intent)
                }
            }

            searchBoxView.setOnEditorActionListener(object : TextView.OnEditorActionListener {
                override fun onEditorAction(
                    v: TextView?,
                    actionId: Int,
                    event: KeyEvent?
                ): Boolean {
                    startSearch(
                        searchBoxView,
                        searchBtnView,
                        loader,
                        searchedImagesList,
                        internetImageAdapter
                    )
                    return false
                }

            })

            searchBtnView.setOnClickListener {
                startSearch(
                    searchBoxView,
                    searchBtnView,
                    loader,
                    searchedImagesList,
                    internetImageAdapter
                )
            }

        }

        apAddDescriptionView.setOnClickListener {
            userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String

            if (userCurrentCredits.toFloat() >= 1.0) {

                launchActivity.launch(
                    Intent(
                        requireActivity(),
                        RainForestApiActivity::class.java
                    )
                )
            } else {
                MaterialAlertDialogBuilder(requireActivity())
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

        val cateSpinnerAdapter = ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_spinner_item,
            originalCategoriesList
        )
        cateSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categoriesSpinner.adapter = cateSpinnerAdapter

        categoriesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = originalCategoriesList[position]
                selectedCategoryId = selectedItem.id
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }
        getCategories(cateSpinnerAdapter)

        apTestDataView.setOnClickListener {
            apTitleView.setText(requireActivity().resources.getString(R.string.test_text))
            apDescriptionView.setText(requireActivity().resources.getString(R.string.test_text))
            apPriceView.setText("1")
            apQuantityView.setText("1")
        }

        apCancelBtn.setOnClickListener {
            dismiss()
        }

        apSubmitBtn.setOnClickListener {

            if (addProductValidation(
                    categoriesSpinner,
                    apTitleView,
                    apTitleSpinnerSelectedPosition,
                    apQuantityView,
                    apQuantitySpinnerSelectedPosition,
                    apPriceView,
                    apPriceSpinnerSelectedPosition
                )
            ) {
                BaseActivity.startLoading(requireActivity())
                finalTitleText = if (apTitleSpinnerSelectedPosition == 1) {
                    apTitleView.text.toString().toString()
                } else if (apTitleSpinnerSelectedPosition == 2) {
                    apTitleListSpinner.selectedItem.toString().trim()
                } else {
                    apTitleView.text.toString().trim()
                }

                finalDescriptionText = if (apDescriptionSpinnerSelectedPosition == 1) {
                    apDescriptionView.text.toString().toString()
                } else if (apDescriptionSpinnerSelectedPosition == 2) {
                    apDescriptionListSpinner.selectedItem.toString().trim()
                } else {
                    apDescriptionView.text.toString().trim()
                }

                finalQuantityText = if (apQuantitySpinnerSelectedPosition == 1) {
                    apQuantityDefaultInputBox.text.toString().toString()
                } else if (apQuantitySpinnerSelectedPosition == 2) {
                    apQuantityListSpinner.selectedItem.toString().trim()
                } else {
                    apQuantityView.text.toString().trim()
                }

                finalPriceText = if (apPriceSpinnerSelectedPosition == 1) {
                    apPriceDefaultInputBox.text.toString().toString()
                } else if (apPriceSpinnerSelectedPosition == 2) {
                    apPriceListSpinner.selectedItem.toString().trim()
                } else {
                    apPriceView.text.toString().trim()
                }


                viewModel.callAddProduct(
                    requireActivity(),
                    shopName,
                    email,
                    password,
                    selectedCategoryId,
                    finalTitleText,
                    finalDescriptionText,
                    finalQuantityText,
                    finalPriceText,
                    ""
                )
                viewModel.getAddProductResponse()
                    .observe(requireActivity(), Observer { response ->
                        if (response != null) {
                            if (response.get("status").asString == "200") {
                                val details = response.getAsJsonObject("details")
                                val productId = details.get("id").asInt

                                if (multiImagesList.isNotEmpty()) {
                                    BaseActivity.dismiss()
                                    Constants.startImageUploadService(productId, multiImagesList.joinToString(","), "add_image", true)
                                    Constants.multiImagesSelectedListSize = multiImagesList.size
                                    resetFieldValues()
                                    dismiss()
                                    listener.onSuccess("")
                                } else {
                                    Constants.multiImagesSelectedListSize = 0
                                    Handler(Looper.myLooper()!!).postDelayed({
                                        BaseActivity.dismiss()
                                        resetFieldValues()
                                        dismiss()
                                        listener.onSuccess("")
                                    }, 3000)
                                }

                            } else {
                                BaseActivity.dismiss()
                                BaseActivity.showAlert(
                                    requireActivity(),
                                    response.get("message").asString
                                )
                            }
                        } else {
                            BaseActivity.dismiss()
                        }
                    })
            }
        }
    }

    private fun checkImageAlreadyExist(list: List<String>, source: String): Boolean {
        var isFound = false
        for (i in 0 until list.size) {
            if (source.equals(list[i])) {
                isFound = true
                break
            } else {
                isFound = false
            }
        }
        return isFound
    }

    private fun resetFieldValues() {
        appSettings.remove("AP_BARCODE_ID")
        appSettings.remove("AP_PRODUCT_CATEGORY")
        appSettings.remove("AP_PRODUCT_TITLE")
        appSettings.remove("AP_PRODUCT_DESCRIPTION")
        appSettings.remove("AP_PRODUCT_QUANTITY")
        appSettings.remove("AP_PRODUCT_PRICE")
        barcodeImageList.clear()
    }

    var index = 0
    private fun uploadImages(
        productId: Int,
        listImages: List<String>,
        responseListener: ResponseListener
    ) {

        var imageType = ""
        val imageFile = listImages[index]
        if (imageFile.contains("http")) {
            imageType = "src"
            selectedInternetImage = imageFile
        } else {
            imageType = "attachment"
            selectedImageBase64String = ImageManager.convertImageToBase64(
                requireActivity(),
                imageFile
            )
        }

        viewModel.callAddProductImage(
            requireActivity(),
            shopName,
            email,
            password,
            selectedImageBase64String,
            productId,
            "${System.currentTimeMillis()}.jpg",
            if (imageType == "attachment") {
                ""
            } else {
                selectedInternetImage
            }
        )
        viewModel.getAddProductImageResponse()
            .observe(
                requireActivity(),
                Observer { response ->

                    if (response != null) {
//                            if (response.get("status").asString == "200") {
                        selectedImageBase64String = ""
                        selectedInternetImage = ""

                        if (index == listImages.size - 1) {
                            index = 0
                            responseListener.onSuccess("success")
                        } else {
                            index++
                            uploadImages(productId, listImages, responseListener)
                        }
//                            } else {
//                                BaseActivity.dismiss()
//                                BaseActivity.showAlert(
//                                    requireActivity(),
//                                    response.get("message").asString
//                                )
//                            }
                    } else {
                        BaseActivity.dismiss()
                        BaseActivity.showAlert(
                            requireActivity(),
                            getString(R.string.something_wrong_error)
                        )
                    }
                })
    }

    fun pickImageFromGallery() {
        val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher1.launch(
            Intent.createChooser(
                pickPhoto, getString(R.string.choose_image_gallery)
            )
        )
    }

    private var resultLauncher1 =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val cropPicUri = CropImage.getPickImageResultUri(requireActivity(), data)
                cropImage(cropPicUri)
            }
        }

    private var cameraResultLauncher1 =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // THIS LINE OF CODE WILL CHECK THE IMAGE HAS BEEN SELECTED OR NOT
            if (result.resultCode == Activity.RESULT_OK) {
                val text = result.data!!.getStringExtra("SCAN_TEXT")
                if (Constants.CIVType == "ap_title") {
                    val currentPItemTitle = apTitleView.text.toString().trim()
                    val stringBuilder = java.lang.StringBuilder()
                    stringBuilder.append(currentPItemTitle)
                    stringBuilder.append(text)
                    apTitleView.setText(stringBuilder.toString())
                } else {
                    val currentPItemTitle = apDescriptionView.text.toString().trim()
                    val stringBuilder = java.lang.StringBuilder()
                    stringBuilder.append(currentPItemTitle)
                    stringBuilder.append(text)
                    apDescriptionView.setText(stringBuilder.toString())
                }
            }
        }

    private var voiceResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // THIS LINE OF CODE WILL CHECK THE IMAGE HAS BEEN SELECTED OR NOT
            if (result.resultCode == Activity.RESULT_OK) {
                var spokenText: String =
                    result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        .let { results ->
                            results!!.get(0)
                        }
                if (Constants.CIVType == "ap_title") {
                    val currentPItemTitle = apTitleView.text.toString().trim()
                    val stringBuilder = java.lang.StringBuilder()
                    stringBuilder.append(currentPItemTitle)
                    spokenText = spokenText.capitalized()
                    stringBuilder.append("$spokenText. ")
                    apTitleView.setText(stringBuilder.toString())
                    apTitleView.setSelection(apTitleView.text.toString().length)
                    apTitleView.requestFocus()
                    //BaseActivity.showSoftKeyboard(requireActivity(),apTitleView)

                } else if (Constants.CIVType == "ap_description"){
                    val currentPItemTitle = apDescriptionView.text.toString().trim()
                    val stringBuilder = java.lang.StringBuilder()
                    stringBuilder.append(currentPItemTitle)
                    spokenText = spokenText.capitalized()
                    stringBuilder.append("$spokenText. ")
                    apDescriptionView.setText(stringBuilder.toString())
                    apDescriptionView.setSelection(apDescriptionView.text.toString().length)
                    apDescriptionView.requestFocus()
                    //BaseActivity.showSoftKeyboard(requireActivity(),apDescriptionView)

                } else if (Constants.CIVType == "ap_quantity"){
                    apQuantityView.setText(spokenText)
                } else if (Constants.CIVType == "ap_price"){
                    apPriceView.setText(spokenText)
                }
            }
        }

    fun pickImageFromCamera() {
        val takePictureIntent = Intent(context, OcrActivity::class.java)
        cameraResultLauncher1.launch(takePictureIntent)
    }

    private fun cropImage(imageUri: Uri) {

        CropImage.activity(imageUri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setMultiTouchEnabled(true)
            .start(requireActivity())
    }

    private var voiceResultLauncher1 =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // THIS LINE OF CODE WILL CHECK THE IMAGE HAS BEEN SELECTED OR NOT
            if (result.resultCode == Activity.RESULT_OK) {
                val spokenText: String =
                    result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        .let { results ->
                            results!![0]
                        }

                searchBoxView.setText(spokenText)
                Constants.hideKeyboar(requireActivity())
                startSearch(
                    searchBoxView, searchBtnView, loader,
                    searchedImagesList as java.util.ArrayList<String>, internetImageAdapter
                )
            }
        }

    private var barcodeImageResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // THIS LINE OF CODE WILL CHECK THE IMAGE HAS BEEN SELECTED OR NOT
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data != null && result.data!!.hasExtra("SCANNED_BARCODE_VALUE")) {
                    val barcodeId =
                        result.data!!.getStringExtra("SCANNED_BARCODE_VALUE") as String
                    if (barcodeId.isNotEmpty()) {
                        searchBoxView.setText(barcodeId)
                        Constants.hideKeyboar(requireActivity())
                        startSearch(
                            searchBoxView,
                            searchBtnView,
                            loader,
                            searchedImagesList as java.util.ArrayList<String>,
                            internetImageAdapter
                        )
                    }
                }


            }
        }

    private fun startSearch(
        searchBoxView: TextInputEditText,
        searchBtnView: ImageButton,
        loader: ProgressBar,
        searchedImagesList: java.util.ArrayList<String>,
        internetImageAdapter: InternetImageAdapter
    ) {
        var creditChargePrice: Double = 0.0
        var chargeCredits: Double = 0.0
        if (searchBoxView.text.toString().trim().isNotEmpty()) {


            val firebaseDatabase = FirebaseDatabase.getInstance().reference
            firebaseDatabase.child("SearchImagesLimit")
                .addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val creditPrice = snapshot.child("credits")
                            .getValue(Double::class.java) as Double
                        val images = snapshot.child("images")
                            .getValue(Int::class.java) as Int
                        creditChargePrice = creditPrice / images

                        userCurrentCredits =
                            appSettings.getString(Constants.userCreditsValue) as String

                        if (userCurrentCredits.isNotEmpty() && (userCurrentCredits != "0" || userCurrentCredits != "0.0") && userCurrentCredits.toFloat() >= creditChargePrice) {
                            BaseActivity.hideSoftKeyboard(
                                requireActivity(),
                                searchBtnView
                            )
                            //Constants.hideKeyboar(requireActivity())
                            val query = searchBoxView.text.toString().trim()
                            requireActivity().runOnUiThread {
                                loader.visibility = View.VISIBLE
                            }

                            BaseActivity.searchInternetImages(
                                requireActivity(),
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
                                            chargeCredits = creditChargePrice * searchedImagesList.size
                                            internetImageAdapter.notifyItemRangeChanged(
                                                0,
                                                searchedImagesList.size
                                            )
                                            internetImageDoneBtn.visibility = View.VISIBLE
                                        }
                                        //userCurrentCredits = appSettings.getString(Constants.userCreditsValue) as String
                                        val hashMap = HashMap<String, Any>()
                                        val remaining =
                                            userCurrentCredits.toFloat() - chargeCredits
                                        Log.d("TEST199", "$remaining")
                                        hashMap["credits"] =
                                            remaining.toString()
                                        firebaseDatabase.child(Constants.firebaseUserCredits)
                                            .child(Constants.firebaseUserId)
                                            .updateChildren(hashMap)
                                            .addOnSuccessListener {
                                                BaseActivity.getUserCredits(
                                                    requireActivity()
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

                                        BaseActivity.showAlert(
                                            requireActivity(),
                                            error.localizedMessage!!
                                        )
                                    }

                                })
                        } else {
                            MaterialAlertDialogBuilder(requireActivity())
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

                    override fun onCancelled(error: DatabaseError) {

                    }

                })


        } else {
            if (loader.visibility == View.VISIBLE) {
                loader.visibility = View.INVISIBLE
            }

            BaseActivity.showAlert(
                requireActivity(),
                getString(R.string.empty_text_error)
            )
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (!quickModeCheckBox.isChecked && requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            val result = CropImage.getActivityResult(data)
            val imgUri = result.uri
            try {
                TextRecogniser.runTextRecognition(
                    requireActivity(), if (Constants.CIVType == "ap_title") {
                        apTitleView
                    } else {
                        apDescriptionView
                    }, imgUri
                )
                Constants.hint = "default"
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
            for (fragment in childFragmentManager.fragments) {
                fragment.onActivityResult(requestCode, resultCode, data)
            }
        }

        //super.onActivityResult(requestCode, resultCode, data)
    }

    //private lateinit var tableGenerator: TableGenerator
    private lateinit var adapter: FieldListsAdapter
    private var listId: Int? = null
    private fun openListWithFieldsDialog(fieldType: String) {

        val listItems = mutableListOf<ListItem>()
        val layout =
            LayoutInflater.from(context).inflate(
                R.layout.list_with_fields_value_layout,
                null
            )
        val listWithFieldsValueRecyclerView =
            layout.findViewById<RecyclerView>(R.id.list_with_fields_recycler_view)
        listWithFieldsValueRecyclerView.layoutManager = LinearLayoutManager(context)
        listWithFieldsValueRecyclerView.hasFixedSize()
        adapter = FieldListsAdapter(requireActivity(), listItems as ArrayList<ListItem>)
        listWithFieldsValueRecyclerView.adapter = adapter
        val closeDialogBtn = layout.findViewById<AppCompatImageView>(R.id.lwfv_dialog_close_btn)

        val builder = MaterialAlertDialogBuilder(requireActivity())
        builder.setView(layout)
        builder.setCancelable(false)
        val alert = builder.create()
        alert.show()
        closeDialogBtn.setOnClickListener {
            alert.dismiss()
        }
        val tempList = tableGenerator.getList()
        if (tempList.isNotEmpty()) {
            listItems.clear()
            listItems.addAll(tempList)
            adapter.notifyDataSetChanged()
        } else {
            adapter.notifyDataSetChanged()
        }


        adapter.setOnItemClickListener(object : FieldListsAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val listValue = listItems[position]
                listId = listValue.id
                val list = tableGenerator.getListValues(listId!!)
                if (list.isNotEmpty()) {
                    //selectedListTextView.text = listValue.value
                    if (fieldType == "ap_title") {
                        appSettings.putInt("AP_TITLE_LIST_ID", listId!!)
                        appSettings.putString("AP_TITLE_LIST_NAME", listValue.value)
                        apTitleActiveListNameView.text = "Active List: ${listValue.value}"

                        val listOptions: String = tableGenerator.getListValues(listId!!)
                        val listValues = listOptions.split(",")
                        if (listValues.isNotEmpty()) {
                            appSettings.putString("AP_PRODUCT_TITLE", listValues[0])
                        }
                        val apTitleSpinnerAdapter = ArrayAdapter(
                            requireActivity(),
                            android.R.layout.simple_spinner_item,
                            listValues
                        )
                        apTitleSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        apTitleListSpinner.adapter = apTitleSpinnerAdapter

                        apTitleListSpinner.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(
                                    parent: AdapterView<*>?,
                                    view: View?,
                                    position: Int,
                                    id: Long
                                ) {
                                    appSettings.putString("AP_PRODUCT_TITLE", parent!!.selectedItem.toString())
                                }

                                override fun onNothingSelected(parent: AdapterView<*>?) {

                                }

                            }


                    }
                    else if (fieldType == "ap_description") {
                        appSettings.putInt("AP_DESCRIPTION_LIST_ID", listId!!)
                        appSettings.putString("AP_DESCRIPTION_LIST_NAME", listValue.value)
                        apDescriptionActiveListNameView.text = "Active List: ${listValue.value}"

                        val listOptions: String = tableGenerator.getListValues(listId!!)
                        val listValues = listOptions.split(",")
                        if (listValues.isNotEmpty()){
                            appSettings.putString("AP_PRODUCT_DESCRIPTION",listValues[0])
                        }
                        val apDescriptionSpinnerAdapter = ArrayAdapter(
                            requireActivity(),
                            android.R.layout.simple_spinner_item,
                            listValues
                        )
                        apDescriptionSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        apDescriptionListSpinner.adapter = apDescriptionSpinnerAdapter

                        apDescriptionListSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                appSettings.putString("AP_PRODUCT_DESCRIPTION",parent!!.selectedItem.toString())
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {

                            }

                        }

                    } else if (fieldType == "ap_quantity") {
                        appSettings.putInt("AP_QUANTITY_LIST_ID", listId!!)
                        appSettings.putString("AP_QUANTITY_LIST_NAME", listValue.value)
                        apQuantityActiveListNameView.text = "Active Lis: ${listValue.value}"

                        val listOptions: String = tableGenerator.getListValues(listId!!)
                        val listValues = listOptions.split(",")
                        if (listValues.isNotEmpty()) {
                            appSettings.putString("AP_PRODUCT_QUANTITY", listValues[0])
                        }
                        val apQuantitySpinnerAdapter = ArrayAdapter(
                            requireActivity(),
                            android.R.layout.simple_spinner_item,
                            listValues
                        )
                        apQuantitySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        apQuantityListSpinner.adapter = apQuantitySpinnerAdapter

                        apQuantityListSpinner.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(
                                    parent: AdapterView<*>?,
                                    view: View?,
                                    position: Int,
                                    id: Long
                                ) {
                                    appSettings.putString(
                                        "AP_PRODUCT_QUANTITY",
                                        parent!!.selectedItem.toString()
                                    )
                                }

                                override fun onNothingSelected(parent: AdapterView<*>?) {

                                }

                            }

                    } else if (fieldType == "ap_price") {
                        appSettings.putInt("AP_PRICE_LIST_ID", listId!!)
                        appSettings.putString("AP_PRICE_LIST_NAME", listValue.value)
                        apPriceActiveListNameView.text = "Active List: ${listValue.value}"

                        val listOptions: String = tableGenerator.getListValues(listId!!)
                        val listValues = listOptions.split(",")
                        if (listValues.isNotEmpty()){
                            appSettings.putString("AP_PRODUCT_PRICE",listValues[0])
                        }
                        val apPriceSpinnerAdapter = ArrayAdapter(
                            requireActivity(),
                            android.R.layout.simple_spinner_item,
                            listValues
                        )
                        apPriceSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        apPriceListSpinner.adapter = apPriceSpinnerAdapter

                        apPriceListSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                appSettings.putString("AP_PRODUCT_PRICE",parent!!.selectedItem.toString())
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {

                            }

                        }

                    }
                    alert.dismiss()
                } else {
                    MaterialAlertDialogBuilder(requireActivity())
                        .setMessage(getString(R.string.field_list_value_empty_error_text))
                        .setNegativeButton(getString(R.string.cancel_text)) { dialog, which ->
                            dialog.dismiss()
                        }
                        .setPositiveButton(getString(R.string.add_text)) { dialog, which ->
                            dialog.dismiss()
                            addTableDialog(listId!!)
                        }
                        .create().show()
                }

            }

            override fun onAddItemClick(position: Int) {
                alert.dismiss()
                val intent = Intent(context, FieldListsActivity::class.java)
//                    intent.putExtra("TABLE_NAME", tableName)
//                    intent.putExtra("FLAG", "yes")
                requireActivity().startActivity(intent)
            }
        })
    }

    private fun addTableDialog(id: Int) {
        val listValueLayout = LayoutInflater.from(context).inflate(
            R.layout.add_list_value_layout,
            null
        )
        val heading = listValueLayout.findViewById<MaterialTextView>(R.id.dialog_heading)
        heading.text = getString(R.string.list_value_hint_text)
        val listValueInputBox =
            listValueLayout.findViewById<TextInputEditText>(R.id.add_list_value_input_field)
        val listValueAddBtn =
            listValueLayout.findViewById<MaterialButton>(R.id.add_list_value_btn)
        val builder = MaterialAlertDialogBuilder(requireActivity())
        builder.setView(listValueLayout)
        val alert = builder.create()
        alert.show()
        listValueAddBtn.setOnClickListener {
            if (listValueInputBox.text.toString().isNotEmpty()) {
                val value = listValueInputBox.text.toString().trim()
                tableGenerator.insertListValue(id, value)
                alert.dismiss()
            } else {
                BaseActivity.showAlert(
                    requireActivity(),
                    getString(R.string.add_list_value_error_text)
                )
            }
        }
    }

    var launchActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data

                if (data != null && data.hasExtra("TITLE")) {
                    val title = data.getStringExtra("TITLE") as String
                    if (title.isNotEmpty()) {
                        val apTitleSpinnerSelectedPosition = appSettings.getInt("AP_TITLE_SPINNER_SELECTED_POSITION")

                        if (apTitleSpinnerSelectedPosition == 1){
                            val currentPItemTitle = apTitleDefaultInputBox.text.toString().trim()
                            val stringBuilder = java.lang.StringBuilder()
                            stringBuilder.append(currentPItemTitle)
                            stringBuilder.append(title)
                            appSettings.putString("AP_PRODUCT_TITLE", stringBuilder.toString())
                            apTitleView.setText(stringBuilder.toString())
                            apTitleVoiceRecView.visibility = View.GONE
                            apTitleCameraRecView.visibility = View.GONE
                            apTitleImageRecView.visibility = View.GONE
                            apTitleListBtn.visibility = View.GONE
                            apTitleActiveListNameView.visibility = View.GONE
                            apTitleDefaultInputWrapper.visibility = View.GONE
//                            apTitleDefaultValueMessage.visibility = View.GONE
                            apTitleListSpinner.visibility = View.GONE
                            apTitleViewWrapper.visibility = View.VISIBLE
                            appSettings.putInt("AP_TITLE_SPINNER_SELECTED_POSITION", 0)
                            apTitleSpinner.setSelection(0, false)
                        }
                        else{
                            val currentPItemTitle = apTitleView.text.toString().trim()
                            val stringBuilder = java.lang.StringBuilder()
                            stringBuilder.append(currentPItemTitle)
                            stringBuilder.append(title)
                            apTitleView.setText(stringBuilder.toString())
                            appSettings.putString("AP_PRODUCT_TITLE", stringBuilder.toString())
                        }

                    }
                }

                if (data != null && data.hasExtra("DESCRIPTION")) {
                    val description = data.getStringExtra("DESCRIPTION") as String
                    if (description.isNotEmpty()) {
                        val apDescriptionSpinnerSelectedPosition = appSettings.getInt("AP_DESCRIPTION_SPINNER_SELECTED_POSITION")
                        if (apDescriptionSpinnerSelectedPosition == 1){
                            val currentPItemDescription = apDescriptionDefaultInputBox.text.toString().trim()
                            val stringBuilder = java.lang.StringBuilder()
                            stringBuilder.append(currentPItemDescription)
                            stringBuilder.append(description)
                            appSettings.putString("AP_PRODUCT_DESCRIPTION", stringBuilder.toString())
                            apDescriptionView.setText(stringBuilder.toString())
                            apDescriptionVoiceRecView.visibility = View.GONE
                            apDescriptionCameraRecView.visibility = View.GONE
                            apDescriptionImageRecView.visibility = View.GONE
                            apDescriptionListBtn.visibility = View.GONE
                            apDescriptionActiveListNameView.visibility = View.GONE
                            apDescriptionDefaultInputWrapper.visibility = View.GONE
//                            apDescriptionDefaultValueMessage.visibility = View.GONE
                            apDescriptionListSpinner.visibility = View.GONE
                            apDescriptionViewWrapper.visibility = View.VISIBLE
                            appSettings.putInt("AP_DESCRIPTION_SPINNER_SELECTED_POSITION", 0)
                            apDescriptionSpinner.setSelection(0,false)
                        }
                        else{
                            val currentPItemDescription = apDescriptionView.text.toString().trim()
                            val stringBuilder = java.lang.StringBuilder()
                            stringBuilder.append(currentPItemDescription)
                            stringBuilder.append(description)
                            apDescriptionView.setText(stringBuilder.toString())
                            appSettings.putString("AP_PRODUCT_DESCRIPTION", stringBuilder.toString())
                        }


                    }
                }

                if (apDescriptionView.text.toString().isNotEmpty()) {
                    apDescriptionView.setSelection(apDescriptionView.text.toString().length)
                    apDescriptionView.requestFocus()
                    Constants.openKeyboar(requireActivity())
                }
            }
        }

    var launchActivity1 =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data

                if (data != null && data.hasExtra("TITLE")) {
                    val title = data.getStringExtra("TITLE") as String
                    if (title.isNotEmpty()) {
//                            val currentPItemTitle = apTitleView.text.toString().trim()
//                            val stringBuilder = java.lang.StringBuilder()
//                            stringBuilder.append(currentPItemTitle)
//                            stringBuilder.append(title)
//                            apTitleView.setText(stringBuilder.toString())
                        appSettings.putString("AP_PRODUCT_TITLE", title)
//                                val currentFragment = childFragmentManager.getFragments().get(apViewPager.getCurrentItem())
//                                if (currentFragment is ApTitleInputFragment) {
//                                    currentFragment.updateTestData(title)
//                                }

                    }
                }

                if (data != null && data.hasExtra("DESCRIPTION")) {
                    val description = data.getStringExtra("DESCRIPTION") as String
                    if (description.isNotEmpty()) {
                        appSettings.putString("AP_PRODUCT_DESCRIPTION", description)
//                                val currentFragment = childFragmentManager.getFragments().get(apViewPager.getCurrentItem())
//                                if (currentFragment is ApDescriptionInputFragment) {
//                                    currentFragment.updateTestData(description)
//                                }
//                            val currentPItemDescription = apDescriptionView.text.toString().trim()
//                            val stringBuilder = java.lang.StringBuilder()
//                            stringBuilder.append(currentPItemDescription)
//                            stringBuilder.append(description)
//                            apDescriptionView.setText(stringBuilder.toString())

                    }
                }

//                    if (apDescriptionView.text.toString().isNotEmpty()) {
//                        apDescriptionView.setSelection(apDescriptionView.text.toString().length)
//                        apDescriptionView.requestFocus()
//                        Constants.openKeyboar(requireActivity())
//                    }
            }
        }


    private fun addProductValidation(
        categoriesSpinner: AppCompatSpinner?,
        apTitleView: TextInputEditText?,
        apTitleSelectedPosition: Int,
        apQuantityView: TextInputEditText?,
        apQuantitySelectedPosition: Int,
        apPriceView: TextInputEditText?,
        apPriceSelectedPosition: Int
    ): Boolean {
        if (selectedCategoryId == 0) {
            BaseActivity.showAlert(
                requireActivity(),
                requireActivity().resources.getString(R.string.add_product_cate_error)
            )
            return false
        } else if (apTitleSelectedPosition == 1 && apTitleView!!.text.toString().isEmpty()) {
            BaseActivity.showAlert(
                requireActivity(),
                requireActivity().resources.getString(R.string.empty_text_error)
            )
            return false
        } else if (apQuantitySelectedPosition == 1 && apQuantityView!!.text.toString()
                .isEmpty()
        ) {
            BaseActivity.showAlert(
                requireActivity(),
                requireActivity().resources.getString(R.string.empty_text_error)
            )
            return false
        } else if (apPriceSelectedPosition == 1 && apPriceView!!.text.toString().isEmpty()) {
            BaseActivity.showAlert(
                requireActivity(),
                requireActivity().resources.getString(R.string.empty_text_error)
            )
            return false
        }
        return true
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
                        requireActivity(),
                        imageUri.data
                    )
//                        selectedImageBase64String =
//                            ImageManager.convertImageToBase64(
//                                requireActivity(),
//                                currentPhotoPath!!
//                            )
//                        Log.d("TEST199DIALOG", selectedImageBase64String)
                    Glide.with(requireActivity())
                        .load(currentPhotoPath)
                        .placeholder(R.drawable.placeholder)
                        .centerInside()
                        .into(selectedImageView)
                    barcodeImageList.add(currentPhotoPath!!)
                    multiImagesList.add(currentPhotoPath!!)
                    imagesAdapter.notifyDataSetChanged()
                }

            }
        }

    // THIS RESULT LAUNCHER WILL CALL THE ACTION PICK FROM FILES FOR BACKGROUND AND LOGO IMAGE
    private var cameraResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // THIS LINE OF CODE WILL CHECK THE IMAGE HAS BEEN SELECTED OR NOT
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val bitmap = data!!.extras!!.get("data") as Bitmap
                createImageFile(bitmap)
//                    selectedImageBase64String =
//                        ImageManager.convertImageToBase64(
//                            requireActivity(),
//                            currentPhotoPath!!
//                        )
//                    Log.d("TEST199DIALOG", selectedImageBase64String)
                Glide.with(requireActivity())
                    .load(currentPhotoPath)
                    .placeholder(R.drawable.placeholder)
                    .centerInside()
                    .into(selectedImageView)
                barcodeImageList.add(currentPhotoPath!!)
                multiImagesList.add(currentPhotoPath!!)
                imagesAdapter.notifyDataSetChanged()
            }
        }

    private fun createImageFile(bitmap: Bitmap) {
        currentPhotoPath = ImageManager.readWriteImage(requireActivity(), bitmap).absolutePath
    }

    private fun getCategories(adapter: ArrayAdapter<Category>) {

//            BaseActivity.startLoading(requireActivity())
        viewModel.callCategories(requireActivity(), shopName, email, password)
        viewModel.getCategoriesResponse().observe(this, Observer { response ->
            if (response != null) {
//                    BaseActivity.dismiss()
                if (response.get("status").asString == "200") {
                    val categories = response.get("categories").asJsonArray
                    if (categories.size() > 0) {
                        originalCategoriesList.clear()
                        for (i in 0 until categories.size()) {
                            val category = categories[i].asJsonObject
                            originalCategoriesList.add(
                                Category(
                                    category.get("title").asString,
                                    category.get("id").asInt
                                )
                            )
                        }
                        adapter.notifyDataSetChanged()
                        if (originalCategoriesList.size > 0) {
                            selectedCategoryId = originalCategoriesList[0].id
                            //categoriesSpinner.setSelection(0)
                        }
                    }
                } else {
//                        BaseActivity.dismiss()
                }
            } else {
//                    BaseActivity.dismiss()
            }
        })
    }

    internal class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(
        manager,
        BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {
        private val mFragmentList: MutableList<Fragment> = ArrayList()
        private val mFragmentTitleList: MutableList<String> = ArrayList()
        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mFragmentTitleList[position]
        }
    }

}