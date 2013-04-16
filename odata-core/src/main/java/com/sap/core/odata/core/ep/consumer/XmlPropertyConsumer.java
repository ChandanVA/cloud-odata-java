package com.sap.core.odata.core.ep.consumer;

import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.sap.core.odata.api.edm.EdmException;
import com.sap.core.odata.api.edm.EdmFacets;
import com.sap.core.odata.api.edm.EdmLiteralKind;
import com.sap.core.odata.api.edm.EdmProperty;
import com.sap.core.odata.api.edm.EdmSimpleType;
import com.sap.core.odata.api.ep.EntityProviderException;
import com.sap.core.odata.core.ep.aggregator.EntityComplexPropertyInfo;
import com.sap.core.odata.core.ep.aggregator.EntityInfoAggregator;
import com.sap.core.odata.core.ep.aggregator.EntityPropertyInfo;

public class XmlPropertyConsumer {

  public Map<String, Object> readProperty(final XMLStreamReader reader, final EdmProperty property, final boolean merge) throws EntityProviderException {
    try {
      EntityPropertyInfo eia = EntityInfoAggregator.create(property);
      reader.next();

      Object value = readStartedElement(reader, eia);

      if (eia.isComplex() && merge) {
        mergeWithDefaultValues(value, eia);
      }

      Map<String, Object> result = new HashMap<String, Object>();
      result.put(property.getName(), value);
      return result;
    } catch (Exception e) {
      throw new EntityProviderException(EntityProviderException.COMMON, e);
    }
  }

  @SuppressWarnings("unchecked")
  private void mergeWithDefaultValues(final Object value, final EntityPropertyInfo epi) throws EntityProviderException {
    if (!(value instanceof Map)) {
      throw new EntityProviderException(EntityProviderException.COMMON);
    }
    if (!epi.isComplex()) {
      throw new EntityProviderException(EntityProviderException.COMMON);
    }

    mergeComplexWithDefaultValues((Map<String, Object>) value, (EntityComplexPropertyInfo) epi);
  }

  private void mergeComplexWithDefaultValues(final Map<String, Object> complexValue, final EntityComplexPropertyInfo ecpi) {
    for (EntityPropertyInfo info : ecpi.getPropertyInfos()) {
      Object obj = complexValue.get(info.getName());
      if (obj == null) {
        if (info.isComplex()) {
          Map<String, Object> defaultValue = new HashMap<String, Object>();
          mergeComplexWithDefaultValues(defaultValue, (EntityComplexPropertyInfo) ecpi);
          complexValue.put(info.getName(), defaultValue);
        } else {
          EdmFacets facets = info.getFacets();
          if (facets != null) {
            complexValue.put(info.getName(), facets.getDefaultValue());
          }
        }
      }
    }
  }

  Object readStartedElement(final XMLStreamReader reader, final EntityPropertyInfo propertyInfo) throws EntityProviderException, XMLStreamException, EdmException {
    //
    int eventType = reader.getEventType();
    if (eventType != XMLStreamConstants.START_ELEMENT) {
      throw new EntityProviderException(EntityProviderException.INVALID_STATE);
    }

    //
    Map<String, Object> name2Value = new HashMap<String, Object>();
    Object result = null;

    while (eventType != XMLStreamConstants.END_ELEMENT && eventType != XMLStreamConstants.END_DOCUMENT) {
      eventType = reader.next();
      if (XMLStreamConstants.START_ELEMENT == eventType && propertyInfo.isComplex()) {
        String childName = reader.getLocalName();
        EntityPropertyInfo childProperty = getChildProperty(childName, propertyInfo);

        Object value = readStartedElement(reader, childProperty);
        name2Value.put(childName, value);
      } else if (XMLStreamConstants.CHARACTERS == eventType && !propertyInfo.isComplex()) {
        result = convert(propertyInfo, reader.getText());
      }
    }

    // if reading finished check which result must be returned
    if (result != null) {
      return result;
    } else if (!name2Value.isEmpty()) {
      return name2Value;
    }
    return null;
  }

  private EntityPropertyInfo getChildProperty(final String childPropertyName, final EntityPropertyInfo property) throws EdmException, EntityProviderException {
    if (property.isComplex()) {
      EntityComplexPropertyInfo complex = (EntityComplexPropertyInfo) property;
      return complex.getPropertyInfo(childPropertyName);
    }
    throw new EntityProviderException(EntityProviderException.INVALID_PROPERTY.addContent(
        "Expected complex property but found simple for property with name '" + property.getName() + "'"));
  }

  private Object convert(final EntityPropertyInfo property, final String text) throws EdmException, EntityProviderException {
    if (!property.isComplex()) {
      EdmSimpleType type = (EdmSimpleType) property.getType();
      return type.valueOfString(text, EdmLiteralKind.DEFAULT, property.getFacets(), type.getDefaultType());
    }
    throw new EntityProviderException(EntityProviderException.INVALID_PROPERTY.addContent(
        "Expected simple property but found complex for property with name '" + property.getName() + "'"));
  }
}
