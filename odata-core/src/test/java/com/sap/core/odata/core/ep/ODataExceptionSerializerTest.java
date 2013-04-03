package com.sap.core.odata.core.ep;

import static junit.framework.Assert.assertEquals;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathNotExists;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sap.core.odata.api.commons.HttpStatusCodes;
import com.sap.core.odata.api.edm.Edm;
import com.sap.core.odata.api.ep.EntityProviderException;
import com.sap.core.odata.api.processor.ODataResponse;
import com.sap.core.odata.core.commons.ContentType;
import com.sap.core.odata.testutil.fit.BaseTest;
import com.sap.core.odata.testutil.helper.StringHelper;

/**
 * @author SAP AG
 *
 */
public class ODataExceptionSerializerTest extends BaseTest {

  @BeforeClass
  public static void setup() throws Exception {
    Map<String, String> prefixMap = new HashMap<String, String>();
    prefixMap.put("a", Edm.NAMESPACE_M_2007_08);
    XMLUnit.setXpathNamespaceContext(new SimpleNamespaceContext(prefixMap));
  }

  @Test
  public void testXMLSerializationWithoutInnerError() throws Exception {
    testSerializeXML("ErrorCode", "Message", ContentType.APPLICATION_XML, Locale.GERMAN);
    testSerializeXML("ErrorCode", "Message", ContentType.APPLICATION_XML, Locale.ENGLISH);
    testSerializeXML("ErrorCode", "Message", ContentType.APPLICATION_XML, Locale.CANADA);
    testSerializeXML("ErrorCode", "Message", ContentType.APPLICATION_XML, Locale.FRANCE);
    testSerializeXML("ErrorCode", "Message", ContentType.APPLICATION_XML, Locale.CHINA);

    testSerializeXML("ErrorCode", "Message", ContentType.APPLICATION_ATOM_XML, Locale.GERMAN);
    testSerializeXML("ErrorCode", "Message", ContentType.APPLICATION_ATOM_XML, Locale.ENGLISH);
    testSerializeXML("ErrorCode", "Message", ContentType.APPLICATION_ATOM_XML, Locale.CANADA);
    testSerializeXML("ErrorCode", "Message", ContentType.APPLICATION_ATOM_XML, Locale.FRANCE);
    testSerializeXML("ErrorCode", "Message", ContentType.APPLICATION_ATOM_XML, Locale.CHINA);
  }

  //  @Test
  //  public void testXMLSerializationWithoutLocale() throws Exception {
  //    testSerializeXML("ErrorCode", "Message", null, ContentType.APPLICATION_XML, null);
  //  }

  @Test
  public void testXMLSerializationWithoutMessage() throws Exception {
    testSerializeXML("ErrorCode", null, ContentType.APPLICATION_XML, Locale.GERMAN);
  }

  @Test
  public void testXMLSerializationWithoutAll() throws Exception {
    testSerializeXML(null, null, ContentType.APPLICATION_XML, Locale.GERMAN);
  }

  @Test
  public void testXMLSerializationWithInnerError() throws Exception {
    testSerializeXML("ErrorCode", "Message", ContentType.APPLICATION_XML, Locale.GERMAN);
    testSerializeXML("ErrorCode", "Message", ContentType.APPLICATION_XML, Locale.ENGLISH);
    testSerializeXML("ErrorCode", "Message", ContentType.APPLICATION_XML, Locale.CANADA);
    testSerializeXML("ErrorCode", "Message", ContentType.APPLICATION_XML, Locale.FRANCE);
    testSerializeXML("ErrorCode", "Message", ContentType.APPLICATION_XML, Locale.CHINA);

    testSerializeXML("ErrorCode", "Message", ContentType.APPLICATION_ATOM_XML, Locale.GERMAN);
    testSerializeXML("ErrorCode", "Message", ContentType.APPLICATION_ATOM_XML, Locale.ENGLISH);
    testSerializeXML("ErrorCode", "Message", ContentType.APPLICATION_ATOM_XML, Locale.CANADA);
    testSerializeXML("ErrorCode", "Message", ContentType.APPLICATION_ATOM_XML, Locale.FRANCE);
    testSerializeXML("ErrorCode", "Message", ContentType.APPLICATION_ATOM_XML, Locale.CHINA);
  }

  @Test(expected = EntityProviderException.class)
  public void testJsonSerializationWithoutInnerError() throws Exception {
    testSerializeJSON("ErrorCode", "Message", ContentType.APPLICATION_JSON, Locale.GERMAN);
  }

  @Test(expected = EntityProviderException.class)
  public void testJsonSerializationWithInnerError() throws Exception {
    testSerializeJSON("ErrorCode", "Message", ContentType.APPLICATION_JSON, Locale.GERMAN);
  }

  //HelperMethod
  private void testSerializeJSON(final String errorCode, final String message, final ContentType contentType, final Locale locale) throws Exception {
    ODataResponse response = new ProviderFacadeImpl().writeErrorDocument(contentType.toContentTypeString(), HttpStatusCodes.INTERNAL_SERVER_ERROR, errorCode, message, locale, null);
    String jsonErrorMessage = (String) response.getEntity();
    assertEquals("Fatal Error when serializing an Exception", jsonErrorMessage);
  }

  //HelperMethod
  private void testSerializeXML(final String errorCode, final String message, final ContentType contentType, final Locale locale) throws Exception {
    ODataResponse response = new ProviderFacadeImpl().writeErrorDocument(contentType.toContentTypeString(), HttpStatusCodes.INTERNAL_SERVER_ERROR, errorCode, message, locale, null);
    String xmlErrorMessage = StringHelper.inputStreamToString((InputStream) response.getEntity());
    if (errorCode != null) {
      assertXpathEvaluatesTo(errorCode, "/a:error/a:code", xmlErrorMessage);
    } else {
      assertXpathExists("/a:error/a:code", xmlErrorMessage);
    }
    if (message != null) {
      assertXpathEvaluatesTo(message, "/a:error/a:message", xmlErrorMessage);
      assertXpathExists("/a:error/a:message[@xml:lang=\"" + getLang(locale) + "\"]", xmlErrorMessage);
    } else {
      assertXpathExists("/a:error/a:message", xmlErrorMessage);
    }

    assertXpathNotExists("/a:error/a:innererror", xmlErrorMessage);

  }

  //HelperMethod
  private String getLang(final Locale locale) {
    if (locale == null) {
      return "";
    }
    if (locale.getCountry().isEmpty()) {
      return locale.getLanguage();
    } else {
      return locale.getLanguage() + "-" + locale.getCountry();
    }
  }
}
