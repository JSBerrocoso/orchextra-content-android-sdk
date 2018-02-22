package com.gigigo.orchextra.core.utils

import com.gigigo.orchextra.core.sdk.utils.getUrlParameters
import org.junit.Test

class UrlUtilsTest {

  @Test
  fun shouldGetUrlParams() {
    val url = "http://example.url.com/action?code=#the-code#&language=#device_language#"

    val params = url.getUrlParameters()

    assert(params.contains("the-code"))
    assert(params.contains("device_language"))
  }

  @Test
  fun shouldGetEmptyListOfParams() {
    val url = "http://example.url.com/"

    val params = url.getUrlParameters()

    assert(params.isEmpty())
  }
}