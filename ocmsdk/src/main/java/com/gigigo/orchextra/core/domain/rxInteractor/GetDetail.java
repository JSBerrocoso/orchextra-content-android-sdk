package com.gigigo.orchextra.core.domain.rxInteractor;

import com.gigigo.orchextra.core.domain.entities.DataRequest;
import com.gigigo.orchextra.core.domain.entities.elements.ElementData;
import com.gigigo.orchextra.core.domain.rxExecutor.PostExecutionThread;
import com.gigigo.orchextra.core.domain.rxRepository.OcmRepository;
import io.reactivex.Observable;
import orchextra.javax.inject.Inject;

/**
 * This class is an implementation of {@link UseCase} that represents a use case for
 * retrieving a collection of all {@link ElementData}.
 */
public class GetDetail extends UseCase<ElementData, GetDetail.Params> {

  private final OcmRepository ocmRepository;

  @Inject GetDetail(OcmRepository ocmRepository, PriorityScheduler threadExecutor,
      PostExecutionThread postExecutionThread) {
    super(threadExecutor, postExecutionThread);
    this.ocmRepository = ocmRepository;
  }

  @Override Observable<ElementData> buildUseCaseObservable(Params params) {
    return this.ocmRepository.getDetail(params.forceSource, params.slug);
  }

  public static final class Params {

    private final DataRequest forceSource;
    private final String slug;

    private Params(DataRequest forceSource, String slug) {
      this.forceSource = forceSource;
      this.slug = slug;
    }

    public static Params forDetail(DataRequest forceSource, String slug) {
      return new Params(forceSource, slug);
    }
  }
}
