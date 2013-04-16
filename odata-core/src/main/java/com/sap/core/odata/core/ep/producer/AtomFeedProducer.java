package com.sap.core.odata.core.ep.producer;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamWriter;

import com.sap.core.odata.api.commons.InlineCount;
import com.sap.core.odata.api.edm.Edm;
import com.sap.core.odata.api.edm.EdmFacets;
import com.sap.core.odata.api.edm.EdmLiteralKind;
import com.sap.core.odata.api.ep.EntityProviderException;
import com.sap.core.odata.api.ep.EntityProviderProperties;
import com.sap.core.odata.core.commons.Encoder;
import com.sap.core.odata.core.edm.EdmDateTimeOffset;
import com.sap.core.odata.core.ep.aggregator.EntityInfoAggregator;
import com.sap.core.odata.core.ep.util.FormatXml;
import com.sap.core.odata.core.uri.SystemQueryOption;

/**
 * @author SAP AG
 */
public class AtomFeedProducer {

  private EntityProviderProperties properties;

  public AtomFeedProducer(EntityProviderProperties properties) {
    this.properties = properties;
  }

  public void append(XMLStreamWriter writer, EntityInfoAggregator eia, List<Map<String, Object>> data) throws EntityProviderException {
    try {
      writer.writeStartElement("feed");

      writer.writeDefaultNamespace(Edm.NAMESPACE_ATOM_2005);
      writer.writeNamespace(Edm.PREFIX_M, Edm.NAMESPACE_M_2007_08);
      writer.writeNamespace(Edm.PREFIX_D, Edm.NAMESPACE_D_2007_08);
      writer.writeAttribute(Edm.PREFIX_XML, Edm.NAMESPACE_XML_1998, "base", properties.getServiceRoot().toASCIIString());

      // write all atom infos (mandatory and optional)
      appendAtomMandatoryParts(writer, eia);
      appendAtomSelfLink(writer, eia);
      if (this.properties.getInlineCountType() == InlineCount.ALLPAGES) {
        appendInlineCount(writer, properties.getInlineCount());
      }

      appendEntries(writer, eia, data);

      if (properties.getSkipToken() != null) {
        appendNextLink(writer, eia, properties.getSkipToken());
      }

      writer.writeEndElement();
    } catch (Exception e) {
      throw new EntityProviderException(EntityProviderException.COMMON, e);
    }
  }

  private void appendNextLink(XMLStreamWriter writer, EntityInfoAggregator eia, String nextSkiptoken) throws EntityProviderException {
    try {
      String nextLink = createNextLink(eia, nextSkiptoken);

      writer.writeStartElement(FormatXml.ATOM_LINK);
      writer.writeAttribute(FormatXml.ATOM_HREF, nextLink);
      writer.writeAttribute(FormatXml.ATOM_REL, "next");
      writer.writeEndElement();
    } catch (Exception e) {
      throw new EntityProviderException(EntityProviderException.COMMON, e);
    }
  }

  private void appendEntries(XMLStreamWriter writer, EntityInfoAggregator eia, List<Map<String, Object>> data) throws EntityProviderException {
    AtomEntryEntityProducer entryProvider = new AtomEntryEntityProducer(properties);
    for (Map<String, Object> singleEntryData : data)
      entryProvider.append(writer, eia, singleEntryData, false);
  }

  private void appendInlineCount(XMLStreamWriter writer, int inlinecount) throws EntityProviderException {

    if (inlinecount < 0) {
      throw new EntityProviderException(EntityProviderException.INLINECOUNT_INVALID);
    }

    try {
      writer.writeStartElement(Edm.NAMESPACE_M_2007_08, FormatXml.M_COUNT);
      writer.writeCharacters(String.valueOf(inlinecount));
      writer.writeEndElement();
    } catch (Exception e) {
      throw new EntityProviderException(EntityProviderException.COMMON, e);
    }
  }

  private void appendAtomSelfLink(XMLStreamWriter writer, EntityInfoAggregator eia) throws EntityProviderException {
    try {
      String selfLink = createSelfLink(eia);

      writer.writeStartElement(FormatXml.ATOM_LINK);
      writer.writeAttribute(FormatXml.ATOM_HREF, selfLink);
      writer.writeAttribute(FormatXml.ATOM_REL, "self");
      writer.writeAttribute(FormatXml.ATOM_TITLE, eia.getEntitySetName());
      writer.writeEndElement();
    } catch (Exception e) {
      throw new EntityProviderException(EntityProviderException.COMMON, e);
    }
  }

  private String createNextLink(EntityInfoAggregator eia, String nextSkiptoken) throws EntityProviderException {
    try {
      String query = SystemQueryOption.$skiptoken + "=" + Encoder.encodeQueryPart(nextSkiptoken);
      String path = createSelfLink(eia);
      return path + "?" + query;
    } catch (Exception e) {
      throw new EntityProviderException(EntityProviderException.COMMON, e);
    }
  }

  private String createSelfLink(EntityInfoAggregator eia) throws EntityProviderException {
    try {
      StringBuilder sb = new StringBuilder();
      if (!eia.isDefaultEntityContainer()) {
        String entityContainerName = Encoder.encodePathPart(eia.getEntityContainerName());
        sb.append(entityContainerName).append(Edm.DELIMITER);
      }
      String entitySetName = Encoder.encodePathPart(eia.getEntitySetName());
      sb.append(entitySetName);
      return sb.toString();
    } catch (Exception e) {
      throw new EntityProviderException(EntityProviderException.COMMON, e);
    }
  }

  private void appendAtomMandatoryParts(XMLStreamWriter writer, EntityInfoAggregator eia) throws EntityProviderException {
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

    } catch (Exception e) {
      throw new EntityProviderException(EntityProviderException.COMMON, e);
    }
  }

  private String createAtomId(EntityInfoAggregator eia) throws EntityProviderException {
    try {
      String id = "";

      if (!eia.isDefaultEntityContainer()) {
        String entityContainerName = Encoder.encodeQueryPart(eia.getEntityContainerName());
        id += entityContainerName + ".";
      }
      String entitySetName = Encoder.encodeQueryPart(eia.getEntitySetName());
      id += entitySetName;

      URI serviceRoot = properties.getServiceRoot();
      return serviceRoot + id;
    } catch (Exception e) {
      throw new EntityProviderException(EntityProviderException.COMMON, e);
    }
  }
}
