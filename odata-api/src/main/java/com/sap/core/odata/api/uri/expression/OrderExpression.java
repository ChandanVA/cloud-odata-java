package com.sap.core.odata.api.uri.expression;

/**
 * Represents a order expression in the expression tree returned by the method 
 * <li>{@link OrderByParser#parseOrderExpression(String)}</li> 
 * <br>
 * <br>
 * <p>A order expression node is inserted in the expression tree for any valid
 * OData order. For example for "$orderby=age desc, name asc" two order expression node
 * will be inserted into the expression tree
 * <br>
 * <br>
 * @author SAP AG
 * @see OrderByParser
 */
public interface OrderExpression 
{
  
  /**
   * @return Returns the sort order (ascending or descending) of the order expression  
   */
  OrderType getSortOrder();
  
  /**
   * @return Returns the expression node which defines the data used to order the output
   * send back to the client. In the simplest case this would be a {@link PropertyExpression}.
   * @see CommonExpression
   */
  CommonExpression getExpression();
}
