package com.gigigo.orchextra.core.sdk.model.detail.viewtypes.cards;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.gigigo.orchextra.core.controller.views.UiBaseContentData;
import com.gigigo.orchextra.core.domain.entities.elementcache.ElementCache;
import com.gigigo.orchextra.ocmsdk.R;

public class CardContentData extends UiBaseContentData {

  private CardItemRecyclerViewContainer cardRecyclerViewContainer;
  private ElementCache elements;

  public static CardContentData newInstance() {
    return new CardContentData();
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.view_cards_elements_item, container, false);

    initViews(view);

    return view;
  }

  private void initViews(View view) {
    cardRecyclerViewContainer =
        (CardItemRecyclerViewContainer) view.findViewById(R.id.cardRecyclerViewContainer);
  }

  @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    init();
  }

  private void init() {
    cardRecyclerViewContainer.addCards(elements);
    cardRecyclerViewContainer.initialize();
  }

  public void addItems(ElementCache elements) {
    this.elements = elements;
  }
}
