package com.sap.core.odata.fit.basic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.junit.Test;

import com.sap.core.odata.api.ODataService;
import com.sap.core.odata.api.batch.BatchChangesetPart;
import com.sap.core.odata.api.batch.BatchPart;
import com.sap.core.odata.api.batch.BatchResult;
import com.sap.core.odata.api.batch.ODataRequestHandlerInterface;
import com.sap.core.odata.api.commons.HttpStatusCodes;
import com.sap.core.odata.api.edm.Edm;
import com.sap.core.odata.api.edm.provider.EdmProvider;
import com.sap.core.odata.api.ep.EntityProvider;
import com.sap.core.odata.api.ep.EntityProviderBatchProperties;
import com.sap.core.odata.api.exception.ODataException;
import com.sap.core.odata.api.processor.ODataRequest;
import com.sap.core.odata.api.processor.ODataResponse;
import com.sap.core.odata.api.processor.ODataSingleProcessor;
import com.sap.core.odata.api.uri.info.GetSimplePropertyUriInfo;
import com.sap.core.odata.api.uri.info.PutMergePatchUriInfo;
import com.sap.core.odata.core.PathInfoImpl;
import com.sap.core.odata.core.processor.ODataSingleProcessorService;
import com.sap.core.odata.testutil.mock.MockFacade;

/**
 * 
 * @author SAP AG
 */
public class BasicBatchTest extends AbstractBasicTest {

  private static final String LF = "\n";
  private static final String REG_EX_BOUNDARY = "(([a-zA-Z0-9_\\-\\.'\\+]{1,70})|\"([a-zA-Z0-9_\\-\\.'\\+\\s\\(\\),/:=\\?]{1,69}[a-zA-Z0-9_\\-\\.'\\+\\(\\),/:=\\?])\")";
  private static final String REG_EX = "multipart/mixed;\\s*boundary=" + REG_EX_BOUNDARY + "\\s*";

  private static final String REQUEST_PAYLOAD = "--batch_98c1-8b13-36bb" + LF
      + "Content-Type: application/http" + LF
      + "Content-Transfer-Encoding: binary" + LF
      + LF
      + "GET Employees('1')/EmployeeName HTTP/1.1" + LF
      + "Host: localhost:19000" + LF
      + "Accept: application/atomsvc+xml;q=0.8, application/json;odata=verbose;q=0.5, */*;q=0.1" + LF
      + "Accept-Language: en" + LF
      + "MaxDataServiceVersion: 2.0" + LF
      + LF
      + LF
      + "--batch_98c1-8b13-36bb" + LF
      + "Content-Type: multipart/mixed; boundary=changeset_f980-1cb6-94dd" + LF
      + LF
      + "--changeset_f980-1cb6-94dd" + LF
      + "Content-Type: application/http" + LF
      + "Content-Transfer-Encoding: binary" + LF
      + LF
      + "PUT Employees('1')/EmployeeName HTTP/1.1" + LF
      + "Host: localhost:19000" + LF
      + "Content-Type: application/json;odata=verbose" + LF
      + "MaxDataServiceVersion: 2.0" + LF
      + LF
      + "{\"EmployeeName\":\"Walter Winter MODIFIED\"}" + LF
      + LF
      + "--changeset_f980-1cb6-94dd--" + LF
      + LF
      + "--batch_98c1-8b13-36bb--";

  @Test
  public void testBatch() throws Exception {
    final HttpPost post = new HttpPost(URI.create(getEndpoint().toString() + "$batch"));
    post.setHeader("Content-Type", "multipart/mixed");
    HttpEntity entity = new StringEntity(REQUEST_PAYLOAD);
    post.setEntity(entity);
    HttpResponse response = getHttpClient().execute(post);

    assertNotNull(response);
    assertEquals(202, response.getStatusLine().getStatusCode());
    assertEquals("HTTP/1.1", response.getProtocolVersion().toString());
    assertTrue(response.containsHeader("Content-Length"));
    assertTrue(response.containsHeader("Content-Type"));
    assertTrue(response.containsHeader("DataServiceVersion"));
    assertTrue(response.getEntity().getContentType().getValue().matches(REG_EX));
    assertNotNull(response.getEntity().getContent());
    Scanner scanner = new Scanner((InputStream) response.getEntity().getContent()).useDelimiter(LF);
    while (scanner.hasNext()) {
      System.out.println(scanner.next());
    }
    scanner.close();
  }

  static class TestSingleProc extends ODataSingleProcessor {
    @Override
    public ODataResponse executeBatch(final ODataRequestHandlerInterface handler, final String contentType, final InputStream content) {
      ODataResponse batchResponse;

      String tempContentType = "multipart/mixed;boundary=batch_98c1-8b13-36bb";

      PathInfoImpl pathInfo = new PathInfoImpl();
      try {
        pathInfo.setServiceRoot(new URI("http://localhost:19000/odata"));

        EntityProviderBatchProperties batchProperties = EntityProviderBatchProperties.init().pathInfo(pathInfo).build();
        BatchResult batch = EntityProvider.parseBatch(tempContentType, content, batchProperties);
        List<ODataResponse> responses = new ArrayList<ODataResponse>();
        for (BatchPart batchPart : batch.getBatchParts()) {
          ODataResponse response = handler.handle(batchPart);
          responses.add(response);
        }
        batchResponse = EntityProvider.writeBatchResponse(responses);
      } catch (URISyntaxException e) {
        throw new RuntimeException(e);
      } catch (ODataException e) {
        throw new RuntimeException(e);
      }
      return batchResponse;
    }

    @Override
    public ODataResponse executeChangeSet(final ODataRequestHandlerInterface handler, final BatchChangesetPart changeset) {
      ODataResponse changeSetResponse;
      List<ODataResponse> responses = new ArrayList<ODataResponse>();
      try {
        for (ODataRequest request : changeset.getRequests()) {
          ODataResponse response = handler.handle(request);
          responses.add(response);
        }
      } catch (ODataException e) {
        throw new RuntimeException(e);
      }
      changeSetResponse = EntityProvider.writeChangeSet(responses);
      return changeSetResponse;
    }

    @Override
    public ODataResponse readEntitySimpleProperty(final GetSimplePropertyUriInfo uriInfo, final String contentType) throws ODataException {
      ODataResponse oDataResponse = ODataResponse.entity("{\"d\":{\"EmployeeName\":\"Walter Winter\"}}").status(HttpStatusCodes.OK).contentHeader("application/json").build();
      return oDataResponse;
    }

    @Override
    public ODataResponse updateEntitySimpleProperty(final PutMergePatchUriInfo uriInfo, final InputStream content, final String requestContentType, final String contentType) throws ODataException {
      ODataResponse oDataResponse = ODataResponse.status(HttpStatusCodes.NO_CONTENT).build();
      return oDataResponse;
    }
  }

  @Override
  protected ODataSingleProcessor createProcessor() throws ODataException {
    return new TestSingleProc();
  }

  @Override
  protected ODataService createService() throws ODataException {
    final EdmProvider provider = createEdmProvider();

    final ODataSingleProcessor processor = createProcessor();

    return new ODataSingleProcessorService(provider, processor) {
      Edm edm = MockFacade.getMockEdm();

      @Override
      public Edm getEntityDataModel() throws ODataException {
        return edm;
      }
    };
  }
}
