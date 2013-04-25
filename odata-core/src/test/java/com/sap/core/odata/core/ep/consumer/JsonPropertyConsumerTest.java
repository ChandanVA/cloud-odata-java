package com.sap.core.odata.core.ep.consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import com.google.gson.stream.JsonReader;
import com.sap.core.odata.api.edm.EdmComplexType;
import com.sap.core.odata.api.edm.EdmProperty;
import com.sap.core.odata.api.edm.EdmSimpleTypeKind;
import com.sap.core.odata.api.ep.EntityProviderException;
import com.sap.core.odata.core.ep.aggregator.EntityComplexPropertyInfo;
import com.sap.core.odata.core.ep.aggregator.EntityInfoAggregator;
import com.sap.core.odata.core.ep.aggregator.EntityPropertyInfo;
import com.sap.core.odata.testutil.fit.BaseTest;
import com.sap.core.odata.testutil.mock.MockFacade;

public class JsonPropertyConsumerTest extends BaseTest {

  @Test
  public void allNumberSimplePropertyKinds() throws Exception {
    String simplePropertyJson = "{\"d\":{\"Age\":67}}";
    EdmProperty edmProperty = mock(EdmProperty.class);
    when(edmProperty.getName()).thenReturn("Age");
    when(edmProperty.isSimple()).thenReturn(true);

    //Byte
    JsonReader reader = prepareReader(simplePropertyJson);
    when(edmProperty.getType()).thenReturn(EdmSimpleTypeKind.Byte.getEdmSimpleTypeInstance());
    Map<String, Object> resultMap = execute(edmProperty, reader);
    assertEquals(Short.valueOf("67"), resultMap.get("Age"));

    //SByte
    reader = prepareReader(simplePropertyJson);
    when(edmProperty.getType()).thenReturn(EdmSimpleTypeKind.SByte.getEdmSimpleTypeInstance());
    resultMap = execute(edmProperty, reader);
    assertEquals(Byte.valueOf("67"), resultMap.get("Age"));
    //Int16
    reader = prepareReader(simplePropertyJson);
    when(edmProperty.getType()).thenReturn(EdmSimpleTypeKind.Int16.getEdmSimpleTypeInstance());
    resultMap = execute(edmProperty, reader);
    assertEquals(Short.valueOf("67"), resultMap.get("Age"));
    //Int32
    reader = prepareReader(simplePropertyJson);
    when(edmProperty.getType()).thenReturn(EdmSimpleTypeKind.Int32.getEdmSimpleTypeInstance());
    resultMap = execute(edmProperty, reader);
    assertEquals(Integer.valueOf("67"), resultMap.get("Age"));
  }

