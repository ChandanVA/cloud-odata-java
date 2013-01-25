package com.sap.core.odata.core.processor;

import java.util.ArrayList;
import java.util.List;

import com.sap.core.odata.api.ODataService;
import com.sap.core.odata.api.ODataServiceFactory;
import com.sap.core.odata.api.ODataServiceVersion;
import com.sap.core.odata.api.commons.HttpContentType;
import com.sap.core.odata.api.edm.Edm;
import com.sap.core.odata.api.edm.provider.EdmProvider;
import com.sap.core.odata.api.exception.ODataException;
import com.sap.core.odata.api.exception.ODataNotImplementedException;
import com.sap.core.odata.api.processor.BatchProcessor;
import com.sap.core.odata.api.processor.EntityComplexPropertyProcessor;
import com.sap.core.odata.api.processor.EntityLinkProcessor;
import com.sap.core.odata.api.processor.EntityLinksProcessor;
import com.sap.core.odata.api.processor.EntityMediaProcessor;
import com.sap.core.odata.api.processor.EntityProcessor;
import com.sap.core.odata.api.processor.EntitySetProcessor;
import com.sap.core.odata.api.processor.EntitySimplePropertyProcessor;
import com.sap.core.odata.api.processor.EntitySimplePropertyValueProcessor;
import com.sap.core.odata.api.processor.FunctionImportProcessor;
import com.sap.core.odata.api.processor.FunctionImportValueProcessor;
import com.sap.core.odata.api.processor.MetadataProcessor;
import com.sap.core.odata.api.processor.ODataProcessor;
import com.sap.core.odata.api.processor.ODataSingleProcessor;
import com.sap.core.odata.api.processor.ServiceDocumentProcessor;
import com.sap.core.odata.api.processor.feature.CustomContentType;
import com.sap.core.odata.api.rt.RuntimeDelegate;

/**
 * <p>An {@link ODataService} implementation that uses {@link ODataSingleProcessor}.</p>
 * <p>Usually custom services create an instance by their implementation of
 * {@link ODataServiceFactory} and populate it with their custom {@link EdmProvider}
 * and custom {@link ODataSingleProcessor} implementations.</p>
 *
 * @author SAP AG
 */
public class ODataSingleProcessorService implements ODataService {

  private final ODataSingleProcessor processor;
  private final Edm edm;

  /**
   * Construct service
   * @param provider A custom {@link EdmProvider}
   * @param processor A custom {@link ODataSingleProcessor}
   */
  public ODataSingleProcessorService(EdmProvider provider, ODataSingleProcessor processor) {
    this.processor = processor;
    edm = RuntimeDelegate.createEdm(provider);
  }

  /**
   * @see ODataService
   */
  @Override
  public String getVersion() throws ODataException {
    return ODataServiceVersion.V20;
  }

  /**
   * @see ODataService
   */
  @Override
  public Edm getEntityDataModel() throws ODataException {
    return edm;
  }

  /**
   * @see ODataService
   */
  @Override
  public MetadataProcessor getMetadataProcessor() throws ODataException {
    return processor;
  }

  /**
   * @see ODataService
   */
  @Override
  public ServiceDocumentProcessor getServiceDocumentProcessor() throws ODataException {
    return processor;
  }

  /**
   * @see ODataService
   */
  @Override
  public EntityProcessor getEntityProcessor() throws ODataException {
    return processor;
  }

  /**
   * @see ODataService
   */
  @Override
  public EntitySetProcessor getEntitySetProcessor() throws ODataException {
    return processor;
  }

  /**
   * @see ODataService
   */
  @Override
  public EntityComplexPropertyProcessor getEntityComplexPropertyProcessor() throws ODataException {
    return processor;
  }

  /**
   * @see ODataService
   */
  @Override
  public EntityLinkProcessor getEntityLinkProcessor() throws ODataException {
    return processor;
  }

  /**
   * @see ODataService
   */
  @Override
  public EntityLinksProcessor getEntityLinksProcessor() throws ODataException {
    return processor;
  }

  /**
   * @see ODataService
   */
  @Override
  public EntityMediaProcessor getEntityMediaProcessor() throws ODataException {
    return processor;
  }

  /**
   * @see ODataService
   */
  @Override
  public EntitySimplePropertyProcessor getEntitySimplePropertyProcessor() throws ODataException {
    return processor;
  }

  /**
   * @see ODataService
   */
  @Override
  public EntitySimplePropertyValueProcessor getEntitySimplePropertyValueProcessor() throws ODataException {
    return processor;
  }

  /**
   * @see ODataService
   */
  @Override
  public FunctionImportProcessor getFunctionImportProcessor() throws ODataException {
    return processor;
  }

  /**
   * @see ODataService
   */
  @Override
  public FunctionImportValueProcessor getFunctionImportValueProcessor() throws ODataException {
    return processor;
  }

  /**
   * @see ODataService
   */
  @Override
  public BatchProcessor getBatchProcessor() throws ODataException {
    return processor;
  }

  /**
   * @see ODataService
   */
  @Override
  public ODataProcessor getProcessor() throws ODataException {
    return processor;
  }

  @Override
  public List<String> getSupportedContentTypes(Class<? extends ODataProcessor> processorFeature) throws ODataException {
    List<String> result = new ArrayList<String>();

    if(processor instanceof CustomContentType) {
      result.addAll(((CustomContentType)processor).getCustomContentTypes(processorFeature));
    }

    if (processorFeature == BatchProcessor.class) {
      result.add(HttpContentType.MULTIPART_MIXED);
    } else if (processorFeature == EntityProcessor.class) {
      result.add(HttpContentType.APPLICATION_ATOM_XML_ENTRY);
      result.add(HttpContentType.APPLICATION_ATOM_XML);
      result.add(HttpContentType.APPLICATION_JSON);
      result.add(HttpContentType.APPLICATION_XML);
    } else if (processorFeature == FunctionImportProcessor.class
        || processorFeature == EntityLinkProcessor.class
        || processorFeature == EntityLinksProcessor.class
        || processorFeature == EntitySimplePropertyProcessor.class
        || processorFeature == EntityComplexPropertyProcessor.class) {
      result.add(HttpContentType.APPLICATION_XML);
      result.add(HttpContentType.APPLICATION_JSON);
    } else if (processorFeature == EntityMediaProcessor.class
        || processorFeature == EntitySimplePropertyValueProcessor.class
        || processorFeature == FunctionImportValueProcessor.class) {
      result.add(HttpContentType.WILDCARD);
    } else if (processorFeature == EntitySetProcessor.class) {
      result.add(HttpContentType.APPLICATION_ATOM_XML_FEED);
      result.add(HttpContentType.APPLICATION_ATOM_XML);
      result.add(HttpContentType.APPLICATION_JSON);
      result.add(HttpContentType.APPLICATION_XML);
    } else if (processorFeature == MetadataProcessor.class) {
      result.add(HttpContentType.APPLICATION_XML);
    } else if (processorFeature == ServiceDocumentProcessor.class) {
      result.add(HttpContentType.APPLICATION_ATOM_SVC);
      result.add(HttpContentType.APPLICATION_ATOM_XML);
      result.add(HttpContentType.APPLICATION_JSON);
      result.add(HttpContentType.APPLICATION_XML);
    } else {
      throw new ODataNotImplementedException();
    }

    return result;
  }
}
