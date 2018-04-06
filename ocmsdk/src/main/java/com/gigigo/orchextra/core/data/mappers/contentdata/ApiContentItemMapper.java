package com.gigigo.orchextra.core.data.mappers.contentdata;

import android.util.Log;
import com.gigigo.ggglib.mappers.ExternalClassToModelMapper;
import com.gigigo.orchextra.core.data.api.dto.content.ApiContentItem;
import com.gigigo.orchextra.core.data.api.dto.elements.ApiElement;
import com.gigigo.orchextra.core.data.mappers.DbMappersKt;
import com.gigigo.orchextra.core.domain.entities.contentdata.ContentItem;
import com.gigigo.orchextra.core.domain.entities.elements.Element;
import com.gigigo.orchextra.core.sdk.utils.DateUtils;
import java.util.ArrayList;
import java.util.List;

public class ApiContentItemMapper
    implements ExternalClassToModelMapper<ApiContentItem, ContentItem> {

  private final ApiContentItemLayoutMapper apiContentItemLayoutMapper;

  public ApiContentItemMapper(ApiContentItemLayoutMapper apiContentItemLayoutMapper) {
    this.apiContentItemLayoutMapper = apiContentItemLayoutMapper;
  }

  @Override public ContentItem externalClassToModel(ApiContentItem data) {
    final long time = System.currentTimeMillis();

    ContentItem model = new ContentItem();
    model.setSlug(data.getSlug());
    model.setType(data.getType());

    List<String> tagList = new ArrayList<>();
    if (data.getTags() != null) {
      for (String tag : data.getTags()) {
        tagList.add(tag);
      }
    }
    model.setTags(tagList);

    model.setLayout(apiContentItemLayoutMapper.externalClassToModel(data.getLayout()));

    List<Element> elementList = new ArrayList<>();
    if (data.getElements() != null) {
      for (ApiElement apiElement : data.getElements()) {

        if (DateUtils.isBetweenTwoDates(apiElement.getDates())) {
          Element element = DbMappersKt.toElement(apiElement); //apiElementMapper.externalClassToModel(apiElement);
          if (element != null) {
            elementList.add(element);
          }
        }
      }
    }
    model.setElements(elementList);
    Log.v("TT - ApiContentItem", (System.currentTimeMillis() - time) / 1000 + "");

    return model;
  }
}
