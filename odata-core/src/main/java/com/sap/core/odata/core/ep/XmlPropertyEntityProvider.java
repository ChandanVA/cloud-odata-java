package com.sap.core.odata.core.ep;

import java.util.Collection;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.sap.core.odata.api.edm.Edm;
import com.sap.core.odata.api.edm.EdmCustomizableFeedMappings;
import com.sap.core.odata.api.edm.EdmException;
import com.sap.core.odata.api.edm.EdmFacets;
import com.sap.core.odata.api.edm.EdmLiteralKind;
import com.sap.core.odata.api.edm.EdmSimpleType;
import com.sap.core.odata.api.ep.ODataEntityProviderException;
import com.sap.core.odata.core.ep.aggregator.EntityComplexPropertyInfo;
import com.sap.core.odata.core.ep.aggregator.EntityPropertyInfo;

/**
 * Internal EntityProvider for simple and complex edm properties which are pre analyzed as {@link EntityPropertyInfo}.
 * 
 */
public class XmlPropertyEntityProvider {

  /**
   * Append {@link Object} <code>value</code> based on {@link EntityPropertyInfo} to {@link XMLStreamWriter}.
   * Based on <code>isRootElement</code> the default namespace is written or not.
   * 
   * @param writer
   * @param propertyInfo
   * @param value
   * @param isRootElement
   * @throws ODataEntityProviderException
   */
  public void append(XMLStreamWriter writer, EntityPropertyInfo propertyInfo, Object value, boolean isRootElement) throws ODataEntityProviderException {
    try {
      String name = propertyInfo.getName();

      if (isRootElement) {
        writer.writeStartElement(name);
        writer.writeDefaultNamespace(Edm.NAMESPACE_D_2007_08);
        writer.writeNamespace(Edm.PREFIX_M, Edm.NAMESPACE_M_2007_08);
      } else if (hasCustomNamespace(propertyInfo)) {
        writeStartElementWithCustomNamespace(writer, propertyInfo, name);
      } else {
        writer.writeStartElement(Edm.NAMESPACE_D_2007_08, name);
      }

      if (propertyInfo.isComplex()) {
        EntityComplexPropertyInfo ecp = (EntityComplexPropertyInfo) propertyInfo;
        appendProperty(writer, ecp, value);
      } else {
        appendProperty(writer, propertyInfo, value);
      }

      writer.writeEndElement();
    } catch (Exception e) {
      throw new ODataEntityProviderException(ODataEntityProviderException.COMMON, e);
    }
  }

  /**
   * 
   * @param writer
   * @param propertyInfo
   * @param value
   * @throws XMLStreamException
   * @throws EdmException
   * @throws ODataEntityProviderException
   */
  private void appendProperty(XMLStreamWriter writer, EntityComplexPropertyInfo propertyInfo, Object value) throws XMLStreamException, EdmException, ODataEntityProviderException {

    if (value == null) {
      writer.writeAttribute(Edm.NAMESPACE_M_2007_08, FormatXml.ATOM_NULL, FormatXml.ATOM_VALUE_TRUE);
    } else {
      writer.writeAttribute(Edm.NAMESPACE_M_2007_08, FormatXml.ATOM_TYPE, getFqnTypeName(propertyInfo));
      Collection<EntityPropertyInfo> propertyInfos = propertyInfo.getPropertyInfos();
      for (EntityPropertyInfo childPropertyInfo : propertyInfos) {
        Object childValue = extractChildValue(value, childPropertyInfo.getName());
        append(writer, childPropertyInfo, childValue, false);
      }
    }
  }

  /**
   * 
   * @param propertyInfo
   * @return
   * @throws EdmException
   */
  private String getFqnTypeName(EntityComplexPropertyInfo propertyInfo) throws EdmException {
    return propertyInfo.getType().getNamespace() + Edm.DELIMITER + propertyInfo.getType().getName();
  }

  /**
   * If <code>value</code> is a {@link Map} the element with given <code>name</code> as key is returned.
   * If <code>value</code> is NOT a {@link Map} its {@link String#valueOf(Object)} result is returned.
   * 
   * @param value
   * @param name
   * @return
   */
  private Object extractChildValue(Object value, String name) {
    if (value instanceof Map) {
      Map<?, ?> map = (Map<?, ?>) value;
      return map.get(name);
    }
    return String.valueOf(value);
  }

  /**
   * 
   * @param writer
   * @param prop
   * @param value
   * @throws XMLStreamException
   * @throws EdmException
   */
  private void appendProperty(XMLStreamWriter writer, EntityPropertyInfo prop, Object value) throws XMLStreamException, EdmException {
    EdmLiteralKind literalKind = EdmLiteralKind.DEFAULT;
    EdmFacets facets = prop.getFacets();
    EdmSimpleType st = (EdmSimpleType) prop.getType();
    String valueAsString = st.valueToString(value, literalKind, facets);

    if (valueAsString == null) {
      writer.writeAttribute(Edm.NAMESPACE_M_2007_08, FormatXml.ATOM_NULL, FormatXml.ATOM_VALUE_TRUE);
    } else {
      writer.writeCharacters(valueAsString);
    }
  }

  /**
   * 
   * @param writer
   * @param prop
   * @param name
   * @throws XMLStreamException
   */
  private void writeStartElementWithCustomNamespace(XMLStreamWriter writer, EntityPropertyInfo prop, String name) throws XMLStreamException {
    EdmCustomizableFeedMappings mapping = prop.getCustomMapping();
    String nsPrefix = mapping.getFcNsPrefix();
    String nsUri = mapping.getFcNsUri();
    writer.writeStartElement(nsPrefix, name, nsUri);
    writer.writeNamespace(nsPrefix, nsUri);
  }

  /**
   * 
   * @param prop
   * @return
   */
  private boolean hasCustomNamespace(EntityPropertyInfo prop) {
    if (prop.getCustomMapping() != null) {
      EdmCustomizableFeedMappings mapping = prop.getCustomMapping();
      return !(mapping.getFcNsPrefix() == null || mapping.getFcNsUri() == null);
    }
    return false;
  }

}
