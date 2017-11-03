package com.gigigo.orchextra.core.sdk;

import android.text.TextUtils;
import android.widget.ImageView;
import com.gigigo.orchextra.core.domain.OcmController;
import com.gigigo.orchextra.core.domain.entities.elementcache.ElementCache;
import com.gigigo.orchextra.core.domain.entities.elementcache.ElementCacheRender;
import com.gigigo.orchextra.core.domain.entities.elementcache.ElementCacheType;
import com.gigigo.orchextra.core.domain.entities.elementcache.FederatedAuthorization;
import com.gigigo.orchextra.core.domain.entities.elementcache.VideoFormat;
import com.gigigo.orchextra.core.domain.entities.menus.RequiredAuthoritation;
import com.gigigo.orchextra.core.domain.entities.ocm.Authoritation;
import com.gigigo.orchextra.core.sdk.actions.ActionHandler;
import com.gigigo.orchextra.core.sdk.application.OcmContextProvider;
import com.gigigo.orchextra.core.sdk.model.detail.DetailActivity;
import com.gigigo.orchextra.core.sdk.utils.DeviceUtils;
import com.gigigo.orchextra.ocm.OCManager;
import java.lang.ref.WeakReference;

public class OcmSchemeHandler {

  private final OcmContextProvider contextProvider;
  private final OcmController ocmController;
  private final ActionHandler actionHandler;
  private final Authoritation authoritation;

  public OcmSchemeHandler(OcmContextProvider contextProvider, OcmController ocmController,
      ActionHandler actionHandler, Authoritation authoritation) {
    this.contextProvider = contextProvider;
    this.ocmController = ocmController;
    this.actionHandler = actionHandler;
    this.authoritation = authoritation;
  }

  public void processElementUrl(final String elementUrl) {
    ocmController.getDetails(false, elementUrl, new OcmController.GetDetailControllerCallback() {
      @Override public void onGetDetailLoaded(ElementCache elementCache) {
        if (elementCache != null) {
          if (elementRequiredUserToBeLogged(elementCache)) {
            OCManager.notifyRequiredLoginToContinue();
          } else {
            executeAction(elementCache, elementUrl, null, 0, 0, null);
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

  public void processElementUrl(String elementUrl, String urlImageToExpand, int widthScreen,
      int heightScreen, ImageView imageViewToExpandInDetail) {

    WeakReference<ImageView> imageViewWeakReference =
        new WeakReference<>(imageViewToExpandInDetail);

    ocmController.getDetails(false, elementUrl, new OcmController.GetDetailControllerCallback() {
      @Override public void onGetDetailLoaded(ElementCache elementCache) {
        if (elementCache != null) {
          if (elementRequiredUserToBeLogged(elementCache)) {
            OCManager.notifyRequiredLoginToContinue();
          } else {
            executeAction(elementCache, elementUrl, urlImageToExpand, widthScreen, heightScreen,
                imageViewWeakReference);
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

  private boolean elementRequiredUserToBeLogged(ElementCache elementCache) {
    return elementCache.getSegmentation().getRequiredAuth().equals(
        RequiredAuthoritation.LOGGED) && !authoritation.isAuthorizatedUser();
  }

  private void executeAction(ElementCache cachedElement, String elementUrl, String urlImageToExpand,
      int widthScreen, int heightScreen, WeakReference<ImageView> imageViewToExpandInDetail) {

    boolean hasPreview = cachedElement.getPreview() != null;

    if (hasPreview) {
      processDetailActivity(elementUrl, urlImageToExpand, widthScreen, heightScreen,
          imageViewToExpandInDetail);
      return;
    }

    ElementCacheType type = cachedElement.getType();
    ElementCacheRender render = cachedElement.getRender();

    switch (type) {
      case VUFORIA:
        if (render != null) {
          processImageRecognitionAction();
        }
        break;
      case SCAN:
        if (render != null) {
          processScanAction();
        }
        break;
      case BROWSER:
        if (render != null) {
          processCustomTabs(render.getUrl(), render.getFederatedAuth());
        }
        break;
      case EXTERNAL_BROWSER:
        if (render != null) {
          processExternalBrowser(render.getUrl(), render.getFederatedAuth());
        }
        break;
      case DEEP_LINK:
        if (render != null) {
          processDeepLink(render.getUri());
        }
        break;
      case VIDEO:
        if (render != null) {
          processVideo(render.getFormat(), render.getSource());
        }
        break;
      default:
        processDetailActivity(elementUrl, urlImageToExpand, widthScreen, heightScreen,
            imageViewToExpandInDetail);
        break;
    }
  }

  private void processDetailActivity(String elementUrl, String urlImageToExpand, int widthScreen,
      int heightScreen, WeakReference<ImageView> imageViewToExpandInDetail) {
    ImageView imageView = null;
    if (imageViewToExpandInDetail != null) {
      imageView = imageViewToExpandInDetail.get();
    }
    openDetailActivity(elementUrl, urlImageToExpand, widthScreen, heightScreen, imageView);
  }

  private void processVideo(VideoFormat format, String source) {
    if (TextUtils.isEmpty(source) || format == VideoFormat.NONE) {
      return;
    } else if (format == VideoFormat.YOUTUBE) {
      actionHandler.launchYoutubePlayer(source);
    } else if (format == VideoFormat.VIMEO) {
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
