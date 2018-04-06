package com.gigigo.orchextra.core.sdk.model.grid.verticalviewpager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import com.gigigo.multiplegridrecyclerview.entities.Cell;
import com.gigigo.orchextra.core.controller.dto.CellCarouselContentData;
import com.gigigo.orchextra.ocm.views.UiListedBaseContentData;
import java.util.List;

public class VerticalViewPagerAdapter extends FragmentStatePagerAdapter {

  private final UiListedBaseContentData.ListedContentListener listedContentListener;
  private int mLoops = 1;
  private List<Cell> cellDataList;

  public VerticalViewPagerAdapter(FragmentManager fm,
      UiListedBaseContentData.ListedContentListener listedContentListener) {
    super(fm);
    this.listedContentListener = listedContentListener;
  }

  @Override public Fragment getItem(int position) {
    int realSize = 1;
    if (getCount() > 0) realSize = getCount() / mLoops;

    if (mLoops > 1) position = position % realSize;
    final int finalPosition = position;

    VerticalItemPageFragment verticalItemPageFragment = VerticalItemPageFragment.newInstance();
    verticalItemPageFragment.setOnClickHorizontalItem(view -> {
      if (listedContentListener != null) {
        listedContentListener.onItemClicked(finalPosition, view);
      }
    });

    if (cellDataList.get(finalPosition) instanceof CellCarouselContentData) {
      CellCarouselContentData cell = (CellCarouselContentData) cellDataList.get(finalPosition);
      verticalItemPageFragment.setCell(cell);
    }

    return verticalItemPageFragment;
  }

  @Override public int getCount() {
    return cellDataList != null ? cellDataList.size() * mLoops : 0;
  }

  public void setItems(List<Cell> cellDataList) {
    this.cellDataList = cellDataList;
    notifyDataSetChanged();
  }

  public void setLoops(int mLoops) {
    this.mLoops = mLoops;
    notifyDataSetChanged();
  }
}
