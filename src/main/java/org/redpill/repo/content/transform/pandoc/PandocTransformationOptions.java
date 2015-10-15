package org.redpill.repo.content.transform.pandoc;

import org.alfresco.service.cmr.repository.TransformationOptions;

public class PandocTransformationOptions extends TransformationOptions {

  public static final String DEFAULT_MARGIN_TOP = "1.5cm";
  public static final String DEFAULT_MARGIN_BOTTOM = "1.5cm";
  public static final String DEFAULT_MARGIN_LEFT = "2.0cm";
  public static final String DEFAULT_MARGIN_RIGHT = "2.0cm";
  public static final String DEFAULT_PAPER_SIZE = "a4paper";

  private String _marginTop = DEFAULT_MARGIN_TOP;

  private String _marginBottom = DEFAULT_MARGIN_BOTTOM;

  private String _marginRight = DEFAULT_MARGIN_RIGHT;

  private String _marginLeft = DEFAULT_MARGIN_LEFT;

  private String _paperSize = DEFAULT_PAPER_SIZE;

  public String getMarginTop() {
    return _marginTop;
  }

  public void setMarginTop(String marginTop) {
    _marginTop = marginTop;
  }

  public String getMarginBottom() {
    return _marginBottom;
  }

  public void setMarginBottom(String marginBottom) {
    _marginBottom = marginBottom;
  }

  public String getMarginRight() {
    return _marginRight;
  }

  public void setMarginRight(String marginRight) {
    _marginRight = marginRight;
  }

  public String getMarginLeft() {
    return _marginLeft;
  }

  public void setMarginLeft(String marginLeft) {
    _marginLeft = marginLeft;
  }

  public String getPaperSize() {
    return _paperSize;
  }

  public void setPaperSize(String paperSize) {
    _paperSize = paperSize;
  }

}
