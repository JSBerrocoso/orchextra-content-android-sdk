package com.gigigo.orchextra.core.controller

import android.util.Log
import com.gigigo.orchextra.core.data.rxException.ApiMenuNotFoundException
import com.gigigo.orchextra.core.data.rxException.ApiSectionNotFoundException
import com.gigigo.orchextra.core.data.rxException.ApiVersionNotFoundException
import com.gigigo.orchextra.core.domain.OcmControllerKt
import com.gigigo.orchextra.core.domain.OcmControllerKt.GetMenusControllerCallback
import com.gigigo.orchextra.core.domain.OcmControllerKt.GetSectionControllerCallback
import com.gigigo.orchextra.core.domain.entities.DataRequest.FORCE_CACHE
import com.gigigo.orchextra.core.domain.entities.DataRequest.FORCE_CLOUD
import com.gigigo.orchextra.core.domain.entities.contentdata.ContentData
import com.gigigo.orchextra.core.domain.entities.elements.Element
import com.gigigo.orchextra.core.domain.entities.menus.MenuContentData
import com.gigigo.orchextra.core.domain.entities.version.VersionData
import com.gigigo.orchextra.core.domain.rxInteractor.DefaultObserver
import com.gigigo.orchextra.core.domain.rxInteractor.GetMenus
import com.gigigo.orchextra.core.domain.rxInteractor.GetSection
import com.gigigo.orchextra.core.domain.rxInteractor.GetVersion
import com.gigigo.orchextra.core.domain.rxInteractor.PriorityScheduler.Priority.HIGH
import com.gigigo.orchextra.core.sdk.utils.OcmPreferences
import com.gigigo.orchextra.ocm.dto.UiMenu
import com.gigigo.orchextra.ocm.dto.UiMenuData
import java.lang.Exception
import java.util.ArrayList

class OcmControllerImpKt(
    private val getVersion: GetVersion,
    private val getMenus: GetMenus,
    private val getSection: GetSection,
    private val ocmPreferences: OcmPreferences) : OcmControllerKt {

  lateinit var menuCallback: GetMenusControllerCallback

  override fun updateContent(callback: GetMenusControllerCallback) {
    menuCallback = callback

    getVersion.execute(VersionObserver(object : GetVersionObserverCallback {
      override fun onVersionLoaded(versionData: VersionData) {
        checkVersion(versionData)
      }

      override fun onVersionFails(exception: Exception) {
        loadMenuSections(
            onLoaded = {
              checkMenus(it)
            },
            onFailed = {
              checkMenus()
            }
        )
      }

    }), GetVersion.Params.forVersion(), HIGH)
  }

  private fun checkVersion(versionData: VersionData) {
    if (versionHasChanged(versionData)) {
      ocmPreferences.saveVersion(versionData.version)
      loadMenuSections(
          onLoaded = {
            checkMenus(it)
          },
          onFailed = {
            checkMenus()
          }
      )
    } else {
      //DO NOTHING: retrieve cached
      loadMenuSections(
          onLoaded = {
            menuCallback.onMenusLoaded(it, false)
          },
          onFailed = {
            menuCallback.onMenusFails(it)
          }
      )
    }
  }

  private fun versionHasChanged(versionData: VersionData): Boolean =
      ocmPreferences.version != versionData.version

  private fun loadMenuSections(onLoaded: (UiMenuData?) -> Unit = {},
      onFailed: (Exception) -> Unit = {}) {
    getMenus.execute(MenuObserver(object : GetMenusObserverCallback {
      override fun onMenusLoaded(uiMenuData: UiMenuData?) {
        onLoaded(uiMenuData)
      }

      override fun onMenusFails(exception: Exception) {
        onFailed(exception)
      }

    }), GetMenus.Params.forForceSource(FORCE_CACHE), HIGH)
  }

  private fun checkMenus(cachedMenuData: UiMenuData? = null) {
    getMenus.execute(MenuObserver(object : GetMenusObserverCallback {
      override fun onMenusLoaded(uiMenuData: UiMenuData?) {
        if (menusHasChanged(cachedMenuData, uiMenuData)) {
          //DISPLAY NEW CONTENT AVAILABLE BUTTON
          menuCallback.onMenusLoaded(uiMenuData, true)
        } else {
          //DO NOTHING
          loadMenuSections(
              onLoaded = {
                menuCallback.onMenusLoaded(it, false)
              },
              onFailed = {
                menuCallback.onMenusFails(it)
              }
          )
          //TODO: loadSections
        }
      }

      override fun onMenusFails(exception: Exception) {
        //DISPLAY NEW CONTENT AVAILABLE BUTTON
        menuCallback.onMenusLoaded(cachedMenuData, true)
      }

    }), GetMenus.Params.forForceSource(FORCE_CLOUD), HIGH)
  }

  private fun menusHasChanged(cachedMenuData: UiMenuData?, uiMenuData: UiMenuData?): Boolean {
    return uiMenuData?.uiMenuList?.equals(cachedMenuData?.uiMenuList) == false
  }


  private fun loadSections(uiMenuData: UiMenuData? = null, onLoaded: (ContentData?) -> Unit = {},
      onFailed: (Exception) -> Unit = {}) {
    menuCallback.onMenusLoaded(uiMenuData, false)

    /*

    uiMenuData?.uiMenuList?.forEach { menu ->
      val menuSize = uiMenuData?.uiMenuList?.size
      var reached = 0
      menu.elementCache.render.contentUrl?.let { content ->
        loadSection(content,
            onLoaded = {
              reached ++
              if(reached == menuSize) {
                //TODO: all sections loaded
              }
            },
            onFailed = {

            }
        )
      }
    }
    */
  }


  override fun openSection(contentUrl: String, imagesToDownload: Int,
      getSectionControllerCallback: GetSectionControllerCallback) {
    loadSection(contentUrl, imagesToDownload,
        onLoaded = {
          getSectionControllerCallback.onSectionLoaded(it, false)
        },
        onFailed = {
          checkSection(contentUrl, imagesToDownload, getSectionControllerCallback)
        }
    )
  }

  private fun loadSection(contentUrl: String, imagesToDownload: Int,
      onLoaded: (ContentData?) -> Unit = {}, onFailed: (Exception) -> Unit = {}) {
    getSection.execute(SectionObserver(object : GetSectionObserverCallback {
      override fun onSectionLoaded(contentData: ContentData?) {
        onLoaded(contentData)
      }

      override fun onSectionFails(exception: Exception) {
        onFailed(exception)
      }

    }), GetSection.Params.forSection(FORCE_CACHE, contentUrl, imagesToDownload), HIGH)
  }

  private fun checkSection(contentUrl: String, imagesToDownload: Int,
      getSectionControllerCallback: GetSectionControllerCallback,
      cachedSectionData: ContentData? = null) {
    getSection.execute(SectionObserver(object : GetSectionObserverCallback {
      override fun onSectionLoaded(contentData: ContentData?) {
        if (sectionHasChanged(cachedSectionData, contentData)) {
          getSectionControllerCallback.onSectionLoaded(contentData, true)
        } else {
          getSectionControllerCallback.onSectionLoaded(cachedSectionData, false)
        }
      }

      override fun onSectionFails(exception: Exception) {
        getSectionControllerCallback.onSectionFails(exception)
      }

    }), GetSection.Params.forSection(FORCE_CLOUD, contentUrl, imagesToDownload), HIGH)
  }

  private fun sectionHasChanged(cachedSectionData: ContentData?,
      contentData: ContentData?): Boolean {
    val cachedElements = cachedSectionData?.content?.elements ?: emptyList()
    val newElements = contentData?.content?.elements ?: emptyList()

    when {
      cachedElements.size != newElements.size -> return true
      else -> for ((index, cachedElement) in cachedElements.withIndex()) {
        var newElement = newElements[index]
        if (!cachedElement.slug.equals(newElement.slug, true)) {
          return true
        } else {
          val cachedElementCache = cachedSectionData?.elementsCache?.get(cachedElement.elementUrl)
          val newElementCache = contentData?.elementsCache?.get(newElement.elementUrl)

          if (cachedElementCache?.updateAt != newElementCache?.updateAt) {
            return true
          }
        }
      }
    }

    return false
  }

  companion object {
    fun transformMenu(menuContentData: MenuContentData?): UiMenuData {

      val time = System.currentTimeMillis()

      val uiMenuData = UiMenuData()

      val menuList = ArrayList<UiMenu>()

      if (menuContentData != null
          && menuContentData.menuContentList != null
          && menuContentData.menuContentList.size > 0) {

        uiMenuData.isFromCloud = menuContentData.isFromCloud

        for (element in menuContentData.menuContentList[0].elements) {
          val uiMenu = UiMenu()

          uiMenu.slug = element.slug
          uiMenu.text = element.sectionView!!.text
          uiMenu.elementUrl = element.elementUrl

          if (menuContentData.elementsCache != null) {
            val elementCache = menuContentData.elementsCache[element.elementUrl]
            if (elementCache != null) {
              uiMenu.elementCache = elementCache
              uiMenu.updateAt = elementCache.updateAt
              if (elementCache.render != null) {
                uiMenu.contentUrl = elementCache.render.contentUrl
              }
            }
          }

          menuList.add(uiMenu)
        }
      }

      uiMenuData.uiMenuList = menuList

      Log.v("TT - UiMenuData", ((System.currentTimeMillis() - time) / 1000).toString() + "")

      return uiMenuData
    }
  }
}


