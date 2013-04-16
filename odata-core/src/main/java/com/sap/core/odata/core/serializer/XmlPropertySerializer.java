package com.sap.core.odata.core.serializer;

import java.util.Collection;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.sap.core.odata.api.edm.EdmComplexType;
import com.sap.core.odata.api.edm.EdmException;
import com.sap.core.odata.api.edm.EdmFacets;
import com.sap.core.odata.api.edm.EdmLiteralKind;
import com.sap.core.odata.api.edm.EdmProperty;
import com.sap.core.odata.api.edm.EdmSimpleType;
import com.sap.core.odata.api.edm.EdmType;
import com.sap.core.odata.api.serialization.ODataSerializationException;
import com.sap.core.odata.core.edm.EdmString;

public class XmlPropertySerializer {

  public void append(XMLStreamWriter writer, EdmProperty edmProperty, Object value, boolean isRootElement, AtomInfoAggregator aia) throws EdmException, XMLStreamException, ODataSerializationException {
    EdmType edmType = edmProperty.getType();

    String name = edmProperty.getName();

    if (isRootElement) {
      writer.writeStartElement(name);
      writer.writeDefaultNamespace(AtomEntrySerializer.NS_DATASERVICES);
      writer.writeNamespace("m", AtomEntrySerializer.NS_DATASERVICES_METADATA);
    } else {
      writer.writeStartElement(AtomEntrySerializer.NS_DATASERVICES, name);
    }

    if (edmType instanceof EdmSimpleType) {
      EdmSimpleType st = (EdmSimpleType) edmType;
      appendProperty(writer, st, edmProperty, value, aia);
    } else if (edmType instanceof EdmComplexType) {
      appendProperty(writer, (EdmComplexType) edmType, edmProperty, value, aia);
    } else {
      throw new ODataSerializationException(ODataSerializationException.UNSUPPORTED_PROPERTY_TYPE.addContent(edmType.getName()));
    }

    writer.writeEndElement();
  }

  private void appendProperty(XMLStreamWriter writer, EdmComplexType type, EdmProperty prop, Object value, AtomInfoAggregator aia) throws XMLStreamException, EdmException, ODataSerializationException {

    if (value == null) {
      writer.writeAttribute(AtomEntrySerializer.NS_DATASERVICES_METADATA, "null", "true");
    } else {
      Collection<String> propNames = type.getPropertyNames();
      for (String pName : propNames) {
        EdmProperty internalProperty = (EdmProperty) type.getProperty(pName);
        Object childValue = extractChildValue(value, pName);
        append(writer, internalProperty, childValue, false, aia);
      }
    }
  }

  private Object extractChildValue(Object value, String name) {
    if (value instanceof Map) {
      Map<?, ?> map = (Map<?, ?>) value;
      return map.get(name);
    }
    return String.valueOf(value);
  }

  private void appendProperty(XMLStreamWriter writer, EdmSimpleType st, EdmProperty prop, Object value, AtomInfoAggregator aia) throws XMLStreamException, EdmException {
    EdmLiteralKind literalKind = EdmLiteralKind.DEFAULT;
    EdmFacets facets = prop.getFacets();
    String valueAsString = st.valueToString(value, literalKind, facets);

    if (aia != null) {
      aia.addInfo(prop, valueAsString);
    }

    if (valueAsString == null) {
      writer.writeAttribute(AtomEntrySerializer.NS_DATASERVICES_METADATA, "null", "true");
    } else {
      if (!(st instanceof EdmString)) {
        writer.writeAttribute(AtomEntrySerializer.NS_DATASERVICES_METADATA, "type", st.getNamespace() + "." + st.getName());
      }

      writer.writeCharacters(valueAsString);
    }
  }

}
