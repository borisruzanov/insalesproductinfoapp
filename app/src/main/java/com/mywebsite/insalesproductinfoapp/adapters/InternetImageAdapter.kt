package com.mywebsite.insalesproductinfoapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.mywebsite.insalesproductinfoapp.R
import com.mywebsite.insalesproductinfoapp.databinding.SearchImageItemRowDesignBinding

class InternetImageAdapter(private val context: Context, private val imagesList: ArrayList<String>) : RecyclerView.Adapter<InternetImageAdapter.ItemViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onItemAttachClick(btn:MaterialButton,position: Int)
    }

    private var mListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    open class ItemViewHolder(private val binding: SearchImageItemRowDesignBinding, private val listener: OnItemClickListener) : RecyclerView.ViewHolder(binding.root) {

        fun bindData(image:String,context: Context,position: Int){
            Glide.with(context)
                .load(image)
                .thumbnail(Glide.with(context).load(R.drawable.loader))
                .fitCenter()
                .into(binding.searchImageView)

            binding.searchImageView.setOnClickListener {
                listener.onItemClick(layoutPosition)
            }
            binding.searchImageAttachBtn.tag = "attach"
            binding.searchImageAttachBtn.setOnClickListener {
                listener.onItemAttachClick(binding.searchImageAttachBtn,layoutPosition)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {

        val view = SearchImageItemRowDesignBinding.inflate(LayoutInflater.from(parent.context),parent,false)//LayoutInflater.from(parent.context).inflate(R.layout.search_image_item_row_design, parent, false)
        return ItemViewHolder(view, mListener!!)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val image = imagesList[position]
        holder.bindData(image,context,position)
    }

    override fun getItemCount(): Int {
        return imagesList.size
    }

}