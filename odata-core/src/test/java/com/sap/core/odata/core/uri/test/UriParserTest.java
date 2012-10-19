package com.sap.core.odata.core.uri.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.PathSegment;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Test;

import com.sap.core.odata.core.edm.Edm;
import com.sap.core.odata.core.edm.EdmComplexType;
import com.sap.core.odata.core.edm.EdmEntityContainer;
import com.sap.core.odata.core.edm.EdmEntitySet;
import com.sap.core.odata.core.edm.EdmEntityType;
import com.sap.core.odata.core.edm.EdmException;
import com.sap.core.odata.core.edm.EdmFacets;
import com.sap.core.odata.core.edm.EdmFunctionImport;
import com.sap.core.odata.core.edm.EdmMultiplicity;
import com.sap.core.odata.core.edm.EdmNavigationProperty;
import com.sap.core.odata.core.edm.EdmParameter;
import com.sap.core.odata.core.edm.EdmProperty;
import com.sap.core.odata.core.edm.EdmServiceMetadata;
import com.sap.core.odata.core.edm.EdmSimpleType;
import com.sap.core.odata.core.edm.EdmType;
import com.sap.core.odata.core.edm.EdmTypeKind;
import com.sap.core.odata.core.edm.EdmTyped;
import com.sap.core.odata.core.exception.ODataException;
import com.sap.core.odata.core.uri.KeyPredicate;
import com.sap.core.odata.core.uri.UriParser;
import com.sap.core.odata.core.uri.UriParserException;
import com.sap.core.odata.core.uri.UriParserResult;
import com.sap.core.odata.core.uri.enums.Format;
import com.sap.core.odata.core.uri.enums.InlineCount;
import com.sap.core.odata.core.uri.enums.UriType;

public class UriParserTest {

  static {
    DOMConfigurator.configureAndWatch("log4j.xml");
  }

  public Edm getEdm() throws ODataException {
    return createMockEdm();
  }

