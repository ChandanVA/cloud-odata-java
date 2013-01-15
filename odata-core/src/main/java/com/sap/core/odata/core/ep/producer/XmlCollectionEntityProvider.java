package com.sap.core.odata.core.ep.producer;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.sap.core.odata.api.edm.Edm;
import com.sap.core.odata.api.ep.EntityProviderException;
import com.sap.core.odata.core.ep.FormatXml;
import com.sap.core.odata.core.ep.aggregator.EntityPropertyInfo;

/**
 * Provider for writing a collection of simple-type or complex-type instances
 * @author SAP AG
 */
public class XmlCollectionEntityProvider {

  public static void append(XMLStreamWriter writer, final EntityPropertyInfo propertyInfo, final List<?> data) throws EntityProviderException {
    try {
      writer.writeStartElement(propertyInfo.getName());
      writer.writeDefaultNamespace(Edm.NAMESPACE_D_2007_08);
      if (propertyInfo.isComplex())
        writer.writeNamespace(Edm.PREFIX_M, Edm.NAMESPACE_M_2007_08);
      XmlPropertyEntityProvider provider = new XmlPropertyEntityProvider();
      for (final Object propertyData : data)
        provider.append(writer, FormatXml.D_ELEMENT, propertyInfo, propertyData);
      writer.writeEndElement();
      writer.flush();
    } catch (XMLStreamException e) {
      throw new EntityProviderException(EntityProviderException.COMMON, e);
    }
  }
}
