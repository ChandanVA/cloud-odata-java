package com.sap.core.odata.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

public class ODataApplication extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> classes = new HashSet<Class<?>>();
    classes.add(ODataRootLocator.class);
    classes.add(ODataExceptionMapperImpl.class);
    return classes;
  }

  /**
   * Singletons are not recommended because they break the state less REST principle.
   */
  @Override
  public Set<Object> getSingletons() {
    return Collections.emptySet();
  }

}