  private Edm createMockEdm() throws ODataException {
    EdmServiceMetadata serviceMetadata = mock(EdmServiceMetadata.class);
    when(serviceMetadata.getDataServiceVersion()).thenReturn("MockEdm");

    EdmEntityContainer defaultContainer = mock(EdmEntityContainer.class);
    EdmEntitySet employeeEntitySet = createEntitySetMock(defaultContainer, "Employees", EdmSimpleType.STRING, "EmployeeId");
    EdmEntitySet managerEntitySet = createEntitySetMock(defaultContainer, "Managers", EdmSimpleType.STRING, "EmployeeId");

    EdmType navigationType = mock(EdmType.class);
    when(navigationType.getKind()).thenReturn(EdmTypeKind.NAVIGATION);

    EdmNavigationProperty employeeProperty = mock(EdmNavigationProperty.class);
    when(employeeProperty.getType()).thenReturn(navigationType);
    when(employeeProperty.getMultiplicity()).thenReturn(EdmMultiplicity.MANY);
    when(managerEntitySet.getRelatedEntitySet(employeeProperty)).thenReturn(employeeEntitySet);

    EdmEntityType managerType = managerEntitySet.getEntityType();
    when(managerType.getProperty("nm_Employees")).thenReturn(employeeProperty);

    EdmNavigationProperty managerProperty = mock(EdmNavigationProperty.class);
    when(managerProperty.getType()).thenReturn(navigationType);
    when(managerProperty.getMultiplicity()).thenReturn(EdmMultiplicity.ONE);
    when(employeeEntitySet.getRelatedEntitySet(managerProperty)).thenReturn(managerEntitySet);

    EdmEntityType employeeType = employeeEntitySet.getEntityType();
    when(employeeType.getKind()).thenReturn(EdmTypeKind.ENTITY);
    when(employeeType.hasStream()).thenReturn(true);
    when(employeeType.getProperty("ne_Manager")).thenReturn(managerProperty);
    when(employeeType.getProperty(matches(".+wrong|\\$links"))).thenThrow(new EdmException("Property not found"));

    EdmProperty employeeSimpleProperty = mock(EdmProperty.class);
    when(employeeSimpleProperty.getType()).thenReturn(EdmSimpleType.STRING);
    when(employeeSimpleProperty.getName()).thenReturn("EmployeeName");
    when(employeeType.getProperty("EmployeeName")).thenReturn(employeeSimpleProperty);

    EdmComplexType locationComplexType = mock(EdmComplexType.class);
    when(locationComplexType.getKind()).thenReturn(EdmTypeKind.COMPLEX);

    EdmProperty locationComplexProperty = mock(EdmProperty.class);
    when(locationComplexProperty.getType()).thenReturn(locationComplexType);
    when(locationComplexProperty.getName()).thenReturn("Location");
    when(employeeType.getProperty("Location")).thenReturn(locationComplexProperty);

    EdmProperty countryProperty = mock(EdmProperty.class);
    when(countryProperty.getType()).thenReturn(EdmSimpleType.STRING);
    when(countryProperty.getName()).thenReturn("Country");
    when(locationComplexType.getProperty("Country")).thenReturn(countryProperty);
    when(locationComplexType.getProperty("somethingwrong")).thenThrow(new EdmException("Property not found"));

    EdmEntitySet teamsEntitySet = createEntitySetMock(defaultContainer, "Teams", EdmSimpleType.STRING, "Id");
    when(teamsEntitySet.getEntityType().getProperty("nt_Employees")).thenReturn(employeeProperty);
    when(teamsEntitySet.getRelatedEntitySet(employeeProperty)).thenReturn(employeeEntitySet);

    createEntitySetMock(defaultContainer, "Decimals", EdmSimpleType.DECIMAL, "Id");
    createEntitySetMock(defaultContainer, "Int16s", EdmSimpleType.INT16, "Id");
    createEntitySetMock(defaultContainer, "Int32s", EdmSimpleType.INT32, "Id");
    createEntitySetMock(defaultContainer, "Int64s", EdmSimpleType.INT64, "Id");
    createEntitySetMock(defaultContainer, "Strings", EdmSimpleType.STRING, "Id");
    createEntitySetMock(defaultContainer, "Singles", EdmSimpleType.SINGLE, "Id");
    createEntitySetMock(defaultContainer, "Doubles", EdmSimpleType.DOUBLE, "Id");
    createEntitySetMock(defaultContainer, "DateTimes", EdmSimpleType.DATETIME, "Id");
    createEntitySetMock(defaultContainer, "DateTimeOffsets", EdmSimpleType.DATETIMEOFFSET, "Id");
    createEntitySetMock(defaultContainer, "Booleans", EdmSimpleType.BOOLEAN, "Id");
    createEntitySetMock(defaultContainer, "SBytes", EdmSimpleType.SBYTE, "Id");
    createEntitySetMock(defaultContainer, "Binaries", EdmSimpleType.BINARY, "Id");
    createEntitySetMock(defaultContainer, "Bytes", EdmSimpleType.BYTE, "Id");
    createEntitySetMock(defaultContainer, "Guids", EdmSimpleType.GUID, "Id");
    createEntitySetMock(defaultContainer, "Times", EdmSimpleType.TIME, "Id");

    when(defaultContainer.getEntitySet(matches("some.+|.+Search|All.+|Max.+|Most.+|ManagerPhoto|Old.+"))).thenThrow(new EdmException("Entity set not found"));

    EdmFunctionImport employeeSearchFunctionImport = createFunctionImportMock(defaultContainer, "EmployeeSearch", employeeType, EdmMultiplicity.MANY);
    when(employeeSearchFunctionImport.getEntitySet()).thenReturn(employeeEntitySet);
    List<String> employeeSearchParameterNames = new ArrayList<String>();
    employeeSearchParameterNames.add("q");
    EdmParameter employeeSearchParameter = mock(EdmParameter.class);
    when(employeeSearchParameter.getType()).thenReturn(EdmSimpleType.STRING);
    when(employeeSearchFunctionImport.getParameterNames()).thenReturn(employeeSearchParameterNames);
    when(employeeSearchFunctionImport.getParameter("q")).thenReturn(employeeSearchParameter);
    createFunctionImportMock(defaultContainer, "AllLocations", locationComplexType, EdmMultiplicity.MANY);
    createFunctionImportMock(defaultContainer, "AllUsedRoomIds", EdmSimpleType.STRING, EdmMultiplicity.MANY);
    createFunctionImportMock(defaultContainer, "MaximalAge", EdmSimpleType.INT16, EdmMultiplicity.ONE);
    createFunctionImportMock(defaultContainer, "MostCommonLocation", locationComplexType, EdmMultiplicity.ONE);
    EdmFunctionImport managerPhotoFunctionImport = createFunctionImportMock(defaultContainer, "ManagerPhoto", EdmSimpleType.BINARY, EdmMultiplicity.ONE);
    List<String> managerPhotoParameterNames = new ArrayList<String>();
    managerPhotoParameterNames.add("Id");
    EdmParameter managerPhotoParameter = mock(EdmParameter.class);
    when(managerPhotoParameter.getType()).thenReturn(EdmSimpleType.STRING);
    EdmFacets managerPhotoParameterFacets = mock(EdmFacets.class);
    when(managerPhotoParameterFacets.isNullable()).thenReturn(false);
    when(managerPhotoParameter.getFacets()).thenReturn(managerPhotoParameterFacets);
    when(managerPhotoFunctionImport.getParameterNames()).thenReturn(managerPhotoParameterNames);
    when(managerPhotoFunctionImport.getParameter("Id")).thenReturn(managerPhotoParameter);
    createFunctionImportMock(defaultContainer, "OldestEmployee", employeeType, EdmMultiplicity.ONE);
    when(defaultContainer.getFunctionImport("somethingwrong")).thenThrow(new EdmException("Function import not found"));

    EdmEntityContainer specificContainer = mock(EdmEntityContainer.class);
    when(specificContainer.getEntitySet("Employees")).thenReturn(employeeEntitySet);
    when(specificContainer.getName()).thenReturn("Container1");

    EdmProperty photoIdProperty = mock(EdmProperty.class);
    when(photoIdProperty.getType()).thenReturn(EdmSimpleType.INT32);
    when(photoIdProperty.getName()).thenReturn("Id");
    EdmProperty photoTypeProperty = mock(EdmProperty.class);
    when(photoTypeProperty.getType()).thenReturn(EdmSimpleType.STRING);
    when(photoTypeProperty.getName()).thenReturn("Type");
    List<EdmProperty> photoKeyProperties = new ArrayList<EdmProperty>();
    photoKeyProperties.add(photoIdProperty);
    photoKeyProperties.add(photoTypeProperty);
    EdmEntityType photoEntityType = mock(EdmEntityType.class);
    when(photoEntityType.getKeyProperties()).thenReturn(photoKeyProperties);
    when(photoEntityType.getProperty("Id")).thenReturn(photoIdProperty);
    when(photoEntityType.getProperty("Type")).thenReturn(photoTypeProperty);
    EdmEntitySet photoEntitySet = mock(EdmEntitySet.class);
    when(photoEntitySet.getName()).thenReturn("Photos");
    when(photoEntitySet.getEntityType()).thenReturn(photoEntityType);
    EdmEntityContainer photoContainer = mock(EdmEntityContainer.class);
    when(photoContainer.getEntitySet("Photos")).thenReturn(photoEntitySet);
    when(photoContainer.getName()).thenReturn("Container2");

    Edm edm = mock(Edm.class);
    when(edm.getServiceMetadata()).thenReturn(serviceMetadata);
    when(edm.getDefaultEntityContainer()).thenReturn(defaultContainer);
    when(edm.getEntityContainer("Container1")).thenReturn(specificContainer);
    when(edm.getEntityContainer("Container2")).thenReturn(photoContainer);
    when(edm.getEntityContainer("somethingwrong")).thenThrow(new EdmException("Container not found"));
    return edm;
  }

  private EdmEntitySet createEntitySetMock(final EdmEntityContainer container, final String name, final EdmType type, final String keyPropertyId) throws ODataException {
    EdmEntityType entityType = createEntityTypeMock(type, keyPropertyId);

    EdmEntitySet entitySet = mock(EdmEntitySet.class);
    when(entitySet.getName()).thenReturn(name);
    when(entitySet.getEntityType()).thenReturn(entityType);

    when(container.getEntitySet(name)).thenReturn(entitySet);

    return entitySet;
  }

  private EdmEntityType createEntityTypeMock(final EdmType type, final String keyPropertyId) throws ODataException {
    EdmProperty edmProperty = mock(EdmProperty.class);
    when(edmProperty.getName()).thenReturn(keyPropertyId);
    when(edmProperty.getType()).thenReturn(type);

    List<EdmProperty> keyProperties = new ArrayList<EdmProperty>();
    keyProperties.add(edmProperty);

    EdmEntityType entityType = mock(EdmEntityType.class);
    when(entityType.getKeyProperties()).thenReturn(keyProperties);
    when(entityType.getProperty(keyPropertyId)).thenReturn(edmProperty);
    return entityType;
  }

  private EdmFunctionImport createFunctionImportMock(final EdmEntityContainer container, final String name, final EdmType type, final EdmMultiplicity multiplicity) throws ODataException {
    EdmTyped returnType = mock(EdmTyped.class);
    when(returnType.getType()).thenReturn(type);
    when(returnType.getMultiplicity()).thenReturn(multiplicity);
    EdmFunctionImport functionImport = mock(EdmFunctionImport.class);
    when(functionImport.getName()).thenReturn(name);
    when(functionImport.getReturnType()).thenReturn(returnType);
    when(container.getFunctionImport(name)).thenReturn(functionImport);
    return functionImport;
  }

