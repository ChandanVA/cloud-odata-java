package com.sap.core.odata.api.uri.expression;

import com.sap.core.odata.api.edm.Edm;
import com.sap.core.odata.api.edm.EdmType;
import com.sap.core.odata.api.exception.ODataMessageException;
import com.sap.core.odata.api.rt.RuntimeDelegate;


public class ExpressionFacade
{
  public static FilterExpression parseFilter(Edm edm, EdmType edmType, String expression ) throws FilterParserException, ODataMessageException
  {
    return RuntimeDelegate.getFilterParser(edm, edmType).ParseExpression(expression);
  }

}
