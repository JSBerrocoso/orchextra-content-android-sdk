package com.gigigo.orchextra.core.domain.entities.contentdata;

import java.util.List;

public class ContentItemLayout {

  private String name;
  private ContentItemTypeLayout type;
  private List<ContentItemPattern> pattern;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<ContentItemPattern> getPattern() {
    return pattern;
  }

  public void setPattern(List<ContentItemPattern> pattern) {
    this.pattern = pattern;
  }

  public ContentItemTypeLayout getType() {
    return type;
  }

  public void setType(ContentItemTypeLayout type) {
    this.type = type;
  }
}
