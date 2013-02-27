package com.sap.core.odata.processor.core.jpa.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.persistence.metamodel.Attribute;

import org.junit.BeforeClass;
import org.junit.Test;

import com.sap.core.odata.api.edm.EdmMultiplicity;
import com.sap.core.odata.api.edm.FullQualifiedName;
import com.sap.core.odata.api.edm.provider.Association;
import com.sap.core.odata.api.edm.provider.AssociationEnd;
import com.sap.core.odata.api.edm.provider.EntityType;
import com.sap.core.odata.processor.api.jpa.exception.ODataJPAModelException;
import com.sap.core.odata.processor.api.jpa.exception.ODataJPARuntimeException;
import com.sap.core.odata.processor.api.jpa.model.JPAEdmReferentialConstraintView;
import com.sap.core.odata.processor.core.jpa.common.JPATestConstants;
import com.sap.core.odata.processor.core.jpa.mock.model.JPAAttributeMock;
import com.sap.core.odata.processor.core.jpa.mock.model.JPAEdmMockData.SimpleType;

public class JPAEdmAssociationTest extends JPAEdmTestModelView {

	private static JPAEdmAssociation objAssociation = null;
	private static String ASSOCIATION_NAME = "SalesOrderHeader_String";
	private static JPAEdmAssociationTest localView = null;

	@BeforeClass
	public static void setup() {
		localView = new JPAEdmAssociationTest();
		objAssociation = new JPAEdmAssociation(localView, localView, localView);
		try {
			objAssociation.getBuilder().build();
		} catch (ODataJPAModelException e) {
			fail(JPATestConstants.EXCEPTION_MSG_PART_1+e.getMessage()+ JPATestConstants.EXCEPTION_MSG_PART_2);
		} catch (ODataJPARuntimeException e) {
			fail(JPATestConstants.EXCEPTION_MSG_PART_1+e.getMessage()+ JPATestConstants.EXCEPTION_MSG_PART_2);
		}
	}

	@Override
	public AssociationEnd getEdmAssociationEnd1() {
		AssociationEnd associationEnd = new AssociationEnd();
		associationEnd.setType(new FullQualifiedName("salesorderprocessing",
				"SalesOrderHeader"));
		associationEnd.setRole("SalesOrderHeader");
		associationEnd.setMultiplicity(EdmMultiplicity.ONE);
		return associationEnd;
	}

	@Override
	public AssociationEnd getEdmAssociationEnd2() {
		AssociationEnd associationEnd = new AssociationEnd();
		associationEnd.setType(new FullQualifiedName("salesorderprocessing",
				"String"));
		associationEnd.setRole("String");
		associationEnd.setMultiplicity(EdmMultiplicity.MANY);
		return associationEnd;
	}

	@Override
	public Association getEdmAssociation() {
		Association association = new Association();
		association.setEnd1(new AssociationEnd().setType(new FullQualifiedName(
				"salesorderprocessing", "SalesOrderHeader")));
		association.setEnd2(new AssociationEnd().setType(new FullQualifiedName(
				"salesorderprocessing", "String")));

		return association;
	}

	@Override
	public boolean isExists() {
		return true;
	}

	@Override
	public JPAEdmReferentialConstraintView getJPAEdmReferentialConstraintView() {
		JPAEdmReferentialConstraint refConstraintView = new JPAEdmReferentialConstraint(
				localView, localView, localView);
		return refConstraintView;
	}

	@Test
	public void testGetBuilder() {
		assertNotNull(objAssociation.getBuilder());
	}

	@Test
	public void testGetEdmAssociation() {
		assertNotNull(objAssociation.getEdmAssociation());
		assertEquals(objAssociation.getEdmAssociation().getName(),
				ASSOCIATION_NAME);
	}

	@Test
	public void testGetConsistentEdmAssociationList() {
		assertTrue(objAssociation.getConsistentEdmAssociationList().size() > 0);
	}

	@Override
	public String getEdmRelationShipName() {
		return "Association_SalesOrderHeader_String";
	}

