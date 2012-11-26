package com.sap.core.odata.core.serialization.test;

import static org.custommonkey.xmlunit.XMLAssert.*;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.custommonkey.xmlunit.exceptions.XpathException;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.sap.core.odata.api.edm.EdmEntityContainer;
import com.sap.core.odata.api.edm.EdmEntitySet;
import com.sap.core.odata.api.edm.EdmEntityType;
import com.sap.core.odata.api.edm.EdmException;
import com.sap.core.odata.api.edm.EdmProperty;
import com.sap.core.odata.api.edm.EdmSimpleTypeKind;
import com.sap.core.odata.api.enums.Format;
import com.sap.core.odata.api.exception.ODataException;
import com.sap.core.odata.api.processor.ODataContext;
import com.sap.core.odata.api.processor.ODataUriInfo;
import com.sap.core.odata.api.serialization.ODataSerializationException;
import com.sap.core.odata.api.serialization.ODataSerializer;
import com.sap.core.odata.core.serializer.AtomEntrySerializer;
import com.sap.core.odata.testutils.helper.StringHelper;
import com.sap.core.odata.testutils.helper.XMLUnitHelper;

public class AtomEntrySerializationTest {

  private static final URI BASE_URI;
  static {
    try {
      BASE_URI = new URI("http://host:port/särvice/");
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  private static final Logger LOG = LoggerFactory.getLogger(AtomEntrySerializationTest.class);

  private Map<String, Object> data;

  {
    try {
      this.data = new HashMap<String, Object>();

      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm");
      Date date;
      date = formatter.parse("1999-01-01T00:00");

      this.data.put("employeeId", "1");
      this.data.put("immageUrl", null);
      this.data.put("managerId", "1");
      this.data.put("age", new Integer(52));
      this.data.put("roomId", "1");
      this.data.put("entryData", date);
      this.data.put("teamId", "1");
      this.data.put("employeeName", "Walter Winter");
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  @Before
  public void before() throws EdmException {
    Map<String, String> ns = new HashMap<String, String>();
    ns.put("d", AtomEntrySerializer.NS_DATASERVICES);
    ns.put("m", AtomEntrySerializer.NS_DATASERVICES_METADATA);
    ns.put("a", AtomEntrySerializer.NS_ATOM);
    XMLUnitHelper.registerXmlNs(ns);
  }

  @Test
  public void serializeEntry() throws IOException, XpathException, SAXException, XMLStreamException, FactoryConfigurationError, ODataException {
    ODataSerializer ser = ODataSerializer.create(Format.ATOM);
    assertNotNull(ser);

    ODataContext ctx = createContextMock();
    EdmEntitySet es = createEdmEntitySetMock(false);

    ser.setContext(ctx);
    ser.setData(this.data);
    ser.setEdmEntitySet(es);

    InputStream xmlStream = ser.serialize();
    String xmlString = StringHelper.inputStreamToString(xmlStream);

    assertXpathExists("/a:entry", xmlString);
    assertXpathEvaluatesTo(BASE_URI.toASCIIString(), "/a:entry/@xml:base", xmlString);
  }

  @Test
  public void serializeEntryId() throws IOException, XpathException, SAXException, XMLStreamException, FactoryConfigurationError, ODataException {
    ODataSerializer ser = ODataSerializer.create(Format.ATOM);
    assertNotNull(ser);

    ODataContext ctx = createContextMock();
    EdmEntitySet es = createEdmEntitySetMock(false);

    ser.setContext(ctx);
    ser.setData(this.data);
    ser.setEdmEntitySet(es);

    InputStream xmlStream = ser.serialize();
    String xmlString = StringHelper.inputStreamToString(xmlStream);

    assertXpathExists("/a:entry", xmlString);
    assertXpathEvaluatesTo(BASE_URI.toASCIIString(), "/a:entry/@xml:base", xmlString);
    assertXpathExists("/a:entry/a:id", xmlString);
    assertXpathEvaluatesTo(BASE_URI.toASCIIString() + "Container.Employees('1')", "/a:entry/a:id/text()", xmlString);
  }

  @Test
  public void serializeEntryTitle() throws Exception {
    ODataSerializer ser = ODataSerializer.create(Format.ATOM);
    assertNotNull(ser);

    ODataContext ctx = createContextMock();
    EdmEntitySet es = createEdmEntitySetMock(false);

    ser.setContext(ctx);
    ser.setData(this.data);
    ser.setEdmEntitySet(es);

    InputStream xmlStream = ser.serialize();
    String xmlString = StringHelper.inputStreamToString(xmlStream);

    assertXpathExists("/a:entry/a:title", xmlString);
    assertXpathEvaluatesTo("text", "/a:entry/a:title/@type", xmlString);
    assertXpathEvaluatesTo("Walter Winter", "/a:entry/a:title/text()", xmlString);
  }

  private ODataContext createContextMock() throws ODataException {
    ODataUriInfo uriInfo = mock(ODataUriInfo.class);
    when(uriInfo.getBaseUri()).thenReturn(BASE_URI);
    ODataContext ctx = mock(ODataContext.class);
    when(ctx.getUriInfo()).thenReturn(uriInfo);
    return ctx;
  }

  private EdmEntitySet createEdmEntitySetMock(boolean multipleIds) throws EdmException {
    EdmEntityContainer ec = mock(EdmEntityContainer.class);
    when(ec.getName()).thenReturn("Container");
    when(ec.isDefaultEntityContainer()).thenReturn(false);

    List<EdmProperty> kpl = new ArrayList<EdmProperty>();
    EdmProperty idp = mock(EdmProperty.class);
    when(idp.getName()).thenReturn("employeeId");
    when(idp.getType()).thenReturn(EdmSimpleTypeKind.stringInstance());
    kpl.add(idp);

    if (multipleIds) {
      EdmProperty idp2 = mock(EdmProperty.class);
      when(idp2.getName()).thenReturn("age");
      when(idp2.getType()).thenReturn(EdmSimpleTypeKind.int32Instance());
      kpl.add(idp2);
    }

    EdmEntityType et = mock(EdmEntityType.class);
    when(et.getKeyProperties()).thenReturn(kpl);

    EdmEntitySet es = mock(EdmEntitySet.class);
    when(es.getName()).thenReturn("Employees");
    when(es.getEntityContainer()).thenReturn(ec);
    when(es.getEntityType()).thenReturn(et);
    return es;
  }

  @Test
  public void serializeIds() throws IOException, XpathException, SAXException, XMLStreamException, FactoryConfigurationError, ODataException {
    ODataSerializer ser = ODataSerializer.create(Format.ATOM);
    assertNotNull(ser);

    ODataContext ctx = createContextMock();
    EdmEntitySet es = createEdmEntitySetMock(true);

    ser.setContext(ctx);
    ser.setData(this.data);
    ser.setEdmEntitySet(es);

    InputStream xmlStream = ser.serialize();
    String xmlString = StringHelper.inputStreamToString(xmlStream);

    assertXpathExists("/a:entry", xmlString);
    assertXpathEvaluatesTo(BASE_URI.toASCIIString(), "/a:entry/@xml:base", xmlString);
    assertXpathExists("/a:entry/a:id", xmlString);
    assertXpathEvaluatesTo(BASE_URI.toASCIIString() + "Container.Employees(employeeId='1',age=null)", "/a:entry/a:id/text()", xmlString);
  }

}
