package com.gigigo.orchextra.core.controller.model.detail;

import com.gigigo.orchextra.ocm.Ocm;
import com.gigigo.orchextra.ocm.callbacks.OnFinishViewListener;
import com.gigigo.orchextra.ocm.views.UiDetailBaseContentData;
import com.gigigo.orchextra.oxlegacy.Presenter;

public class DetailPresenter extends Presenter<DetailView> {

  private OnFinishViewListener onFinishViewListener;

  public DetailPresenter() {

  }

  @Override public void onViewAttached() {
    getView().initUi();
  }

  public void loadSection(String elementUrl) {
    UiDetailBaseContentData contentDetailView = Ocm.generateDetailView(elementUrl);

    if (contentDetailView != null) {
      contentDetailView.setOnFinishListener(onFinishViewListener);
      getView().setView(contentDetailView);
    } else {
      getView().showError();
    }
  }

  public void setOnFinishViewListener(OnFinishViewListener onFinishViewListener) {
    this.onFinishViewListener = onFinishViewListener;
  }
}
