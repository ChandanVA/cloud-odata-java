package com.sap.core.odata.core.ep.consumer;

import static com.sap.core.odata.core.ep.util.FormatXml.ATOM_CONTENT;
import static com.sap.core.odata.core.ep.util.FormatXml.ATOM_ENTRY;
import static com.sap.core.odata.core.ep.util.FormatXml.ATOM_HREF;
import static com.sap.core.odata.core.ep.util.FormatXml.ATOM_ID;
import static com.sap.core.odata.core.ep.util.FormatXml.ATOM_LINK;
import static com.sap.core.odata.core.ep.util.FormatXml.ATOM_REL;
import static com.sap.core.odata.core.ep.util.FormatXml.ATOM_SRC;
import static com.sap.core.odata.core.ep.util.FormatXml.M_ETAG;
import static com.sap.core.odata.core.ep.util.FormatXml.M_PROPERTIES;
import static com.sap.core.odata.core.ep.util.FormatXml.M_TYPE;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.sap.core.odata.api.edm.Edm;
import com.sap.core.odata.api.edm.EdmException;
import com.sap.core.odata.api.ep.EntityProviderException;
import com.sap.core.odata.api.ep.entry.ODataEntry;
import com.sap.core.odata.core.ep.aggregator.EntityInfoAggregator;
import com.sap.core.odata.core.ep.aggregator.EntityPropertyInfo;
import com.sap.core.odata.core.ep.entry.EntryMetadataImpl;
import com.sap.core.odata.core.ep.entry.MediaMetadataImpl;
import com.sap.core.odata.core.ep.entry.ODataEntryImpl;

/**
 * Atom/XML format reader/consumer for entries.
 */
public class XmlEntryConsumer {

//  private static final String DEFAULT_NS_PREFIX = "";
  
  final Map<String, String> foundPrefix2NamespaceUri;
  final ODataEntryImpl readEntryResult;
  final Map<String, Object> properties;
  final MediaMetadataImpl mediaMetadata;
  final EntryMetadataImpl entryMetadata;

  public XmlEntryConsumer() {
    properties = new HashMap<String, Object>();
    mediaMetadata = new MediaMetadataImpl();
    entryMetadata = new EntryMetadataImpl();
    readEntryResult = new ODataEntryImpl(properties, mediaMetadata, entryMetadata);
    foundPrefix2NamespaceUri = new HashMap<String, String>();
  }

  public ODataEntry readEntry(final XMLStreamReader reader, final EntityInfoAggregator eia, final boolean merge) throws EntityProviderException {
    try {
      int eventType;
      while ((eventType = reader.next()) != XMLStreamConstants.END_DOCUMENT) {
        if (eventType == XMLStreamConstants.START_ELEMENT) {
          String tagName = reader.getLocalName();
          handleStartedTag(reader, tagName, eia);
        }
      }

      if (!merge) {
        readEntryResult.validate(eia);
      }

      return readEntryResult;
    } catch (EntityProviderException e) {
      throw e;
    } catch (Exception e) {
      throw new EntityProviderException(EntityProviderException.COMMON, e);
    }
  }

  /**
   * 
   * @param reader
   * @param tagName
   * @param eia 
   * @throws EntityProviderException
   * @throws XMLStreamException
   * @throws EdmException
   */
  private void handleStartedTag(final XMLStreamReader reader, final String tagName, final EntityInfoAggregator eia) throws EntityProviderException, XMLStreamException, EdmException {
    if (ATOM_ID.equals(tagName)) {
      readId(reader);
    } else if (ATOM_ENTRY.equals(tagName)) {
      readEntry(reader);
    } else if (ATOM_LINK.equals(tagName)) {
      readLink(reader);
    } else if (ATOM_CONTENT.equals(tagName)) {
      readContent(reader, eia);
    } else if (M_PROPERTIES.equals(tagName)) {
      readProperties(reader, eia);
    } else {
      readCustomElement(reader, tagName, eia);
    }
  }

  private void readCustomElement(final XMLStreamReader reader, final String tagName, final EntityInfoAggregator eia) throws EdmException, EntityProviderException, XMLStreamException {
    EntityPropertyInfo targetPathInfo = eia.getTargetPathInfo(tagName);
    if (targetPathInfo != null) {
      String edmPrefix = targetPathInfo.getCustomMapping().getFcNsPrefix();
      String edmNamespaceURI = targetPathInfo.getCustomMapping().getFcNsUri();
      XmlPropertyConsumer xpc = new XmlPropertyConsumer();

      NamespaceContext nsctx = reader.getNamespaceContext();
      if (edmPrefix != null && edmNamespaceURI != null) {
        String xmlPrefix = nsctx.getPrefix(edmNamespaceURI);
        String xmlNamespaceUri = reader.getNamespaceURI(edmPrefix);

        if (edmNamespaceURI.equals(xmlNamespaceUri) && edmPrefix.equals(xmlPrefix)) {
          String name = reader.getLocalName();
          Object value = xpc.readStartedElement(reader, getValidatedPropertyInfo(eia, name));
          properties.put(name, value);
        }
      }

    }
  }

