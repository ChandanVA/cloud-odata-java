package com.sap.core.odata.api.uri.expression;

public interface UnaryExpression extends CommonExpression 
{

  /**
   * @return Operator object that represent the operator
   * @see UnaryOperator
   */
  public UnaryOperator getOperator();

  public CommonExpression getoperand();

}