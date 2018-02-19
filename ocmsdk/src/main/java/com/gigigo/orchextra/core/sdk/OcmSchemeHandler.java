package com.gigigo.orchextra.core.sdk;

import android.text.TextUtils;
import android.widget.ImageView;
import com.gigigo.orchextra.core.data.rxException.NetworkConnectionException;
import com.gigigo.orchextra.core.domain.OcmController;
import com.gigigo.orchextra.core.domain.OcmControllerKt;
import com.gigigo.orchextra.core.domain.entities.elementcache.ElementCache;
import com.gigigo.orchextra.core.domain.entities.elementcache.ElementCacheRender;
import com.gigigo.orchextra.core.domain.entities.elementcache.ElementCacheType;
import com.gigigo.orchextra.core.domain.entities.elementcache.FederatedAuthorization;
import com.gigigo.orchextra.core.domain.entities.elementcache.VideoFormat;
import com.gigigo.orchextra.core.domain.entities.elements.ElementSegmentation;
import com.gigigo.orchextra.core.domain.entities.menus.RequiredAuthoritation;
import com.gigigo.orchextra.core.domain.entities.ocm.Authoritation;
import com.gigigo.orchextra.core.domain.utils.ConnectionUtils;
import com.gigigo.orchextra.core.sdk.actions.ActionHandler;
import com.gigigo.orchextra.core.sdk.application.OcmContextProvider;
import com.gigigo.orchextra.core.sdk.model.detail.DetailActivity;
import com.gigigo.orchextra.core.sdk.ui.OcmWebViewActivity;
import com.gigigo.orchextra.core.sdk.utils.DeviceUtils;
import com.gigigo.orchextra.ocm.OCManager;
import com.gigigo.orchextra.ocm.OcmEvent;
import java.lang.ref.WeakReference;

public class OcmSchemeHandler {

  private final OcmContextProvider contextProvider;
  private final OcmController ocmController;
  private final OcmControllerKt ocmControllerKt;
  private final ActionHandler actionHandler;
  private final Authoritation authoritation;
  private final ConnectionUtils connectionUtils;
  private String elementURL;
  private String processElementURL;

  public OcmSchemeHandler(OcmContextProvider contextProvider, OcmController ocmController, OcmControllerKt ocmControllerKt,
      ActionHandler actionHandler, Authoritation authoritation, ConnectionUtils connectionUtils) {
    this.contextProvider = contextProvider;
    this.ocmController = ocmController;
    this.ocmControllerKt = ocmControllerKt;
    this.actionHandler = actionHandler;
    this.authoritation = authoritation;
    this.connectionUtils = connectionUtils;
  }

  public void processElementUrl(final String elementUrl) {
    String elementUri = elementUrl;
    if (elementURL != null) {
      elementUri = elementURL;
      elementURL = null;
    }

    String finalElementUri = elementUri;
    ocmControllerKt.getDetail(elementUri, new OcmControllerKt.GetDetailControllerCallback() {
      @Override public void onDetailLoaded(ElementCache elementCache) {
        if (elementCache != null) {
          if (!connectionUtils.hasConnection() && (elementCache.getType() == ElementCacheType.WEBVIEW || elementCache.getType() == ElementCacheType.VIDEO)) {
           // processElementCallback.onProcessElementFail(new NetworkConnectionException());
          }
          else {
            if (elementRequiredUserToBeLogged(elementCache)) {
              // Save url of the element that require login
              elementURL = elementUrl;
              OCManager.notifyRequiredLoginToContinue(elementURL);
            } else {
              executeAction(elementCache, finalElementUri, null, null);
            }
          }
        }
      }

      @Override public void onDetailFails(Exception e) {
        e.printStackTrace();
      }
    });
  }

  public interface ProcessElementCallback {
    void onProcessElementSuccess(ElementCache elementCache);
    void onProcessElementFail(Exception exception);
  }

  public void processElementUrl(String elementUrl, ImageView imageViewToExpandInDetail, ProcessElementCallback processElementCallback) {

    WeakReference<ImageView> imageViewWeakReference =
        new WeakReference<>(imageViewToExpandInDetail);

    String elementUri = elementUrl;
    if (processElementURL != null) {
      elementUri = processElementURL;
      processElementURL = null;
    }

    String finalElementUri = elementUri;
    ocmControllerKt.getDetail(elementUri, new OcmControllerKt.GetDetailControllerCallback() {
      @Override public void onDetailLoaded(ElementCache elementCache) {
        if (elementCache != null) {
          if (!connectionUtils.hasConnection() && (elementCache.getType() == ElementCacheType.WEBVIEW || elementCache.getType() == ElementCacheType.VIDEO)) {
            processElementCallback.onProcessElementFail(new NetworkConnectionException());
          }
          else {

            processElementCallback.onProcessElementSuccess(elementCache);

            String urlImageToExpand = null;
            if (elementCache != null && elementCache.getPreview() != null) {
              urlImageToExpand = elementCache.getPreview().getImageUrl();
            }

            if (elementRequiredUserToBeLogged(elementCache)) {
              // Save url of the element that require login
              processElementURL = elementUrl;
              OCManager.notifyRequiredLoginToContinue(processElementURL);
            } else {
              executeAction(elementCache, finalElementUri, urlImageToExpand, imageViewWeakReference);
            }
          }
        }
      }

      @Override public void onDetailFails(Exception exception) {
        processElementCallback.onProcessElementFail(exception);
      }
    });
  }

