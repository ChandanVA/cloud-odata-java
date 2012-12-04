package com.sap.core.odata.testutils.mocks;

import com.sap.core.odata.api.exception.MessageReference;
import com.sap.core.odata.api.exception.ODataMessageException;

@SuppressWarnings("serial")
public class SampleClassForInvalidMessageReferences extends ODataMessageException
{
  public SampleClassForInvalidMessageReferences(MessageReference messageReference) {
    super(messageReference);
  }

  public static final MessageReference EXIST = createMessageReference(SampleClassForInvalidMessageReferences.class, "EXIST");
  public static final MessageReference DOES_NOT_EXIST = createMessageReference(SampleClassForInvalidMessageReferences.class, "DOES_NOT_EXIST");
  public static final MessageReference EXITS_BUT_EMPTY = createMessageReference(SampleClassForInvalidMessageReferences.class, "EXITS_BUT_EMPTY");
}
