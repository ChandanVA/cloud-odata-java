package com.sap.core.odata.api.uri.expression;


public interface FilterExpression extends CommonExpression
{
  String getFilterExpression();

  CommonExpression getExpression();
 
}
