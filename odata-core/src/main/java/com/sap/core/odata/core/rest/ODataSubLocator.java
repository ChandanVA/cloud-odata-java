package com.sap.core.odata.core.rest;

import java.io.IOException;
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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import com.sap.core.odata.api.ODataService;
import com.sap.core.odata.api.ODataServiceFactory;
import com.sap.core.odata.api.exception.ODataBadRequestException;
import com.sap.core.odata.api.exception.ODataException;
import com.sap.core.odata.api.exception.ODataMethodNotAllowedException;
import com.sap.core.odata.api.exception.ODataNotAcceptableException;
import com.sap.core.odata.api.exception.ODataNotFoundException;
import com.sap.core.odata.api.processor.ODataResponse;
import com.sap.core.odata.api.processor.feature.ProcessorFeature;
import com.sap.core.odata.api.uri.PathInfo;
import com.sap.core.odata.api.uri.PathSegment;
import com.sap.core.odata.core.Dispatcher;
import com.sap.core.odata.core.ODataContextImpl;
import com.sap.core.odata.core.ODataPathSegmentImpl;
import com.sap.core.odata.core.ODataUriInfoImpl;
import com.sap.core.odata.core.commons.ContentType;
import com.sap.core.odata.core.commons.ODataHttpMethod;
import com.sap.core.odata.core.uri.UriInfoImpl;
import com.sap.core.odata.core.uri.UriParserImpl;

/**
 * @author SAP AG
 */
public final class ODataSubLocator implements ODataLocator {

  private ODataService service;

  private Dispatcher dispatcher;

  private UriParserImpl uriParser;

  private ODataContextImpl context = new ODataContextImpl();

  private Map<String, String> queryParameters;

  private List<ContentType> acceptHeaderContentTypes;

  private ServletInputStream requestContent;

  @GET
  public Response handleGet() throws ODataException {
    List<PathSegment> pathSegments =  context.getPathInfo().getODataSegments();
    UriInfoImpl uriParserResult = (UriInfoImpl)  uriParser.parse(pathSegments,  queryParameters);

    final String contentType = doContentNegotiation(uriParserResult);

    ODataResponse odataResponse = dispatcher.dispatch(ODataHttpMethod.GET, uriParserResult, null, contentType);
    Response response =  convertResponse(odataResponse);

    return response;
  }

  private String doContentNegotiation(UriInfoImpl uriParserResult) throws ODataException {
    String format;
    if (uriParserResult.getFormat() == null) {
      format = doContentNegotiationForAcceptHeader(uriParserResult).toContentTypeString();
    } else {
      format = doContentNegotiationForFormat(uriParserResult);
    }
    return format;
  }

  private String doContentNegotiationForFormat(UriInfoImpl uriParserResult) throws ODataException {
    String format = getFormat(uriParserResult);
    ContentType tmp = ContentType.create(format);
    Class<? extends ProcessorFeature> processorFeature = dispatcher.mapUriTypeToProcessorFeature(uriParserResult);
    List<ContentType> supportedContentTypes = getSupportedContentTypes(processorFeature);
    for (ContentType contentType : supportedContentTypes) {
      if (contentType.equals(tmp)) {
        return format;
      }
    }

    throw new ODataNotAcceptableException(ODataNotAcceptableException.NOT_SUPPORTED_CONTENT_TYPE.addContent(format));
  }

  private String getFormat(UriInfoImpl uriParserResult) {
    String format = uriParserResult.getFormat();
    if ("xml".equals(format)) {
      format = ContentType.APPLICATION_XML.toContentTypeString();
    } else if ("atom".equals(format)) {
      format = ContentType.APPLICATION_ATOM_XML.toContentTypeString();
    } else if ("json".equals(format)) {
      format = ContentType.APPLICATION_JSON.toContentTypeString();
    }
    return format;
  }

  private ContentType doContentNegotiationForAcceptHeader(UriInfoImpl uriParserResult) throws ODataException {
    Class<? extends ProcessorFeature> processorFeature = dispatcher.mapUriTypeToProcessorFeature(uriParserResult);
    List<ContentType> supportedContentTypes = getSupportedContentTypes(processorFeature);
    ContentType contentType = contentNegotiation(acceptHeaderContentTypes, supportedContentTypes);
    return contentType;
  }

  private List<ContentType> getSupportedContentTypes(Class<? extends ProcessorFeature> processorFeature) throws ODataException {
    List<ContentType> resultContentTypes = new ArrayList<ContentType>();
    for (String contentType : service.getSupportedContentTypes(processorFeature))
      resultContentTypes.add(ContentType.create(contentType));

    return resultContentTypes;
  }

