package com.gigigo.orchextra.core.data.rxRepository.rxDatasource;

import android.content.Context;
import android.util.Log;
import com.gigigo.orchextra.core.data.api.mappers.contentdata.ApiContentDataResponseMapper;
import com.gigigo.orchextra.core.data.api.mappers.elements.ApiElementDataMapper;
import com.gigigo.orchextra.core.data.api.mappers.menus.ApiMenuContentListResponseMapper;
import com.gigigo.orchextra.core.data.rxCache.OcmCache;
import com.gigigo.orchextra.core.data.rxRepository.DbMappersKt;
import com.gigigo.orchextra.core.domain.entities.contentdata.ContentData;
import com.gigigo.orchextra.core.domain.entities.elements.ElementData;
import com.gigigo.orchextra.core.domain.entities.menus.MenuContentData;
import com.gigigo.orchextra.core.domain.entities.version.VersionData;
import gigigo.com.vimeolibs.VimeoInfo;
import io.reactivex.Observable;
import orchextra.javax.inject.Inject;
import orchextra.javax.inject.Singleton;

@Singleton public class OcmDiskDataStore implements OcmDataStore {
  private final OcmCache ocmCache;

  private final ApiMenuContentListResponseMapper apiMenuContentListResponseMapper;
  private final ApiContentDataResponseMapper apiContentDataResponseMapper;
  private final ApiElementDataMapper apiElementDataMapper;

  @Inject public OcmDiskDataStore(OcmCache ocmCache,
      ApiMenuContentListResponseMapper apiMenuContentListResponseMapper,
      ApiContentDataResponseMapper apiContentDataResponseMapper,
      ApiElementDataMapper apiElementDataMapper) {
    this.ocmCache = ocmCache;

    this.apiMenuContentListResponseMapper = apiMenuContentListResponseMapper;
    this.apiContentDataResponseMapper = apiContentDataResponseMapper;
    this.apiElementDataMapper = apiElementDataMapper;
  }

  @Override public Observable<VersionData> getVersion() {
    return ocmCache.getVersion().map(dbVersionData -> DbMappersKt.toVersionData(dbVersionData));
  }

  @Override public Observable<MenuContentData> getMenuEntity() {
    final long time = System.currentTimeMillis();

    return ocmCache.getMenus().doOnNext(apiMenuContentData -> {
      apiMenuContentData.setFromCloud(false);
      Log.v("TT - DISK - Menus", (System.currentTimeMillis() - time) / 1000 + "");
    }).map(apiMenuContentListResponseMapper::externalClassToModel);
  }


  @Override public Observable<ContentData> getSectionEntity(String elementUrl,
      int numberOfElementsToDownload) {
    final long time = System.currentTimeMillis();

    return ocmCache.getSection(elementUrl).doOnNext(apiSectionContentData -> {
      apiSectionContentData.setFromCloud(false);

      Log.v("TT - DISK - Sections", (System.currentTimeMillis() - time) / 1000 + "");
    }).map(apiContentDataResponseMapper::externalClassToModel);
  }

  @Override public Observable<ContentData> searchByText(String section) {
    return null;
  }

  @Override public Observable<ElementData> getElementById(String slug) {
    final long time = System.currentTimeMillis();

    return ocmCache.getDetail(slug)
        .doOnNext(apiElementData -> Log.v("TT - DISK - Details",
            (System.currentTimeMillis() - time) / 1000 + ""))
        .map(apiElementDataMapper::externalClassToModel);
  }

  @Override public Observable<VimeoInfo> getVideoById(Context context, String videoId,
      boolean isWifiConnection, boolean isFastConnection) {
    return ocmCache.getVideo(videoId).map(videoData -> DbMappersKt.toVimeoInfo(videoData));
  }

  @Override public boolean isFromCloud() {
    return false;
  }

  public OcmCache getOcmCache() {
    return ocmCache;
  }
}