package com.sap.core.odata.core.uri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.core.odata.api.edm.Edm;
import com.sap.core.odata.api.edm.EdmComplexType;
import com.sap.core.odata.api.edm.EdmEntitySet;
import com.sap.core.odata.api.edm.EdmEntityType;
import com.sap.core.odata.api.edm.EdmException;
import com.sap.core.odata.api.edm.EdmFunctionImport;
import com.sap.core.odata.api.edm.EdmMultiplicity;
import com.sap.core.odata.api.edm.EdmNavigationProperty;
import com.sap.core.odata.api.edm.EdmParameter;
import com.sap.core.odata.api.edm.EdmProperty;
import com.sap.core.odata.api.edm.EdmSimpleType;
import com.sap.core.odata.api.edm.EdmSimpleTypeFacade;
import com.sap.core.odata.api.edm.EdmType;
import com.sap.core.odata.api.edm.EdmTypeKind;
import com.sap.core.odata.api.edm.EdmTyped;
import com.sap.core.odata.api.enums.Format;
import com.sap.core.odata.api.enums.InlineCount;
import com.sap.core.odata.api.processor.ODataPathSegment;
import com.sap.core.odata.api.uri.EdmLiteral;
import com.sap.core.odata.api.uri.KeyPredicate;
import com.sap.core.odata.api.uri.NavigationPropertySegment;
import com.sap.core.odata.api.uri.NavigationSegment;
import com.sap.core.odata.api.uri.SelectItem;
import com.sap.core.odata.api.uri.UriNotMatchingException;
import com.sap.core.odata.api.uri.UriParser;
import com.sap.core.odata.api.uri.UriParserResult;
import com.sap.core.odata.api.uri.UriSyntaxException;
import com.sap.core.odata.api.uri.expression.CommonExpression;
import com.sap.core.odata.api.uri.expression.FilterParserException;
import com.sap.core.odata.api.uri.expression.OrderByExpression;
import com.sap.core.odata.core.edm.EdmSimpleTypeFacadeImpl;
import com.sap.core.odata.core.exception.ODataRuntimeException;
import com.sap.core.odata.core.uri.expression.FilterParserImpl;
import com.sap.core.odata.core.uri.expression.FilterParserInternalError;

/**
 * @author SAP AG
 */
public class UriParserImpl implements UriParser {

  private static final Logger LOG = LoggerFactory.getLogger(UriParserImpl.class);

  private static final Pattern INITIAL_SEGMENT_PATTERN = Pattern.compile("(?:([^.()]+)\\.)?([^.()]+)(?:\\((.+)\\)|(\\(\\)))?");
  private static final Pattern NAVIGATION_SEGMENT_PATTERN = Pattern.compile("([^()]+)(?:\\((.+)\\)|(\\(\\)))?");
  private static final Pattern NAMED_VALUE_PATTERN = Pattern.compile("(?:([^=]+)=)?([^=]+)");

  private final Edm edm;
  private final EdmSimpleTypeFacade simpleTypeFacade;
  private List<String> pathSegments;
  private String currentPathSegment;
  private UriParserResultImpl uriResult;
  private Map<SystemQueryOption, String> systemQueryOptions;
  private Map<String, String> otherQueryParameters;

  public UriParserImpl(final Edm edm) {
    this.edm = edm;
    simpleTypeFacade = new EdmSimpleTypeFacadeImpl();
  }

  /**
   * Parse the URI part after an OData service root,
   * already splitted into path segments and query parameters.
   * @param pathSegments  the {@link ODataPathSegment}s of the resource path, already unescaped
   * @param queryParameters  the query parameters, already unescaped
   * @return a {@link UriParserResultImpl} instance containing the parsed information
   */
  @Override
  public UriParserResult parse(final List<ODataPathSegment> pathSegments, final Map<String, String> queryParameters) throws UriSyntaxException, UriNotMatchingException, EdmException {
    this.pathSegments = this.copyPathSegmentList(pathSegments);
    systemQueryOptions = new HashMap<SystemQueryOption, String>();
    otherQueryParameters = new HashMap<String, String>();
    uriResult = new UriParserResultImpl();

    preparePathSegments();

    handleResourcePath();

    distributeQueryParameters(queryParameters);
    checkSystemQueryOptionsCompatibility();
    handleSystemQueryOptions();
    handleOtherQueryParameters();

    UriParserImpl.LOG.debug(uriResult.toString());
    return uriResult;
  }

