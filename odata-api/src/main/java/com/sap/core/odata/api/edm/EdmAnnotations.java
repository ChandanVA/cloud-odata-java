package com.sap.core.odata.api.edm;

public interface EdmAnnotations {

  //TODO
  void getAnnotationElements();

  //TODO
  void getAnnotationElement(String name, String namespaceorprefix);

  //TODO
  void getAnnotationAttributes();

  String getAnnotationAttribute(String name, String namespace, String prefix);
}
