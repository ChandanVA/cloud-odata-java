package com.sap.core.odata.ref.test;

import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.PathSegment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.core.odata.core.producer.ODataProducer;
import com.sap.core.odata.core.rest.ODataRootLocator;
import com.sap.core.odata.core.rest.ODataLocator;
import com.sap.core.odata.ref.producer.ScenarioProducer;

@Path("/{segment1}/{segment2}")
public class TestRootLocator extends ODataRootLocator {

  private static final Logger log = LoggerFactory.getLogger(TestRootLocator.class);

  @Path("/{odataPathSegments: .*}")
  public ODataLocator getSubLocator(
      @PathParam("segment1") String segment1,
      @PathParam("segment2") String segment2,
      @PathParam("odataPathSegments") List<PathSegment> odataPathSegments
      ) {

    TestRootLocator.log.debug("+++ ScenarioRootLocator:getSubLocator()");
    TestRootLocator.log.debug("Path Segment 1: " + segment1);
    TestRootLocator.log.debug("Path Segment 2: " + segment2);
    TestRootLocator.log.debug("OData Segments: " + odataPathSegments);

    ODataLocator subLocator = super.getSubLocator(odataPathSegments);

    if (subLocator.getProducer() instanceof ScenarioProducer) {
      ScenarioProducer producer = (ScenarioProducer) subLocator.getProducer();
      producer.injectServiceResolutionPath(segment1, segment2);
    } else {
      ODataProducer producer = subLocator.getProducer();
      throw new RuntimeException("unsupported producer: " + producer != null ? producer.getClass().getCanonicalName() : null);
    }

    return subLocator;
  }

}
