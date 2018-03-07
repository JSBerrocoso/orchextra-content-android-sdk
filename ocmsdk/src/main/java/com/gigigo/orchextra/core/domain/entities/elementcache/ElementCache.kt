package com.gigigo.orchextra.core.domain.entities.elementcache

import java.io.Serializable

class ElementCache : Serializable {

  var slug: String? = null
  var type: ElementCacheType? = null
  var tags: List<String>? = null
  var preview: ElementCachePreview? = null
  var render: ElementCacheRender? = null
  var share: ElementCacheShare? = null
  var customProperties: Map<String, Any>? = null
  var name: String? = null
  var updateAt: Long = 0
}