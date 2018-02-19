package com.gigigo.orchextra.core.controller

import com.gigigo.orchextra.core.data.rxException.ApiDetailNotFoundException
import com.gigigo.orchextra.core.data.rxException.ApiMenuNotFoundException
import com.gigigo.orchextra.core.data.rxException.ApiSectionNotFoundException
import com.gigigo.orchextra.core.data.rxException.ApiVersionNotFoundException
import com.gigigo.orchextra.core.domain.OcmControllerKt
import com.gigigo.orchextra.core.domain.OcmControllerKt.GetDetailControllerCallback
import com.gigigo.orchextra.core.domain.OcmControllerKt.GetMenusControllerCallback
import com.gigigo.orchextra.core.domain.OcmControllerKt.GetSectionControllerCallback
import com.gigigo.orchextra.core.domain.entities.DataRequest.DEFAULT
import com.gigigo.orchextra.core.domain.entities.DataRequest.FORCE_CACHE
import com.gigigo.orchextra.core.domain.entities.DataRequest.FORCE_CLOUD
import com.gigigo.orchextra.core.domain.entities.contentdata.ContentData
import com.gigigo.orchextra.core.domain.entities.elements.ElementData
import com.gigigo.orchextra.core.domain.entities.menus.MenuContentData
import com.gigigo.orchextra.core.domain.entities.version.VersionData
import com.gigigo.orchextra.core.domain.rxInteractor.DefaultObserver
import com.gigigo.orchextra.core.domain.rxInteractor.GetDetail
import com.gigigo.orchextra.core.domain.rxInteractor.GetMenus
import com.gigigo.orchextra.core.domain.rxInteractor.GetSection
import com.gigigo.orchextra.core.domain.rxInteractor.GetVersion
import com.gigigo.orchextra.core.domain.rxInteractor.PriorityScheduler
import com.gigigo.orchextra.core.domain.rxInteractor.PriorityScheduler.Priority.HIGH
import com.gigigo.orchextra.core.sdk.utils.DateUtils
import com.gigigo.orchextra.core.sdk.utils.OcmPreferences
import com.gigigo.orchextra.ocm.OCManager
import com.gigigo.orchextra.ocm.dto.UiMenu
import com.gigigo.orchextra.ocm.dto.UiMenuData
import java.lang.Exception
import java.util.ArrayList

