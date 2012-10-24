package com.sap.core.odata.core.dispatcher.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.core.odata.api.enums.UriType;
import com.sap.core.odata.api.exception.ODataError;
import com.sap.core.odata.api.processor.ODataProcessor;
import com.sap.core.odata.api.processor.aspect.Batch;
import com.sap.core.odata.api.processor.aspect.Entity;
import com.sap.core.odata.api.processor.aspect.EntityComplexProperty;
import com.sap.core.odata.api.processor.aspect.EntityLink;
import com.sap.core.odata.api.processor.aspect.EntityLinks;
import com.sap.core.odata.api.processor.aspect.EntityMedia;
import com.sap.core.odata.api.processor.aspect.EntitySet;
import com.sap.core.odata.api.processor.aspect.EntitySimpleProperty;
import com.sap.core.odata.api.processor.aspect.EntitySimplePropertyValue;
import com.sap.core.odata.api.processor.aspect.FunctionImport;
import com.sap.core.odata.api.processor.aspect.FunctionImportValue;
import com.sap.core.odata.api.processor.aspect.Metadata;
import com.sap.core.odata.api.processor.aspect.ServiceDocument;
import com.sap.core.odata.api.rest.ODataResponse;
import com.sap.core.odata.api.uri.UriParserResult;
import com.sap.core.odata.core.dispatcher.Dispatcher;
import com.sap.core.odata.core.enums.ODataHttpMethod;

public class DispatcherTest {

  private static ODataProcessor processor;

  @BeforeClass
  public static void createMockProcessor() throws ODataError {
    ServiceDocument serviceDocument = mock(ServiceDocument.class);
    when(serviceDocument.readServiceDocument()).thenAnswer(getAnswer());

    EntitySet entitySet = mock(EntitySet.class);
    when(entitySet.readEntitySet()).thenAnswer(getAnswer());
    when(entitySet.countEntitySet()).thenAnswer(getAnswer());
    when(entitySet.createEntity()).thenAnswer(getAnswer());

    Entity entity = mock(Entity.class);
    when(entity.readEntity()).thenAnswer(getAnswer());
    when(entity.existsEntity()).thenAnswer(getAnswer());
    when(entity.deleteEntity()).thenAnswer(getAnswer());
    when(entity.updateEntity()).thenAnswer(getAnswer());

    EntityComplexProperty entityComplexProperty = mock(EntityComplexProperty.class);
    when(entityComplexProperty.readEntityComplexProperty()).thenAnswer(getAnswer());
    when(entityComplexProperty.updateEntityComplexProperty()).thenAnswer(getAnswer());

    EntitySimpleProperty entitySimpleProperty = mock(EntitySimpleProperty.class);
    when(entitySimpleProperty.readEntitySimpleProperty()).thenAnswer(getAnswer());
    when(entitySimpleProperty.updateEntitySimpleProperty()).thenAnswer(getAnswer());

    EntitySimplePropertyValue entitySimplePropertyValue = mock(EntitySimplePropertyValue.class);
    when(entitySimplePropertyValue.readEntitySimplePropertyValue()).thenAnswer(getAnswer());
    when(entitySimplePropertyValue.deleteEntitySimplePropertyValue()).thenAnswer(getAnswer());
    when(entitySimplePropertyValue.updateEntitySimplePropertyValue()).thenAnswer(getAnswer());

    EntityLink entityLink = mock(EntityLink.class);
    when(entityLink.readEntityLink()).thenAnswer(getAnswer());
    when(entityLink.existsEntityLink()).thenAnswer(getAnswer());
    when(entityLink.deleteEntityLink()).thenAnswer(getAnswer());
    when(entityLink.updateEntityLink()).thenAnswer(getAnswer());

    EntityLinks entityLinks = mock(EntityLinks.class);
    when(entityLinks.readEntityLinks()).thenAnswer(getAnswer());
    when(entityLinks.countEntityLinks()).thenAnswer(getAnswer());
    when(entityLinks.createEntityLink()).thenAnswer(getAnswer());

    Metadata metadata = mock(Metadata.class);
    when(metadata.readMetadata()).thenAnswer(getAnswer());

    Batch batch = mock(Batch.class);
    when(batch.executeBatch()).thenAnswer(getAnswer());

    FunctionImport functionImport = mock(FunctionImport.class);
    when(functionImport.executeFunctionImport()).thenAnswer(getAnswer());

    FunctionImportValue functionImportValue = mock(FunctionImportValue.class);
    when(functionImportValue.executeFunctionImportValue()).thenAnswer(getAnswer());

    EntityMedia entityMedia = mock(EntityMedia.class);
    when(entityMedia.readEntityMedia()).thenAnswer(getAnswer());
    when(entityMedia.deleteEntityMedia()).thenAnswer(getAnswer());
    when(entityMedia.updateEntityMedia()).thenAnswer(getAnswer());

    processor = mock(ODataProcessor.class);
    when(processor.getServiceDocumentProcessor()).thenReturn(serviceDocument);
    when(processor.getEntitySetProcessor()).thenReturn(entitySet);
    when(processor.getEntityProcessor()).thenReturn(entity);
    when(processor.getEntityComplexPropertyProcessor()).thenReturn(entityComplexProperty);
    when(processor.getEntitySimplePropertyProcessor()).thenReturn(entitySimpleProperty);
    when(processor.getEntitySimplePropertyValueProcessor()).thenReturn(entitySimplePropertyValue);
    when(processor.getEntityLinkProcessor()).thenReturn(entityLink);
    when(processor.getEntityLinksProcessor()).thenReturn(entityLinks);
    when(processor.getMetadataProcessor()).thenReturn(metadata);
    when(processor.getBatchProcessor()).thenReturn(batch);
    when(processor.getFunctionImportProcessor()).thenReturn(functionImport);
    when(processor.getFunctionImportValueProcessor()).thenReturn(functionImportValue);
    when(processor.getEntityMediaProcessor()).thenReturn(entityMedia);
  }

