package com.sap.core.odata.core.experimental.edm.adapter;

import com.sap.core.odata.core.edm.EdmAssociationSet;
import com.sap.core.odata.core.edm.EdmEntityContainer;
import com.sap.core.odata.core.edm.EdmEntitySet;
import com.sap.core.odata.core.edm.EdmFunctionImport;
import com.sap.core.odata.core.edm.EdmNavigationProperty;

public class EdmEntityContainerAdapter extends EdmNamedAdapter implements EdmEntityContainer {

  private org.odata4j.edm.EdmEntityContainer edmEntityContainer;

  public EdmEntityContainerAdapter(org.odata4j.edm.EdmEntityContainer edmEntityContainer) {
    super(edmEntityContainer.getName());
    this.edmEntityContainer = edmEntityContainer;
  }

  @Override
  public EdmEntitySet getEntitySet(String name) {
    for (org.odata4j.edm.EdmEntitySet edmEntitySet : this.edmEntityContainer.getEntitySets()) {
      if (edmEntitySet.getName().equals(name)) {
        return new EdmEntitySetAdapter(edmEntitySet, this.edmEntityContainer);
      }
    }
    return null;
  }

  @Override
  public EdmFunctionImport getFunctionImport(String name) {
    for (org.odata4j.edm.EdmFunctionImport edmFunctionImport : this.edmEntityContainer.getFunctionImports()) {
      if (edmFunctionImport.getName().equals(name)) {
        return new EdmFunctionImportAdapter(edmFunctionImport, this.edmEntityContainer);
      }
    }
    return null;
  }

  @Override
  public EdmAssociationSet getAssociationSet(EdmEntitySet sourceEntitySet, EdmNavigationProperty navigationProperty) {
    String relationship = navigationProperty.getRelationship().getName();
    String fromRole = navigationProperty.getFromRole();

    for (org.odata4j.edm.EdmAssociationSet edmAssociationSet : this.edmEntityContainer.getAssociationSets()) {
      if (edmAssociationSet.getAssociation().getName().equals(relationship)) {
        if (edmAssociationSet.getAssociation().getEnd1().getRole().equals(fromRole)) {
          return new EdmAssociationSetAdapter(edmAssociationSet, this.edmEntityContainer);
        } else if (edmAssociationSet.getAssociation().getEnd2().getRole().equals(fromRole)) {
          return new EdmAssociationSetAdapter(edmAssociationSet, this.edmEntityContainer);
        }
      }
    }
    return null;
  }

}
