package com.sap.core.odata.core.ep;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sap.core.odata.api.edm.Edm;
import com.sap.core.odata.api.edm.EdmEntitySet;
import com.sap.core.odata.api.edm.EdmFunctionImport;
import com.sap.core.odata.api.edm.EdmProperty;
import com.sap.core.odata.api.edm.provider.EdmProvider;
import com.sap.core.odata.api.edm.provider.Schema;
import com.sap.core.odata.api.ep.EntityProvider.EntityProviderInterface;
import com.sap.core.odata.api.ep.EntityProviderBatchProperties;
import com.sap.core.odata.api.ep.EntityProviderException;
import com.sap.core.odata.api.ep.EntityProviderReadProperties;
import com.sap.core.odata.api.ep.EntityProviderWriteProperties;
import com.sap.core.odata.api.ep.entry.ODataEntry;
import com.sap.core.odata.api.ep.feed.ODataFeed;
import com.sap.core.odata.api.exception.ODataNotAcceptableException;
import com.sap.core.odata.api.processor.ODataErrorContext;
import com.sap.core.odata.api.processor.ODataResponse;
import com.sap.core.odata.api.servicedocument.ServiceDocument;
import com.sap.core.odata.core.ODataRequestHandler;
import com.sap.core.odata.core.batch.BatchRequestParser2;
import com.sap.core.odata.core.batch.BatchWriter;
import com.sap.core.odata.core.batch.Batchpart;
import com.sap.core.odata.core.commons.ContentType;
import com.sap.core.odata.core.edm.parser.EdmxProvider;
import com.sap.core.odata.core.edm.provider.EdmImplProv;
import com.sap.core.odata.core.exception.ODataRuntimeException;

/**
 * @author SAP AG
 */
public class ProviderFacadeImpl implements EntityProviderInterface {

  private static BasicEntityProvider create() throws EntityProviderException {
    return new BasicEntityProvider();
  }

  private static ContentTypeBasedEntityProvider create(final String contentType) throws EntityProviderException {
    return create(ContentType.create(contentType));
  }

  private static ContentTypeBasedEntityProvider create(final ContentType contentType) throws EntityProviderException {
    try {
      switch (contentType.getODataFormat()) {
      case ATOM:
      case XML:
        return new AtomEntityProvider(contentType.getODataFormat());
      case JSON:
        return new JsonEntityProvider();
      default:
        throw new ODataNotAcceptableException(ODataNotAcceptableException.NOT_SUPPORTED_CONTENT_TYPE.addContent(contentType));
      }
    } catch (final ODataNotAcceptableException e) {
      throw new EntityProviderException(EntityProviderException.COMMON, e);
    }
  }

  @Override
  public ODataResponse writeErrorDocument(final ODataErrorContext context) {
    try {
      return create(context.getContentType()).writeErrorDocument(context.getHttpStatus(), context.getErrorCode(), context.getMessage(), context.getLocale(), context.getInnerError());
    } catch (EntityProviderException e) {
      throw new ODataRuntimeException(e);
    }
  }

  @Override
  public ODataResponse writeServiceDocument(final String contentType, final Edm edm, final String serviceRoot) throws EntityProviderException {
    return create(contentType).writeServiceDocument(edm, serviceRoot);
  }

  @Override
  public ODataResponse writePropertyValue(final EdmProperty edmProperty, final Object value) throws EntityProviderException {
    return create().writePropertyValue(edmProperty, value);
  }

  @Override
  public ODataResponse writeText(final String value) throws EntityProviderException {
    return create().writeText(value);
  }

  @Override
  public ODataResponse writeBinary(final String mimeType, final byte[] data) throws EntityProviderException {
    return create().writeBinary(mimeType, data);
  }

  @Override
  public ODataResponse writeFeed(final String contentType, final EdmEntitySet entitySet, final List<Map<String, Object>> data, final EntityProviderWriteProperties properties) throws EntityProviderException {
    return create(contentType).writeFeed(entitySet, data, properties);
  }

  @Override
  public ODataResponse writeEntry(final String contentType, final EdmEntitySet entitySet, final Map<String, Object> data, final EntityProviderWriteProperties properties) throws EntityProviderException {
    return create(contentType).writeEntry(entitySet, data, properties);
  }

  @Override
  public ODataResponse writeProperty(final String contentType, final EdmProperty edmProperty, final Object value) throws EntityProviderException {
    return create(contentType).writeProperty(edmProperty, value);
  }

