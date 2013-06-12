package com.sap.core.odata.core.servicedocument;

import java.util.ArrayList;
import java.util.List;

import com.sap.core.odata.api.edm.EdmEntitySetInfo;
import com.sap.core.odata.api.ep.EntityProviderException;
import com.sap.core.odata.api.servicedocument.AtomInfo;
import com.sap.core.odata.api.servicedocument.ServiceDocument;

/**
 * @author SAP AG
 */
public class ServiceDocumentImpl implements ServiceDocument {
  private AtomInfo atomInfo;
  private List<EdmEntitySetInfo> entitySets = new ArrayList<EdmEntitySetInfo>();

  public ServiceDocumentImpl setEntitySetsInfo(final List<EdmEntitySetInfo> entitySets) {
    this.entitySets = entitySets;
    return this;
  }

  @Override
  public List<EdmEntitySetInfo> getEntitySetsInfo() throws EntityProviderException {
    return entitySets;
  }

  @Override
  public AtomInfo getAtomInfo() {
    return atomInfo;
  }

  public void setAtomInfo(final AtomInfo atomInfo) {
    this.atomInfo = atomInfo;
  }

}
