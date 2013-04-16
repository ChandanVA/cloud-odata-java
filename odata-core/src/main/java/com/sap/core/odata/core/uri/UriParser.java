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
import com.sap.core.odata.core.edm.EdmTypeEnum;
import com.sap.core.odata.core.edm.EdmTyped;
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
  private static final Pattern INITIAL_SELECT_PATTERN = Pattern.compile("^\\s*([^,]+)\\s*(?:,\\s*([^,].*))?$");
  private static final Pattern SELECT_PATTERN = Pattern.compile("^\\s*([^/]+)\\s*(?:/\\s*([^/].*))?$");
  
  
  private Edm edm;
  private List<PathSegment> pathSegments;
  private UriParserResult uriResult;
  private Map<SystemQueryOption, String> odataQueryParameters;
  private Map<String, String> otherQueryParameters;

  public UriParser(final Edm edm) {
    this.edm = edm;
    UriParser.LOG.debug("edm version: " + this.edm.getServiceMetadata().getDataServiceVersion());
  }

  public UriParserResult parse(final List<PathSegment> pathSegments, Map<String, String> queryParameters) throws UriParserException {
    UriParser.LOG.debug(pathSegments.toString());
    this.uriResult = new UriParserResult();

    this.pathSegments = pathSegments;

    handleResourcePath();

    this.odataQueryParameters = new HashMap<SystemQueryOption, String>();
    this.otherQueryParameters = new HashMap<String, String>();

    UriParser.LOG.debug("query parameters: " + queryParameters);
    for (String queryOptionString : queryParameters.keySet()) {
      try {
        if (queryOptionString.startsWith("$")) {
          SystemQueryOption queryOption = SystemQueryOption.valueOf(queryOptionString);
          this.odataQueryParameters.put(queryOption, queryParameters.get(queryOptionString));
        } else {
          otherQueryParameters.put(queryOptionString, queryParameters.get(queryOptionString));
        }
      } catch (IllegalArgumentException e) {
        throw new UriParserException("Illegal system query option: " + queryOptionString);
      }
    }

    checkQueryParameterCompatibility();
    handleQueryParameters();

    return uriResult;

  }

  private UriParserResult handleResourcePath() throws UriParserException {

    UriParser.LOG.debug("parsing: " + this.pathSegments);

    if (this.pathSegments.isEmpty()) {
      this.uriResult.setUriType(UriType.URI0);
      return this.uriResult;
    }

    String pathSegment = this.pathSegments.remove(0).getPath();

    if ("$metadata".equals(pathSegment)) {
      if (this.pathSegments.isEmpty()) {
        this.uriResult.setUriType(UriType.URI8);
        return this.uriResult;
      } else {
        throw new UriParserException("$metadata not last path segment: " + this.pathSegments);
      }
    }

    if ("$batch".equals(pathSegment)) {
      if (this.pathSegments.isEmpty()) {
        this.uriResult.setUriType(UriType.URI9);
        return this.uriResult;
      } else {
        throw new UriParserException("$batch not last path segment: " + this.pathSegments);
      }
    }

    final Matcher matcher = INITIAL_SEGMENT_PATTERN.matcher(pathSegment);
    if (!matcher.matches())
      throw new UriParserException("Problems matching segment " + pathSegment);

    final String entityContainerName = matcher.group(1);
    final String segmentName = matcher.group(2);
    final String keyPredicate = matcher.group(3);
    final String emptyParentheses = matcher.group(4);
    UriParser.LOG.debug("RegEx (" + pathSegment + ") : entityContainerName=" + entityContainerName + ", segmentName=" + segmentName + ", keyPredicate=" + keyPredicate + ", emptyParentheses=" + emptyParentheses);

    EdmEntityContainer entityContainer;
    if (entityContainerName != null) {
      entityContainer = this.edm.getEntityContainer(entityContainerName);
      if (entityContainer == null) {
        throw new UriParserException("Cannot find container with name: " + entityContainerName);
      }
    } else {
      entityContainer = this.edm.getDefaultEntityContainer();
    }

    this.uriResult.setEntityContainer(entityContainer);

    final EdmEntitySet entitySet = entityContainer.getEntitySet(segmentName);
    if (entitySet != null) {
      this.uriResult.setEntitySet(entitySet);
      this.uriResult = this.handleEntitySet(entitySet, keyPredicate);
    } else {
      final EdmFunctionImport functionImport = entityContainer.getFunctionImport(segmentName);
      if (functionImport == null)
        throw new UriParserException("cannot parse URI path segments: " + pathSegment + "/" + this.pathSegments);
      this.uriResult.setFunctionImport(functionImport);
      this.uriResult = this.handleFunctionImport(functionImport, emptyParentheses, keyPredicate);
    }

    return this.uriResult;
  }

  private UriParserResult handleEntitySet(final EdmEntitySet entitySet, final String keyPredicate) throws UriParserException {
    final EdmEntityType entityType = entitySet.getEntityType();

    this.uriResult.setTargetType(entityType);
    this.uriResult.setTargetEntitySet(entitySet);

    if (keyPredicate == null) {
      if (this.pathSegments.isEmpty()) {
        this.uriResult.setUriType(UriType.URI1);
      } else {
        final String pathSegment = this.pathSegments.remove(0).getPath();
        if ("$count".equals(pathSegment)) {
          if (this.pathSegments.isEmpty()) {
            this.uriResult.setUriType(UriType.URI15);
            this.uriResult.setCount(true);
          } else {
            throw new UriParserException("Not last path segment: " + pathSegment + ", " + this.pathSegments);
          }
        } else {
          throw new UriParserException("Entity set instead of entity: " + pathSegment + ", " + this.pathSegments);
        }
      }
    } else {
      this.uriResult.setKeyPredicates(this.parseKey(keyPredicate, entityType));
      if (this.pathSegments.isEmpty())
        this.uriResult.setUriType(UriType.URI2);
      else
        this.uriResult = this.handleNavigationPathOptions(entitySet);
    }

    return this.uriResult;
  }

  private UriParserResult handleNavigationPathOptions(final EdmEntitySet fromEntitySet) throws UriParserException {
    final String pathSegment = this.pathSegments.remove(0).getPath();

    if ("$count".equals(pathSegment)) {
      this.uriResult.setTargetType(fromEntitySet.getEntityType());
      this.uriResult.setTargetEntitySet(fromEntitySet);

      if (this.pathSegments.isEmpty()) {
        this.uriResult.setCount(true);
        if (this.uriResult.getNavigationSegments().isEmpty()) {
          this.uriResult.setUriType(UriType.URI16); // Entity set with key is handled elsewhere
        } else {
          NavigationSegment navigationSegmentEnd = this.uriResult.getNavigationSegments().get(this.uriResult.getNavigationSegments().size() - 1);
          if (navigationSegmentEnd.getNavigationProperty().getMultiplicity() == EdmMultiplicity.MANY)
            if (navigationSegmentEnd.getKeyPredicates().isEmpty())
              this.uriResult.setUriType(UriType.URI15);
            else
              this.uriResult.setUriType(UriType.URI16);
          else
            this.uriResult.setUriType(UriType.URI16);
        }
      } else {
        throw new UriParserException("Not last path segment: " + pathSegment + ", " + this.pathSegments);
      }

    } else if ("$value".equals(pathSegment)) {
      if (fromEntitySet.getEntityType().hasStream()) {
        if (this.pathSegments.isEmpty()) {
          this.uriResult.setUriType(UriType.URI17);
          this.uriResult.setTargetType(fromEntitySet.getEntityType());
          this.uriResult.setTargetEntitySet(fromEntitySet);
          this.uriResult.setValue(true);
        } else {
          throw new UriParserException("not last path segment: " + pathSegment + ", " + this.pathSegments);
        }
      } else {
        throw new UriParserException("Resource is no media resource");
      }

    } else if ("$links".equals(pathSegment)) {
      this.uriResult.setLinks(true);
      this.uriResult = this.handleNavigationProperties(null, fromEntitySet, null);

    } else {
      final Matcher matcher = NAVIGATION_SEGMENT_PATTERN.matcher(pathSegment);
      if (!matcher.matches())
        throw new UriParserException("Problems matching segment " + pathSegment);

      final String navigationPropertyName = matcher.group(1);
      final String keyPredicateName = matcher.group(2);
      final String emptyParentheses = matcher.group(3);

      final EdmTyped property = fromEntitySet.getEntityType().getProperty(navigationPropertyName);
      if (property == null)
        throw new UriParserException("Cannot find property: " + property);

      switch (property.getType().getKind()) {
      case SIMPLE:
      case COMPLEX:
        if (keyPredicateName != null || emptyParentheses != null)
          throw new UriParserException("Invalid segment: " + pathSegment + ", " + this.pathSegments);
        handlePropertyPath((EdmProperty) property);
        break;

      case NAVIGATION:
        final EdmNavigationProperty navigationProperty = (EdmNavigationProperty) property;
        if (keyPredicateName != null || emptyParentheses != null)
          if (navigationProperty.getMultiplicity() != EdmMultiplicity.MANY)
            throw new UriParserException("Invalid segment: " + pathSegment + ", " + this.pathSegments);
        handleNavigationProperties(navigationProperty, fromEntitySet, keyPredicateName);
        break;

      default:
        throw new UriParserException("Invalid property: " + pathSegment + ", " + this.pathSegments);
      }
    }

    return this.uriResult;
  }

  private UriParserResult handleNavigationProperties(final EdmNavigationProperty navigationProperty, final EdmEntitySet fromEntitySet, final String keyPredicateName) throws UriParserException {

    if (this.uriResult.isLinks()) {
      if (this.pathSegments.isEmpty())
        throw new UriParserException("$links must not be the last segment");

      String pathSegment = this.pathSegments.remove(0).getPath();

      final Matcher matcher = NAVIGATION_SEGMENT_PATTERN.matcher(pathSegment);
      if (!matcher.matches())
        throw new UriParserException("Problems matching segment " + pathSegment);

      final String navigationPropertyName = matcher.group(1);
      final String targetKeyPredicateName = matcher.group(2);
      final String emptyParentheses = matcher.group(3);
      UriParser.LOG.debug("RegEx (" + pathSegment + "): NavigationProperty=" + navigationPropertyName + ", keyPredicate=" + targetKeyPredicateName + ", emptyParentheses=" + emptyParentheses);

      final EdmTyped property = fromEntitySet.getEntityType().getProperty(navigationPropertyName);
      if (property == null)
        throw new UriParserException("Cannot find property with name: " + navigationPropertyName);

      if (property.getType().getKind() != EdmTypeEnum.NAVIGATION)
        throw new UriParserException("Invalid EDM type. Type is not a navigation type");

      if (!this.pathSegments.isEmpty()) {
        pathSegment = this.pathSegments.remove(0).getPath();
        if ("$count".equals(pathSegment))
          if (this.pathSegments.isEmpty())
            this.uriResult.setCount(true);
          else
            throw new UriParserException("Not last path segment: " + pathSegment + ", " + this.pathSegments);
        else
          throw new UriParserException("Invalid path segment: " + pathSegment + ", " + this.pathSegments);
      }

      final EdmNavigationProperty navProperty = (EdmNavigationProperty) property;

      boolean many = false;
      if (navProperty.getMultiplicity() == EdmMultiplicity.MANY)
        many = targetKeyPredicateName == null;
      else if (targetKeyPredicateName != null || emptyParentheses != null)
        throw new UriParserException("Navigation property " + navProperty.getName() + " must have no key predicate, " + this.pathSegments);
      if (many)
        if (this.uriResult.isCount())
          this.uriResult.setUriType(UriType.URI50B);
        else
          this.uriResult.setUriType(UriType.URI7B);
      else if (this.uriResult.isCount())
        this.uriResult.setUriType(UriType.URI50A);
      else
        this.uriResult.setUriType(UriType.URI7A);

      final EdmEntitySet targetEntitySet = fromEntitySet.getRelatedEntitySet(navProperty);
      this.uriResult.setTargetEntitySet(targetEntitySet);
      this.uriResult.setTargetType(targetEntitySet.getEntityType());

      NavigationSegment navigationSegment = new NavigationSegment();
      navigationSegment.setEntitySet(targetEntitySet);
      navigationSegment.setNavigationProperty(navProperty);
      if (targetKeyPredicateName != null)
        navigationSegment.setKeyPredicates(parseKey(targetKeyPredicateName, targetEntitySet.getEntityType()));
      this.uriResult.addNavigationSegment(navigationSegment);

    } else {
      EdmEntitySet targetEntitySet = fromEntitySet.getRelatedEntitySet(navigationProperty);
      this.uriResult.setTargetEntitySet(targetEntitySet);
      this.uriResult.setTargetType(targetEntitySet.getEntityType());

      NavigationSegment navigationSegment = new NavigationSegment();
      navigationSegment.setEntitySet(targetEntitySet);
      navigationSegment.setNavigationProperty(navigationProperty);
      if (keyPredicateName != null)
        navigationSegment.setKeyPredicates(parseKey(keyPredicateName, targetEntitySet.getEntityType()));
      this.uriResult.addNavigationSegment(navigationSegment);

      if (this.pathSegments.isEmpty()) {
        if (keyPredicateName == null && navigationProperty.getMultiplicity() == EdmMultiplicity.MANY)
          this.uriResult.setUriType(UriType.URI6B);
        else
          this.uriResult.setUriType(UriType.URI6A);
      } else {
        if (keyPredicateName == null && navigationProperty.getMultiplicity() == EdmMultiplicity.MANY && !"$count".equals(this.pathSegments.get(0).getPath()) && !"$links".equals(this.pathSegments.get(0).getPath()))
          throw new UriParserException("Not last path segment: " + navigationProperty.getName() + ", " + this.pathSegments);
        handleNavigationPathOptions(targetEntitySet);
      }
    }

    return this.uriResult;
  }

  private UriParserResult handlePropertyPath(final EdmProperty property) throws UriParserException {
    this.uriResult.addProperty(property);
    final EdmType type = property.getType();

    if (pathSegments.isEmpty()) {
      if (type.getKind() == EdmTypeEnum.SIMPLE)
        if (this.uriResult.getPropertyPath().size() == 1)
          this.uriResult.setUriType(UriType.URI5);
        else
          this.uriResult.setUriType(UriType.URI4);
      if (type.getKind() == EdmTypeEnum.COMPLEX)
        this.uriResult.setUriType(UriType.URI3);
      this.uriResult.setTargetType(type);
    } else {
      final String segment = this.pathSegments.remove(0).getPath();
      if (type.getKind() == EdmTypeEnum.SIMPLE) {
        if ("$value".equals(segment))
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
          throw new UriParserException("Invalid path segment: " + segment + ", " + this.pathSegments);
        this.uriResult.setTargetType(type);
      }
      if (type.getKind() == EdmTypeEnum.COMPLEX) {
        final EdmProperty nextProperty = (EdmProperty) ((EdmComplexType) type).getProperty(segment);
        if (nextProperty == null)
          throw new UriParserException("Invalid segment: " + segment);
        handlePropertyPath(nextProperty);
      }
    }

    return this.uriResult;
  }

  private ArrayList<KeyPredicate> parseKey(final String keyPredicate, final EdmEntityType entityType) throws UriParserException {
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
      else
        // if ("F".equalsIgnoreCase(suffix))
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
      else
        // if ("time".equals(prefix))
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

  private UriParserResult handleFunctionImport(final EdmFunctionImport functionImport, final String emptyParentheses, final String keyPredicate) throws UriParserException {
    final EdmTyped returnType = functionImport.getReturnType();
    final EdmType type = returnType.getType();
    final boolean isCollection = returnType.getMultiplicity() == EdmMultiplicity.MANY;

    if (type.getKind() == EdmTypeEnum.ENTITY && isCollection)
      return handleEntitySet(functionImport.getEntitySet(), keyPredicate);

    if (emptyParentheses != null)
      throw new UriParserException("Invalid segment");

    this.uriResult.setTargetType(type);
    switch (type.getKind()) {
    case SIMPLE:
      this.uriResult.setUriType(isCollection ? UriType.URI13 : UriType.URI14);
      break;
    case COMPLEX:
      this.uriResult.setUriType(isCollection ? UriType.URI11 : UriType.URI12);
      break;
    case ENTITY:
      this.uriResult.setUriType(UriType.URI10);
      break;
    default:
      throw new UriParserException("Invalid kind of function-import return type");
    }

    String segment = null;

    if (!this.pathSegments.isEmpty())
      if (this.uriResult.getUriType() == UriType.URI14) {
        segment = pathSegments.remove(0).getPath();
        if ("$value".equals(segment))
          this.uriResult.setValue(true);
        else
          throw new UriParserException("Invalid segment: " + segment);
      } else {
        segment = functionImport.getName();
      }

    if (!pathSegments.isEmpty())
      throw new UriParserException("Segment is not last segment: " + segment + ", " + this.pathSegments);

    return this.uriResult;
  }

  private void checkQueryParameterCompatibility() throws UriParserException {
    UriType uriType = uriResult.getUriType();

    for (SystemQueryOption queryOption : odataQueryParameters.keySet()) {

      if (uriType == UriType.URI4 && queryOption == SystemQueryOption.$format && uriResult.isValue() == true)
        throw new UriParserException("Query parameter : " + queryOption + " in combination with $value is not compatible with uri type :" + uriType);

      if (uriType == UriType.URI5 && queryOption == SystemQueryOption.$format && uriResult.isValue() == true)
        throw new UriParserException("Query parameter : " + queryOption + " in combination with $value is not compatible with uri type :" + uriType);

      if (!uriType.isCompatible(queryOption))
        throw new UriParserException("Query parameter : " + queryOption + " not compatible with uri type :" + uriType);
    }

  }

  private void handleQueryParameters() throws UriParserException {

    for (SystemQueryOption queryOption : odataQueryParameters.keySet()) {

      switch (queryOption) {
      case $format:
        handleSystemQueryOptionFormat(odataQueryParameters.get(SystemQueryOption.$format));
        break;
      case $filter:
        handleSystemQueryOptionFilter(odataQueryParameters.get(SystemQueryOption.$filter));
        break;
      case $inlinecount:
        handleSystemQueryOptionInlineCount(odataQueryParameters.get(SystemQueryOption.$inlinecount));
        break;
      case $orderby:
        handleSystemQueryOptionOrderBy(odataQueryParameters.get(SystemQueryOption.$orderby));
        break;
      case $skiptoken:
        handleSystemQueryOptionSkipToken(odataQueryParameters.get(SystemQueryOption.$skiptoken));
        break;
      case $skip:
        handleSystemQueryOptionSkip(odataQueryParameters.get(SystemQueryOption.$skip));
        break;
      case $top:
        handleSystemQueryOptionTop(odataQueryParameters.get(SystemQueryOption.$top));
        break;
      case $expand:
        handleSystemQueryOptionExpand(odataQueryParameters.get(SystemQueryOption.$expand));
        break;
      case $select:
        handleSystemQueryOptionSelect(odataQueryParameters.get(SystemQueryOption.$select));
        break;
      default:
        //This should never happen
        throw new UriParserException("Invalid queryparameter" + queryOption);
      }
    }

    handleFunctionImportParameters();

  }

  private void handleSystemQueryOptionSkipToken(String skiptoken) throws UriParserException {
    if ("".equals(skiptoken))
      throw new UriParserException("Invalid value Null for $skiptoken");

    uriResult.setSkipToken(skiptoken);
  }

  private void handleSystemQueryOptionOrderBy(String orderby) throws UriParserException {
    if ("".equals(orderby))
      throw new UriParserException("Invalid value Null for $orderby");
    // TODO Implement SystemQueryOption Orderby

  }

  private void handleSystemQueryOptionSelect(final String selectStatement) throws UriParserException {
    ArrayList<SelectItem> select = new ArrayList<SelectItem>();
    String selectStatementHelper = selectStatement;

    while (selectStatementHelper != null) {
      Matcher matcher = INITIAL_SELECT_PATTERN.matcher(selectStatementHelper);
      if (!matcher.matches())
        throw new UriParserException("Problems matching select statement " + selectStatement);

      String selectItemName = matcher.group(1);
      selectStatementHelper = matcher.group(2);

      SelectItem selectItem = new SelectItem();
      EdmTyped property = null;
      boolean exit = false;
      EdmEntitySet fromEntitySet = uriResult.getTargetEntitySet();

      while (selectItemName != null) {
        matcher = SELECT_PATTERN.matcher(selectItemName);
        if (!matcher.matches())
          throw new UriParserException("Problems matching select statement " + selectItemName);

        if (exit)
          throw new UriParserException("No segment after a simple or complex property allowed" + selectItemName);

        String segment = matcher.group(1);
        selectItemName = matcher.group(2);

        if ("*".equals(segment)) {
          selectItem.setStar(true);
          exit = true;
          continue;
        }

        property = fromEntitySet.getEntityType().getProperty(segment);
        if (property == null)
          throw new UriParserException("Can´t find property with name: " + segment);

        switch (property.getType().getKind()) {
        case SIMPLE:
        case COMPLEX:
          selectItem.setProperty(property);
          exit = true;
          break;
        case NAVIGATION:
          EdmNavigationProperty navigationProperty = (EdmNavigationProperty) property;
          EdmEntitySet targetEntitySet = fromEntitySet.getRelatedEntitySet(navigationProperty);

          NavigationPropertySegment navigationPropertySegment = new NavigationPropertySegment();
          navigationPropertySegment.setNavigationProperty(navigationProperty);
          navigationPropertySegment.setTargetEntitySet(targetEntitySet);
          selectItem.addNavigationPropertySegment(navigationPropertySegment);

          fromEntitySet = targetEntitySet;
          break;
        default:
          break;
        }
      }

      select.add(selectItem);
    }

    uriResult.setSelect(select);
  }

  private void handleSystemQueryOptionExpand(String expand) throws UriParserException {
    if ("".equals(expand))
      throw new UriParserException("Invalid value Null for $expand");
    // TODO:  Implement SystemQueryOption Expand

  }

  private void handleSystemQueryOptionFilter(final String filter) throws UriParserException {
    if ("".equals(filter))
      throw new UriParserException("Invalid value Null for $filter");
    //TODO: Implement SystemQueryOption Filter
  }

  private void handleSystemQueryOptionFormat(final String format) throws UriParserException {
    if ("atom".equals(format))
      uriResult.setFormat(Format.ATOM);
    else if ("json".equals(format))
      uriResult.setFormat(Format.JSON);
    else if ("xml".equals(format))
      uriResult.setFormat(Format.XML);
    else
      throw new UriParserException("Invalid value " + format + " for $format");
  }

  private void handleSystemQueryOptionInlineCount(final String inlineCount) throws UriParserException {
    if ("allpages".equals(inlineCount))
      uriResult.setInlineCount(InlineCount.ALLPAGES);
    else if ("none".equals(inlineCount))
      uriResult.setInlineCount(InlineCount.NONE);
    else
      throw new UriParserException("Invalid value " + inlineCount + " for $inlinecount");
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

  private void handleFunctionImportParameters() throws UriParserException {
    final EdmFunctionImport functionImport = uriResult.getFunctionImport();
    if (functionImport == null)
      return;

    for (String parameterName : functionImport.getParameterNames()) {
      final EdmParameter parameter = functionImport.getParameter(parameterName);
      final String value = otherQueryParameters.get(parameterName);
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
