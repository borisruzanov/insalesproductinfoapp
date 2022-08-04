package com.boris.expert.csvmagic.model

import com.google.gson.annotations.SerializedName
import com.mywebsite.insalesproductinfoapp.model.Feedback

data class FeedbackResponse(
    @SerializedName("feedbacks")
    val feedbacks: ArrayList<Feedback>
)