package com.sap.core.odata.core.uri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.PathSegment;

import org.odata4j.repack.org.apache.commons.codec.DecoderException;
import org.odata4j.repack.org.apache.commons.codec.binary.Base64;
import org.odata4j.repack.org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.core.odata.core.edm.Edm;
import com.sap.core.odata.core.edm.EdmComplexType;
import com.sap.core.odata.core.edm.EdmEntityContainer;
import com.sap.core.odata.core.edm.EdmEntitySet;
import com.sap.core.odata.core.edm.EdmEntityType;
import com.sap.core.odata.core.edm.EdmFunctionImport;
import com.sap.core.odata.core.edm.EdmMultiplicity;
import com.sap.core.odata.core.edm.EdmNavigationProperty;
import com.sap.core.odata.core.edm.EdmParameter;
import com.sap.core.odata.core.edm.EdmProperty;
import com.sap.core.odata.core.edm.EdmSimpleType;
import com.sap.core.odata.core.edm.EdmType;
import com.sap.core.odata.core.edm.EdmTypeKind;
import com.sap.core.odata.core.edm.EdmTyped;
import com.sap.core.odata.core.exception.ODataError;
import com.sap.core.odata.core.uri.enums.Format;
import com.sap.core.odata.core.uri.enums.InlineCount;
import com.sap.core.odata.core.uri.enums.SystemQueryOption;
import com.sap.core.odata.core.uri.enums.UriType;

public class UriParser {

  private static final Logger LOG = LoggerFactory.getLogger(UriParser.class);

  private static final Pattern INITIAL_SEGMENT_PATTERN = Pattern.compile("(?:([^.()]+)\\.)?([^.()]+)(?:\\((.+)\\)|(\\(\\)))?");
  private static final Pattern NAVIGATION_SEGMENT_PATTERN = Pattern.compile("([^()]+)(?:\\((.+)\\)|(\\(\\)))?");
  private static final Pattern NAMED_VALUE_PATTERN = Pattern.compile("(?:([^=]+)=)?([^=]+)");
  private static final Pattern WHOLE_NUMBER_PATTERN = Pattern.compile("(-?\\p{Digit}+)([lL])?");
  private static final Pattern DECIMAL_NUMBER_PATTERN = Pattern.compile("(-?\\p{Digit}+(?:\\.\\p{Digit}*(?:[eE][-+]?\\p{Digit}+)?)?)([mMdDfF])");
  private static final Pattern STRING_VALUE_PATTERN = Pattern.compile("(X|binary|datetime|datetimeoffset|guid|time)?'(.*)'");

  private Edm edm;
  private List<PathSegment> pathSegments;
  private String currentPathSegment;
  private UriParserResult uriResult;
  private Map<SystemQueryOption, String> systemQueryOptions;
  private Map<String, String> otherQueryParameters;

  public UriParser(final Edm edm) {
    this.edm = edm;
    UriParser.LOG.debug("edm version: " + this.edm.getServiceMetadata().getDataServiceVersion());
  }

  /**
   * Parse the URI part after an OData service root,
   * already splitted into path segments and query parameters.
   * @param pathSegments  the segments of the resource path, already unescaped
   * @param queryParameters  the query parameters, already unescaped
   * @return a {@link UriParserResult} instance containing the parsed information
   * @throws UriParserException
   */
  public UriParserResult parse(final List<PathSegment> pathSegments, final Map<String, String> queryParameters) throws ODataError {
    UriParser.LOG.debug(pathSegments.toString());
    uriResult = new UriParserResult();

    this.pathSegments = pathSegments;
    preparePathSegments();
    handleResourcePath();

    this.systemQueryOptions = new HashMap<SystemQueryOption, String>();
    this.otherQueryParameters = new HashMap<String, String>();

    distributeQueryParameters(queryParameters);
    checkSystemQueryOptionsCompatibility();
    handleSystemQueryOptions();
    handleFunctionImportParameters();
    uriResult.setCustomQueryOptions(otherQueryParameters);

    UriParser.LOG.debug(uriResult.toString());
    return uriResult;
  }

