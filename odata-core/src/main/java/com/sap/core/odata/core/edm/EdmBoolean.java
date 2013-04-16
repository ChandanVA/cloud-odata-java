package com.sap.core.odata.core.edm;

import com.sap.core.odata.api.edm.EdmException;
import com.sap.core.odata.api.edm.EdmFacets;
import com.sap.core.odata.api.edm.EdmLiteralKind;
import com.sap.core.odata.api.edm.EdmSimpleType;
import com.sap.core.odata.api.edm.EdmSimpleTypeFacade;
import com.sap.core.odata.api.edm.EdmSimpleTypeKind;
import com.sap.core.odata.api.edm.EdmTypeKind;

public class EdmBoolean implements EdmSimpleType {

  private static final EdmBoolean instance = new EdmBoolean();

  private EdmBoolean() {

  }

  public static EdmBoolean getInstance() {
    return instance;
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj || obj instanceof EdmBoolean;
  }

  @Override
  public String getNamespace() throws EdmException {
    return EdmSimpleTypeFacade.edmNamespace;
  }

  @Override
  public EdmTypeKind getKind() {
    return EdmTypeKind.SIMPLE;
  }

  @Override
  public String getName() throws EdmException {
    return EdmSimpleTypeKind.Boolean.toString();
  }

  @Override
  public boolean isCompatible(final EdmSimpleType simpleType) {
    return simpleType instanceof Bit || simpleType instanceof EdmBoolean;
  }

  @Override
  public boolean validate(final String value, final EdmLiteralKind literalKind, final EdmFacets facets) {
    if (value == null)
      return facets == null || facets.isNullable() == null || facets.isNullable();
    else
      return "true".equals(value) || "1".equals(value)
          || "false".equals(value) || "0".equals(value);
  }

  @Override
  public Object valueOfString(final String value, final EdmLiteralKind literalKind, final EdmFacets facets) {
    if (validate(value, literalKind, facets))
      if (value == null)
        return null;
      else
        return "true".equals(value) || "1".equals(value);
    else
      throw new IllegalArgumentException();
  }

  @Override
  public String valueToString(final Object value, final EdmLiteralKind literalKind, final EdmFacets facets) {
    if (value == null)
      if (facets == null || facets.isNullable() == null || facets.isNullable())
        return null;
      else
        throw new IllegalArgumentException();
    else if (value instanceof Boolean)
      return (Boolean) value ? "true" : "false";
    else
      throw new IllegalArgumentException();
  }

  @Override
  public String toUriLiteral(final String literal) {
    return literal;
  }
}
