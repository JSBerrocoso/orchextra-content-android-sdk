package com.gigigo.sample;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.gigigo.orchextra.Orchextra;
import com.gigigo.orchextra.ocm.OCManager;
import com.gigigo.orchextra.ocm.OCManagerCallbacks;
import com.gigigo.orchextra.ocm.Ocm;
import com.gigigo.orchextra.ocm.OcmCallbacks;
import com.gigigo.orchextra.ocm.callbacks.OcmCredentialCallback;
import com.gigigo.orchextra.ocm.callbacks.OnCustomSchemeReceiver;
import com.gigigo.orchextra.ocm.callbacks.OnRequiredLoginCallback;
import com.gigigo.orchextra.ocm.dto.UiMenu;
import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {

  private static final Boolean AUTO_INIT = true;
  static final String COUNTRY = "it";
  private TabLayout tabLayout;
  private View loadingView;
  private ViewPager viewpager;
  private ScreenSlidePagerAdapter adapter;
  private View newContentMainContainer;

  private TabLayout.OnTabSelectedListener onTabSelectedListener =
      new TabLayout.OnTabSelectedListener() {
        @Override public void onTabSelected(TabLayout.Tab tab) {
          viewpager.setCurrentItem(tab.getPosition());
          ScreenSlidePageFragment frag =
              ((ScreenSlidePageFragment) adapter.getItem(viewpager.getCurrentItem()));
          frag.reloadSection();
        }

        @Override public void onTabUnselected(TabLayout.Tab tab) {
        }

        @Override public void onTabReselected(TabLayout.Tab tab) {
          viewpager.setCurrentItem(tab.getPosition());
          ((ScreenSlidePageFragment) adapter.getItem(viewpager.getCurrentItem())).reloadSection();
        }
      };

  private OnRequiredLoginCallback onDoRequiredLoginCallback = new OnRequiredLoginCallback() {
    @Override public void doRequiredLogin() {
      Toast.makeText(getApplicationContext(), "Item needs permissions", Toast.LENGTH_SHORT).show();
    }
  };

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    initViews();
    showLoading();

    Ocm.setOnDoRequiredLoginCallback(onDoRequiredLoginCallback);

    if (AUTO_INIT) {
      startCredentials();
    }
  }

  @Override protected void onResume() {
    super.onResume();
    //ReadedArticles
    if (OCManager.getShowReadedArticlesInGrayScale() && adapter != null) {
      adapter.reloadSections();
      Toast.makeText(this, "Refresh grid from integratied app if readed articles are enabled"
          + OCManager.getShowReadedArticlesInGrayScale(), Toast.LENGTH_LONG).show();
    }
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    if (!AUTO_INIT) {
      getMenuInflater().inflate(R.menu.menu_main, menu);
    }
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {

    switch (item.getItemId()) {
      case R.id.action_init:
        startCredentials();
        return true;
      case R.id.action_refresh:
        adapter.reloadSections();
        return true;
      case R.id.action_clean:
        Toast.makeText(MainActivity.this, "Delete all data webStorage", Toast.LENGTH_LONG).show();
        clearDataAndGoToChangeCountryView();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void initViews() {
    tabLayout = (TabLayout) findViewById(R.id.tabLayout);
    viewpager = (ViewPager) findViewById(R.id.viewpager);
    loadingView = findViewById(R.id.loading_view);

    adapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
    viewpager.setAdapter(adapter);
    viewpager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
  }

  private void startCredentials() {
    Ocm.setBusinessUnit(COUNTRY);
    Ocm.startWithCredentials(App.API_KEY, App.API_SECRET, new OcmCredentialCallback() {
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
        hideLoading();
      }
    });

    Ocm.setOnCustomSchemeReceiver(new OnCustomSchemeReceiver() {
      @Override public void onReceive(String customScheme) {
        Toast.makeText(MainActivity.this, customScheme, Toast.LENGTH_SHORT).show();
        Orchextra.startScannerActivity();
      }
    });
    Ocm.start();//likewoah
  }

  //region clear all data
  private void clearDataAndGoToChangeCountryView() {
    clearApplicationData();
    Orchextra.stop(); //asv V.I.Code
    Ocm.clearData(true, true, new OCManagerCallbacks.Clear() {
      @Override public void onDataClearedSuccessfull() {

      }

      @Override public void onDataClearFails(Exception e) {

      }
    });
  }

  public void clearApplicationData() {
    File cache = getCacheDir();
    File appDir = new File(cache.getParent());
    if (appDir.exists()) {
      String[] children = appDir.list();
      for (String s : children) {
        if (!s.equals("lib")) {
          deleteDir(new File(appDir, s));
        }
      }
    }
  }

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
  //endregion

  private void getContent() {
    Ocm.getMenus(false, new OcmCallbacks.Menus() {
      @Override public void onMenusLoaded(List<UiMenu> uiMenu) {
        if (uiMenu == null) {
          Toast.makeText(MainActivity.this, "menu is null", Toast.LENGTH_SHORT).show();
        } else {
          hideLoading();
          viewpager.setOffscreenPageLimit(uiMenu.size());
          onGoDetailView(uiMenu);
          adapter.setDataItems(uiMenu);
          checkIfMenuHasChanged(uiMenu);
        }
      }

      @Override public void onMenusFails(Throwable e) {
        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
      }
    });
  }

  private void checkIfMenuHasChanged(final List<UiMenu> oldMenus) {
    Ocm.getMenus(true, new OcmCallbacks.Menus() {
      @Override public void onMenusLoaded(List<UiMenu> newMenus) {
        if (oldMenus == null || newMenus == null) {
          return;
        }
        if (oldMenus.size() != newMenus.size()) {
          showIconNewContent(newMenus);
        } else {
          for (int i = 0; i < newMenus.size(); i++) {
            if (oldMenus.get(i).getUpdateAt() != newMenus.get(i).getUpdateAt()) {
              showIconNewContent(newMenus);
              return;
            }
          }
        }
      }

      @Override public void onMenusFails(Throwable e) {
      }
    });
  }

  private void showIconNewContent(final List<UiMenu> newMenus) {
    newContentMainContainer.setVisibility(View.VISIBLE);
    newContentMainContainer.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        newContentMainContainer.setVisibility(View.GONE);

        adapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        hideLoading();
        adapter.setDataItems(newMenus);
        viewpager.removeAllViews();
        viewpager.setAdapter(adapter);

        tabLayout.removeAllTabs();
        onGoDetailView(newMenus);
      }
    });
  }

  private void onGoDetailView(List<UiMenu> uiMenu) {
    if (uiMenu.size() > 0) {
      for (int i = 0; i < uiMenu.size(); i++) {
        UiMenu menu = uiMenu.get(i);
        TabLayout.Tab tab = tabLayout.newTab().setText(menu.getText());
        tabLayout.addTab(tab);
      }
    }

    tabLayout.addOnTabSelectedListener(onTabSelectedListener);
  }

  private void showLoading() {
    loadingView.setVisibility(View.VISIBLE);
    viewpager.setVisibility(View.GONE);
  }

  private void hideLoading() {
    loadingView.setVisibility(View.GONE);
    viewpager.setVisibility(View.VISIBLE);
  }
}
