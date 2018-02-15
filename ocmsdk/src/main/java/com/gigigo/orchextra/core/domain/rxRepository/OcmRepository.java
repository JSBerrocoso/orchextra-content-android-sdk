package com.gigigo.orchextra.core.domain.rxRepository;

import com.gigigo.orchextra.core.domain.entities.DataRequest;
import com.gigigo.orchextra.core.domain.entities.contentdata.ContentData;
import com.gigigo.orchextra.core.domain.entities.elements.ElementData;
import com.gigigo.orchextra.core.domain.entities.menus.MenuContentData;
import com.gigigo.orchextra.core.domain.entities.version.VersionData;
import io.reactivex.Observable;

/**
 * Created by francisco.hernandez on 22/5/17.
 */

public interface OcmRepository {

  Observable<VersionData> getVersion();

  Observable<MenuContentData> getMenu(DataRequest forceSource);

  Observable<ContentData> getSectionElements(DataRequest forceSource, String elementUrl,
      int numberOfElementsToDownload);

  Observable<ElementData> getDetail(DataRequest forceSource, String section);

  Observable<ContentData> doSearch(String textToSearch);

  Observable<Void> clear(boolean images, boolean data);
}
