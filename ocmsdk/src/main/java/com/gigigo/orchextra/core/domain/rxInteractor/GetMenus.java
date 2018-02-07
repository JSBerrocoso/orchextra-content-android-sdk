package com.gigigo.orchextra.core.domain.rxInteractor;

import com.gigigo.orchextra.core.domain.entities.DataRequest;
import com.gigigo.orchextra.core.domain.entities.menus.MenuContentData;
import com.gigigo.orchextra.core.domain.rxExecutor.PostExecutionThread;
import com.gigigo.orchextra.core.domain.rxRepository.OcmRepository;
import io.reactivex.Observable;
import orchextra.javax.inject.Inject;

/**
 * This class is an implementation of {@link UseCase} that represents a use case for
 * retrieving a collection of all {@link MenuContentData}.
 */
public class GetMenus extends UseCase<MenuContentData, GetMenus.Params> {

  private final OcmRepository ocmRepository;

  @Inject GetMenus(OcmRepository ocmRepository, PriorityScheduler threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.ocmRepository = ocmRepository;
  }

  @Override Observable<MenuContentData> buildUseCaseObservable(Params params) {
    return this.ocmRepository.getMenu(params.forceSource);
  }

  public static final class Params {

    private final DataRequest forceSource;

    private Params(DataRequest source) {
      this.forceSource = source;
    }

    public static Params forForceSource(DataRequest source) {
      return new Params(source);
    }
  }
}
