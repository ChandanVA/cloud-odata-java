package com.sap.core.odata.testutils.mocks;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sap.core.odata.api.edm.Edm;
import com.sap.core.odata.api.edm.EdmComplexType;
import com.sap.core.odata.api.edm.EdmConcurrencyMode;
import com.sap.core.odata.api.edm.EdmCustomizableFeedMappings;
import com.sap.core.odata.api.edm.EdmEntityContainer;
import com.sap.core.odata.api.edm.EdmEntitySet;
import com.sap.core.odata.api.edm.EdmEntityType;
import com.sap.core.odata.api.edm.EdmException;
import com.sap.core.odata.api.edm.EdmFacets;
import com.sap.core.odata.api.edm.EdmFunctionImport;
import com.sap.core.odata.api.edm.EdmMultiplicity;
import com.sap.core.odata.api.edm.EdmNavigationProperty;
import com.sap.core.odata.api.edm.EdmParameter;
import com.sap.core.odata.api.edm.EdmProperty;
import com.sap.core.odata.api.edm.EdmServiceMetadata;
import com.sap.core.odata.api.edm.EdmSimpleTypeKind;
import com.sap.core.odata.api.edm.EdmTargetPath;
import com.sap.core.odata.api.edm.EdmType;
import com.sap.core.odata.api.edm.EdmTypeKind;
import com.sap.core.odata.api.edm.EdmTyped;
import com.sap.core.odata.api.edm.provider.CustomizableFeedMappings;
import com.sap.core.odata.api.exception.ODataException;

class EdmMock {

