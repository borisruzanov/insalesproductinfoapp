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
import com.mywebsite.insalesproductinfoapp.model.RainForestApiObject
import java.util.*


class RainForestApiAdapter(
    private val context: Context,
    val rainForestApiList: ArrayList<RainForestApiObject>
) : RecyclerView.Adapter<RainForestApiAdapter.VideoItemViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
    var count = 0
    private var mListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    inner class VideoItemViewHolder(itemView: View, listener: OnItemClickListener) :
        RecyclerView.ViewHolder(
            itemView
        ) {
        var title: MaterialTextView
        var image: AppCompatImageView
        var getDescription:MaterialButton

        init {

            title = itemView.findViewById(R.id.rfa_item_title_view)
            image = itemView.findViewById(R.id.rfa_item_image_view)
            getDescription = itemView.findViewById(R.id.rfa_item_get_description_view)

            getDescription.setOnClickListener {
                mListener!!.onItemClick(layoutPosition)
            }

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoItemViewHolder {

            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.rain_forest_api_item_row_design,
                parent,
                false
            )

            return VideoItemViewHolder(view, mListener!!)

    }

    override fun onBindViewHolder(holder: VideoItemViewHolder, position: Int) {
        val item = rainForestApiList[position]

        Glide.with(context).load(item.image).into(holder.image)
//        GcpTranslator.translateFromEngToRus(context,item.title,object : TranslationCallback {
//            override fun onTextTranslation(translatedText: String) {
//                BaseActivity.dismiss()
//                if (translatedText.isNotEmpty()){
//                    holder.title.text = translatedText
//                }
//                else{
                    holder.title.text = item.title
//                }
//
//            }
//        })

    }

    override fun getItemCount(): Int {
        return rainForestApiList.size
    }

}