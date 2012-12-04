package com.sap.core.odata.fit.ref.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.http.HttpResponse;
import org.junit.Test;

/**
 * Tests employing the reference scenario reading entity sets in XML format
 * @author SAP AG
 */
public class FeedTest extends AbstractRefTest {

  @Test
  public void feed() throws Exception {
    HttpResponse response = callUri("Employees()");
    checkMediaType(response, APPLICATION_ATOM_XML_FEED);
    final String payload = getBody(response);
    assertTrue(payload.contains("Employee"));
    assertTrue(payload.contains(EMPLOYEE_1_NAME));
    assertTrue(payload.contains(EMPLOYEE_2_NAME));
    assertTrue(payload.contains(EMPLOYEE_3_NAME));
    assertTrue(payload.contains(EMPLOYEE_4_NAME));
    assertTrue(payload.contains(EMPLOYEE_5_NAME));
    assertTrue(payload.contains(EMPLOYEE_6_NAME));

    response = callUri("Rooms()");
    checkMediaType(response, APPLICATION_ATOM_XML_FEED);
    assertTrue(getBody(response).contains("Room"));

    // notFound("$top");
  }

  @Test
  public void navigationFeed() throws Exception {
    HttpResponse response = callUri("Employees('3')/ne_Room/nr_Employees()");
    checkMediaType(response, APPLICATION_ATOM_XML_FEED);
    String body = getBody(response);
    // assertTrue(body.contains("feed"));
    // assertTrue(body.contains("entry"));
    // count <d:EmployeeName> = 4
    assertTrue(body.contains(EMPLOYEE_2_NAME));
    assertTrue(body.contains(EMPLOYEE_3_NAME));
    assertTrue(body.contains(EMPLOYEE_4_NAME));
    assertTrue(body.contains(EMPLOYEE_6_NAME));
    assertFalse(body.contains(EMPLOYEE_1_NAME));
    assertFalse(body.contains(EMPLOYEE_5_NAME));

    response = callUri("Rooms('2')/nr_Employees");
    checkMediaType(response, APPLICATION_ATOM_XML_FEED);
    assertFalse(getBody(response).isEmpty());
    // count <d:EmployeeName> = 4

    // response = callUri("Employees('2')/ne_Team/nt_Employees?$orderby=Age&$top=1");
    // checkMediaType(response, APPLICATION_ATOM_XML_FEED);
    // count <d:EmployeeName> = 1
    // assertTrue(getBody(response).contains(EMPLOYEE_2_NAME));
  }

  @Test
  public void skipAndTop() throws Exception {
    HttpResponse response = callUri("Employees?$skip=1&$top=1");
    checkMediaType(response, APPLICATION_ATOM_XML_FEED);
    String body = getBody(response);
    // assertTrue(body.contains("feed"));
    // assertTrue(body.contains("entry"));
    // count <d:EmployeeName> = 1
    assertTrue(body.contains(EMPLOYEE_2_NAME));

    response = callUri("Teams()?$skip=4");
    checkMediaType(response, APPLICATION_ATOM_XML_FEED);
    assertFalse(getBody(response).contains("entry"));

    badRequest("Employees?$top=a");
    badRequest("Employees?$top=-1");
    badRequest("Teams('3')?$top=1");
  }

  @Test
  public void skiptoken() throws Exception {
    HttpResponse response = callUri("Employees?$skiptoken=6");
    checkMediaType(response, APPLICATION_ATOM_XML_FEED);
    String body = getBody(response);
    // count <d:EmployeeName> = 1
    assertTrue(body.contains(EMPLOYEE_6_NAME));
    assertFalse(body.contains(EMPLOYEE_1_NAME));

    response = callUri("Container2.Photos?$skiptoken=4foo");
    checkMediaType(response, APPLICATION_ATOM_XML_FEED);
    body = getBody(response);
    assertFalse(body.isEmpty());
    // count <d:Name> = 1
  }

