package com.sap.core.odata.api.processor.aspect;

import com.sap.core.odata.api.exception.ODataException;
import com.sap.core.odata.api.processor.ODataResponse;

/**
 * Execute a OData batch request. 
 * 
 * @author SAP AG
 *
 */
public interface Batch {

  /**
   * @return a {@link ODataResponse} object
   * @throws ODataException
   */
  ODataResponse executeBatch() throws ODataException;
}