  ContentType contentNegotiation(List<ContentType> contentTypes, List<ContentType> supportedContentTypes) throws ODataException {
    Set<ContentType> setSupported = new HashSet<ContentType>(supportedContentTypes);

    if (contentTypes.isEmpty()) {
      if (!setSupported.isEmpty()) {
        return supportedContentTypes.get(0);
      }
    } else {
      for (ContentType ct : contentTypes) {
        ContentType match = ct.match(supportedContentTypes);
        if (match != null)
          return match;
      }
    }

    throw new ODataNotAcceptableException(ODataNotAcceptableException.NOT_SUPPORTED_CONTENT_TYPE.addContent(contentTypes.toString()));
  }

  @POST
  public Response handlePost(@HeaderParam("X-HTTP-Method") String xmethod) throws ODataException {
    Response response;

    /* tunneling */
    if (xmethod == null) {
      response = handlePost();
    } else if ("MERGE".equals(xmethod)) {
      response =  handleMerge();
    } else if ("PATCH".equals(xmethod)) {
      response =  handlePatch();
    } else if ("DELETE".equals(xmethod)) {
      response =  handleDelete();
    } else {
      response = Response.status(Status.METHOD_NOT_ALLOWED).build();
    }

    return response;
  }

  public Response handlePost() throws ODataException {
    List<PathSegment> pathSegments = context.getPathInfo().getODataSegments();
    UriInfoImpl uriParserResult = (UriInfoImpl) uriParser.parse(pathSegments, queryParameters);

    final String contentType = doContentNegotiation(uriParserResult);

    ODataResponse odataResponse = dispatcher.dispatch(ODataHttpMethod.POST, uriParserResult, requestContent, contentType);
    Response response = convertResponse(odataResponse);

    return response;
  }

  @PUT
  public Response handlePut() throws ODataException {
    List<PathSegment> pathSegments = context.getPathInfo().getODataSegments();
    UriInfoImpl uriParserResult = (UriInfoImpl) uriParser.parse(pathSegments, queryParameters);

    final String contentType = doContentNegotiation(uriParserResult);
    
    ODataResponse odataResponse = dispatcher.dispatch(ODataHttpMethod.PUT, uriParserResult, requestContent, contentType);
    Response response = convertResponse(odataResponse);
    return response;
  }

  @PATCH
  public Response handlePatch() throws ODataException {
    return handlePut();
  }

  @MERGE
  public Response handleMerge() throws ODataException {
    return handlePatch();
  }

  @DELETE
  public Response handleDelete() throws ODataException {
    final List<PathSegment> pathSegments = context.getPathInfo().getODataSegments();
    final UriInfoImpl uriParserResult = (UriInfoImpl) uriParser.parse(pathSegments, queryParameters);

    final ODataResponse odataResponse = dispatcher.dispatch(ODataHttpMethod.DELETE, uriParserResult, null, null);
    return convertResponse(odataResponse);
  }

  @OPTIONS
  public Response handleOptions() throws ODataException {
    throw new ODataMethodNotAllowedException(ODataMethodNotAllowedException.COMMON);
  }

  @HEAD
  public Response handleHead() throws ODataException {
    throw new ODataMethodNotAllowedException(ODataMethodNotAllowedException.COMMON);
  }

  public void initialize(InitParameter param) throws ODataException {
    fillRequestHeader(param.httpHeaders);
    context.setUriInfo(buildODataUriInfo(param));

    queryParameters = convertToSinglevaluedMap(param.getUriInfo().getQueryParameters());

    acceptHeaderContentTypes = convertMediaTypes(param.httpHeaders.getAcceptableMediaTypes());
    requestContent = extractRequestContent(param);
    service = param.getServiceFactory().createService(context);
    context.setService(service);
    service.getProcessor().setContext(context);

    uriParser = new UriParserImpl(service.getEntityDataModel());
    dispatcher = new Dispatcher(service);
  }

  /**
   * 
   * @param param
   * @return
   * @throws ODataException
   */
  private ServletInputStream extractRequestContent(InitParameter param) throws ODataException {
    try {
      return param.getServletRequest().getInputStream();
    } catch (IOException e) {
      throw new ODataException("Error getting request content as ServletInputStream.", e);
    }
  }

