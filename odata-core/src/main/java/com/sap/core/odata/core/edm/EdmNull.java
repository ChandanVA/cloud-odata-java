/**
 * (c) 2013 by SAP AG
 */
package com.sap.core.odata.core.edm;

import com.sap.core.odata.api.edm.EdmFacets;
import com.sap.core.odata.api.edm.EdmLiteralKind;
import com.sap.core.odata.api.edm.EdmSimpleTypeException;

/**
 * Implementation of the simple type Null
 * @author SAP AG
 */
public class EdmNull extends AbstractSimpleType {

  private static final EdmNull instance = new EdmNull();

  public static EdmNull getInstance() {
    return instance;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj == null;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public Class<?> getDefaultType() {
    return null;
  }

  @Override
  public <T> T valueOfString(final String value, final EdmLiteralKind literalKind, final EdmFacets facets, final Class<T> returnType) throws EdmSimpleTypeException {
    return null;
  }

  @Override
  public String valueToString(final Object value, final EdmLiteralKind literalKind, final EdmFacets facets) throws EdmSimpleTypeException {
    return null;
  }

  @Override
  public String toUriLiteral(final String literal) {
    return "null";
  }
}
