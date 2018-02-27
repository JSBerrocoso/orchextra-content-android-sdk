package com.gigigo.orchextra.core.sdk;

import android.text.TextUtils;
import android.widget.ImageView;
import com.gigigo.orchextra.core.data.rxException.NetworkConnectionException;
import com.gigigo.orchextra.core.domain.OcmController;
import com.gigigo.orchextra.core.domain.entities.elementcache.ElementCache;
import com.gigigo.orchextra.core.domain.entities.elementcache.ElementCacheRender;
import com.gigigo.orchextra.core.domain.entities.elementcache.ElementCacheType;
import com.gigigo.orchextra.core.domain.entities.elementcache.FederatedAuthorization;
import com.gigigo.orchextra.core.domain.entities.elementcache.VideoFormat;
import com.gigigo.orchextra.core.sdk.actions.ActionHandler;
import com.gigigo.orchextra.core.sdk.application.OcmContextProvider;
import com.gigigo.orchextra.core.sdk.model.detail.DetailActivity;
import com.gigigo.orchextra.core.sdk.ui.OcmWebViewActivity;
import com.gigigo.orchextra.core.sdk.utils.DeviceUtils;
import com.gigigo.orchextra.ocm.OCManager;
import com.gigigo.orchextra.ocm.OcmEvent;
import java.lang.ref.WeakReference;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class OcmSchemeHandler {

  private final OcmContextProvider contextProvider;
  private final OcmController ocmController;
  private final ActionHandler actionHandler;

  public OcmSchemeHandler(OcmContextProvider contextProvider, OcmController ocmController,
      ActionHandler actionHandler) {
    this.contextProvider = contextProvider;
    this.ocmController = ocmController;
    this.actionHandler = actionHandler;
  }

  public void processRedirectElementUrl(final String elementUrl) {
    ocmController.getDetails(elementUrl, new OcmController.GetDetailControllerCallback() {
      @Override public void onGetDetailLoaded(ElementCache elementCache) {
        if (elementCache != null) {
          if (elementCache.getCustomProperties() != null) {
            OCManager.notifyCustomBehaviourContinue(elementCache.getCustomProperties(), null,
                new Function1<Boolean, Unit>() {
                  @Override public Unit invoke(Boolean canContinue) {
                    if (canContinue) {
                      executeAction(elementCache, elementUrl, null, null);
                    }
                    return null;
                  }
                });
          } else {
            executeAction(elementCache, elementUrl, null, null);
          }
        }
      }

      @Override public void onGetDetailFails(Exception e) {
        e.printStackTrace();
      }

      @Override public void onGetDetailNoAvailable(Exception e) {
        e.printStackTrace();
      }
    });
  }

  public void processElementUrl(String elementUrl, ImageView imageViewToExpandInDetail,
      ProcessElementCallback processElementCallback) {
    WeakReference<ImageView> imageViewWeakReference =
        new WeakReference<>(imageViewToExpandInDetail);

    ocmController.getDetails(elementUrl, new OcmController.GetDetailControllerCallback() {

      @Override public void onGetDetailLoaded(ElementCache elementCache) {
        if (elementCache != null) {
          if (elementCache.getCustomProperties() != null) {
            OCManager.notifyCustomBehaviourContinue(elementCache.getCustomProperties(), null,
                new Function1<Boolean, Unit>() {
                  @Override public Unit invoke(Boolean canContinue) {
                    if (canContinue) {
                      processElementCallback.onProcessElementSuccess(elementCache);

                      String urlImageToExpand = null;
                      if (elementCache != null && elementCache.getPreview() != null) {
                        urlImageToExpand = elementCache.getPreview().getImageUrl();
                      }

                      executeAction(elementCache, elementUrl, urlImageToExpand, imageViewWeakReference);
                    }
                    return null;
                  }
                });
          } else {
            processElementCallback.onProcessElementSuccess(elementCache);

            String urlImageToExpand = null;
            if (elementCache != null && elementCache.getPreview() != null) {
              urlImageToExpand = elementCache.getPreview().getImageUrl();
            }

            executeAction(elementCache, elementUrl, urlImageToExpand, imageViewWeakReference);
          }
        }
      }

      @Override public void onGetDetailFails(Exception e) {
        processElementCallback.onProcessElementFail(e);
      }

      @Override public void onGetDetailNoAvailable(Exception e) {
        processElementCallback.onProcessElementFail(new NetworkConnectionException());
      }
    });
  }

  private void executeAction(ElementCache cachedElement, String elementUrl, String urlImageToExpand,
      WeakReference<ImageView> imageViewToExpandInDetail) {

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

  private void processDetailActivity(String elementUrl, String urlImageToExpand,
      WeakReference<ImageView> imageViewToExpandInDetail) {
    int widthScreen =
        DeviceUtils.calculateRealWidthDeviceInImmersiveMode(contextProvider.getCurrentActivity());
    int heightScreen =
        DeviceUtils.calculateHeightDeviceInImmersiveMode(contextProvider.getCurrentActivity());

    ImageView imageView = null;
    if (imageViewToExpandInDetail != null) {
      imageView = imageViewToExpandInDetail.get();
    }
    DetailActivity.open(contextProvider.getCurrentActivity(), elementUrl, urlImageToExpand, widthScreen, heightScreen, imageView);
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

  public interface ProcessElementCallback {
    void onProcessElementSuccess(ElementCache elementCache);

    void onProcessElementFail(Exception exception);
  }
}
