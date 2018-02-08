package com.gigigo.orchextra.wrapper

import android.app.Application

interface OxManager {

  fun startImageRecognition()

  fun startScanner()

  fun init(application: Application, config: OxConfig, statusListener: StatusListener)

  fun finish()

  fun removeListeners()

  fun isReady(): Boolean

  fun getToken(tokenReceiver: TokenReceiver)

  fun setErrorListener(errorListener: ErrorListener)

  fun setBusinessUnits(businessUnits: List<String>)

  fun setCustomSchemeReceiver(customSchemeReceiver: CustomActionListener)

  interface TokenReceiver {
    fun onGetToken(token: String)
  }

  interface CustomActionListener {
    fun onCustomSchema(customSchema: String)
  }

  interface StatusListener {
    fun isReady()

    fun onError(error: String)
  }

  interface ErrorListener {
    fun onError(error: String)
  }
}