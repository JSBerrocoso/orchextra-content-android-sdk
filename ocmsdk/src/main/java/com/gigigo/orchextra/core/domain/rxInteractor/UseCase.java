package com.gigigo.orchextra.core.domain.rxInteractor;

import com.fernandocejas.arrow.checks.Preconditions;
import com.gigigo.orchextra.core.domain.rxExecutor.PostExecutionThread;
import com.gigigo.orchextra.core.domain.rxExecutor.ThreadExecutor;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Abstract class for a Use Case (Interactor in terms of Clean Architecture).
 * This interface represents a execution unit for different use cases (this means any use case
 * in the application should implement this contract).
 *
 * By convention each UseCase implementation will return the result using a {@link DisposableObserver}
 * that will execute its job in a background thread and will post the result in the UI thread.
 */
public abstract class UseCase<T, Params> {

  private final PriorityScheduler threadExecutor;
  private final PostExecutionThread postExecutionThread;


  protected UseCase(PriorityScheduler threadExecutor, PostExecutionThread postExecutionThread) {
    this.threadExecutor = threadExecutor;
    this.postExecutionThread = postExecutionThread;

  }

  /**
   * Builds an {@link Observable} which will be used when executing the current {@link UseCase}.
   */
  abstract Observable<T> buildUseCaseObservable(Params params);

  /**
   * Executes the current use case.
   *
   * @param observer {@link DisposableObserver} which will be listening to the observable build
   * by {@link #buildUseCaseObservable(Params)} ()} method.
   * @param params Parameters (Optional) used to build/execute this use case.
   */
  public void execute(DisposableObserver<T> observer, Params params, PriorityScheduler.Priority priority) {
    Preconditions.checkNotNull(observer);

    final Observable<T> observable = this.buildUseCaseObservable(params)
        .subscribeOn(threadExecutor.priority(priority.getPriority()))
        .observeOn(postExecutionThread.getScheduler());

    observable.subscribeWith(observer);

    //observable.subscribe(new Observer<T>() {
    //      @Override public void onSubscribe(@NonNull Disposable d) {
    //        disposables.add(d);
    //      }
    //
    //      @Override public void onNext(@NonNull T t) {
    //
    //      }
    //
    //      @Override public void onError(@NonNull Throwable e) {
    //
    //      }
    //
    //      @Override public void onComplete() {
    //        disposables.dispose();
    //      }
    //    });
    //addDisposable(observable.subscribeWith(observer));
  }

  ///**
  // * Dispose from current {@link CompositeDisposable}.
  // */
  //public void dispose() {
  //  if (!disposables.isDisposed()) {
  //    disposables.dispose();
  //  }
  //}

  ///**
  // * Dispose from current {@link CompositeDisposable}.
  // */
  //private void addDisposable(Disposable disposable) {
  //  Preconditions.checkNotNull(disposable);
  //  Preconditions.checkNotNull(disposables);
  //  disposables.add(disposable);
  //}
}