  public static Edm createMockEdm() throws ODataException {
    EdmServiceMetadata serviceMetadata = mock(EdmServiceMetadata.class);
    when(serviceMetadata.getDataServiceVersion()).thenReturn("MockEdm");

    EdmEntityContainer defaultContainer = mock(EdmEntityContainer.class);
    EdmEntitySet employeeEntitySet = createEntitySetMock(defaultContainer, "Employees", EdmSimpleTypeKind.String.getEdmSimpleTypeInstance(), "EmployeeId");
    EdmEntitySet managerEntitySet = createEntitySetMock(defaultContainer, "Managers", EdmSimpleTypeKind.String.getEdmSimpleTypeInstance(), "EmployeeId");
    EdmEntitySet roomEntitySet = createEntitySetMock(defaultContainer, "Rooms", EdmSimpleTypeKind.String.getEdmSimpleTypeInstance(), "Id");
    EdmEntitySet teamEntitySet = createEntitySetMock(defaultContainer, "Teams", EdmSimpleTypeKind.String.getEdmSimpleTypeInstance(), "Id");

    when(defaultContainer.getEntitySet("Employees")).thenReturn(employeeEntitySet);
    when(defaultContainer.getEntitySet("Rooms")).thenReturn(roomEntitySet);
    when(defaultContainer.getEntitySet("Teams")).thenReturn(teamEntitySet);
    when(defaultContainer.isDefaultEntityContainer()).thenReturn(true);

    EdmType navigationType = mock(EdmType.class);
    when(navigationType.getKind()).thenReturn(EdmTypeKind.ENTITY);

    EdmNavigationProperty employeeProperty = mock(EdmNavigationProperty.class);
    when(employeeProperty.getType()).thenReturn(navigationType);
    when(employeeProperty.getMultiplicity()).thenReturn(EdmMultiplicity.MANY);
    when(managerEntitySet.getRelatedEntitySet(employeeProperty)).thenReturn(employeeEntitySet);

    EdmEntityType roomType = roomEntitySet.getEntityType();
    when(roomType.getName()).thenReturn("Room");
    when(roomType.getPropertyNames()).thenReturn(Arrays.asList("Id"));
    when(roomType.hasStream()).thenReturn(false);
    EdmProperty roomId = roomType.getKeyProperties().get(0);
    EdmFacets roomIdFacet = mock(EdmFacets.class);
    when(roomIdFacet.getMaxLength()).thenReturn(100);
    when(roomIdFacet.getConcurrencyMode()).thenReturn(EdmConcurrencyMode.Fixed);
    when(roomId.getFacets()).thenReturn(roomIdFacet);

    EdmEntityType managerType = managerEntitySet.getEntityType();
    when(managerType.getProperty("nm_Employees")).thenReturn(employeeProperty);
    //when(managerType.getProperty("somethingwrong")).thenThrow(new EdmException("Property not found"));

    EdmNavigationProperty managerProperty = mock(EdmNavigationProperty.class);
    when(managerProperty.getType()).thenReturn(navigationType);
    when(managerProperty.getName()).thenReturn("ne_Manager");
    when(managerProperty.getMultiplicity()).thenReturn(EdmMultiplicity.ONE);
    when(employeeEntitySet.getRelatedEntitySet(managerProperty)).thenReturn(managerEntitySet);
    when(employeeEntitySet.getEntityContainer()).thenReturn(defaultContainer);

    EdmNavigationProperty teamNavigationProperty = mock(EdmNavigationProperty.class);
    when(teamNavigationProperty.getType()).thenReturn(navigationType);
    when(teamNavigationProperty.getName()).thenReturn("ne_Team");
    when(teamNavigationProperty.getMultiplicity()).thenReturn(EdmMultiplicity.ONE);
    when(employeeEntitySet.getRelatedEntitySet(teamNavigationProperty)).thenReturn(teamEntitySet);
    when(employeeEntitySet.getEntityContainer()).thenReturn(defaultContainer);

    EdmNavigationProperty roomNavigationProperty = mock(EdmNavigationProperty.class);
    when(roomNavigationProperty.getType()).thenReturn(navigationType);
    when(roomNavigationProperty.getName()).thenReturn("ne_Room");
    when(roomNavigationProperty.getMultiplicity()).thenReturn(EdmMultiplicity.ONE);
    when(employeeEntitySet.getRelatedEntitySet(roomNavigationProperty)).thenReturn(roomEntitySet);
    when(employeeEntitySet.getEntityContainer()).thenReturn(defaultContainer);

    
    EdmEntityType employeeType = employeeEntitySet.getEntityType();
    when(employeeType.getKind()).thenReturn(EdmTypeKind.ENTITY);
    when(employeeType.hasStream()).thenReturn(true);
    when(employeeType.getPropertyNames()).thenReturn(Arrays.asList("EmployeeId", "EmployeeName", "ImageUrl", "Age", "TeamId", "RoomId", "EntryDate", "Location"));
    when(employeeType.getProperty("ne_Manager")).thenReturn(managerProperty);
    when(employeeType.getProperty("ne_Team")).thenReturn(teamNavigationProperty);
    when(employeeType.getProperty("ne_Room")).thenReturn(roomNavigationProperty);
    when(employeeType.getKeyPropertyNames()).thenReturn(Arrays.asList("EmployeeId"));
    when(employeeType.getName()).thenReturn("Employee");
    when(employeeType.getNamespace()).thenReturn("RefScenario");
    when(employeeType.getNavigationPropertyNames()).thenReturn(Arrays.asList("ne_Manager", "ne_Team", "ne_Room"));

    EdmProperty employeeIdProperty = mock(EdmProperty.class);
    when(employeeIdProperty.getType()).thenReturn(EdmSimpleTypeKind.String.getEdmSimpleTypeInstance());
    when(employeeIdProperty.getName()).thenReturn("EmployeeId");
    when(employeeType.getProperty("EmployeeId")).thenReturn(employeeIdProperty);
    when(employeeType.getKeyProperties()).thenReturn(Arrays.asList(employeeIdProperty));

    EdmProperty employeeNameProperty = mock(EdmProperty.class);
    when(employeeNameProperty.getType()).thenReturn(EdmSimpleTypeKind.String.getEdmSimpleTypeInstance());
    when(employeeNameProperty.getName()).thenReturn("EmployeeName");
    when(employeeType.getProperty("EmployeeName")).thenReturn(employeeNameProperty);

    EdmProperty employeeImageUrlProperty = mock(EdmProperty.class);
    when(employeeImageUrlProperty.getType()).thenReturn(EdmSimpleTypeKind.String.getEdmSimpleTypeInstance());
    when(employeeImageUrlProperty.getName()).thenReturn("ImageUrl");
    when(employeeType.getProperty("ImageUrl")).thenReturn(employeeImageUrlProperty);

    EdmProperty employeeAgeProperty = mock(EdmProperty.class);
    when(employeeAgeProperty.getType()).thenReturn(EdmSimpleTypeKind.Int32.getEdmSimpleTypeInstance());
    when(employeeAgeProperty.getName()).thenReturn("Age");
    when(employeeType.getProperty("Age")).thenReturn(employeeAgeProperty);

    EdmProperty employeeRoomIdProperty = mock(EdmProperty.class);
    when(employeeRoomIdProperty.getType()).thenReturn(EdmSimpleTypeKind.String.getEdmSimpleTypeInstance());
    when(employeeRoomIdProperty.getName()).thenReturn("RoomId");
    when(employeeType.getProperty("RoomId")).thenReturn(employeeRoomIdProperty);

    EdmProperty employeeEntryDateProperty = mock(EdmProperty.class);
    when(employeeEntryDateProperty.getType()).thenReturn(EdmSimpleTypeKind.DateTime.getEdmSimpleTypeInstance());
    when(employeeEntryDateProperty.getName()).thenReturn("EntryDate");
    when(employeeType.getProperty("EntryDate")).thenReturn(employeeEntryDateProperty);

    EdmCustomizableFeedMappings employeeUpdatedeMappings = mock(EdmCustomizableFeedMappings.class);
    when(employeeUpdatedeMappings.getFcTargetPath()).thenReturn(EdmTargetPath.SYNDICATION_UPDATED);
    when(employeeUpdatedeMappings.isFcKeepInContent()).thenReturn(Boolean.TRUE);
    when(employeeEntryDateProperty.getCustomizableFeedMappings()).thenReturn(employeeUpdatedeMappings);

    EdmProperty employeeTeamIdProperty = mock(EdmProperty.class);
    when(employeeTeamIdProperty.getType()).thenReturn(EdmSimpleTypeKind.String.getEdmSimpleTypeInstance());
    when(employeeTeamIdProperty.getName()).thenReturn("TeamId");
    when(employeeType.getProperty("TeamId")).thenReturn(employeeTeamIdProperty);

    EdmCustomizableFeedMappings employeeTitleMappings = mock(EdmCustomizableFeedMappings.class);
    when(employeeTitleMappings.getFcTargetPath()).thenReturn(EdmTargetPath.SYNDICATION_TITLE);
    when(employeeTitleMappings.isFcKeepInContent()).thenReturn(Boolean.TRUE);
    when(employeeNameProperty.getCustomizableFeedMappings()).thenReturn(employeeTitleMappings);

    EdmComplexType locationComplexType = mock(EdmComplexType.class);
    when(locationComplexType.getKind()).thenReturn(EdmTypeKind.COMPLEX);
    when(locationComplexType.getName()).thenReturn("c_Location");
    when(locationComplexType.getNamespace()).thenReturn("RefScenario");
    when(locationComplexType.getPropertyNames()).thenReturn(Arrays.asList("City", "Country"));

    EdmProperty locationComplexProperty = mock(EdmProperty.class);
    when(locationComplexProperty.getType()).thenReturn(locationComplexType);
    when(locationComplexProperty.getName()).thenReturn("Location");
    when(employeeType.getProperty("Location")).thenReturn(locationComplexProperty);

    EdmProperty countryProperty = mock(EdmProperty.class);
    when(countryProperty.getType()).thenReturn(EdmSimpleTypeKind.String.getEdmSimpleTypeInstance());
    when(countryProperty.getName()).thenReturn("Country");
    when(locationComplexType.getProperty("Country")).thenReturn(countryProperty);

    EdmComplexType cityComplexType = mock(EdmComplexType.class);
    when(cityComplexType.getKind()).thenReturn(EdmTypeKind.COMPLEX);
    when(cityComplexType.getName()).thenReturn("City");
    when(cityComplexType.getNamespace()).thenReturn("RefScenario");
    when(cityComplexType.getPropertyNames()).thenReturn(Arrays.asList("PostalCode", "CityName"));

    EdmProperty cityProperty = mock(EdmProperty.class);
    when(cityProperty.getType()).thenReturn(cityComplexType);
    when(cityProperty.getName()).thenReturn("City");
    when(locationComplexType.getProperty("City")).thenReturn(cityProperty);

    EdmProperty postalCodeProperty = mock(EdmProperty.class);
    when(postalCodeProperty.getType()).thenReturn(EdmSimpleTypeKind.String.getEdmSimpleTypeInstance());
    when(postalCodeProperty.getName()).thenReturn("PostalCode");

    when(cityComplexType.getProperty("PostalCode")).thenReturn(postalCodeProperty);

    EdmProperty cityNameProperty = mock(EdmProperty.class);
    when(cityNameProperty.getType()).thenReturn(EdmSimpleTypeKind.String.getEdmSimpleTypeInstance());
    when(cityNameProperty.getName()).thenReturn("CityName");
    when(cityComplexType.getProperty("CityName")).thenReturn(cityNameProperty);

    EdmEntitySet teamsEntitySet = createEntitySetMock(defaultContainer, "Teams", EdmSimpleTypeKind.String.getEdmSimpleTypeInstance(), "Id");
    when(teamsEntitySet.getEntityType().getProperty("nt_Employees")).thenReturn(employeeProperty);
    when(teamsEntitySet.getRelatedEntitySet(employeeProperty)).thenReturn(employeeEntitySet);

    EdmFunctionImport employeeSearchFunctionImport = createFunctionImportMock(defaultContainer, "EmployeeSearch", employeeType, EdmMultiplicity.MANY);
    when(employeeSearchFunctionImport.getEntitySet()).thenReturn(employeeEntitySet);
    List<String> employeeSearchParameterNames = new ArrayList<String>();
    employeeSearchParameterNames.add("q");
    EdmParameter employeeSearchParameter = mock(EdmParameter.class);
    when(employeeSearchParameter.getType()).thenReturn(EdmSimpleTypeKind.String.getEdmSimpleTypeInstance());
    when(employeeSearchFunctionImport.getParameterNames()).thenReturn(employeeSearchParameterNames);
    when(employeeSearchFunctionImport.getParameter("q")).thenReturn(employeeSearchParameter);
    createFunctionImportMock(defaultContainer, "AllLocations", locationComplexType, EdmMultiplicity.MANY);
    createFunctionImportMock(defaultContainer, "AllUsedRoomIds", EdmSimpleTypeKind.String.getEdmSimpleTypeInstance(), EdmMultiplicity.MANY);
    createFunctionImportMock(defaultContainer, "MaximalAge", EdmSimpleTypeKind.Int16.getEdmSimpleTypeInstance(), EdmMultiplicity.ONE);
    createFunctionImportMock(defaultContainer, "MostCommonLocation", locationComplexType, EdmMultiplicity.ONE);
    EdmFunctionImport managerPhotoFunctionImport = createFunctionImportMock(defaultContainer, "ManagerPhoto", EdmSimpleTypeKind.Binary.getEdmSimpleTypeInstance(), EdmMultiplicity.ONE);
    List<String> managerPhotoParameterNames = new ArrayList<String>();
    managerPhotoParameterNames.add("Id");
    EdmParameter managerPhotoParameter = mock(EdmParameter.class);
    when(managerPhotoParameter.getType()).thenReturn(EdmSimpleTypeKind.String.getEdmSimpleTypeInstance());
    EdmFacets managerPhotoParameterFacets = mock(EdmFacets.class);
    when(managerPhotoParameterFacets.isNullable()).thenReturn(false);
    when(managerPhotoParameter.getFacets()).thenReturn(managerPhotoParameterFacets);
    when(managerPhotoFunctionImport.getParameterNames()).thenReturn(managerPhotoParameterNames);
    when(managerPhotoFunctionImport.getParameter("Id")).thenReturn(managerPhotoParameter);
    EdmFunctionImport oldestEmployeeFunctionImport = createFunctionImportMock(defaultContainer, "OldestEmployee", employeeType, EdmMultiplicity.ONE);
    when(oldestEmployeeFunctionImport.getEntitySet()).thenReturn(employeeEntitySet);

    EdmEntityContainer specificContainer = mock(EdmEntityContainer.class);
    when(specificContainer.getEntitySet("Employees")).thenReturn(employeeEntitySet);
    when(specificContainer.getName()).thenReturn("Container1");

    EdmProperty photoIdProperty = mock(EdmProperty.class);
    when(photoIdProperty.getType()).thenReturn(EdmSimpleTypeKind.Int32.getEdmSimpleTypeInstance());
    when(photoIdProperty.getName()).thenReturn("Id");
    EdmFacets photoIdFacet = mock(EdmFacets.class);
    when(photoIdFacet.getConcurrencyMode()).thenReturn(EdmConcurrencyMode.Fixed);
    when(photoIdProperty.getFacets()).thenReturn(photoIdFacet);
    EdmProperty photoTypeProperty = mock(EdmProperty.class);
    when(photoTypeProperty.getType()).thenReturn(EdmSimpleTypeKind.String.getEdmSimpleTypeInstance());
    when(photoTypeProperty.getName()).thenReturn("Type");
    List<EdmProperty> photoKeyProperties = new ArrayList<EdmProperty>();
    photoKeyProperties.add(photoIdProperty);
    photoKeyProperties.add(photoTypeProperty);

    EdmProperty photoRussianProperty = createProperty("Содержание", EdmSimpleTypeKind.String);
    CustomizableFeedMappings cfm = new CustomizableFeedMappings().setFcKeepInContent(false).setFcNsPrefix("py").setFcNsUri("http://localhost").setFcTargetPath("Содержание");
    when(photoRussianProperty.getCustomizableFeedMappings()).thenReturn(cfm);

    EdmEntityType photoEntityType = mock(EdmEntityType.class);
    when(photoEntityType.getName()).thenReturn("Photo");
    when(photoEntityType.getNamespace()).thenReturn("RefScenario2");
    when(photoEntityType.getPropertyNames()).thenReturn(Arrays.asList("Id", "Type", "Содержание"));
    when(photoEntityType.getKeyPropertyNames()).thenReturn(Arrays.asList("Id", "Type"));
    when(photoEntityType.getKeyProperties()).thenReturn(photoKeyProperties);
    when(photoEntityType.getProperty("Id")).thenReturn(photoIdProperty);
    when(photoEntityType.getProperty("Type")).thenReturn(photoTypeProperty);
    when(photoEntityType.getProperty(photoRussianProperty.getName())).thenReturn(photoRussianProperty);
    EdmEntitySet photoEntitySet = mock(EdmEntitySet.class);
    when(photoEntitySet.getName()).thenReturn("Photos");
    when(photoEntitySet.getEntityType()).thenReturn(photoEntityType);
    EdmEntityContainer photoContainer = mock(EdmEntityContainer.class);
    when(photoContainer.isDefaultEntityContainer()).thenReturn(false);
    when(photoContainer.getEntitySet("Photos")).thenReturn(photoEntitySet);
    when(photoContainer.getName()).thenReturn("Container2");

    //        properties.add(new SimpleProperty().setName("Содержание")
    //.setType(EdmSimpleTypeKind.String).setFacets(new Facets().setNullable(true))
    //.setCustomizableFeedMappings(new CustomizableFeedMappings().setFcKeepInContent(false).setFcNsPrefix("py").setFcNsUri("http://localhost").setFcTargetPath("Содержание")).setMapping(new Mapping().setValue("getContent")));

    when(photoEntitySet.getEntityContainer()).thenReturn(photoContainer);

    Edm edm = mock(Edm.class);
    when(edm.getServiceMetadata()).thenReturn(serviceMetadata);
    when(edm.getDefaultEntityContainer()).thenReturn(defaultContainer);
    when(edm.getEntityContainer("Container1")).thenReturn(specificContainer);
    when(edm.getEntityContainer("Container2")).thenReturn(photoContainer);
    when(edm.getEntityType("RefScenario", "Employee")).thenReturn(employeeType);

    return edm;
  }

