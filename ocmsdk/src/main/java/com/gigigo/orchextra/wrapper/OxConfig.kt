package com.gigigo.orchextra.wrapper

import android.app.Activity

data class OxConfig(
    val apiKey: String,
    val apiSecret: String,
    val firebaseApiKey: String = "",
    val firebaseApplicationId: String = "",
    val notificationActivityClass: Class<Activity>? = null)