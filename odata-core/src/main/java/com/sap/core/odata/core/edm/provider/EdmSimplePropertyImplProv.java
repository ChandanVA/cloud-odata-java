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

import com.sap.core.odata.api.edm.EdmException;
import com.sap.core.odata.api.edm.EdmType;
import com.sap.core.odata.api.edm.provider.SimpleProperty;
import com.sap.core.odata.core.edm.EdmSimpleTypeFacadeImpl;

public class EdmSimplePropertyImplProv extends EdmPropertyImplProv {

  private SimpleProperty property;

  public EdmSimplePropertyImplProv(final EdmImplProv edm, final SimpleProperty property) throws EdmException {
    super(edm, property.getType().getFullQualifiedName(), property);
    this.property = property;
  }

  @Override
  public EdmType getType() throws EdmException {
    if (edmType == null) {
      edmType = EdmSimpleTypeFacadeImpl.getEdmSimpleType(property.getType());
      if (edmType == null) {
        throw new EdmException(EdmException.COMMON);
      }
    }
    return edmType;
  }

  @Override
  public boolean isSimple() {
    return true;
  }
}
