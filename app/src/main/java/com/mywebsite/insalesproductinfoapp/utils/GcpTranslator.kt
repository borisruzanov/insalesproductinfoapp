package com.mywebsite.insalesproductinfoapp.utils

import android.content.Context
import android.text.TextUtils
import com.google.cloud.translate.Detection
import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions
import com.mywebsite.insalesproductinfoapp.R
import com.mywebsite.insalesproductinfoapp.interfaces.TranslationCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


object GcpTranslator {

    fun translateFromEngToRus(context: Context, text: String, listener: TranslationCallback){
//        listener.onTextTranslation(text)
//        return
        val appSettings = AppSettings(context)
        val apiKey = appSettings.getString("G_API_KEY")
        System.setProperty("GOOGLE_API_KEY",apiKey)
        CoroutineScope(Dispatchers.IO).launch {

            val translate = TranslateOptions.getDefaultInstance().service
            val detection: Detection = translate.detect(text)
            val detectedLanguage = detection.language
            try {
                val translation = translate.translate(
                    text,
                    Translate.TranslateOption.sourceLanguage(detectedLanguage),
                    Translate.TranslateOption.targetLanguage("ru"))
                CoroutineScope(Dispatchers.Main).launch {
                    listener.onTextTranslation(translation.translatedText)
                }
            }
            catch (e:Exception){
                e.printStackTrace()
            }
//            if (detectedLanguage == "en"){
//                CoroutineScope(Dispatchers.Main).launch {
//                    listener.onTextTranslation(text)
//                }
//
//            }
//            else{
//                val translation = translate.translate(
//                    text,
//                    Translate.TranslateOption.sourceLanguage(detectedLanguage),
//                    Translate.TranslateOption.targetLanguage("ru"))
//                CoroutineScope(Dispatchers.Main).launch {
//                    listener.onTextTranslation(translation.translatedText)
//                }
//
//            }
        }
    }

    fun translateFromRusToEng(context: Context, text: String, listener: TranslationCallback){
//        listener.onTextTranslation(text)
//        return
        val appSettings = AppSettings(context)
        val apiKey = appSettings.getString("G_API_KEY")
        System.setProperty("GOOGLE_API_KEY",apiKey)
        val translate = TranslateOptions.getDefaultInstance().service
        val detection: Detection = translate.detect(text)
        val detectedLanguage = detection.language
        if (detectedLanguage == "en"){
            listener.onTextTranslation(text)
        }
        else{
            if (TextUtils.isDigitsOnly(text)){
                listener.onTextTranslation(text)
            }
            else{
                val translation = translate.translate(
                    text,
                    Translate.TranslateOption.sourceLanguage(detectedLanguage),
                    Translate.TranslateOption.targetLanguage("en"))
                listener.onTextTranslation(translation.translatedText)
            }

        }

    }

}