class OcmControllerImpKt(
    private val getVersion: GetVersion,
    private val getMenus: GetMenus,
    private val getSection: GetSection,
    private val getDetail: GetDetail,
    private val ocmPreferences: OcmPreferences) : OcmControllerKt {

  lateinit var menuCallback: GetMenusControllerCallback
  var imagesToDownload: Int = 21

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

          if (noCachedMenuData(cachedMenuData)) {
            loadSections(uiMenuData,
                onLoaded = {
                  menuCallback.onMenusLoaded(uiMenuData, true)
                },
                onFailed = {
                  menuCallback.onMenusFails(it)
                }
            )
          }
        } else {
          loadSections(cachedMenuData,
              onLoaded = {
                menuCallback.onMenusLoaded(cachedMenuData, false)
              },
              onFailed = {
                menuCallback.onMenusFails(it)
              }
          )
        }
      }

      override fun onMenusFails(exception: Exception) {
        //DISPLAY NEW CONTENT AVAILABLE BUTTON
        menuCallback.onMenusLoaded(cachedMenuData, true)
      }

    }), GetMenus.Params.forForceSource(FORCE_CLOUD), HIGH)
  }

  private fun noCachedMenuData(cachedMenuData: UiMenuData?): Boolean =
      cachedMenuData == null || cachedMenuData.uiMenuList == null || cachedMenuData.uiMenuList.isEmpty()

  private fun menusHasChanged(cachedMenuData: UiMenuData?, uiMenuData: UiMenuData?): Boolean {
    return uiMenuData?.uiMenuList?.equals(cachedMenuData?.uiMenuList) == false
  }


  private fun loadSections(uiMenuData: UiMenuData? = null, onLoaded: () -> Unit = {},
      onFailed: (Exception) -> Unit = {}) {

    val menuSize = uiMenuData?.uiMenuList?.size
    var reached = 0
    uiMenuData?.uiMenuList?.forEach { menu ->
      menu.elementCache.render.contentUrl?.let { content ->
        loadSection(content, imagesToDownload,
            onLoaded = {
              reached++
              if (reached == menuSize) {
                onLoaded()
                OCManager.notifyOnLoadDataContentSectionFinished(menu)
              }
            },
            onFailed = {
              checkSection(content, imagesToDownload, object : GetSectionControllerCallback {
                override fun onSectionLoaded(contentData: ContentData?, hasChanged: Boolean) {
                  OCManager.notifyOnLoadDataContentSectionFinished(menu)
                }

                override fun onSectionFails(e: Exception) {
                  OCManager.notifyOnLoadDataContentSectionFinished(menu)
                }

              })
            }
        )
      }
    }
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
      contentData: ContentData?): Boolean = ((cachedSectionData?.version != contentData?.version) || DateUtils.isAfterCurrentDate(
      contentData?.expiredAt))


  private fun getSlug(elementUrl: String): String? {
    try {
      return elementUrl.substring(elementUrl.lastIndexOf("/") + 1, elementUrl.length)
    } catch (ignored: Exception) {
      return null
    }
  }

  override fun getDetail(elementUrl: String,
      getDetailControllerCallback: GetDetailControllerCallback) {
    val slug = getSlug(elementUrl)
    getDetail.execute(DetailObserver(object : GetDetailObserverCallback {
      override fun onDetailLoaded(elementData: ElementData) {
        getDetailControllerCallback.onDetailLoaded(elementData.element)
      }

      override fun onDetailFails(exception: Exception) {
        getDetailControllerCallback.onDetailFails(exception)
      }
    }),
        GetDetail.Params.forDetail(DEFAULT, slug), PriorityScheduler.Priority.HIGH)
  }

  companion object {
    fun transformMenu(menuContentData: MenuContentData?): UiMenuData {

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

      return uiMenuData
    }
  }
}


class VersionObserver(
    private val getVersionControllerCallback: GetVersionObserverCallback?) : DefaultObserver<VersionData>() {

  override fun onNext(versionData: VersionData) {
    getVersionControllerCallback?.onVersionLoaded(versionData)
  }

  override fun onError(exception: Throwable) {
    getVersionControllerCallback?.onVersionFails(ApiVersionNotFoundException(exception))
    exception.printStackTrace()
  }
}

class MenuObserver(
    private val getMenusCallback: GetMenusObserverCallback?) : DefaultObserver<MenuContentData>() {

  override fun onComplete() {

  }

  override fun onNext(menuContentData: MenuContentData) {
    getMenusCallback?.onMenusLoaded(OcmControllerImpKt.transformMenu(menuContentData))
  }

  override fun onError(exception: Throwable) {
    getMenusCallback?.onMenusFails(ApiMenuNotFoundException(exception))
    exception.printStackTrace()
  }
}

class SectionObserver(
    private val getSectionObserverCallback: GetSectionObserverCallback?) : DefaultObserver<ContentData>() {

  override fun onComplete() {

  }

  override fun onNext(contentData: ContentData) {
    getSectionObserverCallback?.onSectionLoaded(contentData)
  }

  override fun onError(exception: Throwable) {
    getSectionObserverCallback?.onSectionFails(ApiSectionNotFoundException(exception))
    exception.printStackTrace()
  }
}

class DetailObserver(
    private val getDetailObserverCallback: GetDetailObserverCallback?) : DefaultObserver<ElementData>() {

  override fun onNext(elementData: ElementData) {
    getDetailObserverCallback?.onDetailLoaded(elementData)
  }

  override fun onComplete() {}

  override fun onError(exception: Throwable) {
    getDetailObserverCallback?.onDetailFails(ApiDetailNotFoundException(exception))
    exception.printStackTrace()
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

interface GetDetailObserverCallback {
  fun onDetailLoaded(elementData: ElementData)
  fun onDetailFails(e: Exception)
}
