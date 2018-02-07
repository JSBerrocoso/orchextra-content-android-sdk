package com.gigigo.orchextra.core.sdk.di.modules;

import com.gigigo.orchextra.core.controller.OcmControllerImp;
import com.gigigo.orchextra.core.controller.OcmControllerImpKt;
import com.gigigo.orchextra.core.data.rxCache.OcmCache;
import com.gigigo.orchextra.core.data.rxCache.OcmCacheImp;
import com.gigigo.orchextra.core.data.rxCache.imageCache.OcmImageCache;
import com.gigigo.orchextra.core.data.rxCache.imageCache.OcmImageCacheImp;
import com.gigigo.orchextra.core.data.rxExecutor.JobExecutor;
import com.gigigo.orchextra.core.data.rxRepository.OcmDataRepository;
import com.gigigo.orchextra.core.domain.OcmController;
import com.gigigo.orchextra.core.domain.OcmControllerKt;
import com.gigigo.orchextra.core.domain.rxExecutor.PostExecutionThread;
import com.gigigo.orchextra.core.domain.rxExecutor.ThreadExecutor;
import com.gigigo.orchextra.core.domain.rxInteractor.ClearCache;
import com.gigigo.orchextra.core.domain.rxInteractor.GetDetail;
import com.gigigo.orchextra.core.domain.rxInteractor.GetMenus;
import com.gigigo.orchextra.core.domain.rxInteractor.GetSection;
import com.gigigo.orchextra.core.domain.rxInteractor.GetVersion;
import com.gigigo.orchextra.core.domain.rxInteractor.PriorityScheduler;
import com.gigigo.orchextra.core.domain.rxInteractor.SearchElements;
import com.gigigo.orchextra.core.domain.rxRepository.OcmRepository;
import com.gigigo.orchextra.core.domain.utils.ConnectionUtils;
import com.gigigo.orchextra.core.sdk.application.OcmContextProvider;
import com.gigigo.orchextra.core.sdk.utils.OcmPreferences;
import com.gigigo.orchextra.ocm.UIThread;
import orchextra.dagger.Module;
import orchextra.dagger.Provides;
import orchextra.javax.inject.Singleton;

@Module(includes = { DomainModule.class, InteractorModule.class }) public class ControllerModule {

  @Provides OcmController provideOcmController(GetVersion getVersion, GetMenus getMenus,
      GetSection getSection, GetDetail getDetail, SearchElements searchElements,
      ClearCache clearCache, ConnectionUtils connectionUtils, OcmPreferences ocmPreferences) {

    return new OcmControllerImp(getVersion, getMenus, getSection, getDetail, searchElements,
        clearCache, connectionUtils, ocmPreferences);
  }

  @Provides OcmControllerKt provideOcmControllerKt(GetVersion getVersion, GetMenus getMenus,
      GetSection getSection, OcmPreferences ocmPreferences) {

    return new OcmControllerImpKt(getVersion, getMenus, getSection, ocmPreferences);
  }


  @Provides @Singleton ThreadExecutor provideThreadExecutor(JobExecutor jobExecutor) {
    return jobExecutor;
  }

  @Provides @Singleton PriorityScheduler providePriorityScheduler() {
    return PriorityScheduler.create();
  }

  @Provides @Singleton PostExecutionThread providePostExecutionThread(UIThread uiThread) {
    return uiThread;
  }

  @Provides @Singleton OcmRepository provideOcmRepository(OcmDataRepository ocmDataRepository) {
    return ocmDataRepository;
  }

  @Provides @Singleton OcmCache provideCache(OcmContextProvider context) {
    return new OcmCacheImp(context.getApplicationContext(),
        context.getApplicationContext().getCacheDir().getPath());
  }

  @Provides @Singleton OcmImageCache provideImageCache(OcmContextProvider context,
      ThreadExecutor threadExecutor, ConnectionUtils connectionUtils) {
    return new OcmImageCacheImp(context.getApplicationContext(), threadExecutor, connectionUtils);
  }
}
