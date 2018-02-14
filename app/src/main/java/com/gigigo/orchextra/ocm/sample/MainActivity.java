package com.gigigo.orchextra.ocm.sample;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;
import com.gigigo.orchextra.Orchextra;
import com.gigigo.orchextra.core.domain.entities.menus.DataRequest;
import com.gigigo.orchextra.ocm.Ocm;
import com.gigigo.orchextra.ocm.OcmCallbacks;
import com.gigigo.orchextra.ocm.callbacks.OcmCredentialCallback;
import com.gigigo.orchextra.ocm.callbacks.OnChangedMenuCallback;
import com.gigigo.orchextra.ocm.callbacks.OnCustomSchemeReceiver;
import com.gigigo.orchextra.ocm.callbacks.OnLoadContentSectionFinishedCallback;
import com.gigigo.orchextra.ocm.callbacks.OnRequiredLoginCallback;
import com.gigigo.orchextra.ocm.customProperties.Disabled;
import com.gigigo.orchextra.ocm.customProperties.OcmCustomBehaviourDelegate;
import com.gigigo.orchextra.ocm.customProperties.ViewCustomizationType;
import com.gigigo.orchextra.ocm.customProperties.ViewLayer;
import com.gigigo.orchextra.ocm.customProperties.ViewType;
import com.gigigo.orchextra.ocm.dto.UiMenu;
import com.gigigo.orchextra.ocm.dto.UiMenuData;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {

  private TabLayout tabLayout;
  private ViewPager viewpager;
  private ScreenSlidePagerAdapter adapter;
  private View newContentMainContainer;
  private TabLayout.OnTabSelectedListener onTabSelectedListener =
      new TabLayout.OnTabSelectedListener() {
        @Override public void onTabSelected(TabLayout.Tab tab) {
          viewpager.setCurrentItem(tab.getPosition());
          ScreenSlidePageFragment frag =
              ((ScreenSlidePageFragment) adapter.getItem(viewpager.getCurrentItem()));
          frag.reloadSection(false);
        }

        @Override public void onTabUnselected(TabLayout.Tab tab) {
        }

        @Override public void onTabReselected(TabLayout.Tab tab) {
          viewpager.setCurrentItem(tab.getPosition());
          ((ScreenSlidePageFragment) adapter.getItem(viewpager.getCurrentItem())).reloadSection(
              false);
        }
      };

  private OnRequiredLoginCallback onDoRequiredLoginCallback = new OnRequiredLoginCallback() {
    @Override public void doRequiredLogin() {
      Toast.makeText(getApplicationContext(), "Item needs permissions", Toast.LENGTH_SHORT).show();
    }

    @Override public void doRequiredLogin(String elementUrl) {
      Toast.makeText(getApplicationContext(), "Item needs permissions" + elementUrl,
          Toast.LENGTH_SHORT).show();
    }
  };

  private OcmCustomBehaviourDelegate customPropertiesDelegate = new OcmCustomBehaviourDelegate() {

    @Override public void customizationForContent(@NotNull Map<String, ?> customProperties,
        @NotNull ViewType viewType, @NotNull
        Function1<? super List<? extends ViewCustomizationType>, Unit> onGetCustomization) {

      Handler handler = new Handler();
      handler.postDelayed(() -> {
        List<ViewCustomizationType> viewCustomizationType = new ArrayList<>();
        viewCustomizationType.add(new Disabled());

        if (customProperties.containsKey("requiredAuth") && customProperties.containsValue(
            "logged")) {
          View view = getLayoutInflater().inflate(R.layout.padlock_view, null);
          viewCustomizationType.add(new ViewLayer(view));
        }

        onGetCustomization.invoke(viewCustomizationType);
      }, 3000);
    }

    @Override public void contentNeedsValidation(@NotNull Map<String, ?> customProperties,
        @NotNull ViewType viewType, @NotNull Function1<? super Boolean, Unit> completion) {

      Set<? extends Map.Entry<String, ?>> entrySet = customProperties.entrySet();
      Iterator<? extends Map.Entry<String, ?>> iterator = entrySet.iterator();
      while (iterator.hasNext()) {
        Map.Entry<String, ?> next = iterator.next();
        String property = next.getKey();
        Object value = next.getValue();

        Handler handler = new Handler();
        switch (property) {
          case "requiredAuth":
            completion.invoke(true);
            handler.postDelayed(() -> {

              /*
              if(value.equals("logged")) {
                completion.invoke(false);
                Toast.makeText(MainActivity.this, "can't continue, requires authorization", Toast.LENGTH_SHORT).show();
              }
              else {
                completion.invoke(true);
              }
              */
            }, 3000);
            break;
        }
      }
    }
  };

  private List<UiMenu> oldUiMenuList;

  public static boolean deleteDir(File dir) {
    if (dir != null && dir.isDirectory()) {
      String[] children = dir.list();
      for (int i = 0; i < children.length; i++) {
        boolean success = deleteDir(new File(dir, children[i]));
        if (!success) {
          return false;
        }
      }
    }

    return dir.delete();
  }

  @Override protected void onResume() {
    super.onResume();

    Ocm.setOnChangedMenuCallback(new OnChangedMenuCallback() {
      @Override public void onChangedMenu(UiMenuData uiMenuData) {
        boolean menuHasChanged = checkIfMenuHasChanged(oldUiMenuList, uiMenuData.getUiMenuList());
        if (menuHasChanged) {
          showIconNewContent(uiMenuData.getUiMenuList());
        } else {
          adapter.reloadSections(viewpager.getCurrentItem());
        }
      }
    });
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    initViews();

    Ocm.setOnDoRequiredLoginCallback(onDoRequiredLoginCallback);
    Ocm.setCustomBehaviourDelegate(customPropertiesDelegate);

    startCredentials();
  }

  private void initViews() {
    tabLayout = findViewById(R.id.tabLayout);
    viewpager = findViewById(R.id.viewpager);
    //View fabReload = findViewById(R.id.fabReload);
    View fabSearch = findViewById(R.id.fabSearch);
    View fabClean = findViewById(R.id.fabClean);

    fabClean.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        //Orchextra.startScannerActivity();
        //Orchextra.startImageRecognition();
        adapter.setEmotion("happy");
      }
    });

    fabSearch.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        SearcherActivity.open(MainActivity.this);
      }
    });

    newContentMainContainer = findViewById(R.id.newContentMainContainer);

    adapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
    viewpager.setAdapter(adapter);
    viewpager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
  }
  //endregion

  private void startCredentials() {
    Ocm.startWithCredentials(BuildConfig.API_KEY, BuildConfig.API_SECRET,
        new OcmCredentialCallback() {
          @Override public void onCredentialReceiver(String accessToken) {
            //TODO Fix in Orchextra
            runOnUiThread(new Runnable() {
              @Override public void run() {
                getContent();
              }
            });
          }

          @Override public void onCredentailError(String code) {
            Snackbar.make(tabLayout,
                "No Internet Connection: " + code + "\n check Credentials-Enviroment",
                Snackbar.LENGTH_INDEFINITE).show();
          }
        });

    Ocm.setOnCustomSchemeReceiver(new OnCustomSchemeReceiver() {
      @Override public void onReceive(String customScheme) {
        // Toast.makeText(MainActivity.this, customScheme, Toast.LENGTH_SHORT).show();
        Orchextra.startScannerActivity();
      }
    });
    Ocm.start();//likewoah
  }

  private List<UiMenu> copy(List<UiMenu> list) {
    List<UiMenu> copyList = new ArrayList<>();
    copyList.addAll(list);
    return copyList;
  }

  private void getContent() {
    Ocm.setOnLoadDataContentSectionFinished(new OnLoadContentSectionFinishedCallback() {
      @Override public void onLoadContentSectionFinished() {
        Toast.makeText(MainActivity.this, "onLoadContentSectionFinished", Toast.LENGTH_SHORT).show();
      }
    });

    Ocm.updateContent(new OcmCallbacks.Menus() {
      @Override public void onMenusLoaded(UiMenuData menus, boolean hasChanged) {
        if(menus != null) {
          Toast.makeText(MainActivity.this, menus.toString(), Toast.LENGTH_SHORT).show();
          onGoDetailView(menus.getUiMenuList());
          if (hasChanged) {
            showIconNewContent(menus.getUiMenuList());
          }
        }
      }

      @Override public void onMenusFails(Throwable e) {
        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
      }
    });
  }

  private boolean checkIfMenuHasChanged(List<UiMenu> oldUiMenuList, List<UiMenu> newUiMenuList) {
    if (oldUiMenuList == null || newUiMenuList == null) {
      return false;
    }

    if (oldUiMenuList.size() != newUiMenuList.size()) {
      return true;
    } else {
      for (int i = 0; i < newUiMenuList.size(); i++) {
        if (!oldUiMenuList.get(i).getSlug().equals(newUiMenuList.get(i).getSlug())) {
          return true;
        }
      }
    }
    return false;
  }

  private void showIconNewContent(final List<UiMenu> newMenus) {
    newContentMainContainer.setVisibility(View.VISIBLE);
    newContentMainContainer.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        newContentMainContainer.setVisibility(View.GONE);

        tabLayout.removeAllTabs();
        onGoDetailView(newMenus);
      }
    });
  }

  private void onGoDetailView(List<UiMenu> uiMenu) {
    tabLayout.removeAllTabs();
    if (uiMenu.size() > 0) {
      for (int i = 0; i < uiMenu.size(); i++) {
        UiMenu menu = uiMenu.get(i);
        TabLayout.Tab tab = tabLayout.newTab().setText(menu.getText());
        tabLayout.addTab(tab);
      }
    }

    adapter.setDataItems(uiMenu);

    tabLayout.addOnTabSelectedListener(onTabSelectedListener);
  }
}
