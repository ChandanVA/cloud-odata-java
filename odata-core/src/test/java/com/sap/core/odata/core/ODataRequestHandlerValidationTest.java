package com.sap.core.odata.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.sap.core.odata.api.ODataService;
import com.sap.core.odata.api.ODataServiceFactory;
import com.sap.core.odata.api.commons.HttpContentType;
import com.sap.core.odata.api.commons.HttpStatusCodes;
import com.sap.core.odata.api.commons.ODataHttpMethod;
import com.sap.core.odata.api.edm.Edm;
import com.sap.core.odata.api.edm.EdmEntityType;
import com.sap.core.odata.api.edm.EdmFacets;
import com.sap.core.odata.api.edm.EdmProperty;
import com.sap.core.odata.api.exception.ODataException;
import com.sap.core.odata.api.processor.ODataContext;
import com.sap.core.odata.api.processor.ODataProcessor;
import com.sap.core.odata.api.processor.ODataRequest;
import com.sap.core.odata.api.processor.ODataResponse;
import com.sap.core.odata.api.processor.part.BatchProcessor;
import com.sap.core.odata.api.processor.part.EntityComplexPropertyProcessor;
import com.sap.core.odata.api.processor.part.EntityLinkProcessor;
import com.sap.core.odata.api.processor.part.EntityLinksProcessor;
import com.sap.core.odata.api.processor.part.EntityMediaProcessor;
import com.sap.core.odata.api.processor.part.EntityProcessor;
import com.sap.core.odata.api.processor.part.EntitySetProcessor;
import com.sap.core.odata.api.processor.part.EntitySimplePropertyProcessor;
import com.sap.core.odata.api.processor.part.EntitySimplePropertyValueProcessor;
import com.sap.core.odata.api.processor.part.FunctionImportProcessor;
import com.sap.core.odata.api.processor.part.FunctionImportValueProcessor;
import com.sap.core.odata.api.processor.part.MetadataProcessor;
import com.sap.core.odata.api.processor.part.ServiceDocumentProcessor;
import com.sap.core.odata.api.uri.PathInfo;
import com.sap.core.odata.api.uri.PathSegment;
import com.sap.core.odata.api.uri.UriParser;
import com.sap.core.odata.core.commons.ContentType;
import com.sap.core.odata.core.commons.ContentType.ODataFormat;
import com.sap.core.odata.core.uri.UriInfoImpl;
import com.sap.core.odata.core.uri.UriType;
import com.sap.core.odata.testutil.fit.BaseTest;
import com.sap.core.odata.testutil.mock.MockFacade;

/**
 * Tests for the validation of HTTP method, URI path, query options,
 * and request-body content type.
 * @author SAP AG
 */
public class ODataRequestHandlerValidationTest extends BaseTest {

  private Edm edm = null;

  @Before
  public void setEdm() throws ODataException {
    edm = MockFacade.getMockEdm();
  }

  private List<String> mockPathSegments(final UriType uriType, final boolean moreNavigation, final boolean isValue) {
    List<String> segments = new ArrayList<String>();

    if (uriType == UriType.URI1 || uriType == UriType.URI15) {
      if (moreNavigation) {
        segments.add("Managers('1')");
        segments.add("nm_Employees");
      } else {
        segments.add("Employees");
      }
    } else if (uriType == UriType.URI2 || uriType == UriType.URI3
        || uriType == UriType.URI4 || uriType == UriType.URI5
        || uriType == UriType.URI16 || uriType == UriType.URI17) {
      if (moreNavigation) {
        segments.add("Managers('1')");
        segments.add("nm_Employees('1')");
      } else {
        segments.add("Employees('1')");
      }
    } else if (uriType == UriType.URI6A || uriType == UriType.URI7A || uriType == UriType.URI50A) {
      segments.add("Managers('1')");
      if (moreNavigation) {
        segments.add("nm_Employees('1')");
        segments.add("ne_Manager");
      }
      if (uriType == UriType.URI7A || uriType == UriType.URI50A) {
        segments.add("$links");
      }
      segments.add("nm_Employees('1')");
    } else if (uriType == UriType.URI6B || uriType == UriType.URI7B || uriType == UriType.URI50B) {
      segments.add("Managers('1')");
      if (moreNavigation) {
        segments.add("nm_Employees('1')");
        segments.add("ne_Manager");
      }
      if (uriType == UriType.URI7B || uriType == UriType.URI50B) {
        segments.add("$links");
      }
      segments.add("nm_Employees");
    } else if (uriType == UriType.URI8) {
      segments.add("$metadata");
    } else if (uriType == UriType.URI9) {
      segments.add("$batch");
    } else if (uriType == UriType.URI10) {
      segments.add("OldestEmployee");
    } else if (uriType == UriType.URI11) {
      segments.add("AllLocations");
    } else if (uriType == UriType.URI12) {
      segments.add("MostCommonLocation");
    } else if (uriType == UriType.URI13) {
      segments.add("AllUsedRoomIds");
    } else if (uriType == UriType.URI14) {
      segments.add("MaximalAge");
    }

    if (uriType == UriType.URI3 || uriType == UriType.URI4) {
      segments.add("Location");
    }
    if (uriType == UriType.URI4) {
      segments.add("Country");
    } else if (uriType == UriType.URI5) {
      segments.add("EmployeeName");
    }

    if (uriType == UriType.URI15 || uriType == UriType.URI16
        || uriType == UriType.URI50A || uriType == UriType.URI50B) {
      segments.add("$count");
    }

    if (uriType == UriType.URI17 || isValue) {
      segments.add("$value");
    }

    // self-test
    try {
      final UriInfoImpl uriInfo = (UriInfoImpl) UriParser.parse(edm,
          MockFacade.getPathSegmentsAsODataPathSegmentMock(segments),
          Collections.<String, String> emptyMap());
      assertEquals(uriType, uriInfo.getUriType());
      assertEquals(uriType == UriType.URI17 || isValue, uriInfo.isValue());
    } catch (final ODataException e) {
      fail();
    }

    return segments;
  }

