package com.gigigo.showcase;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import com.gigigo.orchextra.core.Orchextra;
import com.gigigo.orchextra.core.controller.model.grid.ImageTransformReadArticle;
import com.gigigo.orchextra.core.domain.entities.OxCRM;
import com.gigigo.orchextra.ocm.OCManagerCallbacks;
import com.gigigo.orchextra.ocm.Ocm;
import com.gigigo.orchextra.ocm.OcmBuilder;
import com.gigigo.orchextra.ocm.OcmCallbacks;
import com.gigigo.orchextra.ocm.OcmEvent;
import com.gigigo.orchextra.ocm.OcmStyleUiBuilder;
import com.gigigo.orchextra.ocm.callbacks.OcmCredentialCallback;
import com.gigigo.orchextra.ocm.callbacks.OnEventCallback;
import com.gigigo.orchextra.ocm.views.UiGridBaseContentData;
import com.gigigo.showcase.main.MainActivity;
import com.gigigo.showcase.settings.ProjectData;
import java.util.Date;
import java.util.Map;

public class ContentManager {

  private static final ContentManager instance = new ContentManager();
  private static final String COUNTRY = "it";
  private Handler handler;
  private Orchextra orchextra;

  public static ContentManager getInstance() {
    return instance;
  }

  private ContentManager() {
    this.handler = new Handler(Looper.getMainLooper());
    this.orchextra = Orchextra.INSTANCE;
  }

  public void start(Application application, ContentManagerCallback<String> callback) {
    start(application, ProjectData.getDefaultApiKey(), ProjectData.getDefaultApiSecret(), callback);
  }

  public void start(Application application, String apiKey, String apiSecret,
      final ContentManagerCallback<String> callback) {

    //Ocm.setBusinessUnit(COUNTRY);

    OcmBuilder ocmBuilder =
        new OcmBuilder(application).setNotificationActivityClass(MainActivity.class)
            .setShowReadArticles(true)
            .setTransformReadArticleMode(ImageTransformReadArticle.BITMAP_TRANSFORM)
            .setMaxReadArticles(100)
            .setOrchextraCredentials(apiKey, apiSecret)
            .setContentLanguage("EN")
            .setFirebaseApiKey("TODO") // TODO set Firebase Api Key
            .setFirebaseApplicationId("TODO") // TODO set Firebase Application Id
            .setOnEventCallback(new OnEventCallback() {
              @Override public void doEvent(OcmEvent event, Object data) {
              }

              @Override public void doEvent(OcmEvent event) {
              }
            });

    Ocm.init(ocmBuilder, new OcmCredentialCallback() {
      @Override public void onCredentialReceiver(final String accessToken) {
        handler.post(new Runnable() {
          @Override public void run() {
            callback.onSuccess(accessToken);
          }
        });
      }

      @Override public void onCredentailError(final String code) {
        handler.post(new Runnable() {
          @Override public void run() {
            callback.onError(new Exception(code));
          }
        });
      }
    });

    OcmStyleUiBuilder ocmStyleUiBuilder =
        new OcmStyleUiBuilder().setTitleToolbarEnabled(true).setEnabledStatusBar(true);

    Ocm.setStyleUi(ocmStyleUiBuilder);
  }

  public void clear() {
    Ocm.clearData(true, true, new OCManagerCallbacks.Clear() {
      @Override public void onDataClearedSuccessfull() {

      }

      @Override public void onDataClearFails(Exception e) {

      }
    });
  }

  public void getContent(String section, int imagesToDownload,
      final ContentManagerCallback<UiGridBaseContentData> callback) {

    Ocm.generateSectionView(section, null, imagesToDownload, new OcmCallbacks.Section() {
      @Override public void onSectionLoaded(UiGridBaseContentData uiGridBaseContentData) {
        callback.onSuccess(uiGridBaseContentData);
      }

      @Override public void onSectionFails(Exception e) {
        callback.onError(e);
      }
    });
  }

  public Map<String, String> getCustomFields() {
    return orchextra.getCrmManager().getCrm().getCustomFields();
  }

  public void setUserCustomFields(Map<String, String> customFields) {
    orchextra.getCrmManager().bindUser(getCrmUser("test2", customFields));
  }

  private OxCRM getCrmUser(String id, Map<String, String> customFields) {
    return new OxCRM(id, "f", new Date(), null, null, customFields);
  }

  public interface ContentManagerCallback<T> {

    void onSuccess(T result);

    void onError(Exception exception);
  }
}