  @Test
  public void allStringSimplePropertyKinds() throws Exception {
    EdmProperty edmProperty = mock(EdmProperty.class);
    when(edmProperty.getName()).thenReturn("Name");
    when(edmProperty.isSimple()).thenReturn(true);
    String simplePropertyJson;

    //DateTime
    simplePropertyJson = "{\"d\":{\"Name\":\"\\/Date(915148800000)\\/\"}}";
    JsonReader reader = prepareReader(simplePropertyJson);
    when(edmProperty.getType()).thenReturn(EdmSimpleTypeKind.DateTime.getEdmSimpleTypeInstance());
    Map<String, Object> resultMap = execute(edmProperty, reader);
    Calendar entryDate = (Calendar) resultMap.get("Name");
    assertEquals(Long.valueOf(915148800000l), Long.valueOf(entryDate.getTimeInMillis()));
    //DateTimeOffset
    simplePropertyJson = "{\"d\":{\"Name\":\"\\/Date(915148800000)\\/\"}}";
    reader = prepareReader(simplePropertyJson);
    when(edmProperty.getType()).thenReturn(EdmSimpleTypeKind.DateTime.getEdmSimpleTypeInstance());
    resultMap = execute(edmProperty, reader);
    entryDate = (Calendar) resultMap.get("Name");
    assertEquals(Long.valueOf(915148800000l), Long.valueOf(entryDate.getTimeInMillis()));
    //Decimal
    simplePropertyJson = "{\"d\":{\"Name\":\"123456789\"}}";
    reader = prepareReader(simplePropertyJson);
    when(edmProperty.getType()).thenReturn(EdmSimpleTypeKind.Decimal.getEdmSimpleTypeInstance());
    resultMap = execute(edmProperty, reader);
    assertEquals(BigDecimal.valueOf(Long.valueOf("123456789")), resultMap.get("Name"));
    //Double
    simplePropertyJson = "{\"d\":{\"Name\":\"123456789\"}}";
    reader = prepareReader(simplePropertyJson);
    when(edmProperty.getType()).thenReturn(EdmSimpleTypeKind.Double.getEdmSimpleTypeInstance());
    resultMap = execute(edmProperty, reader);
    assertEquals(Double.valueOf("123456789"), resultMap.get("Name"));
    //Int64
    simplePropertyJson = "{\"d\":{\"Name\":\"123456789\"}}";
    reader = prepareReader(simplePropertyJson);
    when(edmProperty.getType()).thenReturn(EdmSimpleTypeKind.Int64.getEdmSimpleTypeInstance());
    resultMap = execute(edmProperty, reader);
    assertEquals(Long.valueOf("123456789"), resultMap.get("Name"));
    //Single
    simplePropertyJson = "{\"d\":{\"Name\":\"123456\"}}";
    reader = prepareReader(simplePropertyJson);
    when(edmProperty.getType()).thenReturn(EdmSimpleTypeKind.Single.getEdmSimpleTypeInstance());
    resultMap = execute(edmProperty, reader);
    assertEquals(Float.valueOf("123456"), resultMap.get("Name"));
    //String
    simplePropertyJson = "{\"d\":{\"Name\":\"123456789\"}}";
    reader = prepareReader(simplePropertyJson);
    when(edmProperty.getType()).thenReturn(EdmSimpleTypeKind.String.getEdmSimpleTypeInstance());
    resultMap = execute(edmProperty, reader);
    assertEquals("123456789", resultMap.get("Name"));
    //Guid
    simplePropertyJson = "{\"d\":{\"Name\":\"AABBCCDD-AABB-CCDD-EEFF-AABBCCDDEEFF\"}}";
    reader = prepareReader(simplePropertyJson);
    when(edmProperty.getType()).thenReturn(EdmSimpleTypeKind.Guid.getEdmSimpleTypeInstance());
    resultMap = execute(edmProperty, reader);
    assertEquals(UUID.fromString("aabbccdd-aabb-ccdd-eeff-aabbccddeeff"), resultMap.get("Name"));
    //Binary
    byte[] binary = new byte[] { (byte) 0xAA, (byte) 0xBB, (byte) 0xCC };
    simplePropertyJson = "{\"d\":{\"Name\":\"qrvM\"}}";
    reader = prepareReader(simplePropertyJson);
    when(edmProperty.getType()).thenReturn(EdmSimpleTypeKind.Binary.getEdmSimpleTypeInstance());
    resultMap = execute(edmProperty, reader);
    //TODO: check why byte is comming back insted of Byte object
    //Byte[] expectedArray = new Byte[] { binary[0], binary[1], binary[2] };
    byte[] actualArray = (byte[]) resultMap.get("Name");
    assertTrue(Arrays.equals(binary, actualArray));
    //Time
    simplePropertyJson = "{\"d\":{\"Name\":\"PT23H32M3S\"}}";
    reader = prepareReader(simplePropertyJson);
    when(edmProperty.getType()).thenReturn(EdmSimpleTypeKind.Time.getEdmSimpleTypeInstance());
    resultMap = execute(edmProperty, reader);
    Calendar dateTime = Calendar.getInstance();
    dateTime.clear();
    dateTime.set(Calendar.HOUR_OF_DAY, 23);
    dateTime.set(Calendar.MINUTE, 32);
    dateTime.set(Calendar.SECOND, 3);
    assertEquals(dateTime, resultMap.get("Name"));
  }

