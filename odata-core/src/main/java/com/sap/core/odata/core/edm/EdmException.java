package com.sap.core.odata.core.edm;

import com.sap.core.odata.core.exception.ODataError;

public class EdmException extends ODataError {

  public EdmException() {
    super();
  }

  public EdmException(String msg) {
    super(msg);
  }

  public EdmException(String msg, Throwable e) {
    super(msg, e);
  }

  public EdmException(Throwable e) {
    super(e);
  }

  private static final long serialVersionUID = 1L;

}
