package com.mywebsite.insalesproductinfoapp.adapters

import android.content.Context
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.boris.expert.csvmagic.model.ProductImages
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.mywebsite.insalesproductinfoapp.R
import com.mywebsite.insalesproductinfoapp.databinding.InsalesProductsItemRowDesignBinding
import com.mywebsite.insalesproductinfoapp.databinding.InsalesProductsItemRowMinimalDesignBinding
import com.mywebsite.insalesproductinfoapp.model.Product
import com.mywebsite.insalesproductinfoapp.utils.WrapContentLinearLayoutManager
import net.expandable.ExpandableTextView

class InSalesProductsAdapter(
    val context: Context,
    var productsItems: List<Product>,
    private val type: Int
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onItemEditClick(position: Int, imagePosition: Int)
        fun onItemAddImageClick(position: Int)
        fun onItemRemoveClick(position: Int, imagePosition: Int)
        fun onItemEditImageClick(position: Int)
        fun onItemGrammarCheckClick(
            position: Int,
            grammarCheckBtn: AppCompatImageView,
            title: ExpandableTextView,
            description: ExpandableTextView,
            grammarStatusView: MaterialTextView
        )

        fun onItemGetDescriptionClick(position: Int)
        fun onItemCameraIconClick(
            position: Int,
            title: ExpandableTextView,
            description: ExpandableTextView
        )

        fun onItemImageIconClick(
            position: Int,
            title: ExpandableTextView,
            description: ExpandableTextView
        )
    }

    private var productViewListType = type
    private var mListener: OnItemClickListener? = null

    fun updateListType(type: Int) {
        productViewListType = type
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    class ItemViewHolder1(
        private val binding: InsalesProductsItemRowMinimalDesignBinding,
        private val mListener: OnItemClickListener
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindData(item: Product, context: Context, position: Int) {
            if (item.productImages!!.size > 0) {
                Glide.with(context).load(item.productImages!![0].imageUrl)
                    .into(binding.insalesPItemImageView)
            }

            if (item.sku.isEmpty()) {
                binding.skuBarcodeTextview.text = "Sku/Barcode: None"
            } else {
                binding.skuBarcodeTextview.text = "Sku/Barcode: ${item.sku}"
            }

            binding.totalTitleSizeTextview.setText("Title Size: ${item.title.length}")
            binding.totalDescriptionSizeTextview.setText("Description Size: ${item.fullDesc.length}")
            binding.totalImagesSizeTextview.setText("Total Images: ${item.productImages!!.size}")
            if (item.title.length > 10) {
                binding.insalesPItemTitle.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.white
                    )
                )
            } else {
                //holder.productTitle.text = context.getString(R.string.product_title_error)
                binding.insalesPItemTitle.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.light_red
                    )
                )
            }

            binding.insalesPItemTitle.text = item.title
            binding.insalesPItemTitle.isExpanded = false
            binding.insalesPItemTitle.movementMethod = ScrollingMovementMethod.getInstance()

            if (item.fullDesc.length > 10) {
                binding.insalesPItemDescription.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.white
                    )
                )
            } else {
                //holder.productDescription.text = context.getString(R.string.product_description_error)
                binding.insalesPItemDescription.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.light_red
                    )
                )
            }

            binding.insalesPItemDescription.text = item.fullDesc
            binding.insalesPItemDescription.isExpanded = false
            binding.insalesPItemDescription.movementMethod = ScrollingMovementMethod.getInstance()

            binding.collapseExpandImg.setOnClickListener {
                if (binding.insalesPItemTitle.isExpanded) {
                    binding.insalesPItemTitle.isExpanded = false
//                    collapseExpandLayout.visibility = View.GONE
                    binding.collapseExpandImg.setImageResource(R.drawable.ic_arrow_down)
                } else {
                    binding.insalesPItemTitle.isExpanded = true
//                    collapseExpandLayout.visibility = View.VISIBLE
                    binding.collapseExpandImg.setImageResource(R.drawable.ic_arrow_up)
                }
            }

            binding.collapseExpandDescriptionImg.setOnClickListener {
                if (binding.insalesPItemDescription.isExpanded) {
                    binding.insalesPItemDescription.isExpanded = false
//                    collapseExpandLayout.visibility = View.GONE
                    binding.collapseExpandDescriptionImg.setImageResource(R.drawable.ic_arrow_down)
                } else {
                    binding.insalesPItemDescription.isExpanded = true
//                    collapseExpandLayout.visibility = View.VISIBLE
                    binding.collapseExpandDescriptionImg.setImageResource(R.drawable.ic_arrow_up)
                }
            }

            binding.insalesPItemTitle.setOnExpandableClickListener(
                onExpand = { // Expand action
                    binding.collapseExpandImg.setImageResource(R.drawable.ic_arrow_up)
                },
                onCollapse = { // Collapse action
                    binding.collapseExpandImg.setImageResource(R.drawable.ic_arrow_down)
                }
            )

            binding.insalesPItemDescription.setOnExpandableClickListener(
                onExpand = { // Expand action
                    binding.collapseExpandDescriptionImg.setImageResource(R.drawable.ic_arrow_up)
                },
                onCollapse = { // Collapse action
                    binding.collapseExpandDescriptionImg.setImageResource(R.drawable.ic_arrow_down)
                }
            )
        }

        init {
//            titleScrollView = itemView.findViewById(R.id.title_scrollbar)
//            descriptionScrollView = itemView.findViewById(R.id.description_scrollbar)

//            titleScrollView.setOnTouchListener(OnTouchListener { v, event -> // Disallow the touch request for parent scroll on touch of child view
//                val isLarger: Boolean
//                isLarger = (v as ExpandableTextView).lineCount * v.lineHeight > v.getHeight()
//                if (event.action === MotionEvent.ACTION_MOVE && isLarger) {
//                    v.getParent().requestDisallowInterceptTouchEvent(true)
//                } else {
//                    v.getParent().requestDisallowInterceptTouchEvent(false)
//                }
//                return@OnTouchListener false
//            })
//
//            descriptionScrollView.setOnTouchListener(OnTouchListener { v, event -> // Disallow the touch request for parent scroll on touch of child view
//                val isLarger: Boolean
//                isLarger = (v as ExpandableTextView).lineCount * v.lineHeight > v.getHeight()
//                if (event.action === MotionEvent.ACTION_MOVE && isLarger) {
//                    v.getParent().requestDisallowInterceptTouchEvent(true)
//                } else {
//                    v.getParent().requestDisallowInterceptTouchEvent(false)
//                }
//                return@OnTouchListener false
//            })

//            productTitle.setOnClickListener { v ->
//                Listener.onItemClick(layoutPosition)
//            }
//
//            productDescription.setOnClickListener(View.OnClickListener { v ->
//
//            })
//
//
//            editImageView.setOnClickListener {
//                Listener.onItemEditImageClick(layoutPosition)
//            }
//
//            insalesItemEditTextview.setOnClickListener {
//                Listener.onItemEditImageClick(layoutPosition)
//            }
//
//            grammarCheckView.setOnClickListener {
//                Listener.onItemGrammarCheckClick(
//                        layoutPosition,
//                        grammarCheckView,
//                        productTitle,
//                        productDescription,
//                        grammarStatusView
//                )
//            }
//
//            getDescriptionBtn.setOnClickListener {
//                Listener.onItemGetDescriptionClick(layoutPosition)
//            }
//
//            cameraIconView.setOnClickListener {
//                Listener.onItemCameraIconClick(layoutPosition, productTitle, productDescription)
//            }
//            imageIconView.setOnClickListener {
//                Listener.onItemImageIconClick(layoutPosition, productTitle, productDescription)
//            }
//        }
        }
    }

    class ItemViewHolder(
        private val binding: InsalesProductsItemRowDesignBinding,
        private val mListener: OnItemClickListener
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindData(item: Product, context: Context, position: Int) {
            if (item.sku.isEmpty()) {
                binding.skuBarcodeTextview.text = "Sku/Barcode: None"
            } else {
                binding.skuBarcodeTextview.text = "Sku/Barcode: ${item.sku}"
            }

            binding.totalTitleSizeTextview.text = "Title Size: ${item.title.length}"
            binding.totalDescriptionSizeTextview.text = "Description Size: ${item.fullDesc.length}"
            binding.totalImagesSizeTextview.text = "Total Images: ${item.productImages!!.size}"
            if (item.title.length > 10) {
                binding.insalesPItemTitle.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.white
                    )
                )
            } else {
                //holder.productTitle.text = context.getString(R.string.product_title_error)
                binding.insalesPItemTitle.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.light_red
                    )
                )
            }

            binding.insalesPItemTitle.text = item.title
            binding.insalesPItemTitle.isExpanded = false
            binding.insalesPItemTitle.movementMethod = ScrollingMovementMethod.getInstance()

            if (item.fullDesc.length > 10) {
                binding.insalesPItemDescription.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.white
                    )
                )
            } else {
                //holder.productDescription.text = context.getString(R.string.product_description_error)
                binding.insalesPItemDescription.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.light_red
                    )
                )
            }

            binding.insalesPItemDescription.text = item.fullDesc
            binding.insalesPItemDescription.isExpanded = false
            binding.insalesPItemDescription.movementMethod = ScrollingMovementMethod.getInstance()
            item.productImages!!.sortByDescending { it.id }
            binding.productsImagesRecyclerview.layoutManager =
                WrapContentLinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            binding.productsImagesRecyclerview.hasFixedSize()
            val adapter = ProductImagesAdapter(
                context,
                item.productImages as java.util.ArrayList<ProductImages>
            )
            binding.productsImagesRecyclerview.adapter = adapter
            adapter.setOnItemClickListener(object : ProductImagesAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {

                }

                override fun onItemEditClick(btn: MaterialButton, imagePosition: Int) {
                    mListener.onItemEditClick(position, imagePosition)
                }

                override fun onItemRemoveClick(imagePosition: Int) {
                    mListener.onItemRemoveClick(position, imagePosition)
                }

                override fun onItemAddImageClick(pos: Int) {
                    mListener.onItemAddImageClick(position)
                }

            })
            if (item.productImages!!.size > 0) {
                adapter.notifyItemRangeChanged(0, item.productImages!!.size)
            } else {
                adapter.notifyDataSetChanged()
            }

            binding.collapseExpandImg.setOnClickListener {
                if (binding.insalesPItemTitle.isExpanded) {
                    binding.insalesPItemTitle.isExpanded = false
//                    collapseExpandLayout.visibility = View.GONE
                    binding.collapseExpandImg.setImageResource(R.drawable.ic_arrow_down)
                } else {
                    binding.insalesPItemTitle.isExpanded = true
//                    collapseExpandLayout.visibility = View.VISIBLE
                    binding.collapseExpandImg.setImageResource(R.drawable.ic_arrow_up)
                }
            }

            binding.collapseExpandDescriptionImg.setOnClickListener {
                if (binding.insalesPItemDescription.isExpanded) {
                    binding.insalesPItemDescription.isExpanded = false
//                    collapseExpandLayout.visibility = View.GONE
                    binding.collapseExpandDescriptionImg.setImageResource(R.drawable.ic_arrow_down)
                } else {
                    binding.insalesPItemDescription.isExpanded = true
//                    collapseExpandLayout.visibility = View.VISIBLE
                    binding.collapseExpandDescriptionImg.setImageResource(R.drawable.ic_arrow_up)
                }
            }

            binding.insalesPItemTitle.setOnExpandableClickListener(
                onExpand = { // Expand action
                    binding.collapseExpandImg.setImageResource(R.drawable.ic_arrow_up)
                },
                onCollapse = { // Collapse action
                    binding.collapseExpandImg.setImageResource(R.drawable.ic_arrow_down)
                }
            )

            binding.insalesPItemDescription.setOnExpandableClickListener(
                onExpand = { // Expand action
                    binding.collapseExpandDescriptionImg.setImageResource(R.drawable.ic_arrow_up)
                },
                onCollapse = { // Collapse action
                    binding.collapseExpandDescriptionImg.setImageResource(R.drawable.ic_arrow_down)
                }
            )
        }

