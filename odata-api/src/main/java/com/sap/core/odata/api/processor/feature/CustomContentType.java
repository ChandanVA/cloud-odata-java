package com.sap.core.odata.api.processor.feature;

import java.util.List;

import com.sap.core.odata.api.exception.ODataException;
import com.sap.core.odata.api.processor.ODataProcessor;

/**
 * Data processor feature if processor supports custom content types. By default the OData library supports
 * various types like Json (application/json), Atom (application/xml+atom) and XML (application/xml). But
 * the OData specification allows also other types like e.g. CSV or plain text.  
 * 
 * @author SAP AG
 */
public interface CustomContentType extends ODataProcessorFeature {

  /**
   * The OData library will consider these additional content types during negotiation of http content type header.
   * @param processorFeature
   * @return a list of additional supported content types in the format "type/sub type"
   * @throws ODataException
   */
  public List<String> getCustomContentTypes(Class<? extends ODataProcessor> processorFeature) throws ODataException;
}
