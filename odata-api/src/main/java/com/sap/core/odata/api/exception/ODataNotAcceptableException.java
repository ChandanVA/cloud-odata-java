/*******************************************************************************
 * Copyright 2013 SAP AG
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.sap.core.odata.api.exception;

import com.sap.core.odata.api.commons.HttpStatusCodes;

/**
 * Exceptions of this class will result in a HTTP status 406 not acceptable
 * @author SAP AG
 */
public class ODataNotAcceptableException extends ODataHttpException {

  private static final long serialVersionUID = 1L;

  public static final MessageReference COMMON = createMessageReference(ODataNotAcceptableException.class, "COMMON");
  public static final MessageReference NOT_SUPPORTED_CONTENT_TYPE = createMessageReference(ODataNotAcceptableException.class, "NOT_SUPPORTED_CONTENT_TYPE");
  public static final MessageReference NOT_SUPPORTED_ACCEPT_HEADER = createMessageReference(ODataNotAcceptableException.class, "NOT_SUPPORTED_ACCEPT_HEADER");

  public ODataNotAcceptableException(final MessageReference context) {
    super(context, HttpStatusCodes.NOT_ACCEPTABLE);
  }

  public ODataNotAcceptableException(final MessageReference context, final Throwable cause) {
    super(context, cause, HttpStatusCodes.NOT_ACCEPTABLE);
  }

  public ODataNotAcceptableException(final MessageReference context, final String errorCode) {
    super(context, HttpStatusCodes.NOT_ACCEPTABLE, errorCode);
  }

  public ODataNotAcceptableException(final MessageReference context, final Throwable cause, final String errorCode) {
    super(context, cause, HttpStatusCodes.NOT_ACCEPTABLE, errorCode);
  }
}