  private Map<String, String> mockOptions(
      final boolean format,
      final boolean filter, final boolean inlineCount, final boolean orderBy,
      final boolean skipToken, final boolean skip, final boolean top,
      final boolean expand, final boolean select) {

    Map<String, String> map = new HashMap<String, String>();

    if (format) {
      map.put("$format", ODataFormat.XML.toString());
    }
    if (filter) {
      map.put("$filter", "true");
    }
    if (inlineCount) {
      map.put("$inlinecount", "none");
    }
    if (orderBy) {
      map.put("$orderby", "Age");
    }
    if (skipToken) {
      map.put("$skiptoken", "x");
    }
    if (skip) {
      map.put("$skip", "0");
    }
    if (top) {
      map.put("$top", "0");
    }
    if (expand) {
      map.put("$expand", "ne_Team");
    }
    if (select) {
      map.put("$select", "Age");
    }

    return map;
  }

  private ODataRequest mockODataRequest(
      final ODataHttpMethod method,
      final List<String> pathSegments,
      final Map<String, String> queryParameters,
      final String requestContentType) throws ODataException {
    ODataRequest request = mock(ODataRequest.class);
    when(request.getMethod()).thenReturn(method);
    PathInfo pathInfo = mock(PathInfo.class);
    List<PathSegment> segments = new ArrayList<PathSegment>();
    for (final String pathSegment : pathSegments) {
      PathSegment segment = mock(PathSegment.class);
      when(segment.getPath()).thenReturn(pathSegment);
      segments.add(segment);
    }
    when(pathInfo.getODataSegments()).thenReturn(segments);
    when(request.getPathInfo()).thenReturn(pathInfo);
    when(request.getQueryParameters())
        .thenReturn(queryParameters == null ? new HashMap<String, String>() : queryParameters);
    when(request.getContentType()).thenReturn(requestContentType);
    return request;
  }