  private UriParserResult parse(final String uri) throws ODataException {

    final String[] path = uri.split("\\?", -1);
    if (path.length > 2)
      throw new UriParserException("Wrong URI Syntax");

    final List<PathSegment> pathSegments = getPathSegments(path[0]);
    Map<String, String> queryParameters;
    if (path.length == 2)
      queryParameters = getQueryParameters(unescape(path[1]));
    else
      queryParameters = new HashMap<String, String>();

    return new UriParser(getEdm()).parse(pathSegments, queryParameters);
  }

  private List<PathSegment> getPathSegments(final String uri) throws UriParserException {
    List<PathSegment> pathSegments = new ArrayList<PathSegment>();
    for (final String segment : uri.split("/", -1)) {
      PathSegment pathSegmentMock = mock(PathSegment.class);
      final String unescapedSegment = unescape(segment);
      when(pathSegmentMock.getPath()).thenReturn(unescapedSegment);
      pathSegments.add(pathSegmentMock);
    }
    return pathSegments;
  }

  private Map<String, String> getQueryParameters(final String uri) throws UriParserException {
    Map<String, String> queryParameters = new HashMap<String, String>();
    for (final String option : uri.split("&")) {
      final String[] keyAndValue = option.split("=");
      if (keyAndValue.length == 2)
        queryParameters.put(keyAndValue[0], keyAndValue[1]);
      else
        queryParameters.put(keyAndValue[0], "");
    }
    return queryParameters;
  }

  private String unescape(final String s) throws UriParserException {
    try {
      return new URI(s).getPath();
    } catch (URISyntaxException e) {
      throw new UriParserException("Error while unescaping", e);
    }
  }

  public void parseWrongUri(final String uri) throws Exception {
    try {
      parse(uri);
      fail("Expected ODataException not thrown");
    } catch (ODataException e) {
      assertNotNull(e);
    }
  }

  @Test
  public void parseServiceDocument() throws ODataException {
    UriParserResult result = parse("/");
    assertEquals(UriType.URI0, result.getUriType());

    result = parse("");
    assertEquals(UriType.URI0, result.getUriType());
  }

  @Test
  public void parseMetadata() throws ODataException {
    UriParserResult result = parse("/$metadata");
    assertEquals(UriType.URI8, result.getUriType());
  }

  @Test
  public void parseMetadataError() throws Exception {
    parseWrongUri("/$metadata/somethingwrong");
  }

  @Test
  public void parseBatch() throws ODataException {
    UriParserResult result = parse("/$batch");
    assertEquals(UriType.URI9, result.getUriType());
  }

  @Test
  public void parseBatchError() throws Exception {
    parseWrongUri("/$batch/somethingwrong");
  }

  @Test
  public void parseSomethingEntitySet() throws Exception {
    parseWrongUri("/somethingwrong");
  }

  @Test
  public void parseContainerWithoutEntitySet() throws Exception {
    parseWrongUri("Container1.");
  }

  @Test
  public void parseEmployeesEntitySet() throws ODataException {
    UriParserResult result = parse("/Employees");
    assertNull(result.getEntityContainer().getName());
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertEquals(UriType.URI1, result.getUriType());
  }

  @Test
  public void parseEmployeesEntitySetParenthesesCount() throws ODataException {
    UriParserResult result = parse("/Employees()/$count");
    assertNull(result.getEntityContainer().getName());
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertEquals(UriType.URI15, result.getUriType());
    assertTrue(result.isCount());
  }

  @Test
  public void parseEmployeesEntitySetParenthesesCountNotLast() throws Exception {
    parseWrongUri("/Employees()/$count/somethingwrong");
  }

  @Test
  public void parseEmployeesEntitySetParentheses() throws ODataException {
    UriParserResult result = parse("/Employees()");
    assertNull(result.getEntityContainer().getName());
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertEquals(UriType.URI1, result.getUriType());
  }

  @Test
  public void parseWrongEntities() throws Exception {
    parseWrongUri("//");
    parseWrongUri("/Employ ees('1')");
    parseWrongUri("/Employees()/somethingwrong");
    parseWrongUri("/Employees/somethingwrong");
    parseWrongUri("Employees/");
    parseWrongUri("//Employees");
    parseWrongUri("Employees//");
  }

  @Test
  public void parseEmployeesEntityWithKey() throws ODataException {
    UriParserResult result = parse("/Employees('1')");
    assertNull(result.getEntityContainer().getName());
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertEquals(UriType.URI2, result.getUriType());

    assertEquals(1, result.getKeyPredicates().size());
    assertEquals("1", result.getKeyPredicates().get(0).getLiteral());
    assertEquals("EmployeeId", result.getKeyPredicates().get(0).getProperty().getName());
  }

  @Test
  public void parseEmployeesEntity() throws ODataException {
    UriParserResult result = parse("/Employees('1')");
    assertNull(result.getEntityContainer().getName());
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertEquals(UriType.URI2, result.getUriType());

    assertEquals(1, result.getKeyPredicates().size());
    assertEquals("1", result.getKeyPredicates().get(0).getLiteral());
    assertEquals("EmployeeId", result.getKeyPredicates().get(0).getProperty().getName());
  }

  @Test
  public void parseEmployeesEntityWithExplicitKey() throws ODataException {
    UriParserResult result = parse("/Employees(EmployeeId='1')");
    assertNull(result.getEntityContainer().getName());
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertEquals(UriType.URI2, result.getUriType());

    assertEquals(1, result.getKeyPredicates().size());
    assertEquals("1", result.getKeyPredicates().get(0).getLiteral());
    assertEquals("EmployeeId", result.getKeyPredicates().get(0).getProperty().getName());
  }

  @Test
  public void parseEmployeesEntityWithKeyValue() throws ODataException {
    UriParserResult result = parse("/Employees('1')/$value");
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertTrue(result.getEntitySet().getEntityType().hasStream());
    assertEquals(UriType.URI17, result.getUriType());
    assertTrue(result.isValue());

    assertEquals(1, result.getKeyPredicates().size());
    assertEquals("1", result.getKeyPredicates().get(0).getLiteral());
    assertEquals("EmployeeId", result.getKeyPredicates().get(0).getProperty().getName());
  }

  @Test
  public void parseEmployeesEntityWithKeyCount() throws ODataException {
    UriParserResult result = parse("/Employees('1')/$count");
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertEquals(UriType.URI16, result.getUriType());
    assertTrue(result.isCount());

    assertEquals(1, result.getKeyPredicates().size());
    assertEquals("1", result.getKeyPredicates().get(0).getLiteral());
    assertEquals("EmployeeId", result.getKeyPredicates().get(0).getProperty().getName());
  }

