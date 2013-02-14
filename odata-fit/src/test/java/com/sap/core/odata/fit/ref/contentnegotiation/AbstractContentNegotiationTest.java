package com.sap.core.odata.fit.ref.contentnegotiation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

import com.sap.core.odata.api.ODataService;
import com.sap.core.odata.api.commons.HttpHeaders;
import com.sap.core.odata.api.commons.HttpStatusCodes;
import com.sap.core.odata.api.edm.provider.EdmProvider;
import com.sap.core.odata.api.exception.ODataException;
import com.sap.core.odata.api.processor.ODataSingleProcessor;
import com.sap.core.odata.core.commons.ContentType;
import com.sap.core.odata.core.processor.ODataSingleProcessorService;
import com.sap.core.odata.core.uri.UriType;
import com.sap.core.odata.ref.edm.ScenarioEdmProvider;
import com.sap.core.odata.ref.model.DataContainer;
import com.sap.core.odata.ref.processor.ListsProcessor;
import com.sap.core.odata.ref.processor.ScenarioDataSource;
import com.sap.core.odata.testutil.fit.BaseTest;
import com.sap.core.odata.testutil.helper.StringHelper;
import com.sap.core.odata.testutil.helper.TestutilException;
import com.sap.core.odata.testutil.server.TestServer;

/**
 * @author SAP AG
 */
public abstract class AbstractContentNegotiationTest extends BaseTest {
  
  private static final Logger LOG = Logger.getLogger(AbstractContentNegotiationTest.class);
  
  protected final static List<String> ACCEPT_HEADER_VALUES = Arrays.asList(
      "", // for request with none 'Accept-Header' set
      "text/plain",
      "text/plain; charset=utf-8",
      "application/json",
      "application/json; charset=utf-8",
      "application/xml",
      "application/xml; charset=utf-8",
      "application/atom+xml",
      "application/atom+xml; charset=utf-8",
      "application/atomsvc+xml",
      "application/atomsvc+xml; charset=utf-8"
      );
  protected final static List<String> QUERY_OPTIONS = Arrays.asList(
      "",
      "?$format=xml",
      "?$format=atom",
      "?$format=json"
      );
  
  public static final List<String> CONTENT_TYPE_VALUES = Arrays.asList(
      "text/plain",
      "text/plain; charset=utf-8",
      "application/json",
      "application/json; charset=utf-8",
      "application/xml",
      "application/xml; charset=utf-8",
      "application/atom+xml",
      "application/atom+xml; charset=utf-8"
      );

  protected static ODataService createService() throws ODataException {
    DataContainer dataContainer = new DataContainer();
    dataContainer.init();
    final ODataSingleProcessor processor = new ListsProcessor(new ScenarioDataSource(dataContainer));
    final EdmProvider provider = new ScenarioEdmProvider();
    return new ODataSingleProcessorService(provider, processor) {};
  }

  protected final static TestServer server = new TestServer();


  protected URI getEndpoint() {
    return server.getEndpoint();
  }

  @BeforeClass
  public static void beforeClass() {
    try {
      ODataService service = createService();
      server.startServer(service);
    } catch (final ODataException e) {
      throw new TestutilException(e);
    } 
  }

  @AfterClass
  public static void afterClass() {
    server.stopServer();
  }
  
  @Before
  public void before() throws InterruptedException {
    TimeUnit.MILLISECONDS.sleep(250);
  }

  
  //
  //
  //
  //
  //

  protected static class FitTestSetBuilder {
    private final FitTestSet testSet;
    
    public FitTestSetBuilder(FitTestSet testSet) {
      this.testSet = testSet;
    }

    public FitTestSetBuilder queryOptions(List<String> queryOptions) {
      testSet.queryOptions = queryOptions;
      return this;
    }
    public FitTestSetBuilder acceptHeader(List<String> acceptHeader) {
      testSet.acceptHeader = acceptHeader;
      return this;
    }
    public FitTestSetBuilder expectedStatusCode(int expectedStatusCode) {
      testSet.expectedStatusCode = expectedStatusCode;
      return this;
    }
    public FitTestSetBuilder expectedContentType(String expectedContentType) {
      testSet.expectedContentType = expectedContentType;
      return this;
    }