  private void preparePathSegments() throws UriSyntaxException {
    if (!pathSegments.isEmpty()) {
      if (pathSegments.get(0).equals("")) // initial '/' is allowed but ignored
        pathSegments.remove(0);
      if (pathSegments.size() == 1)
        if (pathSegments.get(0).equals("")) // only '/': service document
          pathSegments.remove(0);
      for (String pathSegment : pathSegments)
        if (pathSegment.equals(""))
          throw new UriSyntaxException(UriSyntaxException.EMPTYSEGMENT);
    }
  }

  private void handleResourcePath() throws UriSyntaxException, UriNotMatchingException, EdmException {
    UriParserImpl.LOG.debug("parsing: " + pathSegments);

    if (pathSegments.isEmpty()) {
      uriResult.setUriType(UriType.URI0);
    } else {

      currentPathSegment = pathSegments.remove(0);

      if ("$metadata".equals(currentPathSegment)) {
        ensureLastSegment();
        uriResult.setUriType(UriType.URI8);

      } else if ("$batch".equals(currentPathSegment)) {
        ensureLastSegment();
        uriResult.setUriType(UriType.URI9);

      } else {
        handleNormalInitialSegment();
      }
    }
  }

  private void handleNormalInitialSegment() throws UriSyntaxException, UriNotMatchingException, EdmException {
    final Matcher matcher = INITIAL_SEGMENT_PATTERN.matcher(currentPathSegment);
    if (!matcher.matches())
      throw new UriNotMatchingException(UriNotMatchingException.MATCHPROBLEM);

    final String entityContainerName = matcher.group(1);
    final String segmentName = matcher.group(2);
    final String keyPredicate = matcher.group(3);
    final String emptyParentheses = matcher.group(4);
    UriParserImpl.LOG.debug("RegEx (" + currentPathSegment + ") : entityContainerName=" + entityContainerName + ", segmentName=" + segmentName + ", keyPredicate=" + keyPredicate + ", emptyParentheses=" + emptyParentheses);

    uriResult.setEntityContainer(entityContainerName == null ? edm.getDefaultEntityContainer() : edm.getEntityContainer(entityContainerName));
    if (uriResult.getEntityContainer() == null)
      throw new UriNotMatchingException(UriNotMatchingException.CONTAINERNOTFOUND.addContent(entityContainerName));

    EdmEntitySet entitySet = null;

    entitySet = uriResult.getEntityContainer().getEntitySet(segmentName);
    if (entitySet != null) {
      uriResult.setStartEntitySet(entitySet);
      handleEntitySet(entitySet, keyPredicate);
    } else {
      EdmFunctionImport functionImport = null;
      functionImport = uriResult.getEntityContainer().getFunctionImport(segmentName);
      if (functionImport != null) {
        uriResult.setFunctionImport(functionImport);
        handleFunctionImport(functionImport, emptyParentheses, keyPredicate);
      } else {
        throw new UriNotMatchingException(UriNotMatchingException.NOTFOUND.addContent(segmentName));
      }
    }
  }

  private void handleEntitySet(final EdmEntitySet entitySet, final String keyPredicate) throws UriSyntaxException, UriNotMatchingException, EdmException {
    final EdmEntityType entityType = entitySet.getEntityType();

    uriResult.setTargetType(entityType);
    uriResult.setTargetEntitySet(entitySet);

    if (keyPredicate == null) {
      if (pathSegments.isEmpty()) {
        uriResult.setUriType(UriType.URI1);
      } else {
        currentPathSegment = pathSegments.remove(0);
        checkCount();
        if (uriResult.isCount())
          uriResult.setUriType(UriType.URI15);
        else
          throw new UriSyntaxException(UriSyntaxException.ENTITYSETINSTEADOFENTITY);
      }
    } else {
      uriResult.setKeyPredicates(parseKey(keyPredicate, entityType));
      if (pathSegments.isEmpty())
        uriResult.setUriType(UriType.URI2);
      else
        handleNavigationPathOptions();
    }
  }

