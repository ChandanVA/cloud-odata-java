package com.sap.core.odata.core.exception;

import junit.framework.Assert;

import org.junit.Test;
import org.odata4j.exceptions.MethodNotAllowedException;

import com.sap.core.odata.api.exception.ODataException;
import com.sap.core.odata.api.exception.ODataNotFoundException;

public class ODataExceptionTest {

  @Test
  public void testNoCause() {
    ODataException exception = new ODataException("Some message.");

    Assert.assertEquals(false, exception.causedByContextedException());
  }

  @Test
  public void testNPECause() {
    ODataException exception = new ODataException("Some message.", new NullPointerException());

    Assert.assertEquals(false, exception.causedByContextedException());
  }

  @Test
  public void testODataContextedCause() {
    ODataException exception = new ODataException("Some message.", new ODataNotFoundException(ODataNotFoundException.ENTITY));

    Assert.assertEquals(true, exception.causedByContextedException());
  }

  @Test
  public void testODataContextedCauseLayer3() {
    ODataException exception = new ODataException("Some message.", 
        new IllegalArgumentException(
            new MethodNotAllowedException(
                new ODataNotFoundException(ODataNotFoundException.ENTITY))));

    Assert.assertEquals(true, exception.causedByContextedException());
  }
}
