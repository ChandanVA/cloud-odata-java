package com.sap.core.odata.core.edm.provider;

import com.sap.core.odata.api.edm.EdmAssociation;
import com.sap.core.odata.api.edm.EdmComplexType;
import com.sap.core.odata.api.edm.EdmEntityContainer;
import com.sap.core.odata.api.edm.EdmEntityType;
import com.sap.core.odata.api.edm.EdmException;
import com.sap.core.odata.api.edm.FullQualifiedName;
import com.sap.core.odata.api.edm.provider.EdmProvider;
import com.sap.core.odata.api.exception.ODataMessageException;
import com.sap.core.odata.api.exception.ODataRuntimeException;
import com.sap.core.odata.core.edm.EdmImpl;

public class EdmImplProv extends EdmImpl {

  protected EdmProvider edmProvider;

  public EdmImplProv(EdmProvider edmProvider) {
    super();
    this.edmProvider = edmProvider;
  }

  @Override
  protected EdmEntityContainer createEntityContainer(String name) throws ODataRuntimeException, ODataMessageException, EdmException {
    return new EdmEntityContainerImplProv(this, edmProvider.getEntityContainer(name));
  }

  @Override
  protected EdmEntityType createEntityType(FullQualifiedName fqName) throws ODataRuntimeException, ODataMessageException, EdmException {
    return new EdmEntityTypeImplProv(this, edmProvider.getEntityType(fqName), fqName.getNamespace());
  }

  @Override
  protected EdmComplexType createComplexType(FullQualifiedName fqName) throws ODataRuntimeException, ODataMessageException, EdmException {
    return new EdmComplexTypeImplProv(this, edmProvider.getComplexType(fqName), fqName.getNamespace());
  }

  @Override
  protected EdmAssociation createAssociation(FullQualifiedName fqName) throws ODataRuntimeException, ODataMessageException, EdmException {
    return new EdmAssociationImplProv(this, edmProvider.getAssociation(fqName), fqName.getNamespace());
  }
}