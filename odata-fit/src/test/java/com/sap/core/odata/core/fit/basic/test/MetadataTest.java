package com.sap.core.odata.core.fit.basic.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;

import com.sap.core.odata.api.exception.ODataError;
import com.sap.core.odata.api.processor.aspect.Metadata;
import com.sap.core.odata.api.rest.ODataResponse;
import com.sap.core.testutils.StringHelper;

public class MetadataTest extends AbstractBasicTest {
  
  @Test
  public void readMetadata() throws ClientProtocolException, IOException, ODataError {
    
    Metadata metadata = this.getProcessor().getMetadataProcessor();
    when(metadata.readMetadata()).thenReturn(ODataResponse.status(200).entity("metadata").build());
    
    HttpGet get = new HttpGet(URI.create(this.getEndpoint().toString() + "$metadata"));
    HttpResponse response = this.getHttpClient().execute(get);
    
    String payload = StringHelper.inputStreamToString(response.getEntity().getContent());

    assertEquals("metadata", payload);
    assertEquals(200, response.getStatusLine().getStatusCode());
  }

}