//        val productTitle: ExpandableTextView
//        val productDescription: ExpandableTextView
//        val imagesRecyclerView: RecyclerView
//
//        //        val addImageView:AppCompatImageView
//        val editImageView: AppCompatImageView
//        val grammarCheckView: AppCompatImageView
//        val grammarStatusView: MaterialTextView
//        val titleSizeView: MaterialTextView
//        val descriptionSizeView: MaterialTextView
//        val totalImagesView: MaterialTextView
//        val skuBarcodeView: MaterialTextView
//        val collapseExpandImg: AppCompatImageView
//        val collapseExpandDescriptionImg: AppCompatImageView
////        val collapseExpandLayout: LinearLayout
//        val insalesItemEditTextview:MaterialTextView
//
//        //        val sliderView:SliderView
////        val addProductCard: CardView
//        val getDescriptionBtn: AppCompatImageView
//        val cameraIconView: AppCompatImageView
//        val imageIconView: AppCompatImageView

        init {
//            productTitle = itemView.findViewById(R.id.insales_p_item_title)
//            productDescription = itemView.findViewById(R.id.insales_p_item_description)
//            imagesRecyclerView = itemView.findViewById(R.id.products_images_recyclerview)
////            addImageView = itemView.findViewById(R.id.insales_p_item_add_image)
//            editImageView = itemView.findViewById(R.id.insales_p_item_edit_image)
//            insalesItemEditTextview = itemView.findViewById(R.id.insales_item_edit_textview)
//            grammarCheckView = itemView.findViewById(R.id.grammar_check_icon_view)
//            grammarStatusView = itemView.findViewById(R.id.grammar_status_textview)
//            titleSizeView = itemView.findViewById(R.id.total_title_size_textview)
//            skuBarcodeView = itemView.findViewById(R.id.sku_barcode_textview)
//            descriptionSizeView = itemView.findViewById(R.id.total_description_size_textview)
//            totalImagesView = itemView.findViewById(R.id.total_images_size_textview)
////            collapseExpandLayout = itemView.findViewById(R.id.collapse_expand_layout)
//            collapseExpandImg = itemView.findViewById(R.id.collapse_expand_img)
//            collapseExpandDescriptionImg = itemView.findViewById(R.id.collapse_expand_description_img)
////            sliderView = itemView.findViewById(R.id.imageSlider)
////            addProductCard = itemView.findViewById(R.id.add_product_card)
//            getDescriptionBtn = itemView.findViewById(R.id.get_description_text_view)
//            cameraIconView = itemView.findViewById(R.id.insales_item_photo_icon_view)
//            imageIconView = itemView.findViewById(R.id.insales_item_image_icon_view)

            //            titleScrollView = itemView.findViewById(R.id.title_scrollbar)
//            descriptionScrollView = itemView.findViewById(R.id.description_scrollbar)

//            titleScrollView.setOnTouchListener(OnTouchListener { v, event -> // Disallow the touch request for parent scroll on touch of child view
//                val isLarger: Boolean
//                isLarger = (v as ExpandableTextView).lineCount * v.lineHeight > v.getHeight()
//                if (event.action === MotionEvent.ACTION_MOVE && isLarger) {
//                    v.getParent().requestDisallowInterceptTouchEvent(true)
//                } else {
//                    v.getParent().requestDisallowInterceptTouchEvent(false)
//                }
//                return@OnTouchListener false
//            })
//
//            descriptionScrollView.setOnTouchListener(OnTouchListener { v, event -> // Disallow the touch request for parent scroll on touch of child view
//                val isLarger: Boolean
//                isLarger = (v as ExpandableTextView).lineCount * v.lineHeight > v.getHeight()
//                if (event.action === MotionEvent.ACTION_MOVE && isLarger) {
//                    v.getParent().requestDisallowInterceptTouchEvent(true)
//                } else {
//                    v.getParent().requestDisallowInterceptTouchEvent(false)
//                }
//                return@OnTouchListener false
//            })

//            productTitle.setOnClickListener { v ->
//                Listener.onItemClick(layoutPosition)
//            }
//
//            productDescription.setOnClickListener(View.OnClickListener { v ->
//
//            })
//
//
//            editImageView.setOnClickListener {
//                Listener.onItemEditImageClick(layoutPosition)
//            }
//
//            insalesItemEditTextview.setOnClickListener {
//                Listener.onItemEditImageClick(layoutPosition)
//            }
//
//            grammarCheckView.setOnClickListener {
//                Listener.onItemGrammarCheckClick(
//                    layoutPosition,
//                    grammarCheckView,
//                    productTitle,
//                    productDescription,
//                    grammarStatusView
//                )
//            }
//
//            getDescriptionBtn.setOnClickListener {
//                Listener.onItemGetDescriptionClick(layoutPosition)
//            }
//
//            cameraIconView.setOnClickListener {
//                Listener.onItemCameraIconClick(layoutPosition, productTitle, productDescription)
//            }
//            imageIconView.setOnClickListener {
//                Listener.onItemImageIconClick(layoutPosition, productTitle, productDescription)
//            }
        }
    }

    fun updateList(list: List<Product>) {
        productsItems = ArrayList(list)
        notifyItemChanged(0, productsItems.size)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        if (viewType == 0) {
            val view = InsalesProductsItemRowDesignBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ItemViewHolder(view, mListener!!)
        } else {
            val view = InsalesProductsItemRowMinimalDesignBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ItemViewHolder1(view, mListener!!)
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val item = productsItems[position]

        if (holder.itemViewType == 0) {
            val itemViewHolder = holder as ItemViewHolder
            itemViewHolder.bindData(item, context, position)
        } else {
            val itemViewHolder1 = holder as ItemViewHolder1
            itemViewHolder1.bindData(item, context, position)
        }
    }

    override fun getItemCount(): Int {
        return productsItems.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (productViewListType == 0) {
            0
        } else {
            1
        }
    }


}