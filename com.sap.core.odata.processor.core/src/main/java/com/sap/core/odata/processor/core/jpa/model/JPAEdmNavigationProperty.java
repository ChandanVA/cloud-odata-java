package com.sap.core.odata.processor.core.jpa.model;

import java.util.ArrayList;
import java.util.List;

import com.sap.core.odata.api.edm.provider.NavigationProperty;
import com.sap.core.odata.processor.api.jpa.access.JPAEdmBuilder;
import com.sap.core.odata.processor.api.jpa.exception.ODataJPAModelException;
import com.sap.core.odata.processor.api.jpa.model.JPAEdmAssociationView;
import com.sap.core.odata.processor.api.jpa.model.JPAEdmNavigationPropertyView;
import com.sap.core.odata.processor.api.jpa.model.JPAEdmPropertyView;
import com.sap.core.odata.processor.api.jpa.model.JPAEdmSchemaView;
import com.sap.core.odata.processor.core.jpa.access.model.JPAEdmNameBuilder;

public class JPAEdmNavigationProperty extends JPAEdmBaseViewImpl implements
    JPAEdmNavigationPropertyView {

  private JPAEdmAssociationView associationView = null;
  private NavigationProperty currentNavigationProperty = null;
  private JPAEdmPropertyView propertyView = null;
  private List<NavigationProperty> consistentNavigationProperties = null;
  private int count;

  public JPAEdmNavigationProperty(final JPAEdmAssociationView associationView,
      final JPAEdmPropertyView propertyView, final int countNumber) {
    super(associationView);
    this.associationView = associationView;
    this.propertyView = propertyView;
    count = countNumber;
    if (consistentNavigationProperties == null)
    {
      consistentNavigationProperties = new ArrayList<NavigationProperty>();
    }
  }

  public JPAEdmNavigationProperty(final JPAEdmSchemaView schemaView) {
    super(schemaView);
    consistentNavigationProperties = new ArrayList<NavigationProperty>();

  }

  @Override
  public JPAEdmBuilder getBuilder() {
    if (builder == null) {
      builder = new JPAEdmNavigationPropertyBuilder();
    }

    return builder;
  }

  private class JPAEdmNavigationPropertyBuilder implements JPAEdmBuilder {

    @Override
    public void build() throws ODataJPAModelException {

      currentNavigationProperty = new NavigationProperty();
      JPAEdmNameBuilder.build(associationView, propertyView,
          JPAEdmNavigationProperty.this, count);
      consistentNavigationProperties.add(currentNavigationProperty);
    }

  }

  @Override
  public NavigationProperty getEdmNavigationProperty() {
    return currentNavigationProperty;
  }

  @Override
  public List<NavigationProperty> getConsistentEdmNavigationProperties() {
    return consistentNavigationProperties;
  }

  @Override
  public void addJPAEdmNavigationPropertyView(
      final JPAEdmNavigationPropertyView view) {
    if (view != null && view.isConsistent()) {
      currentNavigationProperty = view.getEdmNavigationProperty();
      consistentNavigationProperties.add(currentNavigationProperty);

    }
  }

}
