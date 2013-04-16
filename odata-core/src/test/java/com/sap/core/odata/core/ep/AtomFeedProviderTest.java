package com.sap.core.odata.core.ep;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathNotExists;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import com.sap.core.odata.api.edm.EdmEntitySet;
import com.sap.core.odata.api.enums.ContentType;
import com.sap.core.odata.api.enums.InlineCount;
import com.sap.core.odata.api.ep.ODataEntityProvider;
import com.sap.core.odata.api.ep.ODataEntityProviderException;
import com.sap.core.odata.api.ep.ODataEntityProviderProperties;
import com.sap.core.odata.api.processor.ODataResponse;
import com.sap.core.odata.api.uri.resultviews.GetEntitySetView;
import com.sap.core.odata.testutils.helper.StringHelper;
import com.sap.core.odata.testutils.mocks.MockFacade;

public class AtomFeedProviderTest extends AbstractProviderTest {

  private GetEntitySetView view;

  @Before
  public void before() throws Exception {
    super.before();

    initializeRoomData(1);

    view = mock(GetEntitySetView.class);

    EdmEntitySet set = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");
    when(view.getTargetEntitySet()).thenReturn(set);
  }

  @Test
  public void testFeedNamespaces() throws Exception {
    ODataEntityProvider ser = createAtomEntityProvider();
    ODataEntityProviderProperties properties = ODataEntityProviderProperties.baseUri(BASE_URI).mediaResourceMimeType("mediatype").build();
    ODataResponse response = ser.writeFeed(view, this.roomsData, properties);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:feed", xmlString);
    assertXpathEvaluatesTo(BASE_URI.toASCIIString(), "/a:feed/@xml:base", xmlString);
  }

  @Test
  public void testSelfLink() throws Exception {
    ODataEntityProvider ser = createAtomEntityProvider();
    ODataEntityProviderProperties properties = ODataEntityProviderProperties.baseUri(BASE_URI).mediaResourceMimeType("mediatype").build();
    ODataResponse response = ser.writeFeed(view, this.roomsData, properties);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:feed/a:link[@rel='self']", xmlString);
    assertXpathEvaluatesTo("Rooms", "/a:feed/a:link[@rel='self']/@href", xmlString);
    assertXpathEvaluatesTo("Rooms", "/a:feed/a:link[@rel='self']/@title", xmlString);
  }

  @Test
  public void testFeedMandatoryParts() throws Exception {
    ODataEntityProvider ser = createAtomEntityProvider();
    ODataEntityProviderProperties properties = ODataEntityProviderProperties.baseUri(BASE_URI).mediaResourceMimeType("mediatype").build();
    ODataResponse response = ser.writeFeed(view, this.roomsData, properties);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:feed/a:id", xmlString);
    assertXpathEvaluatesTo(BASE_URI.toASCIIString() + "Rooms", "/a:feed/a:id/text()", xmlString);

    assertXpathExists("/a:feed/a:title", xmlString);
    assertXpathEvaluatesTo("Rooms", "/a:feed/a:title/text()", xmlString);

    assertXpathExists("/a:feed/a:updated", xmlString);
    assertXpathExists("/a:feed/a:author", xmlString);
    assertXpathExists("/a:feed/a:author/a:name", xmlString);
  }

  private String verifyResponse(ODataResponse response) throws IOException {
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertEquals(ContentType.APPLICATION_ATOM_XML_FEED.toString() + "; charset=utf-8", response.getContentHeader());
    String xmlString = StringHelper.inputStreamToString((InputStream)response.getEntity());
    return xmlString;
  }

  @Test
  public void testInlineCountAllpages() throws Exception {
    initializeRoomData(20);
    when(view.getInlineCount()).thenReturn(InlineCount.ALLPAGES);

    ODataEntityProvider ser = createAtomEntityProvider();
    ODataEntityProviderProperties properties = ODataEntityProviderProperties.baseUri(BASE_URI)
        .mediaResourceMimeType("mediatype")
        .inlineCount(Integer.valueOf(103))
        .build();
    ODataResponse response = ser.writeFeed(view, this.roomsData, properties);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:feed/m:count", xmlString);
    assertXpathEvaluatesTo("103", "/a:feed/m:count/text()", xmlString);
  }

  @Test
  public void testInlineCountNone() throws Exception {
    when(view.getInlineCount()).thenReturn(InlineCount.NONE);

    ODataEntityProvider ser = createAtomEntityProvider();
    ODataEntityProviderProperties properties = ODataEntityProviderProperties.baseUri(BASE_URI).mediaResourceMimeType("mediatype").build();
    ODataResponse response = ser.writeFeed(view, this.roomsData, properties);
    String xmlString = verifyResponse(response);

    assertXpathNotExists("/a:feed/m:count", xmlString);
  }

  @Test
  public void testNextLink() throws Exception {
    when(view.getInlineCount()).thenReturn(InlineCount.NONE);

    ODataEntityProvider ser = createAtomEntityProvider();
    ODataEntityProviderProperties properties = ODataEntityProviderProperties.baseUri(BASE_URI)
        .mediaResourceMimeType("mediatype")
        .skipToken("äbc")
        .build();
    ODataResponse response = ser.writeFeed(view, this.roomsData, properties);
    String xmlString = verifyResponse(response);

    System.out.println(xmlString);

    assertXpathExists("/a:feed/a:link[@rel='next']", xmlString);
    assertXpathEvaluatesTo("Rooms?$skiptoken=%C3%A4bc", "/a:feed/a:link[@rel='next']/@href", xmlString);
  }

  @Test
  public void testNoNextLink() throws Exception {
    when(view.getInlineCount()).thenReturn(InlineCount.NONE);

    ODataEntityProvider ser = createAtomEntityProvider();
    ODataEntityProviderProperties properties = ODataEntityProviderProperties.baseUri(BASE_URI).mediaResourceMimeType("mediatype").build();
    ODataResponse response = ser.writeFeed(view, this.roomsData, properties);
    String xmlString = verifyResponse(response);

    System.out.println(xmlString);

    assertXpathNotExists("/a:feed/a:link[@rel='next']", xmlString);
  }

  @Test(expected = ODataEntityProviderException.class)
  public void testInlineCountInvalid() throws Exception {
    when(view.getInlineCount()).thenReturn(InlineCount.ALLPAGES);

    ODataEntityProvider ser = createAtomEntityProvider();
    ODataEntityProviderProperties properties = ODataEntityProviderProperties.baseUri(BASE_URI).mediaResourceMimeType("mediatype").build();
    ser.writeFeed(view, this.roomsData, properties);
  }

  @Test
  public void testEntries() throws Exception {
    initializeRoomData(103);

    ODataEntityProvider ser = createAtomEntityProvider();
    ODataEntityProviderProperties properties = ODataEntityProviderProperties.baseUri(BASE_URI).mediaResourceMimeType("mediatype").build();
    ODataResponse response = ser.writeFeed(view, this.roomsData, properties);
    String xmlString = verifyResponse(response);

    assertXpathExists("/a:feed/a:entry[1]", xmlString);
    assertXpathExists("/a:feed/a:entry[2]", xmlString);
    assertXpathExists("/a:feed/a:entry[103]", xmlString);
  }

}