  private void handleNavigationPathOptions() throws UriSyntaxException, UriNotMatchingException, EdmException {
    currentPathSegment = pathSegments.remove(0);

    checkCount();
    if (uriResult.isCount()) {
      uriResult.setUriType(UriType.URI16); // Count of multiple entities is handled elsewhere

    } else if ("$value".equals(currentPathSegment)) {
      if (uriResult.getTargetEntitySet().getEntityType().hasStream()) {
        ensureLastSegment();
        uriResult.setUriType(UriType.URI17);
        uriResult.setValue(true);
      }
      else
        throw new UriSyntaxException(UriSyntaxException.NOMEDIARESOURCE);

    } else if ("$links".equals(currentPathSegment)) {
      this.uriResult.setLinks(true);
      if (pathSegments.isEmpty())
        throw new UriSyntaxException(UriSyntaxException.NOTLASTSEGMENT.addContent(currentPathSegment));
      currentPathSegment = pathSegments.remove(0);
      handleNavigationProperties();

    } else {
      handleNavigationProperties();
    }
  }

  private void handleNavigationProperties() throws UriSyntaxException, UriNotMatchingException, EdmException {

    final Matcher matcher = NAVIGATION_SEGMENT_PATTERN.matcher(currentPathSegment);
    if (!matcher.matches())
      throw new UriNotMatchingException(UriNotMatchingException.MATCHPROBLEM.addContent(currentPathSegment));

    final String navigationPropertyName = matcher.group(1);
    final String keyPredicateName = matcher.group(2);
    final String emptyParentheses = matcher.group(3);
    UriParserImpl.LOG.debug("RegEx (" + currentPathSegment + "): NavigationProperty=" + navigationPropertyName + ", keyPredicate=" + keyPredicateName + ", emptyParentheses=" + emptyParentheses);

    final EdmTyped property = uriResult.getTargetEntitySet().getEntityType().getProperty(navigationPropertyName);
    if (property == null)
      throw new UriNotMatchingException(UriNotMatchingException.PROPERTYNOTFOUND.addContent(navigationPropertyName));

    switch (property.getType().getKind()) {
    case SIMPLE:
    case COMPLEX:
      if (keyPredicateName != null || emptyParentheses != null)
        throw new UriSyntaxException(UriSyntaxException.INVALIDSEGMENT.addContent(currentPathSegment));
      if (uriResult.isLinks())
        throw new UriSyntaxException(UriSyntaxException.NONAVIGATIONPROPERTY.addContent(property));
      else
        handlePropertyPath((EdmProperty) property);
      break;

    case ENTITY: // navigation properties point to entities
      final EdmNavigationProperty navigationProperty = (EdmNavigationProperty) property;
      if (keyPredicateName != null || emptyParentheses != null)
        if (navigationProperty.getMultiplicity() != EdmMultiplicity.MANY)
          throw new UriSyntaxException(UriSyntaxException.INVALIDSEGMENT.addContent(currentPathSegment));

      addNavigationSegment(keyPredicateName, navigationProperty);

      boolean many = false;
      if (navigationProperty.getMultiplicity() == EdmMultiplicity.MANY)
        many = keyPredicateName == null;

      if (pathSegments.isEmpty())
        if (many)
          if (uriResult.isLinks())
            uriResult.setUriType(UriType.URI7B);
          else
            uriResult.setUriType(UriType.URI6B);
        else if (uriResult.isLinks())
          uriResult.setUriType(UriType.URI7A);
        else
          uriResult.setUriType(UriType.URI6A);
      else if (many || uriResult.isLinks()) {
        currentPathSegment = pathSegments.remove(0);
        checkCount();
        if (!uriResult.isCount())
          throw new UriSyntaxException(UriSyntaxException.INVALIDSEGMENT.addContent(currentPathSegment));
        if (many)
          if (uriResult.isLinks())
            uriResult.setUriType(UriType.URI50B);
          else
            uriResult.setUriType(UriType.URI15);
        else
          uriResult.setUriType(UriType.URI50A);
      } else {
        handleNavigationPathOptions();
      }
      break;

    default:
      throw new UriSyntaxException(UriSyntaxException.INVALIDPROPERTYTYPE);
    }
  }