  private ODataService mockODataService(final ODataServiceFactory serviceFactory) throws ODataException {
    ODataService service = DispatcherTest.getMockService();
    when(service.getEntityDataModel()).thenReturn(edm);
    when(service.getProcessor()).thenReturn(mock(ODataProcessor.class));
    when(serviceFactory.createService(Matchers.any(ODataContext.class))).thenReturn(service);

    Mockito.when(service.getSupportedContentTypes(BatchProcessor.class)).thenReturn(
        Arrays.asList(HttpContentType.MULTIPART_MIXED));

    Mockito.when(service.getSupportedContentTypes(EntityProcessor.class)).thenReturn(Arrays.asList(
        HttpContentType.APPLICATION_ATOM_XML_ENTRY_UTF8,
        HttpContentType.APPLICATION_ATOM_XML_UTF8,
        HttpContentType.APPLICATION_JSON_UTF8,
        HttpContentType.APPLICATION_JSON_UTF8_VERBOSE,
        HttpContentType.APPLICATION_XML_UTF8));

    Mockito.when(service.getSupportedContentTypes(FunctionImportProcessor.class)).thenReturn(Arrays.asList(
        HttpContentType.APPLICATION_JSON_UTF8,
        HttpContentType.APPLICATION_JSON_UTF8_VERBOSE,
        HttpContentType.APPLICATION_XML_UTF8));
    Mockito.when(service.getSupportedContentTypes(EntityLinkProcessor.class)).thenReturn(Arrays.asList(
        HttpContentType.APPLICATION_JSON_UTF8,
        HttpContentType.APPLICATION_JSON_UTF8_VERBOSE,
        HttpContentType.APPLICATION_XML_UTF8));
    Mockito.when(service.getSupportedContentTypes(EntityLinksProcessor.class)).thenReturn(Arrays.asList(
        HttpContentType.APPLICATION_JSON_UTF8,
        HttpContentType.APPLICATION_JSON_UTF8_VERBOSE,
        HttpContentType.APPLICATION_XML_UTF8));
    Mockito.when(service.getSupportedContentTypes(EntitySimplePropertyProcessor.class)).thenReturn(Arrays.asList(
        HttpContentType.APPLICATION_JSON_UTF8,
        HttpContentType.APPLICATION_JSON_UTF8_VERBOSE,
        HttpContentType.APPLICATION_XML_UTF8));
    Mockito.when(service.getSupportedContentTypes(EntityComplexPropertyProcessor.class)).thenReturn(Arrays.asList(
        HttpContentType.APPLICATION_JSON_UTF8,
        HttpContentType.APPLICATION_JSON_UTF8_VERBOSE,
        HttpContentType.APPLICATION_XML_UTF8));

    Mockito.when(service.getSupportedContentTypes(EntityMediaProcessor.class)).thenReturn(Arrays.asList(
        HttpContentType.WILDCARD));
    Mockito.when(service.getSupportedContentTypes(EntitySimplePropertyValueProcessor.class)).thenReturn(Arrays.asList(
        HttpContentType.WILDCARD));
    Mockito.when(service.getSupportedContentTypes(FunctionImportValueProcessor.class)).thenReturn(Arrays.asList(
        HttpContentType.WILDCARD));

    Mockito.when(service.getSupportedContentTypes(EntitySetProcessor.class)).thenReturn(Arrays.asList(
        HttpContentType.APPLICATION_ATOM_XML_FEED_UTF8,
        HttpContentType.APPLICATION_ATOM_XML_UTF8,
        HttpContentType.APPLICATION_JSON_UTF8,
        HttpContentType.APPLICATION_JSON_UTF8_VERBOSE,
        HttpContentType.APPLICATION_XML_UTF8));

    Mockito.when(service.getSupportedContentTypes(MetadataProcessor.class)).thenReturn(Arrays.asList(
        HttpContentType.APPLICATION_XML_UTF8));

    Mockito.when(service.getSupportedContentTypes(ServiceDocumentProcessor.class)).thenReturn(Arrays.asList(
        HttpContentType.APPLICATION_ATOM_SVC_UTF8,
        HttpContentType.APPLICATION_JSON_UTF8,
        HttpContentType.APPLICATION_JSON_UTF8_VERBOSE,
        HttpContentType.APPLICATION_XML_UTF8));

    return service;
  }

  private ODataResponse executeRequest(final ODataHttpMethod method,
      final List<String> pathSegments,
      final Map<String, String> queryParameters,
      final String requestContentType) throws ODataException {
    ODataServiceFactory serviceFactory = mock(ODataServiceFactory.class);
    final ODataService service = mockODataService(serviceFactory);
    when(serviceFactory.createService(Matchers.any(ODataContext.class))).thenReturn(service);
    return new ODataRequestHandler(serviceFactory).handle(mockODataRequest(method, pathSegments, queryParameters, requestContentType));
  }

  private void checkValueContentType(final ODataHttpMethod method, final UriType uriType, final String requestContentType) throws Exception {
    executeRequest(method, mockPathSegments(uriType, false, true), null, requestContentType);
  }

  private void wrongRequest(final ODataHttpMethod method, final List<String> pathSegments, final Map<String, String> queryParameters) throws ODataException {
    final ODataResponse response = executeRequest(method, pathSegments, queryParameters, null);
    assertNotNull(response);
    assertEquals(HttpStatusCodes.METHOD_NOT_ALLOWED, response.getStatus());
  }

  private void wrongOptions(final ODataHttpMethod method, final UriType uriType,
      final boolean format,
      final boolean filter, final boolean inlineCount, final boolean orderBy,
      final boolean skipToken, final boolean skip, final boolean top,
      final boolean expand, final boolean select) throws ODataException {
    wrongRequest(method,
        mockPathSegments(uriType, false, false),
        mockOptions(format, filter, inlineCount, orderBy, skipToken, skip, top, expand, select));
  }

  private void wrongFunctionHttpMethod(final ODataHttpMethod method, final UriType uriType) throws ODataException {
    if (uriType == UriType.URI1) {
      wrongRequest(method, Arrays.asList("EmployeeSearch"), null);
    } else {
      wrongRequest(method, mockPathSegments(uriType, false, false), null);
    }
  }

