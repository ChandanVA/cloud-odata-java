package com.sap.core.odata.core.edm;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sap.core.odata.api.edm.EdmFacets;
import com.sap.core.odata.api.edm.EdmLiteralKind;
import com.sap.core.odata.api.edm.EdmSimpleType;
import com.sap.core.odata.api.edm.EdmSimpleTypeException;

/**
 * Implementation of the EDM simple type Double
 * @author SAP AG
 */
public class EdmDouble extends AbstractSimpleType {

  // value-range limitations according to the CSDL document
  private static final int MAX_PRECISION = 15;
  private static final int MAX_SCALE = 308;

  private static final Pattern PATTERN = Pattern.compile(
      "(?:\\+|-)?\\p{Digit}{1,15}(?:\\.\\p{Digit}{1,15})?(?:(?:E|e)(?:\\+|-)?\\p{Digit}{1,3})?(D|d)?");
  private static final EdmDouble instance = new EdmDouble();

  public static EdmDouble getInstance() {
    return instance;
  }

  @Override
  public boolean isCompatible(final EdmSimpleType simpleType) {
    return simpleType instanceof Bit
        || simpleType instanceof Uint7
        || simpleType instanceof EdmByte
        || simpleType instanceof EdmSByte
        || simpleType instanceof EdmInt16
        || simpleType instanceof EdmInt32
        || simpleType instanceof EdmInt64
        || simpleType instanceof EdmSingle
        || simpleType instanceof EdmDouble;
  }

  @Override
  public Class<?> getDefaultType() {
    return Double.class;
  }

  @Override
  public <T> T valueOfString(final String value, final EdmLiteralKind literalKind, final EdmFacets facets, final Class<T> returnType) throws EdmSimpleTypeException {
    if (value == null) {
      checkNullLiteralAllowed(facets);
      return null;
    }

    if (literalKind == null) {
      throw new EdmSimpleTypeException(EdmSimpleTypeException.LITERAL_KIND_MISSING);
    }

    String valueString = value;
    Double result = null;
    // Handle special values first.
    if (value.equals("-INF")) {
      result = Double.NEGATIVE_INFINITY;
    } else if (value.equals("INF")) {
      result = Double.POSITIVE_INFINITY;
    } else if (value.equals("NaN")) {
      result = Double.NaN;
    } else {
      // Now only "normal" numbers remain.
      final Matcher matcher = PATTERN.matcher(value);
      if (!matcher.matches()
          || (literalKind == EdmLiteralKind.URI) == (matcher.group(1) == null)) {
        throw new EdmSimpleTypeException(EdmSimpleTypeException.LITERAL_ILLEGAL_CONTENT.addContent(value));
      }

      if (literalKind == EdmLiteralKind.URI) {
        valueString = value.substring(0, value.length() - 1);
      }

      // The number format is checked above, so we don't have to catch NumberFormatException.
      result = Double.valueOf(valueString);
      // "Real" infinite values have been treated already above, so we can throw an exception
      // if the conversion to a float results in an infinite value.
      if (result.isInfinite()) {
        throw new EdmSimpleTypeException(EdmSimpleTypeException.LITERAL_ILLEGAL_CONTENT.addContent(value));
      }
    }

    if (returnType.isAssignableFrom(Double.class)) {
      return returnType.cast(result);
    } else if (returnType.isAssignableFrom(Float.class)) {
      if (result.floatValue() == result) {
        return returnType.cast(result.floatValue());
      } else {
        throw new EdmSimpleTypeException(EdmSimpleTypeException.LITERAL_UNCONVERTIBLE_TO_VALUE_TYPE.addContent(value, returnType));
      }
    } else if (result.isInfinite() || result.isNaN()) {
      throw new EdmSimpleTypeException(EdmSimpleTypeException.LITERAL_UNCONVERTIBLE_TO_VALUE_TYPE.addContent(value, returnType));
    } else {
      try {
        final BigDecimal valueBigDecimal = new BigDecimal(valueString);
        if (returnType.isAssignableFrom(BigDecimal.class)) {
          return returnType.cast(valueBigDecimal);
        } else if (returnType.isAssignableFrom(Long.class)) {
          return returnType.cast(valueBigDecimal.longValueExact());
        } else if (returnType.isAssignableFrom(Integer.class)) {
          return returnType.cast(valueBigDecimal.intValueExact());
        } else if (returnType.isAssignableFrom(Short.class)) {
          return returnType.cast(valueBigDecimal.shortValueExact());
        } else if (returnType.isAssignableFrom(Byte.class)) {
          return returnType.cast(valueBigDecimal.byteValueExact());
        } else {
          throw new EdmSimpleTypeException(EdmSimpleTypeException.VALUE_TYPE_NOT_SUPPORTED.addContent(returnType));
        }

      } catch (ArithmeticException e) {
        throw new EdmSimpleTypeException(EdmSimpleTypeException.LITERAL_UNCONVERTIBLE_TO_VALUE_TYPE.addContent(value, returnType), e);
      }
    }
  }

  @Override
  public String valueToString(final Object value, final EdmLiteralKind literalKind, final EdmFacets facets) throws EdmSimpleTypeException {
    if (value == null) {
      return getNullOrDefaultLiteral(facets);
    }

    if (literalKind == null) {
      throw new EdmSimpleTypeException(EdmSimpleTypeException.LITERAL_KIND_MISSING);
    }

    String result;
    if (value instanceof Long) {
      if (Math.abs((Long) value) < Math.pow(10, MAX_PRECISION)) {
        result = value.toString();
      } else {
        throw new EdmSimpleTypeException(EdmSimpleTypeException.VALUE_ILLEGAL_CONTENT.addContent(value));
      }
    } else if (value instanceof Integer || value instanceof Short || value instanceof Byte) {
      result = value.toString();
    } else if (value instanceof Double) {
      if (((Double) value).isInfinite()) {
        return value.toString().toUpperCase(Locale.ROOT).substring(0, value.toString().length() - 5);
      } else {
        result = value.toString();
      }
    } else if (value instanceof Float) {
      if (((Float) value).isInfinite()) {
        return value.toString().toUpperCase(Locale.ROOT).substring(0, value.toString().length() - 5);
      } else {
        result = value.toString();
      }
    } else if (value instanceof BigDecimal) {
      if (((BigDecimal) value).precision() <= MAX_PRECISION && Math.abs(((BigDecimal) value).scale()) <= MAX_SCALE) {
        result = ((BigDecimal) value).toString();
      } else {
        throw new EdmSimpleTypeException(EdmSimpleTypeException.VALUE_ILLEGAL_CONTENT.addContent(value));
      }
    } else {
      throw new EdmSimpleTypeException(EdmSimpleTypeException.VALUE_TYPE_NOT_SUPPORTED.addContent(value.getClass()));
    }

    if (literalKind == EdmLiteralKind.URI) {
      result = toUriLiteral(result);
    }

    return result;
  }

  @Override
  public String toUriLiteral(final String literal) {
    return literal + "D";
  }
}
