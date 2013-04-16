package com.sap.core.odata.core.uri.expression;

import java.util.List;

import com.sap.core.odata.api.edm.EdmType;
import com.sap.core.odata.api.uri.expression.BinaryOperator;

/**
 * Describes a binary operator which is allowed in OData expressions
 * @author SAP AG
 */
class InfoBinaryOperator
{
  private BinaryOperator operator;
  private String category;
  private String syntax;
  private int priority;
  ParameterSetCombination combination;

  public InfoBinaryOperator(BinaryOperator operator, String category, int priority, ParameterSetCombination combination) 
  {
    this.operator = operator;
    this.category = category;
    this.syntax = operator.toUriLiteral();
    this.priority = priority;
    this.combination = combination;
  }

  public String getCategory()
  {
    return this.category;
  }

  public String getSyntax()
  {
    return this.syntax;
  }

  public BinaryOperator getOperator()
  {
    return operator;
  }

  public int getPriority()
  {
    return priority;
  }

  public ParameterSet validateParameterSet(List<EdmType> actualParameterTypes) throws ExpressionParserInternalError 
  {
    return combination.validate(actualParameterTypes);
  }

}
