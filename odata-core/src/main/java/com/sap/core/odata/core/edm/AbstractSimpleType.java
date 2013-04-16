package com.sap.core.odata.core.edm;

import com.sap.core.odata.api.edm.EdmException;
import com.sap.core.odata.api.edm.EdmFacets;
import com.sap.core.odata.api.edm.EdmLiteralKind;
import com.sap.core.odata.api.edm.EdmSimpleType;
import com.sap.core.odata.api.edm.EdmSimpleTypeException;
import com.sap.core.odata.api.edm.EdmTypeKind;

/**
 * Abstract implementation of the EDM simple-type interface
 * @author SAP AG
 */
public abstract class AbstractSimpleType implements EdmSimpleType {

  @Override
  public boolean equals(final Object obj) {
    return this == obj || getClass().equals(obj.getClass());
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public String getNamespace() throws EdmException {
    return EDM_NAMESPACE;
  }

  @Override
  public EdmTypeKind getKind() {
    return EdmTypeKind.SIMPLE;
  }

  @Override
  public String getName() throws EdmException {
    final String name = getClass().getSimpleName();
    if (name.startsWith(EDM_NAMESPACE))
      return name.substring(3);
    else
      return name;
  }

  @Override
  public boolean isCompatible(final EdmSimpleType simpleType) {
    return equals(simpleType);
  }

  @Override
  public boolean validate(final String value, final EdmLiteralKind literalKind, final EdmFacets facets) {
    try {
      valueOfString(value, literalKind, facets);
      return true;
    } catch (EdmSimpleTypeException e) {
      return false;
    }
  }

  @Override
  public Object valueOfString(final String value, final EdmLiteralKind literalKind, final EdmFacets facets) throws EdmSimpleTypeException {
    return null;
  }

  @Override
  public String valueToString(final Object value, final EdmLiteralKind literalKind, final EdmFacets facets) throws EdmSimpleTypeException {
    return null;
  }

  @Override
  public String toUriLiteral(final String literal) throws EdmSimpleTypeException {
    return literal;
  }

  protected static <T> T getCheckedNullValue(final EdmFacets facets) throws EdmSimpleTypeException {
    if (facets == null || facets.isNullable() == null || facets.isNullable())
      return null;
    else
      throw new EdmSimpleTypeException(EdmSimpleTypeException.LITERAL_NULL_NOT_ALLOWED);
  }

  protected static String getNullOrDefaultValue(final EdmFacets facets) throws EdmSimpleTypeException {
    if (facets == null)
      return null;
    else if (facets.getDefaultValue() == null)
      if (facets.isNullable() == null || facets.isNullable())
        return null;
      else
        throw new EdmSimpleTypeException(EdmSimpleTypeException.VALUE_NULL_NOT_ALLOWED);
    else
      return facets.getDefaultValue();
  }
}
