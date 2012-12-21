package com.sap.core.odata.core.ep;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.sap.core.odata.api.edm.Edm;
import com.sap.core.odata.api.edm.EdmLiteralKind;
import com.sap.core.odata.api.edm.EdmSimpleType;
import com.sap.core.odata.api.edm.EdmSimpleTypeException;
import com.sap.core.odata.api.ep.ODataEntityProviderException;
import com.sap.core.odata.api.ep.ODataEntityProviderProperties;
import com.sap.core.odata.core.ep.aggregator.EntityInfoAggregator;
import com.sap.core.odata.core.ep.aggregator.EntityPropertyInfo;
import com.sap.core.odata.core.ep.util.UriUtils;

/**
 * Provider for writing a link
 * @author SAP AG
 */
public class XmlLinkEntityProvider {

  private final ODataEntityProviderProperties properties;

  XmlLinkEntityProvider(final ODataEntityProviderProperties properties) throws ODataEntityProviderException {
    this.properties = properties;
  }

  public void append(XMLStreamWriter writer, final EntityInfoAggregator entityInfo, final Map<String, Object> data, final boolean isRootElement) throws ODataEntityProviderException {
    try {
      writer.writeStartElement(FormatXml.D_URI);
      if (isRootElement)
        writer.writeDefaultNamespace(Edm.NAMESPACE_D_2007_08);
      writer.writeCharacters(createAbsoluteUri(entityInfo, data));
      writer.writeEndElement();
      writer.flush();
    } catch (XMLStreamException e) {
      throw new ODataEntityProviderException(ODataEntityProviderException.COMMON, e);
    }
  }

  private String createAbsoluteUri(final EntityInfoAggregator entityInfo, final Map<String, Object> data) throws ODataEntityProviderException {
    try {
      return UriUtils.encodeUri(properties.getBaseUri(),
          (entityInfo.isDefaultEntityContainer() ? "" : entityInfo.getEntityContainerName() + Edm.DELIMITER)
              + entityInfo.getEntitySetName()
              + "(" + createEntryKey(entityInfo, data) + ")");
    } catch (URISyntaxException e) {
      throw new ODataEntityProviderException(ODataEntityProviderException.COMMON, e);
    }
  }

  private static String createEntryKey(final EntityInfoAggregator entityInfo, final Map<String, Object> data) throws ODataEntityProviderException {
    final List<EntityPropertyInfo> keyPropertyInfos = entityInfo.getKeyPropertyInfos();
    String keys = "";

    for (final EntityPropertyInfo keyPropertyInfo : keyPropertyInfos) {
      if (!keys.isEmpty())
        keys += ",";
      if (keyPropertyInfos.size() > 1)
        keys += keyPropertyInfo.getName() + "=";
      final EdmSimpleType type = (EdmSimpleType) keyPropertyInfo.getType();
      try {
        keys += type.valueToString(data.get(keyPropertyInfo.getName()), EdmLiteralKind.URI, keyPropertyInfo.getFacets());
      } catch (EdmSimpleTypeException e) {
        throw new ODataEntityProviderException(ODataEntityProviderException.COMMON, e);
      }
    }

    return keys;
  }
}
