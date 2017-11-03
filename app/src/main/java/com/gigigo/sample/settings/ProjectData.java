package com.gigigo.sample.settings;

import android.support.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;

public final class ProjectData {

  @NonNull private final String name;
  @NonNull private final String apiKey;
  @NonNull private final String apiSecret;

  public ProjectData(@NonNull String name, @NonNull String apiKey, @NonNull String apiSecret) {
    this.name = name;
    this.apiKey = apiKey;
    this.apiSecret = apiSecret;
  }

  @NonNull public String getName() {
    return name;
  }

  @NonNull public String getApiKey() {
    return apiKey;
  }

  @NonNull public String getApiSecret() {
    return apiSecret;
  }

  @NonNull public static List<ProjectData> getDefaultProjectDataList() {

    List<ProjectData> projectDataList = new ArrayList<>();

    projectDataList.add(new ProjectData("Default", "33ecdcbe03d60cb530e6ae13a531a3c9cf3c150e",
        "be772ab61e2571230c596aa95237cc618023befb"));

    projectDataList.add(
        new ProjectData("[PRO][ES] ORCHEXTRA DEMO", "9d9f74d0a9b293a2ea1a7263f47e01baed2cb0f3",
            "6a4d8072f2a519c67b0124656ce6cb857a55276a"));

    projectDataList.add(
        new ProjectData("[UAT] WOAH MARKETS", "ef08c4dccb7649b9956296a863db002a68240be2",
            "6bc18c500546f253699f61c11a62827679178400"));

    projectDataList.add(
        new ProjectData("ANDROID SDK - {{staging}}", "34a4654b9804eab82aae05b2a5f949eb2a9f412c",
            "2d5bce79e3e6e9cabf6d7b040d84519197dc22f3"));

    return projectDataList;
  }
}
