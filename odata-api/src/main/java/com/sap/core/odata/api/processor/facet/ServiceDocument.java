package com.sap.core.odata.api.processor.facet;

import com.sap.core.odata.api.exception.ODataError;
import com.sap.core.odata.api.rest.ODataResponse;

public interface ServiceDocument {

  ODataResponse readServiceDocument() throws ODataError;

}
