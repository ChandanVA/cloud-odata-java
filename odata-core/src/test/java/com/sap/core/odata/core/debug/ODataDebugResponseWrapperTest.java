package com.sap.core.odata.core.debug;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.sap.core.odata.api.commons.HttpContentType;
import com.sap.core.odata.api.commons.HttpHeaders;
import com.sap.core.odata.api.commons.HttpStatusCodes;
import com.sap.core.odata.api.commons.ODataHttpMethod;
import com.sap.core.odata.api.edm.EdmNavigationProperty;
import com.sap.core.odata.api.edm.EdmProperty;
import com.sap.core.odata.api.exception.ODataException;
import com.sap.core.odata.api.exception.ODataMessageException;
import com.sap.core.odata.api.processor.ODataContext;
import com.sap.core.odata.api.processor.ODataContext.RuntimeMeasurement;
import com.sap.core.odata.api.processor.ODataResponse;
import com.sap.core.odata.api.uri.NavigationPropertySegment;
import com.sap.core.odata.api.uri.PathInfo;
import com.sap.core.odata.api.uri.SelectItem;
import com.sap.core.odata.api.uri.UriInfo;
import com.sap.core.odata.testutil.fit.BaseTest;

/**
 * Tests for the debug information output.
 * @author SAP AG
 */
public class ODataDebugResponseWrapperTest extends BaseTest {

  private ODataContext mockContext(final ODataHttpMethod method) throws ODataException {
    ODataContext context = mock(ODataContext.class);
    when(context.getHttpMethod()).thenReturn(method.name());
    PathInfo pathInfo = mock(PathInfo.class);
    when(pathInfo.getRequestUri()).thenReturn(URI.create("http://test/entity"));
    when(context.getPathInfo()).thenReturn(pathInfo);
    return context;
  }

  private ODataResponse mockResponse(final HttpStatusCodes status, final String body, final String contentType) {
    ODataResponse response = mock(ODataResponse.class);
    when(response.getStatus()).thenReturn(status);
    if (body != null) {
      final InputStream stream = new ByteArrayInputStream(body.getBytes());
      when(response.getEntity()).thenReturn(stream);
    }
    if (contentType != null) {
      Set<String> headerNames = new HashSet<String>();
      headerNames.add(HttpHeaders.CONTENT_TYPE);
      when(response.getHeaderNames()).thenReturn(headerNames);
      when(response.getHeader(HttpHeaders.CONTENT_TYPE)).thenReturn(contentType);
      when(response.getContentHeader()).thenReturn(contentType);
    }
    return response;
  }

  private RuntimeMeasurement mockRuntimeMeasurement(final String method, final long start, final long stop) {
    RuntimeMeasurement measurement = mock(RuntimeMeasurement.class);
    when(measurement.getClassName()).thenReturn("class");
    when(measurement.getMethodName()).thenReturn(method);
    when(measurement.getTimeStarted()).thenReturn(start);
    when(measurement.getTimeStopped()).thenReturn(stop);
    return measurement;
  }

  @Test
  public void minimal() throws Exception {
    final ODataContext context = mockContext(ODataHttpMethod.PUT);
    final ODataResponse wrappedResponse = mockResponse(HttpStatusCodes.NO_CONTENT, null, null);

    final ODataResponse response = new ODataDebugResponseWrapper(context, wrappedResponse, mock(UriInfo.class), null, ODataDebugResponseWrapper.ODATA_DEBUG_JSON)
        .wrapResponse();

    assertEquals("{\"body\":null,"
        + "\"request\":{\"method\":\"PUT\",\"uri\":\"http://test/entity\",\"headers\":{}},"
        + "\"response\":{\"status\":{\"code\":204,\"info\":\"No Content\"},\"headers\":{}},"
        + "\"uri\":{\"expand/select\":{\"all\":true,\"properties\":[],\"links\":[]}},"
        + "\"runtime\":[]}", response.getEntity());
  }

