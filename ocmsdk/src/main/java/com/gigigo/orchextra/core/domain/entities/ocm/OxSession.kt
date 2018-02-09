package com.gigigo.orchextra.core.domain.entities.ocm

class OxSession {
  var key: String? = null
    private set

  var secret: String? = null
    private set

  var token: String? = null

  fun getAccessToken(): String = "JWT " + token

  fun setCredentials(key: String, secret: String) {
    this.key = key
    this.secret = secret
  }
}