package com.sap.core.odata.core.ep.producer;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.sap.core.odata.api.commons.InlineCount;
import com.sap.core.odata.api.edm.Edm;
import com.sap.core.odata.api.edm.EdmFacets;
import com.sap.core.odata.api.edm.EdmLiteralKind;
import com.sap.core.odata.api.edm.EdmSimpleTypeException;
import com.sap.core.odata.api.ep.EntityProviderException;
import com.sap.core.odata.api.ep.EntityProviderProperties;
import com.sap.core.odata.core.commons.Encoder;
import com.sap.core.odata.core.edm.EdmDateTimeOffset;
import com.sap.core.odata.core.ep.aggregator.EntityInfoAggregator;
import com.sap.core.odata.core.ep.util.FormatXml;

/**
 * Serializes an ATOM feed.
 * @author SAP AG
 */
public class AtomFeedProducer {

  private final EntityProviderProperties properties;

  public AtomFeedProducer(final EntityProviderProperties properties) {
    this.properties = properties;
  }

  public void append(final XMLStreamWriter writer, final EntityInfoAggregator eia, final List<Map<String, Object>> data) throws EntityProviderException {
    try {
      writer.writeStartElement("feed");

      writer.writeDefaultNamespace(Edm.NAMESPACE_ATOM_2005);
      writer.writeNamespace(Edm.PREFIX_M, Edm.NAMESPACE_M_2007_08);
      writer.writeNamespace(Edm.PREFIX_D, Edm.NAMESPACE_D_2007_08);
      writer.writeAttribute(Edm.PREFIX_XML, Edm.NAMESPACE_XML_1998, "base", properties.getServiceRoot().toASCIIString());

      // write all atom infos (mandatory and optional)
      appendAtomMandatoryParts(writer, eia);
      appendAtomSelfLink(writer, eia);
      if (properties.getInlineCountType() == InlineCount.ALLPAGES) {
        appendInlineCount(writer, properties.getInlineCount());
      }

      appendEntries(writer, eia, data);

      if (properties.getNextLink() != null) {
        appendNextLink(writer, properties.getNextLink());
      }

      writer.writeEndElement();
    } catch (XMLStreamException e) {
      throw new EntityProviderException(EntityProviderException.COMMON, e);
    }
  }

  private void appendNextLink(final XMLStreamWriter writer, final String nextLink) throws EntityProviderException {
    try {
      writer.writeStartElement(FormatXml.ATOM_LINK);
      writer.writeAttribute(FormatXml.ATOM_HREF, nextLink);
      writer.writeAttribute(FormatXml.ATOM_REL, "next");
      writer.writeEndElement();
    } catch (XMLStreamException e) {
      throw new EntityProviderException(EntityProviderException.COMMON, e);
    }
  }

  private void appendEntries(final XMLStreamWriter writer, final EntityInfoAggregator eia, final List<Map<String, Object>> data) throws EntityProviderException {
    AtomEntryEntityProducer entryProvider = new AtomEntryEntityProducer(properties);
    for (Map<String, Object> singleEntryData : data) {
      entryProvider.append(writer, eia, singleEntryData, false, true);
    }
  }

  private void appendInlineCount(final XMLStreamWriter writer, final int inlinecount) throws EntityProviderException {
    if (inlinecount < 0) {
      throw new EntityProviderException(EntityProviderException.INLINECOUNT_INVALID);
    }
    try {
      writer.writeStartElement(Edm.NAMESPACE_M_2007_08, FormatXml.M_COUNT);
      writer.writeCharacters(String.valueOf(inlinecount));
      writer.writeEndElement();
    } catch (XMLStreamException e) {
      throw new EntityProviderException(EntityProviderException.COMMON, e);
    }
  }

  private void appendAtomSelfLink(final XMLStreamWriter writer, final EntityInfoAggregator eia) throws EntityProviderException {
    String selfLink = createSelfLink(eia);
    try {
      writer.writeStartElement(FormatXml.ATOM_LINK);
      writer.writeAttribute(FormatXml.ATOM_HREF, selfLink);
      writer.writeAttribute(FormatXml.ATOM_REL, "self");
      writer.writeAttribute(FormatXml.ATOM_TITLE, eia.getEntitySetName());
      writer.writeEndElement();
    } catch (XMLStreamException e) {
      throw new EntityProviderException(EntityProviderException.COMMON, e);
    }
  }

  private String createSelfLink(final EntityInfoAggregator eia) throws EntityProviderException {
    StringBuilder sb = new StringBuilder();
    if (!eia.isDefaultEntityContainer()) {
      String entityContainerName = Encoder.encode(eia.getEntityContainerName());
      sb.append(entityContainerName).append(Edm.DELIMITER);
    }
    String entitySetName = Encoder.encode(eia.getEntitySetName());
    sb.append(entitySetName);
    return sb.toString();
  }

  private void appendAtomMandatoryParts(final XMLStreamWriter writer, final EntityInfoAggregator eia) throws EntityProviderException {
    try {
      writer.writeStartElement(FormatXml.ATOM_ID);
      writer.writeCharacters(createAtomId(eia));
      writer.writeEndElement();

      writer.writeStartElement(FormatXml.ATOM_TITLE);
      writer.writeAttribute(FormatXml.M_TYPE, "text");
      writer.writeCharacters(eia.getEntitySetName());
      writer.writeEndElement();

      writer.writeStartElement(FormatXml.ATOM_UPDATED);

      Object updateDate = null;
      EdmFacets updateFacets = null;
      updateDate = new Date();
      writer.writeCharacters(EdmDateTimeOffset.getInstance().valueToString(updateDate, EdmLiteralKind.DEFAULT, updateFacets));
      writer.writeEndElement();

      writer.writeStartElement(FormatXml.ATOM_AUTHOR);
      writer.writeStartElement(FormatXml.ATOM_AUTHOR_NAME);
      writer.writeEndElement();
      writer.writeEndElement();

    } catch (XMLStreamException e) {
      throw new EntityProviderException(EntityProviderException.COMMON, e);
    } catch (EdmSimpleTypeException e) {
      throw new EntityProviderException(EntityProviderException.COMMON, e);
    }
  }

  private String createAtomId(final EntityInfoAggregator eia) throws EntityProviderException {
    String id = "";

    if (!eia.isDefaultEntityContainer()) {
      String entityContainerName = Encoder.encode(eia.getEntityContainerName());
      id += entityContainerName + ".";
    }
    String entitySetName = Encoder.encode(eia.getEntitySetName());
    id += entitySetName;

    URI serviceRoot = properties.getServiceRoot();
    return serviceRoot + id;
  }
}