class VersionObserver(
    private val getVersionControllerCallback: GetVersionObserverCallback?) : DefaultObserver<VersionData>() {

  override fun onNext(versionData: VersionData) {
    getVersionControllerCallback?.onVersionLoaded(versionData)
  }

  override fun onError(e: Throwable) {
    getVersionControllerCallback?.onVersionFails(ApiVersionNotFoundException(e))
    e.printStackTrace()
  }
}

class MenuObserver(
    private val getMenusCallback: GetMenusObserverCallback?) : DefaultObserver<MenuContentData>() {

  override fun onComplete() {

  }

  override fun onNext(menuContentData: MenuContentData) {
    getMenusCallback?.onMenusLoaded(OcmControllerImpKt.transformMenu(menuContentData))
  }

  override fun onError(e: Throwable) {
    getMenusCallback?.onMenusFails(ApiMenuNotFoundException(e))
    e.printStackTrace()
  }
}

class SectionObserver(
    private val getSectionObserverCallback: GetSectionObserverCallback?) : DefaultObserver<ContentData>() {

  override fun onComplete() {

  }

  override fun onNext(contentData: ContentData) {
    getSectionObserverCallback?.onSectionLoaded(contentData)
  }

  override fun onError(e: Throwable) {
    getSectionObserverCallback?.onSectionFails(ApiSectionNotFoundException(e))
    e.printStackTrace()
  }
}


interface GetVersionObserverCallback {
  fun onVersionLoaded(versionData: VersionData)
  fun onVersionFails(exception: Exception)
}

interface GetMenusObserverCallback {
  fun onMenusLoaded(uiMenuData: UiMenuData?)
  fun onMenusFails(exception: Exception)
}

interface GetSectionObserverCallback {
  fun onSectionLoaded(contentData: ContentData?)
  fun onSectionFails(exception: Exception)
}
