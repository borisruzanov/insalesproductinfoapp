package com.mywebsite.insalesproductinfoapp.view.fragments

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.android.volley.VolleyError
import com.boris.expert.csvmagic.model.ProductImages
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mywebsite.insalesproductinfoapp.R
import com.mywebsite.insalesproductinfoapp.adapters.BarcodeImageAdapter
import com.mywebsite.insalesproductinfoapp.adapters.InSalesProductsAdapter
import com.mywebsite.insalesproductinfoapp.adapters.InternetImageAdapter
import com.mywebsite.insalesproductinfoapp.adapters.ProductImagesAdapter
import com.mywebsite.insalesproductinfoapp.interfaces.APICallback
import com.mywebsite.insalesproductinfoapp.interfaces.ResponseListener
import com.mywebsite.insalesproductinfoapp.model.Product
import com.mywebsite.insalesproductinfoapp.utils.*
import com.mywebsite.insalesproductinfoapp.view.activities.BarcodeReaderActivity
import com.mywebsite.insalesproductinfoapp.view.activities.BaseActivity
import com.mywebsite.insalesproductinfoapp.view.activities.MainActivity
import com.mywebsite.insalesproductinfoapp.view.activities.UserScreenActivity
import com.mywebsite.insalesproductinfoapp.viewmodel.SalesCustomersViewModel
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import io.paperdb.Paper
import org.apmem.tools.layouts.FlowLayout
import org.json.JSONObject
import java.util.*

class CustomDialog(
    private val shopName: String,
    private val email: String,
    private val password: String,
    private val pItem: Product,
    private val position: Int,
    private val selectedProductImages: List<String>?,
    private val insalesAdapter: InSalesProductsAdapter,
    private val viewModel: SalesCustomersViewModel,
    private val listener: ResponseListener
) : DialogFragment() , View.OnClickListener{

    private var defaultLayout = 0
    private var barcodeImageList = mutableListOf<String>()
    var multiImagesList = mutableListOf<String>()
    private lateinit var selectedImageView: AppCompatImageView
    private lateinit var imagesRecyclerView: RecyclerView
    private var adapter: BarcodeImageAdapter? = null
    private lateinit var appSettings: AppSettings
    private lateinit var internetImageAdapter: InternetImageAdapter
    private lateinit var searchBtnView: ImageButton
    private lateinit var searchBoxView: TextInputEditText
    private lateinit var loader: ProgressBar
    private lateinit var voiceSearchIcon: AppCompatImageView
    private var voiceLanguageCode = "en"
    var searchedImagesList = mutableListOf<String>()
    private var currentPhotoPath: String? = null
    private lateinit var internetSearchLayout: View
    private lateinit var internetImageRecyclerView: RecyclerView
    private lateinit var internetImageDoneBtn: MaterialButton
    private var barcodeSearchHint = "default"
    private var intentType = 0
    private var selectedInternetImage = ""
    private var userCurrentCredits = ""
    private lateinit var searchEditText: TextInputEditText
    private lateinit var searchBox: TextInputEditText
    private lateinit var searchImageBtn: ImageButton
    private var voiceSearchHint = "default"
    private var productImagesChanges = false
    private var selectedImageBase64String: String = ""
    private var titleTextViewList = mutableListOf<MaterialTextView>()
    private var shortDescTextViewList = mutableListOf<MaterialTextView>()
    private var fullDescTextViewList = mutableListOf<MaterialTextView>()
    private lateinit var fullDescriptionBox: TextInputEditText
    private lateinit var titleBox: TextInputEditText
    private lateinit var dynamicTitleTextViewWrapper: FlowLayout
    private lateinit var dynamicFullDescTextViewWrapper: FlowLayout
    private lateinit var dynamicKeywordsTextViewWrapper: FlowLayout
//    private lateinit var pItem:Product

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            R.style.FullScreenDialogStyle
        )
        appSettings = AppSettings(requireActivity())
        productImagesChanges = false
