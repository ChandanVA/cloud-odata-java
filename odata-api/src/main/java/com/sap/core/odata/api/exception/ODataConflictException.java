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
 * Exceptions of this class will result in a HTTP status 409 Conflict
 * @author SAP AG
 */
public class ODataConflictException extends ODataHttpException {

  private static final long serialVersionUID = 1L;

  public static final MessageReference COMMON = createMessageReference(ODataConflictException.class, "COMMON");

  public ODataConflictException(final MessageReference context) {
    super(context, HttpStatusCodes.CONFLICT);
  }

  public ODataConflictException(final MessageReference context, final Throwable cause) {
    super(context, cause, HttpStatusCodes.CONFLICT);
  }

  public ODataConflictException(final MessageReference context, final String errorCode) {
    super(context, HttpStatusCodes.CONFLICT, errorCode);
  }

  public ODataConflictException(final MessageReference context, final Throwable cause, final String errorCode) {
    super(context, cause, HttpStatusCodes.CONFLICT, errorCode);
  }

}
