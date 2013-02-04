package com.sap.core.odata.processor.jpa.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.Metamodel;

import org.junit.BeforeClass;
import org.junit.Test;

import com.sap.core.odata.api.edm.FullQualifiedName;
import com.sap.core.odata.api.edm.provider.ComplexType;
import com.sap.core.odata.api.edm.provider.Mapping;
import com.sap.core.odata.api.edm.provider.Property;
import com.sap.core.odata.api.edm.provider.SimpleProperty;
import com.sap.core.odata.processor.jpa.api.exception.ODataJPAModelException;
import com.sap.core.odata.processor.jpa.api.model.JPAEdmMapping;
import com.sap.core.odata.processor.jpa.model.mock.JPAEmbeddableMock;
import com.sap.core.odata.processor.jpa.model.mock.JPAMetaModelMock;
import com.sap.core.odata.processor.jpa.model.mock.JPASingularAttributeMock;

public class JPAEdmComplexTypeTest extends JPAEdmTestModelView {
	
	private static JPAEdmComplexType objComplexType = null;
	private static JPAEdmComplexTypeTest localView = null;
	@BeforeClass
	public static void setup() throws ODataJPAModelException{
		localView = new JPAEdmComplexTypeTest();
		objComplexType = new JPAEdmComplexType(localView);
		objComplexType.getBuilder().build();
	}

	@Override
	public EmbeddableType<?> getJPAEmbeddableType() {
		@SuppressWarnings("hiding")
		class JPAComplexAttribute<Long> extends JPAEmbeddableMock<Long>
		{

			@SuppressWarnings("unchecked")
			@Override
			public Class<Long> getJavaType() {
				
				return (Class<Long>)java.lang.Long.class;
			}
			
		}
		return new JPAComplexAttribute();
	}

	@Override
	public String getpUnitName() {
		return "salesorderprocessing";
	}

	@Override
	public Metamodel getJPAMetaModel() {
		return new JPAEdmMetaModel();
	}

	

	@Test
	public void testGetBuilder() {
		
		assertNotNull(objComplexType.getBuilder());
	}

	@Test
	public void testGetEdmComplexType() {
		assertEquals(objComplexType.getEdmComplexType().getName(), "String");
	}

	@Test
	public void testSearchComplexTypeString() {
		assertNotNull(objComplexType.searchComplexType("java.lang.String"));
		
	}

	@Test
	public void testGetJPAEmbeddableType() {
		assertTrue(objComplexType.getJPAEmbeddableType().getAttributes().size() > 0);
		
	}

	@Test
	public void testGetConsistentEdmComplexTypes() {
		assertEquals(objComplexType.getConsistentEdmComplexTypes().size(), 1);
	}

	@Test
	public void testSearchComplexTypeFullQualifiedName() {
		assertNotNull(objComplexType.searchComplexType(new FullQualifiedName("salesorderprocessing", "String")));
		
	}
	@Test
	public void testSearchComplexTypeFullQualifiedNameNegative()
	{
		assertNull(objComplexType.searchComplexType(new FullQualifiedName("salesorderprocessing", "lang.String")));
	}

	@Test
	public void testAddCompleTypeView() {
		
		objComplexType.addCompleTypeView(localView);
		
	}

	@Test
	public void testExpandEdmComplexType() {
		ComplexType complexType = new ComplexType();
		List<Property> properties = new ArrayList<Property>();
		JPAEdmMapping mapping1 = new JPAEdmMappingImpl();
		mapping1.setColumnName("LINEITEMID");
		((Mapping)mapping1).setInternalName("LineItemKey.LiId");
		JPAEdmMapping mapping2 = new JPAEdmMappingImpl();
		mapping2.setColumnName("LINEITEMNAME");
		((Mapping)mapping2).setInternalName("LineItemKey.LiName");
		properties.add(new SimpleProperty().setName("LIID").setMapping((Mapping) mapping1));
		properties.add(new SimpleProperty().setName("LINAME").setMapping((Mapping) mapping2));
		complexType.setProperties(properties );
		List<Property> expandedList = null;
		try
		{
			objComplexType.expandEdmComplexType(complexType, expandedList,"SalesOrderItemKey");
		}
		catch(ClassCastException e)
		{
			assertTrue(false);
		}
		assertTrue(true);
		
		
	}
	
	@Test
	public void testComplexTypeCreation() throws ODataJPAModelException
	{
		objComplexType.getBuilder().build();
		assertEquals(objComplexType.pUnitName, "salesorderprocessing");
	}
	
	private class JPAEdmMetaModel extends JPAMetaModelMock
	{
		Set<EmbeddableType<?>> embeddableSet;
		
		public JPAEdmMetaModel() {
			embeddableSet = new HashSet<EmbeddableType<?>>();
		}
		
		@Override
		public Set<EmbeddableType<?>> getEmbeddables() {
			embeddableSet.add(new JPAEdmEmbeddable<String>());
			return embeddableSet;
		}
		
	}
	
	@SuppressWarnings("hiding")
	private class JPAEdmEmbeddable<String> extends JPAEmbeddableMock<String>
	{
 
		Set<Attribute<? super String, ?>> attributeSet = new HashSet<Attribute<? super String,?>>();
		@SuppressWarnings({ "unchecked", "rawtypes" })
		private void setValuesToSet()
		{
			attributeSet.add((Attribute< ? super String,String>)new JPAEdmAttribute(java.lang.String.class, "SOID"));
			attributeSet.add((Attribute< ? super String,String>)new JPAEdmAttribute(java.lang.String.class, "SONAME"));
		}
		@Override
		public Set<Attribute<? super String, ?>> getAttributes() {
			setValuesToSet();
			return attributeSet;
		}
		@SuppressWarnings("unchecked")
		@Override
		public Class<String> getJavaType() {
			return (Class<String>)java.lang.String.class;
		}
		
		
	}
	
	@SuppressWarnings("hiding")
	private class JPAEdmAttribute<Object,String> extends JPASingularAttributeMock<Object, String>
	{

		@Override
		public PersistentAttributeType getPersistentAttributeType() {
			return PersistentAttributeType.BASIC;
		}

		Class<String> clazz;
		java.lang.String attributeName;
		public JPAEdmAttribute(Class<String> javaType,java.lang.String name) {
			 this.clazz = javaType;
			 this.attributeName = name;
			
		}
		
		@Override
		public Class<String> getJavaType() {
			return clazz;
		}

		@Override
		public java.lang.String getName() {
			return this.attributeName;
		}

		@Override
		public boolean isId() {
			return false;
		}
		
		
	}
}