  private void addNavigationSegment(final String keyPredicateName, final EdmNavigationProperty navigationProperty) throws UriSyntaxException, EdmException {
    final EdmEntitySet targetEntitySet = uriResult.getTargetEntitySet().getRelatedEntitySet(navigationProperty);
    uriResult.setTargetEntitySet(targetEntitySet);
    uriResult.setTargetType(targetEntitySet.getEntityType());

    NavigationSegmentImpl navigationSegment = new NavigationSegmentImpl();
    navigationSegment.setEntitySet(targetEntitySet);
    navigationSegment.setNavigationProperty(navigationProperty);
    if (keyPredicateName != null)
      navigationSegment.setKeyPredicates(parseKey(keyPredicateName, targetEntitySet.getEntityType()));
    uriResult.addNavigationSegment((NavigationSegment) navigationSegment);
  }

  private void handlePropertyPath(final EdmProperty property) throws UriSyntaxException, UriNotMatchingException, EdmException {
    this.uriResult.addProperty(property);
    final EdmType type = property.getType();

    if (pathSegments.isEmpty()) {
      if (type.getKind() == EdmTypeKind.SIMPLE)
        if (this.uriResult.getPropertyPath().size() == 1)
          this.uriResult.setUriType(UriType.URI5);
        else
          this.uriResult.setUriType(UriType.URI4);
      else if (type.getKind() == EdmTypeKind.COMPLEX)
        this.uriResult.setUriType(UriType.URI3);
      else
        throw new UriSyntaxException(UriSyntaxException.INVALIDPROPERTYTYPE);
      this.uriResult.setTargetType(type);
    } else {

      currentPathSegment = pathSegments.remove(0);
      switch (type.getKind()) {
      case SIMPLE:
        if ("$value".equals(currentPathSegment)) {
          ensureLastSegment();
          uriResult.setValue(true);
          if (uriResult.getPropertyPath().size() == 1)
            uriResult.setUriType(UriType.URI5);
          else
            uriResult.setUriType(UriType.URI4);
        } else {
          throw new UriSyntaxException(UriSyntaxException.INVALIDSEGMENT.addContent(currentPathSegment));
        }
        uriResult.setTargetType(type);
        break;

      case COMPLEX:
        final EdmProperty nextProperty = (EdmProperty) ((EdmComplexType) type).getProperty(currentPathSegment);
        if (nextProperty == null) {
          throw new UriNotMatchingException(UriNotMatchingException.PROPERTYNOTFOUND.addContent(currentPathSegment));
        }
        handlePropertyPath(nextProperty);
        break;

      default:
        throw new UriSyntaxException(UriSyntaxException.INVALIDPROPERTYTYPE);
      }
    }
  }

  private void ensureLastSegment() throws UriSyntaxException {
    if (!pathSegments.isEmpty())
      throw new UriSyntaxException(UriSyntaxException.MUSTBELASTSEGMENT.addContent(currentPathSegment));
  }

  private void checkCount() throws UriSyntaxException {
    if ("$count".equals(currentPathSegment))
      if (pathSegments.isEmpty())
        uriResult.setCount(true);
      else
        throw new UriSyntaxException(UriSyntaxException.NOTLASTSEGMENT.addContent(currentPathSegment));
  }

