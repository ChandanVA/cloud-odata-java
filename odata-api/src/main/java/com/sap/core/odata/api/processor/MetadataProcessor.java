package com.sap.core.odata.api.processor;

import com.sap.core.odata.api.exception.ODataException;
import com.sap.core.odata.api.uri.info.GetMetadataUriInfo;

/**
 * Execute a OData metadata request. 
 * 
 * @author SAP AG
 */
public interface MetadataProcessor extends ODataProcessor {

  /**
   * @param contentType 
   * @return a {@link ODataResponse} object
   * @throws ODataException
   */
  ODataResponse readMetadata(GetMetadataUriInfo uriInfo, String contentType) throws ODataException;

}
