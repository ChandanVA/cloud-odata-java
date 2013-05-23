package com.sap.core.odata.core.servicedocument;

import com.sap.core.odata.api.servicedocument.Accept;
import com.sap.core.odata.api.servicedocument.CommonAttributes;

public class AcceptImpl implements Accept {
  private String value;
  private CommonAttributes commonAttributes;

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public CommonAttributes getCommonAttributes() {
    return commonAttributes;
  }

  public AcceptImpl setText(final String text) {
    value = text;
    return this;
  }

  public AcceptImpl setCommonAttributes(final CommonAttributes commonAttributes) {
    this.commonAttributes = commonAttributes;
    return this;
  }
}
