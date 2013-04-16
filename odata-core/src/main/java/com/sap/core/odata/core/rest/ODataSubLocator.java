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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;

import com.sap.core.odata.api.ODataService;
import com.sap.core.odata.api.ODataServiceFactory;
import com.sap.core.odata.api.ODataServiceVersion;
import com.sap.core.odata.api.commons.ODataHttpHeaders;
import com.sap.core.odata.api.exception.ODataBadRequestException;
import com.sap.core.odata.api.exception.ODataException;
import com.sap.core.odata.api.exception.ODataMessageException;
import com.sap.core.odata.api.exception.ODataMethodNotAllowedException;
import com.sap.core.odata.api.exception.ODataNotAcceptableException;
import com.sap.core.odata.api.exception.ODataNotFoundException;
import com.sap.core.odata.api.processor.ODataProcessor;
import com.sap.core.odata.api.processor.ODataResponse;
import com.sap.core.odata.api.uri.PathInfo;
import com.sap.core.odata.api.uri.PathSegment;
import com.sap.core.odata.core.Dispatcher;
import com.sap.core.odata.core.ODataContextImpl;
import com.sap.core.odata.core.ODataPathSegmentImpl;
import com.sap.core.odata.core.PathInfoImpl;
import com.sap.core.odata.core.commons.ContentType;
import com.sap.core.odata.core.commons.ContentType.ODataFormat;
import com.sap.core.odata.core.commons.ODataHttpMethod;
import com.sap.core.odata.core.uri.UriInfoImpl;
import com.sap.core.odata.core.uri.UriParserImpl;
import com.sap.core.odata.core.uri.UriType;

/**
 * @author SAP AG
 */
public final class ODataSubLocator implements ODataLocator {

  private static final String DEFAULT_CHARSET = "utf-8";

  private ODataService service;

  private Dispatcher dispatcher;

  private UriParserImpl uriParser;

  private final ODataContextImpl context = new ODataContextImpl();

  private Map<String, String> queryParameters;

  private List<ContentType> acceptHeaderContentTypes;

  private InputStream requestContent;

  private String requestContentType;

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
  public Response handlePost(@HeaderParam("X-HTTP-Method") String xHttpMethod) throws ODataException {
    Response response;

    if (xHttpMethod == null) {
      response = handleHttpMethod(ODataHttpMethod.POST);
    } else {
      /* tunneling */
      if ("MERGE".equals(xHttpMethod)) {
        response = handleMerge();
      } else if ("PATCH".equals(xHttpMethod)) {
        response = handlePatch();
      } else if ("DELETE".equals(xHttpMethod)) {
        response = handleDelete();
      } else if ("PUT".equals(xHttpMethod)) {
        response = handlePut();
      } else {
        throw new ODataMethodNotAllowedException(ODataMethodNotAllowedException.TUNNELING);
      }
    }
    return response;
  }

  @OPTIONS
  public Response handleOptions() throws ODataException {
    throw new ODataMethodNotAllowedException(ODataMessageException.COMMON);
  }

  @HEAD
  public Response handleHead() throws ODataException {
    throw new ODataMethodNotAllowedException(ODataMessageException.COMMON);
  }

  private Response handleHttpMethod(final ODataHttpMethod method) throws ODataException {
    final String serverDataServiceVersion = getServerDataServiceVersion();
    validateDataServiceVersion(serverDataServiceVersion);
    final List<PathSegment> pathSegments = context.getPathInfo().getODataSegments();
    final UriInfoImpl uriInfo = (UriInfoImpl) uriParser.parse(pathSegments, queryParameters);
    final String contentType = doContentNegotiation(uriInfo);

    final ODataResponse odataResponse = dispatcher.dispatch(method, uriInfo, requestContent, requestContentType, contentType);
    final Response response = convertResponse(odataResponse, serverDataServiceVersion);

    return response;
  }

  private String doContentNegotiation(final UriInfoImpl uriInfo) throws ODataException {
    ContentType contentType;
    if (uriInfo.getFormat() == null) {
      contentType = doContentNegotiationForAcceptHeader(uriInfo);
    } else {
      contentType = doContentNegotiationForFormat(uriInfo);
    }

    if (contentType.getODataFormat() == ODataFormat.CUSTOM) {
      return contentType.getType();
    }
    return contentType.toContentTypeString();
  }

  private ContentType doContentNegotiationForFormat(final UriInfoImpl uriInfo) throws ODataException {
    ContentType formatContentType = mapFormat(uriInfo);
    formatContentType = ensureCharsetIsSet(formatContentType);

    final Class<? extends ODataProcessor> processorFeature = dispatcher.mapUriTypeToProcessorFeature(uriInfo);
    final List<ContentType> supportedContentTypes = getSupportedContentTypes(processorFeature);
    for (final ContentType contentType : supportedContentTypes) {
      if (contentType.equals(formatContentType)) {
        return formatContentType;
      }
    }

    throw new ODataNotAcceptableException(ODataNotAcceptableException.NOT_SUPPORTED_CONTENT_TYPE.addContent(uriInfo.getFormat()));
  }