  @Test
  public void parseEmployeesSimpleProperty() throws ODataException {
    UriParserResult result = parse("/Employees('1')/EmployeeName");
    assertNull(result.getEntityContainer().getName());
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertEquals(UriType.URI5, result.getUriType());
    assertEquals("EmployeeName", result.getPropertyPath().get(0).getName());
  }

  @Test
  public void parseEmployeesSimplePropertyValue() throws ODataException {
    UriParserResult result = parse("/Employees('1')/EmployeeName/$value");
    assertNull(result.getEntityContainer().getName());
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertEquals(UriType.URI5, result.getUriType());
    assertEquals("EmployeeName", result.getPropertyPath().get(0).getName());
    assertTrue(result.isValue());
  }

  @Test
  public void parseEmployeesComplexProperty() throws ODataException {
    UriParserResult result = parse("/Employees('1')/Location");
    assertNull(result.getEntityContainer().getName());
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertEquals(UriType.URI3, result.getUriType());
    assertEquals("Location", result.getPropertyPath().get(0).getName());
  }

  @Test
  public void parseEmployeesComplexPropertyWithEntity() throws ODataException {
    UriParserResult result = parse("/Employees('1')/Location/Country");
    assertNull(result.getEntityContainer().getName());
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertEquals(UriType.URI4, result.getUriType());
    assertEquals("Location", result.getPropertyPath().get(0).getName());
    assertEquals("Country", result.getPropertyPath().get(1).getName());
  }

  @Test
  public void parseEmployeesComplexPropertyWithEntityValue() throws ODataException {
    UriParserResult result = parse("/Employees('1')/Location/Country/$value");
    assertNull(result.getEntityContainer().getName());
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertEquals(UriType.URI4, result.getUriType());
    assertEquals("Location", result.getPropertyPath().get(0).getName());
    assertEquals("Country", result.getPropertyPath().get(1).getName());
    assertTrue(result.isValue());
  }

  @Test
  public void simplePropertyWrong() throws Exception {
    parseWrongUri("/Employees('1')/EmployeeName(1)");
    parseWrongUri("/Employees('1')/EmployeeName()");
    parseWrongUri("/Employees('1')/EmployeeName/something");
    parseWrongUri("/Employees('1')/EmployeeName/$value/something");
  }

  @Test
  public void complexPropertyWrong() throws Exception {
    parseWrongUri("/Employees('1')/Location(1)");
    parseWrongUri("/Employees('1')/Location/somethingwrong");
  }

  @Test
  public void EmployeesNoProperty() throws Exception {
    parseWrongUri("/Employees('1')/somethingwrong");
  }

  @Test
  public void parseNavigationPropertyWithEntityResult() throws ODataException {
    UriParserResult result = parse("/Employees('1')/ne_Manager");
    assertEquals("Managers", result.getTargetEntitySet().getName());
    assertEquals(UriType.URI6A, result.getUriType());
  }

  @Test
  public void parseNavigationPropertyWithEntitySetResult() throws ODataException {
    UriParserResult result = parse("/Managers('1')/nm_Employees");
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertEquals(UriType.URI6B, result.getUriType());
  }

  @Test
  public void parseNavigationPropertyWithEntitySetResultParenthesis() throws ODataException {
    UriParserResult result = parse("/Managers('1')/nm_Employees()");
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertEquals(UriType.URI6B, result.getUriType());
  }

  @Test
  public void parseNavigationPropertyWithEntityResultWithKey() throws ODataException {
    UriParserResult result = parse("/Managers('1')/nm_Employees('1')");
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertEquals(UriType.URI6A, result.getUriType());
  }

  @Test
  public void parseNavigationPropertyWithLinksOne() throws ODataException {
    UriParserResult result = parse("/Employees('1')/$links/ne_Manager");
    assertEquals("Managers", result.getTargetEntitySet().getName());
    assertTrue(result.isLinks());
    assertEquals(UriType.URI7A, result.getUriType());
  }

  @Test
  public void parseNavigationPropertyWithLinksMany() throws ODataException {
    UriParserResult result = parse("/Managers('1')/$links/nm_Employees");
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertTrue(result.isLinks());
    assertEquals(UriType.URI7B, result.getUriType());
  }

  @Test
  public void parseNavigationPropertyWithManagersCount() throws ODataException {
    UriParserResult result = parse("/Employees('1')/ne_Manager/$count");
    assertEquals("Managers", result.getTargetEntitySet().getName());
    assertTrue(result.isCount());
    assertEquals(UriType.URI16, result.getUriType());
  }

  @Test
  public void parseNavigationPropertyWithEmployeesCount() throws ODataException {
    UriParserResult result = parse("/Managers('1')/nm_Employees/$count");
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertTrue(result.isCount());
    assertEquals(UriType.URI15, result.getUriType());
  }

  @Test
  public void parseNavigationPropertyWithEmployeeCount() throws ODataException {
    UriParserResult result = parse("Managers('1')/nm_Employees('1')/$count");
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertTrue(result.isCount());
    assertEquals(UriType.URI16, result.getUriType());
  }

  @Test
  public void parseNavigationPropertyWithLinksCountMany() throws ODataException {
    UriParserResult result = parse("/Managers('1')/$links/nm_Employees/$count");
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertTrue(result.isLinks());
    assertTrue(result.isCount());
    assertEquals(UriType.URI50B, result.getUriType());
  }

  @Test
  public void parseNavigationPropertyWithLinksCountOne() throws ODataException {
    UriParserResult result = parse("/Employees('1')/$links/ne_Manager/$count");
    assertEquals("Managers", result.getTargetEntitySet().getName());
    assertTrue(result.isLinks());
    assertTrue(result.isCount());
    assertEquals(UriType.URI50A, result.getUriType());

    result = parse("/Managers('1')/$links/nm_Employees('1')/$count");
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertTrue(result.isLinks());
    assertTrue(result.isCount());
    assertEquals(UriType.URI50A, result.getUriType());
  }

  @Test
  public void navigationPropertyWrong() throws Exception {
    parseWrongUri("Employees('1')//ne_Manager");
    parseWrongUri("Employees('1')/ne_Manager()");
    parseWrongUri("Employees('1')/ne_Manager('1')");
    parseWrongUri("Employees('1')/$links");
    parseWrongUri("Employees('1')/$links/ne_Manager('1')");
    parseWrongUri("Employees('1')/$links/ne_Manager()");
    parseWrongUri("Employees('1')/$links/ne_Manager/somethingwrong");
    parseWrongUri("Employees('1')/ne_Manager/$count/somethingwrong");
    parseWrongUri("Employees('1')/$links/ne_Manager/$count/somethingwrong");
    parseWrongUri("Employees('1')/ne_Manager/$value");
    parseWrongUri("Managers('1')/nm_Employees('1')/$value/somethingwrong");
    parseWrongUri("Managers('1')/nm_Employees/$links");
    parseWrongUri("Managers('1')/nm_Employees/$links/Manager");
    parseWrongUri("Managers('1')/nm_Employees/somethingwrong");
    parseWrongUri("Employees('1')/$links/somethingwrong");
    parseWrongUri("Employees('1')/$links/EmployeeName");
    parseWrongUri("Employees('1')/$links/$links/ne_Manager");
    parseWrongUri("Managers('1')/nm_Employee/");
  }

