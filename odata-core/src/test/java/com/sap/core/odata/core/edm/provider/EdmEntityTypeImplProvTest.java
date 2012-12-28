package com.sap.core.odata.core.edm.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.sap.core.odata.api.edm.EdmAnnotatable;
import com.sap.core.odata.api.edm.EdmAnnotations;
import com.sap.core.odata.api.edm.EdmProperty;
import com.sap.core.odata.api.edm.EdmSimpleTypeKind;
import com.sap.core.odata.api.edm.FullQualifiedName;
import com.sap.core.odata.api.edm.provider.EdmProvider;
import com.sap.core.odata.api.edm.provider.EntityType;
import com.sap.core.odata.api.edm.provider.Key;
import com.sap.core.odata.api.edm.provider.NavigationProperty;
import com.sap.core.odata.api.edm.provider.Property;
import com.sap.core.odata.api.edm.provider.PropertyRef;
import com.sap.core.odata.api.edm.provider.SimpleProperty;
import com.sap.core.odata.core.edm.provider.EdmEntityTypeImplProv;
import com.sap.core.odata.core.edm.provider.EdmImplProv;

/**
 * @author SAP AG
 */
public class EdmEntityTypeImplProvTest {

  private static EdmEntityTypeImplProv edmEntityType;
  private static EdmEntityTypeImplProv edmEntityTypeWithBaseType;
  private static EdmProvider edmProvider;

  @BeforeClass
  public static void getEdmEntityContainerImpl() throws Exception {

    edmProvider = mock(EdmProvider.class);
    EdmImplProv edmImplProv = new EdmImplProv(edmProvider);

    List<NavigationProperty> navigationProperties = new ArrayList<NavigationProperty>();
    FullQualifiedName fooBarAssocName = new FullQualifiedName("namespace", "fooBarAssoc");
    navigationProperties.add(new NavigationProperty().setName("fooBarNav").setFromRole("fromFoo").setRelationship(fooBarAssocName).setToRole("toBar"));

    EntityType fooEntityType = new EntityType().setName("fooEntityType").setNavigationProperties(navigationProperties);
    FullQualifiedName fooEntityTypeFullName = new FullQualifiedName("namespace", "fooEntityType");
    when(edmProvider.getEntityType(fooEntityTypeFullName)).thenReturn(fooEntityType);

    List<Property> keyPropertysFoo = new ArrayList<Property>();
    Property keyPropFoo = new SimpleProperty().setName("Id").setType(EdmSimpleTypeKind.String);
    keyPropertysFoo.add(keyPropFoo);
    fooEntityType.setProperties(keyPropertysFoo);

    PropertyRef refToKeyFoo = new PropertyRef().setName("Id");
    List<PropertyRef> propRefKeysFoo = new ArrayList<PropertyRef>();
    propRefKeysFoo.add(refToKeyFoo);

    Key fooTypeKey = new Key().setKeys(propRefKeysFoo);
    fooEntityType.setKey(fooTypeKey);

    edmEntityType = new EdmEntityTypeImplProv(edmImplProv, fooEntityType, "namespace");

    FullQualifiedName barBaseTypeName = new FullQualifiedName("namespace", "barBase");
    EntityType barBase = new EntityType().setName("barBase");
    when(edmProvider.getEntityType(barBaseTypeName)).thenReturn(barBase);

    List<NavigationProperty> navigationPropertiesBarBase = new ArrayList<NavigationProperty>();
    navigationPropertiesBarBase.add(new NavigationProperty().setName("barBaseNav"));
    barBase.setNavigationProperties(navigationPropertiesBarBase);

    List<Property> keyPropertysBarBase = new ArrayList<Property>();
    Property keyPropBarBase = new SimpleProperty().setName("Id").setType(EdmSimpleTypeKind.String);
    keyPropertysBarBase.add(keyPropBarBase);
    barBase.setProperties(keyPropertysBarBase);

    PropertyRef refToKeyBarBase = new PropertyRef().setName("Id");
    List<PropertyRef> propRefKeysBarBase = new ArrayList<PropertyRef>();
    propRefKeysBarBase.add(refToKeyBarBase);

    Key barBaseTypeKey = new Key().setKeys(propRefKeysBarBase);
    barBase.setKey(barBaseTypeKey);

    EntityType barEntityType = new EntityType().setName("barEntityType").setBaseType(barBaseTypeName);
    FullQualifiedName barEntityTypeFullName = new FullQualifiedName("namespace", "barEntityType");
    when(edmProvider.getEntityType(barEntityTypeFullName)).thenReturn(barEntityType);

    edmEntityTypeWithBaseType = new EdmEntityTypeImplProv(edmImplProv, barEntityType, "namespace");

  }

  @Test
  public void getKeyProperties() throws Exception {
    List<EdmProperty> keyProperties = edmEntityType.getKeyProperties();
    assertNotNull(keyProperties);
    assertEquals("Id", keyProperties.get(0).getName());
  }

  @Test
  public void getKeyPropertiesNames() throws Exception {
    List<String> keyProperties = edmEntityType.getKeyPropertyNames();
    assertNotNull(keyProperties);
    assertTrue(keyProperties.contains("Id"));

    List<String> properties = edmEntityType.getPropertyNames();
    assertNotNull(properties);
    assertTrue(properties.contains("Id"));
  }

  @Test
  public void getPropertiesNames() throws Exception {
    List<String> properties = edmEntityType.getPropertyNames();
    assertNotNull(properties);
    assertTrue(properties.contains("Id"));
  }

  @Test
  public void getNavProperties() throws Exception {
    List<String> navProperties = edmEntityType.getNavigationPropertyNames();
    assertNotNull(navProperties);
    assertTrue(navProperties.contains("fooBarNav"));
  }

  @Test
  public void getKeyPropertiesWithBaseType() throws Exception {
    List<EdmProperty> keyProperties = edmEntityTypeWithBaseType.getKeyProperties();
    assertNotNull(keyProperties);
    assertEquals("Id", keyProperties.get(0).getName());
  }

  @Test
  public void getKeyPropertiesNamesWithBaseType() throws Exception {
    List<String> keyProperties = edmEntityTypeWithBaseType.getKeyPropertyNames();
    assertNotNull(keyProperties);
    assertTrue(keyProperties.contains("Id"));
  }

  @Test
  public void getPropertiesWithBaseType() throws Exception {
    List<String> properties = edmEntityTypeWithBaseType.getPropertyNames();
    assertNotNull(properties);
    assertTrue(properties.contains("Id"));
  }

  @Test
  public void getNavPropertiesWithBaseType() throws Exception {
    List<String> navProperties = edmEntityTypeWithBaseType.getNavigationPropertyNames();
    assertNotNull(navProperties);
    assertTrue(navProperties.contains("barBaseNav"));
  }

  @Test
  public void getAnnotations() throws Exception {
    EdmAnnotatable annotatable = edmEntityType;
    EdmAnnotations annotations = annotatable.getAnnotations();
    assertNull(annotations.getAnnotationAttributes());
    assertNull(annotations.getAnnotationElements());
  }
}
