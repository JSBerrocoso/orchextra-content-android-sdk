package com.gigigo.orchextra.ocm;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.webkit.WebStorage;
import android.widget.ImageView;
import com.gigigo.orchextra.core.Orchextra;
import com.gigigo.orchextra.core.OrchextraErrorListener;
import com.gigigo.orchextra.core.OrchextraOptions;
import com.gigigo.orchextra.core.OrchextraStatusListener;
import com.gigigo.orchextra.core.controller.OcmViewGenerator;
import com.gigigo.orchextra.core.domain.OcmController;
import com.gigigo.orchextra.core.domain.actions.actionexecutors.customaction.CustomActionListener;
import com.gigigo.orchextra.core.domain.entities.Error;
import com.gigigo.orchextra.core.domain.entities.OxCRM;
import com.gigigo.orchextra.core.domain.entities.elementcache.ElementCache;
import com.gigigo.orchextra.core.domain.entities.ocm.Authoritation;
import com.gigigo.orchextra.core.domain.entities.ocm.OxSession;
import com.gigigo.orchextra.core.sdk.OcmSchemeHandler;
import com.gigigo.orchextra.core.sdk.OcmStyleUi;
import com.gigigo.orchextra.core.sdk.application.OcmContextProvider;
import com.gigigo.orchextra.core.sdk.application.OcmSdkLifecycle;
import com.gigigo.orchextra.core.sdk.di.components.DaggerOcmComponent;
import com.gigigo.orchextra.core.sdk.di.components.OcmComponent;
import com.gigigo.orchextra.core.sdk.di.injector.Injector;
import com.gigigo.orchextra.core.sdk.di.injector.InjectorImpl;
import com.gigigo.orchextra.core.sdk.di.modules.OcmModule;
import com.gigigo.orchextra.core.sdk.model.detail.DetailActivity;
import com.gigigo.orchextra.ocm.callbacks.OcmCredentialCallback;
import com.gigigo.orchextra.ocm.callbacks.OnCustomSchemeReceiver;
import com.gigigo.orchextra.ocm.callbacks.OnEventCallback;
import com.gigigo.orchextra.ocm.callbacks.OnRequiredLoginCallback;
import com.gigigo.orchextra.ocm.dto.UiMenuData;
import com.gigigo.orchextra.ocm.views.UiDetailBaseContentData;
import com.gigigo.orchextra.ocm.views.UiGridBaseContentData;
import com.gigigo.orchextra.ocm.views.UiSearchBaseContentData;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import orchextra.javax.inject.Inject;

public final class OCManager {

  private static OCManager instance;

  @Inject OcmSdkLifecycle ocmSdkLifecycle;
  @Inject OcmContextProvider ocmContextProvider;
  @Inject OcmViewGenerator ocmViewGenerator;
  @Inject OxSession oxSession;
  @Inject Authoritation authoritation;
  @Inject OcmSchemeHandler schemeHandler;
  @Inject OcmStyleUi ocmStyleUi;
  @Inject OcmController ocmController;

  private OnRequiredLoginCallback onRequiredLoginCallback;
  private OnEventCallback onEventCallback;
  private String language;
  private InjectorImpl injector;
  private Map<String, String> localStorage;
  private OnCustomSchemeReceiver onCustomSchemeReceiver;
  private boolean isShowReadedArticles = false;
  private int maxReadArticles = 100;

  //cambio para el inicio selectivo, MEJORAR,
  //necesitamos un contexto para q la funcion setNewOrchextracredentials pueda comprobar las preferences
  //lo suyo es no guardarlo en las preferences, de momneto así y una mejora sencilla seria añadir el contexto a
  //la funcion de setNewOrchextracredentials, para no mantener el application cuando no es necesario

  static void initSdk(Application application) {
    getInstance();
    instance.initOcm(application);
  }

  static void setDoRequiredLoginCallback(OnRequiredLoginCallback onRequiredLoginCallback) {
    if (instance != null) {
      instance.onRequiredLoginCallback = onRequiredLoginCallback;
    }
  }