  @Test
  public void navigationPathWrongMatch() throws Exception {
    parseWrongUri("/Employees('1')/(somethingwrong(");

  }

  @Test
  public void navigationSegmentWrongMatch() throws Exception {
    parseWrongUri("/Employees('1')/$links/(somethingwrong(");

  }

  @Test
  public void parseTeamsEntityWithIntKeyValue() throws Exception {
    parseWrongUri("/Teams(1)/$value");
  }

  @Test
  public void parseWrongKey() throws Exception {
    parseWrongUri("/Employees(EmployeeId='1',somethingwrong=abc)");
    parseWrongUri("/Employees(somethingwrong=1)");
    parseWrongUri("/Container2.Photos(Id=1;Type='abc')");
    parseWrongUri("Container2.Photos(Id=1,'abc')");
    parseWrongUri("Container2.Photos(Id=1)");
  }

  @Test
  public void parsePhotoEntityWithExplicitKeySet() throws ODataException {
    UriParserResult result = parse("/Container2.Photos(Id=1,Type='abc')");
    assertEquals("Container2", result.getEntityContainer().getName());
    assertEquals("Photos", result.getTargetEntitySet().getName());
    assertEquals(UriType.URI2, result.getUriType());
    assertEquals(2, result.getKeyPredicates().size());
    assertEquals("1", result.getKeyPredicates().get(0).getLiteral());
    assertEquals("Id", result.getKeyPredicates().get(0).getProperty().getName());
    assertEquals("abc", result.getKeyPredicates().get(1).getLiteral());
    assertEquals("Type", result.getKeyPredicates().get(1).getProperty().getName());

    result = parse("/Container2.Photos(Id=1,Type='abc%20xyz')");
    assertEquals("abc xyz", result.getKeyPredicates().get(1).getLiteral());

    result = parse("/Container2.Photos(Id=1,Type='image%2Fpng')");
    assertEquals("image/png", result.getKeyPredicates().get(1).getLiteral());
  }

  @Test
  public void parseContainerEmployeesEntitySet() throws ODataException {
    UriParserResult result = parse("/Container1.Employees");
    assertEquals("Container1", result.getEntityContainer().getName());
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertEquals(UriType.URI1, result.getUriType());
  }

  @Test
  public void parseContainerEmployeesEntitySetParenthese() throws ODataException {
    UriParserResult result = parse("/Container1.Employees()");
    assertEquals("Container1", result.getEntityContainer().getName());
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertEquals(UriType.URI1, result.getUriType());
  }

  @Test
  public void parseContainerEmployeesEntityWithKey() throws ODataException {
    UriParserResult result = parse("/Container1.Employees('1')");
    assertEquals("Container1", result.getEntityContainer().getName());
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertEquals(UriType.URI2, result.getUriType());

    assertEquals(1, result.getKeyPredicates().size());
    assertEquals("1", result.getKeyPredicates().get(0).getLiteral());
    assertEquals("EmployeeId", result.getKeyPredicates().get(0).getProperty().getName());
  }

  @Test
  public void parseNonexistentContainer() throws Exception {
    parseWrongUri("/somethingwrong.Employees()");
  }

  @Test
  public void parseInvalidSegment() throws Exception {
    parseWrongUri("/.somethingwrong");
  }

  /**
   * Parse an URI string with an entity set and a single-value key
   * and assert that it has the correct parsed type and value.
   * 
   * @param uri
   *          the URI to be parsed as string; it must refer to an entity with one key property
   * @param type
   *          the expected {@link EdmSimpleType} of the key property
   * @param expectedLiteral
   *          the expected literal value of the key
   * @throws UriParserException
   */
  private void parseOneKey(final String uri, final EdmType type, final String expectedLiteral) throws ODataException {
    final UriParserResult result = parse(uri);
    final KeyPredicate keyPredicate = result.getKeyPredicates().get(0);

    assertEquals(type, keyPredicate.getProperty().getType());
    assertEquals(expectedLiteral, keyPredicate.getLiteral());
  }

  @Test
  public void parseDecimalKey() throws ODataException {
    parseOneKey("/Decimals(4.5m)", EdmSimpleType.DECIMAL, "4.5");
    parseOneKey("/Decimals(4.5M)", EdmSimpleType.DECIMAL, "4.5");
    parseOneKey("/Decimals(1)", EdmSimpleType.DECIMAL, "1");
    parseOneKey("/Decimals(255)", EdmSimpleType.DECIMAL, "255");
    parseOneKey("/Decimals(-32768)", EdmSimpleType.DECIMAL, "-32768");
    parseOneKey("/Decimals(32768)", EdmSimpleType.DECIMAL, "32768");
    parseOneKey("/Decimals(3000000)", EdmSimpleType.DECIMAL, "3000000");
    parseOneKey("/Decimals(4.5d)", EdmSimpleType.DECIMAL, "4.5");
    parseOneKey("/Decimals(4.2E9F)", EdmSimpleType.DECIMAL, "4.2E9");
    parseOneKey("/Decimals(1234567890L)", EdmSimpleType.DECIMAL, "1234567890");
  }

  @Test
  public void parseInt16Key() throws ODataException {
    parseOneKey("/Int16s(16)", EdmSimpleType.INT16, "16");
    parseOneKey("/Int16s(-16)", EdmSimpleType.INT16, "-16");
    parseOneKey("/Int16s(255)", EdmSimpleType.INT16, "255");
    parseOneKey("/Int16s(-32768)", EdmSimpleType.INT16, "-32768");
  }

  @Test
  public void parseInt32Key() throws ODataException {
    parseOneKey("/Int32s(32)", EdmSimpleType.INT32, "32");
    parseOneKey("/Int32s(-127)", EdmSimpleType.INT32, "-127");
    parseOneKey("/Int32s(255)", EdmSimpleType.INT32, "255");
    parseOneKey("/Int32s(32767)", EdmSimpleType.INT32, "32767");
    parseOneKey("/Int32s(-32769)", EdmSimpleType.INT32, "-32769");
  }

  @Test
  public void parseInt64Key() throws ODataException {
    parseOneKey("/Int64s(64)", EdmSimpleType.INT64, "64");
    parseOneKey("/Int64s(255)", EdmSimpleType.INT64, "255");
    parseOneKey("/Int64s(1000)", EdmSimpleType.INT64, "1000");
    parseOneKey("/Int64s(100000)", EdmSimpleType.INT64, "100000");
    parseOneKey("/Int64s(-64L)", EdmSimpleType.INT64, "-64");
    parseOneKey("/Int64s(" + Long.MAX_VALUE + "L)", EdmSimpleType.INT64, "" + Long.MAX_VALUE);
    parseOneKey("/Int64s(" + Long.MIN_VALUE + "l)", EdmSimpleType.INT64, "" + Long.MIN_VALUE);
  }