  private List<ContentType> convertMediaTypes(List<MediaType> acceptableMediaTypes) {
    List<ContentType> mediaTypes = new ArrayList<ContentType>();

    for (MediaType x : acceptableMediaTypes)
      mediaTypes.add(ContentType.create(x.getType(), x.getSubtype(), x.getParameters()));

    return mediaTypes;
  }

  private void fillRequestHeader(HttpHeaders httpHeaders) {
    MultivaluedMap<String, String> headers = httpHeaders.getRequestHeaders();

    for (String key : headers.keySet()) {
      String value = httpHeaders.getHeaderString(key);
      context.setHttpRequestHeader(key, value);
    }
  }

  private PathInfo buildODataUriInfo(InitParameter param) throws ODataException {
    ODataUriInfoImpl odataUriInfo = new ODataUriInfoImpl();

    splitPath(odataUriInfo, param);

    URI uri = buildBaseUri(param.getUriInfo(), odataUriInfo.getPrecedingSegments());
    odataUriInfo.setBaseUri(uri);

    context.setUriInfo(odataUriInfo);

    return odataUriInfo;
  }

  private void splitPath(ODataUriInfoImpl odataUriInfo, InitParameter param) throws ODataException {
    List<javax.ws.rs.core.PathSegment> precedingPathSegments;
    List<javax.ws.rs.core.PathSegment> pathSegments;

    if (param.getPathSplit() == 0) {
      precedingPathSegments = Collections.emptyList();
      pathSegments = param.getPathSegments();
    } else {
      if (param.getPathSegments().size() < param.getPathSplit()) {
        throw new ODataBadRequestException(ODataBadRequestException.URLTOSHORT);
      }

      precedingPathSegments = param.getPathSegments().subList(0, param.getPathSplit());
      int pathSegmentCount = param.getPathSegments().size();
      pathSegments = param.getPathSegments().subList(param.getPathSplit(), pathSegmentCount);
    }

    // post condition: we do not allow matrix parameter in OData path segments
    for (javax.ws.rs.core.PathSegment ps : pathSegments) {
      if (ps.getMatrixParameters() != null && !ps.getMatrixParameters().isEmpty()) {
        throw new ODataNotFoundException(ODataNotFoundException.MATRIX.addContent(ps.getMatrixParameters().keySet(), ps.getPath()));
      }
    }

    odataUriInfo.setODataPathSegment(convertPathSegmentList(pathSegments));
    odataUriInfo.setPrecedingPathSegment(convertPathSegmentList(precedingPathSegments));
  }

  private URI buildBaseUri(javax.ws.rs.core.UriInfo uriInfo, List<PathSegment> precedingPathSegments) throws ODataException {
    try {
      UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();
      for (PathSegment ps : precedingPathSegments) {
        uriBuilder = uriBuilder.path(ps.getPath());
        for (String key : ps.getMatrixParameters().keySet()) {
          Object[] v = ps.getMatrixParameters().get(key).toArray();
          uriBuilder = uriBuilder.matrixParam(key, v);
        }
      }

      String uriString = uriBuilder.build().toString();
      if (!uriString.endsWith("/")) {
        uriString = uriString + "/";
      }

      return new URI(uriString);
    } catch (URISyntaxException e) {
      throw new ODataException(e);
    }
  }

  public List<PathSegment> convertPathSegmentList(List<javax.ws.rs.core.PathSegment> pathSegments) {
    ArrayList<PathSegment> converted = new ArrayList<PathSegment>();

    for (javax.ws.rs.core.PathSegment pathSegment : pathSegments) {
      PathSegment segment = new ODataPathSegmentImpl(pathSegment.getPath(), pathSegment.getMatrixParameters());
      converted.add(segment);
    }
    return converted;
  }

  private Map<String, String> convertToSinglevaluedMap(MultivaluedMap<String, String> multi) {
    Map<String, String> single = new HashMap<String, String>();

    for (String key : multi.keySet()) {
      String value = multi.getFirst(key);
      single.put(key, value);
    }

    return single;
  }

  private Response convertResponse(final ODataResponse odataResponse) {
    ResponseBuilder responseBuilder = Response.noContent()
        .status(odataResponse.getStatus().getStatusCode())
        .entity(odataResponse.getEntity());

    for (final String name : odataResponse.getHeaderNames())
      responseBuilder = responseBuilder.header(name, odataResponse.getHeader(name));

    String eTag = odataResponse.getETag();
    if (eTag != null)
      responseBuilder.header(HttpHeaders.ETAG, eTag);

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
