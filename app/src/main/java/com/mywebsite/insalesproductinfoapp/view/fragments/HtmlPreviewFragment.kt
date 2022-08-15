package com.mywebsite.insalesproductinfoapp.view.fragments

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.textview.MaterialTextView
import com.mywebsite.insalesproductinfoapp.R


class HtmlPreviewFragment(private val fullDesc:String) : DialogFragment() {

    private lateinit var textView:MaterialTextView
    private lateinit var dialogCloseBtn:AppCompatImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            R.style.FullScreenDialogStyle
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_html_preview, container, false)

        initViews(v)

        return v
    }

    private fun initViews(view: View) {
        textView = view.findViewById(R.id.full_desc_html_text_view)
        dialogCloseBtn = view.findViewById(R.id.full_desc_close_dialog)

        dialogCloseBtn.setOnClickListener {
            dismiss()
        }

        textView.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            HtmlCompat.fromHtml(
                HtmlCompat.fromHtml(
                    fullDesc,
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                ).toString(), HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        } else {
            Html.fromHtml(Html.fromHtml(fullDesc).toString())
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

}