  @Test
  public void parseStringKey() throws ODataException {
    parseOneKey("/Strings('abc')", EdmSimpleType.STRING, "abc");
    parseOneKey("/Strings('abc%20xyz')", EdmSimpleType.STRING, "abc xyz");
    parseOneKey("/Strings('true')", EdmSimpleType.STRING, "true");
    parseOneKey("/Strings('')", EdmSimpleType.STRING, "");
  }

  @Test
  public void parseSingleKey() throws ODataException {
    parseOneKey("/Singles(45)", EdmSimpleType.SINGLE, "45");
    parseOneKey("/Singles(255)", EdmSimpleType.SINGLE, "255");
    parseOneKey("/Singles(-32768)", EdmSimpleType.SINGLE, "-32768");
    parseOneKey("/Singles(32768)", EdmSimpleType.SINGLE, "32768");
    parseOneKey("/Singles(1L)", EdmSimpleType.SINGLE, "1");
    parseOneKey("/Singles(4.5f)", EdmSimpleType.SINGLE, "4.5");
    parseOneKey("/Singles(4.5F)", EdmSimpleType.SINGLE, "4.5");
    parseOneKey("/Singles(4.5e9f)", EdmSimpleType.SINGLE, "4.5e9");
  }

  @Test
  public void parseDoubleKey() throws ODataException {
    parseOneKey("/Doubles(45)", EdmSimpleType.DOUBLE, "45");
    parseOneKey("/Doubles(255)", EdmSimpleType.DOUBLE, "255");
    parseOneKey("/Doubles(-32768)", EdmSimpleType.DOUBLE, "-32768");
    parseOneKey("/Doubles(32768)", EdmSimpleType.DOUBLE, "32768");
    parseOneKey("/Doubles(1l)", EdmSimpleType.DOUBLE, "1");
    parseOneKey("/Doubles(4.5d)", EdmSimpleType.DOUBLE, "4.5");
    parseOneKey("/Doubles(4.5D)", EdmSimpleType.DOUBLE, "4.5");
    parseOneKey("/Doubles(4.5e21f)", EdmSimpleType.DOUBLE, "4.5e21");
  }

  @Test
  public void parseByteKey() throws ODataException {
    parseOneKey("/Bytes(255)", EdmSimpleType.BYTE, "255");
    parseOneKey("Bytes(123)", EdmSimpleType.BYTE, "123");
  }

  @Test
  public void parseGuidKey() throws ODataException {
    parseOneKey("/Guids(guid'1225c695-cfb8-4ebb-aaaa-80da344efa6a')", EdmSimpleType.GUID, "1225c695-cfb8-4ebb-aaaa-80da344efa6a");
  }

  @Test
  public void parseTimeKey() throws ODataException {
    parseOneKey("/Times(time'P120D')", EdmSimpleType.TIME, "P120D");
  }

  @Test
  public void parseDatetimeKey() throws ODataException {
    parseOneKey("/DateTimes(datetime'2009-12-26T21%3A23%3A38')", EdmSimpleType.DATETIME, "2009-12-26T21:23:38");
    parseOneKey("/DateTimes(datetime'2009-12-26T21%3A23%3A38Z')", EdmSimpleType.DATETIME, "2009-12-26T21:23:38Z");
  }

  @Test
  public void parseDatetimeOffsetKey() throws ODataException {
    parseOneKey("/DateTimeOffsets(datetimeoffset'2009-12-26T21%3A23%3A38Z')", EdmSimpleType.DATETIMEOFFSET, "2009-12-26T21:23:38Z");
    parseOneKey("/DateTimeOffsets(datetimeoffset'2002-10-10T12%3A00%3A00-05%3A00')", EdmSimpleType.DATETIMEOFFSET, "2002-10-10T12:00:00-05:00");
  }

  @Test
  public void parseBooleanKey() throws ODataException {
    parseOneKey("/Booleans(true)", EdmSimpleType.BOOLEAN, "true");
    parseOneKey("/Booleans(false)", EdmSimpleType.BOOLEAN, "false");
    parseOneKey("/Booleans(1)", EdmSimpleType.BOOLEAN, "1");
    parseOneKey("/Booleans(0)", EdmSimpleType.BOOLEAN, "0");
  }

  @Test
  public void parseSByteKey() throws ODataException {
    parseOneKey("/SBytes(-123)", EdmSimpleType.SBYTE, "-123");
    parseOneKey("/SBytes(12)", EdmSimpleType.SBYTE, "12");
  }

  @Test
  public void parseBinaryKey() throws ODataException {
    parseOneKey("/Binaries(X'Fa12aAA1')", EdmSimpleType.BINARY, "+hKqoQ==\r\n");
    parseOneKey("/Binaries(binary'FA12AAA1')", EdmSimpleType.BINARY, "+hKqoQ==\r\n");
  }

  @Test
  public void parseKeyWithWrongContent() throws Exception {
    parseWrongUri("/Binaries(binary'abcde')");
    parseWrongUri("Strings(')");
    parseWrongUri("Strings('a)");
    parseWrongUri("Times(wrongprefix'PT1H2M3S')");
    parseWrongUri("/Int32s(32i)");
    parseWrongUri("Int32s(12345678901234567890)");
  }

  @Test
  public void parseIncompatibleKeys() throws Exception {
    parseWrongUri("Binaries(1D)");
    parseWrongUri("Booleans('0')");
    parseWrongUri("Booleans('1')");
    parseWrongUri("Booleans(2)");
    parseWrongUri("Bytes(-1)");
    parseWrongUri("Bytes(-129)");
    parseWrongUri("DateTimes(time'PT11H12M13S')");
    parseWrongUri("DateTimeOffsets(time'PT11H12M13S')");
    parseWrongUri("Decimals('1')");
    parseWrongUri("Doubles(1M)");
    parseWrongUri("Guids(1)");
    parseWrongUri("Int16s(32768)");
    parseWrongUri("Int32s(1L)");
    parseWrongUri("Int64s(1M)");
    parseWrongUri("SBytes(128)");
    parseWrongUri("Singles(1D)");
    parseWrongUri("Strings(1)");
    parseWrongUri("Times(datetime'2012-10-10T11:12:13')");
  }

