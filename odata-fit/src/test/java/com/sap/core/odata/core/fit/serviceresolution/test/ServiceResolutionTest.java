package com.sap.core.odata.core.fit.serviceresolution.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.core.odata.api.edm.provider.EdmProvider;
import com.sap.core.odata.api.enums.HttpStatusCodes;
import com.sap.core.odata.api.exception.ODataException;
import com.sap.core.odata.api.processor.ODataContext;
import com.sap.core.odata.api.processor.ODataProcessor;
import com.sap.core.odata.api.processor.ODataResponse;
import com.sap.core.odata.api.processor.ODataSingleProcessor;
import com.sap.core.odata.api.processor.aspect.Metadata;
import com.sap.core.odata.api.processor.aspect.ServiceDocument;
import com.sap.core.odata.api.service.ODataService;
import com.sap.core.odata.api.service.ODataSingleProcessorService;
import com.sap.core.odata.api.uri.resultviews.GetMetadataView;
import com.sap.core.odata.api.uri.resultviews.GetServiceDocumentView;
import com.sap.core.odata.testutils.fit.ServiceFactory;
import com.sap.core.odata.testutils.server.TestServer;

public class ServiceResolutionTest {

  static {
    DOMConfigurator.configureAndWatch("log4j.xml");
  }

  private HttpClient httpClient = new DefaultHttpClient();
  private TestServer server = new TestServer();
  private ODataProcessor processor;
  private EdmProvider edmProvider;
  private ODataContext context;
  private ODataService service;

  @Before
  public void before() throws Exception {
    this.processor = this.createProcessorMock();
    this.edmProvider = this.createEdmProviderMock();

    this.service = new ODataSingleProcessorService(this.edmProvider, (ODataSingleProcessor) this.processor) {};
    ServiceFactory.setService(this.service);
    
    ServiceFactory.setService(this.service);
  }

  @After
  public void after() throws Exception {
    try {
      if (this.server != null) {
        this.server.stopServer();
      }
    } finally {
      ServiceFactory.setService(null);
    }
  }

  private EdmProvider createEdmProviderMock() {
    EdmProvider edmProvider = mock(EdmProvider.class);
    return edmProvider;
  }

  private ODataProcessor createProcessorMock() throws ODataException {
    ODataProcessor processor = mock(ODataSingleProcessor.class);

    // science fiction (return context after setContext)
    // see http://www.planetgeek.ch/2010/07/20/mockito-answer-vs-return/

    doAnswer(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        ServiceResolutionTest.this.context = (ODataContext) invocation.getArguments()[0];
        return null;
      }
    }).when(processor).setContext(any(ODataContext.class));

    when(processor.getContext()).thenAnswer(new Answer<ODataContext>() {
      @Override
      public ODataContext answer(InvocationOnMock invocation) throws Throwable {
        return ServiceResolutionTest.this.context;
      }
    });

    when(((Metadata) processor).readMetadata(any(GetMetadataView.class))).thenReturn(ODataResponse.entity("metadata").status(HttpStatusCodes.OK).build());
    when(((ServiceDocument) processor).readServiceDocument(any(GetServiceDocumentView.class))).thenReturn(ODataResponse.entity("servicedocument").status(HttpStatusCodes.OK).build());

    return processor;
  }

  @Test
  public void testSplit0() throws ClientProtocolException, IOException, ODataException {
    this.server.setPathSplit(0);
    this.server.startServer(ServiceFactory.class);

    HttpGet get = new HttpGet(URI.create(this.server.getEndpoint().toString() + "/$metadata"));
    HttpResponse response = this.httpClient.execute(get);

    assertEquals(200, response.getStatusLine().getStatusCode());

    ODataContext ctx = this.processor.getContext();
    assertNotNull(ctx);

    assertTrue(ctx.getPrecedingPathSegmentList().isEmpty());
    assertEquals("$metadata", ctx.getODataPathSegmentList().get(0).getPath());
  }

  @Test
  public void testSplit1() throws ClientProtocolException, IOException, ODataException {
    this.server.setPathSplit(1);
    this.server.startServer(ServiceFactory.class);

    HttpGet get = new HttpGet(URI.create(this.server.getEndpoint().toString() + "/aaa/$metadata"));
    HttpResponse response = this.httpClient.execute(get);

    assertEquals(200, response.getStatusLine().getStatusCode());

    ODataContext ctx = this.processor.getContext();
    assertNotNull(ctx);

    assertEquals("aaa", ctx.getPrecedingPathSegmentList().get(0).getPath());
    assertEquals("$metadata", ctx.getODataPathSegmentList().get(0).getPath());
  }

  @Test
  public void testSplit2() throws ClientProtocolException, IOException, ODataException {
    this.server.setPathSplit(2);
    this.server.startServer(ServiceFactory.class);

    HttpGet get = new HttpGet(URI.create(this.server.getEndpoint().toString() + "/aaa/bbb/$metadata"));
    HttpResponse response = this.httpClient.execute(get);

    assertEquals(200, response.getStatusLine().getStatusCode());

    ODataContext ctx = this.processor.getContext();
    assertNotNull(ctx);

    assertEquals("aaa", ctx.getPrecedingPathSegmentList().get(0).getPath());
    assertEquals("bbb", ctx.getPrecedingPathSegmentList().get(1).getPath());
    assertEquals("$metadata", ctx.getODataPathSegmentList().get(0).getPath());
  }

  @Test
  public void testSplitUrlToShort() throws ClientProtocolException, IOException, ODataException {
    this.server.setPathSplit(3);
    this.server.startServer(ServiceFactory.class);

    HttpGet get = new HttpGet(URI.create(this.server.getEndpoint().toString() + "/aaa/$metadata"));
    HttpResponse response = this.httpClient.execute(get);

    assertEquals(400, response.getStatusLine().getStatusCode());
  }

  @Test
  public void testSplitUrlServiceDocument() throws ClientProtocolException, IOException, ODataException {
    this.server.setPathSplit(1);
    this.server.startServer(ServiceFactory.class);

    HttpGet get = new HttpGet(URI.create(this.server.getEndpoint().toString() + "/aaa/"));
    HttpResponse response = this.httpClient.execute(get);

    assertEquals(200, response.getStatusLine().getStatusCode());

    ODataContext ctx = this.processor.getContext();
    assertNotNull(ctx);
    
    assertEquals("", ctx.getODataPathSegmentList().get(0).getPath());
    assertEquals("aaa", ctx.getPrecedingPathSegmentList().get(0).getPath());
  }

  @Test
  public void testNoMatrixParameterInODataPath() throws ClientProtocolException, IOException, ODataException {
    this.server.setPathSplit(0);
    this.server.startServer(ServiceFactory.class);

    HttpGet get = new HttpGet(URI.create(this.server.getEndpoint().toString() + "/$metadata;matrix"));
    HttpResponse response = this.httpClient.execute(get);

    assertEquals(404, response.getStatusLine().getStatusCode());
  }


}