  private ArrayList<KeyPredicate> parseKey(final String keyPredicate, final EdmEntityType entityType) throws UriSyntaxException, EdmException {
    final List<EdmProperty> keyProperties = entityType.getKeyProperties();
    ArrayList<EdmProperty> parsedKeyProperties = new ArrayList<EdmProperty>();
    ArrayList<KeyPredicate> keyPredicates = new ArrayList<KeyPredicate>();

    for (final String key : keyPredicate.split(",", -1)) {

      final Matcher matcher = NAMED_VALUE_PATTERN.matcher(key);
      if (!matcher.matches())
        throw new UriSyntaxException(UriSyntaxException.INVALIDKEYPREDICATE.addContent(keyPredicate));

      String name = matcher.group(1);
      final String value = matcher.group(2);
      UriParserImpl.LOG.debug("RegEx (" + keyPredicate + "): name=" + name + ", value=" + value);

      if (name == null)
        if (keyProperties.size() == 1)
          name = keyProperties.get(0).getName();
        else
          throw new UriSyntaxException(UriSyntaxException.MISSINGKEYPREDICATENAME.addContent(key));

      EdmProperty keyProperty = null;
      for (final EdmProperty testKeyProperty : keyProperties)
        if (testKeyProperty.getName().equals(name)) {
          keyProperty = testKeyProperty;
          break;
        }
      if (keyProperty == null)
        throw new UriSyntaxException(UriSyntaxException.INVALIDKEYPREDICATE.addContent(keyPredicate));
      if (parsedKeyProperties.contains(keyProperty))
        throw new UriSyntaxException(UriSyntaxException.DUPLICATEKEYNAMES.addContent(keyPredicate));
      parsedKeyProperties.add(keyProperty);

      final EdmLiteral uriLiteral = simpleTypeFacade.parseUriLiteral(value);

      if (!((EdmSimpleType) keyProperty.getType()).isCompatible(uriLiteral.getType()))
        throw new UriSyntaxException(UriSyntaxException.INCOMPATIBLELITERAL.addContent(keyProperty, uriLiteral));

      keyPredicates.add(new KeyPredicateImpl(uriLiteral.getLiteral(), keyProperty));
    }

    if (parsedKeyProperties.size() != keyProperties.size())
      throw new UriSyntaxException(UriSyntaxException.INVALIDKEYPREDICATE.addContent(keyPredicate));

    return keyPredicates;
  }

  private void handleFunctionImport(final EdmFunctionImport functionImport, final String emptyParentheses, final String keyPredicate) throws UriSyntaxException, UriNotMatchingException, EdmException {
    final EdmTyped returnType = functionImport.getReturnType();
    final EdmType type = returnType.getType();
    final boolean isCollection = returnType.getMultiplicity() == EdmMultiplicity.MANY;

    if (type.getKind() == EdmTypeKind.ENTITY && isCollection) {
      handleEntitySet(functionImport.getEntitySet(), keyPredicate);
      return;
    }

    if (emptyParentheses != null)
      throw new UriSyntaxException(UriSyntaxException.INVALIDSEGMENT);

    uriResult.setTargetType(type);
    switch (type.getKind()) {
    case SIMPLE:
      uriResult.setUriType(isCollection ? UriType.URI13 : UriType.URI14);
      break;
    case COMPLEX:
      uriResult.setUriType(isCollection ? UriType.URI11 : UriType.URI12);
      break;
    case ENTITY:
      uriResult.setUriType(UriType.URI10);
      break;
    default:
      throw new UriSyntaxException(UriSyntaxException.INVALIDRETURNTYPE);
    }

    if (!pathSegments.isEmpty())
      if (uriResult.getUriType() == UriType.URI14) {
        currentPathSegment = pathSegments.remove(0);
        if ("$value".equals(currentPathSegment))
          uriResult.setValue(true);
        else
          throw new UriSyntaxException(UriSyntaxException.INVALIDSEGMENT.addContent(currentPathSegment));
      }
    ensureLastSegment();
  }

