package com.gigigo.orchextra.core.data.rxRepository.rxDatasource;

import android.util.Log;
import com.gigigo.orchextra.core.data.rxCache.OcmCache;
import com.gigigo.orchextra.core.domain.entities.DataRequest;
import com.gigigo.orchextra.core.domain.entities.ocm.OxSession;
import com.gigigo.orchextra.core.domain.utils.ConnectionUtils;
import orchextra.javax.inject.Inject;
import orchextra.javax.inject.Singleton;

/**
 * Created by francisco.hernandez on 23/5/17.
 */

@Singleton public class OcmDataStoreFactory {

  private static final String TAG = OcmDataStoreFactory.class.getSimpleName();

  private final OcmCloudDataStore cloudDataStore;
  private final OcmDiskDataStore diskDataStore;
  private final ConnectionUtils connectionUtils;
  private final OxSession session;

  @Inject
  public OcmDataStoreFactory(OcmCloudDataStore cloudDataStore, OcmDiskDataStore diskDataStore,
      ConnectionUtils connectionUtils, OxSession session) {
    this.cloudDataStore = cloudDataStore;
    this.diskDataStore = diskDataStore;
    this.connectionUtils = connectionUtils;
    this.session = session;
  }

  public OcmDataStore getDataStoreForVersion() {
    if (session.getToken() == null) {
      return getDiskDataStore();
    }

    OcmCache ocmCache = diskDataStore.getOcmCache();

    if (ocmCache.isVersionCached() && !ocmCache.isVersionExpired()) {
      Log.i(TAG, "DISK  - Version");
      return getDiskDataStore();
    } else {
      Log.i(TAG, "CLOUD - Version");
      return getCloudDataStore();
    }
  }

  public OcmDataStore getDataStoreForMenus(DataRequest forceSource) {
    OcmDataStore ocmDataStore;

    if (!connectionUtils.hasConnection()) return getDiskDataStore();

    switch (forceSource) {
      case FORCE_CACHE:
        Log.i(TAG, "FORCE DISK - Menus");
        ocmDataStore = getDiskDataStore();
        break;
      case FORCE_CLOUD:
        Log.i(TAG, "FORCE CLOUD - Menus");
        ocmDataStore = getCloudDataStore();
        break;
      default:
      case DEFAULT:
        OcmCache cache = diskDataStore.getOcmCache();
        if (cache.isMenuCached() && !cache.isMenuExpired()) {
          Log.i(TAG, "DISK  - Menus");
          ocmDataStore = getDiskDataStore();
        } else {
          Log.i(TAG, "CLOUD - Menus");
          ocmDataStore = getCloudDataStore();
        }
        break;
    }

    return ocmDataStore;
  }

  public OcmDataStore getDataStoreForSections(DataRequest forceSource, String section) {
    OcmDataStore ocmDataStore;

    if (!connectionUtils.hasConnection()) return getDiskDataStore();

    switch (forceSource) {
      case FORCE_CACHE:
        Log.i(TAG, "DISK  - Sections");
        ocmDataStore = getDiskDataStore();
        break;
      case FORCE_CLOUD:
        Log.i(TAG, "CLOUD - Sections");
        ocmDataStore = getCloudDataStore();
        break;
      default:
      case DEFAULT:
        OcmCache cache = diskDataStore.getOcmCache();
        if (cache.isSectionCached(section) && !cache.isSectionExpired(section)) {
          Log.i(TAG, "DISK  - Sections");
          ocmDataStore = getDiskDataStore();
        } else {
          Log.i(TAG, "CLOUD - Sections");
          ocmDataStore = getCloudDataStore();
        }
        break;
    }

    return ocmDataStore;
  }

  public OcmDataStore getDataStoreForDetail(boolean force, String slug) {
    OcmDataStore ocmDataStore;

    if (!connectionUtils.hasConnection()) return getDiskDataStore();

    if (force) {
      Log.i(TAG, "CLOUD - Detail");
      ocmDataStore = getCloudDataStore();
    } else {
      OcmCache cache = diskDataStore.getOcmCache();
      if (cache.isDetailCached(slug) && !cache.isDetailExpired(slug)) {
        Log.i(TAG, "DISK  - Detail");
        ocmDataStore = getDiskDataStore();
      } else {
        Log.i(TAG, "CLOUD - Detail");
        ocmDataStore = getCloudDataStore();
      }
    }

    return ocmDataStore;
  }

  public OcmDataStore getCloudDataStore() {
    return cloudDataStore;
  }

  public OcmDataStore getDiskDataStore() {
    return diskDataStore;
  }
}
