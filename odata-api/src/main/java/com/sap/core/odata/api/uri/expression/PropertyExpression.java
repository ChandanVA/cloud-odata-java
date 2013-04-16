package com.sap.core.odata.api.uri.expression;

import com.sap.core.odata.api.edm.EdmProperty;

/**
 * Represents a property expression in the expression tree returned by the methods:
 * <li>{@link FilterParser#ParseExpression(String)}</li>
 * <li>{@link OrderByParser#parseOrderExpression(String)}</li> 
 * <br>
 * <br>
 * <p>A property expression node is inserted in the expression tree for any property.
 * If an EDM is available during parsing the property is automatically verified 
 * against the EDM.
 * <br>
 * <br>
 * @author SAP AG
 * @see FilterParser
 * @see OrderByParser
 */
public interface PropertyExpression extends CommonExpression
{
  /**
   * @return the property name as used in the EDM
   */
  public String getPropertyName();
  
  /**
   * @return Returns the EDM property matching the property name used in the expression String
   * @see EdmProperty    
   */
  public EdmProperty getEdmProperty();
  
}