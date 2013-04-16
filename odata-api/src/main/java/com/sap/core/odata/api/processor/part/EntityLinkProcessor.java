/**
 * (c) 2013 by SAP AG
 */
package com.sap.core.odata.api.processor.part;

import java.io.InputStream;

import com.sap.core.odata.api.exception.ODataException;
import com.sap.core.odata.api.processor.ODataProcessor;
import com.sap.core.odata.api.processor.ODataResponse;
import com.sap.core.odata.api.uri.info.DeleteUriInfo;
import com.sap.core.odata.api.uri.info.GetEntityLinkCountUriInfo;
import com.sap.core.odata.api.uri.info.GetEntityLinkUriInfo;
import com.sap.core.odata.api.uri.info.PutMergePatchUriInfo;

/**
 * Execute an OData entity link request. 
 * 
 * @author SAP AG
 */
public interface EntityLinkProcessor extends ODataProcessor {
  /**
   * Reads the URI of the target entity of a navigation property.
   * @param uriInfo information about the request URI
   * @param contentType the content type of the response
   * @return an {@link ODataResponse} object
   * @throws ODataException
   */
  ODataResponse readEntityLink(GetEntityLinkUriInfo uriInfo, String contentType) throws ODataException;

  /**
   * Returns whether the target entity of a navigation property exists.
   * @param uriInfo information about the request URI
   * @param contentType the content type of the response
   * @return an {@link ODataResponse} object
   * @throws ODataException
   */
  ODataResponse existsEntityLink(GetEntityLinkCountUriInfo uriInfo, String contentType) throws ODataException;

  /**
   * Updates the link to the target entity of a navigation property.
   * @param uriInfo information about the request URI
   * @param content the content of the request, containing the new URI
   * @param requestContentType the content type of the request body
   * @param contentType the content type of the response
   * @return an {@link ODataResponse} object
   * @throws ODataException
   */
  ODataResponse updateEntityLink(PutMergePatchUriInfo uriInfo, InputStream content, String requestContentType, String contentType) throws ODataException;

  /**
   * Deletes the link to the target entity of a navigation property.
   * @param uriInfo information about the request URI
   * @param contentType the content type of the response
   * @return an {@link ODataResponse} object
   * @throws ODataException
   */
  ODataResponse deleteEntityLink(DeleteUriInfo uriInfo, String contentType) throws ODataException;
}