  static void setEventCallback(OnEventCallback onEventCallback) {
    if (instance != null) {
      instance.onEventCallback = onEventCallback;
    }
  }

  static void getMenus(boolean forceReload, final OCManagerCallbacks.Menus menusCallback) {
    if (instance != null) {
      instance.ocmViewGenerator.getMenu(forceReload,
          new OcmViewGenerator.GetMenusViewGeneratorCallback() {
            @Override public void onGetMenusLoaded(UiMenuData menus) {
              menusCallback.onMenusLoaded(menus);
            }

            @Override public void onGetMenusFails(Throwable e) {
              menusCallback.onMenusFails(e);
            }
          });
    }
  }

  static void generateSectionView(String viewId, String filter, int imagesToDownload,
      final OCManagerCallbacks.Section sectionCallback) {
    instance.ocmViewGenerator.generateSectionView(viewId, filter, imagesToDownload,
        new OcmViewGenerator.GetSectionViewGeneratorCallback() {
          @Override public void onSectionViewLoaded(UiGridBaseContentData uiGridBaseContentData) {
            sectionCallback.onSectionLoaded(uiGridBaseContentData);
          }

          @Override public void onSectionViewFails(Exception e) {
            sectionCallback.onSectionFails(e);
          }
        });
  }

  static void generateActionView(ElementCache elementCache,
      final OCManagerCallbacks.Section sectionCallback) {
    instance.ocmViewGenerator.generateActionView(elementCache,
        new OcmViewGenerator.GetSectionViewGeneratorCallback() {
          @Override public void onSectionViewLoaded(UiGridBaseContentData uiGridBaseContentData) {
            sectionCallback.onSectionLoaded(uiGridBaseContentData);
          }

          @Override public void onSectionViewFails(Exception e) {
            sectionCallback.onSectionFails(e);
          }
        });
  }

  public static void clearData(boolean images, boolean data,
      final OCManagerCallbacks.Clear clearCallback) {
    if (instance != null) {

      instance.ocmController.clearCache(images, data, new OcmController.ClearCacheCallback() {
        @Override public void onClearCacheSuccess() {
          // clearCookiesFedexAuth();
          clearCallback.onDataClearedSuccessfull();
        }

        @Override public void onClearCacheFails(Exception e) {
          clearCallback.onDataClearFails(e);
        }
      });
    }
  }

  public static UiDetailBaseContentData generateDetailView(String elementUrl) {
    return instance.ocmViewGenerator.generateDetailView(elementUrl);
  }

  public static void generateDetailView(String elementUrl, String urlImageToExpand, int widthScreen,
      int heightScreen, ImageView imageViewToExpandInDetail) {

    if (instance != null) {
      instance.schemeHandler.processElementUrl(elementUrl, urlImageToExpand, widthScreen,
          heightScreen, imageViewToExpandInDetail);
    }
  }

  static UiSearchBaseContentData generateSearchView() {
    return instance.ocmViewGenerator.generateSearchView();
  }

  static void setUserIsAuthorizated(boolean isAuthorizated) {
    if (instance != null) {
      instance.authoritation.setAuthorizatedUser(isAuthorizated);
    }
  }

  static void setLoggedAction(String elementUrl) {
    if (instance != null) {
      instance.schemeHandler.processElementUrl(elementUrl);
    }
  }

  static void setStyleUi(OcmStyleUiBuilder ocmUiBuilder) {
    if (instance != null) {
      instance.ocmStyleUi.setStyleUi(ocmUiBuilder);
    }
  }

  static void processDeepLinks(String path) {
    if (instance != null) {
      instance.schemeHandler.processElementUrl(path);
    }
  }

  public static Injector getInjector() {
    if (instance == null || instance.injector == null) {
      return null;
    }
    return instance.injector;
  }

  public static void notifyRequiredLoginToContinue() {
    if (instance != null && instance.onRequiredLoginCallback != null) {
      instance.onRequiredLoginCallback.doRequiredLogin();
    }
  }

