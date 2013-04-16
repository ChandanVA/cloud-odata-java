/*******************************************************************************
 * Copyright 2013 SAP AG
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.sap.core.odata.core.edm.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sap.core.odata.api.edm.EdmAnnotatable;
import com.sap.core.odata.api.edm.EdmAnnotations;
import com.sap.core.odata.api.edm.EdmEntityContainer;
import com.sap.core.odata.api.edm.EdmEntitySet;
import com.sap.core.odata.api.edm.EdmException;
import com.sap.core.odata.api.edm.EdmFunctionImport;
import com.sap.core.odata.api.edm.EdmMapping;
import com.sap.core.odata.api.edm.EdmParameter;
import com.sap.core.odata.api.edm.EdmTyped;
import com.sap.core.odata.api.edm.provider.FunctionImport;
import com.sap.core.odata.api.edm.provider.FunctionImportParameter;
import com.sap.core.odata.api.edm.provider.ReturnType;

/**
 * @author SAP AG
 */
public class EdmFunctionImportImplProv extends EdmNamedImplProv implements EdmFunctionImport, EdmAnnotatable {

  private FunctionImport functionImport;
  private EdmEntityContainer edmEntityContainer;
  private Map<String, EdmParameter> edmParameters;
  private Map<String, FunctionImportParameter> parameters;
  private List<String> parametersList;

  public EdmFunctionImportImplProv(final EdmImplProv edm, final FunctionImport functionImport, final EdmEntityContainer edmEntityContainer) throws EdmException {
    super(edm, functionImport.getName());
    this.functionImport = functionImport;
    this.edmEntityContainer = edmEntityContainer;

    buildFunctionImportParametersInternal();

    edmParameters = new HashMap<String, EdmParameter>();
  }

  private void buildFunctionImportParametersInternal() {
    parameters = new HashMap<String, FunctionImportParameter>();

    List<FunctionImportParameter> parameters = functionImport.getParameters();
    if (parameters != null) {
      FunctionImportParameter functionImportParameter;
      for (Iterator<FunctionImportParameter> iterator = parameters.iterator(); iterator.hasNext();) {
        functionImportParameter = iterator.next();
        this.parameters.put(functionImportParameter.getName(), functionImportParameter);
      }
    }
  }

  @Override
  public EdmParameter getParameter(final String name) throws EdmException {
    EdmParameter parameter = null;
    if (edmParameters.containsKey(name)) {
      parameter = edmParameters.get(name);
    } else {
      parameter = createParameter(name);
    }

    return parameter;
  }

  private EdmParameter createParameter(final String name) throws EdmException {
    EdmParameter edmParameter = null;
    if (parameters.containsKey(name)) {
      FunctionImportParameter parameter = parameters.get(name);
      edmParameter = new EdmParameterImplProv(edm, parameter);
      edmParameters.put(name, edmParameter);
    }
    return edmParameter;
  }

  @Override
  public List<String> getParameterNames() throws EdmException {
    if (parametersList == null) {
      parametersList = new ArrayList<String>();

      Set<String> keySet = parameters.keySet();
      Iterator<String> iterator = keySet.iterator();
      while (iterator.hasNext()) {
        parametersList.add(iterator.next());
      }
    }

    return parametersList;
  }

  @Override
  public EdmEntitySet getEntitySet() throws EdmException {
    return edmEntityContainer.getEntitySet(functionImport.getEntitySet());
  }

  @Override
  public String getHttpMethod() throws EdmException {
    return functionImport.getHttpMethod();
  }

  @Override
  public EdmTyped getReturnType() throws EdmException {
    final ReturnType returnType = functionImport.getReturnType();
    return new EdmTypedImplProv(edm, functionImport.getName(), returnType.getTypeName(), returnType.getMultiplicity());
  }

  @Override
  public EdmEntityContainer getEntityContainer() throws EdmException {
    return edmEntityContainer;
  }

  @Override
  public EdmAnnotations getAnnotations() throws EdmException {
    return new EdmAnnotationsImplProv(functionImport.getAnnotationAttributes(), functionImport.getAnnotationElements());
  }

  @Override
  public EdmMapping getMapping() throws EdmException {
    return functionImport.getMapping();
  }
}
