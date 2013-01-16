package com.sap.core.odata.core.ep;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.sap.core.odata.api.edm.EdmEntitySet;
import com.sap.core.odata.api.edm.EdmFunctionImport;
import com.sap.core.odata.api.edm.EdmProperty;
import com.sap.core.odata.api.ep.EntityProviderException;
import com.sap.core.odata.api.ep.EntityProviderProperties;
import com.sap.core.odata.api.ep.ReadEntryResult;
import com.sap.core.odata.api.processor.ODataResponse;

/**
 * Interface for all none basic (content type <b>dependent</b>) provider methods.
 * 
 * @author SAP AG
 */
public interface ContentTypeBasedEntityProvider {

  ReadEntryResult readEntry(EdmEntitySet entitySet, InputStream content) throws EntityProviderException;

  Map<String, Object> readLink(EdmEntitySet entitySet, InputStream content) throws EntityProviderException;

  Map<String, Object> readProperty(EdmProperty edmProperty, InputStream content) throws EntityProviderException;

  List<Map<String, Object>> readLinks(EdmEntitySet entitySet, InputStream content) throws EntityProviderException;

  ODataResponse writeFeed(EdmEntitySet entitySet, List<Map<String, Object>> data, EntityProviderProperties properties) throws EntityProviderException;

  ODataResponse writeEntry(EdmEntitySet entitySet, Map<String, Object> data, EntityProviderProperties properties) throws EntityProviderException;

  ODataResponse writeProperty(EdmProperty edmProperty, Object value) throws EntityProviderException;

  ODataResponse writeLink(EdmEntitySet entitySet, Map<String, Object> data, EntityProviderProperties properties) throws EntityProviderException;

  ODataResponse writeLinks(EdmEntitySet entitySet, List<Map<String, Object>> data, EntityProviderProperties properties) throws EntityProviderException;

  ODataResponse writeFunctionImport(EdmFunctionImport functionImport, Object data, EntityProviderProperties properties) throws EntityProviderException;
}
