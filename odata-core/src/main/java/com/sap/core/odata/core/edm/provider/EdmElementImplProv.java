package com.sap.core.odata.core.edm.provider;

import com.sap.core.odata.api.edm.EdmElement;
import com.sap.core.odata.api.edm.EdmException;
import com.sap.core.odata.api.edm.EdmFacets;
import com.sap.core.odata.api.edm.EdmMapping;
import com.sap.core.odata.api.edm.EdmMultiplicity;
import com.sap.core.odata.api.edm.FullQualifiedName;

/**
 * @author SAP AG
 */
public class EdmElementImplProv extends EdmTypedImplProv implements EdmElement {

  private EdmFacets edmFacets;
  private EdmMapping edmMapping;

  public EdmElementImplProv(EdmImplProv edm, String name, FullQualifiedName typeName, EdmFacets edmFacets, EdmMapping edmMapping) throws EdmException {
    super(edm, name, typeName, edmFacets == null || edmFacets.isNullable() ? EdmMultiplicity.ZERO_TO_ONE : EdmMultiplicity.ONE);
    this.edmFacets = edmFacets;
    this.edmMapping = edmMapping;
  }

  @Override
  public EdmMapping getMapping() throws EdmException {
    return edmMapping;
  }

  @Override
  public EdmFacets getFacets() throws EdmException {
    return edmFacets;
  }
}