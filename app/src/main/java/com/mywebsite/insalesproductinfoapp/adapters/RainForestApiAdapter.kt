package com.mywebsite.insalesproductinfoapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.mywebsite.insalesproductinfoapp.R
import com.mywebsite.insalesproductinfoapp.databinding.RainForestApiItemRowDesignBinding
import com.mywebsite.insalesproductinfoapp.model.RainForestApiObject
import java.util.*


class RainForestApiAdapter(
    private val context: Context,
    val rainForestApiList: ArrayList<RainForestApiObject>
) : RecyclerView.Adapter<RainForestApiAdapter.ItemViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
    var count = 0
    private var mListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    inner class ItemViewHolder(private val binding:RainForestApiItemRowDesignBinding,private val listener: OnItemClickListener) :
        RecyclerView.ViewHolder(
            binding.root
        ) {

        fun bindData(item:RainForestApiObject,context: Context,position: Int){
            Glide.with(context).load(item.image).into(binding.rfaItemImageView)
//        GcpTranslator.translateFromEngToRus(context,item.title,object : TranslationCallback {
//            override fun onTextTranslation(translatedText: String) {
//                BaseActivity.dismiss()
//                if (translatedText.isNotEmpty()){
//                    holder.title.text = translatedText
//                }
//                else{
            binding.rfaItemTitleView.text = item.title
//                }
//
//            }
//        })

            binding.rfaItemGetDescriptionView.setOnClickListener {
                mListener!!.onItemClick(layoutPosition)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {

            val layoutBinding = RainForestApiItemRowDesignBinding.inflate(LayoutInflater.from(parent.context),parent,false)//LayoutInflater.from(parent.context).inflate(R.layout.rain_forest_api_item_row_design, parent, false)

            return ItemViewHolder(layoutBinding, mListener!!)

    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = rainForestApiList[position]
        holder.bindData(item,context,position)
    }

    override fun getItemCount(): Int {
        return rainForestApiList.size
    }

}