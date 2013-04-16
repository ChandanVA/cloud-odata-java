package com.sap.core.odata.core.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.core.odata.api.edm.Edm;
import com.sap.core.odata.api.edm.provider.EdmProvider;
import com.sap.core.odata.api.exception.ODataException;
import com.sap.core.odata.api.processor.ODataProcessor;
import com.sap.core.odata.api.processor.ODataResponse;
import com.sap.core.odata.api.service.ODataService;
import com.sap.core.odata.api.service.ODataServiceFactory;
import com.sap.core.odata.core.dispatcher.Dispatcher;
import com.sap.core.odata.core.edm.provider.EdmImplProv;
import com.sap.core.odata.core.enums.ODataHttpMethod;
import com.sap.core.odata.core.service.ODataSingleProcessorService;
import com.sap.core.odata.core.uri.UriParserImpl;
import com.sap.core.odata.core.uri.UriParserResultImpl;

public final class ODataLocatorImpl {

  private static final Logger log = LoggerFactory.getLogger(ODataLocatorImpl.class);

  private ODataService service;

  private Dispatcher dispatcher;

  private UriParserImpl uriParser;

  private ODataContextImpl context;

  private List<String> pathSegments;
  
  private Map<String, String> queryParameters;
  
  @GET
  public Response handleGet() throws ODataException {
    try {
      ODataLocatorImpl.log.debug("+++ ODataSubResource:handleGet()");
      this.context.log();

      UriParserResultImpl uriParserResult = (UriParserResultImpl) this.uriParser.parse(this.pathSegments, this.queryParameters);

      ODataResponse odataResponse = dispatcher.dispatch(ODataHttpMethod.GET, uriParserResult);
      Response response = this.convertResponse(odataResponse);

      return response;
    } catch (ODataException e) {
      throw new RuntimeException(e);
    }
  }

  @POST
  @Produces(MediaType.TEXT_PLAIN)
  public Response handlePost(
      @HeaderParam("X-HTTP-Method") String xmethod
      ) throws ODataException {

    ODataLocatorImpl.log.debug("+++ ODataSubResource:handlePost()");
    Response response;

    /* tunneling */
    if (xmethod == null) {
      this.context.log();
      response = Response.ok().entity("POST: status 200 ok").build();
    } else if ("MERGE".equals(xmethod)) {
      response = this.handleMerge();
    } else if ("PATCH".equals(xmethod)) {
      response = this.handlePatch();
    } else if ("DELETE".equals(xmethod)) {
      response = this.handleDelete();
    } else {
      response = Response.status(405).build(); // method not allowed!
    }

    return response;
  }

  @PUT
  public Response handlePut() throws ODataException {
    ODataLocatorImpl.log.debug("+++ ODataSubResource:handlePut()");
    this.context.log();

    return Response.ok().entity("PUT: status 200 ok").build();
  }

  @PATCH
  public Response handlePatch() throws ODataException {
    ODataLocatorImpl.log.debug("+++ ODataSubResource:handlePatch()");
    this.context.log();

    return Response.ok().entity("PATCH: status 200 ok").build();
  }

  @MERGE
  public Response handleMerge() throws ODataException {
    ODataLocatorImpl.log.debug("+++ ODataSubResource:handleMerge()");
    this.context.log();

    return Response.ok().entity("MERGE: status 200 ok").build();
  }

  @DELETE
  public Response handleDelete() throws ODataException {
    ODataLocatorImpl.log.debug("+++ ODataSubResource:handleDelete()");
    this.context.log();

    return Response.ok().entity("DELETE: status 200 ok").build();
  }

  public void setContext(ODataContextImpl context) {
    this.context = context;
  }

  public void initializeService(ODataServiceFactory serviceFactory, List<PathSegment> odataPathSegments, HttpHeaders httpHeaders, UriInfo uriInfo, Request request) throws ODataException {
    this.context = new ODataContextImpl();

    this.context.putContextObject(httpHeaders.getClass(), httpHeaders);
    this.context.putContextObject(odataPathSegments.getClass(), odataPathSegments);
    this.context.putContextObject(uriInfo.getClass(), uriInfo);
    this.context.putContextObject(request.getClass(), request);

    this.pathSegments = this.getPathSegmentsAsStrings(odataPathSegments);
    this.queryParameters = this.convertToSinglevaluedMap(uriInfo.getQueryParameters());
    
    ODataProcessor processor = serviceFactory.createProcessor();
    processor.setContext(this.context);

    EdmProvider provider = serviceFactory.createProvider();
    Edm edm = new EdmImplProv(provider);

    this.service = new ODataSingleProcessorService(processor, edm);
    this.context.putContextObject(ODataService.class, this.service);

    this.uriParser = new UriParserImpl(service.getEntityDataModel());
    this.dispatcher = new Dispatcher(this.service);
  }

  public List<String> getPathSegmentsAsStrings(List<PathSegment> pathSegments) {
    ArrayList<String> pathSegmentsAsString = new ArrayList<String>();

    for (PathSegment pathSegment : pathSegments) {
      pathSegmentsAsString.add(pathSegment.getPath());
    }
    return pathSegmentsAsString;
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
    ResponseBuilder responseBuilder = Response.noContent();

    responseBuilder = responseBuilder.status(odataResponse.getStatus().getStatusCode());
    responseBuilder = responseBuilder.entity(odataResponse.getEntity());

    for (String name : odataResponse.getHeaderNames())
      responseBuilder = responseBuilder.header(name, odataResponse.getHeader(name));

    String eTag = odataResponse.getETag();
    if (eTag != null) {
      responseBuilder.header(HttpHeaders.ETAG, eTag);
    }

    return responseBuilder.build();
  }

}
