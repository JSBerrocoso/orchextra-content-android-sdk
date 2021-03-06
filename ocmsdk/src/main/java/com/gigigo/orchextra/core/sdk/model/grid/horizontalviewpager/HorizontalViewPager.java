package com.gigigo.orchextra.core.sdk.model.grid.horizontalviewpager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.gigigo.multiplegridrecyclerview.entities.Cell;
import com.gigigo.orchextra.ocm.views.UiListedBaseContentData;
import com.gigigo.orchextra.ocmsdk.R;
import java.util.List;

public class HorizontalViewPager extends UiListedBaseContentData {

  private ViewPager listedHorizontalViewPager;
  private HorizontalViewPagerAdapter adapter;
  private List<Cell> cellDataList;
  private FragmentManager fragmentManager;

  public static HorizontalViewPager newInstance() {
    return new HorizontalViewPager();
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    fragmentManager = getActivity().getSupportFragmentManager();
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.view_horizontal_viewpager_item, container, false);

    initViews(view);
    initViewPager();

    return view;
  }

  private void initViews(View view) {
    listedHorizontalViewPager = (ViewPager) view.findViewById(R.id.listedHorizontalViewPager);
  }

  private void initViewPager() {
    adapter = new HorizontalViewPagerAdapter(fragmentManager, imageLoader, listedContentListener);
    listedHorizontalViewPager.setAdapter(adapter);
  }

  @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    adapter.setItems(cellDataList);
  }

  @Override public void setData(List<Cell> cellDataList) {
    this.cellDataList = cellDataList;
    if (listedHorizontalViewPager != null) {
      adapter.setItems(cellDataList);
    }
  }

  @Override public void scrollToTop() {

  }

  @Override public void showErrorView() {
    listedHorizontalViewPager.setVisibility(View.GONE);
    errorView.setVisibility(View.VISIBLE);
  }

  @Override public void showEmptyView() {
    listedHorizontalViewPager.setVisibility(View.GONE);
    emptyView.setVisibility(View.VISIBLE);
  }

  @Override public void showProgressView(boolean isVisible) {
    loadingView.setVisibility(isVisible ? View.VISIBLE : View.GONE);
  }
}