  private void distributeQueryParameters(final Map<String, String> queryParameters) throws UriSyntaxException {
    UriParserImpl.LOG.debug("query parameters: " + queryParameters);
    for (String queryOptionString : queryParameters.keySet())
      if (queryOptionString.startsWith("$")) {
        SystemQueryOption queryOption;
        try {
          queryOption = SystemQueryOption.valueOf(queryOptionString);
        } catch (IllegalArgumentException e) {
          throw new UriSyntaxException(UriSyntaxException.INVALIDSYSTEMQUERYOPTION.addContent(queryOptionString), e);
        }
        final String value = queryParameters.get(queryOptionString);
        if ("".equals(value))
          throw new UriSyntaxException(UriSyntaxException.INVALIDNULLVALUE);
        else
          systemQueryOptions.put(queryOption, value);
      } else {
        otherQueryParameters.put(queryOptionString, queryParameters.get(queryOptionString));
      }
  }

  private void checkSystemQueryOptionsCompatibility() throws UriSyntaxException {
    final UriType uriType = uriResult.getUriType();

    for (SystemQueryOption queryOption : systemQueryOptions.keySet()) {

      if (queryOption == SystemQueryOption.$format && (uriType == UriType.URI4 || uriType == UriType.URI5) && uriResult.isValue())
        throw new UriSyntaxException(UriSyntaxException.INCOMPATIBLESYSTEMQUERYOPTION.addContent(queryOption));

      if (!uriType.isCompatible(queryOption))
        throw new UriSyntaxException(UriSyntaxException.INCOMPATIBLESYSTEMQUERYOPTION.addContent(queryOption));
    }
  }

  private void handleSystemQueryOptions() throws UriSyntaxException, UriNotMatchingException, EdmException {

    for (SystemQueryOption queryOption : systemQueryOptions.keySet())
      switch (queryOption) {
      case $format:
        handleSystemQueryOptionFormat(systemQueryOptions.get(SystemQueryOption.$format));
        break;
      case $filter:
        handleSystemQueryOptionFilter(systemQueryOptions.get(SystemQueryOption.$filter));
        break;
      case $inlinecount:
        handleSystemQueryOptionInlineCount(systemQueryOptions.get(SystemQueryOption.$inlinecount));
        break;
      case $orderby:
        handleSystemQueryOptionOrderBy(systemQueryOptions.get(SystemQueryOption.$orderby));
        break;
      case $skiptoken:
        handleSystemQueryOptionSkipToken(systemQueryOptions.get(SystemQueryOption.$skiptoken));
        break;
      case $skip:
        handleSystemQueryOptionSkip(systemQueryOptions.get(SystemQueryOption.$skip));
        break;
      case $top:
        handleSystemQueryOptionTop(systemQueryOptions.get(SystemQueryOption.$top));
        break;
      case $expand:
        handleSystemQueryOptionExpand(systemQueryOptions.get(SystemQueryOption.$expand));
        break;
      case $select:
        handleSystemQueryOptionSelect(systemQueryOptions.get(SystemQueryOption.$select));
        break;
      default:
        throw new ODataRuntimeException("Invalid System Query Option " + queryOption);
      }
  }

  private void handleSystemQueryOptionFormat(final String format) throws UriSyntaxException {
    if ("atom".equals(format))
      uriResult.setFormat(Format.ATOM);
    else if ("json".equals(format))
      uriResult.setFormat(Format.JSON);
    else if ("xml".equals(format))
      uriResult.setFormat(Format.XML);
    else
      uriResult.setFormat(format);
  }

  private void handleSystemQueryOptionFilter(final String filter) throws UriSyntaxException {
    try {
      if (uriResult.getTargetType() instanceof EdmEntityType) //TODO improve with correct error 
        uriResult.setFilter(new FilterParserImpl(edm, (EdmEntityType) uriResult.getTargetType()).parseExpression(filter));

    } catch (FilterParserException e) {
      throw new UriSyntaxException(UriSyntaxException.INVALIDFILTEREXPRESSION.addContent(filter), e);
    } catch (FilterParserInternalError e) {
      throw new UriSyntaxException(UriSyntaxException.INVALIDFILTEREXPRESSION.addContent(filter), e);
    }
  }

