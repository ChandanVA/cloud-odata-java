package com.sap.core.odata.core.ep;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Locale;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.core.odata.api.edm.Edm;
import com.sap.core.odata.api.enums.ContentType;
import com.sap.core.odata.core.ep.util.CircleStreamBuffer;

/**
 * @author SAP AG
 */
public class ODataExceptionSerializer {

  private final static Logger LOG = LoggerFactory.getLogger(ODataExceptionSerializer.class);

  public static InputStream serialize(String errorCode, String message, ContentType contentType, Locale locale) {

    InputStream returnMessage;

    switch (contentType.getODataFormat()) {
    case XML:
    case ATOM:
      returnMessage = serializeXml(errorCode, message, locale);
      break;
    case JSON:
      returnMessage = serializeJson(errorCode, message, locale);
      break;
    default:
      returnMessage = serializeXml(errorCode, message, locale);
      break;
    }

    return returnMessage;
  }

  private static InputStream serializeJson(String errorCode, String message, Locale locale) {
    String notsupported = "not supported error format JSON";
    return new ByteArrayInputStream(notsupported.getBytes(Charset.forName("utf-8")));
  }

  private static InputStream serializeXml(String errorCode, String message, Locale locale) {
    InputStream outputMessage = null;
    try {
      CircleStreamBuffer csb = new CircleStreamBuffer();
      OutputStream outputStream = csb.getOutputStream();
      OutputStreamWriter writer = new OutputStreamWriter(
          outputStream, "utf-8");
      XMLStreamWriter xmlStreamWriter = XMLOutputFactory
          .newInstance().createXMLStreamWriter(writer);

      xmlStreamWriter.writeStartDocument();
      xmlStreamWriter.writeStartElement("error");
      xmlStreamWriter.writeDefaultNamespace(Edm.NAMESPACE_M_2007_08);
      xmlStreamWriter.writeStartElement("code");
      xmlStreamWriter.writeCharacters(errorCode);
      xmlStreamWriter.writeEndElement();
      xmlStreamWriter.writeStartElement("message");
      xmlStreamWriter.writeAttribute(Edm.PREFIX_XML, Edm.NAMESPACE_XML_1998, "lang", getLang(locale));
      xmlStreamWriter.writeCharacters(message == null ? "" : message);
      xmlStreamWriter.writeEndDocument();

      outputStream.flush();
      writer.flush();
      xmlStreamWriter.flush();

      outputMessage = csb.getInputStream();
    } catch (Exception e) {
      LOG.error("Fatal Error when serializing an Exception.", e);
    }
    return outputMessage;
  }

  /**
   * Gets language and country as defined in RFC 4646 based on {@link Locale}.
   */
  private static String getLang(Locale locale) {
    if (locale.getCountry().isEmpty()) {
      return locale.getLanguage();
    }
    return locale.getLanguage() + "-" + locale.getCountry();
  }

}
