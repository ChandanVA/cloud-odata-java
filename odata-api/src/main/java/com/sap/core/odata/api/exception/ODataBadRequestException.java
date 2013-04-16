package com.sap.core.odata.api.exception;

import com.sap.core.odata.api.commons.HttpStatusCodes;

/**
 * @author SAP AG
 */
public class ODataBadRequestException extends ODataHttpException {

  private static final long serialVersionUID = 1L;

  public static final MessageReference COMMON = createMessageReference(ODataBadRequestException.class, "COMMON");
  public static final MessageReference NOTSUPPORTED = createMessageReference(ODataBadRequestException.class, "NOTSUPPORTED");
  public static final MessageReference URLTOOSHORT = createMessageReference(ODataBadRequestException.class, "URLTOOSHORT");
  public static final MessageReference VERSIONERROR = createMessageReference(ODataBadRequestException.class, "VERSIONERROR");
  public static final MessageReference PARSEVERSIONERROR = createMessageReference(ODataBadRequestException.class, "PARSEVERSIONERROR");
  public static final MessageReference BODY = createMessageReference(ODataBadRequestException.class, "BODY");
  public static final MessageReference AMBIGIOUS_XMETHOD = createMessageReference(ODataBadRequestException.class, "AMBIGIOUS_XMETHOD");

  public ODataBadRequestException(MessageReference messageReference) {
    super(messageReference, HttpStatusCodes.BAD_REQUEST);
  }

  public ODataBadRequestException(MessageReference messageReference, Throwable cause) {
    super(messageReference, cause, HttpStatusCodes.BAD_REQUEST);
  }
}
