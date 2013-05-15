package com.sap.core.odata.core.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.sap.core.odata.api.ODataServiceFactory;
import com.sap.core.odata.api.commons.HttpHeaders;
import com.sap.core.odata.api.commons.ODataHttpMethod;
import com.sap.core.odata.api.exception.ODataBadRequestException;
import com.sap.core.odata.api.exception.ODataException;
import com.sap.core.odata.api.exception.ODataNotFoundException;
import com.sap.core.odata.api.exception.ODataNotImplementedException;
import com.sap.core.odata.api.exception.ODataUnsupportedMediaTypeException;
import com.sap.core.odata.api.processor.ODataResponse;
import com.sap.core.odata.api.uri.PathSegment;
import com.sap.core.odata.core.ODataPathSegmentImpl;
import com.sap.core.odata.core.ODataRequestImpl;
import com.sap.core.odata.core.PathInfoImpl;
import com.sap.core.odata.core.commons.ContentType;
import com.sap.core.odata.core.commons.Decoder;

/**
 * @author SAP AG
 */
public final class ODataSubLocator implements ODataLocator {

  private ODataRequestImpl request;

  private ODataRequestHandler requestHandler;

  @GET
  public Response handleGet() throws ODataException {
    return handleHttpMethod(ODataHttpMethod.GET);
  }

  @PUT
  public Response handlePut() throws ODataException {
    return handleHttpMethod(ODataHttpMethod.PUT);
  }

  @PATCH
  public Response handlePatch() throws ODataException {
    return handleHttpMethod(ODataHttpMethod.PATCH);
  }

  @MERGE
  public Response handleMerge() throws ODataException {
    return handleHttpMethod(ODataHttpMethod.MERGE);
  }

  @DELETE
  public Response handleDelete() throws ODataException {
    return handleHttpMethod(ODataHttpMethod.DELETE);
  }

  @POST
  public Response handlePost(@HeaderParam("X-HTTP-Method") final String xHttpMethod) throws ODataException {
    Response response;

    if (xHttpMethod == null) {
      response = handleHttpMethod(ODataHttpMethod.POST);
    } else {
      /* tunneling */
      if ("MERGE".equals(xHttpMethod)) {
        response = handleHttpMethod(ODataHttpMethod.MERGE);
      } else if ("PATCH".equals(xHttpMethod)) {
        response = handleHttpMethod(ODataHttpMethod.PATCH);
      } else if (HttpMethod.DELETE.equals(xHttpMethod)) {
        response = handleHttpMethod(ODataHttpMethod.DELETE);
      } else if (HttpMethod.PUT.equals(xHttpMethod)) {
        response = handleHttpMethod(ODataHttpMethod.PUT);
      } else if (HttpMethod.GET.equals(xHttpMethod)) {
        response = handleHttpMethod(ODataHttpMethod.GET);
      } else if (HttpMethod.POST.equals(xHttpMethod)) {
        response = handleHttpMethod(ODataHttpMethod.POST);
      } else if (HttpMethod.HEAD.equals(xHttpMethod)) {
        response = handleHead();
      } else if (HttpMethod.OPTIONS.equals(xHttpMethod)) {
        response = handleOptions();
      } else {
        // RFC 2616, 5.1.1: "An origin server SHOULD return the status code [...]
        // 501 (Not Implemented) if the method is unrecognized [...] by the origin server."
        throw new ODataNotImplementedException(ODataNotImplementedException.TUNNELING);
      }
    }
    return response;
  }

  @OPTIONS
  public Response handleOptions() throws ODataException {
    // RFC 2616, 5.1.1: "An origin server SHOULD return the status code [...]
    // 501 (Not Implemented) if the method is unrecognized or not implemented
    // by the origin server."
    throw new ODataNotImplementedException(ODataNotImplementedException.COMMON);
  }

  @HEAD
  public Response handleHead() throws ODataException {
    // RFC 2616, 5.1.1: "An origin server SHOULD return the status code [...]
    // 501 (Not Implemented) if the method is unrecognized or not implemented
    // by the origin server."
    throw new ODataNotImplementedException(ODataNotImplementedException.COMMON);
  }

  private Response handleHttpMethod(final ODataHttpMethod method) throws ODataException {
    request.setMethod(method);
    final ODataResponse odataResponse = requestHandler.handleHttpMethod(request);

    final Response response = Util.convertResponse(odataResponse);

    return response;
  }