  @Test
  public void simplePropertyOnOpenReader() throws Exception {
    String simplePropertyJson = "{\"Name\":\"Team 1\"}";
    JsonReader reader = prepareReader(simplePropertyJson);
    EdmProperty edmProperty = (EdmProperty) MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams").getEntityType().getProperty("Name");
    EntityPropertyInfo entityPropertyInfo = EntityInfoAggregator.create(edmProperty);
    reader.beginObject();
    reader.nextName();

    JsonPropertyConsumer jpc = new JsonPropertyConsumer();
    Object value = jpc.readProperty(reader, entityPropertyInfo, null);
    assertEquals("Team 1", value);
  }

  @Test
  public void simplePropertyWithNullMappingStandalone() throws Exception {
    String simplePropertyJson = "{\"d\":{\"Age\":67}}";
    JsonReader reader = prepareReader(simplePropertyJson);
    final EdmProperty edmProperty = (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("Age");

    Map<String, Object> typeMappings = null;
    JsonPropertyConsumer jpc = new JsonPropertyConsumer();
    Map<String, Object> resultMap = jpc.readPropertyStandalone(reader, edmProperty, typeMappings);

    assertEquals(Integer.valueOf(67), resultMap.get("Age"));
  }

  @Test
  public void simplePropertyWithEmptyMappingStandalone() throws Exception {
    String simplePropertyJson = "{\"d\":{\"Age\":67}}";
    JsonReader reader = prepareReader(simplePropertyJson);
    final EdmProperty edmProperty = (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("Age");

    Map<String, Object> typeMappings = new HashMap<String, Object>();
    JsonPropertyConsumer jpc = new JsonPropertyConsumer();
    Map<String, Object> resultMap = jpc.readPropertyStandalone(reader, edmProperty, typeMappings);

    assertEquals(Integer.valueOf(67), resultMap.get("Age"));
  }

  @Test
  public void simplePropertyWithStringToLongMappingStandalone() throws Exception {
    String simplePropertyJson = "{\"d\":{\"Age\":67}}";
    JsonReader reader = prepareReader(simplePropertyJson);
    final EdmProperty edmProperty = (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("Age");

    Map<String, Object> typeMappings = new HashMap<String, Object>();
    typeMappings.put("Age", Long.class);
    JsonPropertyConsumer jpc = new JsonPropertyConsumer();
    Map<String, Object> resultMap = jpc.readPropertyStandalone(reader, edmProperty, typeMappings);

    assertEquals(Long.valueOf(67), resultMap.get("Age"));
  }

  @Test
  public void simplePropertyWithStringToNullMappingStandalone() throws Exception {
    String simplePropertyJson = "{\"d\":{\"Age\":67}}";
    JsonReader reader = prepareReader(simplePropertyJson);
    final EdmProperty edmProperty = (EdmProperty) MockFacade.getMockEdm().getEntityType("RefScenario", "Employee").getProperty("Age");

    Map<String, Object> typeMappings = new HashMap<String, Object>();
    typeMappings.put("Age", null);
    JsonPropertyConsumer jpc = new JsonPropertyConsumer();
    Map<String, Object> resultMap = jpc.readPropertyStandalone(reader, edmProperty, typeMappings);

    assertEquals(Integer.valueOf(67), resultMap.get("Age"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void complexPropertyWithStringToStringMappingStandalone() throws Exception {
    String simplePropertyJson = "{\"d\":{\"City\":{\"__metadata\":{\"type\":\"RefScenario.c_City\"},\"PostalCode\":\"69124\",\"CityName\":\"Heidelberg\"}}}";
    JsonReader reader = prepareReader(simplePropertyJson);
    EdmComplexType complexPropertyType = (EdmComplexType) MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees").getEntityType().getProperty("Location").getType();
    EdmProperty edmProperty = (EdmProperty) complexPropertyType.getProperty("City");

    JsonPropertyConsumer jpc = new JsonPropertyConsumer();
    Map<String, Object> innerMappings = new HashMap<String, Object>();
    innerMappings.put("PostalCode", String.class);
    Map<String, Object> typeMappings = new HashMap<String, Object>();
    typeMappings.put("City", innerMappings);
    Map<String, Object> result = jpc.readPropertyStandalone(reader, edmProperty, typeMappings);

    assertEquals(1, result.size());
    Map<String, Object> innerResult = (Map<String, Object>) result.get("City");
    assertEquals("Heidelberg", innerResult.get("CityName"));
    assertEquals("69124", innerResult.get("PostalCode"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void deepComplexPropertyWithStringToStringMappingStandalone() throws Exception {
    String simplePropertyJson = "{\"d\":{\"Location\":{\"__metadata\":{\"type\":\"RefScenario.c_Location\"},\"City\":{\"__metadata\":{\"type\":\"RefScenario.c_City\"},\"PostalCode\":\"69124\",\"CityName\":\"Heidelberg\"},\"Country\":\"Germany\"}}}";
    JsonReader reader = prepareReader(simplePropertyJson);
    EdmProperty edmProperty = (EdmProperty) MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees").getEntityType().getProperty("Location");

    JsonPropertyConsumer jpc = new JsonPropertyConsumer();

    Map<String, Object> cityMappings = new HashMap<String, Object>();
    cityMappings.put("PostalCode", String.class);
    Map<String, Object> locationMappings = new HashMap<String, Object>();
    locationMappings.put("City", cityMappings);
    Map<String, Object> mappings = new HashMap<String, Object>();
    mappings.put("Location", locationMappings);

    Map<String, Object> result = (Map<String, Object>) jpc.readPropertyStandalone(reader, edmProperty, mappings);

    assertEquals(1, result.size());
    Map<String, Object> locationResult = (Map<String, Object>) result.get("Location");
    assertEquals(2, locationResult.size());
    assertEquals("Germany", locationResult.get("Country"));
    Map<String, Object> innerResult = (Map<String, Object>) locationResult.get("City");
    assertEquals(2, innerResult.size());
    assertEquals("Heidelberg", innerResult.get("CityName"));
    assertEquals("69124", innerResult.get("PostalCode"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void complexPropertyOnOpenReader() throws Exception {
    String simplePropertyJson = "{\"__metadata\":{\"type\":\"RefScenario.c_City\"},\"PostalCode\":\"69124\",\"CityName\":\"Heidelberg\"}";
    JsonReader reader = prepareReader(simplePropertyJson);
    EdmComplexType complexPropertyType = (EdmComplexType) MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees").getEntityType().getProperty("Location").getType();
    EdmProperty edmProperty = (EdmProperty) complexPropertyType.getProperty("City");
    EntityComplexPropertyInfo entityPropertyInfo = (EntityComplexPropertyInfo) EntityInfoAggregator.create(edmProperty);

    JsonPropertyConsumer jpc = new JsonPropertyConsumer();
    Map<String, Object> result = (Map<String, Object>) jpc.readProperty(reader, entityPropertyInfo, null);

    assertEquals(2, result.size());
    assertEquals("Heidelberg", result.get("CityName"));
    assertEquals("69124", result.get("PostalCode"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void complexPropertyOnOpenReaderWithNoMetadata() throws Exception {
    String simplePropertyJson = "{\"PostalCode\":\"69124\",\"CityName\":\"Heidelberg\"}";
    JsonReader reader = prepareReader(simplePropertyJson);
    EdmComplexType complexPropertyType = (EdmComplexType) MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees").getEntityType().getProperty("Location").getType();
    EdmProperty edmProperty = (EdmProperty) complexPropertyType.getProperty("City");
    EntityComplexPropertyInfo entityPropertyInfo = (EntityComplexPropertyInfo) EntityInfoAggregator.create(edmProperty);

    JsonPropertyConsumer jpc = new JsonPropertyConsumer();
    Map<String, Object> result = (Map<String, Object>) jpc.readProperty(reader, entityPropertyInfo, null);

    assertEquals(2, result.size());
    assertEquals("Heidelberg", result.get("CityName"));
    assertEquals("69124", result.get("PostalCode"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void deepComplexPropertyOnOpenReader() throws Exception {
    String simplePropertyJson = "{\"__metadata\":{\"type\":\"RefScenario.c_Location\"},\"City\":{\"__metadata\":{\"type\":\"RefScenario.c_City\"},\"PostalCode\":\"69124\",\"CityName\":\"Heidelberg\"},\"Country\":\"Germany\"}";
    JsonReader reader = prepareReader(simplePropertyJson);
    EdmProperty edmProperty = (EdmProperty) MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees").getEntityType().getProperty("Location");
    EntityComplexPropertyInfo entityPropertyInfo = (EntityComplexPropertyInfo) EntityInfoAggregator.create(edmProperty);

    JsonPropertyConsumer jpc = new JsonPropertyConsumer();
    Map<String, Object> result = (Map<String, Object>) jpc.readProperty(reader, entityPropertyInfo, null);

    assertEquals(2, result.size());
    assertEquals("Germany", result.get("Country"));
    Map<String, Object> innerResult = (Map<String, Object>) result.get("City");
    assertEquals(2, innerResult.size());
    assertEquals("Heidelberg", innerResult.get("CityName"));
    assertEquals("69124", innerResult.get("PostalCode"));
  }

  @Test
  public void simplePropertyStandalone() throws Exception {
    String simplePropertyJson = "{\"d\":{\"Name\":\"Team 1\"}}";
    EdmProperty edmProperty = (EdmProperty) MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Teams").getEntityType().getProperty("Name");
    JsonReader reader = prepareReader(simplePropertyJson);

    JsonPropertyConsumer jpc = new JsonPropertyConsumer();
    Map<String, Object> result = jpc.readPropertyStandalone(reader, edmProperty);
    assertEquals("Team 1", result.get("Name"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void complexPropertyStandalone() throws Exception {
    String simplePropertyJson = "{\"d\":{\"City\":{\"__metadata\":{\"type\":\"RefScenario.c_City\"},\"PostalCode\":\"69124\",\"CityName\":\"Heidelberg\"}}}";
    JsonReader reader = prepareReader(simplePropertyJson);
    EdmComplexType complexPropertyType = (EdmComplexType) MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees").getEntityType().getProperty("Location").getType();
    EdmProperty edmProperty = (EdmProperty) complexPropertyType.getProperty("City");

    JsonPropertyConsumer jpc = new JsonPropertyConsumer();
    Map<String, Object> result = jpc.readPropertyStandalone(reader, edmProperty);

    assertEquals(1, result.size());
    Map<String, Object> innerResult = (Map<String, Object>) result.get("City");
    assertEquals("Heidelberg", innerResult.get("CityName"));
    assertEquals("69124", innerResult.get("PostalCode"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void deepComplexPropertyStandalone() throws Exception {
    String simplePropertyJson = "{\"d\":{\"Location\":{\"__metadata\":{\"type\":\"RefScenario.c_Location\"},\"City\":{\"__metadata\":{\"type\":\"RefScenario.c_City\"},\"PostalCode\":\"69124\",\"CityName\":\"Heidelberg\"},\"Country\":\"Germany\"}}}";
    JsonReader reader = prepareReader(simplePropertyJson);
    EdmProperty edmProperty = (EdmProperty) MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees").getEntityType().getProperty("Location");

    JsonPropertyConsumer jpc = new JsonPropertyConsumer();
    Map<String, Object> result = (Map<String, Object>) jpc.readPropertyStandalone(reader, edmProperty, null);

    assertEquals(1, result.size());
    Map<String, Object> locationResult = (Map<String, Object>) result.get("Location");
    assertEquals(2, locationResult.size());
    assertEquals("Germany", locationResult.get("Country"));
    Map<String, Object> innerResult = (Map<String, Object>) locationResult.get("City");
    assertEquals(2, innerResult.size());
    assertEquals("Heidelberg", innerResult.get("CityName"));
    assertEquals("69124", innerResult.get("PostalCode"));
  }

  private JsonReader prepareReader(final String json) throws UnsupportedEncodingException {
    InputStream jsonStream = createContentAsStream(json);
    JsonReader reader = new JsonReader(new InputStreamReader(jsonStream));
    return reader;
  }

  private Map<String, Object> execute(final EdmProperty edmProperty, final JsonReader reader) throws EntityProviderException {
    JsonPropertyConsumer jpc = new JsonPropertyConsumer();
    Map<String, Object> resultMap = jpc.readPropertyStandalone(reader, edmProperty, null);
    return resultMap;
  }

  private InputStream createContentAsStream(final String json) throws UnsupportedEncodingException {
    return new ByteArrayInputStream(json.getBytes("UTF-8"));
  }

}
