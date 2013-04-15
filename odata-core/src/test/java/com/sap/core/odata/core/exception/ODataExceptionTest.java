/**
 * (c) 2013 by SAP AG
 */
package com.sap.core.odata.core.exception;

import junit.framework.Assert;

import org.junit.Test;

import com.sap.core.odata.api.edm.EdmException;
import com.sap.core.odata.api.edm.EdmLiteralException;
import com.sap.core.odata.api.edm.EdmSimpleTypeException;
import com.sap.core.odata.api.exception.ODataApplicationException;
import com.sap.core.odata.api.exception.ODataBadRequestException;
import com.sap.core.odata.api.exception.ODataConflictException;
import com.sap.core.odata.api.exception.ODataException;
import com.sap.core.odata.api.exception.ODataForbiddenException;
import com.sap.core.odata.api.exception.ODataHttpException;
import com.sap.core.odata.api.exception.ODataMessageException;
import com.sap.core.odata.api.exception.ODataMethodNotAllowedException;
import com.sap.core.odata.api.exception.ODataNotAcceptableException;
import com.sap.core.odata.api.exception.ODataNotFoundException;
import com.sap.core.odata.api.exception.ODataNotImplementedException;
import com.sap.core.odata.api.exception.ODataPreconditionFailedException;
import com.sap.core.odata.api.exception.ODataPreconditionRequiredException;
import com.sap.core.odata.api.exception.ODataServiceUnavailableException;
import com.sap.core.odata.api.exception.ODataUnsupportedMediaTypeException;
import com.sap.core.odata.api.uri.UriNotMatchingException;
import com.sap.core.odata.api.uri.UriSyntaxException;
import com.sap.core.odata.testutil.fit.BaseTest;
import com.sap.core.odata.testutil.helper.ODataMessageTextVerifier;

/**
 * @author SAP AG
 */
public class ODataExceptionTest extends BaseTest {

  @Test
  public void testNoCause() {
    ODataException exception = new ODataException("Some message.");
    Assert.assertFalse(exception.isCausedByHttpException());
  }

  @Test
  public void testNPECause() {
    ODataException exception = new ODataException("Some message.", new NullPointerException());
    Assert.assertFalse(exception.isCausedByHttpException());
  }

  @Test
  public void testODataContextedCause() {
    ODataException exception = new ODataException("Some message.", new ODataNotFoundException(ODataNotFoundException.ENTITY));
    Assert.assertTrue(exception.isCausedByHttpException());
  }

  @Test
  public void testODataContextedCauseLayer3() {
    ODataException exception = new ODataException("Some message.",
        new IllegalArgumentException(
            new ODataNotFoundException(ODataNotFoundException.ENTITY)));
    Assert.assertTrue(exception.isCausedByHttpException());
  }

  //The following tests verify whether all fields of type {@link MessageReference} of 
  //the tested (Exception) class are provided in the <b>i18n.properties</b> file.
  @Test
  public void TestMessagesOfODataException() {
    ODataMessageTextVerifier.TestClass(ODataException.class);
  }

  @Test
  public void TestMessagesOfODataApplicationException() {
    ODataMessageTextVerifier.TestClass(ODataApplicationException.class);
  }

  @Test
  public void TestMessagesOfODataMessageException() {
    ODataMessageTextVerifier.TestClass(ODataMessageException.class);
  }

  @Test
  public void TestMessagesOfUriNotMatchingException() {
    ODataMessageTextVerifier.TestClass(UriNotMatchingException.class);
  }

  @Test
  public void TestMessagesOfUriSyntaxException() {
    ODataMessageTextVerifier.TestClass(UriSyntaxException.class);
  }

  @Test
  public void TestMessagesOfEdmLiteralException() {
    ODataMessageTextVerifier.TestClass(EdmLiteralException.class);
  }

  @Test
  public void TestMessagesOfEdmException() {
    ODataMessageTextVerifier.TestClass(EdmException.class);
  }

  @Test
  public void TestMessagesOfEdmSimpleTypeException() {
    ODataMessageTextVerifier.TestClass(EdmSimpleTypeException.class);
  }

  @Test
  public void TestMessagesOfODataHttpException() {
    ODataMessageTextVerifier.TestClass(ODataHttpException.class);
  }

  @Test
  public void TestMessagesOfODataBadRequestException() {
    ODataMessageTextVerifier.TestClass(ODataBadRequestException.class);
  }

  @Test
  public void TestMessagesOfODataConflictException() {
    ODataMessageTextVerifier.TestClass(ODataConflictException.class);
  }

  @Test
  public void TestMessagesOfODataForbiddenException() {
    ODataMessageTextVerifier.TestClass(ODataForbiddenException.class);
  }

  @Test
  public void TestMessagesOfODataNotFoundException() {
    ODataMessageTextVerifier.TestClass(ODataNotFoundException.class);
  }

  @Test
  public void TestMessagesOfODataMethodNotAllowedException() {
    ODataMessageTextVerifier.TestClass(ODataMethodNotAllowedException.class);
  }

  @Test
  public void TestMessagesOfODataNotAcceptableException() {
    ODataMessageTextVerifier.TestClass(ODataNotAcceptableException.class);
  }

  @Test
  public void TestMessagesOfODataPreconditionFailedException() {
    ODataMessageTextVerifier.TestClass(ODataPreconditionFailedException.class);
  }

  @Test
  public void TestMessagesOfODataPreconditionRequiredException() {
    ODataMessageTextVerifier.TestClass(ODataPreconditionRequiredException.class);
  }

  @Test
  public void TestMessagesOfODataServiceUnavailableException() {
    ODataMessageTextVerifier.TestClass(ODataServiceUnavailableException.class);
  }

  @Test
  public void TestMessagesOfODataUnsupportedMediaTypeException() {
    ODataMessageTextVerifier.TestClass(ODataUnsupportedMediaTypeException.class);
  }

  @Test
  public void TestMessagesOfODataNotImplementedException() {
    ODataMessageTextVerifier.TestClass(ODataNotImplementedException.class);
  }
}
