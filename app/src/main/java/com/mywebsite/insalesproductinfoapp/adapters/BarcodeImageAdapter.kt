package com.mywebsite.insalesproductinfoapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mywebsite.insalesproductinfoapp.R

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

    open class ItemViewHolder(itemView: View, listener: OnItemClickListener) : RecyclerView.ViewHolder(itemView) {
        val barcodeImageView: AppCompatImageView
        val editImageView: AppCompatImageView
        val deleteImageView: AppCompatImageView

        init {
            barcodeImageView = itemView.findViewById(R.id.barcode_image_item_view)
            editImageView = itemView.findViewById(R.id.barcode_image_edit_btn)
            deleteImageView = itemView.findViewById(R.id.barcode_image_delete_btn)

            barcodeImageView.setOnClickListener {
                listener.onImageClick(layoutPosition)
            }

            editImageView.setOnClickListener {
                listener.onAddItemEditClick(layoutPosition)
            }

            deleteImageView.setOnClickListener {
                listener.onItemDeleteClick(layoutPosition)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.barcode_image_item_design, parent, false)
        return ItemViewHolder(view, mListener!!)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        var tempImg = ""
        val image = imagesList[position]
        tempImg = if (image.contains("itmagicapp.com")) {
            image.replace("itmagicapp.com", "itmagic.app")
        } else {
            image
        }
        Glide.with(context)
                .load(tempImg)
                .placeholder(R.drawable.placeholder)
                .centerInside()
                .into(holder.barcodeImageView)

    }

    override fun getItemCount(): Int {
        return imagesList.size
    }

}