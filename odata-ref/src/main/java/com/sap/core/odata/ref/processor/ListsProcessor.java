package com.sap.core.odata.ref.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.core.odata.api.edm.EdmEntitySet;
import com.sap.core.odata.api.edm.EdmException;
import com.sap.core.odata.api.edm.EdmLiteralKind;
import com.sap.core.odata.api.edm.EdmProperty;
import com.sap.core.odata.api.edm.EdmSimpleType;
import com.sap.core.odata.api.enums.HttpStatus;
import com.sap.core.odata.api.enums.InlineCount;
import com.sap.core.odata.api.exception.ODataException;
import com.sap.core.odata.api.exception.ODataNotFoundException;
import com.sap.core.odata.api.exception.ODataNotImplementedException;
import com.sap.core.odata.api.processor.ODataResponse;
import com.sap.core.odata.api.processor.ODataResponse.ODataResponseBuilder;
import com.sap.core.odata.api.processor.ODataSingleProcessor;
import com.sap.core.odata.api.uri.KeyPredicate;
import com.sap.core.odata.api.uri.NavigationSegment;
import com.sap.core.odata.api.uri.UriLiteral;
import com.sap.core.odata.api.uri.resultviews.GetEntityCountView;
import com.sap.core.odata.api.uri.resultviews.GetEntitySetCountView;
import com.sap.core.odata.api.uri.resultviews.GetEntitySetView;
import com.sap.core.odata.api.uri.resultviews.GetEntityView;
import com.sap.core.odata.api.uri.resultviews.GetFunctionImportView;
import com.sap.core.odata.api.uri.resultviews.GetMetadataView;
import com.sap.core.odata.api.uri.resultviews.GetServiceDocumentView;

/**
 * Implementation of the centralized parts of OData processing,
 * allowing to use the simplified {@link ListsDataSource}
 * for the actual data handling
 * @author SAP AG
 */
public class ListsProcessor extends ODataSingleProcessor {

  private final ListsDataSource dataSource;