  public static void notifyRequiredLoginToContinue(String elementUrl) {
    if (instance != null && instance.onRequiredLoginCallback != null) {
      instance.onRequiredLoginCallback.doRequiredLogin(elementUrl);
    }
  }

  public static void notifyEvent(OcmEvent event, Object data) {
    if (instance != null && instance.onEventCallback != null) {
      instance.onEventCallback.doEvent(event, data);
    }
  }

  public static void notifyEvent(OcmEvent event) {
    if (instance != null && instance.onEventCallback != null) {
      instance.onEventCallback.doEvent(event);
    }
  }

  static void bindUser(OxCRM crmUser) {
    Orchextra.INSTANCE.getCrmManager().bindUser(crmUser);
  }

  //region Orchextra method

  public static Map<String, String> getLocalStorage() {
    if (instance == null) {
      System.out.println("main getLocalStorageinstance ==null");
      return null;
    } else {
      System.out.println("main getLocalStorageinstance!==null");
    }
    return instance.localStorage;
  }

  //region cid
  public static void setLocalStorage(Map<String, String> localStorage) {
    if (instance != null) {
      instance.localStorage = localStorage;
      System.out.println("main setLocalStorage we have localstorage");
    } else {
      System.out.println("main setLocalStorage we have NOT localstorage");
    }
  }

  public static String getContentLanguage() {
    if (instance == null) {
      return null;
    }
    return instance.language;
  }

  static void setContentLanguage(String language) {
    if (instance != null) {
      instance.language = language;
    }
  }

  public static void setOnCustomSchemeReceiver(OnCustomSchemeReceiver onCustomSchemeReceiver) {
    OCManager.instance.onCustomSchemeReceiver = onCustomSchemeReceiver;
  }

  public static void stop() {
    Orchextra.INSTANCE.finish();
  }

  public static void returnOcCustomSchemeCallback(String customScheme) {
    if (instance.onCustomSchemeReceiver != null) {
      instance.onCustomSchemeReceiver.onReceive(customScheme);
    }
  }

  public static OcmContextProvider getOcmContextProvider() {
    OCManager instance = OCManager.instance;
    if (instance == null) {
      return null;
    }
    return OCManager.instance.ocmContextProvider;
  }

  static void closeDetailView() {
    OCManager instance = OCManager.instance;
    if (instance != null && instance.ocmContextProvider != null) {
      Activity currentActivity = instance.ocmContextProvider.getCurrentActivity();
      if (currentActivity != null && currentActivity instanceof DetailActivity) {
        currentActivity.finish();
      }
    }
  }

  public static synchronized OCManager getInstance() {
    if (instance != null) {
      return instance;
    }

    instance = new OCManager();
    return instance;
  }

  public static void showIconNewContent() {

  }

  private void initOcm(Application app) {
    initDependencyInjection(app);
    initLifecyle(app);
  }

  //endregion

  private void initDependencyInjection(Application app) {
    OcmComponent ocmComponent = DaggerOcmComponent.builder().ocmModule(new OcmModule(app)).build();

    injector = new InjectorImpl(ocmComponent);

    ocmComponent.injectOcm(OCManager.instance);
  }

  private void initLifecyle(Application app) {
    app.registerActivityLifecycleCallbacks(ocmSdkLifecycle);
  }

  public static void initOrchextra(Application app, String apiKey, String apiSecret,
      Class notificationActivityClass, String firebaseApiKey, String firebaseApplicationId,
      OcmCredentialCallback ocmCredentialCallback) {

    Orchextra orchextra = Orchextra.INSTANCE;

    orchextra.setStatusListener(new OrchextraStatusListener() {
      @Override public void onStatusChange(boolean isReady) {

      }
    });

    orchextra.setErrorListener(new OrchextraErrorListener() {
      @Override public void onError(Error error) {
        ocmCredentialCallback.onCredentailError(error.getMessage());
      }
    });

    orchextra.setCustomActionListener(new CustomActionListener() {
      @Override public void onCustomSchema(String customScheme) {
        returnOcCustomSchemeCallback(customScheme);
      }
    });

    OrchextraOptions options = new OrchextraOptions.Builder().firebaseApiKey(firebaseApiKey)
        .firebaseApplicationId(firebaseApplicationId)
        .debuggable(true)
        .build();

    orchextra.init(app, apiKey, apiSecret, options);
    orchextra.setScanTime(30);
    orchextra.setNotificationActivityClass(notificationActivityClass);
  }

