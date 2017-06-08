package com.gigigo.orchextra.core.data.rxCache.imageCache;

/**
 * Created by francisco.hernandez on 6/6/17.
 */

class ImageData {

  private final String path;
  private final int priority;

  public ImageData(String path, int priority) {
    this.path = path;
    this.priority = priority;
  }

  public String getPath() {
    return path;
  }

  public int getPriority() {
    return priority;
  }
}