    public FitTestSet init() {
      return init(true);
    }

    public FitTestSet init(boolean populate) {
      if(populate) {
        testSet.populate();
      }
      return testSet;
    }

    public FitTestSetBuilder content(String content) {
      testSet.content = content;
      return this;
    }

    public FitTestSetBuilder httpMethod(String httpMethod) {
      testSet.httpMethod = httpMethod;
      return this;
    }

    public FitTestSetBuilder requestContentTypes(List<String> contentTypes) {
      testSet.requestContentTypes = contentTypes;
      return this;
    }
  }
  
  /**
   * A set of {@link FitTest} which can be executed against a service endpoint.
   * 
   */
  protected static class FitTestSet {

    private final Set<FitTest> testParameters = new HashSet<AbstractContentNegotiationTest.FitTest>();
    
    private final UriType uriType;
    private final String path;

    private List<String> queryOptions = Arrays.asList("");
    private List<String> acceptHeader = Arrays.asList("");
    private List<String> requestContentTypes = Arrays.asList("");

    private int expectedStatusCode = HttpStatusCodes.OK.getStatusCode();
    private String expectedContentType = null;
    private String httpMethod = "GET";
    private String content = null;

    private FitTestSet(UriType uriType, String path) {
      super();
      this.uriType = uriType;
      this.path = path;
    }

    public static FitTestSetBuilder create(UriType uriType, String path) {
      return create(uriType, path, true, true, false);
    }

    public static FitTestSetBuilder create(UriType uriType, String path, 
        boolean defaultQueryOptions, boolean defaultAcceptHeaders, boolean defaultRequestContentTypeHeaders) {

      FitTestSetBuilder builder = new FitTestSetBuilder(new FitTestSet(uriType, path));
      if(defaultQueryOptions) {
        builder.queryOptions(QUERY_OPTIONS);
      }
      if(defaultAcceptHeaders) {
        builder.acceptHeader(ACCEPT_HEADER_VALUES);
      }
      if(defaultRequestContentTypeHeaders) {
        builder.requestContentTypes(CONTENT_TYPE_VALUES);
      }
      return builder;
    }

    public void populate() {
      testParameters.addAll(FitTest.create(this));
    }

    public void setTestParam(FitTest fitTest) {
      testParameters.remove(fitTest);
      testParameters.add(fitTest);
    }

    public void modifyRequestContentTypes(List<String> requestContentTypes, int expectedStatusCode, String expectedContentType) {
      FitTestSet fts = new FitTestSetBuilder(this)
                        .requestContentTypes(requestContentTypes)
                        .expectedStatusCode(expectedStatusCode)
                        .expectedContentType(expectedContentType).init(false);
      replaceTestParameters(FitTest.create(fts));
    }

    public void setTestParam(List<String> acceptHeader, int expectedStatusCode, String expectedContentType) {
      setTestParam(queryOptions, acceptHeader, expectedStatusCode, expectedContentType);
    }
    
    public void setTestParam(List<String> queryOptions, List<String> acceptHeader, int expectedStatusCode, String expectedContentType) {
      List<FitTest> tp = FitTest.create(this, queryOptions, acceptHeader, expectedStatusCode, expectedContentType);
      replaceTestParameters(tp);
    }

    private void replaceTestParameters(List<FitTest> tp) {
      testParameters.removeAll(tp);
      testParameters.addAll(tp);
    }

    
    public void execute(URI serviceEndpoint) throws Exception {
      Map<FitTest, AssertionError> test2Failure = new HashMap<AbstractContentNegotiationTest.FitTest, AssertionError>();
      List<FitTest> successTests = new ArrayList<AbstractContentNegotiationTest.FitTest>();
      
      for (FitTest testParam : testParameters) {
        try {
          testParam.execute(serviceEndpoint);
          successTests.add(testParam);
        } catch(AssertionError e) {
          test2Failure.put(testParam, e);
        }
      }
      
//      System.out.println("#########################################");
//      System.out.println("# Success: '" + successTests.size() + "', failed '" + test2Failure.size() +
//          "', total '" + testParameters.size() + "'.");
//      System.out.println("#########################################");

      if(!test2Failure.isEmpty()) {
        Set<Entry<FitTest, AssertionError>> failedTests = test2Failure.entrySet();
        List<AssertionError> errors = new ArrayList<AssertionError>();
        for (Entry<FitTest, AssertionError> entry : failedTests) {
          errors.add(entry.getValue());
        }
        Assert.fail("Found '" + test2Failure.size() + "' test failures. See [\n" + errors + "]");
      }
    }
  }

