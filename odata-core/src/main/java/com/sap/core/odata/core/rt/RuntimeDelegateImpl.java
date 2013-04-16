package com.sap.core.odata.core.rt;

import com.sap.core.odata.api.edm.Edm;
import com.sap.core.odata.api.edm.EdmSimpleType;
import com.sap.core.odata.api.edm.EdmSimpleTypeFacade;
import com.sap.core.odata.api.edm.EdmSimpleTypeKind;
import com.sap.core.odata.api.edm.EdmType;
import com.sap.core.odata.api.edm.provider.EdmProvider;
import com.sap.core.odata.api.enums.Format;
import com.sap.core.odata.api.processor.ODataContext;
import com.sap.core.odata.api.processor.ODataResponse.ODataResponseBuilder;
import com.sap.core.odata.api.rt.RuntimeDelegate.RuntimeDelegateInstance;
import com.sap.core.odata.api.serialization.ODataSerializationException;
import com.sap.core.odata.api.serialization.ODataSerializer;
import com.sap.core.odata.api.uri.UriParser;
import com.sap.core.odata.api.uri.expression.FilterParser;
import com.sap.core.odata.core.ODataResponseBuilderImpl;
import com.sap.core.odata.core.edm.EdmSimpleTypeFacadeImpl;
import com.sap.core.odata.core.edm.provider.EdmImplProv;
import com.sap.core.odata.core.serializer.ODataSerializerFactory;
import com.sap.core.odata.core.uri.UriParserImpl;
import com.sap.core.odata.core.uri.expression.FilterParserImpl;

public class RuntimeDelegateImpl extends RuntimeDelegateInstance {

  @Override
  protected ODataResponseBuilder createODataResponseBuilder() {
    return new ODataResponseBuilderImpl();
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
  protected ODataSerializer createSerializer(Format format, ODataContext ctx) throws ODataSerializationException {
    return ODataSerializerFactory.create(format, ctx);
  }

  @Override
  protected FilterParser getFilterParser(Edm edm, EdmType edmType) {
    return new FilterParserImpl(edm, edmType);
  }

}