  public ListsProcessor(ListsDataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public ODataResponse readServiceDocument(final GetServiceDocumentView uriParserResultView) throws ODataException {
    return ODataResponse.status(HttpStatus.OK).entity("this should be the service document").build();
  }

  @Override
  public ODataResponse readMetadata(final GetMetadataView uriParserResultView) throws ODataException {
    return ODataResponse.status(HttpStatus.OK).entity("this should be the metadata document").build();
  }

  @Override
  public ODataResponse readEntitySet(final GetEntitySetView uriParserResultView) throws ODataException {
    ArrayList<Object> data = new ArrayList<Object>();
    data.addAll((List<?>) retrieveData(
        uriParserResultView.getStartEntitySet(),
        uriParserResultView.getKeyPredicates(),
        uriParserResultView.getNavigationSegments()));

    applySystemQueryOptions(
        uriParserResultView.getTargetEntitySet(),
        data,
        uriParserResultView.getInlineCount(),
        uriParserResultView.getFilter(),
        uriParserResultView.getOrderBy(),
        uriParserResultView.getSkipToken(),
        uriParserResultView.getSkip(),
        uriParserResultView.getTop());

    return ODataResponseBuilder.newInstance().status(HttpStatus.OK).entity(data.toString()).build();
  }

  @Override
  public ODataResponse countEntitySet(final GetEntitySetCountView uriParserResultView) throws ODataException {
    ArrayList<Object> data = new ArrayList<Object>();
    data.addAll((List<?>) retrieveData(
        uriParserResultView.getStartEntitySet(),
        uriParserResultView.getKeyPredicates(),
        uriParserResultView.getNavigationSegments()));

    applySystemQueryOptions(
        uriParserResultView.getTargetEntitySet(),
        data,
        null,
        uriParserResultView.getFilter(),
        null,
        null,
        uriParserResultView.getSkip(),
        uriParserResultView.getTop());

    return ODataResponseBuilder.newInstance().status(HttpStatus.OK).entity(String.valueOf(data.size())).build();
  }

  @Override
  public ODataResponse readEntity(final GetEntityView uriParserResultView) throws ODataException {
    final Object data = retrieveData(
        uriParserResultView.getStartEntitySet(),
        uriParserResultView.getKeyPredicates(),
        uriParserResultView.getNavigationSegments());

    if (appliesFilter(data, uriParserResultView.getFilter()))
      return ODataResponseBuilder.newInstance().status(HttpStatus.OK).entity(data.toString()).build();
    else
      throw new ODataNotFoundException(ODataNotFoundException.ENTITY);
  }

  @Override
  public ODataResponse existsEntity(final GetEntityCountView uriParserResultView) throws ODataException {
    final Object data = retrieveData(
        uriParserResultView.getStartEntitySet(),
        uriParserResultView.getKeyPredicates(),
        uriParserResultView.getNavigationSegments());

    return ODataResponseBuilder.newInstance().status(HttpStatus.OK).entity(
        appliesFilter(data, uriParserResultView.getFilter()) ? "1" : "0")
        .build();
  }

  @Override
  public ODataResponse executeFunctionImport(GetFunctionImportView uriParserResultView) throws ODataException {
    final Object data = dataSource.readDataFromFunction(
        uriParserResultView.getFunctionImport(),
        mapFunctionParameters(uriParserResultView.getFunctionImportParameters()),
        null);

    return ODataResponseBuilder.newInstance().status(HttpStatus.OK).entity(data.toString()).build();
  }

  private HashMap<String, Object> mapKey(final List<KeyPredicate> keys) throws EdmException {
    HashMap<String, Object> keyMap = new HashMap<String, Object>();
    for (final KeyPredicate key : keys) {
      final EdmProperty property = key.getProperty();
      final EdmSimpleType type = (EdmSimpleType) property.getType();
      keyMap.put(property.getName(), type.valueOfString(key.getLiteral(), EdmLiteralKind.DEFAULT, property.getFacets()));
    }
    return keyMap;
  }

  private Map<String, Object> mapFunctionParameters(final Map<String, UriLiteral> functionImportParameters) {
    HashMap<String, Object> parameterMap = new HashMap<String, Object>();
    for (final String parameterName : functionImportParameters.keySet()) {
      final UriLiteral literal = functionImportParameters.get(parameterName);
      final EdmSimpleType type = (EdmSimpleType) literal.getType();
      parameterMap.put(parameterName, type.valueOfString(literal.getLiteral(), EdmLiteralKind.DEFAULT, null));
    }
    return parameterMap;
  }

  private Object retrieveData(final EdmEntitySet startEntitySet, final List<KeyPredicate> keyPredicates, final List<NavigationSegment> navigationSegments) throws ODataException {
    Object data;
    final HashMap<String, Object> keys = mapKey(keyPredicates);

    if (keys.isEmpty())
      data = dataSource.readData(startEntitySet);
    else
      data = dataSource.readData(startEntitySet, keys);

    EdmEntitySet currentEntitySet = startEntitySet;
    for (NavigationSegment navigationSegment : navigationSegments) {
      data = dataSource.readRelatedData(
          currentEntitySet,
          data,
          navigationSegment.getEntitySet(),
          mapKey(navigationSegment.getKeyPredicates()));
      currentEntitySet = navigationSegment.getEntitySet();
    }

    return data;
  }

  private Integer applySystemQueryOptions(final EdmEntitySet targetEntitySet, List<?> data, final InlineCount inlineCount, final String filter, final String orderBy, final String skipToken, final int skip, final Integer top) throws ODataException {
    if (filter != null)
      for (Object element : data)
        if (!appliesFilter(element, filter))
          data.remove(element);

    final Integer count = inlineCount == InlineCount.ALLPAGES ? data.size() : null;

    if (orderBy != null)
      ;
    else if (skipToken != null || skip != 0 || top != null)
      try {
        // Collections.sort(data);
        throw new ODataNotImplementedException();
      } catch (ClassCastException e) {
        throw new ODataException(e);
      } catch (UnsupportedOperationException e) {
        throw new ODataException(e);
      }

    if (skipToken != null)
    ;

    for (int i = 0; i < skip; i++)
      data.remove(0);

    if (top != null)
      while (data.size() > top)
        data.remove(top.intValue());

    return count;
  }

  private boolean appliesFilter(final Object data, final String filter) {
    return true;
  }
}