  private void readEntry(final XMLStreamReader reader) throws EntityProviderException, XMLStreamException {
    validateStartPosition(reader, ATOM_ENTRY);
    
    extractNamespacesFromTag(reader);

    
    Map<String, String> attributes = readAttributes(reader);

    String etag = attributes.get(M_ETAG);
    entryMetadata.setEtag(etag);
  }

  private void extractNamespacesFromTag(XMLStreamReader reader) throws EntityProviderException {
    // collect namespaces
    int namespaceCount = reader.getNamespaceCount();
    for (int i = 0; i < namespaceCount; i++) {
      String namespacePrefix = reader.getNamespacePrefix(i);
      String namespaceUri = reader.getNamespaceURI(i);
      
      foundPrefix2NamespaceUri.put(namespacePrefix, namespaceUri);
    }
  }
  
  private void checkAllMandatoryNamespacesAvailable() throws EntityProviderException {
    if(!foundPrefix2NamespaceUri.containsValue(Edm.NAMESPACE_D_2007_08)) {
      throw new EntityProviderException(EntityProviderException.INVALID_NAMESPACE.addContent(Edm.NAMESPACE_D_2007_08));
    } else if(!foundPrefix2NamespaceUri.containsValue(Edm.NAMESPACE_M_2007_08)) {
      throw new EntityProviderException(EntityProviderException.INVALID_NAMESPACE.addContent(Edm.NAMESPACE_M_2007_08));
    } else if(!foundPrefix2NamespaceUri.containsValue(Edm.NAMESPACE_ATOM_2005)) {
      throw new EntityProviderException(EntityProviderException.INVALID_NAMESPACE.addContent(Edm.NAMESPACE_ATOM_2005));
    }
  }

  private void checkNamespace(QName name, String expectedNsUriForPrefix) throws EntityProviderException {
    String nsPrefix = name.getPrefix();
//    String nsUri = name.getNamespaceURI();
    
    String atomNamespaceUri = foundPrefix2NamespaceUri.get(nsPrefix);
    if(atomNamespaceUri == null) {
      throw new EntityProviderException(EntityProviderException.INVALID_NAMESPACE.addContent(name.getLocalPart()));
    }
    if (!atomNamespaceUri.equals(expectedNsUriForPrefix)) {
      throw new EntityProviderException(EntityProviderException.INVALID_NAMESPACE.addContent(name.getLocalPart()));
    }
  }


  /**
   * 
   * @param reader
   * @throws EntityProviderException
   * @throws XMLStreamException
   */
  private void readLink(final XMLStreamReader reader) throws EntityProviderException, XMLStreamException {
    validateStartPosition(reader, ATOM_LINK);
    Map<String, String> attributes = readAttributes(reader);
    readAndValidateEndPosition(reader, ATOM_LINK);

    String uri = attributes.get(ATOM_HREF);
    String rel = attributes.get(ATOM_REL);

    if (rel == null || uri == null) {
      throw new EntityProviderException(EntityProviderException.MISSING_ATTRIBUTE.addContent(
          "'" + ATOM_HREF + "' and/or '" + ATOM_REL + "' at tag '" + ATOM_LINK + "'"));
    } else if (rel.startsWith(Edm.NAMESPACE_REL_2007_08)) {
      String navigationPropertyName = rel.substring(Edm.NAMESPACE_REL_2007_08.length());
      entryMetadata.putAssociationUri(navigationPropertyName, uri);
    } else if (rel.equals(Edm.LINK_REL_EDIT_MEDIA)) {
      mediaMetadata.setEditLink(uri);
      String etag = attributes.get(M_ETAG);
      mediaMetadata.setEtag(etag);
    }
  }

  private void readContent(final XMLStreamReader reader, final EntityInfoAggregator eia) throws EntityProviderException, XMLStreamException, EdmException {
    validateStartPosition(reader, ATOM_CONTENT);
    //
    extractNamespacesFromTag(reader);
    //
    checkAllMandatoryNamespacesAvailable();
    
    Map<String, String> attributes = readAttributes(reader);
    int nextEventType = reader.nextTag();

    if (XMLStreamConstants.END_ELEMENT == nextEventType) {
      validateEndPosition(reader, ATOM_CONTENT);
    } else if (XMLStreamConstants.START_ELEMENT == nextEventType && reader.getLocalName().equals(M_PROPERTIES)) {
      readProperties(reader, eia);
    } else {
      throw new EntityProviderException(EntityProviderException.INVALID_STATE
          .addContent("Expected closing 'content' or starting 'properties' but found '" + reader.getLocalName() + "'."));
    }
    //
    String contentType = attributes.get(M_TYPE);
    mediaMetadata.setContentType(contentType);
    String sourceLink = attributes.get(ATOM_SRC);
    mediaMetadata.setSourceLink(sourceLink);
  }

  private void readId(final XMLStreamReader reader) throws EntityProviderException, XMLStreamException {
    validateStartPosition(reader, ATOM_ID);
    int eventType = reader.next();
    String value = null;
    if (eventType == XMLStreamConstants.CHARACTERS) {
      value = reader.getText();
    }
    readAndValidateEndPosition(reader, ATOM_ID);

    entryMetadata.setId(value);
  }

