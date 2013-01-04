package com.sap.core.odata.api.processor.feature;

import com.sap.core.odata.api.exception.ODataException;
import com.sap.core.odata.api.processor.ODataResponse;
import com.sap.core.odata.api.uri.info.GetMetadataUriInfo;

/**
 * Execute a OData metadata request. 
 * 
 * @author SAP AG
 *
 */
public interface Metadata {

  /**
   * @param contentType 
   * @return a {@link ODataResponse} object
   * @throws ODataException
   */
  ODataResponse readMetadata(GetMetadataUriInfo uriParserResultView, String contentType) throws ODataException;

}
