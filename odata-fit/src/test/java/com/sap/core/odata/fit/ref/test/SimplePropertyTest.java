package com.sap.core.odata.fit.ref.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.http.HttpResponse;
import org.junit.Test;

/**
 * Tests employing the reference scenario reading simple properties in XML format
 * @author SAP AG
 */
public class SimplePropertyTest extends AbstractRefTest {

  @Test
  public void simpleProperty() throws Exception {
    HttpResponse response = callUri("Employees('2')/Age/$value");
    checkMediaType(response, TEXT_PLAIN);
    assertEquals(EMPLOYEE_2_AGE, getBody(response));

    response = callUri("Employees('2')/Age");
    // checkMediaType(response, APPLICATION_XML);
    assertTrue(getBody(response).contains(EMPLOYEE_2_AGE));

    response = callUri("Container2.Photos(Id=3,Type='image%2Fjpeg')/Image/$value");
    // checkMediaType(response, IMAGE_JPEG);
    assertNotNull(getBody(response));

    response = callUri("Container2.Photos(Id=3,Type='image%2Fjpeg')/Image");
    // checkMediaType(response, APPLICATION_XML);
    assertNotNull(getBody(response));
    // assertTrue(getBody(response).contains("<d:Image m:type=\"Edm.Binary\" m:MimeType=\"image/jpeg\""));

    response = callUri("Rooms('2')/Seats/$value");
    checkMediaType(response, TEXT_PLAIN);
    // checkEtag(response, "W/\"2\"");
    assertEquals("5", getBody(response));

    response = callUri("Rooms('2')/Seats");
    // checkMediaType(response, APPLICATION_XML);
    // checkEtag(response, "W/\"2\"");
    assertNotNull(getBody(response));
    // assertTrue(getBody(response).contains("5</"));

    // response = callUri("Container2.Photos(Id=3,Type='image%2Fjpeg')/BinaryData/$value");
    // checkMediaType(response, IMAGE_JPEG);
    // assertNotNull(getBody(response));

    // response = callUri("Container2.Photos(Id=3,Type='image%2Fjpeg')/BinaryData");
    // checkMediaType(response, APPLICATION_XML);
    // assertNotNull(getBody(response));

    // notFound("Employees('2')/Foo");
    // notFound("Employees('2')/Age()");
  }

  @Test
  public void navigationSimpleProperty() throws Exception {
    HttpResponse response = callUri("Employees('2')/ne_Room/nr_Employees('6')/Age");
    // checkMediaType(response, APPLICATION_XML);
    assertTrue(getBody(response).contains(EMPLOYEE_6_AGE));

    response = callUri("Employees('4')/ne_Team/nt_Employees('5')/EmployeeName");
    // checkMediaType(response, APPLICATION_XML);
    assertTrue(getBody(response).contains(EMPLOYEE_5_NAME));

    response = callUri("Rooms('2')/nr_Employees('4')/Location/City/CityName");
    // checkMediaType(response, APPLICATION_XML);
    assertTrue(getBody(response).contains(CITY_2_NAME));
  }
}