  private static Answer<ODataResponse> getAnswer() {
    return new Answer<ODataResponse>() {
      public ODataResponse answer(InvocationOnMock invocation) {
        return mockResponse(invocation.getMethod().getName());
      }
    };
  }

  private static ODataResponse mockResponse(final String value) {
    ODataResponse response = mock(ODataResponse.class);
    when(response.getEntity()).thenReturn(value);
    return response;
  }

  private void checkDispatch(final ODataHttpMethod method, final UriType uriType, final boolean isValue, final String expectedMethodName) throws ODataError {
    Dispatcher dispatcher = new Dispatcher();
    dispatcher.setProcessor(processor);

    UriParserResult uriParserResult = mock(UriParserResult.class);
    when(uriParserResult.getUriType()).thenReturn(uriType);
    when(uriParserResult.isValue()).thenReturn(isValue);
    final ODataResponse response = dispatcher.dispatch(method, uriParserResult);
    assertEquals(expectedMethodName, response.getEntity());
  }

  private void checkDispatch(final ODataHttpMethod method, final UriType uriType, final String expectedMethodName) throws ODataError {
    checkDispatch(method, uriType, false, expectedMethodName);
  }

  private void wrongDispatch(final ODataHttpMethod method, final UriType uriType) {
    try {
      checkDispatch(method, uriType, null);
      fail("Expected ODataError not thrown");
    } catch (ODataError e) {
      assertNotNull(e);
    }
  }

