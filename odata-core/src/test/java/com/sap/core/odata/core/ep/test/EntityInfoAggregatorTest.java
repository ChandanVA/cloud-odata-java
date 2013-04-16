package com.sap.core.odata.core.ep.test;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.sap.core.odata.api.edm.EdmEntitySet;
import com.sap.core.odata.core.ep.EntityInfoAggregator;
import com.sap.core.odata.testutils.mocks.MockFacade;


public class EntityInfoAggregatorTest extends AbstractSerializerTest {

  @Test
  public void simpleTest() throws Exception {
    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");

    EntityInfoAggregator eia = EntityInfoAggregator.init(entitySet);
    
    log.debug("Result:\n\t" + eia.getPropertyNames());
    log.debug("Result:\n\t" + eia.getTargetPaths());
    log.debug("Result:\n\t" + eia.getNavigationPropertyNames());
    
    assertNotNull(eia);
  }
  
//  @Test
//  public void simpleEtagTest() throws Exception {
//    EdmEntitySet entitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees");
//    Map<String, Object> data = this.employeeData;
//    DataContainer dc = new DataContainer(entitySet, data);
//    ODataItem it = dc.init();
//
//    log.debug("Result:\n\t" + it.toString());
//    
//    assertNotNull(it);
//    assertEquals("", dc.createETag());
//  }
}
