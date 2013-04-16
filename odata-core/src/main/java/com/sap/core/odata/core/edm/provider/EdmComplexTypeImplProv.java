/**
 * (c) 2013 by SAP AG
 */
package com.sap.core.odata.core.edm.provider;

import com.sap.core.odata.api.edm.EdmAnnotations;
import com.sap.core.odata.api.edm.EdmComplexType;
import com.sap.core.odata.api.edm.EdmException;
import com.sap.core.odata.api.edm.EdmTypeKind;
import com.sap.core.odata.api.edm.provider.ComplexType;

public class EdmComplexTypeImplProv extends EdmStructuralTypeImplProv implements EdmComplexType {

  public EdmComplexTypeImplProv(final EdmImplProv edm, final ComplexType complexType, final String namespace) throws EdmException {
    super(edm, complexType, EdmTypeKind.COMPLEX, namespace);
  }

  @Override
  public EdmComplexType getBaseType() throws EdmException {
    return (EdmComplexType) edmBaseType;
  }

  @Override
  public EdmAnnotations getAnnotations() throws EdmException {
    return new EdmAnnotationsImplProv(structuralType.getAnnotationAttributes(), structuralType.getAnnotationElements());
  }

}