  @Test
  public void parseFunctionImports() throws Exception {
    UriParserResult result = parse("EmployeeSearch");
    assertEquals("EmployeeSearch", result.getFunctionImport().getName());
    assertEquals(EdmTypeKind.ENTITY, result.getTargetType().getKind());
    assertEquals(UriType.URI1, result.getUriType());

    result = parse("AllLocations");
    assertEquals("AllLocations", result.getFunctionImport().getName());
    assertEquals(UriType.URI11, result.getUriType());

    result = parse("AllUsedRoomIds");
    assertEquals("AllUsedRoomIds", result.getFunctionImport().getName());
    assertEquals(UriType.URI13, result.getUriType());

    result = parse("MaximalAge");
    assertEquals("MaximalAge", result.getFunctionImport().getName());
    assertEquals(UriType.URI14, result.getUriType());

    result = parse("MaximalAge/$value");
    assertEquals("MaximalAge", result.getFunctionImport().getName());
    assertTrue(result.isValue());
    assertEquals(UriType.URI14, result.getUriType());

    result = parse("MostCommonLocation");
    assertEquals("MostCommonLocation", result.getFunctionImport().getName());
    assertEquals(UriType.URI12, result.getUriType());

    result = parse("ManagerPhoto?Id='1'");
    assertEquals("ManagerPhoto", result.getFunctionImport().getName());
    assertEquals(UriType.URI14, result.getUriType());

    result = parse("OldestEmployee");
    assertEquals("OldestEmployee", result.getFunctionImport().getName());
    assertEquals(UriType.URI10, result.getUriType());
  }

  @Test
  public void parseFunctionImportParameters() throws Exception {
    UriParserResult result = parse("EmployeeSearch?q='Hugo'&notaparameter=2");
    assertEquals("EmployeeSearch", result.getFunctionImport().getName());
    assertEquals(1, result.getFunctionImportParameters().size());
    assertEquals(EdmSimpleType.STRING, result.getFunctionImportParameters().get("q").getType());
    assertEquals("Hugo", result.getFunctionImportParameters().get("q").getLiteral());
  }

  @Test
  public void parseWrongFunctionImports() throws Exception {
    parseWrongUri("EmployeeSearch?q=42");
    parseWrongUri("AllLocations/$value");
    parseWrongUri("MaximalAge()");
    parseWrongUri("MaximalAge/somethingwrong");
    parseWrongUri("ManagerPhoto");
    parseWrongUri("ManagerPhoto?Id='");
  }

  @Test
  public void parseSystemQueryOptions() throws Exception {
    UriParserResult result = parse("Employees?$format=json&$inlinecount=allpages&$skiptoken=abc&$skip=2&$top=1");
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertEquals(UriType.URI1, result.getUriType());
    assertEquals(Format.JSON, result.getFormat());
    assertEquals(InlineCount.ALLPAGES, result.getInlineCount());
    assertEquals("abc", result.getSkipToken());
    assertEquals(2, result.getSkip());
    assertEquals(1, result.getTop().intValue());

    result = parse("Employees?$format=atom&$inlinecount=none&$top=0");
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertEquals(UriType.URI1, result.getUriType());
    assertEquals(Format.ATOM, result.getFormat());
    assertEquals(InlineCount.NONE, result.getInlineCount());
    assertEquals(0, result.getTop().intValue());

    result = parse("Employees?$format=json&$inlinecount=none");
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertEquals(UriType.URI1, result.getUriType());
    assertEquals(Format.JSON, result.getFormat());
    assertEquals(InlineCount.NONE, result.getInlineCount());

    result = parse("Employees?$format=atom");
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertEquals(UriType.URI1, result.getUriType());
    assertEquals(Format.ATOM, result.getFormat());

    result = parse("Employees?$format=xml");
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertEquals(UriType.URI1, result.getUriType());
    assertEquals(Format.XML, result.getFormat());
    assertNull(result.getTop());

    result = parse("Employees?$format=xml&$format=json");
    assertEquals(Format.JSON, result.getFormat());

    result = parse("Employees?$format=custom");
    assertNull(result.getFormat());
    assertEquals("custom", result.getCustomFormat());

    result = parse("/Employees('1')/Location/Country?$format=json");
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertEquals(UriType.URI4, result.getUriType());
    assertEquals(Format.JSON, result.getFormat());

    result = parse("/Employees('1')/EmployeeName?$format=json");
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertEquals(UriType.URI5, result.getUriType());
    assertEquals(Format.JSON, result.getFormat());

    result = parse("Employees?$filter=Age%20gt%2020&$orderby=EmployeeName%20desc");
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertEquals(UriType.URI1, result.getUriType());
    assertNotNull(result.getFilter());
    assertNotNull(result.getOrderBy());
  }

  @Test
  public void parseWrongSystemQueryOptions() throws Exception {
    parseWrongUri("Employees??");
    parseWrongUri("Employees?$inlinecount=no");
    parseWrongUri("Employees?&$skiptoken==");
    parseWrongUri("Employees?$skip=-1");
    parseWrongUri("Employees?$skip='a'");
    parseWrongUri("Employees?$top=-1");
    parseWrongUri("Employees?$top=12345678901234567890");
    parseWrongUri("Employees?$somethingwrong");
    parseWrongUri("Employees?$somethingwrong=");
    parseWrongUri("Employees?$somethingwrong=adjaodjai");
    parseWrongUri("Employees?$formatformat=xml");
    parseWrongUri("Employees?$Format=atom");
  }

  @Test
  public void parseWrongSystemQueryOptionInitialValues() throws Exception {
    parseWrongUri("Employees?$expand=");
    parseWrongUri("Employees?$filter=");
    parseWrongUri("Employees?$orderby=");
    parseWrongUri("Employees?$format=");
    parseWrongUri("Employees?$skip=");
    parseWrongUri("Employees?$top=");
    parseWrongUri("Employees?$skiptoken=");
    parseWrongUri("Employees?$inlinecount=");
    parseWrongUri("Employees?$select=");

    parseWrongUri("Employees?$expand");
    parseWrongUri("Employees?$filter");
    parseWrongUri("Employees?$orderby");
    parseWrongUri("Employees?$format");
    parseWrongUri("Employees?$skip");
    parseWrongUri("Employees?$top");
    parseWrongUri("Employees?$skiptoken");
    parseWrongUri("Employees?$inlinecount");
    parseWrongUri("Employees?$select");
  }

  @Test
  public void parseCompatibleSystemQueryOptions() throws Exception {
    UriParserResult result = parse("Employees?$format=json&$inlinecount=allpages&$skiptoken=abc&$skip=2&$top=1");
    assertEquals("Employees", result.getEntitySet().getName());
    assertEquals(UriType.URI1, result.getUriType());
    assertEquals(Format.JSON, result.getFormat());
    assertEquals(InlineCount.ALLPAGES, result.getInlineCount());
    assertEquals("abc", result.getSkipToken());
    assertEquals(2, result.getSkip());
    assertEquals(1, result.getTop().intValue());
  }

