package com.sap.core.odata.core.uri.expression;

import java.util.Vector;

import com.sap.core.odata.api.edm.EdmType;
import com.sap.core.odata.api.exception.ODataApplicationException;
import com.sap.core.odata.api.uri.expression.CommonExpression;
import com.sap.core.odata.api.uri.expression.ExceptionVisitExpression;
import com.sap.core.odata.api.uri.expression.ExpressionKind;
import com.sap.core.odata.api.uri.expression.ExpressionVisitor;
import com.sap.core.odata.api.uri.expression.MethodExpression;
import com.sap.core.odata.api.uri.expression.MethodOperator;

public class MethodExpressionImpl implements MethodExpression {

  private InfoMethod infoMethod;
  private EdmType returnType;
  private Vector<CommonExpression> actualParameters;

  public MethodExpressionImpl(InfoMethod infoMethod) {
    this.infoMethod = infoMethod;
    this.returnType = infoMethod.getReturnType();
    this.actualParameters = new Vector<CommonExpression>();
  }

  @Override
  public EdmType getEdmType() {
    return returnType;
  }

  @Override
  public CommonExpression setEdmType(EdmType edmType) {
    this.returnType = edmType;
    return this;
  }

  @Override
  public MethodOperator getMethod()
  {
    return infoMethod.getMethod();
  }

  @Override
  public Vector<CommonExpression> getParameters() {
    return actualParameters;
  }

  @Override
  public int getParameterCount() {
    return actualParameters.size();
  }

  public CommonExpression appendParameter(CommonExpression expression) {
    actualParameters.add(expression);
    return null;
  }

  @Override
  public ExpressionKind getKind() {
    return ExpressionKind.METHOD;
  }

  @Override
  public String getUriLiteral() {
    return infoMethod.getSyntax();
  }

  @Override
  public Object accept(ExpressionVisitor visitor) throws ExceptionVisitExpression, ODataApplicationException
  {
    Vector<Object> retParameters = new Vector<Object>();
    for (CommonExpression parameter : actualParameters)
    {
      Object retParameter = parameter.accept(visitor);
      retParameters.add(retParameter);
    }

    Object ret = visitor.visitMethod(this, this.getMethod(), retParameters);
    return ret;
  }

}