  public void initialize(final InitParameter param) throws ODataException {
    requestHandler = new ODataRequestHandler(param.getServiceFactory());

    request = new ODataRequestImpl();

    request.setHeaders(extractRequestHeaders(param.getHttpHeaders()));
    request.setPathInfo(buildODataPathInfo(param));
    request.setBody(contentAsStream(extractRequestContent(param)));

    requestHandler.setQueryParameters(convertToSinglevaluedMap(param.getUriInfo().getQueryParameters()));
    requestHandler.setAcceptHeaderContentTypes(extractAcceptHeaders(param));
    requestHandler.setRequestContentType(extractRequestContentType(param));
    requestHandler.setAcceptableLanguages(param.httpHeaders.getAcceptableLanguages());

  }

  private ContentType extractRequestContentType(final InitParameter param) throws ODataUnsupportedMediaTypeException {
    final MediaType requestMediaType = param.getHttpHeaders().getMediaType();
    if (requestMediaType == null) {
      return null;
    } else if (requestMediaType == MediaType.WILDCARD_TYPE
        || requestMediaType == MediaType.TEXT_PLAIN_TYPE
        || requestMediaType == MediaType.APPLICATION_XML_TYPE) {
      // The JAX-RS implementation of media-type parsing decided to
      // return one of the known constants for the special case of
      // an invalid content type that has no subtype;
      // at least CXF 2.7 is known to do that.
      // The "==" comparison above is optimized for this case.
      throw new ODataUnsupportedMediaTypeException(ODataUnsupportedMediaTypeException.NOT_SUPPORTED.addContent(param.getHttpHeaders().getRequestHeader(HttpHeaders.CONTENT_TYPE).get(0)));
    } else {
      try {
        return ContentType.create(requestMediaType.toString());
      } catch (IllegalArgumentException e) {
        throw new ODataUnsupportedMediaTypeException(ODataUnsupportedMediaTypeException.NOT_SUPPORTED.addContent(requestMediaType.toString()), e);
      }
    }
  }

  /**
   * Extracts the request content from the servlet as input stream.
   * @param param initialization parameters
   * @return the request content as input stream
   * @throws ODataException
   */
  private ServletInputStream extractRequestContent(final InitParameter param) throws ODataException {
    try {
      return param.getServletRequest().getInputStream();
    } catch (final IOException e) {
      throw new ODataException("Error getting request content as ServletInputStream.", e);
    }
  }

  private static <T> InputStream contentAsStream(final T content) throws ODataException {
    if (content == null) {
      throw new ODataBadRequestException(ODataBadRequestException.COMMON);
    }

    InputStream inputStream;
    if (content instanceof InputStream) {
      inputStream = (InputStream) content;
    } else if (content instanceof String) {
      try {
        inputStream = new ByteArrayInputStream(((String) content).getBytes("UTF-8"));
      } catch (final UnsupportedEncodingException e) {
        throw new ODataBadRequestException(ODataBadRequestException.COMMON, e);
      }
    } else {
      throw new ODataBadRequestException(ODataBadRequestException.COMMON);
    }
    return inputStream;
  }

  private List<String> extractAcceptHeaders(final InitParameter param) throws ODataBadRequestException {
    final List<MediaType> acceptableMediaTypes = param.getHttpHeaders().getAcceptableMediaTypes();
    final List<String> mediaTypes = new ArrayList<String>();

    for (final MediaType mediaType : acceptableMediaTypes) {
      mediaTypes.add(mediaType.toString());
    }

    return mediaTypes;
  }

  private Map<String, String> extractRequestHeaders(final javax.ws.rs.core.HttpHeaders httpHeaders) {
    final MultivaluedMap<String, String> headers = httpHeaders.getRequestHeaders();
    Map<String, String> headerMap = new HashMap<String, String>();

    for (final String key : headers.keySet()) {
      final String value = httpHeaders.getHeaderString(key);
      headerMap.put(key, value);
    }
    return headerMap;
  }

  private PathInfoImpl buildODataPathInfo(final InitParameter param) throws ODataException {

    final PathInfoImpl pathInfo = splitPath(param);

    final URI uri = buildBaseUri(param.getUriInfo(), pathInfo.getPrecedingSegments());
    pathInfo.setServiceRoot(uri);

    return pathInfo;
  }

