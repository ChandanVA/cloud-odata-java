package com.sap.core.odata.fit.basic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;

import com.sap.core.odata.api.ODataService;
import com.sap.core.odata.api.commons.HttpStatusCodes;
import com.sap.core.odata.api.exception.ODataException;
import com.sap.core.odata.api.processor.ODataContext;
import com.sap.core.odata.api.processor.ODataResponse;
import com.sap.core.odata.api.processor.ODataSingleProcessor;
import com.sap.core.odata.api.processor.feature.Metadata;
import com.sap.core.odata.api.processor.feature.ServiceDocument;
import com.sap.core.odata.api.uri.info.GetMetadataUriInfo;
import com.sap.core.odata.api.uri.info.GetServiceDocumentUriInfo;
import com.sap.core.odata.core.enums.ContentType;

public class ContextTest extends AbstractBasicTest {

  @Override
  protected ODataSingleProcessor createProcessor() throws ODataException {
    ODataSingleProcessor processor = mock(ODataSingleProcessor.class);
    when(((Metadata) processor).readMetadata(any(GetMetadataUriInfo.class),any(String.class))).thenReturn(ODataResponse.entity("metadata").status(HttpStatusCodes.OK).build());
    when(((ServiceDocument) processor).readServiceDocument(any(GetServiceDocumentUriInfo.class),any(String.class))).thenReturn(ODataResponse.entity("service document").status(HttpStatusCodes.OK).build());
    return processor;
  }

  @Test
  public void checkContextExists() throws ClientProtocolException, IOException, ODataException {
    assertNull(this.getService().getProcessor().getContext());
    HttpGet get = new HttpGet(URI.create(this.getEndpoint().toString() + "$metadata"));
    HttpResponse response = this.getHttpClient().execute(get);

    ODataContext ctx = this.getService().getProcessor().getContext();
    assertNotNull(ctx);

    ODataService service = ctx.getService();
    assertNotNull(service);

    assertEquals(Status.OK.getStatusCode(), response.getStatusLine().getStatusCode());

    assertEquals("$metadata", ctx.getUriInfo().getODataSegments().get(0).getPath());
  }

  @Test
  public void checkBaseUriForServiceDocument() throws ClientProtocolException, IOException, ODataException {
    HttpGet get = new HttpGet(URI.create(this.getEndpoint().toString()));
    this.getHttpClient().execute(get);

    ODataContext ctx = this.getService().getProcessor().getContext();
    assertNotNull(ctx);
    assertEquals(this.getEndpoint().toString(), ctx.getUriInfo().getServiceRoot().toASCIIString());
  }

  @Test
  public void checkBaseUriForMetadata() throws ClientProtocolException, IOException, ODataException {
    HttpGet get = new HttpGet(URI.create(this.getEndpoint().toString() + "/$metadata"));
    this.getHttpClient().execute(get);

    ODataContext ctx = this.getService().getProcessor().getContext();
    assertNotNull(ctx);
    assertEquals(this.getEndpoint().toString(), ctx.getUriInfo().getServiceRoot().toASCIIString());
  }

}
