package com.sap.core.odata.core.uri.expression;

import java.util.Vector;

import com.sap.core.odata.api.edm.EdmSimpleType;


/**
 * Parameter set is a vector of 1 or more EDM types, it is used to store the possible 
 * input and return types of a <i>OData filter operator</i> or <i>OData filter method</i>
 * @see InfoMethod TODO
 * @see InfoBinaryOperator TODO
 * @see InfoUnaryOperator TODO
 */
class ParameterSet //param_types_s
{
  private EdmSimpleType returnType;
  public Vector<EdmSimpleType> params = new Vector<EdmSimpleType>();

  public ParameterSet(EdmSimpleType returnType, EdmSimpleType type1)
  {
    this.returnType = returnType;
    params.add(type1);
  }

  public ParameterSet(EdmSimpleType returnType, EdmSimpleType type1, EdmSimpleType type2)
  {
    this.returnType = returnType;
    params.add(type1);
    params.add(type2);
  }

  public ParameterSet(EdmSimpleType returnType, EdmSimpleType type1, EdmSimpleType type2, EdmSimpleType type3)
  {
    params.add(returnType);
    params.add(type1);
    params.add(type2);
    params.add(type3);
  }

  public EdmSimpleType getReturnType()
  {
    return returnType;
  }

}