  /**
   * 
   * @param reader
   * @param entitySet
   * @throws XMLStreamException
   * @throws EdmException
   * @throws EntityProviderException
   */
  private void readProperties(final XMLStreamReader reader, final EntityInfoAggregator entitySet) throws XMLStreamException, EdmException, EntityProviderException {
    //
//    extractNamespacesFromTag(reader);
    // validate namespace
    checkAllMandatoryNamespacesAvailable();
    checkNamespace(reader.getName(), Edm.NAMESPACE_M_2007_08);
    //
    int nextTagEventType = reader.next();

    XmlPropertyConsumer xpc = new XmlPropertyConsumer();
    boolean run = true;
    while (run) {
      if (nextTagEventType == XMLStreamConstants.START_ELEMENT) {
        String name = getValidPropertyName(reader);
        EntityPropertyInfo property = getValidatedPropertyInfo(entitySet, name);
        Object value = xpc.readStartedElement(reader, property);
        properties.put(name, value);
      } else if (nextTagEventType == XMLStreamConstants.END_ELEMENT) {
        String name = reader.getLocalName();
        if (M_PROPERTIES.equals(name)) {
          run = false;
        }
      }
      nextTagEventType = reader.next();
    }
  }

  /**
   * Get validated name for property of currently read tag in {@link XMLStreamReader}.
   * If validation fails an {@link EntityProviderException} is thrown.
   * 
   * Currently this is the case if tag has none or a wrong namespace set. 
   * Expected and valid namespace uri for edm properties is {@value Edm#NAMESPACE_D_2007_08}.
   * 
   * @param reader {@link XMLStreamReader} with position at to checked tag
   * @return valid tag name (which is never <code>NULL</code>).
   * @throws EntityProviderException
   */
  private String getValidPropertyName(XMLStreamReader reader) throws EntityProviderException {
    QName name = reader.getName();
    checkNamespace(name, Edm.NAMESPACE_D_2007_08);
    
    return name.getLocalPart();
  }

  /**
   * Get validated {@link EntityPropertyInfo} for property with given <code>name</code>.
   * If validation fails an {@link EntityProviderException} is thrown.
   * 
   * Currently this is the case if no {@link EntityPropertyInfo} if found for given <code>name</code>.
   * 
   * @param entitySet
   * @param name
   * @return valid {@link EntityPropertyInfo} (which is never <code>NULL</code>).
   * @throws EntityProviderException
   */
  private EntityPropertyInfo getValidatedPropertyInfo(final EntityInfoAggregator entitySet, final String name) throws EntityProviderException {
    EntityPropertyInfo info = entitySet.getPropertyInfo(name);
    if(info == null) {
      throw new EntityProviderException(EntityProviderException.INVALID_PROPERTY.addContent(name));
    }
    return info;
  }

  /**
   * Read all attributes for current element to map (key=AttributeName; value=AttributeValue).
   * 
   * @param reader
   * @return all the attributes for the current element
   */
  private Map<String, String> readAttributes(final XMLStreamReader reader) {
    int attributesCount = reader.getAttributeCount();

    Map<String, String> attributes = new HashMap<String, String>();
    for (int i = 0; i < attributesCount; i++) {
      String name = reader.getAttributeName(i).getLocalPart();
      String value = reader.getAttributeValue(i);
      attributes.put(name, value);
    }
    return attributes;
  }

  /**
   * 
   * @param reader
   * @param tagName
   * @throws EntityProviderException
   */
  private void validateStartPosition(final XMLStreamReader reader, final String tagName) throws EntityProviderException {
    validatePosition(reader, tagName, XMLStreamConstants.START_ELEMENT);
  }

  private void validateEndPosition(final XMLStreamReader reader, final String tagName) throws EntityProviderException {
    validatePosition(reader, tagName, XMLStreamConstants.END_ELEMENT);
  }

  private void readAndValidateEndPosition(final XMLStreamReader reader, final String tagName) throws EntityProviderException, XMLStreamException {
    readAndValidatePosition(reader, tagName, XMLStreamConstants.END_ELEMENT);
  }

  private void readAndValidatePosition(final XMLStreamReader reader, final String tagName, final int eventType) throws EntityProviderException, XMLStreamException {
    if (eventType != reader.next() || !reader.getLocalName().equals(tagName)) {
      String msg = "Invalid position for expected name=" + tagName + " event='" + eventType +
          "'; found name='" + reader.getLocalName() + "' event='" + reader.getEventType() + "'.";
      throw new EntityProviderException(EntityProviderException.INVALID_STATE.addContent(msg));
    }
  }

  private void validatePosition(final XMLStreamReader reader, final String tagName, final int eventType) throws EntityProviderException {
    if (eventType != reader.getEventType() || !reader.getLocalName().equals(tagName)) {
      String msg = "Invalid position for expected name=" + tagName + " event='" + eventType +
          "'; found name='" + reader.getLocalName() + "' event='" + reader.getEventType() + "'.";
      throw new EntityProviderException(EntityProviderException.INVALID_STATE.addContent(msg));
    }
  }
}
