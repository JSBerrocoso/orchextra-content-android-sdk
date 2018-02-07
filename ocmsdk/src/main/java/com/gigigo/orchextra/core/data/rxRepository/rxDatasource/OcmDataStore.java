package com.gigigo.orchextra.core.data.rxRepository.rxDatasource;

import com.gigigo.orchextra.core.data.api.dto.content.ApiSectionContentData;
import com.gigigo.orchextra.core.data.api.dto.elements.ApiElementData;
import com.gigigo.orchextra.core.data.api.dto.menus.ApiMenuContentData;
import com.gigigo.orchextra.core.data.api.dto.versioning.ApiVersionKache;
import com.gigigo.orchextra.core.domain.entities.DataRequest;
import io.reactivex.Observable;

/**
 * Interface that represents a data store from where data is retrieved.
 */
public interface OcmDataStore {

  /**
   * Get an {@link Observable} which will emit a {@link ApiMenuContentData}.
   */
  Observable<ApiMenuContentData> getMenuEntity();

  /**
   * Get an {@link Observable} which will emit a {@link ApiSectionContentData}.
   */
  Observable<ApiSectionContentData> getSectionEntity(String elementUrl, int numberOfElementsToDownload);

  /**
   * Get an {@link Observable} which will emit a {@link ApiSectionContentData}.
   */
  Observable<ApiSectionContentData> searchByText(String section);

  /**
   * Get an {@link Observable} which will emit a {@link ApiElementData}.
   */
  Observable<ApiElementData> getElementById(String slug);

  Observable<ApiVersionKache> getVersion();

  boolean isFromCloud();
}
