package com.sap.core.odata.ref.processor;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.core.odata.api.edm.EdmEntitySet;
import com.sap.core.odata.api.edm.EdmException;
import com.sap.core.odata.api.edm.EdmFunctionImport;
import com.sap.core.odata.api.edm.EdmLiteralKind;
import com.sap.core.odata.api.edm.EdmProperty;
import com.sap.core.odata.api.edm.EdmSimpleType;
import com.sap.core.odata.api.edm.EdmSimpleTypeException;
import com.sap.core.odata.api.edm.EdmSimpleTypeKind;
import com.sap.core.odata.api.edm.EdmStructuralType;
import com.sap.core.odata.api.edm.EdmTypeKind;
import com.sap.core.odata.api.enums.Format;
import com.sap.core.odata.api.enums.HttpStatusCodes;
import com.sap.core.odata.api.enums.InlineCount;
import com.sap.core.odata.api.exception.ODataException;
import com.sap.core.odata.api.exception.ODataNotFoundException;
import com.sap.core.odata.api.exception.ODataNotImplementedException;
import com.sap.core.odata.api.processor.ODataResponse;
import com.sap.core.odata.api.processor.ODataSingleProcessor;
import com.sap.core.odata.api.serialization.ODataSerializer;
import com.sap.core.odata.api.uri.EdmLiteral;
import com.sap.core.odata.api.uri.KeyPredicate;
import com.sap.core.odata.api.uri.NavigationSegment;
import com.sap.core.odata.api.uri.expression.FilterExpression;
import com.sap.core.odata.api.uri.expression.OrderByExpression;
import com.sap.core.odata.api.uri.resultviews.GetComplexPropertyView;
import com.sap.core.odata.api.uri.resultviews.GetEntityCountView;
import com.sap.core.odata.api.uri.resultviews.GetEntityLinkCountView;
import com.sap.core.odata.api.uri.resultviews.GetEntityLinkView;
import com.sap.core.odata.api.uri.resultviews.GetEntitySetCountView;
import com.sap.core.odata.api.uri.resultviews.GetEntitySetLinksCountView;
import com.sap.core.odata.api.uri.resultviews.GetEntitySetLinksView;
import com.sap.core.odata.api.uri.resultviews.GetEntitySetView;
import com.sap.core.odata.api.uri.resultviews.GetEntityView;
import com.sap.core.odata.api.uri.resultviews.GetFunctionImportView;
import com.sap.core.odata.api.uri.resultviews.GetMediaResourceView;
import com.sap.core.odata.api.uri.resultviews.GetServiceDocumentView;
import com.sap.core.odata.api.uri.resultviews.GetSimplePropertyView;
import com.sap.core.odata.ref.util.ObjectHelper;

/**
 * Implementation of the centralized parts of OData processing,
 * allowing to use the simplified {@link ListsDataSource}
 * for the actual data handling
 * @author SAP AG
 */
public class ListsProcessor extends ODataSingleProcessor {

  private static final String CONTENT_TYPE = "Content-Type";
  private static final String TEXT_PLAIN = "text/plain";
  private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
  private static final String APPLICATION_ATOM_XML_ENTRY = "application/atom+xml;type=entry";
  private static final String APPLICATION_ATOM_XML_FEED = "application/atom+xml;type=feed";
  private static final String APPLICATION_XML = "application/xml";

  private final ListsDataSource dataSource;