  @Test
  public void filter() throws Exception {
    // HttpResponse response = callUri("Employees?$filter=RoomId%20eq%20%273%27");
    // checkMediaType(response, APPLICATION_ATOM_XML_FEED);
    // String body = getBody(response);
    // count </entry> = 1
    // assertTrue(body.contains(EMPLOYEE_5_NAME));

    // response = callUri("Employees?$filter=EntryDate%20gt%20datetime%272003-12-24T00%3A00%3A00%27");
    // checkMediaType(response, APPLICATION_ATOM_XML_FEED);
    // body = getBody(response);
    // count </entry> = 2
    // assertTrue(body.contains(EMPLOYEE_6_NAME));

    // response = callUri("Buildings?$filter=Image%20eq%20X%2700%27");
    // checkMediaType(response, APPLICATION_ATOM_XML_FEED);
    // assertFalse(getBody(response).contains("entry"));

    // response = callUri("Employees?$filter=day(EntryDate)%20eq%20(Age%20mod%208%20add%201)");
    // checkMediaType(response, APPLICATION_ATOM_XML_FEED);
    // body = getBody(response);
    // count </entry> = 1
    // assertTrue(body.contains(EMPLOYEE_2_NAME));

    // response = callUri("Employees?$filter=indexof(ImageUrl,EmployeeId)%20mod%20(Age%20sub%2028)%20eq%20month(EntryDate)%20mul%203%20div%2027%20sub%201");
    // checkMediaType(response, APPLICATION_ATOM_XML_FEED);
    // body = getBody(response);
    // count </entry> = 1
    // assertTrue(body.contains(EMPLOYEE_4_NAME));

    // response = callUri("Employees?$filter=not(Age%20sub%2030%20ge%20-hour(EntryDate))");
    // checkMediaType(response, APPLICATION_ATOM_XML_FEED);
    // body = getBody(response);
    // count </entry> = 1
    // assertTrue(body.contains(EMPLOYEE_6_NAME));

    // response = callUri("Employees('1')/ne_Room/nr_Employees?$filter=EmployeeId eq '1'");
    // assertTrue(getBody(response).contains("entry"));

    // response = callUri("Employees('1')/ne_Room/nr_Employees?$filter=EmployeeId eq '2'");
    // assertTrue(getBody(response).contains("entry"));

    // response = callUri("Employees?$filter=Location/City/PostalCode%20lt%20%2769150%27");
    // checkMediaType(response, APPLICATION_ATOM_XML_FEED);
    // body = getBody(response);
    // count </entry> = 1
    // assertTrue(body.contains("69124"));

    // response = callUri("Employees?$filter=length(trim(Location/City/CityName))%20gt%209");
    // checkMediaType(response, APPLICATION_ATOM_XML_FEED);
    // body = getBody(response);
    // count </entry> = 1
    // assertFalse(body.contains(CITY_2_NAME));

    // response = callUri("Employees('2')?$filter=Age%20eq%2032");
    // checkMediaType(response, APPLICATION_ATOM_XML_ENTRY);
    // body = getBody(response);
    // count </entry> = 1
    // assertFalse(body.contains(EMPLOYEE_2_NAME));

    // checkUri("Employees('1')/ne_Room/nr_Employees('1')?$filter=EmployeeId eq '1'");
    // checkUri("Container2.Photos(Id=4,Type='foo')?$filter=%D0%A1%D0%BE%D0%B4%D0%B5%D1%80%D0%B6%D0%B0%D0%BD%D0%B8%D0%B5 eq '%D0%9F%D1%80%D0%BE%D0%B4%D1%83%D0%BA%D1%82'");

    // notFound("Employees('4')?$filter=Age%20eq%2099");
    // notFound("Rooms('1')/nr_Employees('1')?$filter=Age%20eq%2099");
    // notFound("Employees('4')/ne_Room?$filter=Id%20eq%20%271%27");

    // badRequest("Employees?$filter=(EmployeeId");
    // badRequest("Employees?$filter=(EmployeeId)");
    // badRequest("Employees?$filter=loca/city/cityname%20eq%20%27Heidelberg%27");
    // badRequest("Employees?$filter=endswith(Location,'y')");
    // badRequest("Buildings?$filter=Image%20eq%20X%27notonlyhexdigits%27");
  }
}