  static class FitTestBuilder {
    private FitTest test;
    
    public FitTestBuilder(FitTestSet testSet) {
      test = new FitTest(testSet);
    }
    public FitTestBuilder(UriType uriType, String httpMethod, String path, int expectedStatusCode, String expectedContentType) {
      test = new FitTest(uriType, httpMethod, path, expectedStatusCode, expectedContentType);
    }
    public FitTestBuilder queryOptions(String queryOptions) {
      test.queryOptions = queryOptions;
      return this;
    }
    public FitTestBuilder content(String content) {
      test.request.content = content;
      return this;
    }
    /**
     * Set header with name to given value without any sort of checking or validation of value.
     * @param name
     * @param value
     * @return
     */
    public FitTestBuilder header(String name, String value) {
      test.request.headers.put(name, value);
      return this;
    }
    /**
     * Set the accept header if value is not <code>NULL</code> and has a <code>length > 0</code>
     * @param value
     * @return
     */
    public FitTestBuilder acceptHeader(String value) {
      if(value != null && value.length() > 0) {
        return header(HttpHeaders.ACCEPT, value);
      }
      return this;
    }
    /**
     * Set the accept header if value is not <code>NULL</code> and has a <code>length > 0</code>
     * @param value
     * @return
     */
    public FitTestBuilder contentTypeHeader(String value) {
      if(value != null && value.length() > 0) {
        return header(HttpHeaders.CONTENT_TYPE, value);
      }
      return this;
    }
    public FitTestBuilder isResponseContentExpected(boolean isExpected) {
      test.isContentExpected = isExpected;
      return this;
    }
    
    public FitTest build() {
      return test;
    }
  }

  
  /**
   * Combination of test parameters and expected test result which can be tested/executed against a service endpoint.
   */
  protected static class FitTest {
    private final UriType uriType;
    private final String path;
    private final FitTestRequest request;
    
    private String queryOptions;
    
    private String requestLine;
    
    private int expectedStatusCode;
    private String expectedContentType;
    private boolean isContentExpected;

    public FitTest(FitTestSet testSet) {
      this(testSet.uriType, testSet.httpMethod, testSet.path, testSet.expectedStatusCode, testSet.expectedContentType);
    }
    
    public FitTest(UriType uriType, String httpMethod, String path,
        int expectedStatusCode, String expectedContentType) {
      super();
      this.uriType = uriType;
      this.path = path;
      this.expectedStatusCode = expectedStatusCode;
      this.expectedContentType = expectedContentType;
      this.isContentExpected = false;
      //
      this.request = new FitTestRequest();
      request.type = httpMethod;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return "TestParam [testUrl=" + requestLine + ", \n\turiType=" + uriType + ", path=" + path + 
          ", queryOption=" + queryOptions + ", request=" + request + 
          ", expectedStatusCode=" + expectedStatusCode + ", expectedContentType="
          + expectedContentType + ", isContentExpected=" + isContentExpected + "]";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      String acceptHeader = request.headers.get(HttpHeaders.ACCEPT);
      result = prime * result + ((acceptHeader == null) ? 0 : acceptHeader.hashCode());
      String contentType = request.headers.get(HttpHeaders.CONTENT_TYPE);
      result = prime * result + ((contentType == null) ? 0 : contentType.hashCode());
      result = prime * result + ((path == null) ? 0 : path.hashCode());
      result = prime * result + ((queryOptions == null) ? 0 : queryOptions.hashCode());
      return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      FitTest other = (FitTest) obj;
      if (request == null) {
        if (other.request != null)
          return false;
      } else if (!request.equals(other.request))
        return false;
      if (path == null) {
        if (other.path != null)
          return false;
      } else if (!path.equals(other.path))
        return false;
      if (queryOptions == null) {
        if (other.queryOptions != null)
          return false;
      } else if (!queryOptions.equals(other.queryOptions))
        return false;
      return true;
    }