  private void preparePathSegments() throws UriParserException {
    if (!pathSegments.isEmpty()) {
      if (pathSegments.get(0).getPath().equals("")) // initial '/' is allowed but ignored
        pathSegments.remove(0);
      if (pathSegments.size() == 1)
        if (pathSegments.get(0).getPath().equals("")) // only '/': service document
          pathSegments.remove(0);
      for (PathSegment pathSegment : pathSegments)
        if (pathSegment.getPath().equals(""))
          throw new UriParserException("Empty path segment in URI, " + pathSegments);
    }
  }

  private void handleResourcePath() throws ODataError {

    UriParser.LOG.debug("parsing: " + pathSegments);

    if (pathSegments.isEmpty()) {
      uriResult.setUriType(UriType.URI0);
      return;
    }

    currentPathSegment = pathSegments.remove(0).getPath();

    if ("$metadata".equals(currentPathSegment))
      if (this.pathSegments.isEmpty()) {
        uriResult.setUriType(UriType.URI8);
        return;
      } else {
        throw new UriParserException("$metadata not last path segment: " + this.pathSegments);
      }

    if ("$batch".equals(currentPathSegment))
      if (this.pathSegments.isEmpty()) {
        uriResult.setUriType(UriType.URI9);
        return;
      } else {
        throw new UriParserException("$batch not last path segment: " + this.pathSegments);
      }

    final Matcher matcher = INITIAL_SEGMENT_PATTERN.matcher(currentPathSegment);
    if (!matcher.matches())
      throw new UriParserException("Problems matching segment " + currentPathSegment);

    final String entityContainerName = matcher.group(1);
    final String segmentName = matcher.group(2);
    final String keyPredicate = matcher.group(3);
    final String emptyParentheses = matcher.group(4);
    UriParser.LOG.debug("RegEx (" + currentPathSegment + ") : entityContainerName=" + entityContainerName + ", segmentName=" + segmentName + ", keyPredicate=" + keyPredicate + ", emptyParentheses=" + emptyParentheses);

    determineEntityContainer(entityContainerName);
    final EdmEntitySet entitySet = uriResult.getEntityContainer().getEntitySet(segmentName);
    if (entitySet != null) {
      uriResult.setEntitySet(entitySet);
      handleEntitySet(entitySet, keyPredicate);
    } else {
      final EdmFunctionImport functionImport = uriResult.getEntityContainer().getFunctionImport(segmentName);
      if (functionImport == null)
        throw new UriParserException("cannot parse URI path segments: " + currentPathSegment + "/" + this.pathSegments);
      uriResult.setFunctionImport(functionImport);
      handleFunctionImport(functionImport, emptyParentheses, keyPredicate);
    }
  }

  private void determineEntityContainer(final String entityContainerName) throws ODataError {
    if (entityContainerName != null) {
      EdmEntityContainer entityContainer = edm.getEntityContainer(entityContainerName);
      if (entityContainer == null)
        throw new UriParserException("Cannot find container with name: " + entityContainerName);
      else
        uriResult.setEntityContainer(entityContainer);
    } else {
      uriResult.setEntityContainer(edm.getDefaultEntityContainer());
    }
  }

  private void handleEntitySet(final EdmEntitySet entitySet, final String keyPredicate) throws ODataError {
    final EdmEntityType entityType = entitySet.getEntityType();

    this.uriResult.setTargetType(entityType);
    this.uriResult.setTargetEntitySet(entitySet);

    if (keyPredicate == null) {
      if (this.pathSegments.isEmpty()) {
        this.uriResult.setUriType(UriType.URI1);
      } else {
        currentPathSegment = pathSegments.remove(0).getPath();
        checkCount();
        if (uriResult.isCount())
          uriResult.setUriType(UriType.URI15);
        else
          throw new UriParserException("Entity set instead of entity: " + currentPathSegment + ", " + this.pathSegments);
      }
    } else {
      this.uriResult.setKeyPredicates(this.parseKey(keyPredicate, entityType));
      if (this.pathSegments.isEmpty())
        this.uriResult.setUriType(UriType.URI2);
      else
        handleNavigationPathOptions();
    }
  }

