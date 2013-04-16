package com.sap.core.odata.fit.ref;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.http.HttpResponse;
import org.junit.Test;

import com.sap.core.odata.core.enums.ContentType;

/**
 * Tests employing the reference scenario reading complex properties in XML format
 * @author SAP AG
 */
public class ComplexPropertyTest extends AbstractRefTest {
  @Test
  public void complexProperty() throws Exception {
    HttpResponse response = callUri("Employees('2')/Location/City/CityName/$value");
    checkMediaType(response, ContentType.TEXT_PLAIN, false);
    assertEquals(CITY_2_NAME, getBody(response));

    response = callUri("Employees('2')/Location");
    checkMediaType(response, ContentType.APPLICATION_XML);
    assertTrue(getBody(response).contains("PostalCode"));

    // notFound("Employees('2')/Location()");
    notFound("Employees('2')/Location/City/$value");
  }
}
