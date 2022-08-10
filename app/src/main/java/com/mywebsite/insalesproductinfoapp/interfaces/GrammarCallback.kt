package com.mywebsite.insalesproductinfoapp.interfaces

import android.text.SpannableStringBuilder

interface GrammarCallback {
    fun onSuccess(response:SpannableStringBuilder?,errors:Boolean)
}