  private boolean elementRequiredUserToBeLogged(ElementCache elementCache){
    ElementSegmentation segmentation = elementCache.getSegmentation();

    boolean loggedRequired = false;
    if(segmentation!=null) {
      loggedRequired = RequiredAuthoritation.LOGGED.equals(segmentation.getRequiredAuth());
    }

    return loggedRequired && !authoritation.isAuthorizatedUser();
  }

  public void executeAction(ElementCache cachedElement, String elementUrl, String urlImageToExpand, WeakReference<ImageView> imageViewToExpandInDetail) {

    boolean hasPreview = cachedElement.getPreview() != null;

    if (hasPreview) {
      processDetailActivity(elementUrl, urlImageToExpand, imageViewToExpandInDetail);
      return;
    }

    ElementCacheType type = cachedElement.getType();
    ElementCacheRender render = cachedElement.getRender();

    switch (type) {
      case VUFORIA:
        OCManager.notifyEvent(OcmEvent.OPEN_IR, cachedElement);
        if (render != null) {
          processImageRecognitionAction();
        }
        break;
      case SCAN:
        OCManager.notifyEvent(OcmEvent.OPEN_BARCODE, cachedElement);
        if (render != null) {
          processScanAction();
        }
        break;
      case WEBVIEW:
        OCManager.notifyEvent(OcmEvent.VISIT_URL, cachedElement);
        if (render != null) {
          OcmWebViewActivity.open(contextProvider.getCurrentActivity(), render, "");
        }
        break;

      case BROWSER:
        OCManager.notifyEvent(OcmEvent.VISIT_URL, cachedElement);
        if (render != null) {
          processCustomTabs(render.getUrl(), render.getFederatedAuth());
        }
        break;
      case EXTERNAL_BROWSER:
        OCManager.notifyEvent(OcmEvent.VISIT_URL, cachedElement);
        if (render != null) {
          processExternalBrowser(render.getUrl(), render.getFederatedAuth());
        }
        break;
      case DEEP_LINK:
        OCManager.notifyEvent(OcmEvent.VISIT_URL, cachedElement);
        if (render != null) {
          processDeepLink(render.getSchemeUri());
        }
        break;
      case VIDEO:
        if (render != null) {
          processVideo(render.getFormat(), render.getSource(), cachedElement);
        }
        break;
      default:
        processDetailActivity(elementUrl, urlImageToExpand, imageViewToExpandInDetail);
        break;
    }
  }

  private void processDetailActivity(String elementUrl, String urlImageToExpand, WeakReference<ImageView> imageViewToExpandInDetail) {
    int widthScreen = DeviceUtils.calculateRealWidthDeviceInImmersiveMode(contextProvider.getCurrentActivity());
    int heightScreen = DeviceUtils.calculateHeightDeviceInImmersiveMode(contextProvider.getCurrentActivity());

    ImageView imageView = null;
    if (imageViewToExpandInDetail != null) {
      imageView = imageViewToExpandInDetail.get();
    }
    openDetailActivity(elementUrl, urlImageToExpand, widthScreen, heightScreen, imageView);
  }

  private void processVideo(VideoFormat format, String source, ElementCache cachedElement) {
    if (TextUtils.isEmpty(source) || format == VideoFormat.NONE) {
      return;
    } else if (format == VideoFormat.YOUTUBE) {
      OCManager.notifyEvent(OcmEvent.PLAY_YOUTUBE, cachedElement);
      actionHandler.launchYoutubePlayer(source);
    } else if (format == VideoFormat.VIMEO) {
      OCManager.notifyEvent(OcmEvent.PLAY_VIMEO, cachedElement);
      actionHandler.launchVimeoPlayer(source);
    }
  }

  private void processCustomTabs(String url, FederatedAuthorization federatedAuthorization) {
    DeviceUtils.openChromeTabs(contextProvider.getCurrentActivity(), url, federatedAuthorization);
  }

  private void processImageRecognitionAction() {
    actionHandler.launchOxVuforia();
  }

  private void processScanAction() {
    actionHandler.lauchOxScan();
  }

  private void processExternalBrowser(String url, FederatedAuthorization federatedAuth) {
    actionHandler.launchExternalBrowser(url, federatedAuth);
  }

  private void processDeepLink(String uri) {
    actionHandler.processDeepLink(uri);
  }

  private void openDetailActivity(String elementUrl, String urlImageToExpand, int widthScreen,
      int heightScreen, ImageView imageViewToExpandInDetail) {
    DetailActivity.open(contextProvider.getCurrentActivity(), elementUrl, urlImageToExpand,
        widthScreen, heightScreen, imageViewToExpandInDetail);
  }
}