  @Override
  public ODataResponse writeLink(final String contentType, final EdmEntitySet entitySet, final Map<String, Object> data, final EntityProviderWriteProperties properties) throws EntityProviderException {
    return create(contentType).writeLink(entitySet, data, properties);
  }

  @Override
  public ODataResponse writeLinks(final String contentType, final EdmEntitySet entitySet, final List<Map<String, Object>> data, final EntityProviderWriteProperties properties) throws EntityProviderException {
    return create(contentType).writeLinks(entitySet, data, properties);
  }

  @Override
  public ODataResponse writeFunctionImport(final String contentType, final EdmFunctionImport functionImport, final Object data, final EntityProviderWriteProperties properties) throws EntityProviderException {
    return create(contentType).writeFunctionImport(functionImport, data, properties);
  }

  @Override
  public ODataFeed readFeed(final String contentType, final EdmEntitySet entitySet, final InputStream content, final EntityProviderReadProperties properties) throws EntityProviderException {
    return create(contentType).readFeed(entitySet, content, properties);
  }

  @Override
  public ODataEntry readEntry(final String contentType, final EdmEntitySet entitySet, final InputStream content, final EntityProviderReadProperties properties) throws EntityProviderException {
    return create(contentType).readEntry(entitySet, content, properties);
  }

  @Override
  public Map<String, Object> readProperty(final String contentType, final EdmProperty edmProperty, final InputStream content, final EntityProviderReadProperties properties) throws EntityProviderException {
    return create(contentType).readProperty(edmProperty, content, properties);
  }

  @Override
  public Object readPropertyValue(final EdmProperty edmProperty, final InputStream content, final Class<?> typeMapping) throws EntityProviderException {
    return create().readPropertyValue(edmProperty, content, typeMapping);
  }

  @Override
  public List<String> readLinks(final String contentType, final EdmEntitySet entitySet, final InputStream content) throws EntityProviderException {
    return create(contentType).readLinks(entitySet, content);
  }

  @Override
  public String readLink(final String contentType, final EdmEntitySet entitySet, final InputStream content) throws EntityProviderException {
    return create(contentType).readLink(entitySet, content);
  }

  @Override
  public byte[] readBinary(final InputStream content) throws EntityProviderException {
    return create().readBinary(content);
  }

  @Override
  public ODataResponse writeMetadata(final List<Schema> schemas, final Map<String, String> predefinedNamespaces) throws EntityProviderException {
    return create().writeMetadata(schemas, predefinedNamespaces);
  }

  @Override
  public Edm readMetadata(final InputStream inputStream, final boolean validate) throws EntityProviderException {
    EdmProvider provider = new EdmxProvider().parse(inputStream, validate);
    return new EdmImplProv(provider);
  }

  @Override
  public ServiceDocument readServiceDocument(final InputStream serviceDocument, final String contentType) throws EntityProviderException {
    return create(contentType).readServiceDocument(serviceDocument);
  }
  @Override
  public ODataResponse writeBatch(final String contentType, final InputStream content, final EntityProviderBatchProperties properties) throws EntityProviderException {
     ODataRequestHandler requestHandler = new ODataRequestHandler(properties.getServiceFactory());
     BatchWriter batchWriter = new BatchWriter();
     List<Batchpart> batchParts = new BatchRequestParser2(contentType, properties).parse(content);
     List<ODataResponse> responses = new ArrayList<ODataResponse>();
     for (Batchpart batchPart: batchParts) {
       responses.add(batchPart.processWithResponse(requestHandler, batchWriter));
     }
     return batchWriter.write(responses);
   }

  /*@Override
  public ODataResponse writeBatch(final String contentType, final InputStream content, final EntityProviderBatchProperties properties) throws EntityProviderException {
    ODataRequestHandler requestHandler = new ODataRequestHandler(properties.getServiceFactory());
    List<Batchpart> batchParts = new BatchRequestParser2(contentType, properties).parse(content);
    BatchWriter batchWriter = new BatchWriter();
    String boundary = "batch_" + UUID.randomUUID().toString();
    for (Batchpart batchPart : batchParts) {
      batchPart.process(requestHandler, batchWriter, boundary);
    }
    ODataResponse response = batchWriter.createResponse(boundary);
    return response;
  }*/
}
