package com.olc.model;


public class LoopModel {

  private String title;

  private int resId;

  public LoopModel(String title, int resId) {
    this.title = title;
    this.resId = resId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public int getResId() {
    return resId;
  }

  public void setResId(int resId) {
    this.resId = resId;
  }
}