  private void handleSystemQueryOptionInlineCount(final String inlineCount) throws UriSyntaxException {
    if ("allpages".equals(inlineCount))
      uriResult.setInlineCount(InlineCount.ALLPAGES);
    else if ("none".equals(inlineCount))
      uriResult.setInlineCount(InlineCount.NONE);
    else
      throw new UriSyntaxException(UriSyntaxException.INVALIDVALUE);
  }

  private void handleSystemQueryOptionOrderBy(final String orderBy) throws UriSyntaxException, EdmException {
    // TODO: $orderby
    uriResult.setOrderBy(new OrderByExpression() {
      @Override
      public CommonExpression getOrdersCount() {
        return null;
      }

      @Override
      public List<CommonExpression> getOrders() {
        return null;
      }
    });
  }

  private void handleSystemQueryOptionSkipToken(final String skiptoken) throws UriSyntaxException {
    uriResult.setSkipToken(skiptoken);
  }

  private void handleSystemQueryOptionSkip(final String skip) throws UriSyntaxException {
    try {
      uriResult.setSkip(Integer.valueOf(skip));
    } catch (NumberFormatException e) {
      throw new UriSyntaxException(UriSyntaxException.INVALIDVALUE.addContent(skip), e);
    }
    if (uriResult.getSkip() < 0)
      throw new UriSyntaxException(UriSyntaxException.INVALIDNEGATIVEVALUE.addContent(skip));

  }

  private void handleSystemQueryOptionTop(final String top) throws UriSyntaxException {
    try {
      uriResult.setTop(Integer.valueOf(top));
    } catch (NumberFormatException e) {
      throw new UriSyntaxException(UriSyntaxException.INVALIDVALUE.addContent(top), e);
    }
    if (uriResult.getTop() < 0)
      throw new UriSyntaxException(UriSyntaxException.INVALIDNEGATIVEVALUE.addContent(top));
  }

  private void handleSystemQueryOptionExpand(final String expandStatement) throws UriSyntaxException, UriNotMatchingException, EdmException {
    ArrayList<ArrayList<NavigationPropertySegment>> expand = new ArrayList<ArrayList<NavigationPropertySegment>>();

    if (expandStatement.startsWith(",") || expandStatement.endsWith(","))
      throw new UriSyntaxException(UriSyntaxException.EMPTYSEGMENT);

    for (String expandItemString : expandStatement.split(",")) {
      expandItemString = expandItemString.trim();
      if ("".equals(expandItemString))
        throw new UriSyntaxException(UriSyntaxException.EMPTYSEGMENT);
      if (expandItemString.startsWith("/") || expandItemString.endsWith("/"))
        throw new UriSyntaxException(UriSyntaxException.EMPTYSEGMENT);

      ArrayList<NavigationPropertySegment> expandNavigationProperties = new ArrayList<NavigationPropertySegment>();
      EdmEntitySet fromEntitySet = uriResult.getTargetEntitySet();

      for (String expandPropertyName : expandItemString.split("/")) {
        if ("".equals(expandPropertyName))
          throw new UriSyntaxException(UriSyntaxException.EMPTYSEGMENT);

        final EdmTyped property = fromEntitySet.getEntityType().getProperty(expandPropertyName);
        if (property == null)
          throw new UriNotMatchingException(UriNotMatchingException.PROPERTYNOTFOUND.addContent(expandPropertyName));
        if (property.getType().getKind() == EdmTypeKind.ENTITY) {
          final EdmNavigationProperty navigationProperty = (EdmNavigationProperty) property;
          fromEntitySet = fromEntitySet.getRelatedEntitySet(navigationProperty);
          NavigationPropertySegmentImpl propertySegment = new NavigationPropertySegmentImpl();
          propertySegment.setNavigationProperty(navigationProperty);
          propertySegment.setTargetEntitySet(fromEntitySet);
          expandNavigationProperties.add(propertySegment);
        } else {
          throw new UriSyntaxException(UriSyntaxException.NONAVIGATIONPROPERTY.addContent(expandPropertyName));
        }
      }
      expand.add(expandNavigationProperties);
    }
    uriResult.setExpand(expand);
  }

