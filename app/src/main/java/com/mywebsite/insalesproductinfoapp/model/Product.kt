package com.mywebsite.insalesproductinfoapp.model

import com.boris.expert.csvmagic.model.ProductImages
import java.io.Serializable

data class Product (
    val id:Int,
    val categoryId:Int,
    var title:String,
    var shortDesc:String,
    var fullDesc:String,
    var sku:String,
    var productImages:ArrayList<ProductImages>?
        ):Serializable{
            constructor():this(0,0,"","","","",null)
        }