  @Test
  public void body() throws Exception {
    final ODataContext context = mockContext(ODataHttpMethod.GET);
    ODataResponse wrappedResponse = mockResponse(HttpStatusCodes.OK, "\"test\"", HttpContentType.APPLICATION_JSON);

    ODataResponse response = new ODataDebugResponseWrapper(context, wrappedResponse, mock(UriInfo.class), null, ODataDebugResponseWrapper.ODATA_DEBUG_JSON)
        .wrapResponse();

    final String expected = "{\"body\":\"test\","
        + "\"request\":{\"method\":\"GET\",\"uri\":\"http://test/entity\",\"headers\":{}},"
        + "\"response\":{\"status\":{\"code\":200,\"info\":\"OK\"},"
        + "\"headers\":{\"Content-Type\":\"application/json\"}},"
        + "\"uri\":{\"expand/select\":{\"all\":true,\"properties\":[],\"links\":[]}},"
        + "\"runtime\":[]}";
    assertEquals(expected, response.getEntity());

    wrappedResponse = mockResponse(HttpStatusCodes.OK, "test", HttpContentType.TEXT_PLAIN);
    response = new ODataDebugResponseWrapper(context, wrappedResponse, mock(UriInfo.class), null, ODataDebugResponseWrapper.ODATA_DEBUG_JSON)
        .wrapResponse();
    assertEquals(expected.replace(HttpContentType.APPLICATION_JSON, HttpContentType.TEXT_PLAIN),
        response.getEntity());

    wrappedResponse = mockResponse(HttpStatusCodes.OK, "test", "image/png");
    response = new ODataDebugResponseWrapper(context, wrappedResponse, mock(UriInfo.class), null, ODataDebugResponseWrapper.ODATA_DEBUG_JSON)
        .wrapResponse();
    assertEquals(expected.replace(HttpContentType.APPLICATION_JSON, "image/png").replace("\"test\"", "\"dGVzdA==\""),
        response.getEntity());
  }

  @Test
  public void headers() throws Exception {
    ODataContext context = mockContext(ODataHttpMethod.GET);
    Map<String, List<String>> headers = new HashMap<String, List<String>>();
    headers.put(HttpHeaders.CONTENT_TYPE, Arrays.asList(HttpContentType.APPLICATION_JSON));
    when(context.getRequestHeaders()).thenReturn(headers);

    final ODataResponse wrappedResponse = mockResponse(HttpStatusCodes.OK, null, HttpContentType.APPLICATION_JSON);

    final ODataResponse response = new ODataDebugResponseWrapper(context, wrappedResponse, mock(UriInfo.class), null, ODataDebugResponseWrapper.ODATA_DEBUG_JSON)
        .wrapResponse();

    assertEquals("{\"body\":null,"
        + "\"request\":{\"method\":\"GET\",\"uri\":\"http://test/entity\","
        + "\"headers\":{\"Content-Type\":\"application/json\"}},"
        + "\"response\":{\"status\":{\"code\":200,\"info\":\"OK\"},"
        + "\"headers\":{\"Content-Type\":\"application/json\"}},"
        + "\"uri\":{\"expand/select\":{\"all\":true,\"properties\":[],\"links\":[]}},"
        + "\"runtime\":[]}", response.getEntity());
  }

  @Test
  public void uri() throws Exception {
    final ODataContext context = mockContext(ODataHttpMethod.GET);
    final ODataResponse wrappedResponse = mockResponse(HttpStatusCodes.OK, null, null);

    UriInfo uriInfo = mock(UriInfo.class);
    List<ArrayList<NavigationPropertySegment>> expand = new ArrayList<ArrayList<NavigationPropertySegment>>();
    NavigationPropertySegment segment = mock(NavigationPropertySegment.class);
    EdmNavigationProperty navigationProperty = mock(EdmNavigationProperty.class);
    when(navigationProperty.getName()).thenReturn("nav");
    when(segment.getNavigationProperty()).thenReturn(navigationProperty);
    ArrayList<NavigationPropertySegment> segments = new ArrayList<NavigationPropertySegment>();
    segments.add(segment);
    expand.add(segments);
    when(uriInfo.getExpand()).thenReturn(expand);
    SelectItem select1 = mock(SelectItem.class);
    SelectItem select2 = mock(SelectItem.class);
    EdmProperty property = mock(EdmProperty.class);
    when(property.getName()).thenReturn("property");
    when(select1.getProperty()).thenReturn(property);
    when(select2.getProperty()).thenReturn(property);
    when(select2.getNavigationPropertySegments()).thenReturn(segments);
    when(uriInfo.getSelect()).thenReturn(Arrays.asList(select1, select2));

    final ODataResponse response = new ODataDebugResponseWrapper(context, wrappedResponse, uriInfo, null, ODataDebugResponseWrapper.ODATA_DEBUG_JSON)
        .wrapResponse();

    assertEquals("{\"body\":null,"
        + "\"request\":{\"method\":\"GET\",\"uri\":\"http://test/entity\",\"headers\":{}},"
        + "\"response\":{\"status\":{\"code\":200,\"info\":\"OK\"},\"headers\":{}},"
        + "\"uri\":{\"expand/select\":{\"all\":false,\"properties\":[\"property\"],"
        + "\"links\":[{\"nav\":{\"all\":false,\"properties\":[\"property\"],\"links\":[]}}]}},"
        + "\"runtime\":[]}", response.getEntity());
  }

