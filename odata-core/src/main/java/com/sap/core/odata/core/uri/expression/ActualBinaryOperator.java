package com.sap.core.odata.core.uri.expression;

public class ActualBinaryOperator {
  final protected InfoBinaryOperator operator;
  final protected Token token;

  public ActualBinaryOperator(final InfoBinaryOperator operatorInfo, final Token token) {
    if (operatorInfo == null) {
      throw new IllegalArgumentException("operatorInfo parameter must not be null");
    }

    operator = operatorInfo;
    this.token = token;
  }

  public Token getToken() {
    return token;
  }

  public InfoBinaryOperator getOP() {
    return operator;
  }

}