    public void execute(URI serviceEndpoint) throws Exception {
      HttpRequestBase request = null;
      
      try {
        String endpoint = serviceEndpoint.toASCIIString();
        String requestUrl = endpoint.substring(0, endpoint.length()-1) + path;
        if(queryOptions != null) {
          requestUrl += queryOptions;
        }
        request = this.request.createRequest(requestUrl);
        
        requestLine = request.getRequestLine().toString();
        HttpClient httpClient = new DefaultHttpClient();
        LOG.debug("Execute test for [" + requestLine + "]");
        final HttpResponse response = httpClient.execute(request);
        LOG.debug("Got response for request [" + requestLine + "]");
        
        int resultStatusCode = response.getStatusLine().getStatusCode();
        assertEquals("Unexpected status code for " + toString(), expectedStatusCode, resultStatusCode);
    
        final String contentType = response.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
        assertEquals("Unexpected content type for " + toString(), ContentType.create(expectedContentType), ContentType.create(contentType));
    
        if(isContentExpected) {
          assertNotNull("Unexpected content for " + toString(), StringHelper.inputStreamToString(response.getEntity().getContent()));
        }
        LOG.trace("Test passed [" + toString() + "]");
      } finally {
        TimeUnit.MILLISECONDS.sleep(10);
        if(request != null) {
          request.releaseConnection();
          LOG.debug("Released connection [" + requestLine + "]");
        }
        TimeUnit.MILLISECONDS.sleep(10);
      }
    }

    public static FitTestBuilder init(UriType uriType, String httpMethod, String path,
        int expectedStatusCode, String expectedContentType) {
      return new FitTestBuilder(uriType, httpMethod, path, expectedStatusCode, expectedContentType);
    }

    public static FitTestBuilder init(FitTestSet fitTestSet) {
      return new FitTestBuilder(fitTestSet);
    }

    public static FitTest create(UriType uriType, String httpMethod, String path, String queryOption, String acceptHeader, 
        String content, String requestContentType,
        int expectedStatusCode, String expectedContentType) {

      return init(uriType, httpMethod, path, expectedStatusCode, expectedContentType)
          .queryOptions(queryOption)
          .content(content).contentTypeHeader(requestContentType)
          .acceptHeader(acceptHeader)
          .build();
    }

    private static List<FitTest> create(FitTestSet fitTestSet, 
        List<String> queryOptions, List<String> acceptHeaders, 
        int expectedStatusCode, String expectedContentType) {

      Map<String, ContentType> acceptHeader2ContentType = new HashMap<String, ContentType>();
      for (String acceptHeader : acceptHeaders) {
        acceptHeader2ContentType.put(acceptHeader, ContentType.create(expectedContentType));
      }
      
      String content = fitTestSet.content;
      List<String> reqContentTypes = fitTestSet.requestContentTypes;
      return create(fitTestSet.uriType, fitTestSet.httpMethod, fitTestSet.path, 
          queryOptions, acceptHeaders, acceptHeader2ContentType, 
          content, reqContentTypes, expectedStatusCode);
    }

    public static List<FitTest> create(FitTestSet fitTestSet) {
      Map<String, ContentType> acceptHeader2ContentType = new HashMap<String, ContentType>();
      if(fitTestSet.expectedContentType != null) {
        for (String acceptHeader : fitTestSet.acceptHeader) {
          acceptHeader2ContentType.put(acceptHeader, ContentType.create(fitTestSet.expectedContentType));
        }
      }
      
      return create(fitTestSet, acceptHeader2ContentType);
    }
    
    public static List<FitTest> create(FitTestSet fitTestSet, Map<String, ContentType> acceptHeader2ContentType) {
      UriType uriType = fitTestSet.uriType;
      String httpMethod = fitTestSet.httpMethod;
      String path = fitTestSet.path;
      List<String> queryOptions = fitTestSet.queryOptions;
      List<String> acceptHeaders = fitTestSet.acceptHeader;
      String content = fitTestSet.content; 
      List<String> requestContentTypeHeaders = fitTestSet.requestContentTypes;
      int expectedStatusCode = fitTestSet.expectedStatusCode;
      
      return create(uriType, httpMethod, path, queryOptions, acceptHeaders, acceptHeader2ContentType, 
          content, requestContentTypeHeaders, expectedStatusCode);
    }

