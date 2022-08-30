package com.mywebsite.insalesproductinfoapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.boris.expert.csvmagic.model.ProductImages
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.mywebsite.insalesproductinfoapp.R
import com.mywebsite.insalesproductinfoapp.databinding.InsalesProductAddImageDesignBinding
import com.mywebsite.insalesproductinfoapp.databinding.InsalesProductImagesItemRowDesignBinding
import com.squareup.picasso.Picasso

class ProductImagesAdapter(private val context: Context, private val productImagesList: ArrayList<ProductImages>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onItemEditClick(btn: MaterialButton, position: Int)
        fun onItemRemoveClick(position: Int)
        fun onItemAddImageClick(position: Int)
    }

    private var mListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    open class ItemViewHolder(private val binding: InsalesProductImagesItemRowDesignBinding,private val mListener: OnItemClickListener) : RecyclerView.ViewHolder(binding.root) {

        fun bindData(item:ProductImages,context: Context,position: Int){

            Glide.with(context)
                .load(item.imageUrl)
                .thumbnail(Glide.with(context).load(R.drawable.loader))
                .fitCenter()
                .into(binding.insalesProductImageView)
//                Picasso.get().load(item.imageUrl).placeholder(R.drawable.loader).fit().centerInside().into(binding.insalesProductImageView)

            binding.insalesProductImageView.setOnClickListener {
                mListener.onItemClick(position)
            }

            binding.insalesProductImageEditBtn.setOnClickListener {
                mListener.onItemEditClick(binding.insalesProductImageEditBtn,position)
            }

            binding.insalesProductRemoveView.setOnClickListener {
                mListener.onItemRemoveClick(position)
            }
        }

    }

    open class AddItemViewHolder(private val binding: InsalesProductAddImageDesignBinding, private val mListener: OnItemClickListener) :
            RecyclerView.ViewHolder(binding.root) {

        fun bindData(size:Int,position: Int){
            if (size == 0){
                binding.insalesAddImageWrapperLayout.setBackgroundResource(R.drawable.empty_image_background)
            }
            else{
                binding.insalesAddImageWrapperLayout.setBackgroundResource(R.drawable.without_empty_image_background)
            }
            binding.insalesAddImageWrapperLayout.setOnClickListener {
                mListener.onItemAddImageClick(position)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {


        return if (viewType == 0) {
            val view = InsalesProductAddImageDesignBinding.inflate(LayoutInflater.from(parent.context),parent,false)//LayoutInflater.from(parent.context).inflate(R.layout.insales_product_add_image_design, parent, false)
            AddItemViewHolder(view, mListener!!)
        } else {
            val view = InsalesProductImagesItemRowDesignBinding.inflate(LayoutInflater.from(parent.context),parent,false)//LayoutInflater.from(parent.context).inflate(R.layout.insales_product_images_item_row_design, parent, false)
            ItemViewHolder(view, mListener!!)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder.itemViewType) {
            0 -> {
                val addViewHolder = holder as AddItemViewHolder
                addViewHolder.bindData(productImagesList.size,position)
            }
            else -> {
                val image = productImagesList[position-1]
                val viewHolder = holder as ItemViewHolder
                viewHolder.bindData(image,context,position-1)
            }
        }

    }


    override fun getItemCount(): Int {
        return productImagesList.size+1
    }

    override fun getItemViewType(position: Int): Int {
        var viewType = 1 //Default Layout is 1
        if (position == 0) viewType = 0 //if zero, it will be a header view
        return viewType
    }
}