  private void handleNavigationPathOptions() throws ODataError {
    currentPathSegment = pathSegments.remove(0).getPath();

    checkCount();
    if (uriResult.isCount()) {
      uriResult.setUriType(UriType.URI16); // Count of multiple entities is handled elsewhere

    } else if ("$value".equals(currentPathSegment)) {
      if (uriResult.getTargetEntitySet().getEntityType().hasStream())
        if (this.pathSegments.isEmpty()) {
          this.uriResult.setUriType(UriType.URI17);
          this.uriResult.setValue(true);
        } else {
          throw new UriParserException("not last path segment: " + currentPathSegment + ", " + this.pathSegments);
        }
      else
        throw new UriParserException("Resource is no media resource");

    } else if ("$links".equals(currentPathSegment)) {
      this.uriResult.setLinks(true);
      if (pathSegments.isEmpty())
        throw new UriParserException("$links must not be the last segment");
      currentPathSegment = pathSegments.remove(0).getPath();
      handleNavigationProperties();

    } else {
      handleNavigationProperties();
    }
  }

  private void handleNavigationProperties() throws ODataError {

    final Matcher matcher = NAVIGATION_SEGMENT_PATTERN.matcher(currentPathSegment);
    if (!matcher.matches())
      throw new UriParserException("Problems matching segment " + currentPathSegment);

    final String navigationPropertyName = matcher.group(1);
    final String keyPredicateName = matcher.group(2);
    final String emptyParentheses = matcher.group(3);
    UriParser.LOG.debug("RegEx (" + currentPathSegment + "): NavigationProperty=" + navigationPropertyName + ", keyPredicate=" + keyPredicateName + ", emptyParentheses=" + emptyParentheses);

    final EdmTyped property = uriResult.getTargetEntitySet().getEntityType().getProperty(navigationPropertyName);
    if (property == null)
      throw new UriParserException("Cannot find property with name: " + navigationPropertyName);

    switch (property.getType().getKind()) {
    case SIMPLE:
    case COMPLEX:
      if (keyPredicateName != null || emptyParentheses != null)
        throw new UriParserException("Invalid segment: " + currentPathSegment + ", " + this.pathSegments);
      if (uriResult.isLinks())
        throw new UriParserException("$links must be followed by a navigation property, but " + currentPathSegment + " is of another property type");
      else
        handlePropertyPath((EdmProperty) property);
      break;

    case NAVIGATION:
      final EdmNavigationProperty navigationProperty = (EdmNavigationProperty) property;
      if (keyPredicateName != null || emptyParentheses != null)
        if (navigationProperty.getMultiplicity() != EdmMultiplicity.MANY)
          throw new UriParserException("Invalid segment: " + currentPathSegment + ", " + this.pathSegments);

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
        currentPathSegment = pathSegments.remove(0).getPath();
        checkCount();
        if (!uriResult.isCount())
          throw new UriParserException("Invalid path segment: " + currentPathSegment + ", " + this.pathSegments);
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
      throw new UriParserException("Invalid type of property: " + currentPathSegment + ", " + pathSegments);
    }
  }

  private void addNavigationSegment(final String keyPredicateName, final EdmNavigationProperty navigationProperty) throws ODataError {
    final EdmEntitySet targetEntitySet = uriResult.getTargetEntitySet().getRelatedEntitySet(navigationProperty);
    uriResult.setTargetEntitySet(targetEntitySet);
    uriResult.setTargetType(targetEntitySet.getEntityType());

    NavigationSegment navigationSegment = new NavigationSegment();
    navigationSegment.setEntitySet(targetEntitySet);
    navigationSegment.setNavigationProperty(navigationProperty);
    if (keyPredicateName != null)
      navigationSegment.setKeyPredicates(parseKey(keyPredicateName, targetEntitySet.getEntityType()));
    uriResult.addNavigationSegment(navigationSegment);
  }

