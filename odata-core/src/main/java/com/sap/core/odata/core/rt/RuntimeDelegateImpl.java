package com.sap.core.odata.core.rt;

import com.sap.core.odata.api.edm.Edm;
import com.sap.core.odata.api.edm.EdmEntityType;
import com.sap.core.odata.api.edm.EdmSimpleType;
import com.sap.core.odata.api.edm.EdmSimpleTypeFacade;
import com.sap.core.odata.api.edm.EdmSimpleTypeKind;
import com.sap.core.odata.api.edm.provider.EdmProvider;
import com.sap.core.odata.api.ep.EntityProvider;
import com.sap.core.odata.api.ep.EntityProviderException;
import com.sap.core.odata.api.processor.ODataResponse.ODataResponseBuilder;
import com.sap.core.odata.api.rt.RuntimeDelegate.RuntimeDelegateInstance;
import com.sap.core.odata.api.uri.UriParser;
import com.sap.core.odata.api.uri.expression.FilterParser;
import com.sap.core.odata.api.uri.expression.OrderByParser;
import com.sap.core.odata.core.ODataResponseImpl;
import com.sap.core.odata.core.edm.EdmSimpleTypeFacadeImpl;
import com.sap.core.odata.core.edm.provider.EdmImplProv;
import com.sap.core.odata.core.enums.ContentType;
import com.sap.core.odata.core.ep.EntityProviderFactory;
import com.sap.core.odata.core.uri.UriParserImpl;
import com.sap.core.odata.core.uri.expression.FilterParserImpl;
import com.sap.core.odata.core.uri.expression.OrderByParserImpl;

public class RuntimeDelegateImpl extends RuntimeDelegateInstance {

  @Override
  protected ODataResponseBuilder createODataResponseBuilder() {
    ODataResponseImpl r = new ODataResponseImpl();
    return r.new ODataResponseBuilderImpl();
  }

  protected EdmSimpleType getEdmSimpleType(EdmSimpleTypeKind edmSimpleType) {
    return EdmSimpleTypeFacadeImpl.getEdmSimpleType(edmSimpleType);
  }

  @Override
  protected UriParser getUriParser(Edm edm) {
    return new UriParserImpl(edm);
  }

  @Override
  protected EdmSimpleType getInternalEdmSimpleTypeByString(String edmSimpleType) {
    return EdmSimpleTypeFacadeImpl.getInternalEdmSimpleTypeByString(edmSimpleType);
  }

  @Override
  protected EdmSimpleTypeFacade getSimpleTypeFacade() {
    return new EdmSimpleTypeFacadeImpl();
  }

  @Override
  protected Edm createEdm(EdmProvider provider) {
    return new EdmImplProv(provider);
  }

  @Override
  protected EntityProvider createSerializer(String contentType) throws EntityProviderException {
    return EntityProviderFactory.create(contentType);
  }

  @Override
  protected FilterParser getFilterParser(Edm edm, EdmEntityType edmType) {
    return new FilterParserImpl(edm, edmType);
  }

  @Override
  protected OrderByParser getOrderByParser(Edm edm, EdmEntityType edmType) {
    return new OrderByParserImpl(edm, edmType);
  }

}
