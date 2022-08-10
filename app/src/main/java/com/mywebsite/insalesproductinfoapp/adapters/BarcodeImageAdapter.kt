package com.mywebsite.insalesproductinfoapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mywebsite.insalesproductinfoapp.R
import com.mywebsite.insalesproductinfoapp.databinding.BarcodeImageItemDesignBinding

class BarcodeImageAdapter(private val context: Context, private val imagesList: ArrayList<String>) : RecyclerView.Adapter<BarcodeImageAdapter.ItemViewHolder>() {

    interface OnItemClickListener {
        fun onItemDeleteClick(position: Int)
        fun onAddItemEditClick(position: Int)
        fun onImageClick(position: Int)
    }

    private var mListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    open class ItemViewHolder(private val binding:BarcodeImageItemDesignBinding,private val listener: OnItemClickListener) : RecyclerView.ViewHolder(binding.root) {

        fun bindData(item:String,context: Context,position: Int){
            Glide.with(context)
                .load(item)
                .placeholder(R.drawable.placeholder)
                .centerInside()
                .into(binding.barcodeImageItemView)

            binding.barcodeImageItemView.setOnClickListener {
                listener.onImageClick(layoutPosition)
            }

            binding.barcodeImageEditBtn.setOnClickListener {
                listener.onAddItemEditClick(layoutPosition)
            }

            binding.barcodeImageDeleteBtn.setOnClickListener {
                listener.onItemDeleteClick(layoutPosition)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {

        val layoutBinding = BarcodeImageItemDesignBinding.inflate(LayoutInflater.from(parent.context),parent,false)//LayoutInflater.from(parent.context).inflate(R.layout.barcode_image_item_design, parent, false)
        return ItemViewHolder(layoutBinding, mListener!!)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        var tempImg = ""
        val image = imagesList[position]
        tempImg = if (image.contains("itmagicapp.com")) {
            image.replace("itmagicapp.com", "itmagic.app")
        } else {
            image
        }

        holder.bindData(tempImg,context,position)

    }

    override fun getItemCount(): Int {
        return imagesList.size
    }

}