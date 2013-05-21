package com.sap.core.odata.core.doc;

import com.sap.core.odata.api.doc.Title;

/**
 * TitleImpl
 * <p>The implementiation of the interface Title
 * @author SAP AG
 */
public class TitleImpl implements Title {
  private String text;

  @Override
  public String getText() {
    return text;
  }

  public TitleImpl setText(final String text) {
    this.text = text;
    return this;
  }
}