  private void wrongProperty(final ODataHttpMethod method, final boolean ofComplex, final boolean key, final boolean nullable) throws ODataException {
    EdmProperty property = (EdmProperty) (ofComplex ?
        edm.getComplexType("RefScenario", "c_Location").getProperty("Country") :
        edm.getEntityType("RefScenario", "Employee").getProperty("Age"));
    EdmFacets facets = mock(EdmFacets.class);
    when(facets.isNullable()).thenReturn(nullable);
    when(property.getFacets()).thenReturn(facets);

    List<String> pathSegments = new ArrayList<String>();
    pathSegments.add("Employees('1')");
    if (ofComplex) {
      pathSegments.add("Location");
      pathSegments.add("Country");
    } else {
      pathSegments.add(key ? "EmployeeId" : "Age");
    }
    pathSegments.add("$value");

    wrongRequest(method, pathSegments, null);
  }

  private void wrongNavigationPath(final ODataHttpMethod method, final UriType uriType, final HttpStatusCodes expectedStatusCode) throws ODataException {
    final ODataResponse response = executeRequest(method, mockPathSegments(uriType, true, false), null, null);
    assertNotNull(response);
    assertEquals(expectedStatusCode, response.getStatus());
  }

  private void wrongRequestContentType(final ODataHttpMethod method, final UriType uriType, final ContentType requestContentType) throws ODataException {
    wrongRequestContentType(method, uriType, false, requestContentType);
  }

  private void wrongRequestContentType(final ODataHttpMethod method, final UriType uriType, final boolean isValue, final ContentType requestContentType) throws ODataException {
    wrongRequestContentType(method, uriType, isValue, requestContentType.toContentTypeString());
  }

  private void wrongRequestContentType(final ODataHttpMethod method, final UriType uriType, final boolean isValue, final String requestContentType) throws ODataException {
    ODataResponse response = executeRequest(method, mockPathSegments(uriType, false, isValue), null, requestContentType);
    assertNotNull(response);
    assertEquals(HttpStatusCodes.UNSUPPORTED_MEDIA_TYPE, response.getStatus());
  }

  private void unsupportedRequestContentType(final ODataHttpMethod method, final UriType uriType, final boolean isValue, final String requestContentType) throws ODataException {
    ODataResponse response = executeRequest(method, mockPathSegments(uriType, false, isValue), null, requestContentType);
    assertNotNull(response);
    assertEquals(HttpStatusCodes.UNSUPPORTED_MEDIA_TYPE, response.getStatus());
  }

  private void invalidRequestDollarFormat(final ODataHttpMethod method, final UriType uriType, final boolean isValue, final String dollarFormatOption) throws ODataException {
    Map<String, String> options = new HashMap<String, String>(3);
    options.put("$format", dollarFormatOption);
    ODataResponse response = executeRequest(method, mockPathSegments(uriType, false, isValue), options, null);
    assertNotNull(response);
    assertEquals(HttpStatusCodes.BAD_REQUEST, response.getStatus());
  }

  @Test
  public void requestContentType() throws Exception {
    executeRequest(ODataHttpMethod.PUT, mockPathSegments(UriType.URI2, false, false), null, HttpContentType.APPLICATION_XML);
    executeRequest(ODataHttpMethod.PATCH, mockPathSegments(UriType.URI2, false, false), null, HttpContentType.APPLICATION_XML);
    executeRequest(ODataHttpMethod.MERGE, mockPathSegments(UriType.URI2, false, false), null, HttpContentType.APPLICATION_XML);

    executeRequest(ODataHttpMethod.PUT, mockPathSegments(UriType.URI4, false, false), null, HttpContentType.APPLICATION_XML);
    executeRequest(ODataHttpMethod.PATCH, mockPathSegments(UriType.URI4, false, false), null, HttpContentType.APPLICATION_XML);
    executeRequest(ODataHttpMethod.MERGE, mockPathSegments(UriType.URI4, false, false), null, HttpContentType.APPLICATION_XML);

    executeRequest(ODataHttpMethod.PUT, mockPathSegments(UriType.URI5, false, false), null, HttpContentType.APPLICATION_XML);
    executeRequest(ODataHttpMethod.PATCH, mockPathSegments(UriType.URI5, false, false), null, HttpContentType.APPLICATION_XML);
    executeRequest(ODataHttpMethod.MERGE, mockPathSegments(UriType.URI5, false, false), null, HttpContentType.APPLICATION_XML);
  }

  @Test
  public void requestContentTypeMediaResource() throws Exception {
    executeRequest(ODataHttpMethod.POST, mockPathSegments(UriType.URI1, false, false), null, "image/jpeg");

    executeRequest(ODataHttpMethod.PUT, mockPathSegments(UriType.URI17, false, true), null, "image/jpeg");
  }

