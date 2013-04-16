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
package com.sap.core.odata.fit.ref;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;

import com.sap.core.odata.api.commons.HttpContentType;
import com.sap.core.odata.api.commons.HttpHeaders;
import com.sap.core.odata.api.commons.HttpStatusCodes;
import com.sap.core.odata.api.edm.Edm;

/**
 * Tests employing the reference scenario reading the service document in JSON format.
 * @author SAP AG
 */
public class ServiceJsonTest extends AbstractRefTest {
  @Test
  public void serviceDocumentDollarFormatJson() throws Exception {
    final HttpResponse response = callUri("?$format=json");
    // checkMediaType(response, HttpContentType.APPLICATION_JSON);
    assertEquals("{\"d\":{\"EntitySets\":["
        + "\"Employees\",\"Teams\",\"Rooms\",\"Managers\",\"Buildings\","
        + "\"Container2.Photos\"]}}",
        getBody(response));
  }

  @Test
  public void serviceDocumentAcceptHeaderJson() throws Exception {
    final HttpResponse response = callUri("", HttpHeaders.ACCEPT, HttpContentType.APPLICATION_JSON);
    assertEquals("{\"d\":{\"EntitySets\":["
        + "\"Employees\",\"Teams\",\"Rooms\",\"Managers\",\"Buildings\","
        + "\"Container2.Photos\"]}}",
        getBody(response));
  }

  @Test
  public void serviceDocumentAcceptHeaderInvalidCharset() throws Exception {
    final HttpResponse response = callUri("", HttpHeaders.ACCEPT, HttpContentType.APPLICATION_XML + "; charset=iso-latin-1", HttpStatusCodes.NOT_ACCEPTABLE);
    final String body = getBody(response);
    Map<String, String> prefixMap = new HashMap<String, String>();
    prefixMap.put("a", Edm.NAMESPACE_M_2007_08);
    XMLUnit.setXpathNamespaceContext(new SimpleNamespaceContext(prefixMap));
    assertXpathExists("/a:error", body);
    assertXpathExists("/a:error/a:code", body);
    assertXpathExists("/a:error/a:message", body);
  }
}
