package com.sap.core.odata.processor.jpa.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.metamodel.EmbeddableType;

import com.sap.core.odata.api.edm.FullQualifiedName;
import com.sap.core.odata.api.edm.provider.ComplexProperty;
import com.sap.core.odata.api.edm.provider.ComplexType;
import com.sap.core.odata.api.edm.provider.Mapping;
import com.sap.core.odata.api.edm.provider.Property;
import com.sap.core.odata.api.edm.provider.SimpleProperty;
import com.sap.core.odata.processor.jpa.access.model.JPAEdmNameBuilder;
import com.sap.core.odata.processor.jpa.api.access.JPAEdmBuilder;
import com.sap.core.odata.processor.jpa.api.exception.ODataJPAModelException;
import com.sap.core.odata.processor.jpa.api.model.JPAEdmComplexTypeView;
import com.sap.core.odata.processor.jpa.api.model.JPAEdmMapping;
import com.sap.core.odata.processor.jpa.api.model.JPAEdmPropertyView;
import com.sap.core.odata.processor.jpa.api.model.JPAEdmSchemaView;

public class JPAEdmComplexType extends JPAEdmBaseViewImpl implements
		JPAEdmComplexTypeView {

	private JPAEdmSchemaView schemaView;
	private ComplexType currentComplexType = null;
	private EmbeddableType<?> currentEmbeddableType = null;
	private HashMap<String, ComplexType> searchMap = null;
	private List<ComplexType> consistentComplextTypes = null;

	public JPAEdmComplexType(JPAEdmSchemaView view) {
		super(view);
		this.schemaView = view;
	}

	@Override
	public JPAEdmBuilder getBuilder() {
		if (this.builder == null)
			this.builder = new JPAEdmComplexTypeBuilder();
		
		return builder;
	}

	@Override
	public ComplexType getEdmComplexType() {
		return currentComplexType;
	}

	@Override
	public ComplexType searchEdmComplexType(String embeddableTypeName) {
		return searchMap.get(embeddableTypeName);
	}

	@Override
	public EmbeddableType<?> getJPAEmbeddableType() {
		return currentEmbeddableType;
	}

	@Override
	public List<ComplexType> getConsistentEdmComplexTypes() {
		return consistentComplextTypes;
	}
	
	@Override
	public ComplexType searchEdmComplexType(FullQualifiedName type) {
		String name = type.getName();
		return searchComplexTypeByName(name);

	}
	
	private ComplexType searchComplexTypeByName(String name){
		for (ComplexType complexType : consistentComplextTypes)
			if (null != complexType && null != complexType.getName() && complexType.getName().equals(name))
				return complexType;

		return null;
	}
	
	@Override
	public void addJPAEdmCompleTypeView(JPAEdmComplexTypeView view) {
		String searchKey = view.getJPAEmbeddableType().getJavaType().getName();

		if (!searchMap.containsKey(searchKey)) {
			consistentComplextTypes.add(view.getEdmComplexType());
			searchMap.put(searchKey, view.getEdmComplexType());
		}
	}
	
	@Override
	public void expandEdmComplexType(ComplexType complexType,List<Property> expandedList,String embeddablePropertyName){
		
		if(expandedList == null) expandedList = new ArrayList<Property>();
		for(Property property : complexType.getProperties())
		{
			try{
			SimpleProperty newSimpleProperty = new SimpleProperty();	
			SimpleProperty oldSimpleProperty = (SimpleProperty) property;
			newSimpleProperty.setAnnotationAttributes(oldSimpleProperty.getAnnotationAttributes());
			newSimpleProperty.setAnnotationElements(oldSimpleProperty.getAnnotationElements());
			newSimpleProperty.setCustomizableFeedMappings(oldSimpleProperty.getCustomizableFeedMappings());
			newSimpleProperty.setDocumentation(oldSimpleProperty.getDocumentation());
			newSimpleProperty.setFacets(oldSimpleProperty.getFacets());
			newSimpleProperty.setMimeType(oldSimpleProperty.getMimeType());
			newSimpleProperty.setName(oldSimpleProperty.getName());
			newSimpleProperty.setType(oldSimpleProperty.getType());
			JPAEdmMappingImpl newMapping = new JPAEdmMappingImpl();
			Mapping mapping = oldSimpleProperty.getMapping();
			JPAEdmMapping oldMapping = (JPAEdmMapping)mapping;
			newMapping.setJPAColumnName(oldMapping.getJPAColumnName());
			newMapping.setInternalName(embeddablePropertyName + "." + mapping.getInternalName());
			newMapping.setMimeType(mapping.getMimeType());
			newMapping.setObject(mapping.getObject());
			newSimpleProperty.setMapping(newMapping);
			expandedList.add(newSimpleProperty);
			}catch (ClassCastException e){
				ComplexProperty complexProperty = (ComplexProperty) property;
				String name = complexProperty.getMapping().getInternalName();
				expandEdmComplexType(searchComplexTypeByName(complexProperty.getName()),expandedList,name);
			}
		}
		
	}

	private class JPAEdmComplexTypeBuilder implements JPAEdmBuilder {
		/*
		 * 
		 * Each call to build method creates a new Complex Type.
		 * The Complex Type is created only if it is not created
		 * earlier. A local buffer is maintained to track the list
		 * of complex types created.
		 *  
		 * ************************************************************
		 * 				Build EDM Complex Type - STEPS
		 * ************************************************************
		 * 1) Fetch list of embeddable types from JPA Model
		 * 2) Search local buffer if there exists already a Complex 
		 * type for the embeddable type. 
		 * 3) If the complex type was already been built continue with
		 * the next embeddable type, else create new EDM Complex Type.
		 * 4) Create a Property view with Complex Type
		 * 5) Get Property Builder and build the Property with Complex
		 * type.
		 * 6) Set EDM complex type with list of properties built by
		 * the property view
		 * 7) Provide name for EDM complex type.
		 * 
		 * ************************************************************
		 * 				Build EDM Complex Type - STEPS
		 * ************************************************************
		 *
		 */
		@Override
		public void build() throws ODataJPAModelException {

			if (consistentComplextTypes == null)
				consistentComplextTypes = new ArrayList<ComplexType>();

			if (searchMap == null)
				searchMap = new HashMap<String, ComplexType>();

			for (EmbeddableType<?> embeddableType : schemaView
					.getJPAMetaModel().getEmbeddables()) {
				
				currentEmbeddableType = embeddableType;
				String searchKey = embeddableType.getJavaType().getName();

				if (searchMap.containsKey(searchKey))
					continue;

				JPAEdmPropertyView propertyView = new JPAEdmProperty(
						schemaView, JPAEdmComplexType.this);
				propertyView.getBuilder().build();

				currentComplexType = new ComplexType();
				currentComplexType
						.setProperties(propertyView.getEdmPropertyList());
				JPAEdmNameBuilder.build(JPAEdmComplexType.this);

				searchMap.put(searchKey, currentComplexType);
				consistentComplextTypes.add(currentComplexType);

			}

		}

	}
}