  @Test
  public void requestValueContentType() throws Exception {
    checkValueContentType(ODataHttpMethod.PUT, UriType.URI4, HttpContentType.TEXT_PLAIN);
    checkValueContentType(ODataHttpMethod.DELETE, UriType.URI4, HttpContentType.TEXT_PLAIN);
    checkValueContentType(ODataHttpMethod.PATCH, UriType.URI4, HttpContentType.TEXT_PLAIN);
    checkValueContentType(ODataHttpMethod.MERGE, UriType.URI4, HttpContentType.TEXT_PLAIN);
    checkValueContentType(ODataHttpMethod.PUT, UriType.URI4, HttpContentType.TEXT_PLAIN_UTF8);
    checkValueContentType(ODataHttpMethod.DELETE, UriType.URI4, HttpContentType.TEXT_PLAIN_UTF8);
    checkValueContentType(ODataHttpMethod.PATCH, UriType.URI4, HttpContentType.TEXT_PLAIN_UTF8);
    checkValueContentType(ODataHttpMethod.MERGE, UriType.URI4, HttpContentType.TEXT_PLAIN_UTF8);

    checkValueContentType(ODataHttpMethod.PUT, UriType.URI5, HttpContentType.TEXT_PLAIN);
    checkValueContentType(ODataHttpMethod.DELETE, UriType.URI5, HttpContentType.TEXT_PLAIN);
    checkValueContentType(ODataHttpMethod.PATCH, UriType.URI5, HttpContentType.TEXT_PLAIN);
    checkValueContentType(ODataHttpMethod.MERGE, UriType.URI5, HttpContentType.TEXT_PLAIN);

    checkValueContentType(ODataHttpMethod.PUT, UriType.URI17, HttpContentType.TEXT_PLAIN);
    checkValueContentType(ODataHttpMethod.DELETE, UriType.URI17, HttpContentType.TEXT_PLAIN);
  }

  @Test
  public void allowedMethods() throws Exception {
    executeRequest(ODataHttpMethod.GET, mockPathSegments(UriType.URI0, false, false), null, null);
    executeRequest(ODataHttpMethod.GET, mockPathSegments(UriType.URI1, false, false), null, null);
    executeRequest(ODataHttpMethod.POST, mockPathSegments(UriType.URI1, false, false), null, null);
    executeRequest(ODataHttpMethod.GET, mockPathSegments(UriType.URI2, false, false), null, null);
    executeRequest(ODataHttpMethod.GET, mockPathSegments(UriType.URI3, false, false), null, null);
    executeRequest(ODataHttpMethod.PATCH, mockPathSegments(UriType.URI3, false, false), null, null);
    executeRequest(ODataHttpMethod.MERGE, mockPathSegments(UriType.URI3, false, false), null, null);
    executeRequest(ODataHttpMethod.GET, mockPathSegments(UriType.URI4, false, false), null, null);
    executeRequest(ODataHttpMethod.POST, mockPathSegments(UriType.URI9, false, false), null, null);
    executeRequest(ODataHttpMethod.GET, mockPathSegments(UriType.URI15, false, false), null, null);
  }

  @Test
  public void notAllowedMethod() throws Exception {
    wrongRequest(ODataHttpMethod.DELETE, mockPathSegments(UriType.URI0, false, false), null);
    wrongRequest(ODataHttpMethod.DELETE, mockPathSegments(UriType.URI1, false, false), null);
    wrongRequest(ODataHttpMethod.POST, mockPathSegments(UriType.URI2, false, false), null);
    wrongRequest(ODataHttpMethod.DELETE, mockPathSegments(UriType.URI3, false, false), null);
    wrongRequest(ODataHttpMethod.POST, mockPathSegments(UriType.URI4, false, false), null);
    wrongRequest(ODataHttpMethod.POST, mockPathSegments(UriType.URI5, false, false), null);
    wrongRequest(ODataHttpMethod.POST, mockPathSegments(UriType.URI6A, false, false), null);
    wrongRequest(ODataHttpMethod.DELETE, mockPathSegments(UriType.URI6B, false, false), null);
    wrongRequest(ODataHttpMethod.POST, mockPathSegments(UriType.URI7A, false, false), null);
    wrongRequest(ODataHttpMethod.DELETE, mockPathSegments(UriType.URI7B, false, false), null);
    wrongRequest(ODataHttpMethod.DELETE, mockPathSegments(UriType.URI8, false, false), null);
    wrongRequest(ODataHttpMethod.DELETE, mockPathSegments(UriType.URI9, false, false), null);
    wrongRequest(ODataHttpMethod.DELETE, mockPathSegments(UriType.URI15, false, false), null);
    wrongRequest(ODataHttpMethod.DELETE, mockPathSegments(UriType.URI16, false, false), null);
    wrongRequest(ODataHttpMethod.PATCH, mockPathSegments(UriType.URI17, false, false), null);
    wrongRequest(ODataHttpMethod.DELETE, mockPathSegments(UriType.URI50A, false, false), null);
    wrongRequest(ODataHttpMethod.DELETE, mockPathSegments(UriType.URI50B, false, false), null);
  }

