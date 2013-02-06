package com.sap.core.odata.processor.core.jpa.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.persistence.metamodel.Attribute;

import org.junit.BeforeClass;
import org.junit.Test;

import com.sap.core.odata.api.edm.FullQualifiedName;
import com.sap.core.odata.api.edm.provider.Association;
import com.sap.core.odata.api.edm.provider.AssociationEnd;
import com.sap.core.odata.processor.api.access.JPAEdmBuilder;
import com.sap.core.odata.processor.api.exception.ODataJPAModelException;
import com.sap.core.odata.processor.api.exception.ODataJPARuntimeException;
import com.sap.core.odata.processor.core.jpa.model.JPAEdmNavigationProperty;
import com.sap.core.odata.processor.core.jpa.model.mock.JPAAttributeMock;

public class JPAEdmNavigationPropertyTest extends JPAEdmTestModelView {

	private static JPAEdmNavigationProperty objNavigationProperty;
	private static JPAEdmNavigationPropertyTest navPropView;

	@BeforeClass
	public static void setup() {
		JPAEdmNavigationPropertyTest localView = new JPAEdmNavigationPropertyTest();
		navPropView = new JPAEdmNavigationPropertyTest();
		objNavigationProperty = new JPAEdmNavigationProperty(localView,
				localView);
		try {
			objNavigationProperty.getBuilder().build();
		} catch (ODataJPAModelException e) {
			fail("ODataJPAModelException not expected");
		} catch (ODataJPARuntimeException e) {
			fail("ODataJPARuntimeException not expected");
		}
	}

	@Override
	public String getpUnitName() {
		return "salesorderprocessing";
	}

	private Attribute<?, ?> getJPAAttributeLocal() {
		AttributeMock<Object, String> attr = new AttributeMock<Object, String>();
		return attr;
	}

	@Override
	public Attribute<?, ?> getJPAAttribute() {
		return getJPAAttributeLocal();
	}

	@Override
	public Association getEdmAssociation() {

		Association association = new Association();
		association.setName("Assoc_SalesOrderHeader_SalesOrderItem");
		association.setEnd1(new AssociationEnd().setType(
				new FullQualifiedName("salesorderprocessing", "String"))
				.setRole("SalesOrderHeader"));
		association.setEnd2(new AssociationEnd()
				.setType(
						new FullQualifiedName("salesorderprocessing",
								"SalesOrderItem")).setRole("SalesOrderItem"));
		return association;
	}

	@Test
	public void testGetBuilder() {
		assertNotNull(objNavigationProperty.getBuilder());

	}

	@Test
	public void testGetBuilderIdempotent() {
		JPAEdmBuilder builder1 = objNavigationProperty.getBuilder();
		JPAEdmBuilder builder2 = objNavigationProperty.getBuilder();

		assertEquals(builder1.hashCode(), builder2.hashCode());
	}

	@Test
	public void testGetEdmNavigationProperty() {
		assertEquals(
				objNavigationProperty.getEdmNavigationProperty().getName(),
				"StringDetails");
	}

	@Test
	public void testGetConsistentEdmNavigationProperties() {
		assertTrue(objNavigationProperty.getConsistentEdmNavigationProperties()
				.size() > 0);
	}

	@Test
	public void testAddJPAEdmNavigationPropertyView() {
		objNavigationProperty.addJPAEdmNavigationPropertyView(navPropView);
		assertTrue(objNavigationProperty.getConsistentEdmNavigationProperties()
				.size() > 1);
	}

	@Override
	public boolean isConsistent() {
		return true;
	}

	@Test
	public void testBuildNavigationProperty() throws ODataJPAModelException {

		try {
			objNavigationProperty.getBuilder().build();
		} catch (ODataJPARuntimeException e) {
			fail("Not expected");
		}
		assertEquals(objNavigationProperty.getEdmNavigationProperty()
				.getFromRole(), "SalesOrderItem");
		assertEquals(objNavigationProperty.getEdmNavigationProperty()
				.getToRole(), "SalesOrderHeader");

	}

	@SuppressWarnings("hiding")
	private class AttributeMock<Object, String> extends
			JPAAttributeMock<Object, String> {

		@SuppressWarnings("unchecked")
		@Override
		public Class<String> getJavaType() {
			return (Class<String>) java.lang.String.class;
		}

	}

}