  private ContentType ensureCharsetIsSet(ContentType contentType) {
    if (isContentTypeODataTextRelated(contentType)) {
      if (!contentType.getParameters().containsKey(ContentType.PARAMETER_CHARSET)) {
        contentType = ContentType.create(contentType, ContentType.PARAMETER_CHARSET, DEFAULT_CHARSET);
      }
    }
    return contentType;
  }

  private boolean isContentTypeODataTextRelated(ContentType contentType) {
    return contentType != null
        && (contentType.equals(ContentType.TEXT_PLAIN)
            || contentType.getODataFormat() == ODataFormat.XML
            || contentType.getODataFormat() == ODataFormat.ATOM
            || contentType.getODataFormat() == ODataFormat.JSON);
  }

  private ContentType mapFormat(final UriInfoImpl uriInfo) {
    final String format = uriInfo.getFormat();
    if ("xml".equals(format)) {
      return ContentType.APPLICATION_XML;
    } else if ("atom".equals(format)) {
      if (uriInfo.getUriType() == UriType.URI0) {
        // special handling for serviceDocument uris (UriType.URI0)
        return ContentType.APPLICATION_ATOM_SVC;
      }
      return ContentType.APPLICATION_ATOM_XML;
    } else if ("json".equals(format)) {
      return ContentType.APPLICATION_JSON;
    }

    return ContentType.create(format);
  }

  private ContentType doContentNegotiationForAcceptHeader(final UriInfoImpl uriInfo) throws ODataException {
    final Class<? extends ODataProcessor> processorFeature = dispatcher.mapUriTypeToProcessorFeature(uriInfo);
    final List<ContentType> supportedContentTypes = getSupportedContentTypes(processorFeature);
    return contentNegotiation(acceptHeaderContentTypes, supportedContentTypes);
  }

  private List<ContentType> getSupportedContentTypes(Class<? extends ODataProcessor> processorFeature) throws ODataException {
    final List<ContentType> resultContentTypes = new ArrayList<ContentType>();
    for (final String contentType : service.getSupportedContentTypes(processorFeature)) {
      resultContentTypes.add(ContentType.create(contentType));
    }

    return resultContentTypes;
  }

  ContentType contentNegotiation(List<ContentType> acceptedContentTypes, List<ContentType> supportedContentTypes) throws ODataException {
    final Set<ContentType> setSupported = new HashSet<ContentType>(supportedContentTypes);

    if (acceptedContentTypes.isEmpty()) {
      if (!setSupported.isEmpty()) {
        return supportedContentTypes.get(0);
      }
    } else {
      for (ContentType contentType : acceptedContentTypes) {
        contentType = ensureCharsetIsSet(contentType);
        final ContentType match = contentType.match(supportedContentTypes);
        if (match != null) {
          return match;
        }
      }
    }

    throw new ODataNotAcceptableException(ODataNotAcceptableException.NOT_SUPPORTED_CONTENT_TYPE.addContent(acceptedContentTypes.toString()));
  }

  public void initialize(InitParameter param) throws ODataException {
    fillRequestHeader(param.httpHeaders);
    context.setUriInfo(buildODataUriInfo(param));

    queryParameters = convertToSinglevaluedMap(param.getUriInfo().getQueryParameters());

    acceptHeaderContentTypes = convertMediaTypes(param.httpHeaders.getAcceptableMediaTypes());
    requestContent = contentAsStream(extractRequestContent(param));
    requestContentType = extractRequestContentType(param);
    service = param.getServiceFactory().createService(context);
    context.setService(service);
    service.getProcessor().setContext(context);

    uriParser = new UriParserImpl(service.getEntityDataModel());
    dispatcher = new Dispatcher(service);
  }

  String getServerDataServiceVersion() throws ODataException {
    String serverDataServiceVersion = ODataServiceVersion.V20;
    if (service.getVersion() != null) {
      serverDataServiceVersion = service.getVersion();
    }
    return serverDataServiceVersion;
  }

  private void validateDataServiceVersion(String serverDataServiceVersion) throws ODataException {
    final String requestDataServiceVersion = context.getHttpRequestHeader(ODataHttpHeaders.DATASERVICEVERSION);
    if (requestDataServiceVersion != null) {
      try {
        final boolean isValid = ODataServiceVersion.validateDataServiceVersion(requestDataServiceVersion);
        if (!isValid || ODataServiceVersion.isBiggerThan(requestDataServiceVersion, serverDataServiceVersion)) {
          throw new ODataBadRequestException(ODataBadRequestException.VERSIONERROR.addContent(requestDataServiceVersion.toString()));
        }
      } catch (final IllegalArgumentException e) {
        throw new ODataBadRequestException(ODataBadRequestException.PARSEVERSIONERROR.addContent(requestDataServiceVersion), e);
      }
    }
  }