  @Test
  public void notAllowedOptions() throws Exception {
    wrongOptions(ODataHttpMethod.POST, UriType.URI1, true, false, false, false, false, false, false, false, false);
    wrongOptions(ODataHttpMethod.POST, UriType.URI1, false, true, false, false, false, false, false, false, false);
    wrongOptions(ODataHttpMethod.POST, UriType.URI1, false, false, true, false, false, false, false, false, false);
    wrongOptions(ODataHttpMethod.POST, UriType.URI1, false, false, false, true, false, false, false, false, false);
    wrongOptions(ODataHttpMethod.POST, UriType.URI1, false, false, false, false, true, false, false, false, false);
    wrongOptions(ODataHttpMethod.POST, UriType.URI1, false, false, false, false, false, true, false, false, false);
    wrongOptions(ODataHttpMethod.POST, UriType.URI1, false, false, false, false, false, false, true, false, false);
    wrongOptions(ODataHttpMethod.POST, UriType.URI1, false, false, false, false, false, false, false, true, false);
    wrongOptions(ODataHttpMethod.POST, UriType.URI1, false, false, false, false, false, false, false, false, true);

    wrongOptions(ODataHttpMethod.PUT, UriType.URI2, true, false, false, false, false, false, false, false, false);
    wrongOptions(ODataHttpMethod.PUT, UriType.URI2, false, false, false, false, false, false, false, true, false);
    wrongOptions(ODataHttpMethod.PUT, UriType.URI2, false, false, false, false, false, false, false, false, true);
    wrongOptions(ODataHttpMethod.PATCH, UriType.URI2, true, false, false, false, false, false, false, false, false);
    wrongOptions(ODataHttpMethod.PATCH, UriType.URI2, false, false, false, false, false, false, false, true, false);
    wrongOptions(ODataHttpMethod.PATCH, UriType.URI2, false, false, false, false, false, false, false, false, true);
    wrongOptions(ODataHttpMethod.DELETE, UriType.URI2, true, false, false, false, false, false, false, false, false);
    wrongOptions(ODataHttpMethod.DELETE, UriType.URI2, false, true, false, false, false, false, false, false, false);
    wrongOptions(ODataHttpMethod.DELETE, UriType.URI2, false, false, false, false, false, false, false, true, false);
    wrongOptions(ODataHttpMethod.DELETE, UriType.URI2, false, false, false, false, false, false, false, false, true);

    wrongOptions(ODataHttpMethod.PUT, UriType.URI3, true, false, false, false, false, false, false, false, false);
    wrongOptions(ODataHttpMethod.PATCH, UriType.URI3, true, false, false, false, false, false, false, false, false);

    wrongOptions(ODataHttpMethod.PUT, UriType.URI4, true, false, false, false, false, false, false, false, false);

    wrongOptions(ODataHttpMethod.PUT, UriType.URI5, true, false, false, false, false, false, false, false, false);

    wrongOptions(ODataHttpMethod.POST, UriType.URI6B, true, false, false, false, false, false, false, false, false);
    wrongOptions(ODataHttpMethod.POST, UriType.URI6B, false, true, false, false, false, false, false, false, false);
    wrongOptions(ODataHttpMethod.POST, UriType.URI6B, false, false, true, false, false, false, false, false, false);
    wrongOptions(ODataHttpMethod.POST, UriType.URI6B, false, false, false, true, false, false, false, false, false);
    wrongOptions(ODataHttpMethod.POST, UriType.URI6B, false, false, false, false, true, false, false, false, false);
    wrongOptions(ODataHttpMethod.POST, UriType.URI6B, false, false, false, false, false, true, false, false, false);
    wrongOptions(ODataHttpMethod.POST, UriType.URI6B, false, false, false, false, false, false, true, false, false);
    wrongOptions(ODataHttpMethod.POST, UriType.URI6B, false, false, false, false, false, false, false, true, false);
    wrongOptions(ODataHttpMethod.POST, UriType.URI6B, false, false, false, false, false, false, false, false, true);

    wrongOptions(ODataHttpMethod.PUT, UriType.URI7A, true, false, false, false, false, false, false, false, false);
    wrongOptions(ODataHttpMethod.PUT, UriType.URI7A, false, true, false, false, false, false, false, false, false);
    wrongOptions(ODataHttpMethod.DELETE, UriType.URI7A, true, false, false, false, false, false, false, false, false);
    wrongOptions(ODataHttpMethod.DELETE, UriType.URI7A, false, true, false, false, false, false, false, false, false);

    wrongOptions(ODataHttpMethod.POST, UriType.URI7B, true, false, false, false, false, false, false, false, false);
    wrongOptions(ODataHttpMethod.POST, UriType.URI7B, false, true, false, false, false, false, false, false, false);
    wrongOptions(ODataHttpMethod.POST, UriType.URI7B, false, false, true, false, false, false, false, false, false);
    wrongOptions(ODataHttpMethod.POST, UriType.URI7B, false, false, false, true, false, false, false, false, false);
    wrongOptions(ODataHttpMethod.POST, UriType.URI7B, false, false, false, false, true, false, false, false, false);
    wrongOptions(ODataHttpMethod.POST, UriType.URI7B, false, false, false, false, false, true, false, false, false);
    wrongOptions(ODataHttpMethod.POST, UriType.URI7B, false, false, false, false, false, false, true, false, false);

    wrongOptions(ODataHttpMethod.PUT, UriType.URI17, false, true, false, false, false, false, false, false, false);
    wrongOptions(ODataHttpMethod.DELETE, UriType.URI17, true, false, false, false, false, false, false, false, false);
    wrongOptions(ODataHttpMethod.DELETE, UriType.URI17, false, true, false, false, false, false, false, false, false);
  }

