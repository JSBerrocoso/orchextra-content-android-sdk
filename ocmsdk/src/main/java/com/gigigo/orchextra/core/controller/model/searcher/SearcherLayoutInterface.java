package com.gigigo.orchextra.core.controller.model.searcher;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.gigigo.multiplegridrecyclerview.entities.Cell;
import com.gigigo.orchextra.core.sdk.OcmSchemeHandler;
import java.util.List;

public interface SearcherLayoutInterface {

  void initUi();

  void setData(List<Cell> cellGridContentDataList);

  void showProgressView(boolean isVisible);

  void showEmptyView(boolean isVisible);

  void navigateToDetailView(String elementUrl, View view, AppCompatActivity activity, OcmSchemeHandler.ProcessElementCallback processElementCallback);
}
