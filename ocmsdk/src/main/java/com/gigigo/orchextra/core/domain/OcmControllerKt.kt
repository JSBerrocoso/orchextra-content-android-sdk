package com.gigigo.orchextra.core.domain

import com.gigigo.orchextra.core.domain.entities.contentdata.ContentData
import com.gigigo.orchextra.core.domain.entities.elementcache.ElementCache
import com.gigigo.orchextra.ocm.dto.UiMenuData
import java.lang.Exception

interface OcmControllerKt {

  fun updateContent(callback: GetMenusControllerCallback)

  fun openSection(contentUrl: String,
      imagesToDownload: Int,
      callback: GetSectionControllerCallback)

  fun getDetail(elementUrl: String,
      getDetailControllerCallback: GetDetailControllerCallback)

  interface GetMenusControllerCallback {
    fun onMenusLoaded(uiMenuData: UiMenuData?, hasChanged: Boolean)
    fun onMenusFails(exception: Exception)
  }

  interface GetSectionControllerCallback {
    fun onSectionLoaded(contentData: ContentData?, hasChanged: Boolean)
    fun onSectionFails(e: Exception)
  }

  interface GetDetailControllerCallback {
    fun onDetailLoaded(elementCache: ElementCache)
    fun onDetailFails(e: Exception)
  }
}