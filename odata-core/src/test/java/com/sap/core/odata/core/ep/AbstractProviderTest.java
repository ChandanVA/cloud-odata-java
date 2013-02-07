package com.sap.core.odata.core.ep;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.core.odata.api.edm.Edm;
import com.sap.core.odata.api.edm.EdmException;
import com.sap.core.odata.api.ep.EntityProvider.EntityProviderInterface;
import com.sap.core.odata.api.ep.EntityProviderException;
import com.sap.core.odata.api.ep.EntityProviderProperties;
import com.sap.core.odata.api.exception.ODataException;
import com.sap.core.odata.api.processor.ODataContext;
import com.sap.core.odata.api.uri.PathInfo;
import com.sap.core.odata.testutil.fit.BaseTest;

/**
 * @author SAP AG
*/
public abstract class AbstractProviderTest extends BaseTest {

  protected final Logger log = LoggerFactory.getLogger(this.getClass());

  protected static final URI BASE_URI;

  static {
    try {
      BASE_URI = new URI("http://host:80/service/");
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
  protected static final EntityProviderProperties DEFAULT_PROPERTIES = EntityProviderProperties.serviceRoot(BASE_URI).build();

  protected Map<String, Object> employeeData;

  protected Map<String, Object> photoData;

  protected Map<String, Object> roomData;

  protected ArrayList<Map<String, Object>> roomsData;

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
    photoData.put("Type", "image/png");
    photoData.put("ImageUrl", "http://www.mopo.de/image/view/2012/6/4/16548086,13385561,medRes,maxh,234,maxw,234,Parodia_Mona_Lisa_Lego_Hamburger_Morgenpost.jpg");
    photoData.put("Image", new byte[] {1, 2, 3, 4});
    photoData.put("BinaryData", new byte[] {-1, -2, -3, -4});
    photoData.put("Содержание", "В лесу шумит водопад. Если он не торопится просп воды");

    this.roomData = new HashMap<String, Object>();
    this.roomData.put("Id", "1");
    this.roomData.put("Name", "Neu Schwanstein");
    this.roomData.put("Seats", new Integer(20));
    this.roomData.put("Version", new Integer(3));
  }

  protected void initializeRoomData(int count) {
    this.roomsData = new ArrayList<Map<String, Object>>();
    for (int i = 1; i <= count; i++) {
      HashMap<String, Object> tmp = new HashMap<String, Object>();
      tmp.put("Id", "" + i);
      tmp.put("Name", "Neu Schwanstein" + i);
      tmp.put("Seats", new Integer(20));
      tmp.put("Version", new Integer(3));
      this.roomsData.add(tmp);
    }
  }

  @Before
  public void setXmlNamespacePrefixes() throws Exception {
    Map<String, String> prefixMap = new HashMap<String, String>();
    prefixMap.put("a", Edm.NAMESPACE_ATOM_2005);
    prefixMap.put("d", Edm.NAMESPACE_D_2007_08);
    prefixMap.put("m", Edm.NAMESPACE_M_2007_08);
    XMLUnit.setXpathNamespaceContext(new SimpleNamespaceContext(prefixMap));
  }

  protected ODataContext createContextMock() throws ODataException {
    PathInfo pathInfo = mock(PathInfo.class);
    when(pathInfo.getServiceRoot()).thenReturn(BASE_URI);
    ODataContext ctx = mock(ODataContext.class);
    when(ctx.getPathInfo()).thenReturn(pathInfo);
    return ctx;
  }

  protected EntityProviderInterface createEntityProvider() throws ODataException, EdmException, EntityProviderException {
    return new ProviderFacadeImpl();
  }

  protected AtomEntityProvider createAtomEntityProvider() throws EntityProviderException {
    return new AtomEntityProvider();
  }
}