  @Test
  public void functionImportWrongHttpMethod() throws Exception {
    wrongFunctionHttpMethod(ODataHttpMethod.POST, UriType.URI1);
    wrongFunctionHttpMethod(ODataHttpMethod.PUT, UriType.URI10);
    wrongFunctionHttpMethod(ODataHttpMethod.POST, UriType.URI11);
    wrongFunctionHttpMethod(ODataHttpMethod.PATCH, UriType.URI12);
    wrongFunctionHttpMethod(ODataHttpMethod.POST, UriType.URI13);
    wrongFunctionHttpMethod(ODataHttpMethod.PUT, UriType.URI14);
  }

  @Test
  public void wrongProperty() throws Exception {
    wrongProperty(ODataHttpMethod.DELETE, true, false, false);

    wrongProperty(ODataHttpMethod.PUT, false, true, false);
    wrongProperty(ODataHttpMethod.PATCH, false, true, false);
    wrongProperty(ODataHttpMethod.DELETE, false, true, false);
    wrongProperty(ODataHttpMethod.DELETE, false, false, false);
  }

  @Test
  public void wrongNavigationPath() throws Exception {
    wrongNavigationPath(ODataHttpMethod.PUT, UriType.URI3, HttpStatusCodes.BAD_REQUEST);
    wrongNavigationPath(ODataHttpMethod.PATCH, UriType.URI3, HttpStatusCodes.BAD_REQUEST);

    wrongNavigationPath(ODataHttpMethod.PUT, UriType.URI4, HttpStatusCodes.BAD_REQUEST);
    wrongNavigationPath(ODataHttpMethod.PATCH, UriType.URI4, HttpStatusCodes.BAD_REQUEST);
    wrongNavigationPath(ODataHttpMethod.DELETE, UriType.URI4, HttpStatusCodes.METHOD_NOT_ALLOWED);

    wrongNavigationPath(ODataHttpMethod.PUT, UriType.URI5, HttpStatusCodes.BAD_REQUEST);
    wrongNavigationPath(ODataHttpMethod.PATCH, UriType.URI5, HttpStatusCodes.BAD_REQUEST);
    wrongNavigationPath(ODataHttpMethod.DELETE, UriType.URI5, HttpStatusCodes.METHOD_NOT_ALLOWED);

    wrongNavigationPath(ODataHttpMethod.PUT, UriType.URI7A, HttpStatusCodes.BAD_REQUEST);
    wrongNavigationPath(ODataHttpMethod.PATCH, UriType.URI7A, HttpStatusCodes.BAD_REQUEST);
    wrongNavigationPath(ODataHttpMethod.DELETE, UriType.URI7A, HttpStatusCodes.BAD_REQUEST);

    wrongNavigationPath(ODataHttpMethod.POST, UriType.URI6B, HttpStatusCodes.BAD_REQUEST);

    wrongNavigationPath(ODataHttpMethod.POST, UriType.URI7B, HttpStatusCodes.BAD_REQUEST);

    wrongNavigationPath(ODataHttpMethod.PUT, UriType.URI17, HttpStatusCodes.BAD_REQUEST);
    wrongNavigationPath(ODataHttpMethod.DELETE, UriType.URI17, HttpStatusCodes.BAD_REQUEST);
  }

