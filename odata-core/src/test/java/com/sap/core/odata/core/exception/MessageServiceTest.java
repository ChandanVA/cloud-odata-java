package com.sap.core.odata.core.exception;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Test;

import com.sap.core.odata.api.exception.MessageReference;
import com.sap.core.odata.api.exception.ODataMessageException;
import com.sap.core.odata.core.exception.MessageService.Message;

public class MessageServiceTest {

  private static final Locale DEFAULT_LANGUAGE = new Locale("test", "SAP");

  @Test
  public void test() throws Exception {
    MessageReference context = MessageReference.create(ODataMessageException.class, "COMMON");
    Message ms = MessageService.getMessage(DEFAULT_LANGUAGE, context);

    assertEquals("Common exception", ms.getText());
  }

  @Test
  public void testParameter() throws Exception {
    MessageReference context = MessageReference.create(ODataMessageException.class, "ONE_REPLACEMENTS").addContent("first");
    Message ms = MessageService.getMessage(DEFAULT_LANGUAGE, context);

    assertEquals("Only replacement is [first]!", ms.getText());
  }

  @Test
  public void testOneParameterForTwo() throws Exception {
    MessageReference context = MessageReference.create(ODataMessageException.class, "TWO_REPLACEMENTS").addContent("first");
    Message ms = MessageService.getMessage(DEFAULT_LANGUAGE, context);

    assertEquals("Missing replacement for place holder in value 'First was [%1$s] and second was [%2$s]!' for following arguments '[first]'!", ms.getText());
  }

  @Test
  public void testTwoParameters() throws Exception {
    MessageReference context = MessageReference.create(ODataMessageException.class, "TWO_REPLACEMENTS").addContent("first", "second");
    Message ms = MessageService.getMessage(DEFAULT_LANGUAGE, context);

    assertEquals("First was [first] and second was [second]!", ms.getText());
  }

  @Test
  public void testThreeParametersForTwo() throws Exception {
    MessageReference context = MessageReference.create(ODataMessageException.class, "TWO_REPLACEMENTS").addContent("first", "second", "third");
    Message ms = MessageService.getMessage(DEFAULT_LANGUAGE, context);

    assertEquals("First was [first] and second was [second]!", ms.getText());
  }
}
