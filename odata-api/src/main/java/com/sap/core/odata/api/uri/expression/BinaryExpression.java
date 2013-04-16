/**
 * (c) 2013 by SAP AG
 */
package com.sap.core.odata.api.uri.expression;

/**
 * Represents a binary expression node in the expression tree returned by the methods 
 * <li>{@link com.sap.core.odata.api.uri.UriParser#parseFilterString(com.sap.core.odata.api.edm.EdmEntityType, String)}</li>
 * <li>{@link com.sap.core.odata.api.uri.UriParser#parseOrderByString(com.sap.core.odata.api.edm.EdmEntityType, String)}</li>
 * <br> 
 * <br>
 * A binary expression node is inserted in the expression tree for any valid
 * ODATA binary operator in {@link BinaryOperator} (e.g. for "and", "div", "eg", ... )
 * <br>
 * @author SAP AG
 */
public interface BinaryExpression extends CommonExpression {
  /**
    * @return Operator object that represents the used operator
    * @see BinaryOperator
    */
  public BinaryOperator getOperator();

  /**
   * @return Expression sub tree of the left operand
   */
  public CommonExpression getLeftOperand();

  /**
   * @return Expression sub tree of the right operand
   */
  public CommonExpression getRightOperand();
}