  @Test
  public void wrongRequestContentType() throws Exception {
    wrongRequestContentType(ODataHttpMethod.PUT, UriType.URI2, ContentType.APPLICATION_ATOM_SVC);
    wrongRequestContentType(ODataHttpMethod.PUT, UriType.URI2, ContentType.APPLICATION_ATOM_SVC_CS_UTF_8);
    wrongRequestContentType(ODataHttpMethod.PUT, UriType.URI2, ContentType.APPLICATION_ATOM_SVC);
    wrongRequestContentType(ODataHttpMethod.PUT, UriType.URI2, ContentType.APPLICATION_ATOM_SVC_CS_UTF_8);

    ODataHttpMethod[] methodsToTest = { ODataHttpMethod.PUT, ODataHttpMethod.PATCH, ODataHttpMethod.MERGE };

    for (ODataHttpMethod oDataHttpMethod : methodsToTest) {
      wrongRequestContentType(oDataHttpMethod, UriType.URI2, ContentType.create("image/jpeg"));

      wrongRequestContentType(oDataHttpMethod, UriType.URI3, ContentType.TEXT_PLAIN);
      wrongRequestContentType(oDataHttpMethod, UriType.URI4, false, ContentType.TEXT_PLAIN);

      wrongRequestContentType(oDataHttpMethod, UriType.URI5, true, ContentType.APPLICATION_ATOM_SVC);
      wrongRequestContentType(oDataHttpMethod, UriType.URI5, true, ContentType.APPLICATION_ATOM_SVC_CS_UTF_8);
      wrongRequestContentType(oDataHttpMethod, UriType.URI5, true, ContentType.APPLICATION_XML);
      wrongRequestContentType(oDataHttpMethod, UriType.URI5, true, ContentType.APPLICATION_XML_CS_UTF_8);
      wrongRequestContentType(oDataHttpMethod, UriType.URI5, true, ContentType.APPLICATION_ATOM_XML);
      wrongRequestContentType(oDataHttpMethod, UriType.URI5, true, ContentType.APPLICATION_ATOM_XML_CS_UTF_8);
      wrongRequestContentType(oDataHttpMethod, UriType.URI5, true, ContentType.APPLICATION_JSON);
      wrongRequestContentType(oDataHttpMethod, UriType.URI5, true, ContentType.APPLICATION_JSON_CS_UTF_8);
      wrongRequestContentType(oDataHttpMethod, UriType.URI5, true, ContentType.create("image/jpeg"));

      wrongRequestContentType(oDataHttpMethod, UriType.URI6A, ContentType.APPLICATION_ATOM_SVC);

      wrongRequestContentType(oDataHttpMethod, UriType.URI7A, ContentType.APPLICATION_ATOM_SVC);
    }

    EdmEntityType entityType = edm.getDefaultEntityContainer().getEntitySet("Employees").getEntityType();
    when(entityType.hasStream()).thenReturn(false);
    wrongRequestContentType(ODataHttpMethod.POST, UriType.URI1, ContentType.APPLICATION_ATOM_SVC);
    wrongRequestContentType(ODataHttpMethod.POST, UriType.URI1, ContentType.APPLICATION_ATOM_SVC_CS_UTF_8);
    wrongRequestContentType(ODataHttpMethod.POST, UriType.URI6B, ContentType.APPLICATION_ATOM_SVC);
    wrongRequestContentType(ODataHttpMethod.POST, UriType.URI7B, ContentType.APPLICATION_ATOM_SVC);
  }

  @Test
  public void unsupportedRequestContentTypeNoneMediaResource() throws Exception {
    EdmEntityType entityType = edm.getDefaultEntityContainer().getEntitySet("Employees").getEntityType();
    when(entityType.hasStream()).thenReturn(false);

    unsupportedRequestContentType(ODataHttpMethod.POST, UriType.URI1, false, "application/octet-stream");
  }

  @Test
  public void notAllowedMethodRequests() throws Exception {
    ODataResponse response = executeRequest(ODataHttpMethod.PUT, mockPathSegments(UriType.URI1, false, false), null, "application/xml");
    assertNotNull(response);
    assertEquals(HttpStatusCodes.METHOD_NOT_ALLOWED, response.getStatus());
  }

  @Test
  public void invalidRequestDollarFormatSyntax() throws Exception {
    invalidRequestDollarFormat(ODataHttpMethod.GET, UriType.URI17, true, "xml");
    invalidRequestDollarFormat(ODataHttpMethod.GET, UriType.URI17, true, "atom+xml");
    invalidRequestDollarFormat(ODataHttpMethod.GET, UriType.URI17, true, "json");
  }
}