    /**
     * 
     */
    private static List<FitTest> create(UriType uriType, String httpMethod, String path, List<String> queryOptions, 
        List<String> acceptHeaders, Map<String, ContentType> acceptHeader2ContentType, 
        String content, List<String> requestContentTypeHeaders, int expectedStatusCode) {

      List<FitTest> testParameters = new ArrayList<AbstractContentNegotiationTest.FitTest>();
      
      for (String queryOption: queryOptions) {
        for (String acceptHeader: acceptHeaders) {
          for (String requestContentType : requestContentTypeHeaders) {
            String expectedContentType = getExpectedResponseContentType(acceptHeader2ContentType, acceptHeader);
            FitTest tp = init(uriType, httpMethod, path, expectedStatusCode, expectedContentType)
                .queryOptions(queryOption)
                .acceptHeader(acceptHeader)
                .content(content).contentTypeHeader(requestContentType)
                .build();
            testParameters.add(tp);
          }
        }
      }

      return testParameters;
    }



    private static String getExpectedResponseContentType(Map<String, ContentType> acceptHeader2ContentType, String acceptHeader) {
      String expectedContentType = null;
      if(acceptHeader != null) {
        ContentType tmpContentType = acceptHeader2ContentType.get(acceptHeader);
        if(tmpContentType == null) {
          tmpContentType = ContentType.create(ContentType.create(acceptHeader), 
              ContentType.PARAMETER_CHARSET, ContentType.CHARSET_UTF_8);
        }
        expectedContentType = tmpContentType.toContentTypeString();
      }
      return expectedContentType;
    }
  }
  
  static class FitTestRequest {
    String type;
    Map<String, String> headers = new HashMap<String, String>();
    String content;

    String requestUrl;
    
    HttpRequestBase createRequest(String requestUrl) {
      this.requestUrl = requestUrl;
      URI uri = URI.create(requestUrl);
      HttpRequestBase request;
      // first try read (GET)
      if("GET".equals(type)) {
        request = new HttpGet(uri);
      } else { //then try write
        HttpEntityEnclosingRequestBase writeRequest;
        if("POST".equals(type)) {
          writeRequest = new HttpPost(uri);
        } else if("PUT".equals(type)) {
          writeRequest = new HttpPut(uri);
        } else {
          throw new IllegalArgumentException("Unsupported HttpMethod of type '" + type + "'.");
        }
        // common write parts
        HttpEntity entity = createEntity();
        writeRequest.setEntity(entity);
        request = writeRequest;
      }
      
      // common request parts
      Set<Entry<String, String>> entries = headers.entrySet();
      
      for (Entry<String, String> entry : entries) {
        request.addHeader(entry.getKey(), entry.getValue());
      }
      
      return request;
    }

    private HttpEntity createEntity() {
      if(content == null) {
        throw new IllegalArgumentException("Found NULL content for '" + toFullString() + "' request.");          
      }
      String contentType = headers.get(HttpHeaders.CONTENT_TYPE);
      if(contentType == null) {
        throw new IllegalArgumentException("Found NONE Content-Type header for '" + toFullString() + "' request.");          
      }
      org.apache.http.entity.ContentType ct = org.apache.http.entity.ContentType.create(contentType);
      return new StringEntity(content, ct);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((content == null) ? 0 : content.hashCode());
      result = prime * result + ((headers == null) ? 0 : headers.hashCode());
      result = prime * result + ((type == null) ? 0 : type.hashCode());
      return result;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      FitTestRequest other = (FitTestRequest) obj;
      if (content == null) {
        if (other.content != null)
          return false;
      } else if (!content.equals(other.content))
        return false;
      if (headers == null) {
        if (other.headers != null)
          return false;
      } else if (!headers.equals(other.headers))
        return false;
      if (type == null) {
        if (other.type != null)
          return false;
      } else if (!type.equals(other.type))
        return false;
      return true;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return "FTR [type=" + type + ", headers=" + headers + "]";
    }

    public String toFullString() {
      return "FitTestRequest [type=" + type + ", requestUrl=" + requestUrl + ", headers=" + headers + ", content=\n{" + content + "\n}]";
    }
  }
}
