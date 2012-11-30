package com.sap.core.odata.core.serialization.test;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.core.odata.api.edm.EdmContentKind;
import com.sap.core.odata.api.edm.EdmCustomizableFeedMappings;
import com.sap.core.odata.api.edm.EdmEntityContainer;
import com.sap.core.odata.api.edm.EdmEntitySet;
import com.sap.core.odata.api.edm.EdmEntityType;
import com.sap.core.odata.api.edm.EdmException;
import com.sap.core.odata.api.edm.EdmProperty;
import com.sap.core.odata.api.edm.EdmSimpleTypeKind;
import com.sap.core.odata.api.edm.EdmTargetPath;
import com.sap.core.odata.api.enums.Format;
import com.sap.core.odata.api.exception.ODataException;
import com.sap.core.odata.api.processor.ODataContext;
import com.sap.core.odata.api.processor.ODataUriInfo;
import com.sap.core.odata.api.serialization.ODataSerializationException;
import com.sap.core.odata.api.serialization.ODataSerializer;
import com.sap.core.odata.core.serializer.AtomEntrySerializer;
import com.sap.core.odata.testutils.helper.XMLUnitHelper;

public abstract class AbstractSerializerTest {

  protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

  protected static final URI BASE_URI;
  static {
    try {
      BASE_URI = new URI("http://host:port/särvice/");
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  protected Map<String, Object> data;

  {
    this.data = new HashMap<String, Object>();

    Calendar date = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    date.clear();
    date.set(1999, 0, 1);

    this.data.put("employeeId", "1");
    this.data.put("immageUrl", null);
    this.data.put("managerId", "1");
    this.data.put("age", new Integer(52));
    this.data.put("roomId", "1");
    this.data.put("entryDate", date);
    this.data.put("teamId", "42");
    this.data.put("employeeName", "Walter Winter");
  }

  @Before
  public void before() throws EdmException {
    Map<String, String> ns = new HashMap<String, String>();
    ns.put("d", AtomEntrySerializer.NS_DATASERVICES);
    ns.put("m", AtomEntrySerializer.NS_DATASERVICES_METADATA);
    ns.put("a", AtomEntrySerializer.NS_ATOM);
    XMLUnitHelper.registerXmlNs(ns);
  }

  protected EdmEntitySet createEdmEntitySetMock(boolean multipleIds) throws EdmException {
    EdmEntityContainer ec = mock(EdmEntityContainer.class);
    when(ec.getName()).thenReturn("Container");
    when(ec.isDefaultEntityContainer()).thenReturn(false);
  
    List<EdmProperty> kpl = new ArrayList<EdmProperty>();
    EdmProperty idp = mock(EdmProperty.class);
    when(idp.getName()).thenReturn("employeeId");
    when(idp.getType()).thenReturn(EdmSimpleTypeKind.String.getEdmSimpleTypeInstance());
    kpl.add(idp);
  
    if (multipleIds) {
      EdmProperty idp2 = mock(EdmProperty.class);
      when(idp2.getName()).thenReturn("age");
      when(idp2.getType()).thenReturn(EdmSimpleTypeKind.Int32.getEdmSimpleTypeInstance());
      kpl.add(idp2);
    }
  
    //
    Set<EdmProperty> mockedProperties = new HashSet<EdmProperty>();
    EdmProperty edmRoomId = mock(EdmProperty.class);
    when(edmRoomId.getName()).thenReturn("roomId");
    when(edmRoomId.getType()).thenReturn(EdmSimpleTypeKind.String.getEdmSimpleTypeInstance());
    mockedProperties.add(edmRoomId);
  
    EdmProperty edmTeamId = mock(EdmProperty.class);
    when(edmTeamId.getName()).thenReturn("teamId");
    when(edmTeamId.getType()).thenReturn(EdmSimpleTypeKind.String.getEdmSimpleTypeInstance());
    mockedProperties.add(edmTeamId);
  
    EdmProperty edmEmployeeName = mock(EdmProperty.class);
    when(edmEmployeeName.getName()).thenReturn("employeeName");
    when(edmEmployeeName.getType()).thenReturn(EdmSimpleTypeKind.String.getEdmSimpleTypeInstance());
    EdmCustomizableFeedMappings feedMapping = mock(EdmCustomizableFeedMappings.class);
    when(feedMapping.getFcTargetPath()).thenReturn(EdmTargetPath.SYNDICATION_TITLE);
    when(feedMapping.getFcContentKind()).thenReturn(EdmContentKind.text);
    when(edmEmployeeName.getCustomizableFeedMappings()).thenReturn(feedMapping);
    mockedProperties.add(edmEmployeeName);
  
    EdmProperty edmEntryDate = mock(EdmProperty.class);
    when(edmEntryDate.getName()).thenReturn("entryDate");
    when(edmEntryDate.getType()).thenReturn(EdmSimpleTypeKind.DateTime.getEdmSimpleTypeInstance());
    EdmCustomizableFeedMappings feMa2 = mock(EdmCustomizableFeedMappings.class);
    when(feMa2.getFcTargetPath()).thenReturn(EdmTargetPath.SYNDICATION_UPDATED);
    when(feMa2.getFcContentKind()).thenReturn(EdmContentKind.text);
    when(edmEntryDate.getCustomizableFeedMappings()).thenReturn(feMa2);
    mockedProperties.add(edmEntryDate);
    //
  
    EdmEntityType et = mock(EdmEntityType.class);
    when(et.getKeyProperties()).thenReturn(kpl);
    //
    Collection<String> propertyNames = new HashSet<String>();
    for (EdmProperty edmProperty : mockedProperties) {
      when(et.getProperty(edmProperty.getName())).thenReturn(edmProperty);
      propertyNames.add(edmProperty.getName());
    }
    when(et.getPropertyNames()).thenReturn(propertyNames);
    //
  
    EdmEntitySet es = mock(EdmEntitySet.class);
    when(es.getName()).thenReturn("Employees");
    when(es.getEntityContainer()).thenReturn(ec);
    when(es.getEntityType()).thenReturn(et);
  
    return es;
  }
  protected ODataContext createContextMock() throws ODataException {
    ODataUriInfo uriInfo = mock(ODataUriInfo.class);
    when(uriInfo.getBaseUri()).thenReturn(BASE_URI);
    ODataContext ctx = mock(ODataContext.class);
    when(ctx.getUriInfo()).thenReturn(uriInfo);
    return ctx;
  }
  

  protected ODataSerializer createAtomSerializer(ODataContext context, EdmEntitySet entitySet, Map<String, Object> data) throws ODataException, EdmException, ODataSerializationException {
    ODataSerializer ser = ODataSerializer.create(Format.ATOM, context);
    assertNotNull(ser);
    return ser;
  }

  protected ODataSerializer createAtomSerializer(EdmEntitySet es) throws ODataException, EdmException, ODataSerializationException {
    ODataContext ctx = createContextMock();
    return createAtomSerializer(ctx, es, data);
  }

  protected ODataSerializer createAtomSerializer() throws ODataException, EdmException, ODataSerializationException {
    ODataContext ctx = createContextMock();
    EdmEntitySet es = createEdmEntitySetMock(false);
    return createAtomSerializer(ctx, es, data);
  }

}
