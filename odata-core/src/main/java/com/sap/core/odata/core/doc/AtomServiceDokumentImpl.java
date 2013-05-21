package com.sap.core.odata.core.doc;

import java.util.ArrayList;
import java.util.List;

import com.sap.core.odata.api.doc.Collection;
import com.sap.core.odata.api.doc.CommonAttributes;
import com.sap.core.odata.api.doc.ExtensionElement;
import com.sap.core.odata.api.doc.Workspace;
import com.sap.core.odata.api.doc.AtomServiceDocument;
import com.sap.core.odata.api.edm.EdmEntitySetInfo;
import com.sap.core.odata.api.edm.EdmException;
import com.sap.core.odata.api.edm.provider.EntityContainerInfo;
import com.sap.core.odata.api.edm.provider.EntitySet;
import com.sap.core.odata.api.ep.EntityProviderException;
import com.sap.core.odata.core.edm.provider.EdmEntitySetInfoImplProv;

/**
 * ServiceDokumentImpl
 * <p>The implementiation of the interface ServiceDocument
 * @author SAP AG
 */
public class AtomServiceDokumentImpl implements AtomServiceDocument {
  private List<WorkspaceImpl> workspaces;
  private CommonAttributes attributes;
  private List<ExtensionElementImpl> extensionElements;

  @Override
  public CommonAttributes getCommonAttributes() {
    return attributes;
  }

  @Override
  public List<Workspace> getWorkspaces() {
    return (List<Workspace>) (List<? extends Workspace>) workspaces;
  }

  @Override
  public List<ExtensionElement> getExtesionElements() {
    return (List<ExtensionElement>) (List<? extends ExtensionElement>) extensionElements;
  }

  public AtomServiceDokumentImpl setWorkspaces(final List<WorkspaceImpl> workspaces) {
    this.workspaces = workspaces;
    return this;
  }

  public AtomServiceDokumentImpl setCommonAttributes(final CommonAttributes attributes) {
    this.attributes = attributes;
    return this;
  }

  public AtomServiceDokumentImpl setExtesionElements(final List<ExtensionElementImpl> elements) {
    extensionElements = elements;
    return this;
  }

  @Override
  public List<EdmEntitySetInfo> getEntitySetsInfo() throws EntityProviderException {
    List<EdmEntitySetInfo> entitySets = new ArrayList<EdmEntitySetInfo>();
    for (Workspace workspace : workspaces) {
      for (Collection collection : workspace.getCollections()) {
        String[] names = collection.getHref().split("\\.");
        try {
          if (names.length == 1) {
            EntitySet entitySet = new EntitySet().setName(names[0]);
            EntityContainerInfo container = new EntityContainerInfo().setDefaultEntityContainer(true);
            EdmEntitySetInfo entitySetInfo = new EdmEntitySetInfoImplProv(entitySet, container);
            entitySets.add(entitySetInfo);
          } else if (names.length == 2) {
            EntitySet entitySet = new EntitySet().setName(names[1]);
            EntityContainerInfo container = new EntityContainerInfo().setName(names[0]).setDefaultEntityContainer(false);
            EdmEntitySetInfo entitySetInfo = new EdmEntitySetInfoImplProv(entitySet, container);
            entitySets.add(entitySetInfo);
          }
        } catch (EdmException e) {
          throw new EntityProviderException(EntityProviderException.COMMON, e);
        }
      }
    }
    return entitySets;
  }
}
