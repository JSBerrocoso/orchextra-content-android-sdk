package com.gigigo.orchextra.core.domain

import com.gigigo.orchextra.core.domain.entities.contentdata.ContentData
import com.gigigo.orchextra.core.domain.entities.version.VersionData
import com.gigigo.orchextra.ocm.dto.UiMenuData
import java.lang.Exception

interface OcmControllerKt {

  fun updateContent(callback: GetMenusControllerCallback)
  /*
  fun openSection(section: String,
      imagesToDownload: Int,
      callback: GetSectionControllerCallback
  )
  */

  interface GetVersionControllerCallback {
    fun onVersionLoaded(versionData: VersionData)
    fun onVersionFails(exception: Exception)
  }

  interface GetMenusControllerCallback {
    fun onMenusLoaded(uiMenuData: UiMenuData?, hasChanged: Boolean)
    fun onMenusFails(exception: Exception)
  }

  interface GetSectionControllerCallback {
    fun onSectionLoaded(contentData: ContentData)
    fun onSectionFails(e: Exception)
  }
}