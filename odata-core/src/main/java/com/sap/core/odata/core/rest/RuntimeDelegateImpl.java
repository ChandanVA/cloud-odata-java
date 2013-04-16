package com.sap.core.odata.core.rest;

import com.sap.core.odata.api.edm.Edm;
import com.sap.core.odata.api.edm.EdmSimpleType;
import com.sap.core.odata.api.edm.EdmSimpleTypeKind;
import com.sap.core.odata.api.rest.ODataLocator;
import com.sap.core.odata.api.rest.ODataResponse.ODataResponseBuilder;
import com.sap.core.odata.api.rest.RuntimeDelegate;
import com.sap.core.odata.api.uri.UriParser;
import com.sap.core.odata.core.edm.simpletype.EdmBinary;
import com.sap.core.odata.core.edm.simpletype.EdmBit;
import com.sap.core.odata.core.edm.simpletype.EdmBoolean;
import com.sap.core.odata.core.edm.simpletype.EdmByte;
import com.sap.core.odata.core.edm.simpletype.EdmDateTime;
import com.sap.core.odata.core.edm.simpletype.EdmDateTimeOffset;
import com.sap.core.odata.core.edm.simpletype.EdmDecimal;
import com.sap.core.odata.core.edm.simpletype.EdmDouble;
import com.sap.core.odata.core.edm.simpletype.EdmGuid;
import com.sap.core.odata.core.edm.simpletype.EdmInt16;
import com.sap.core.odata.core.edm.simpletype.EdmInt32;
import com.sap.core.odata.core.edm.simpletype.EdmInt64;
import com.sap.core.odata.core.edm.simpletype.EdmSByte;
import com.sap.core.odata.core.edm.simpletype.EdmSingle;
import com.sap.core.odata.core.edm.simpletype.EdmString;
import com.sap.core.odata.core.edm.simpletype.EdmTime;
import com.sap.core.odata.core.edm.simpletype.EdmUint7;
import com.sap.core.odata.core.uri.UriParserImpl;

public class RuntimeDelegateImpl extends RuntimeDelegate {

  @Override
  public ODataResponseBuilder createODataResponseBuilder() {
    return new ODataResponseBuilderImpl();
  }

  @Override
  public Class<?> getExceptionMapper() {
    return ODataExceptionMapperImpl.class;
  }

  @Override
  public ODataLocator createODataLocator() {
    return new ODataLocatorImpl();
  }

  public EdmSimpleType getEdmSimpleType(EdmSimpleTypeKind edmSimpleType) {
    EdmSimpleType edmType = null;

    switch (edmSimpleType) {
    case Binary:
      edmType = new EdmBinary();
      break;
    case Boolean:
      edmType = new EdmBoolean();
      break;
    case Byte:
      edmType = new EdmByte();
      break;
    case DateTime:
      edmType = new EdmDateTime();
      break;
    case DateTimeOffset:
      edmType = new EdmDateTimeOffset();
      break;
    case Decimal:
      edmType = new EdmDecimal();
      break;
    case Double:
      edmType = new EdmDouble();
      break;
    case Guid:
      edmType = new EdmGuid();
      break;
    case Int16:
      edmType = new EdmInt16();
      break;
    case Int32:
      edmType = new EdmInt32();
      break;
    case Int64:
      edmType = new EdmInt64();
      break;
    case SByte:
      edmType = new EdmSByte();
      break;
    case Single:
      edmType = new EdmSingle();
      break;
    case String:
      edmType = new EdmString();
      break;
    case Time:
      edmType = new EdmTime();
      break;
    case Bit:
      edmType = new EdmBit();
      break;
    case UInt7:
      edmType = new EdmUint7();
      break;
    default:
      throw new RuntimeException("Invalid Type " + edmSimpleType);
    }

    return edmType;
  }

  @Override
  public UriParser getUriParser(Edm edm) {
    return new UriParserImpl(edm);
  }

}