  @Test
  public void runtime() throws Exception {
    ODataContext context = mockContext(ODataHttpMethod.GET);
    List<RuntimeMeasurement> runtimeMeasurements = new ArrayList<RuntimeMeasurement>();
    runtimeMeasurements.add(mockRuntimeMeasurement("method", 1000, 42000));
    runtimeMeasurements.add(mockRuntimeMeasurement("inner", 2000, 5000));
    runtimeMeasurements.add(mockRuntimeMeasurement("inner", 7000, 12000));
    runtimeMeasurements.add(mockRuntimeMeasurement("inner", 13000, 16000));
    runtimeMeasurements.add(mockRuntimeMeasurement("inner2", 14000, 15000));
    runtimeMeasurements.add(mockRuntimeMeasurement("child", 17000, 21000));
    runtimeMeasurements.add(mockRuntimeMeasurement("second", 45000, 99000));
    when(context.getRuntimeMeasurements()).thenReturn(runtimeMeasurements);

    final ODataResponse wrappedResponse = mockResponse(HttpStatusCodes.OK, null, null);

    ODataResponse response = new ODataDebugResponseWrapper(context, wrappedResponse, mock(UriInfo.class), null, ODataDebugResponseWrapper.ODATA_DEBUG_JSON)
        .wrapResponse();

    assertEquals("{\"body\":null,"
        + "\"request\":{\"method\":\"GET\",\"uri\":\"http://test/entity\",\"headers\":{}},"
        + "\"response\":{\"status\":{\"code\":200,\"info\":\"OK\"},\"headers\":{}},"
        + "\"uri\":{\"expand/select\":{\"all\":true,\"properties\":[],\"links\":[]}},"
        + "\"runtime\":[{\"class\":\"class\",\"method\":\"method\",\"duration\":41,"
        + "\"children\":[{\"class\":\"class\",\"method\":\"inner\",\"duration\":8,\"children\":[]},"
        + "{\"class\":\"class\",\"method\":\"inner\",\"duration\":3,\"children\":["
        + "{\"class\":\"class\",\"method\":\"inner2\",\"duration\":1,\"children\":[]}]},"
        + "{\"class\":\"class\",\"method\":\"child\",\"duration\":4,\"children\":[]}]},"
        + "{\"class\":\"class\",\"method\":\"second\",\"duration\":54,\"children\":[]}]}",
        response.getEntity());
  }

  @Test
  public void exception() throws Exception {
    final ODataContext context = mockContext(ODataHttpMethod.GET);
    final ODataResponse wrappedResponse = mockResponse(HttpStatusCodes.BAD_REQUEST, null, null);

    ODataMessageException exception = mock(ODataMessageException.class);
    when(exception.getMessageReference()).thenReturn(ODataMessageException.COMMON);
    when(exception.getStackTrace()).thenReturn(new StackTraceElement[] {
        new StackTraceElement("class", "method", "file", 42) });

    final ODataResponse response = new ODataDebugResponseWrapper(context, wrappedResponse, mock(UriInfo.class), exception, ODataDebugResponseWrapper.ODATA_DEBUG_JSON)
        .wrapResponse();

    assertEquals("{\"body\":null,"
        + "\"request\":{\"method\":\"GET\",\"uri\":\"http://test/entity\",\"headers\":{}},"
        + "\"response\":{\"status\":{\"code\":400,\"info\":\"Bad Request\"},\"headers\":{}},"
        + "\"uri\":{\"expand/select\":{\"all\":true,\"properties\":[],\"links\":[]}},"
        + "\"runtime\":[],"
        + "\"stacktrace\":{\"exceptions\":[{\"class\":\"" + exception.getClass().getName() + "\","
        + "\"message\":\"Common exception\","
        + "\"invocation\":{\"class\":\"class\",\"method\":\"method\",\"line\":42}}],"
        + "\"stacktrace\":[{\"class\":\"class\",\"method\":\"method\",\"line\":42}]}}",
        response.getEntity());
  }
}
