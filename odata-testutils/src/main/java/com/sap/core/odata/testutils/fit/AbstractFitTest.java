package com.sap.core.odata.testutils.fit;

import java.net.URI;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.core.odata.api.exception.ODataException;
import com.sap.core.odata.api.processor.ODataProcessor;
import com.sap.core.odata.testutils.server.TestServer;

public abstract class AbstractFitTest {

  static {
    DOMConfigurator.configureAndWatch("log4j.xml");
  }

  protected final Logger log = LoggerFactory.getLogger(this.getClass());

  private TestServer server = new TestServer();
  private ODataProcessor processor;
  private HttpClient httpClient = new DefaultHttpClient();

  protected URI getEndpoint() {
    return this.server.getEndpoint();
  }

  protected HttpClient getHttpClient() {
    return httpClient;
  }

  protected ODataProcessor getProcessor() {
    return this.processor;
  }

  @Before
  public void before() throws Exception {
    this.processor = this.createProcessor();
    assertNotNull(this.processor);
    ProcessorFactory.setProcessor(this.processor);
    this.server.startServer(ProcessorFactory.class);
  }

  protected abstract ODataProcessor createProcessor() throws ODataException;

  @After
  public void after() throws Exception {
    try {
      this.server.stopServer();
    } finally {
      ProcessorFactory.setProcessor(null);
    }
  }
}
