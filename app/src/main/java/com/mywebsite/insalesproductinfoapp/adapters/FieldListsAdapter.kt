package com.mywebsite.insalesproductinfoapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.mywebsite.insalesproductinfoapp.R
import com.mywebsite.insalesproductinfoapp.model.ListItem
import com.mywebsite.insalesproductinfoapp.utils.AppSettings

class FieldListsAdapter(val context: Context, val listItems: ArrayList<ListItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onAddItemClick(position: Int)
    }

    private var mListener: OnItemClickListener? = null
    private var appSettings = AppSettings(context)

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    class ItemViewHolder(itemView: View, Listener: OnItemClickListener) :
        RecyclerView.ViewHolder(itemView) {
        val listValueView: MaterialTextView = itemView.findViewById(R.id.table_item_name)
    }

    class AddItemViewHolder(itemView: View, mListener: OnItemClickListener) :
        RecyclerView.ViewHolder(itemView) {
        val addCardViewBtn: CardView = itemView.findViewById(R.id.add_card_view)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.add_list_value_item_layout,
                parent,
                false
            )
            AddItemViewHolder(view, mListener!!)
        } else {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.table_item_row,
            parent,
            false
        )
        return ItemViewHolder(view, mListener!!)
        }
    }


    override fun getItemViewType(position: Int): Int {
        var viewType = 1 //Default Layout is 1
        if (position == listItems.size) viewType = 0 //if zero, it will be a header view
        return viewType
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            0 -> {
                val addViewHolder = holder as AddItemViewHolder
                addViewHolder.addCardViewBtn.setOnClickListener {
                    mListener!!.onAddItemClick(position)
                }
                openAddListTipsDialog(addViewHolder.itemView)
            }
            else -> {
        val listValue = listItems[position]
                val viewHolder = holder as ItemViewHolder
                viewHolder.listValueView.text = listValue.value
                viewHolder.itemView.setOnClickListener {
            mListener!!.onItemClick(position)
        }
            }
        }
    }

    override fun getItemCount(): Int {
        return listItems.size+1
    }

    private fun openAddListTipsDialog(itemView: View) {
//        if (appSettings.getBoolean(context.resources.getString(R.string.key_tips))) {
//            val duration = appSettings.getLong("tt22")
//            if (duration.compareTo(0) == 0 || System.currentTimeMillis()-duration > TimeUnit.DAYS.toMillis(1) ) {
//                SimpleTooltip.Builder(context)
//                    .anchorView(itemView)
//                    .text(context.resources.getString(R.string.tt22_tip_text))
//                    .gravity(Gravity.BOTTOM)
//                    .animated(true)
//                    .transparentOverlay(false)
//                    .onDismissListener { tooltip ->
//                        appSettings.putLong("tt22",System.currentTimeMillis())
//                        tooltip.dismiss()
//                    }
//                    .build()
//                    .show()
//            }
//        }
    }

}