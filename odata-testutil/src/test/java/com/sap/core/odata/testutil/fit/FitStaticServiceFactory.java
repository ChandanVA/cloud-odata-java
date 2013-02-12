package com.sap.core.odata.testutil.fit;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.sap.core.odata.api.ODataService;
import com.sap.core.odata.api.ODataServiceFactory;
import com.sap.core.odata.api.exception.ODataException;
import com.sap.core.odata.api.processor.ODataContext;
import com.sap.core.odata.testutil.server.TestServer;

public class FitStaticServiceFactory extends ODataServiceFactory {

  private static Map<String, ODataService> HOST_2_SERVICE = Collections.synchronizedMap(new HashMap<String, ODataService>());
  
  public static void bindService(TestServer server, ODataService service) {
    HOST_2_SERVICE.put(createId(server), service);
  }

  public static void unbindService(TestServer server) {
    HOST_2_SERVICE.remove(createId(server));
  }

  @Override
  public ODataService createService(ODataContext ctx) throws ODataException {
    Map<String, String> requestHeaders = ctx.getHttpRequestHeaders();
    String host = requestHeaders.get("Host");
    // access and validation in synchronized block
    synchronized (HOST_2_SERVICE) {
      ODataService service = HOST_2_SERVICE.get(host);
      if (service == null) {
        throw new IllegalArgumentException("no static service set for JUnit test");
      }
      return service;
    }
  }

  private static String createId(TestServer server) {
    URI endpoint = server.getEndpoint();
    if(endpoint == null) {
      throw new IllegalArgumentException("Got TestServer without endpoint.");
    }
    return endpoint.getHost() + ":" + endpoint.getPort();
  }
}