//        pItem = currentPItem
    }

    override fun onResume() {
        super.onResume()
        internetSearchLayout = LayoutInflater.from(context)
            .inflate(R.layout.internet_image_search_dialog_layout, null)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v =
            inflater.inflate(
                R.layout.insales_product_detail_update_dialog_layout,
                container
            )

        initViews(v)

        return v
    }

    private fun initViews(dialogLayout: View) {
        val dialogHeading = dialogLayout.findViewById<MaterialTextView>(R.id.dialog_heading)
        val swapLayoutBtn = dialogLayout.findViewById<MaterialCheckBox>(R.id.layout_swap)
        val firstLinearLayout =
            dialogLayout.findViewById<LinearLayout>(R.id.first_linear_layout)
        val secondLinearLayout =
            dialogLayout.findViewById<LinearLayout>(R.id.second_linear_layout)
        dynamicTitleTextViewWrapper =
            dialogLayout.findViewById(R.id.dynamic_insales_title_textview_wrapper)
//                dynamicTitleTextViewWrapper.setOnDragListener(MyDragListener())
        val dynamicShortDescTextViewWrapper =
            dialogLayout.findViewById<FlowLayout>(R.id.dynamic_insales_short_description_textview_wrapper)
        dynamicFullDescTextViewWrapper =
            dialogLayout.findViewById(R.id.dynamic_insales_full_description_textview_wrapper)

        secondLinearLayout.visibility = View.VISIBLE
        titleBox = dialogLayout.findViewById<TextInputEditText>(R.id.insales_product_title_input_field)
        val productShortDescriptionBox =
            dialogLayout.findViewById<TextInputEditText>(R.id.insales_product_short_desc_input_field)
        fullDescriptionBox = dialogLayout.findViewById(R.id.insales_product_full_desc_input_field)
        val getDescriptionView =
            dialogLayout.findViewById<MaterialTextView>(R.id.get_description_text_view)
        val getDescriptionView1 =
            dialogLayout.findViewById<MaterialTextView>(R.id.get_description_text_view1)

        val titleClearBrush =
            dialogLayout.findViewById<AppCompatImageView>(R.id.title_clear_brush_view)
        val shortDescClearBrush =
            dialogLayout.findViewById<AppCompatImageView>(R.id.short_desc_clear_brush_view)
        val fullDescClearBrush =
            dialogLayout.findViewById<AppCompatImageView>(R.id.full_desc_clear_brush_view)

        val productImagesRecyclerView = dialogLayout.findViewById<RecyclerView>(R.id.products_images_recyclerview)
        productImagesRecyclerView.layoutManager =
            WrapContentLinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)
        productImagesRecyclerView.hasFixedSize()
        pItem.productImages!!.sortByDescending { it.id }
        val productImagesadapter = ProductImagesAdapter(requireActivity(), pItem.productImages as ArrayList<ProductImages>)
        productImagesRecyclerView.adapter = productImagesadapter
        productImagesadapter.setOnItemClickListener(object : ProductImagesAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {

            }

            override fun onItemEditClick(btn: MaterialButton, imagePosition: Int) {
                val imageItem = pItem.productImages!![imagePosition]
                val insalesUpdateProductImageLayout = LayoutInflater.from(context).inflate(
                    R.layout.insales_product_image_update_dialog, null
                )
                selectedImageView =
                    insalesUpdateProductImageLayout.findViewById(R.id.selected_insales_product_image_view)
                val cameraImageView =
                    insalesUpdateProductImageLayout.findViewById<AppCompatImageView>(R.id.camera_image_view)
                val imagesImageView =
                    insalesUpdateProductImageLayout.findViewById<AppCompatImageView>(R.id.images_image_view)
                val internetImageView =
                    insalesUpdateProductImageLayout.findViewById<AppCompatImageView>(R.id.internet_image_view)
                val cancelDialogBtn =
                    insalesUpdateProductImageLayout.findViewById<MaterialButton>(R.id.insales_product_dialog_cancel_btn)
                val updateImageBtn =
                    insalesUpdateProductImageLayout.findViewById<MaterialButton>(R.id.insales_product_dialog_update_btn)

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
                    voiceSearchIcon = internetSearchLayout.findViewById(
                        R.id
                            .voice_search_internet_images
                    )
                    val barcodeImage = internetSearchLayout.findViewById<AppCompatImageView>(
                        R.id
                            .barcode_img_search_internet_images
                    )
                    internetImageDoneBtn =
                        internetSearchLayout.findViewById(R.id.iisdl_dialog_done_btn)
                    val builder = MaterialAlertDialogBuilder(requireActivity())
                    builder.setCancelable(false)
                    builder.setView(internetSearchLayout)
                    val iAlert = builder.create()
                    iAlert.show()

                    internetImageDoneBtn.setOnClickListener {
                        iAlert.dismiss()
                    }

                    barcodeImage.setOnClickListener {
                        barcodeSearchHint = "image"
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
                        searchedImagesList as ArrayList<String>
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
                            btn.text = getString(R.string.please_wait)

                            val selectedImage = searchedImagesList[position]
                            val bitmap: Bitmap? = ImageManager.getBitmapFromURL(
                                requireActivity(),
                                selectedImage
                            )
                            if (bitmap != null) {
                                ImageManager.saveMediaToStorage(
                                    requireActivity(),
                                    bitmap,
                                    object : ResponseListener {
                                        override fun onSuccess(result: String) {
                                            if (loader.visibility == View.VISIBLE) {
                                                loader.visibility = View.INVISIBLE
                                            }

                                            if (result.isNotEmpty()) {
                                                currentPhotoPath = ImageManager.getRealPathFromUri(
                                                    requireActivity(),
                                                    Uri.parse(result)
                                                )!!
                                                Glide.with(requireActivity())
                                                    .load(currentPhotoPath)
                                                    .placeholder(R.drawable.placeholder)
                                                    .centerInside()
                                                    .into(selectedImageView)
                                                selectedImageBase64String =
                                                    ImageManager.convertImageToBase64(
                                                        requireActivity(),
                                                        currentPhotoPath!!
                                                    )
                                                iAlert.dismiss()
                                            } else {
                                                BaseActivity.showAlert(
                                                    requireActivity(),
                                                    getString(R.string.something_wrong_error)
                                                )
                                            }
                                        }

                                    })
                            } else {
                                if (loader.visibility == View.VISIBLE) {
                                    loader.visibility = View.INVISIBLE
                                }
                                BaseActivity.showAlert(
                                    requireActivity(),
                                    getString(R.string.something_wrong_error)
                                )
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
                            voiceResultLauncher.launch(intent)
                        }
                    }

                    searchBtnView.setOnClickListener {
                        startSearch(
                            searchBoxView,
                            searchBtnView,
                            loader,
                            searchedImagesList as ArrayList<String>,
                            internetImageAdapter
                        )
                    }
                    searchBoxView.setOnEditorActionListener(object :
                        TextView.OnEditorActionListener {
                        override fun onEditorAction(
                            v: TextView?,
                            actionId: Int,
                            event: KeyEvent?
                        ): Boolean {
                            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                                startSearch(
                                    searchBoxView,
                                    searchBtnView,
                                    loader,
                                    searchedImagesList as ArrayList<String>,
                                    internetImageAdapter
                                )
                            }
                            return false
                        }

                    })
                }


                Glide.with(requireActivity())
                    .load(imageItem.imageUrl)
                    .thumbnail(Glide.with(requireActivity()).load(R.drawable.loader))
                    .fitCenter()
                    .into(selectedImageView)
                val builder = MaterialAlertDialogBuilder(requireActivity())
                builder.setCancelable(false)
                builder.setView(insalesUpdateProductImageLayout)

                val alert = builder.create()
                alert.show()

                cancelDialogBtn.setOnClickListener {
                    alert.dismiss()
                }

                updateImageBtn.setOnClickListener {

                    if (selectedImageBase64String.isNotEmpty()) {
                        alert.dismiss()
                        BaseActivity.startLoading(requireActivity())

                        viewModel.callUpdateProductImage(
                            requireActivity(),
                            shopName,
                            email,
                            password,
                            selectedImageBase64String,
                            imageItem.productId,
                            imageItem.position,
                            imageItem.id,
                            "${System.currentTimeMillis()}.jpg"
                        )
                        viewModel.getUpdateProductImageResponse()
                            .observe(requireActivity(), Observer { response ->

                                if (response != null) {
                                    BaseActivity.dismiss()
                                    if (response.get("status").asString == "200") {
                                        selectedImageBase64String = ""
                                        productImagesChanges = true
                                        imageItem.imageUrl = currentPhotoPath!!
                                        val tempItem = imageItem
                                        pItem.productImages!!.removeAt(imagePosition)
                                        pItem.productImages!!.add(imagePosition, tempItem)
                                        productImagesadapter.notifyDataSetChanged()
                                    } else {
                                        BaseActivity.showAlert(
                                            requireActivity(),
                                            response.get("message").asString
                                        )
                                    }
                                } else {
                                    BaseActivity.dismiss()
                                }
                            })
                    } else {
                        BaseActivity.showAlert(
                            requireActivity(),
                            getString(R.string.image_attach_error)
                        )
                    }
                }
            }

            override fun onItemRemoveClick(imagePosition: Int) {
                MaterialAlertDialogBuilder(requireActivity())
                    .setTitle(getString(R.string.remove_text))
                    .setMessage(getString(R.string.image_remove_warning_message))
                    .setCancelable(false)
                    .setNegativeButton(getString(R.string.cancel_text)) { dialog, which ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(getString(R.string.remove_text)) { dialog, which ->
                        dialog.dismiss()
                        BaseActivity.startLoading(requireActivity())
                        viewModel.callRemoveProductImage(
                            requireActivity(),
                            shopName,
                            email,
                            password,
                            pItem.id,
                            pItem.productImages!![imagePosition].id
                        )
                        viewModel.getRemoveProductImageResponse()
                            .observe(requireActivity(), Observer { response ->

                                if (response != null) {
                                    if (response.get("status").asString == "200") {
                                        BaseActivity.dismiss()
                                        pItem.productImages!!.removeAt(imagePosition)
                                        productImagesadapter.notifyItemRemoved(imagePosition)
                                        productImagesChanges = true
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

                    }.create().show()
            }

            override fun onItemAddImageClick(position: Int) {
                multiImagesList.clear()
                barcodeImageList.clear()
                val insalesUpdateProductImageLayout = LayoutInflater.from(context).inflate(
                    R.layout.insales_product_image_update_dialog, null
                )
                selectedImageView =
                    insalesUpdateProductImageLayout.findViewById(R.id.selected_insales_product_image_view)
                imagesRecyclerView =
                    insalesUpdateProductImageLayout.findViewById(R.id.insales_product_images_recyclerview)
                val cameraImageView =
                    insalesUpdateProductImageLayout.findViewById<AppCompatImageView>(R.id.camera_image_view)
                val imagesImageView =
                    insalesUpdateProductImageLayout.findViewById<AppCompatImageView>(R.id.images_image_view)
                val internetImageView =
                    insalesUpdateProductImageLayout.findViewById<AppCompatImageView>(R.id.internet_image_view)
                val cancelDialogBtn =
                    insalesUpdateProductImageLayout.findViewById<MaterialButton>(R.id.insales_product_dialog_cancel_btn)
                val updateImageBtn =
                    insalesUpdateProductImageLayout.findViewById<MaterialButton>(R.id.insales_product_dialog_update_btn)
                updateImageBtn.setText(requireActivity().resources.getString(R.string.add_text))
                imagesRecyclerView.layoutManager = LinearLayoutManager(
                    requireActivity(), RecyclerView.HORIZONTAL,
                    false
                )
                imagesRecyclerView.hasFixedSize()
                adapter = BarcodeImageAdapter(
                    requireContext(),
                    barcodeImageList as ArrayList<String>
                )
                imagesRecyclerView.adapter = adapter
                adapter!!.setOnItemClickListener(object :
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
                            adapter!!.notifyItemRemoved(position)
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
//                    val searchedImagesList = mutableListOf<String>()
                    val tempImageList = mutableListOf<String>()
//                    val internetSearchLayout = LayoutInflater.from(context)
//                        .inflate(R.layout.internet_image_search_dialog_layout, null)
                    searchedImagesList.clear()
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
                    internetImageDoneBtn =
                        internetSearchLayout.findViewById(R.id.iisdl_dialog_done_btn)
                    val builder = MaterialAlertDialogBuilder(requireActivity())
                    builder.setCancelable(false)
                    builder.setView(internetSearchLayout)
                    val iAlert = builder.create()
                    iAlert.show()

                    internetImageDoneBtn.setOnClickListener {
                        iAlert.dismiss()
                    }

                    barcodeImage.setOnClickListener {
                        barcodeSearchHint = "image"
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
                        searchedImagesList as ArrayList<String>
                    )
                    internetImageRecyclerView.adapter = internetImageAdapter
                    internetImageAdapter.setOnItemClickListener(object :
                        InternetImageAdapter.OnItemClickListener {
                        override fun onItemClick(position: Int) {
                            val selectedImage = searchedImagesList[position]

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
                            if (btn.text.toString()
                                    .toLowerCase(Locale.ENGLISH) == "attach"
                            ) {
                                barcodeImageList.add(selectedInternetImage)
                                multiImagesList.add(selectedInternetImage)
                                btn.text =
                                    requireActivity().resources.getString(R.string.attached_text)
                                btn.setBackgroundColor(
                                    ContextCompat.getColor(
                                        requireActivity(),
                                        R.color.dark_gray
                                    )
                                )
                            } else {
                                btn.text =
                                    requireActivity().resources.getString(R.string.attach_text)
                                btn.setBackgroundColor(
                                    ContextCompat.getColor(
                                        requireActivity(),
                                        R.color.primary_positive_color
                                    )
                                )
                                barcodeImageList.remove(selectedInternetImage)
                                multiImagesList.remove(selectedInternetImage)
                            }
                            adapter!!.notifyDataSetChanged()
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
                            voiceResultLauncher.launch(intent)
                        }
                    }

                    searchBtnView.setOnClickListener {
                        startSearch(
                            searchBoxView,
                            searchBtnView,
                            loader,
                            searchedImagesList as ArrayList<String>,
                            internetImageAdapter
                        )
                    }
                    searchBoxView.setOnEditorActionListener(object :
                        TextView.OnEditorActionListener {
                        override fun onEditorAction(
                            v: TextView?,
                            actionId: Int,
                            event: KeyEvent?
                        ): Boolean {
                            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                                startSearch(
                                    searchBoxView,
                                    searchBtnView,
                                    loader,
                                    searchedImagesList as ArrayList<String>,
                                    internetImageAdapter
                                )
                            }
                            return false
                        }

                    })
                }

                Glide.with(requireActivity())
                    .load("")
                    .thumbnail(Glide.with(requireActivity()).load(R.drawable.placeholder))
                    .fitCenter()
                    .into(selectedImageView)
                val builder = MaterialAlertDialogBuilder(requireActivity())
                builder.setCancelable(false)
                builder.setView(insalesUpdateProductImageLayout)

                val alert = builder.create()
                alert.show()

                cancelDialogBtn.setOnClickListener {
                    alert.dismiss()
                }

                updateImageBtn.setOnClickListener {

                    if (multiImagesList.isNotEmpty()) {
                        val tempProduct = pItem
                        for (i in 0 until multiImagesList.size) {
                            tempProduct.productImages!!.add(
                                ProductImages(
                                    0,
                                    pItem.id,
                                    multiImagesList[i],
                                    0
                                )
                            )
                        }
                        productImagesadapter.notifyItemRangeChanged(0, pItem.productImages!!.size)
//                            Constants.startImageUploadService(pItem.id, multiImagesList.joinToString(","), "add_image", false)
                        Constants.multiImagesSelectedListSize = multiImagesList.size
//                            multiImagesList.clear()
                        productImagesChanges = true
                        alert.dismiss()
                    } else {
                        Constants.multiImagesSelectedListSize = 0
                        BaseActivity.showAlert(
                            requireActivity(),
                            getString(R.string.image_attach_error)
                        )
                    }
                }
            }

        })
        if (pItem.productImages!!.size > 0) {
            productImagesadapter.notifyItemRangeChanged(0, pItem.productImages!!.size)
        } else {
            productImagesadapter.notifyDataSetChanged()
        }


        if (pItem.title.length > 10) {
            titleBox.setBackgroundColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.white
                )
            )
            dynamicTitleTextViewWrapper.setBackgroundColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.white
                )
            )
        }
        else {
            //holder.productTitle.text = context.getString(R.string.product_title_error)
            titleBox.setBackgroundColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.light_red
                )
            )
            dynamicTitleTextViewWrapper.setBackgroundColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.light_red
                )
            )
        }

        if (pItem.fullDesc.length > 10) {
            fullDescriptionBox.setBackgroundColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.white
                )
            )
            dynamicFullDescTextViewWrapper.setBackgroundColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.white
                )
            )
        }
        else {
            //holder.productDescription.text = context.getString(R.string.product_description_error)
            fullDescriptionBox.setBackgroundColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.light_red
                )
            )
            dynamicFullDescTextViewWrapper.setBackgroundColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.light_red
                )
            )
        }

        titleBox.setOnTouchListener(View.OnTouchListener { v, event ->
            if (v.id == R.id.insales_product_title_input_field) {
                v.parent.requestDisallowInterceptTouchEvent(true)
                when (event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_UP -> v.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        })

        productShortDescriptionBox.setOnTouchListener(View.OnTouchListener { v, event ->
            if (v.id == R.id.insales_product_short_desc_input_field) {
                v.parent.requestDisallowInterceptTouchEvent(true)
                when (event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_UP -> v.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        })

        fullDescriptionBox.setOnTouchListener(View.OnTouchListener { v, event ->
            if (v.id == R.id.insales_product_full_desc_input_field) {
                v.parent.requestDisallowInterceptTouchEvent(true)
                when (event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_UP -> v.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        })

        titleClearBrush.setOnClickListener {
            dynamicTitleTextViewWrapper.removeAllViews()
            titleTextViewList.clear()
            titleBox.setText("")
            secondLinearLayout.visibility = View.GONE
            firstLinearLayout.visibility = View.VISIBLE

            defaultLayout = 1
            swapLayoutBtn.isChecked = true
            titleBox.requestFocus()
            BaseActivity.showSoftKeyboard(requireContext(),titleBox)
        }

        shortDescClearBrush.setOnClickListener {
            dynamicShortDescTextViewWrapper.removeAllViews()
            shortDescTextViewList.clear()
            productShortDescriptionBox.setText("")
            secondLinearLayout.visibility = View.GONE
            firstLinearLayout.visibility = View.VISIBLE

            defaultLayout = 1
            swapLayoutBtn.isChecked = true
            productShortDescriptionBox.requestFocus()
            BaseActivity.showSoftKeyboard(requireContext(),productShortDescriptionBox)
        }

        fullDescClearBrush.setOnClickListener {
            dynamicFullDescTextViewWrapper.removeAllViews()
            fullDescTextViewList.clear()
            fullDescriptionBox.setText("")
            secondLinearLayout.visibility = View.GONE
            firstLinearLayout.visibility = View.VISIBLE

            defaultLayout = 1
            swapLayoutBtn.isChecked = true
            fullDescriptionBox.requestFocus()
            BaseActivity.showSoftKeyboard(requireContext(),fullDescriptionBox)
        }

        titleBox.setText(pItem.title)
        productShortDescriptionBox.setText(pItem.shortDesc)
        fullDescriptionBox.setText(pItem.fullDesc)
        val dialogCancelBtn =
            dialogLayout.findViewById<MaterialButton>(R.id.insales_product_detail_dialog_cancel_btn)
        val dialogUpdateBtn =
            dialogLayout.findViewById<MaterialButton>(R.id.insales_product_detail_dialog_update_btn)

        titleTextViewList.clear()
        shortDescTextViewList.clear()
        fullDescTextViewList.clear()
        dynamicTitleTextViewWrapper.removeAllViews()
        dynamicShortDescTextViewWrapper.removeAllViews()
        dynamicFullDescTextViewWrapper.removeAllViews()

        val titleTextList = pItem.title.trim().split(" ")
        val shortDescTextList = pItem.shortDesc.trim().split(" ")
        val fullDescTextList = pItem.fullDesc.trim().split(" ")

        for (i in 0 until titleTextList.size) {
            val params = FlowLayout.LayoutParams(
                FlowLayout.LayoutParams.WRAP_CONTENT,
                FlowLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(5, 5, 5, 5)
            val textView = MaterialTextView(requireActivity())
            textView.layoutParams = params
            textView.text = titleTextList[i].trim()
            textView.tag = "title"
            textView.id = i
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            textView.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
            titleTextViewList.add(textView)
            textView.setOnClickListener(this)
            dynamicTitleTextViewWrapper.addView(textView)
        }

        for (i in 0 until shortDescTextList.size) {
            val params = FlowLayout.LayoutParams(
                FlowLayout.LayoutParams.WRAP_CONTENT,
                FlowLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(5, 5, 5, 5)
            val textView = MaterialTextView(requireActivity())
            textView.layoutParams = params
            textView.text = shortDescTextList[i].trim()
            textView.tag = "title"
            textView.id = i
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            textView.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
            shortDescTextViewList.add(textView)
            textView.setOnClickListener(this)
            dynamicShortDescTextViewWrapper.addView(textView)
        }
//
        for (i in 0 until fullDescTextList.size) {
            val params = FlowLayout.LayoutParams(
                FlowLayout.LayoutParams.WRAP_CONTENT,
                FlowLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(5, 5, 5, 5)
            val textView = MaterialTextView(requireActivity())
            textView.layoutParams = params
            textView.text = fullDescTextList[i].trim()
            textView.tag = "title"
            textView.id = i
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            textView.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
            fullDescTextViewList.add(textView)
            textView.setOnClickListener(this)
            dynamicFullDescTextViewWrapper.addView(textView)
        }

        swapLayoutBtn.setOnClickListener{view ->
            if (!swapLayoutBtn.isChecked){
//                swapLayoutBtn.isChecked = false
                BaseActivity.hideSoftKeyboard(requireContext(),titleBox)
                firstLinearLayout.visibility = View.GONE
                secondLinearLayout.visibility = View.VISIBLE
                defaultLayout = 0

                titleTextViewList.clear()
                shortDescTextViewList.clear()
                fullDescTextViewList.clear()
                dynamicTitleTextViewWrapper.removeAllViews()
                dynamicShortDescTextViewWrapper.removeAllViews()
                dynamicFullDescTextViewWrapper.removeAllViews()

                val titleTextList1 = pItem.title.trim().split(" ")
                val shortDescTextList1 = pItem.shortDesc.trim().split(" ")
                val fullDescTextList1 = pItem.fullDesc.trim().split(" ")

                for (i in 0 until titleTextList1.size) {
                    val params = FlowLayout.LayoutParams(
                        FlowLayout.LayoutParams.WRAP_CONTENT,
                        FlowLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(5, 5, 5, 5)
                    val textView = MaterialTextView(requireActivity())
                    textView.layoutParams = params
                    textView.text = titleTextList1[i].trim()
                    textView.tag = "title"
                    textView.id = i
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                    textView.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
                    titleTextViewList.add(textView)
                    textView.setOnClickListener(this)
                    dynamicTitleTextViewWrapper.addView(textView)
                }

                for (i in 0 until shortDescTextList1.size) {
                    val params = FlowLayout.LayoutParams(
                        FlowLayout.LayoutParams.WRAP_CONTENT,
                        FlowLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(5, 5, 5, 5)
                    val textView = MaterialTextView(requireActivity())
                    textView.layoutParams = params
                    textView.text = shortDescTextList1[i].trim()
                    textView.tag = "title"
                    textView.id = i
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                    textView.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
                    shortDescTextViewList.add(textView)
                    textView.setOnClickListener(this)
                    dynamicShortDescTextViewWrapper.addView(textView)
                }
//
                for (i in 0 until fullDescTextList1.size) {
                    val params = FlowLayout.LayoutParams(
                        FlowLayout.LayoutParams.WRAP_CONTENT,
                        FlowLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(5, 5, 5, 5)
                    val textView = MaterialTextView(requireActivity())
                    textView.layoutParams = params
                    textView.text = fullDescTextList1[i].trim()
                    textView.tag = "title"
                    textView.id = i
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                    textView.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
                    fullDescTextViewList.add(textView)
                    textView.setOnClickListener(this)
                    dynamicFullDescTextViewWrapper.addView(textView)
                }
            }
            else{
//                swapLayoutBtn.isChecked = true
                secondLinearLayout.visibility = View.GONE
                firstLinearLayout.visibility = View.VISIBLE

                defaultLayout = 1
                fullDescriptionBox.setText(pItem.fullDesc)

                titleBox.setText(pItem.title)
                titleBox.setSelection(pItem.title.length)

                BaseActivity.showSoftKeyboard(requireContext(),titleBox)
                titleBox.requestFocus()
            }
        }

//        swapLayoutBtn.setOnCheckedChangeListener { buttonView, isChecked ->
//            if (isChecked) {
//                secondLinearLayout.visibility = View.GONE
//                firstLinearLayout.visibility = View.VISIBLE
//
//                defaultLayout = 1
//                fullDescriptionBox.setText(pItem.fullDesc)
////                    if (insalesFragment!!.titleBox.text.toString().isNotEmpty()) {
//                titleBox.setText(pItem.title)
//                titleBox.setSelection(pItem.title.length)
////                    }
//                Constants.openKeyboar(requireContext())
//                titleBox.requestFocus()
//            } else {
////                Constants.hideKeyboar(requireContext())
//                //BaseActivity.startLoading(requireContext())
//                firstLinearLayout.visibility = View.GONE
//                secondLinearLayout.visibility = View.VISIBLE
//                defaultLayout = 0
//
//                titleTextViewList.clear()
//                shortDescTextViewList.clear()
//                fullDescTextViewList.clear()
//                dynamicTitleTextViewWrapper.removeAllViews()
//                dynamicShortDescTextViewWrapper.removeAllViews()
//                dynamicFullDescTextViewWrapper.removeAllViews()
//
//                val titleTextList1 = pItem.title.trim().split(" ")
//                val shortDescTextList1 = pItem.shortDesc.trim().split(" ")
//                val fullDescTextList1 = pItem.fullDesc.trim().split(" ")
//
//                for (i in 0 until titleTextList1.size) {
//                    val params = FlowLayout.LayoutParams(
//                        FlowLayout.LayoutParams.WRAP_CONTENT,
//                        FlowLayout.LayoutParams.WRAP_CONTENT
//                    )
//                    params.setMargins(5, 5, 5, 5)
//                    val textView = MaterialTextView(requireActivity())
//                    textView.layoutParams = params
//                    textView.text = titleTextList1[i].trim()
//                    textView.tag = "title"
//                    textView.id = i
//                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
//                    textView.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
//                    titleTextViewList.add(textView)
//                    textView.setOnClickListener(this)
//                    dynamicTitleTextViewWrapper.addView(textView)
//                }
//
//                for (i in 0 until shortDescTextList1.size) {
//                    val params = FlowLayout.LayoutParams(
//                        FlowLayout.LayoutParams.WRAP_CONTENT,
//                        FlowLayout.LayoutParams.WRAP_CONTENT
//                    )
//                    params.setMargins(5, 5, 5, 5)
//                    val textView = MaterialTextView(requireActivity())
//                    textView.layoutParams = params
//                    textView.text = shortDescTextList1[i].trim()
//                    textView.tag = "title"
//                    textView.id = i
//                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
//                    textView.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
//                    shortDescTextViewList.add(textView)
//                    textView.setOnClickListener(this)
//                    dynamicShortDescTextViewWrapper.addView(textView)
//                }
////
//                for (i in 0 until fullDescTextList1.size) {
//                    val params = FlowLayout.LayoutParams(
//                        FlowLayout.LayoutParams.WRAP_CONTENT,
//                        FlowLayout.LayoutParams.WRAP_CONTENT
//                    )
//                    params.setMargins(5, 5, 5, 5)
//                    val textView = MaterialTextView(requireActivity())
//                    textView.layoutParams = params
//                    textView.text = fullDescTextList1[i].trim()
//                    textView.tag = "title"
//                    textView.id = i
//                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
//                    textView.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
//                    fullDescTextViewList.add(textView)
//                    textView.setOnClickListener(this)
//                    dynamicFullDescTextViewWrapper.addView(textView)
//                }
//                //BaseActivity.dismiss()
//            }
//        }

        titleBox.setSelection(pItem.title.length)
        titleBox.addTextChangedListener(object : TextWatcher {
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
//                pItem.title = titleBox.text.toString()
            }

        })
        fullDescriptionBox.addTextChangedListener(object : TextWatcher {
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
//                pItem.fullDesc = fullDescriptionBox.text.toString()
            }

        })

        dialogCancelBtn.setOnClickListener {
            BaseActivity.hideSoftKeyboard(requireContext(), dialogCancelBtn)
            if (productImagesChanges) {
                MaterialAlertDialogBuilder(requireActivity())
                    .setCancelable(false)
                    .setMessage(requireActivity().resources.getString(R.string.exit_without_saving_warning_message))
                    .setNegativeButton(requireActivity().resources.getString(R.string.no_text)){dialog,which->
                        dialog.dismiss()
                    }
                    .setPositiveButton(requireActivity().resources.getString(R.string.yes_text)){dialog,which->
                        dialog.dismiss()
                        multiImagesList.clear()
                        dismiss()
                    }
                    .create().show()
            }
            else{
                if(firstLinearLayout.visibility == View.VISIBLE){
                    firstLinearLayout.visibility = View.GONE
                    secondLinearLayout.visibility = View.VISIBLE
                    defaultLayout = 0
                    swapLayoutBtn.isChecked = false

                }else{
                    dismiss()
                }

            }
        }

        dialogUpdateBtn.setOnClickListener {
            val titleText = titleBox.text.toString().trim()
            val shortDesc = productShortDescriptionBox.text.toString().trim()
            val fullDesc = fullDescriptionBox.text.toString().trim()

            if (defaultLayout == 0) {
                var stringBuilder = StringBuilder()

                for (i in 0 until (dynamicTitleTextViewWrapper as ViewGroup).childCount) {
                    val nextChild = (dynamicTitleTextViewWrapper as ViewGroup).getChildAt(i)
                    val text = (nextChild as MaterialTextView).text.toString()
                    stringBuilder.append(text)
                    stringBuilder.append(" ")
                }

                pItem.title = stringBuilder.toString().trim()
                stringBuilder = StringBuilder()

                for (i in 0 until (dynamicShortDescTextViewWrapper as ViewGroup).childCount) {
                    val nextChild =
                        (dynamicShortDescTextViewWrapper as ViewGroup).getChildAt(i)
                    val text = (nextChild as MaterialTextView).text.toString()
                    stringBuilder.append(text)
                    stringBuilder.append(" ")
                }

                pItem.shortDesc = stringBuilder.toString().trim()

                stringBuilder = StringBuilder()

                for (i in 0 until (dynamicFullDescTextViewWrapper as ViewGroup).childCount) {
                    val nextChild =
                        (dynamicFullDescTextViewWrapper as ViewGroup).getChildAt(i)
                    val text = (nextChild as MaterialTextView).text.toString()
                    stringBuilder.append(text)
                    stringBuilder.append(" ")
                }
                pItem.fullDesc = stringBuilder.toString().trim()
            } else {
                BaseActivity.hideSoftKeyboard(requireContext(), dialogUpdateBtn)
                pItem.title = titleText
                pItem.shortDesc = shortDesc
                pItem.fullDesc = fullDesc
            }
            defaultLayout = 0
            if (titleText.isNotEmpty()) {
                BaseActivity.startLoading(
                    requireActivity(),
                    getString(R.string.please_wait_product_update_message)
                )

                Paper.book().destroy()
                Paper.book()
                    .write(Constants.cacheProducts, MainActivity.originalProductsList)
                insalesAdapter.notifyItemChanged(position)

                viewModel.callUpdateProductDetail(
                    requireContext(),
                    shopName,
                    email,
                    password,
                    pItem.id,
                    pItem.title,
                    pItem.shortDesc,
                    pItem.fullDesc
                )
                viewModel.getUpdateProductDetailResponse()
                    .observe(requireActivity(), Observer { response ->
                        if (response != null) {
                            if (response.get("status").asString == "200") {
                                BaseActivity.dismiss()
                                if (multiImagesList.isNotEmpty()){
                                    Constants.startImageUploadService(pItem.id, multiImagesList.joinToString(","), "images", true)
                                }
                                if (selectedProductImages != null && selectedProductImages.isNotEmpty()) {
                                    Constants.startImageUploadService(Constants.pItem!!.id, selectedProductImages.joinToString(","), "images", true)
                                }
                                Constants.pItem = null
                                Constants.pItemPosition = null
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.product_updated_successfully),
                                    Toast.LENGTH_SHORT
                                ).show()
                                dismiss()
                                listener.onSuccess("")
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
            } else {
                BaseActivity.showAlert(
                    requireActivity(),
                    getString(R.string.empty_text_error)
                )
            }
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
                        requireActivity(),
                        imageUri.data
                    )
//                    selectedImageBase64String =
//                        ImageManager.convertImageToBase64(
//                            requireActivity(),
//                            currentPhotoPath!!
//                        )
//                    Log.d("TEST199", selectedImageBase64String)
                    Glide.with(requireActivity())
                        .load(currentPhotoPath)
                        .placeholder(R.drawable.placeholder)
                        .centerInside()
                        .into(selectedImageView)

                    if (adapter != null) {
                        barcodeImageList.add(currentPhotoPath!!)
                        multiImagesList.add(currentPhotoPath!!)
                        adapter!!.notifyDataSetChanged()
                    } else {
                        selectedImageBase64String =
                            ImageManager.convertImageToBase64(
                                requireActivity(),
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
                Glide.with(requireActivity())
                    .load(currentPhotoPath)
                    .placeholder(R.drawable.placeholder)
                    .centerInside()
                    .into(selectedImageView)

                if (adapter != null) {
                    barcodeImageList.add(currentPhotoPath!!)
                    multiImagesList.add(currentPhotoPath!!)
                    adapter!!.notifyDataSetChanged()
                } else {
                    selectedImageBase64String =
                        ImageManager.convertImageToBase64(
                            requireActivity(),
                            currentPhotoPath!!
                        )
                }

            }
        }

    private fun createImageFile(bitmap: Bitmap) {
        currentPhotoPath = ImageManager.readWriteImage(requireActivity(), bitmap).absolutePath
    }

    private var barcodeImageResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // THIS LINE OF CODE WILL CHECK THE IMAGE HAS BEEN SELECTED OR NOT
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data != null && result.data!!.hasExtra("SCANNED_BARCODE_VALUE")) {
                    val barcodeId = result.data!!.getStringExtra("SCANNED_BARCODE_VALUE") as String
                    if (barcodeId.isNotEmpty()) {
                        if (barcodeSearchHint == "default") {
                            //search(barcodeId, "sku")
                        } else {
                            searchBoxView.setText(barcodeId)
                            Constants.hideKeyboar(requireActivity())
                            startSearch(
                                searchBoxView,
                                searchBtnView,
                                loader,
                                searchedImagesList as ArrayList<String>,
                                internetImageAdapter
                            )
                        }

                    }
                }


            } else {
                Constants.listUpdateFlag = 0
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
                    searchBoxView.setText(spokenText)
                    Constants.hideKeyboar(requireActivity())
                    startSearch(
                        searchBoxView, searchBtnView, loader,
                        searchedImagesList as ArrayList<String>, internetImageAdapter
                    )
                } else {
//                            searchBox.setText(spokenText)
//                            search(spokenText, "default")
//                            voiceSearchHint = "default"
                }
            }
        }

    private fun startSearch(
        searchBoxView: TextInputEditText,
        searchBtnView: ImageButton,
        loader: ProgressBar,
        searchedImagesList: ArrayList<String>,
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

                                            internetImageAdapter.notifyItemRangeChanged(
                                                0,
                                                searchedImagesList.size
                                            )
                                            internetImageDoneBtn.visibility = View.VISIBLE
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

            BaseActivity.showAlert(
                requireActivity(),
                getString(R.string.empty_text_error)
            )
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onClick(v: View?) {
        val view = v!!
        when (view.id) {
            else -> {
                val position = view.id
                val textView = view as MaterialTextView
                view.setBackgroundColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.primary_positive_color
                    )
                )
                view.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))

                val balloon = Balloon.Builder(requireActivity())
                    .setLayout(R.layout.ballon_layout_design)
                    .setArrowSize(10)
                    .setArrowOrientation(ArrowOrientation.TOP)
                    .setArrowPosition(0.5f)
                    .setWidthRatio(0.55f)
                    .setCornerRadius(4f)
                    .setBackgroundColor(
                        ContextCompat.getColor(
                            requireActivity(),
                            R.color.light_gray
                        )
                    )
                    .setBalloonAnimation(BalloonAnimation.ELASTIC)
                    .setLifecycleOwner(this)
                    .build()


                val editTextBox = balloon.getContentView()
                    .findViewById<TextInputEditText>(R.id.balloon_edit_text)
                editTextBox.setText(textView.text.toString().trim())
                val clearTextView = balloon.getContentView()
                    .findViewById<AppCompatImageView>(R.id.balloon_brush_clear_view)
                val closeBtn = balloon.getContentView()
                    .findViewById<AppCompatButton>(R.id.balloon_close_btn)
                val applyBtn = balloon.getContentView()
                    .findViewById<AppCompatButton>(R.id.balloon_apply_btn)
                balloon.showAlignTop(textView)
                editTextBox.requestFocus()
                Constants.openKeyboar(requireActivity())
                closeBtn.setOnClickListener {
                    Constants.hideKeyboar(requireActivity())
                    balloon.dismiss()
                    view.setBackgroundColor(
                        ContextCompat.getColor(
                            requireActivity(),
                            R.color.white
                        )
                    )
                    view.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
                }

                clearTextView.setOnClickListener {
                    editTextBox.setText("")
                }

                applyBtn.setOnClickListener {
                    Constants.hideKeyboar(requireActivity())
                    balloon.dismiss()
                    //val tempText = textView.replace(mWord,editTextBox.text.toString().trim())
                    textView.text = editTextBox.text.toString().trim()
                    view.setBackgroundColor(
                        ContextCompat.getColor(
                            requireActivity(),
                            R.color.white
                        )
                    )
                    view.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))

                }
            }
        }
    }

}