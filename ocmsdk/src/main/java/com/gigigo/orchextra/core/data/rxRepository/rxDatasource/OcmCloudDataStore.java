package com.gigigo.orchextra.core.data.rxRepository.rxDatasource;

import android.support.annotation.NonNull;
import com.gigigo.orchextra.core.data.api.dto.content.ApiSectionContentDataResponse;
import com.gigigo.orchextra.core.data.api.dto.elementcache.ApiElementDataResponse;
import com.gigigo.orchextra.core.data.api.dto.menus.ApiMenuContentDataResponse;
import com.gigigo.orchextra.core.data.api.services.OcmApiService;
import com.gigigo.orchextra.core.data.rxCache.OcmCache;
import io.reactivex.Observable;
import javax.inject.Inject;
import orchextra.javax.inject.Singleton;

/**
 * Created by francisco.hernandez on 23/5/17.
 */

@Singleton public class OcmCloudDataStore implements OcmDataStore {

  private final OcmApiService ocmApiService;
  private final OcmCache ocmCache;

  @Inject
  public OcmCloudDataStore(@NonNull OcmApiService ocmApiService, @NonNull OcmCache ocmCache) {
    this.ocmApiService = ocmApiService;
    this.ocmCache = ocmCache;
  }

  @Override public Observable<ApiMenuContentDataResponse> getMenuEntity() {
    return ocmApiService.getMenuDataRx();//.doOnNext();   //TODO: RX
  }

  @Override public Observable<ApiSectionContentDataResponse> getSectionEntity(String elementUrl) {
    return ocmApiService.getSectionDataRx(elementUrl);
  }

  @Override public Observable<ApiSectionContentDataResponse> searchByText(String section) {
    return ocmApiService.searchRx(section);
  }

  @Override public Observable<ApiElementDataResponse> getElementById(String section) {
    return ocmApiService.getElementByIdRx(section);
  }
}