  @Test
  public void dispatch() throws Exception {
    checkDispatch(ODataHttpMethod.GET, UriType.URI0, "readServiceDocument");

    checkDispatch(ODataHttpMethod.GET, UriType.URI1, "readEntitySet");
    checkDispatch(ODataHttpMethod.POST, UriType.URI1, "createEntity");

    checkDispatch(ODataHttpMethod.GET, UriType.URI2, "readEntity");
    checkDispatch(ODataHttpMethod.PUT, UriType.URI2, "updateEntity");
    checkDispatch(ODataHttpMethod.DELETE, UriType.URI2, "deleteEntity");
    checkDispatch(ODataHttpMethod.PATCH, UriType.URI2, "updateEntity");
    checkDispatch(ODataHttpMethod.MERGE, UriType.URI2, "updateEntity");

    checkDispatch(ODataHttpMethod.GET, UriType.URI3, "readEntityComplexProperty");
    checkDispatch(ODataHttpMethod.PUT, UriType.URI3, "updateEntityComplexProperty");
    checkDispatch(ODataHttpMethod.PATCH, UriType.URI3, "updateEntityComplexProperty");
    checkDispatch(ODataHttpMethod.MERGE, UriType.URI3, "updateEntityComplexProperty");

    checkDispatch(ODataHttpMethod.GET, UriType.URI4, "readEntitySimpleProperty");
    checkDispatch(ODataHttpMethod.PUT, UriType.URI4, "updateEntitySimpleProperty");
    checkDispatch(ODataHttpMethod.PATCH, UriType.URI4, "updateEntitySimpleProperty");
    checkDispatch(ODataHttpMethod.MERGE, UriType.URI4, "updateEntitySimpleProperty");
    checkDispatch(ODataHttpMethod.GET, UriType.URI4, true, "readEntitySimplePropertyValue");
    checkDispatch(ODataHttpMethod.PUT, UriType.URI4, true, "updateEntitySimplePropertyValue");
    checkDispatch(ODataHttpMethod.DELETE, UriType.URI4, true, "deleteEntitySimplePropertyValue");
    checkDispatch(ODataHttpMethod.PATCH, UriType.URI4, true, "updateEntitySimplePropertyValue");
    checkDispatch(ODataHttpMethod.MERGE, UriType.URI4, true, "updateEntitySimplePropertyValue");

    checkDispatch(ODataHttpMethod.GET, UriType.URI5, "readEntitySimpleProperty");
    checkDispatch(ODataHttpMethod.PUT, UriType.URI5, "updateEntitySimpleProperty");
    checkDispatch(ODataHttpMethod.PATCH, UriType.URI5, "updateEntitySimpleProperty");
    checkDispatch(ODataHttpMethod.MERGE, UriType.URI5, "updateEntitySimpleProperty");
    checkDispatch(ODataHttpMethod.GET, UriType.URI5, true, "readEntitySimplePropertyValue");
    checkDispatch(ODataHttpMethod.PUT, UriType.URI5, true, "updateEntitySimplePropertyValue");
    checkDispatch(ODataHttpMethod.DELETE, UriType.URI5, true, "deleteEntitySimplePropertyValue");
    checkDispatch(ODataHttpMethod.PATCH, UriType.URI5, true, "updateEntitySimplePropertyValue");
    checkDispatch(ODataHttpMethod.MERGE, UriType.URI5, true, "updateEntitySimplePropertyValue");

    checkDispatch(ODataHttpMethod.GET, UriType.URI6A, "readEntity");
    checkDispatch(ODataHttpMethod.PUT, UriType.URI6A, "updateEntity");
    checkDispatch(ODataHttpMethod.DELETE, UriType.URI6A, "deleteEntity");
    checkDispatch(ODataHttpMethod.PATCH, UriType.URI6A, "updateEntity");
    checkDispatch(ODataHttpMethod.MERGE, UriType.URI6A, "updateEntity");

    checkDispatch(ODataHttpMethod.GET, UriType.URI6B, "readEntitySet");
    checkDispatch(ODataHttpMethod.POST, UriType.URI6B, "createEntity");

    checkDispatch(ODataHttpMethod.GET, UriType.URI7A, "readEntityLink");
    checkDispatch(ODataHttpMethod.PUT, UriType.URI7A, "updateEntityLink");
    checkDispatch(ODataHttpMethod.DELETE, UriType.URI7A, "deleteEntityLink");
    checkDispatch(ODataHttpMethod.PATCH, UriType.URI7A, "updateEntityLink");
    checkDispatch(ODataHttpMethod.MERGE, UriType.URI7A, "updateEntityLink");

    checkDispatch(ODataHttpMethod.GET, UriType.URI7B, "readEntityLinks");
    checkDispatch(ODataHttpMethod.POST, UriType.URI7B, "createEntityLink");

    checkDispatch(ODataHttpMethod.GET, UriType.URI8, "readMetadata");

    checkDispatch(ODataHttpMethod.POST, UriType.URI9, "executeBatch");

    checkDispatch(ODataHttpMethod.GET, UriType.URI10, "executeFunctionImport");
    checkDispatch(ODataHttpMethod.GET, UriType.URI11, "executeFunctionImport");
    checkDispatch(ODataHttpMethod.GET, UriType.URI12, "executeFunctionImport");
    checkDispatch(ODataHttpMethod.GET, UriType.URI13, "executeFunctionImport");
    checkDispatch(ODataHttpMethod.GET, UriType.URI14, "executeFunctionImport");
    checkDispatch(ODataHttpMethod.GET, UriType.URI14, true, "executeFunctionImportValue");

    checkDispatch(ODataHttpMethod.GET, UriType.URI15, "countEntitySet");

    checkDispatch(ODataHttpMethod.GET, UriType.URI16, "existsEntity");

    checkDispatch(ODataHttpMethod.GET, UriType.URI17, "readEntityMedia");
    checkDispatch(ODataHttpMethod.PUT, UriType.URI17, "updateEntityMedia");
    checkDispatch(ODataHttpMethod.DELETE, UriType.URI17, "deleteEntityMedia");

    checkDispatch(ODataHttpMethod.GET, UriType.URI50A, "existsEntityLink");

    checkDispatch(ODataHttpMethod.GET, UriType.URI50B, "countEntityLinks");
  }

