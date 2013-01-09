package com.sap.core.odata.api.edm;

import java.io.InputStream;

import com.sap.core.odata.api.exception.ODataException;

/**
 * Objects of this class provide the service metadata
 * @author SAP AG
 *
 */
public interface EdmServiceMetadata {

  /**
   * @return {@link InputStream} containing the metadata document
   * @throws ODataException
   */
  InputStream getMetadata() throws ODataException;

  /**
   * @return <b>String</b> data service version of this service
   * @throws ODataException
   */
  String getDataServiceVersion() throws ODataException;
}