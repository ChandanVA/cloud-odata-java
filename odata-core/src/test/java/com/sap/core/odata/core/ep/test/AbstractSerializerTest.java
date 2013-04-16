package com.sap.core.odata.core.ep.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.core.odata.api.edm.Edm;
import com.sap.core.odata.api.edm.EdmException;
import com.sap.core.odata.api.enums.Format;
import com.sap.core.odata.api.ep.ODataEntityProviderException;
import com.sap.core.odata.api.ep.ODataEntityProvider;
import com.sap.core.odata.api.exception.ODataException;
import com.sap.core.odata.api.processor.ODataContext;
import com.sap.core.odata.api.processor.ODataUriInfo;
import com.sap.core.odata.testutils.helper.XMLUnitHelper;

/**
 * @author SAP AG
*/
public abstract class AbstractSerializerTest {

  protected final Logger log = LoggerFactory.getLogger(this.getClass());

  protected static final URI BASE_URI;
  static {
    try {
      BASE_URI = new URI("http://host:80/service/");
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  protected Map<String, Object> employeeData;

  protected Map<String, Object> photoData;

  protected Map<String, Object> roomData;

  {
    this.employeeData = new HashMap<String, Object>();

    Calendar date = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    date.clear();
    date.set(1999, 0, 1);

    this.employeeData.put("EmployeeId", "1");
    this.employeeData.put("ImmageUrl", null);
    this.employeeData.put("ManagerId", "1");
    this.employeeData.put("Age", new Integer(52));
    this.employeeData.put("RoomId", "1");
    this.employeeData.put("EntryDate", date);
    this.employeeData.put("TeamId", "42");
    this.employeeData.put("EmployeeName", "Walter Winter");

    Map<String, Object> locationData = new HashMap<String, Object>();
    Map<String, Object> cityData = new HashMap<String, Object>();
    cityData.put("PostalCode", "33470");
    cityData.put("CityName", "Duckburg");
    locationData.put("City", cityData);
    locationData.put("Country", "Calisota");

    this.employeeData.put("Location", locationData);

    this.photoData = new HashMap<String, Object>();
    photoData.put("Id", Integer.valueOf(1));
    photoData.put("Name", "Mona Lisa");
    photoData.put("Type", "JPG");
    photoData.put("ImageUrl", "http://www.mopo.de/image/view/2012/6/4/16548086,13385561,medRes,maxh,234,maxw,234,Parodia_Mona_Lisa_Lego_Hamburger_Morgenpost.jpg");
    photoData.put("Image", "ABCF");
    photoData.put("BinaryData", "ABCD");
    photoData.put("Содержание", "В лесу шумит водопад. Если он не торопится просп воды");

    this.roomData = new HashMap<String, Object>();
    this.roomData.put("Id", "1");
    this.roomData.put("Name", "Neu Schwanstein");
    this.roomData.put("Seats", new Integer(20));
    this.roomData.put("Version", new Integer(3));
  }

  @Before
  public void before() throws Exception {
    Map<String, String> ns = new HashMap<String, String>();
    ns.put("d", Edm.NAMESPACE_D_2007_08);
    ns.put("m", Edm.NAMESPACE_M_2007_08);
    ns.put("a", Edm.NAMESPACE_ATOM_2005);
    XMLUnitHelper.registerXmlNs(ns);
  }

  protected ODataContext createContextMock() throws ODataException {
    ODataUriInfo uriInfo = mock(ODataUriInfo.class);
    when(uriInfo.getBaseUri()).thenReturn(BASE_URI);
    ODataContext ctx = mock(ODataContext.class);
    when(ctx.getUriInfo()).thenReturn(uriInfo);
    return ctx;
  }

  protected ODataEntityProvider createAtomSerializer() throws ODataException, EdmException, ODataEntityProviderException {
    ODataEntityProvider ser = ODataEntityProvider.create(Format.ATOM, createContextMock());
    return ser;
  }

}