  @Test
  public void dispatchNotAllowedCombinations() throws Exception {
    wrongDispatch(null, UriType.URI0);

    wrongDispatch(ODataHttpMethod.PUT, UriType.URI0);
    wrongDispatch(ODataHttpMethod.POST, UriType.URI0);
    wrongDispatch(ODataHttpMethod.DELETE, UriType.URI0);
    wrongDispatch(ODataHttpMethod.PATCH, UriType.URI0);
    wrongDispatch(ODataHttpMethod.MERGE, UriType.URI0);

    wrongDispatch(ODataHttpMethod.PUT, UriType.URI1);
    wrongDispatch(ODataHttpMethod.DELETE, UriType.URI1);
    wrongDispatch(ODataHttpMethod.PATCH, UriType.URI1);
    wrongDispatch(ODataHttpMethod.MERGE, UriType.URI1);

    wrongDispatch(ODataHttpMethod.POST, UriType.URI2);

    wrongDispatch(ODataHttpMethod.POST, UriType.URI3);
    wrongDispatch(ODataHttpMethod.DELETE, UriType.URI3);

    wrongDispatch(ODataHttpMethod.POST, UriType.URI4);
    wrongDispatch(ODataHttpMethod.DELETE, UriType.URI4);

    wrongDispatch(ODataHttpMethod.POST, UriType.URI5);
    wrongDispatch(ODataHttpMethod.DELETE, UriType.URI5);

    wrongDispatch(ODataHttpMethod.POST, UriType.URI6A);

    wrongDispatch(ODataHttpMethod.PUT, UriType.URI6B);
    wrongDispatch(ODataHttpMethod.DELETE, UriType.URI6B);
    wrongDispatch(ODataHttpMethod.PATCH, UriType.URI6B);
    wrongDispatch(ODataHttpMethod.MERGE, UriType.URI6B);

    wrongDispatch(ODataHttpMethod.POST, UriType.URI7A);

    wrongDispatch(ODataHttpMethod.PUT, UriType.URI7B);
    wrongDispatch(ODataHttpMethod.DELETE, UriType.URI7B);
    wrongDispatch(ODataHttpMethod.PATCH, UriType.URI7B);
    wrongDispatch(ODataHttpMethod.MERGE, UriType.URI7B);

    wrongDispatch(ODataHttpMethod.PUT, UriType.URI8);
    wrongDispatch(ODataHttpMethod.POST, UriType.URI8);
    wrongDispatch(ODataHttpMethod.DELETE, UriType.URI8);
    wrongDispatch(ODataHttpMethod.PATCH, UriType.URI8);
    wrongDispatch(ODataHttpMethod.MERGE, UriType.URI8);

    wrongDispatch(ODataHttpMethod.GET, UriType.URI9);
    wrongDispatch(ODataHttpMethod.PUT, UriType.URI9);
    wrongDispatch(ODataHttpMethod.DELETE, UriType.URI9);
    wrongDispatch(ODataHttpMethod.PATCH, UriType.URI9);
    wrongDispatch(ODataHttpMethod.MERGE, UriType.URI9);

    wrongDispatch(ODataHttpMethod.PUT, UriType.URI15);
    wrongDispatch(ODataHttpMethod.POST, UriType.URI15);
    wrongDispatch(ODataHttpMethod.DELETE, UriType.URI15);
    wrongDispatch(ODataHttpMethod.PATCH, UriType.URI15);
    wrongDispatch(ODataHttpMethod.MERGE, UriType.URI15);

    wrongDispatch(ODataHttpMethod.PUT, UriType.URI16);
    wrongDispatch(ODataHttpMethod.POST, UriType.URI16);
    wrongDispatch(ODataHttpMethod.DELETE, UriType.URI16);
    wrongDispatch(ODataHttpMethod.PATCH, UriType.URI16);
    wrongDispatch(ODataHttpMethod.MERGE, UriType.URI16);

    wrongDispatch(ODataHttpMethod.POST, UriType.URI17);
    wrongDispatch(ODataHttpMethod.PATCH, UriType.URI17);
    wrongDispatch(ODataHttpMethod.MERGE, UriType.URI17);

    wrongDispatch(ODataHttpMethod.PUT, UriType.URI50A);
    wrongDispatch(ODataHttpMethod.POST, UriType.URI50A);
    wrongDispatch(ODataHttpMethod.DELETE, UriType.URI50A);
    wrongDispatch(ODataHttpMethod.PATCH, UriType.URI50A);
    wrongDispatch(ODataHttpMethod.MERGE, UriType.URI50A);

    wrongDispatch(ODataHttpMethod.PUT, UriType.URI50B);
    wrongDispatch(ODataHttpMethod.POST, UriType.URI50B);
    wrongDispatch(ODataHttpMethod.DELETE, UriType.URI50B);
    wrongDispatch(ODataHttpMethod.PATCH, UriType.URI50B);
    wrongDispatch(ODataHttpMethod.MERGE, UriType.URI50B);
  }
}