  //region cookies FedexAuth
  public static void clearCookiesFedexAuth() {
    WebStorage.getInstance().deleteAllData();
  }

  public static void addArticleToReadedArticles(String articleSlug) {
    if (instance != null && instance.isShowReadedArticles) {
      ArrayList<String> lstReadArticles = instance.readReadArticles();
      if (lstReadArticles.size() == instance.maxReadArticles) {
        lstReadArticles.remove(instance.maxReadArticles - 1);
      }
      lstReadArticles.add(0, articleSlug);
      instance.writeReadArticles(lstReadArticles);
    }
  }

  public static boolean isThisArticleReaded(String articleSlug) {
    if (instance != null && instance.isShowReadedArticles) {
      ArrayList<String> ArrayReadedArticlesSlug = instance.readReadArticles();
      if (ArrayReadedArticlesSlug.indexOf(articleSlug) > -1) {
        return true;
      }
      return false;
    } else {
      return false;
    }
  }

  //region serialize list of read articles slugs
  private final String READ_ARTICLES_FILE = "read_articles_file.ocm";

  public ArrayList<String> readReadArticles() {

    ArrayList<String> lst =
        readSerializable(ocmContextProvider.getApplicationContext(), READ_ARTICLES_FILE);
    if (lst != null) {
      return lst;
    } else {
      return new ArrayList<>();
    }
  }

  public void writeReadArticles(ArrayList<String> readArticles) {
    saveSerializable(ocmContextProvider.getApplicationContext(), readArticles, READ_ARTICLES_FILE);
  }

  public static <T extends Serializable> void saveSerializable(Context context, T objectToSave,
      String fileName) {
    try {
      FileOutputStream fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
      ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

      objectOutputStream.writeObject(objectToSave);

      objectOutputStream.close();
      fileOutputStream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static <T extends Serializable> T readSerializable(Context context, String fileName) {
    T objectToReturn = null;

    try {
      FileInputStream fileInputStream = context.openFileInput(fileName);
      ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
      objectToReturn = (T) objectInputStream.readObject();

      objectInputStream.close();
      fileInputStream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return objectToReturn;
  }

  public static void removeSerializable(Context context, String filename) {
    context.deleteFile(filename);
  }

  public static boolean isFileExists(String fileName) {

    File file = new File(fileName);
    if (file.exists()) {
      return true;
    } else {
      return false;
    }
  }

  //endregion

  /***
   * you must to reload grid in onResume if this feature are enabled
   * @param showReadArticles
   */
  public static void setShowReadArticles(boolean showReadArticles) {

    if (instance != null) {
      instance.isShowReadedArticles = showReadArticles;
    }
  }

  public static void setMaxReadArticles(int maxReadArticles) {

    if (instance != null) {
      instance.maxReadArticles = maxReadArticles;
    }
  }

  public static boolean getShowReadArticles() {
    if (instance != null) {
      return instance.isShowReadedArticles;
    } else {
      return false;
    }
  }

  //endregion
  //todo readed articles

  public static void setBitmapTransformReadArticles(
      com.bumptech.glide.load.Transformation<Bitmap> transformBitmap) {
    if (getInstance() != null) getInstance().readArticlesBitmapTransform = transformBitmap;
  }

  //public static int transform = -1;
  com.bumptech.glide.load.Transformation<Bitmap> readArticlesBitmapTransform;

  public static com.bumptech.glide.load.Transformation<Bitmap> getBitmapTransformReadArticles() {
    if (getInstance() != null) {
      return getInstance().readArticlesBitmapTransform;
    } else {
      return null;
    }
  }
}
