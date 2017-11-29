package com.gigigo.orchextra.core.controller.model.detail;

import com.gigigo.orchextra.core.Orchextra;
import com.gigigo.orchextra.core.OrchextraTokenReceiver;
import com.gigigo.orchextra.core.domain.entities.OxCRM;
import com.gigigo.orchextra.legacy.Presenter;
import com.gigigo.orchextra.ocm.Ocm;
import com.gigigo.orchextra.ocm.callbacks.OnFinishViewListener;
import com.gigigo.orchextra.ocm.views.UiDetailBaseContentData;

public class DetailPresenter extends Presenter<DetailView> {

  private OnFinishViewListener onFinishViewListener;
  private String accessToken;

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

  public void setLoginUserFromNativeLogin(String userId) {

    OxCRM crmUser = new OxCRM(userId, null, null);
    Ocm.bindUser(crmUser);
    Ocm.setUserIsAuthorizated(true);

    Orchextra.INSTANCE.getToken(new OrchextraTokenReceiver() {
      @Override public void onGetToken(String accessToken) {
        if (!accessToken.equals(DetailPresenter.this.accessToken)) {
          DetailPresenter.this.accessToken = accessToken;
          getView().redirectToAction();
        }
      }
    });
  }
}