	@Test
	public void testSearchAssociation1() {
		class TestAssociationEndView extends JPAEdmTestModelView {
			private Attribute<?, ?> getJPAAttributeLocal() {
				AttributeMock<Object, String> attr = new AttributeMock<Object, String>();
				return attr;
			}

			@Override
			public Attribute<?, ?> getJPAAttribute() {
				return getJPAAttributeLocal();
			}

			@Override
			public String getpUnitName() {
				return "salesorderprocessing";
			}

			@Override
			public EntityType getEdmEntityType() {
				EntityType entityType = new EntityType();
				entityType.setName("SalesOrderHeader");
				return entityType;
			}

			@SuppressWarnings("hiding")
			class AttributeMock<Object, String> extends
					JPAAttributeMock<Object, String> {

				@SuppressWarnings("unchecked")
				@Override
				public Class<String> getJavaType() {
					return (Class<String>) SimpleType.SimpleTypeA.clazz;
				}

				@Override
				public PersistentAttributeType getPersistentAttributeType() {

					return PersistentAttributeType.ONE_TO_MANY;

				}

			}
		}
		TestAssociationEndView objJPAEdmAssociationEndTest = new TestAssociationEndView();
		JPAEdmAssociationEnd objJPAEdmAssociationEnd = new JPAEdmAssociationEnd(
				objJPAEdmAssociationEndTest, objJPAEdmAssociationEndTest);
		try {
			objJPAEdmAssociationEnd.getBuilder().build();
		} catch (ODataJPARuntimeException e) {
			fail(JPATestConstants.EXCEPTION_MSG_PART_1+e.getMessage()+ JPATestConstants.EXCEPTION_MSG_PART_2);
		} catch (ODataJPAModelException e) {
			fail(JPATestConstants.EXCEPTION_MSG_PART_1+e.getMessage()+ JPATestConstants.EXCEPTION_MSG_PART_2);
		}
		assertNotNull(objAssociation.searchAssociation(objJPAEdmAssociationEnd));

	}

	@Test
	public void testAddJPAEdmAssociationView() {

		class LocalJPAAssociationView extends JPAEdmTestModelView {
			@Override
			public AssociationEnd getEdmAssociationEnd1() {
				AssociationEnd associationEnd = new AssociationEnd();
				associationEnd.setType(new FullQualifiedName(
						"salesorderprocessing", "SalesOrderHeader"));
				associationEnd.setRole("SalesOrderHeader");
				associationEnd.setMultiplicity(EdmMultiplicity.ONE);
				return associationEnd;
			}

			@Override
			public AssociationEnd getEdmAssociationEnd2() {
				AssociationEnd associationEnd = new AssociationEnd();
				associationEnd.setType(new FullQualifiedName(
						"salesorderprocessing", "SalesOrderItem"));
				associationEnd.setRole("SalesOrderItem");
				associationEnd.setMultiplicity(EdmMultiplicity.MANY);
				return associationEnd;
			}

			@Override
			public Association getEdmAssociation() {
				Association association = new Association();
				association.setEnd1(new AssociationEnd()
						.setType(new FullQualifiedName("salesorderprocessing",
								"SalesOrderHeader")));
				association.setEnd2(new AssociationEnd()
						.setType(new FullQualifiedName("salesorderprocessing",
								"SalesOrderItem")));

				return association;
			}
		}
		LocalJPAAssociationView assocViewObj = new LocalJPAAssociationView();
		JPAEdmAssociation objLocalAssociation = new JPAEdmAssociation(
				assocViewObj, assocViewObj, assocViewObj);
		try {
			objLocalAssociation.getBuilder().build();
		} catch (ODataJPARuntimeException e) {
			fail(JPATestConstants.EXCEPTION_MSG_PART_1+e.getMessage()+ JPATestConstants.EXCEPTION_MSG_PART_2);
		} catch (ODataJPAModelException e) {
			fail(JPATestConstants.EXCEPTION_MSG_PART_1+e.getMessage()+ JPATestConstants.EXCEPTION_MSG_PART_2);
		}
		objAssociation.addJPAEdmAssociationView(objLocalAssociation);

	}

	@Test
	public void testAddJPAEdmRefConstraintView() {

		localView = new JPAEdmAssociationTest();
		objAssociation = new JPAEdmAssociation(localView, localView, localView);
		try {
			objAssociation.getBuilder().build();
		} catch (ODataJPAModelException e) {
			fail(JPATestConstants.EXCEPTION_MSG_PART_1+e.getMessage()+ JPATestConstants.EXCEPTION_MSG_PART_2);
		} catch (ODataJPARuntimeException e) {
			fail(JPATestConstants.EXCEPTION_MSG_PART_1+e.getMessage()+ JPATestConstants.EXCEPTION_MSG_PART_2);
		}
		
		objAssociation.addJPAEdmRefConstraintView(localView);
		assertTrue(objAssociation.getConsistentEdmAssociationList()
				.size() > 0);
	}

	@Test
	public void testGetJPAEdmReferentialConstraintView() {

	}
}
