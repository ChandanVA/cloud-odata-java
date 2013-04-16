package com.sap.core.odata.core.edm.provider;

import com.sap.core.odata.api.edm.EdmAssociation;
import com.sap.core.odata.api.edm.EdmException;
import com.sap.core.odata.api.edm.EdmMultiplicity;
import com.sap.core.odata.api.edm.EdmNavigationProperty;
import com.sap.core.odata.api.edm.EdmType;
import com.sap.core.odata.api.edm.FullQualifiedName;
import com.sap.core.odata.api.edm.provider.NavigationProperty;

public class EdmNavigationPropertyImplProv extends EdmTypedImplProv implements EdmNavigationProperty {

  private NavigationProperty navigationProperty;

  public EdmNavigationPropertyImplProv(EdmImplProv edm, NavigationProperty property) throws EdmException {
    super(edm, property.getName(), null, null);
    navigationProperty = property;
  }

  @Override
  public EdmType getType() throws EdmException {
    return getRelationship().getEnd(navigationProperty.getToRole()).getEntityType();
  }

  @Override
  public EdmMultiplicity getMultiplicity() throws EdmException {
    return getRelationship().getEnd(navigationProperty.getToRole()).getMultiplicity();
  }

  @Override
  public EdmAssociation getRelationship() throws EdmException {
    final FullQualifiedName relationship = navigationProperty.getRelationship();
    return edm.getAssociation(relationship.getNamespace(), relationship.getName());
  }

  @Override
  public String getFromRole() throws EdmException {
    return navigationProperty.getFromRole();
  }

  @Override
  public String getToRole() throws EdmException {
    return navigationProperty.getToRole();
  }

}