  private void handleSystemQueryOptionSelect(final String selectStatement) throws UriSyntaxException, UriNotMatchingException, EdmException {
    ArrayList<SelectItem> select = new ArrayList<SelectItem>();

    if (selectStatement.startsWith(",") || selectStatement.endsWith(","))
      throw new UriSyntaxException(UriSyntaxException.EMPTYSEGMENT);

    for (String selectItemString : selectStatement.split(",")) {
      selectItemString = selectItemString.trim();
      if ("".equals(selectItemString))
        throw new UriSyntaxException(UriSyntaxException.EMPTYSEGMENT);
      if (selectItemString.startsWith("/") || selectItemString.endsWith("/"))
        throw new UriSyntaxException(UriSyntaxException.EMPTYSEGMENT);

      SelectItemImpl selectItem = new SelectItemImpl();
      boolean exit = false;
      EdmEntitySet fromEntitySet = uriResult.getTargetEntitySet();

      for (String selectedPropertyName : selectItemString.split("/")) {
        if ("".equals(selectedPropertyName))
          throw new UriSyntaxException(UriSyntaxException.EMPTYSEGMENT);

        if (exit)
          throw new UriSyntaxException(UriSyntaxException.INVALIDSEGMENT.addContent(selectItemString));

        if ("*".equals(selectedPropertyName)) {
          selectItem.setStar(true);
          exit = true;
          continue;
        }

        final EdmTyped property = fromEntitySet.getEntityType().getProperty(selectedPropertyName);
        if (property == null)
          throw new UriNotMatchingException(UriNotMatchingException.PROPERTYNOTFOUND.addContent(selectedPropertyName));

        switch (property.getType().getKind()) {
        case SIMPLE:
        case COMPLEX:
          selectItem.setProperty(property);
          exit = true;
          break;

        case ENTITY: // navigation properties point to entities
          final EdmNavigationProperty navigationProperty = (EdmNavigationProperty) property;
          final EdmEntitySet targetEntitySet = fromEntitySet.getRelatedEntitySet(navigationProperty);

          NavigationPropertySegmentImpl navigationPropertySegment = new NavigationPropertySegmentImpl();
          navigationPropertySegment.setNavigationProperty(navigationProperty);
          navigationPropertySegment.setTargetEntitySet(targetEntitySet);
          selectItem.addNavigationPropertySegment(navigationPropertySegment);

          fromEntitySet = targetEntitySet;
          break;

        default:
          throw new UriSyntaxException(UriSyntaxException.INVALIDPROPERTYTYPE);
        }
      }
      select.add(selectItem);
    }
    uriResult.setSelect(select);
  }

  private void handleOtherQueryParameters() throws UriSyntaxException, EdmException {
    final EdmFunctionImport functionImport = uriResult.getFunctionImport();

    if (functionImport != null)
      for (String parameterName : functionImport.getParameterNames()) {
        final EdmParameter parameter = functionImport.getParameter(parameterName);
        final String value = otherQueryParameters.remove(parameterName);
        if (value == null)
          if (parameter.getFacets() == null || parameter.getFacets().isNullable())
            continue;
          else
            throw new UriSyntaxException(UriSyntaxException.MISSINGPARAMETER);
        final EdmLiteral uriLiteral = simpleTypeFacade.parseUriLiteral(value);
        if (!((EdmSimpleType) parameter.getType()).isCompatible(uriLiteral.getType()))
          throw new UriSyntaxException(UriSyntaxException.INCOMPATIBLELITERAL);
        uriResult.addFunctionImportParameter(parameterName, uriLiteral);
      }

    uriResult.setCustomQueryOptions(otherQueryParameters);
  }

  private List<String> copyPathSegmentList(final List<ODataPathSegment> source) {
    List<String> copy = new ArrayList<String>();

    for (final ODataPathSegment segment : source)
      copy.add(segment.getPath());

    return copy;
  }
}