  @Test
  public void parseInCompatibleSystemQueryOptions() throws Exception {
    parseWrongUri("$metadata?$top=1");
    parseWrongUri("Employees('1')?$format=json&$inlinecount=allpages&$skiptoken=abc&$skip=2&$top=1");
    parseWrongUri("/Employees('1')/Location/Country/$value?$format=json");
    parseWrongUri("/Employees('1')/Location/Country/$value?$skip=2");
    parseWrongUri("/Employees('1')/EmployeeName/$value?$format=json");
    parseWrongUri("/Employees('1')/EmployeeName/$value?$skip=2");
  }

  @Test
  public void parsePossibleQueryOptions() throws Exception {
    UriParserResult result = parse("EmployeeSearch?q='a'&sap-client=100&sap-ds-debug=true");
    assertEquals(2, result.getCustomQueryOptions().size());
    assertEquals("100", result.getCustomQueryOptions().get("sap-client"));
    assertEquals("true", result.getCustomQueryOptions().get("sap-ds-debug"));
  }

  @Test
  public void parseSystemQueryOptionSelectSingle() throws Exception {
    UriParserResult result = parse("Employees?$select=EmployeeName");
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertEquals(UriType.URI1, result.getUriType());
    assertEquals(1, result.getSelect().size());
    assertEquals("EmployeeName", result.getSelect().get(0).getProperty().getName());

    result = parse("Employees?$select=*");
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertEquals(UriType.URI1, result.getUriType());
    assertEquals(1, result.getSelect().size());
    assertTrue(result.getSelect().get(0).isStar());

    result = parse("Employees?$select=Location");
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertEquals(UriType.URI1, result.getUriType());
    assertEquals(1, result.getSelect().size());
    assertEquals("Location", result.getSelect().get(0).getProperty().getName());

    result = parse("Employees?$select=ne_Manager");
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertEquals(UriType.URI1, result.getUriType());
    assertEquals(1, result.getSelect().size());
    assertEquals(1, result.getSelect().get(0).getNavigationPropertySegments().size());
    assertEquals("Managers", result.getSelect().get(0).getNavigationPropertySegments().get(0).getTargetEntitySet().getName());

    result = parse("Teams?$select=nt_Employees/ne_Manager/*");
    assertEquals(1, result.getSelect().size());
    assertEquals(2, result.getSelect().get(0).getNavigationPropertySegments().size());
    assertTrue(result.getSelect().get(0).isStar());
  }

  @Test
  public void parseSystemQueryOptionSelectMultiple() throws Exception {
    UriParserResult result = parse("Employees?$select=EmployeeName,Location");
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertEquals(UriType.URI1, result.getUriType());
    assertEquals(2, result.getSelect().size());
    assertEquals("EmployeeName", result.getSelect().get(0).getProperty().getName());
    assertEquals("Location", result.getSelect().get(1).getProperty().getName());

    result = parse("Employees?$select=%20ne_Manager,%20EmployeeName,%20Location");
    assertEquals("Employees", result.getTargetEntitySet().getName());
    assertEquals(UriType.URI1, result.getUriType());
    assertEquals(3, result.getSelect().size());
    assertEquals("EmployeeName", result.getSelect().get(1).getProperty().getName());
    assertEquals("Location", result.getSelect().get(2).getProperty().getName());
    assertEquals(1, result.getSelect().get(0).getNavigationPropertySegments().size());
    assertEquals("Managers", result.getSelect().get(0).getNavigationPropertySegments().get(0).getTargetEntitySet().getName());

    result = parse("Managers('1')?$select=nm_Employees/EmployeeName,nm_Employees/Location");
    assertEquals("Managers", result.getTargetEntitySet().getName());
    assertEquals(UriType.URI2, result.getUriType());
    assertEquals(2, result.getSelect().size());
    assertEquals("EmployeeName", result.getSelect().get(0).getProperty().getName());
    assertEquals("Location", result.getSelect().get(1).getProperty().getName());
    assertEquals(1, result.getSelect().get(0).getNavigationPropertySegments().size());
    assertEquals("Employees", result.getSelect().get(0).getNavigationPropertySegments().get(0).getTargetEntitySet().getName());

  }

  @Test
  public void parseSystemQueryOptionSelectNegative() throws Exception {
    parseWrongUri("Employees?$select=somethingwrong");
    parseWrongUri("Employees?$select=*/Somethingwrong");
    parseWrongUri("Employees?$select=EmployeeName/*");
    parseWrongUri("Employees?$select=,EmployeeName");
    parseWrongUri("Employees?$select=EmployeeName,");
    parseWrongUri("Employees?$select=EmployeeName,,Location");
    parseWrongUri("Employees?$select=*EmployeeName");
    parseWrongUri("Employees?$select=EmployeeName*");
    parseWrongUri("Employees?$select=/EmployeeName");
    parseWrongUri("Employees?$select=EmployeeName/");
    parseWrongUri("Teams('1')?$select=nt_Employees/Id");
    parseWrongUri("Teams('1')?$select=nt_Employees//*");
  }

  @Test
  public void parseSystemQueryOptionExpand() throws Exception {
    UriParserResult result = parse("Managers('1')?$expand=nm_Employees");
    assertEquals("Managers", result.getTargetEntitySet().getName());
    assertEquals(UriType.URI2, result.getUriType());
    assertEquals(1, result.getExpand().size());
    assertEquals(1, result.getExpand().get(0).size());
    assertEquals("Employees", result.getExpand().get(0).get(0).getTargetEntitySet().getName());
    assertEquals(result.getTargetEntitySet().getEntityType().getProperty("nm_Employees"), result.getExpand().get(0).get(0).getNavigationProperty());
  }

  @Test
  public void parseSystemQueryOptionExpandWrong() throws Exception {
    parseWrongUri("Managers('1')?$expand=,nm_Employees");
    parseWrongUri("Managers('1')?$expand=nm_Employees,");
    parseWrongUri("Managers('1')?$expand=nm_Employees,,");
    parseWrongUri("Managers('1')?$expand=nm_Employees,,nm_Employees");
    parseWrongUri("Managers('1')?$expand=nm_Employees, somethingwrong");
    parseWrongUri("Managers('1')?$expand=/nm_Employees");
    parseWrongUri("Managers('1')?$expand=nm_Employees/");
    parseWrongUri("Managers('1')?$expand=nm_Employees//");
    parseWrongUri("Managers('1')?$expand=somethingwrong");
    parseWrongUri("Managers('1')?$expand=nm_Employees/EmployeeName");
    parseWrongUri("Managers('1')?$expand=nm_Employees/somethingwrong");
    parseWrongUri("Managers('1')?$expand=nm_Employees/*");
    parseWrongUri("Managers('1')?$expand=nm_Employees/*,somethingwrong");
    parseWrongUri("Managers('1')?$expand=nm_Employees/*,some()");
    parseWrongUri("Managers('1')?$expand=nm_Employees/(...)");
    parseWrongUri("Teams('1')?$expand=nt_Employees//ne_Manager");
  }

}