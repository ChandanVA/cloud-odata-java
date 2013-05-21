package com.sap.core.odata.core;

import java.util.List;

import com.sap.core.odata.api.ODataService;
import com.sap.core.odata.api.ODataServiceFactory;
import com.sap.core.odata.api.ODataServiceVersion;
import com.sap.core.odata.api.commons.HttpHeaders;
import com.sap.core.odata.api.commons.HttpStatusCodes;
import com.sap.core.odata.api.commons.ODataHttpHeaders;
import com.sap.core.odata.api.commons.ODataHttpMethod;
import com.sap.core.odata.api.exception.ODataBadRequestException;
import com.sap.core.odata.api.exception.ODataException;
import com.sap.core.odata.api.processor.ODataRequest;
import com.sap.core.odata.api.processor.ODataResponse;
import com.sap.core.odata.api.processor.ODataResponse.ODataResponseBuilder;
import com.sap.core.odata.api.uri.PathSegment;
import com.sap.core.odata.core.uri.UriInfoImpl;
import com.sap.core.odata.core.uri.UriParserImpl;
import com.sap.core.odata.core.uri.UriType;

/**
 * 
 * @author SAP AG
 */
public class ODataRequestHandler {

  private ODataServiceFactory serviceFactory;

  private ODataService service;

  private Dispatcher dispatcher;

  private UriParserImpl uriParser;

  private ODataContextImpl context;


  public ODataRequestHandler(final ODataServiceFactory factory) {
    super();
    serviceFactory = factory;
  }

  public ODataResponse handle(final ODataRequest request) throws ODataException {
    context = new ODataContextImpl();

    context.setRequest(request);
    context.setPathInfo(request.getPathInfo());
    context.setHttpMethod(request.getMethod().toString());
 
    context.setAcceptableLanguages(request.getAcceptableLanguages());
    service = serviceFactory.createService(context);
    context.setService(service);
    service.getProcessor().setContext(context);

    uriParser = new UriParserImpl(service.getEntityDataModel());
    dispatcher = new Dispatcher(service, new ContentNegotiator());

    final String serverDataServiceVersion = getServerDataServiceVersion();
    validateDataServiceVersion(serverDataServiceVersion);
    final List<PathSegment> pathSegments = context.getPathInfo().getODataSegments();
    final UriInfoImpl uriInfo = (UriInfoImpl) uriParser.parse(pathSegments, request.getQueryParameters());

    final String requestContentType = request.getContentType();

    ODataResponse odataResponse = dispatcher.dispatch(request.getMethod(), uriInfo, request.getBody(), requestContentType, request.getAcceptHeaders());

    ODataHttpMethod method = request.getMethod();
    final String location = (method == ODataHttpMethod.POST && (uriInfo.getUriType() == UriType.URI1 || uriInfo.getUriType() == UriType.URI6B)) ? odataResponse.getIdLiteral() : null;
    final HttpStatusCodes s = odataResponse.getStatus() == null ? method == ODataHttpMethod.POST ? uriInfo.getUriType() == UriType.URI9 ? HttpStatusCodes.OK : uriInfo.getUriType() == UriType.URI7B ? HttpStatusCodes.NO_CONTENT : HttpStatusCodes.CREATED : method == ODataHttpMethod.PUT || method == ODataHttpMethod.PATCH || method == ODataHttpMethod.MERGE || method == ODataHttpMethod.DELETE ? HttpStatusCodes.NO_CONTENT : HttpStatusCodes.OK : odataResponse.getStatus();

    ODataResponse newResponse = copyResponse(odataResponse, s, serverDataServiceVersion, location);

    return newResponse;
  }

  /**
   * @param odataResponse 
   * @param s
   * @param serverDataServiceVersion
   * @param location
   * @return
   */
  private ODataResponse copyResponse(final ODataResponse odataResponse, final HttpStatusCodes s, final String serverDataServiceVersion, final String location) {
    ODataResponseBuilder builder = ODataResponse.fromResponse(odataResponse);

    builder.status(s);

    if (!odataResponse.containsHeader(ODataHttpHeaders.DATASERVICEVERSION)) {
      builder.header(ODataHttpHeaders.DATASERVICEVERSION, serverDataServiceVersion);
    }

    if (!odataResponse.containsHeader(HttpHeaders.LOCATION) && location != null) {
      builder.header(HttpHeaders.LOCATION, location);
    }

    return builder.build();
  }

  private String getServerDataServiceVersion() throws ODataException {
    String serverDataServiceVersion = ODataServiceVersion.V20;
    if (service.getVersion() != null) {
      serverDataServiceVersion = service.getVersion();
    }
    return serverDataServiceVersion;
  }

  private void validateDataServiceVersion(final String serverDataServiceVersion) throws ODataException {
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

}
