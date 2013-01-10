package com.sap.core.odata.fit.ref;

import org.junit.Test;

import com.sap.core.odata.api.commons.HttpStatusCodes;

/**
 * Tests employing the reference scenario that use neither XML nor JSON
 * and that change data in some way
 * @author SAP AG
 */
public class MiscChangeTest extends AbstractRefTest {

  @Test
  public void deleteEntry() throws Exception {
    deleteUriOk("Employees('2')");
    deleteUriOk("Managers('3')");
    deleteUriOk("Teams('2')");
    deleteUriOk("Rooms('1')"); // if_match "W/\"1\""
    deleteUriOk("Container2.Photos(Id=1,Type='image%2Fpng')"); // if_match "W/\"1\""

    // deleteUri("Rooms('1')", HttpStatusCodes.PRECONDITION_REQUIRED);
    deleteUri("Managers()", HttpStatusCodes.METHOD_NOT_ALLOWED);
    deleteUri("Managers('5')", HttpStatusCodes.NOT_FOUND);
    deleteUri("Employees('2')/ne_Manager", HttpStatusCodes.METHOD_NOT_ALLOWED);
  }

  @Test
  public void deletePropertyValue() throws Exception {
    deleteUriOk("Employees('2')/Age/$value");
    deleteUriOk("Employees('2')/Location/City/PostalCode/$value");
    deleteUriOk("Employees('2')/ne_Manager/Age/$value");

    deleteUri("Employees('2')/Age", HttpStatusCodes.METHOD_NOT_ALLOWED);
    deleteUri("Employees('2')/Foo/$value", HttpStatusCodes.NOT_FOUND);
    deleteUri("Employees('2')/EmployeeId/$value", HttpStatusCodes.METHOD_NOT_ALLOWED);
    deleteUri("Employees('2')/Location/City/$value", HttpStatusCodes.NOT_FOUND);
  }

  @Test
  public void deleteLink() throws Exception {
    deleteUriOk("Employees('6')/$links/ne_Room");
    deleteUriOk("Managers('3')/$links/nm_Employees('5')");
    deleteUriOk("Employees('2')/ne_Team/$links/nt_Employees('1')");

    deleteUri("Managers('3')/$links/nm_Employees()", HttpStatusCodes.METHOD_NOT_ALLOWED);
    deleteUri("Managers('3')/$links/nm_Employees('1')", HttpStatusCodes.NOT_FOUND);
  }

  @Test
  public void deleteMediaResource() throws Exception {
    deleteUriOk("Managers('1')/$value");

    deleteUri("Teams('2')/$value", HttpStatusCodes.BAD_REQUEST);
  }
}
