package com.sap.core.odata.core.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sap.core.odata.api.enums.HttpStatusCodes;
import com.sap.core.odata.api.exception.ODataApplicationException;
import com.sap.core.odata.api.exception.ODataException;
import com.sap.core.odata.api.exception.ODataNotFoundException;
import com.sap.core.odata.api.uri.UriParserException;
import com.sap.core.odata.core.exception.ODataRuntimeException;

public class ODataExceptionMapperImplTest {

  ODataExceptionMapperImpl exceptionMapper;
  
  @Mock UriInfo mockedUriInfo;
  @Mock HttpHeaders mockedHttpHeaders;
  
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    exceptionMapper = new ODataExceptionMapperImpl();
    exceptionMapper.uriInfo = mockedUriInfo;
    exceptionMapper.httpHeaders = mockedHttpHeaders;
  }
  
  @Test
  public void testODataNotFoundException() {
    // prepare
    Exception exception = new ODataNotFoundException(ODataNotFoundException.ENTITY);
    
    // execute
    Response response = exceptionMapper.toResponse(exception);
    
    // verify
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatusCodes.NOT_FOUND.getStatusCode(), response.getStatus());
    // TOOD: adapt test if implementation is finished
    Assert.assertTrue(response.getEntity() instanceof String);
    Assert.assertEquals("Language = 'en', message = 'Requested entity could not be found.'.", response.getEntity().toString());
  }
  
  @Test
  public void testODataNotFoundExceptionDe() {
    // prepare
    Exception exception = new ODataNotFoundException(ODataNotFoundException.ENTITY);
    List<Locale> languages = new ArrayList<Locale>();
    languages.add(Locale.GERMAN);
    Mockito.when(mockedHttpHeaders.getAcceptableLanguages()).thenReturn(languages);
    
    // execute
    Response response = exceptionMapper.toResponse(exception);
    
    // verify
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatusCodes.NOT_FOUND.getStatusCode(), response.getStatus());
    // TOOD: adapt test if implementation is finished
    Assert.assertTrue(response.getEntity() instanceof String);
    Assert.assertEquals("Language = 'de', message = 'Die angefragte Entit\u00e4t wurde nicht gefunden.'.", response.getEntity().toString());
  }

  @Test
  public void testWrappedODataNotFoundException() {
    // prepare
    Exception causeException = new ODataNotFoundException(ODataNotFoundException.ENTITY);
    String exceptionMessage = "Some odd exception";
    Exception exception = new ODataException(exceptionMessage, causeException);
    
    // execute
    Response response = exceptionMapper.toResponse(exception);
    
    // verify
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatusCodes.NOT_FOUND.getStatusCode(), response.getStatus());
    // TOOD: adapt test if implementation is finished
    Assert.assertTrue(response.getEntity() instanceof String);
    Assert.assertEquals("Language = 'en', message = 'Requested entity could not be found.'.", response.getEntity().toString());
  }

  
  @Test
  public void testODataApplicationException() {
    // prepare
    String message = "expected exception message";
    Exception exception = new ODataApplicationException(message);
    
    // execute
    Response response = exceptionMapper.toResponse(exception);
    
    // verify
    Assert.assertNotNull(response);
    Assert.assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    // TOOD: adapt test if implementation is finished
    Assert.assertTrue(response.getEntity() instanceof String);
    Assert.assertEquals(message, response.getEntity().toString());
  }
  
  @Test
  public void testODataApplicationExceptionWrapped() {
    // prepare
    String message = "expected exception message";
    Exception exception = new ODataException(new ODataApplicationException(message));
    
    // execute
    Response response = exceptionMapper.toResponse(exception);
    
    // verify
    Assert.assertNotNull(response);
    Assert.assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    // TOOD: adapt test if implementation is finished
    Assert.assertTrue(response.getEntity() instanceof String);
    Assert.assertEquals(message, response.getEntity().toString());
  }

  @Test
  public void testODataApplicationExceptionWithStatus() {
    // prepare
    String message = "expected exception message";
    HttpStatusCodes status = HttpStatusCodes.OK;
    Exception exception = new ODataApplicationException(message, status);
    
    // execute
    Response response = exceptionMapper.toResponse(exception);
    
    // verify
    Assert.assertNotNull(response);
    Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
    // TOOD: adapt test if implementation is finished
    Assert.assertTrue(response.getEntity() instanceof String);
    Assert.assertEquals(message, response.getEntity().toString());
  }

  @Test
  public void testODataApplicationExceptionWithStatusWrapped() {
    // prepare
    String message = "expected exception message";
    HttpStatusCodes status = HttpStatusCodes.OK;
    Exception exception = new ODataException(new ODataApplicationException(message, status));
    
    // execute
    Response response = exceptionMapper.toResponse(exception);
    
    // verify
    Assert.assertNotNull(response);
    Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
    // TOOD: adapt test if implementation is finished
    Assert.assertTrue(response.getEntity() instanceof String);
    Assert.assertEquals(message, response.getEntity().toString());
  }

  @Test
  public void testUriParserException() {
    // prepare
    Exception exception = new UriParserException(UriParserException.EMPTYSEGMENT);
    
    // execute
    Response response = exceptionMapper.toResponse(exception);
    
    // verify
    Assert.assertNotNull(response);
    Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    // TOOD: adapt test if implementation is finished
//    Assert.assertTrue(response.getEntity() instanceof String);
//    Assert.assertEquals("", response.getEntity().toString());
  }

  @Test
  public void testUriParserExceptionWrapped() {
    // prepare
    Exception exception = new ODataException("outer exception", new UriParserException(UriParserException.EMPTYSEGMENT));
    
    // execute
    Response response = exceptionMapper.toResponse(exception);
    
    // verify
    Assert.assertNotNull(response);
    Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    // TOOD: adapt test if implementation is finished
//    Assert.assertTrue(response.getEntity() instanceof String);
//    Assert.assertEquals("", response.getEntity().toString());
  }

  
  @Test
  public void testIoException() {
    // prepare
    String message = "expected exception message";
    Exception exception = new IOException(message);
    
    // execute
    Response response = exceptionMapper.toResponse(exception);
    
    // verify
    Assert.assertNotNull(response);
    Assert.assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    // TOOD: adapt test if implementation is finished
    Assert.assertTrue(response.getEntity() instanceof String);
    Assert.assertEquals(exception.getClass().getName() + " - " + message, response.getEntity().toString());
  }

  
  @Test
  public void testODataException() {
    // prepare
    String exceptionMessage = "Some odd exception";
    Exception exception = new ODataException(exceptionMessage);
    
    // execute
    Response response = exceptionMapper.toResponse(exception);
    
    // verify
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatusCodes.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    // TOOD: adapt test if implementation is finished
    Assert.assertTrue(response.getEntity() instanceof String);
    Assert.assertEquals(ODataException.class.getName() + " - " + exceptionMessage, response.getEntity().toString());
  }

  @Test
  public void testODataRuntimeException() {
    // prepare
    String exceptionMessage = "Some odd runtime exception";
    Exception exception = new ODataRuntimeException(exceptionMessage);
    
    // execute
    Response response = exceptionMapper.toResponse(exception);
    
    // verify
    Assert.assertNotNull(response);
    Assert.assertEquals(HttpStatusCodes.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    // TOOD: adapt test if implementation is finished
    Assert.assertTrue(response.getEntity() instanceof String);
    Assert.assertEquals(exception.getClass().getName() + " - " + exceptionMessage, response.getEntity().toString());
  }
}
