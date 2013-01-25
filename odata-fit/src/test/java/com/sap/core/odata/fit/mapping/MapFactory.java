package com.sap.core.odata.fit.mapping;

import com.sap.core.odata.api.ODataService;
import com.sap.core.odata.api.ODataServiceFactory;
import com.sap.core.odata.api.exception.ODataException;
import com.sap.core.odata.api.processor.ODataContext;

public class MapFactory extends ODataServiceFactory {

  @Override
  public ODataService createService(ODataContext ctx) throws ODataException {
    MapProvider provider = new MapProvider();
    MapProcessor processor = new MapProcessor();

    return createODataSingleProcessorService(provider, processor);
  }

  public static ODataService create() throws ODataException {
    return new MapFactory().createService(null);
  }

}