  private String extractRequestContentType(InitParameter param) {
    final MediaType requestMediaType = param.httpHeaders.getMediaType();
    return requestMediaType == null ? null : requestMediaType.toString();
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

  private List<ContentType> convertMediaTypes(List<MediaType> acceptableMediaTypes) {
    final List<ContentType> mediaTypes = new ArrayList<ContentType>();

    for (final MediaType x : acceptableMediaTypes) {
      mediaTypes.add(ContentType.create(x.getType(), x.getSubtype(), x.getParameters()));
    }

    return mediaTypes;
  }

  private void fillRequestHeader(HttpHeaders httpHeaders) {
    final MultivaluedMap<String, String> headers = httpHeaders.getRequestHeaders();

    for (final String key : headers.keySet()) {
      final String value = httpHeaders.getHeaderString(key);
      context.setHttpRequestHeader(key, value);
    }
  }

  private PathInfo buildODataUriInfo(InitParameter param) throws ODataException {
    final PathInfoImpl pathInfo = new PathInfoImpl();

    splitPath(pathInfo, param);

    final URI uri = buildBaseUri(param.getUriInfo(), pathInfo.getPrecedingSegments());
    pathInfo.setServiceRoot(uri);

    context.setUriInfo(pathInfo);

    return pathInfo;
  }

  private void splitPath(PathInfoImpl pathInfo, InitParameter param) throws ODataException {
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

    // post condition: we do not allow matrix parameters in OData path segments
    for (final javax.ws.rs.core.PathSegment ps : pathSegments) {
      if ((ps.getMatrixParameters() != null) && !ps.getMatrixParameters().isEmpty()) {
        throw new ODataNotFoundException(ODataNotFoundException.MATRIX.addContent(ps.getMatrixParameters().keySet(), ps.getPath()));
      }
    }

    pathInfo.setODataPathSegment(convertPathSegmentList(pathSegments));
    pathInfo.setPrecedingPathSegment(convertPathSegmentList(precedingPathSegments));
  }

  private URI buildBaseUri(javax.ws.rs.core.UriInfo uriInfo, List<PathSegment> precedingPathSegments) throws ODataException {
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

  public List<PathSegment> convertPathSegmentList(List<javax.ws.rs.core.PathSegment> pathSegments) {
    final ArrayList<PathSegment> converted = new ArrayList<PathSegment>();

    for (final javax.ws.rs.core.PathSegment pathSegment : pathSegments) {
      final PathSegment segment = new ODataPathSegmentImpl(pathSegment.getPath(), pathSegment.getMatrixParameters());
      converted.add(segment);
    }
    return converted;
  }

  private Map<String, String> convertToSinglevaluedMap(MultivaluedMap<String, String> multi) {
    final Map<String, String> single = new HashMap<String, String>();

    for (final String key : multi.keySet()) {
      final String value = multi.getFirst(key);
      single.put(key, value);
    }

    return single;
  }

  private Response convertResponse(final ODataResponse odataResponse, final String version) {
    ResponseBuilder responseBuilder = Response.noContent()
        .status(odataResponse.getStatus().getStatusCode())
        .entity(odataResponse.getEntity());

    for (final String name : odataResponse.getHeaderNames()) {
      responseBuilder = responseBuilder.header(name, odataResponse.getHeader(name));
    }

    if (!odataResponse.containsHeader(ODataHttpHeaders.DATASERVICEVERSION)) {
      responseBuilder = responseBuilder.header(ODataHttpHeaders.DATASERVICEVERSION, version);
    }

    final String eTag = odataResponse.getETag();
    if (eTag != null) {
      responseBuilder.header(HttpHeaders.ETAG, eTag);
    }

    return responseBuilder.build();
  }

  public class InitParameter {

    private List<javax.ws.rs.core.PathSegment> pathSegments;
    private HttpHeaders httpHeaders;
    private javax.ws.rs.core.UriInfo uriInfo;
    private Request request;
    private int pathSplit;
    private ODataServiceFactory serviceFactory;
    private HttpServletRequest servletRequest;

    public ODataServiceFactory getServiceFactory() {
      return serviceFactory;
    }

    public void setServiceFactory(ODataServiceFactory serviceFactory) {
      this.serviceFactory = serviceFactory;
    }

    public List<javax.ws.rs.core.PathSegment> getPathSegments() {
      return pathSegments;
    }

    public void setPathSegments(List<javax.ws.rs.core.PathSegment> pathSegments) {
      this.pathSegments = pathSegments;
    }

    public HttpHeaders getHttpHeaders() {
      return httpHeaders;
    }

    public void setHttpHeaders(HttpHeaders httpHeaders) {
      this.httpHeaders = httpHeaders;
    }

    public javax.ws.rs.core.UriInfo getUriInfo() {
      return uriInfo;
    }

    public void setUriInfo(javax.ws.rs.core.UriInfo uriInfo) {
      this.uriInfo = uriInfo;
    }

    public Request getRequest() {
      return request;
    }

    public void setRequest(Request request) {
      this.request = request;
    }

    public int getPathSplit() {
      return pathSplit;
    }

    public void setPathSplit(int pathSplit) {
      this.pathSplit = pathSplit;
    }

    public void setServletRequest(HttpServletRequest servletRequest) {
      this.servletRequest = servletRequest;
    }

    public HttpServletRequest getServletRequest() {
      return servletRequest;
    }
  }

}