  private PathInfoImpl splitPath(final InitParameter param) throws ODataException {
    final PathInfoImpl pathInfo = new PathInfoImpl();

    List<javax.ws.rs.core.PathSegment> precedingPathSegments;
    List<javax.ws.rs.core.PathSegment> pathSegments;

    if (param.getPathSplit() == 0) {
      precedingPathSegments = Collections.emptyList();
      pathSegments = param.getPathSegments();
    } else {
      if (param.getPathSegments().size() < param.getPathSplit()) {
        throw new ODataBadRequestException(ODataBadRequestException.URLTOOSHORT);
      }

      precedingPathSegments = param.getPathSegments().subList(0, param.getPathSplit());
      final int pathSegmentCount = param.getPathSegments().size();
      pathSegments = param.getPathSegments().subList(param.getPathSplit(), pathSegmentCount);
    }

    // Percent-decode only the preceding path segments.
    // The OData path segments are decoded during URI parsing.
    pathInfo.setPrecedingPathSegment(convertPathSegmentList(precedingPathSegments));

    List<PathSegment> odataSegments = new ArrayList<PathSegment>();
    for (final javax.ws.rs.core.PathSegment segment : pathSegments) {
      if (segment.getMatrixParameters() == null || segment.getMatrixParameters().isEmpty()) {
        odataSegments.add(new ODataPathSegmentImpl(segment.getPath(), null));
      } else {
        // post condition: we do not allow matrix parameters in OData path segments
        throw new ODataNotFoundException(ODataNotFoundException.MATRIX.addContent(segment.getMatrixParameters().keySet(), segment.getPath()));
      }
    }
    pathInfo.setODataPathSegment(odataSegments);

    return pathInfo;
  }

  private URI buildBaseUri(final javax.ws.rs.core.UriInfo uriInfo, final List<PathSegment> precedingPathSegments) throws ODataException {
    try {
      UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();
      for (final PathSegment ps : precedingPathSegments) {
        uriBuilder = uriBuilder.path(ps.getPath());
        for (final String key : ps.getMatrixParameters().keySet()) {
          final Object[] v = ps.getMatrixParameters().get(key).toArray();
          uriBuilder = uriBuilder.matrixParam(key, v);
        }
      }

      String uriString = uriBuilder.build().toString();
      if (!uriString.endsWith("/")) {
        uriString = uriString + "/";
      }

      return new URI(uriString);
    } catch (final URISyntaxException e) {
      throw new ODataException(e);
    }
  }

  public List<PathSegment> convertPathSegmentList(final List<javax.ws.rs.core.PathSegment> pathSegments) {
    ArrayList<PathSegment> converted = new ArrayList<PathSegment>();
    for (final javax.ws.rs.core.PathSegment pathSegment : pathSegments) {
      final PathSegment segment = new ODataPathSegmentImpl(Decoder.decode(pathSegment.getPath()), pathSegment.getMatrixParameters());
      converted.add(segment);
    }
    return converted;
  }

  private Map<String, String> convertToSinglevaluedMap(final MultivaluedMap<String, String> multi) {
    final Map<String, String> single = new HashMap<String, String>();

    for (final String key : multi.keySet()) {
      final String value = multi.getFirst(key);
      single.put(key, value);
    }

    return single;
  }

  public class InitParameter {

    private List<javax.ws.rs.core.PathSegment> pathSegments;
    private javax.ws.rs.core.HttpHeaders httpHeaders;
    private javax.ws.rs.core.UriInfo uriInfo;
    private Request request;
    private int pathSplit;
    private ODataServiceFactory serviceFactory;
    private HttpServletRequest servletRequest;

    public ODataServiceFactory getServiceFactory() {
      return serviceFactory;
    }

    public void setServiceFactory(final ODataServiceFactory serviceFactory) {
      this.serviceFactory = serviceFactory;
    }

    public List<javax.ws.rs.core.PathSegment> getPathSegments() {
      return pathSegments;
    }

    public void setPathSegments(final List<javax.ws.rs.core.PathSegment> pathSegments) {
      this.pathSegments = pathSegments;
    }

    public javax.ws.rs.core.HttpHeaders getHttpHeaders() {
      return httpHeaders;
    }

    public void setHttpHeaders(final javax.ws.rs.core.HttpHeaders httpHeaders) {
      this.httpHeaders = httpHeaders;
    }

    public javax.ws.rs.core.UriInfo getUriInfo() {
      return uriInfo;
    }

    public void setUriInfo(final javax.ws.rs.core.UriInfo uriInfo) {
      this.uriInfo = uriInfo;
    }

    public Request getRequest() {
      return request;
    }

    public void setRequest(final Request request) {
      this.request = request;
    }

    public int getPathSplit() {
      return pathSplit;
    }

    public void setPathSplit(final int pathSplit) {
      this.pathSplit = pathSplit;
    }

    public void setServletRequest(final HttpServletRequest servletRequest) {
      this.servletRequest = servletRequest;
    }

    public HttpServletRequest getServletRequest() {
      return servletRequest;
    }
  }

}