  public ListsProcessor(ListsDataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public ODataResponse readServiceDocument(final GetServiceDocumentView uriParserResultView) throws ODataException {
    return ODataResponse
        .status(HttpStatusCodes.OK)
        .header(CONTENT_TYPE, APPLICATION_XML)
        .entity("this should be the service document")
        .build();
  }

  @Override
  public ODataResponse readEntitySet(final GetEntitySetView uriParserResultView) throws ODataException {
    ArrayList<Object> data = new ArrayList<Object>();
    data.addAll((List<?>) retrieveData(
        uriParserResultView.getStartEntitySet(),
        uriParserResultView.getKeyPredicates(),
        uriParserResultView.getFunctionImport(),
        mapFunctionParameters(uriParserResultView.getFunctionImportParameters()),
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

    return ODataResponse
        .status(HttpStatusCodes.OK)
        .header(CONTENT_TYPE, APPLICATION_ATOM_XML_FEED)
        .entity(serialize(uriParserResultView.getTargetEntitySet(), uriParserResultView.getFormat(), data))
        .build();
  }

  @Override
  public ODataResponse countEntitySet(final GetEntitySetCountView uriParserResultView) throws ODataException {
    ArrayList<Object> data = new ArrayList<Object>();
    data.addAll((List<?>) retrieveData(
        uriParserResultView.getStartEntitySet(),
        uriParserResultView.getKeyPredicates(),
        uriParserResultView.getFunctionImport(),
        mapFunctionParameters(uriParserResultView.getFunctionImportParameters()),
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

    return ODataResponse
        .status(HttpStatusCodes.OK)
        .header(CONTENT_TYPE, TEXT_PLAIN)
        .entity(String.valueOf(data.size()))
        .build();
  }

  @Override
  public ODataResponse readEntityLinks(final GetEntitySetLinksView uriParserResultView) throws ODataException {
    ArrayList<Object> data = new ArrayList<Object>();
    data.addAll((List<?>) retrieveData(
        uriParserResultView.getStartEntitySet(),
        uriParserResultView.getKeyPredicates(),
        uriParserResultView.getFunctionImport(),
        mapFunctionParameters(uriParserResultView.getFunctionImportParameters()),
        uriParserResultView.getNavigationSegments()));

    applySystemQueryOptions(
        uriParserResultView.getTargetEntitySet(),
        data,
        uriParserResultView.getInlineCount(),
        uriParserResultView.getFilter(),
        // uriParserResultView.getOrderBy(),
        null,
        uriParserResultView.getSkipToken(),
        uriParserResultView.getSkip(),
        uriParserResultView.getTop());

    return ODataResponse
        .status(HttpStatusCodes.OK)
        .header(CONTENT_TYPE, APPLICATION_XML)
        .entity("Links to " + data)
        .build();
  }

  @Override
  public ODataResponse countEntityLinks(final GetEntitySetLinksCountView uriParserResultView) throws ODataException {
    return countEntitySet((GetEntitySetCountView) uriParserResultView);
  }

  @Override
  public ODataResponse readEntity(final GetEntityView uriParserResultView) throws ODataException {
    final Object data = retrieveData(
        uriParserResultView.getStartEntitySet(),
        uriParserResultView.getKeyPredicates(),
        uriParserResultView.getFunctionImport(),
        mapFunctionParameters(uriParserResultView.getFunctionImportParameters()),
        uriParserResultView.getNavigationSegments());

    if (!appliesFilter(data, uriParserResultView.getFilter()))
      throw new ODataNotFoundException(ODataNotFoundException.ENTITY);

    // final EdmEntitySet entitySet = uriParserResultView.getTargetEntitySet();
    // final Map<String, Object> values = getStructuralTypeValueMap(data, entitySet.getEntityType());

    return ODataResponse
        .status(HttpStatusCodes.OK)
        .header(CONTENT_TYPE, APPLICATION_ATOM_XML_ENTRY)
        .entity(
            // ODataSerializer.create(uriParserResultView.getFormat(), getContext()).serializeEntry(entitySet, values))
            serialize(uriParserResultView.getTargetEntitySet(), uriParserResultView.getFormat(), data))
        .build();
  }

  @Override
  public ODataResponse existsEntity(final GetEntityCountView uriParserResultView) throws ODataException {
    final Object data = retrieveData(
        uriParserResultView.getStartEntitySet(),
        uriParserResultView.getKeyPredicates(),
        uriParserResultView.getFunctionImport(),
        mapFunctionParameters(uriParserResultView.getFunctionImportParameters()),
        uriParserResultView.getNavigationSegments());

    return ODataResponse
        .status(HttpStatusCodes.OK)
        .header(CONTENT_TYPE, TEXT_PLAIN)
        .entity(appliesFilter(data, uriParserResultView.getFilter()) ? "1" : "0")
        .build();
  }

  @Override
  public ODataResponse readEntityLink(final GetEntityLinkView uriParserResultView) throws ODataException {
    final Object data = retrieveData(
        uriParserResultView.getStartEntitySet(),
        uriParserResultView.getKeyPredicates(),
        uriParserResultView.getFunctionImport(),
        mapFunctionParameters(uriParserResultView.getFunctionImportParameters()),
        uriParserResultView.getNavigationSegments());

    // if (!appliesFilter(data, uriParserResultView.getFilter()))
    if (data == null)
      throw new ODataNotFoundException(ODataNotFoundException.ENTITY);

    return ODataResponse
          .status(HttpStatusCodes.OK)
          .header(CONTENT_TYPE, APPLICATION_XML)
          .entity("Link to " + data)
          .build();
  }

  @Override
  public ODataResponse existsEntityLink(final GetEntityLinkCountView uriParserResultView) throws ODataException {
    return existsEntity((GetEntityCountView) uriParserResultView);
  }

  @Override
  public ODataResponse readEntityComplexProperty(final GetComplexPropertyView uriParserResultView) throws ODataException {
    Object data = retrieveData(
        uriParserResultView.getStartEntitySet(),
        uriParserResultView.getKeyPredicates(),
        uriParserResultView.getFunctionImport(),
        mapFunctionParameters(uriParserResultView.getFunctionImportParameters()),
        uriParserResultView.getNavigationSegments());

    // if (!appliesFilter(data, uriParserResultView.getFilter()))
    //   throw new ODataNotFoundException(ODataNotFoundException.ENTITY);

    EdmProperty property = null;
    for (EdmProperty intermediateProperty : uriParserResultView.getPropertyPath())
      data = getPropertyValue(data, property = intermediateProperty);

    final Object value = property.getType().getKind() == EdmTypeKind.COMPLEX ?
        getStructuralTypeValueMap(data, (EdmStructuralType) property.getType()) : data;

    return ODataResponse
        .status(HttpStatusCodes.OK)
        .header(CONTENT_TYPE, APPLICATION_XML)
        .entity(ODataSerializer.create(Format.XML, getContext()).serializeProperty(property, value))
        .build();
  }

  @Override
  public ODataResponse readEntitySimpleProperty(final GetSimplePropertyView uriParserResultView) throws ODataException {
    return readEntityComplexProperty((GetComplexPropertyView) uriParserResultView);
  }

  @Override
  public ODataResponse readEntitySimplePropertyValue(final GetSimplePropertyView uriParserResultView) throws ODataException {
    Object data = retrieveData(
        uriParserResultView.getStartEntitySet(),
        uriParserResultView.getKeyPredicates(),
        uriParserResultView.getFunctionImport(),
        mapFunctionParameters(uriParserResultView.getFunctionImportParameters()),
        uriParserResultView.getNavigationSegments());

    // if (!appliesFilter(data, uriParserResultView.getFilter()))
    //   throw new ODataNotFoundException(ODataNotFoundException.ENTITY);

    EdmProperty property = null;
    for (EdmProperty intermediateProperty : uriParserResultView.getPropertyPath())
      data = getPropertyValue(data, property = intermediateProperty);

    return ODataResponse
        .status(HttpStatusCodes.OK)
        .header(CONTENT_TYPE,
            (EdmSimpleType) property.getType() == EdmSimpleTypeKind.Binary.getEdmSimpleTypeInstance() ?
                property.getMimeType() : TEXT_PLAIN)
        .entity(data == null ? "" : data.toString())
        .build();
  }

  @Override
  public ODataResponse readEntityMedia(final GetMediaResourceView uriParserResultView) throws ODataException {
    final Object data = retrieveData(
        uriParserResultView.getStartEntitySet(),
        uriParserResultView.getKeyPredicates(),
        uriParserResultView.getFunctionImport(),
        mapFunctionParameters(uriParserResultView.getFunctionImportParameters()),
        uriParserResultView.getNavigationSegments());

    if (!appliesFilter(data, uriParserResultView.getFilter()))
      throw new ODataNotFoundException(ODataNotFoundException.ENTITY);

    StringBuilder mimeType = new StringBuilder();
    final byte[] binaryData = dataSource.readBinaryData(uriParserResultView.getTargetEntitySet(), data, mimeType);

    return ODataResponse
        .status(HttpStatusCodes.OK)
        .header(CONTENT_TYPE,
            mimeType.toString().isEmpty() ? APPLICATION_OCTET_STREAM : mimeType.toString())
        .entity(binaryData)
        .build();
  }

  @Override
  public ODataResponse executeFunctionImport(final GetFunctionImportView uriParserResultView) throws ODataException {
    final Object data = dataSource.readData(
        uriParserResultView.getFunctionImport(),
        mapFunctionParameters(uriParserResultView.getFunctionImportParameters()),
        null);

    return ODataResponse
        .status(HttpStatusCodes.OK)
        .header(CONTENT_TYPE, APPLICATION_XML)
        .entity(data.toString())
        .build();
  }

  @Override
  public ODataResponse executeFunctionImportValue(final GetFunctionImportView uriParserResultView) throws ODataException {
    final EdmFunctionImport functionImport = uriParserResultView.getFunctionImport();
    final Object data = dataSource.readData(
        functionImport,
        mapFunctionParameters(uriParserResultView.getFunctionImportParameters()),
        null);

    return ODataResponse
        .status(HttpStatusCodes.OK)
        .header(CONTENT_TYPE,
            (EdmSimpleType) functionImport.getReturnType().getType() == EdmSimpleTypeKind.Binary.getEdmSimpleTypeInstance() ?
                APPLICATION_OCTET_STREAM : TEXT_PLAIN)
        .entity(data.toString())
        .build();
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

  private Map<String, Object> mapFunctionParameters(final Map<String, EdmLiteral> functionImportParameters) throws EdmSimpleTypeException {
    if (functionImportParameters == null) {
      return Collections.emptyMap();
    } else {
      HashMap<String, Object> parameterMap = new HashMap<String, Object>();
      for (final String parameterName : functionImportParameters.keySet()) {
        final EdmLiteral literal = functionImportParameters.get(parameterName);
        final EdmSimpleType type = (EdmSimpleType) literal.getType();
        parameterMap.put(parameterName, type.valueOfString(literal.getLiteral(), EdmLiteralKind.DEFAULT, null));
      }
      return parameterMap;
    }
  }

  private Object retrieveData(final EdmEntitySet startEntitySet, final List<KeyPredicate> keyPredicates, final EdmFunctionImport functionImport, final Map<String, Object> functionImportParameters, final List<NavigationSegment> navigationSegments) throws ODataException {
    Object data;
    final HashMap<String, Object> keys = mapKey(keyPredicates);

    if (functionImport == null)
      if (keys.isEmpty())
        data = dataSource.readData(startEntitySet);
      else
        data = dataSource.readData(startEntitySet, keys);
    else
      data = dataSource.readData(functionImport, functionImportParameters, keys);

    EdmEntitySet currentEntitySet =
        functionImport == null ? startEntitySet : functionImport.getEntitySet();
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

  private <T> Integer applySystemQueryOptions(final EdmEntitySet targetEntitySet, List<T> data, final InlineCount inlineCount, final FilterExpression filter, final OrderByExpression orderBy, final String skipToken, final int skip, final Integer top) throws ODataException {
    if (filter != null)
      for (T element : data)
        if (!appliesFilter(element, filter))
          data.remove(element);

    final Integer count = inlineCount == InlineCount.ALLPAGES ? data.size() : null;

    if (orderBy != null)
      throw new ODataNotImplementedException();
    else if (skipToken != null || skip != 0 || top != null)
      Collections.sort(data, new Comparator<T>() {
        @Override
        public int compare(T entity1, T entity2) {
          try {
            return getSkipToken(entity1, targetEntitySet).compareTo(getSkipToken(entity2, targetEntitySet));
          } catch (ODataException e) {
            return 0;
          }
        }
      });

    if (skipToken != null)
      while (!data.isEmpty() && !getSkipToken(data.get(0), targetEntitySet).equals(skipToken))
        data.remove(0);

    if (skip >= data.size())
      data.clear();
    else
      for (int i = 0; i < skip; i++)
        data.remove(0);

    if (top != null)
      while (data.size() > top)
        data.remove(top.intValue());

    return count;
  }

  private <T> boolean appliesFilter(final T data, final FilterExpression filter) throws ODataException {
    if (data == null)
      return false;
    if (filter == null)
      return true;
    // TODO: implement filter evaluation
    throw new ODataNotImplementedException();
  }

  private <T> String getSkipToken(final T data, final EdmEntitySet entitySet) throws ODataException {
    List<EdmProperty> keyProperties = entitySet.getEntityType().getKeyProperties();
    // The key properties come from a hash map without predictable order.
    // Since this implementation builds the skip token by concatenating the values
    // of the key properties, order is relevant.
    Collections.sort(keyProperties, new Comparator<EdmProperty>() {
      @Override
      public int compare(final EdmProperty keyProperty1, final EdmProperty keyProperty2) {
        try {
          return keyProperty1.getName().compareToIgnoreCase(keyProperty2.getName());
        } catch (EdmException e) {
          return 0;
        }
      }
    });

    String skipToken = "";
    for (final EdmProperty property : keyProperties) {
      final EdmSimpleType type = (EdmSimpleType) property.getType();
      skipToken = skipToken.concat(type.valueToString(getPropertyValue(data, property), EdmLiteralKind.DEFAULT, property.getFacets()));
    }
    return skipToken;
  }

  private <T> Object getPropertyValue(final T data, final EdmProperty property) throws ODataException {
    final String prefix = property.getType().getKind() == EdmTypeKind.SIMPLE && property.getType() == EdmSimpleTypeKind.Boolean.getEdmSimpleTypeInstance() ? "is" : "get";
    final String methodName = property.getMapping() == null ?
        prefix + property.getName() : property.getMapping().getValue();
    try {
      return data.getClass().getMethod(methodName).invoke(data);
    } catch (SecurityException e) {
      throw new ODataNotFoundException(null, e);
    } catch (NoSuchMethodException e) {
      throw new ODataNotFoundException(null, e);
    } catch (IllegalArgumentException e) {
      throw new ODataNotFoundException(null, e);
    } catch (IllegalAccessException e) {
      throw new ODataNotFoundException(null, e);
    } catch (InvocationTargetException e) {
      throw new ODataNotFoundException(null, e);
    }
  }

  private <T> Map<String, Object> getStructuralTypeValueMap(final T data, final EdmStructuralType type) throws ODataException {
    Map<String, Object> valueMap = new HashMap<String, Object>();

    for (final String propertyName : type.getPropertyNames()) {
      final EdmProperty property = (EdmProperty) type.getProperty(propertyName);
      final Object value = getPropertyValue(data, property);

      if (property.getType().getKind() == EdmTypeKind.COMPLEX)
        valueMap.put(propertyName, getStructuralTypeValueMap(value, (EdmStructuralType) property.getType()));
      else
        valueMap.put(propertyName, value);
      }

    return valueMap;
  }

  private Object serialize(EdmEntitySet entitySet, Format format, List<Object> objects) throws ODataException {
    if (format == null) {
      return objects == null ? "NULL" : objects.toString();
    } else {
      Map<String, Object> rawData = objectToMap(objects);
      ODataSerializer ser = ODataSerializer.create(format, getContext());

      return ser.serializeEntry(entitySet, rawData);
    }
  }

  private Object serialize(EdmEntitySet entitySet, Format format, Object object) throws ODataException {
    if (format == null) {
      return object == null ? "NULL" : object.toString();
    } else {
      Map<String, Object> rawData = objectToMap(object);
      ODataSerializer ser = ODataSerializer.create(format, getContext());

      return ser.serializeEntry(entitySet, rawData);
    }
  }

  private List<Map<String, Object>> objectsToList(List<Object> objects) throws ODataException {
    List<Map<String, Object>> mappedObjects = new ArrayList<Map<String, Object>>();

    for (Object object : objects) {
      mappedObjects.add(objectToMap(object));
    }

    return mappedObjects;
  }

  private Map<String, Object> objectToMap(List<Object> objects) throws ODataException {
    Map<String, Object> mappedObjects = new HashMap<String, Object>();

    for (Object object : objects) {
      mappedObjects.put(String.valueOf(object.hashCode()), objectToMap(object));
    }

    return mappedObjects;
  }

  private Map<String, Object> objectToMap(Object object) throws ODataException {
    ObjectHelper objHelper = ObjectHelper.init(object);
    try {
      return objHelper.getFlatFieldValues();
    } catch (IllegalAccessException e) {
      throw new ODataException("Failure on object mapping.", e);
    }
  }
}
