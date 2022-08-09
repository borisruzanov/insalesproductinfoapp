package com.mywebsite.insalesproductinfoapp.model

import java.io.Serializable

data class RainForestApiObject(
    val asin:String,
    val image: String,
    var title: String,
) : Serializable {
    constructor() : this("","", "")
}