  private static EdmProperty createProperty(String name, EdmSimpleTypeKind kind) throws EdmException {
    EdmProperty property = mock(EdmProperty.class);
    when(property.getType()).thenReturn(kind.getEdmSimpleTypeInstance());
    when(property.getName()).thenReturn(name);
    return property;
  }

  private static EdmEntitySet createEntitySetMock(final EdmEntityContainer container, final String name, final EdmType type, final String keyPropertyId) throws EdmException {
    EdmEntityType entityType = createEntityTypeMock(type, keyPropertyId);

    EdmEntitySet entitySet = mock(EdmEntitySet.class);
    when(entitySet.getName()).thenReturn(name);
    when(entitySet.getEntityType()).thenReturn(entityType);

    when(entitySet.getEntityContainer()).thenReturn(container);

    when(container.getEntitySet(name)).thenReturn(entitySet);

    return entitySet;
  }

  private static EdmEntityType createEntityTypeMock(final EdmType type, final String keyPropertyId) throws EdmException {
    EdmProperty edmProperty = mock(EdmProperty.class);
    when(edmProperty.getName()).thenReturn(keyPropertyId);
    when(edmProperty.getType()).thenReturn(type);

    List<String> keyPropertyNames = new ArrayList<String>();
    keyPropertyNames.add(keyPropertyId);

    List<EdmProperty> keyProperties = new ArrayList<EdmProperty>();
    keyProperties.add(edmProperty);

    EdmEntityType entityType = mock(EdmEntityType.class);
    when(entityType.getKeyPropertyNames()).thenReturn(keyPropertyNames);
    when(entityType.getKeyProperties()).thenReturn(keyProperties);
    when(entityType.getProperty(keyPropertyId)).thenReturn(edmProperty);
    return entityType;
  }

  private static EdmFunctionImport createFunctionImportMock(final EdmEntityContainer container, final String name, final EdmType type, final EdmMultiplicity multiplicity) throws EdmException {
    EdmTyped returnType = mock(EdmTyped.class);
    when(returnType.getType()).thenReturn(type);
    when(returnType.getMultiplicity()).thenReturn(multiplicity);
    EdmFunctionImport functionImport = mock(EdmFunctionImport.class);
    when(functionImport.getName()).thenReturn(name);
    when(functionImport.getReturnType()).thenReturn(returnType);
    when(container.getFunctionImport(name)).thenReturn(functionImport);
    return functionImport;
  }

}