  private void handlePropertyPath(final EdmProperty property) throws ODataError {
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
        throw new UriParserException("Invalid type of property: " + currentPathSegment + ", " + pathSegments);
      this.uriResult.setTargetType(type);
    } else {

      currentPathSegment = pathSegments.remove(0).getPath();
      switch (type.getKind()) {
      case SIMPLE:
        if ("$value".equals(currentPathSegment))
          if (this.pathSegments.isEmpty()) {
            this.uriResult.setValue(true);
            if (this.uriResult.getPropertyPath().size() == 1)
              this.uriResult.setUriType(UriType.URI5);
            else
              this.uriResult.setUriType(UriType.URI4);
          } else {
            throw new UriParserException("$value is not the last segment");
          }
        else
          throw new UriParserException("Invalid path segment: " + currentPathSegment + ", " + this.pathSegments);
        this.uriResult.setTargetType(type);
        break;

      case COMPLEX:
        final EdmProperty nextProperty = (EdmProperty) ((EdmComplexType) type).getProperty(currentPathSegment);
        if (nextProperty == null)
          throw new UriParserException("Invalid segment: " + currentPathSegment);
        handlePropertyPath(nextProperty);
        break;

      default:
        throw new UriParserException("Invalid type of property: " + currentPathSegment + ", " + pathSegments);
      }
    }
  }

  private void checkCount() throws UriParserException {
    if ("$count".equals(currentPathSegment))
      if (pathSegments.isEmpty())
        uriResult.setCount(true);
      else
        throw new UriParserException("$count is not the last path segment, " + pathSegments);
  }

  private ArrayList<KeyPredicate> parseKey(final String keyPredicate, final EdmEntityType entityType) throws ODataError {
    ArrayList<KeyPredicate> keyPredicates = new ArrayList<KeyPredicate>();
    List<EdmProperty> keyProperties = entityType.getKeyProperties();
    final boolean singleKey = keyProperties.size() == 1;

    for (final String key : keyPredicate.split(",")) {

      final Matcher matcher = NAMED_VALUE_PATTERN.matcher(key);
      if (!matcher.matches())
        throw new UriParserException("Invalid key predicate: " + key);

      String name = matcher.group(1);
      final String value = matcher.group(2);

      UriParser.LOG.debug("RegEx (" + keyPredicate + "): name=" + name + ", value=" + value);

      if (name == null)
        if (singleKey)
          name = keyProperties.get(0).getName();
        else
          throw new UriParserException("Missing name in key predicate: " + keyPredicate);

      EdmProperty keyProperty = null;
      for (EdmProperty testKeyProperty : keyProperties)
        if (testKeyProperty.getName().equals(name)) {
          keyProperty = testKeyProperty;
          break;
        }
      if (keyProperty == null)
        throw new UriParserException("Invalid key predicate: " + keyPredicate);

      keyProperties.remove(keyProperty);

      final UriLiteral uriLiteral = parseUriLiteral(value);

      if (!isCompatible(uriLiteral, (EdmSimpleType) keyProperty.getType()))
        throw new UriParserException("Literal " + value + " is not compatible to type of property " + keyProperty.getName());

      keyPredicates.add(new KeyPredicate(uriLiteral.getLiteral(), keyProperty));
    }

    if (!keyProperties.isEmpty())
      throw new UriParserException("Invalid key predicate: " + keyPredicate);

    return keyPredicates;
  }

  private UriLiteral parseUriLiteral(final String uriLiteral) throws UriParserException {
    final String literal = uriLiteral;

    if ("true".equals(literal) || "false".equals(literal))
      return new UriLiteral(EdmSimpleType.BOOLEAN, literal);

    if (literal.length() >= 2)
      if (literal.startsWith("'") && literal.endsWith("'"))
        return new UriLiteral(EdmSimpleType.STRING, literal.substring(1, literal.length() - 1).replace("''", "'"));

    final Matcher wholeNumberMatcher = WHOLE_NUMBER_PATTERN.matcher(literal);
    if (wholeNumberMatcher.matches()) {
      final String value = wholeNumberMatcher.group(1);
      final String suffix = wholeNumberMatcher.group(2);
      UriParser.LOG.debug("RegEx (" + literal + "): value=" + value + ", suffix=" + suffix);

      if ("L".equalsIgnoreCase(suffix))
        return new UriLiteral(EdmSimpleType.INT64, value);
      else
        try {
          final int i = Integer.parseInt(value);
          if (i >= Byte.MIN_VALUE && i <= Byte.MAX_VALUE)
            return new UriLiteral(EdmSimpleType.SBYTE, value);
          else if (i > Byte.MAX_VALUE && i <= 255)
            return new UriLiteral(EdmSimpleType.BYTE, value);
          else if (i >= Short.MIN_VALUE && i <= Short.MAX_VALUE)
            return new UriLiteral(EdmSimpleType.INT16, value);
          else
            return new UriLiteral(EdmSimpleType.INT32, value);
        } catch (NumberFormatException e) {
          throw new UriParserException("Wrong format for literal value: " + uriLiteral, e);
        }
    }

    final Matcher decimalNumberMatcher = DECIMAL_NUMBER_PATTERN.matcher(literal);
    if (decimalNumberMatcher.matches()) {
      final String value = decimalNumberMatcher.group(1);
      final String suffix = decimalNumberMatcher.group(2);
      UriParser.LOG.debug("RegEx (" + literal + "): value=" + value + ", suffix=" + suffix);

      if ("M".equalsIgnoreCase(suffix))
        return new UriLiteral(EdmSimpleType.DECIMAL, value);
      else if ("D".equalsIgnoreCase(suffix))
        return new UriLiteral(EdmSimpleType.DOUBLE, value);
      else if ("F".equalsIgnoreCase(suffix))
        return new UriLiteral(EdmSimpleType.SINGLE, value);
    }

    final Matcher stringValueMatcher = STRING_VALUE_PATTERN.matcher(literal);
    if (stringValueMatcher.matches()) {
      final String prefix = stringValueMatcher.group(1);
      final String value = stringValueMatcher.group(2);
      UriParser.LOG.debug("RegEx (" + literal + "): prefix=" + prefix + ", value=" + value);

      if ("X".equals(prefix) || "binary".equals(prefix)) {
        byte[] b;
        try {
          b = Hex.decodeHex(value.toCharArray());
        } catch (DecoderException e) {
          throw new UriParserException(e);
        }
        return new UriLiteral(EdmSimpleType.BINARY, Base64.encodeBase64String(b));
      }
      if ("datetime".equals(prefix))
        return new UriLiteral(EdmSimpleType.DATETIME, value);
      else if ("datetimeoffset".equals(prefix))
        return new UriLiteral(EdmSimpleType.DATETIMEOFFSET, value);
      else if ("guid".equals(prefix))
        return new UriLiteral(EdmSimpleType.GUID, value);
      else if ("time".equals(prefix))
        return new UriLiteral(EdmSimpleType.TIME, value);
    }

    throw new UriParserException("Wrong format for literal value: " + uriLiteral);
  }

  private boolean isCompatible(final UriLiteral uriLiteral, final EdmSimpleType propertyType) throws UriParserException {
    final EdmSimpleType literalType = uriLiteral.getType();

    if (literalType.equals(propertyType))
      return true;

    switch (propertyType) {
    case BOOLEAN:
      return literalType == EdmSimpleType.SBYTE && (uriLiteral.getLiteral().equals("0") || uriLiteral.getLiteral().equals("1"));
    case BYTE:
      return literalType == EdmSimpleType.SBYTE && !uriLiteral.getLiteral().startsWith("-");
    case DECIMAL:
      return literalType == EdmSimpleType.BYTE || literalType == EdmSimpleType.SBYTE || literalType == EdmSimpleType.INT16 || literalType == EdmSimpleType.INT32 || literalType == EdmSimpleType.INT64 || literalType == EdmSimpleType.SINGLE
          || literalType == EdmSimpleType.DOUBLE;
    case DOUBLE:
      return literalType == EdmSimpleType.BYTE || literalType == EdmSimpleType.SBYTE || literalType == EdmSimpleType.INT16 || literalType == EdmSimpleType.INT32 || literalType == EdmSimpleType.INT64 || literalType == EdmSimpleType.SINGLE;
    case INT16:
      return literalType == EdmSimpleType.BYTE || literalType == EdmSimpleType.SBYTE;
    case INT32:
      return literalType == EdmSimpleType.BYTE || literalType == EdmSimpleType.SBYTE || literalType == EdmSimpleType.INT16;
    case INT64:
      return literalType == EdmSimpleType.BYTE || literalType == EdmSimpleType.SBYTE || literalType == EdmSimpleType.INT16 || literalType == EdmSimpleType.INT32;
    case SINGLE:
      return literalType == EdmSimpleType.BYTE || literalType == EdmSimpleType.SBYTE || literalType == EdmSimpleType.INT16 || literalType == EdmSimpleType.INT32 || literalType == EdmSimpleType.INT64;
    default:
      return false;
    }
  }

  private void handleFunctionImport(final EdmFunctionImport functionImport, final String emptyParentheses, final String keyPredicate) throws ODataError {
    final EdmTyped returnType = functionImport.getReturnType();
    final EdmType type = returnType.getType();
    final boolean isCollection = returnType.getMultiplicity() == EdmMultiplicity.MANY;

    if (type.getKind() == EdmTypeKind.ENTITY && isCollection) {
      handleEntitySet(functionImport.getEntitySet(), keyPredicate);
      return;
    }

    if (emptyParentheses != null)
      throw new UriParserException("Invalid segment");

    this.uriResult.setTargetType(type);
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
      throw new UriParserException("Invalid return type of function import: " + currentPathSegment + ", " + pathSegments);
    }

    if (!this.pathSegments.isEmpty())
      if (this.uriResult.getUriType() == UriType.URI14) {
        currentPathSegment = pathSegments.remove(0).getPath();
        if ("$value".equals(currentPathSegment))
          uriResult.setValue(true);
        else
          throw new UriParserException("Invalid segment: " + currentPathSegment);
      }

    if (!pathSegments.isEmpty())
      throw new UriParserException("Segment is not last segment: " + currentPathSegment + ", " + this.pathSegments);
  }

  private void distributeQueryParameters(final Map<String, String> queryParameters) throws UriParserException {
    UriParser.LOG.debug("query parameters: " + queryParameters);
    for (String queryOptionString : queryParameters.keySet())
      if (queryOptionString.startsWith("$")) {
        SystemQueryOption queryOption;
        try {
          queryOption = SystemQueryOption.valueOf(queryOptionString);
        } catch (IllegalArgumentException e) {
          throw new UriParserException("Illegal system query option: " + queryOptionString);
        }
        final String value = queryParameters.get(queryOptionString);
        if ("".equals(value))
          throw new UriParserException("Invalid null value for " + queryOptionString);
        else
          systemQueryOptions.put(queryOption, value);
      } else {
        otherQueryParameters.put(queryOptionString, queryParameters.get(queryOptionString));
      }
  }

  private void checkSystemQueryOptionsCompatibility() throws UriParserException {
    final UriType uriType = uriResult.getUriType();

    for (SystemQueryOption queryOption : systemQueryOptions.keySet()) {

      if (queryOption == SystemQueryOption.$format && (uriType == UriType.URI4 || uriType == UriType.URI5) && uriResult.isValue())
        throw new UriParserException("$format is not compatible with uri type " + uriType + " in combination with $value");

      if (!uriType.isCompatible(queryOption))
        throw new UriParserException("Query parameter " + queryOption + " is not compatible with uri type " + uriType);
    }
  }

  private void handleSystemQueryOptions() throws ODataError {

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
      }
  }

  private void handleSystemQueryOptionFormat(final String format) throws UriParserException {
    if ("atom".equals(format))
      uriResult.setFormat(Format.ATOM);
    else if ("json".equals(format))
      uriResult.setFormat(Format.JSON);
    else if ("xml".equals(format))
      uriResult.setFormat(Format.XML);
    else
      uriResult.setFormat(format);
  }

  private void handleSystemQueryOptionFilter(final String filter) throws UriParserException {
    //TODO: Implement SystemQueryOption Filter
    uriResult.setFilter(filter);
  }

  private void handleSystemQueryOptionInlineCount(final String inlineCount) throws UriParserException {
    if ("allpages".equals(inlineCount))
      uriResult.setInlineCount(InlineCount.ALLPAGES);
    else if ("none".equals(inlineCount))
      uriResult.setInlineCount(InlineCount.NONE);
    else
      throw new UriParserException("Invalid value " + inlineCount + " for $inlinecount");
  }

  private void handleSystemQueryOptionOrderBy(final String orderBy) throws UriParserException {
    // TODO: $orderby
    uriResult.setOrderBy(orderBy);
  }

  private void handleSystemQueryOptionSkipToken(final String skiptoken) throws UriParserException {
    uriResult.setSkipToken(skiptoken);
  }

  private void handleSystemQueryOptionSkip(final String skip) throws UriParserException {
    try {
      uriResult.setSkip(Integer.valueOf(skip));
    } catch (NumberFormatException e) {
      throw new UriParserException("Invalid value " + skip + " for $skip", e);
    }
    if (uriResult.getSkip() < 0)
      throw new UriParserException("$skip must not be negative");

  }

  private void handleSystemQueryOptionTop(final String top) throws UriParserException {
    try {
      uriResult.setTop(Integer.valueOf(top));
    } catch (NumberFormatException e) {
      throw new UriParserException("Invalid value " + top + " for $top", e);
    }
    if (uriResult.getTop() < 0)
      throw new UriParserException("$top must not be negative");
  }

  private void handleSystemQueryOptionExpand(String expandStatement) throws ODataError {
    ArrayList<ArrayList<NavigationPropertySegment>> expand = new ArrayList<ArrayList<NavigationPropertySegment>>();

    if (expandStatement.startsWith(",") || expandStatement.endsWith(","))
      throw new UriParserException("Empty item in expand statement " + expandStatement);

    for (String expandItemString : expandStatement.split(",")) {
      expandItemString = expandItemString.trim();
      if ("".equals(expandItemString))
        throw new UriParserException("Empty item in expand statement " + expandStatement);
      if (expandItemString.startsWith("/") || expandItemString.endsWith("/"))
        throw new UriParserException("Empty property in expand item " + expandItemString);

      ArrayList<NavigationPropertySegment> expandNavigationProperties = new ArrayList<NavigationPropertySegment>();
      EdmEntitySet fromEntitySet = uriResult.getTargetEntitySet();

      for (String expandPropertyName : expandItemString.split("/")) {
        if ("".equals(expandPropertyName))
          throw new UriParserException("Empty property name in expand item " + expandItemString);

        final EdmTyped property = fromEntitySet.getEntityType().getProperty(expandPropertyName);
        if (property == null)
          throw new UriParserException("Can't find property with name: " + expandPropertyName);
        if (property.getType().getKind() == EdmTypeKind.NAVIGATION) {
          final EdmNavigationProperty navigationProperty = (EdmNavigationProperty) property;
          fromEntitySet = fromEntitySet.getRelatedEntitySet(navigationProperty);
          NavigationPropertySegment propertySegment = new NavigationPropertySegment();
          propertySegment.setNavigationProperty(navigationProperty);
          propertySegment.setTargetEntitySet(fromEntitySet);
          expandNavigationProperties.add(propertySegment);
        } else {
          throw new UriParserException("Property: " + expandPropertyName + " has to be a navigation property");
        }
      }
      expand.add(expandNavigationProperties);
    }
    uriResult.setExpand(expand);
  }

  private void handleSystemQueryOptionSelect(final String selectStatement) throws ODataError {
    ArrayList<SelectItem> select = new ArrayList<SelectItem>();

    if (selectStatement.startsWith(",") || selectStatement.endsWith(","))
      throw new UriParserException("Empty item in select statement " + selectStatement);

    for (String selectItemString : selectStatement.split(",")) {
      selectItemString = selectItemString.trim();
      if ("".equals(selectItemString))
        throw new UriParserException("Empty item in select statement " + selectStatement);
      if (selectItemString.startsWith("/") || selectItemString.endsWith("/"))
        throw new UriParserException("Empty property in select item " + selectItemString);

      SelectItem selectItem = new SelectItem();
      boolean exit = false;
      EdmEntitySet fromEntitySet = uriResult.getTargetEntitySet();

      for (String selectedPropertyName : selectItemString.split("/")) {
        if ("".equals(selectedPropertyName))
          throw new UriParserException("Empty property name in select item " + selectItemString);

        if (exit)
          throw new UriParserException("After a simple or complex property, no further property is allowed: " + selectedPropertyName);

        if ("*".equals(selectedPropertyName)) {
          selectItem.setStar(true);
          exit = true;
          continue;
        }

        final EdmTyped property = fromEntitySet.getEntityType().getProperty(selectedPropertyName);
        if (property == null)
          throw new UriParserException("Can't find property with name: " + selectedPropertyName);

        switch (property.getType().getKind()) {
        case SIMPLE:
        case COMPLEX:
          selectItem.setProperty(property);
          exit = true;
          break;
        case NAVIGATION:
          final EdmNavigationProperty navigationProperty = (EdmNavigationProperty) property;
          final EdmEntitySet targetEntitySet = fromEntitySet.getRelatedEntitySet(navigationProperty);

          NavigationPropertySegment navigationPropertySegment = new NavigationPropertySegment();
          navigationPropertySegment.setNavigationProperty(navigationProperty);
          navigationPropertySegment.setTargetEntitySet(targetEntitySet);
          selectItem.addNavigationPropertySegment(navigationPropertySegment);

          fromEntitySet = targetEntitySet;
          break;
        default:
          throw new UriParserException("Invalid type of property: " + selectedPropertyName);
        }
      }
      select.add(selectItem);
    }
    uriResult.setSelect(select);
  }

  private void handleFunctionImportParameters() throws ODataError {
    final EdmFunctionImport functionImport = uriResult.getFunctionImport();
    if (functionImport == null)
      return;

    for (String parameterName : functionImport.getParameterNames()) {
      final EdmParameter parameter = functionImport.getParameter(parameterName);
      final String value = otherQueryParameters.remove(parameterName);
      if (value == null)
        if (parameter.getFacets() == null || parameter.getFacets().isNullable())
          continue;
        else
          throw new UriParserException("Mandatory function-import parameter missing: " + parameterName);
      final UriLiteral uriLiteral = parseUriLiteral(value);
      if (!isCompatible(uriLiteral, (EdmSimpleType) parameter.getType()))
        throw new UriParserException("Literal " + value + " is not compatible to type of function-import parameter " + parameterName);
      uriResult.addFunctionImportParameter(parameterName, uriLiteral);
    }
  }
}
