package com.gigigo.orchextra.core.data.rxRepository;

import com.gigigo.ggglib.network.mappers.ApiGenericResponseMapper;
import com.gigigo.orchextra.core.data.api.dto.menus.ApiMenuContentData;
import com.gigigo.orchextra.core.data.api.mappers.contentdata.ApiContentDataResponseMapper;
import com.gigigo.orchextra.core.data.api.mappers.menus.ApiMenuContentListResponseMapper;
import com.gigigo.orchextra.core.data.rxRepository.rxDatasource.OcmDataStore;
import com.gigigo.orchextra.core.data.rxRepository.rxDatasource.OcmDataStoreFactory;
import com.gigigo.orchextra.core.domain.entities.contentdata.ContentData;
import com.gigigo.orchextra.core.domain.entities.contentdata.ContentItem;
import com.gigigo.orchextra.core.domain.entities.elementcache.ElementCache;
import com.gigigo.orchextra.core.domain.entities.menus.MenuContentData;
import com.gigigo.orchextra.core.domain.rxRepository.OcmRepository;
import io.reactivex.Observable;
import orchextra.javax.inject.Inject;
import orchextra.javax.inject.Singleton;

/**
 * Created by francisco.hernandez on 23/5/17.
 */

@Singleton public class OcmDataRepository implements OcmRepository {
  private final OcmDataStoreFactory ocmDataStoreFactory;
  private final ApiMenuContentListResponseMapper apiMenuContentListResponseMapper;
  private final ApiContentDataResponseMapper apiContentDataResponseMapper;

  @Inject public OcmDataRepository(OcmDataStoreFactory ocmDataStoreFactory,
      ApiMenuContentListResponseMapper apiMenuContentListResponseMapper,
      ApiContentDataResponseMapper apiContentDataResponseMapper) {
    this.ocmDataStoreFactory = ocmDataStoreFactory;
    this.apiMenuContentListResponseMapper = apiMenuContentListResponseMapper;
    this.apiContentDataResponseMapper = apiContentDataResponseMapper;
  }

  @Override public Observable<MenuContentData> getMenu(boolean forceReload) {
    OcmDataStore ocmDataStore = ocmDataStoreFactory.getDataStoreForMenus(forceReload);
    return ocmDataStore.getMenuEntity()
        .map(apiMenuContentData -> apiMenuContentListResponseMapper.externalClassToModel(
            apiMenuContentData));
  }

  @Override
  public Observable<ContentData> getSectionElements(boolean forceReload, String elementUrl) {
    OcmDataStore ocmDataStore = ocmDataStoreFactory.getDataStoreForSections(forceReload, elementUrl);
    return ocmDataStore.getSectionEntity(elementUrl)
        .map(apiSectionContentData -> apiContentDataResponseMapper.externalClassToModel(
            apiSectionContentData));
  }

  @Override public Observable<ElementCache> getDetail(String section) {
    return null;
  }

  @Override public Observable<ContentItem> doSearch(String textToSearch) {
    return null;
  }
}
