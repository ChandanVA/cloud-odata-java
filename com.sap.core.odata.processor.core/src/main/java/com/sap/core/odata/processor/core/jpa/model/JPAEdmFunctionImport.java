package com.sap.core.odata.processor.core.jpa.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.sap.core.odata.api.annotation.edm.FunctionImport.Multiplicity;
import com.sap.core.odata.api.annotation.edm.FunctionImport.ReturnType;
import com.sap.core.odata.api.annotation.edm.Parameter;
import com.sap.core.odata.api.edm.EdmMultiplicity;
import com.sap.core.odata.api.edm.EdmSimpleTypeKind;
import com.sap.core.odata.api.edm.provider.ComplexType;
import com.sap.core.odata.api.edm.provider.EntityType;
import com.sap.core.odata.api.edm.provider.Facets;
import com.sap.core.odata.api.edm.provider.FunctionImport;
import com.sap.core.odata.api.edm.provider.FunctionImportParameter;
import com.sap.core.odata.api.edm.provider.Mapping;
import com.sap.core.odata.processor.api.jpa.access.JPAEdmBuilder;
import com.sap.core.odata.processor.api.jpa.exception.ODataJPAModelException;
import com.sap.core.odata.processor.api.jpa.exception.ODataJPARuntimeException;
import com.sap.core.odata.processor.api.jpa.model.JPAEdmComplexTypeView;
import com.sap.core.odata.processor.api.jpa.model.JPAEdmEntityTypeView;
import com.sap.core.odata.processor.api.jpa.model.JPAEdmFunctionImportView;
import com.sap.core.odata.processor.api.jpa.model.JPAEdmSchemaView;
import com.sap.core.odata.processor.core.jpa.access.model.JPAEdmNameBuilder;
import com.sap.core.odata.processor.core.jpa.access.model.JPATypeConvertor;

public class JPAEdmFunctionImport extends JPAEdmBaseViewImpl implements
		JPAEdmFunctionImportView {

	private List<FunctionImport> consistentFunctionImportList = new ArrayList<FunctionImport>();
	private JPAEdmBuilder builder = null;
	private JPAEdmSchemaView schemaView;

	public JPAEdmFunctionImport(JPAEdmSchemaView view) {
		super(view);
		this.schemaView = view;
	}

	@Override
	public JPAEdmBuilder getBuilder() {
		if (builder == null)
			builder = new JPAEdmFunctionImportBuilder();
		return builder;
	}

	@Override
	public List<FunctionImport> getConsistentFunctionImportList() {
		return consistentFunctionImportList;
	}

	private class JPAEdmFunctionImportBuilder implements JPAEdmBuilder {

		private JPAEdmEntityTypeView jpaEdmEntityTypeView = null;
		private JPAEdmComplexTypeView jpaEdmComplexTypeView = null;

		@Override
		public void build() throws ODataJPAModelException,
				ODataJPARuntimeException {

			HashMap<Class<?>, String[]> customOperations = schemaView
					.getRegisteredOperations();

			jpaEdmEntityTypeView = schemaView.getJPAEdmEntityContainerView()
					.getJPAEdmEntitySetView().getJPAEdmEntityTypeView();
			jpaEdmComplexTypeView = schemaView.getJPAEdmComplexTypeView();

			if (customOperations != null) {

				for (Class<?> clazz : customOperations.keySet()) {

					String[] operationNames = customOperations.get(clazz);
					Method[] methods = clazz.getMethods();
					Method method = null;

					int length = 0;
					if (operationNames != null)
						length = operationNames.length;
					else
						length = methods.length;

					boolean found = false;
					for (int i = 0; i < length; i++) {

						try {
							if (operationNames != null) {
								for (int j = 0; j < methods.length; j++) {
									if (methods[j].getName().equals(
											operationNames[i])) {
										found = true;
										method = methods[j];
										break;
									}
								}
								if (found == true)
									found = false;
								else
									continue;
							} else
								method = methods[i];

							FunctionImport functionImport = buildFunctionImport(method);
							if (functionImport != null)
								consistentFunctionImportList
										.add(functionImport);

						} catch (SecurityException e) {
							throw ODataJPAModelException.throwException(
									ODataJPAModelException.GENERAL, e);
						}
					}
				}
			}
		}

		private FunctionImport buildFunctionImport(Method method)
				throws ODataJPAModelException {

			com.sap.core.odata.api.annotation.edm.FunctionImport annotation = method
					.getAnnotation(com.sap.core.odata.api.annotation.edm.FunctionImport.class);
			if (annotation != null) {
				FunctionImport functionImport = new FunctionImport();

				if (annotation.name().equals(""))
					functionImport.setName(method.getName());
				else {
					Mapping mapping = new Mapping();
					mapping.setInternalName(method.getName());
					functionImport.setMapping(mapping);
					functionImport.setName(annotation.name());
				}

				buildReturnType(functionImport, method, annotation);
				buildParameter(functionImport, method);

				return functionImport;
			}
			return null;
		}

		private void buildParameter(FunctionImport functionImport, Method method)
				throws ODataJPAModelException {

			Annotation[][] annotations = method.getParameterAnnotations();
			Class<?>[] parameterTypes = method.getParameterTypes();
			List<FunctionImportParameter> funcImpList = new ArrayList<FunctionImportParameter>();
			int j = 0;
			for (Annotation[] annotationArr : annotations) {
				Class<?> parameterType = parameterTypes[j++];

				for (int i = 0; i < annotationArr.length; i++) {
					if (annotationArr[i] instanceof Parameter) {
						Parameter annotation = (Parameter) annotationArr[i];
						FunctionImportParameter functionImportParameter = new FunctionImportParameter();
						if (annotation.name().equals("")) {
							throw ODataJPAModelException.throwException(
									ODataJPAModelException.FUNC_PARAM_NAME_EXP
											.addContent(method
													.getDeclaringClass()
													.getName(), method
													.getName()), null);
						} else
							functionImportParameter.setName(annotation.name());

						functionImportParameter.setType(JPATypeConvertor
								.convertToEdmSimpleType(parameterType));
						functionImportParameter.setMode(annotation.mode()
								.toString());

						Facets facets = new Facets();
						if (annotation.facets().maxLength() > 0)
							facets.setMaxLength(annotation.facets().maxLength());
						if (annotation.facets().nullable() == false)
							facets.setNullable(false);
						else
							facets.setNullable(true);

						if (annotation.facets().precision() > 0)
							facets.setPrecision(annotation.facets().precision());
						if (annotation.facets().scale() >= 0)
							facets.setScale(annotation.facets().scale());

						functionImportParameter.setFacets(facets);
						funcImpList.add(functionImportParameter);
					}
				}
			}
			if (!funcImpList.isEmpty())
				functionImport.setParameters(funcImpList);
		}

		private void buildReturnType(FunctionImport functionImport,
				Method method,
				com.sap.core.odata.api.annotation.edm.FunctionImport annotation)
				throws ODataJPAModelException {
			ReturnType returnType = annotation.returnType();
			Multiplicity multiplicity = null;
			if (returnType != ReturnType.NONE) {
				com.sap.core.odata.api.edm.provider.ReturnType functionReturnType = new com.sap.core.odata.api.edm.provider.ReturnType();
				multiplicity = annotation.multiplicity();

				if (multiplicity == Multiplicity.MANY) {
					if (returnType == ReturnType.ENTITY_TYPE) {
						String entitySet = annotation.entitySet();
						if (entitySet.equals(""))
							throw ODataJPAModelException.throwException(
									ODataJPAModelException.FUNC_ENTITYSET_EXP,
									null);
						functionImport.setEntitySet(entitySet);
					}
					functionReturnType.setMultiplicity(EdmMultiplicity.MANY);
				} else {
					functionReturnType.setMultiplicity(EdmMultiplicity.ONE);
				}

				Class<?> methodReturnType = method.getReturnType();
				if (methodReturnType == null
						|| methodReturnType.getName().equals("void"))
					throw ODataJPAModelException.throwException(
							ODataJPAModelException.FUNC_RETURN_TYPE_EXP
									.addContent(method.getDeclaringClass(),
											method.getName()), null);
				switch (returnType) {
				case ENTITY_TYPE:
					EntityType edmEntityType = null;
					if (multiplicity == Multiplicity.ONE)
						edmEntityType = jpaEdmEntityTypeView
								.searchEdmEntityType(methodReturnType
										.getSimpleName());
					else if (multiplicity == Multiplicity.MANY)
						edmEntityType = jpaEdmEntityTypeView
								.searchEdmEntityType(getReturnTypeSimpleName(method));

					if (edmEntityType == null)
						throw ODataJPAModelException
								.throwException(
										ODataJPAModelException.FUNC_RETURN_TYPE_ENTITY_NOT_FOUND
												.addContent(
														method.getDeclaringClass(),
														method.getName(),
														methodReturnType
																.getSimpleName()),
										null);
					functionReturnType.setTypeName(JPAEdmNameBuilder.build(
							schemaView, edmEntityType.getName()));
					break;
				case SCALAR:

					EdmSimpleTypeKind edmSimpleTypeKind = JPATypeConvertor
							.convertToEdmSimpleType(methodReturnType);
					functionReturnType.setTypeName(edmSimpleTypeKind
							.getFullQualifiedName());

					break;
				case COMPLEX_TYPE:
					ComplexType complexType = null;
					if (multiplicity == Multiplicity.ONE)
						complexType = jpaEdmComplexTypeView
								.searchEdmComplexType(methodReturnType
										.getName());
					else if (multiplicity == Multiplicity.MANY)
						complexType = jpaEdmComplexTypeView
								.searchEdmComplexType(getReturnTypeName(method));
					if (complexType == null)
						throw ODataJPAModelException
								.throwException(
										ODataJPAModelException.FUNC_RETURN_TYPE_ENTITY_NOT_FOUND
												.addContent(
														method.getDeclaringClass(),
														method.getName(),
														methodReturnType
																.getSimpleName()),
										null);
					functionReturnType.setTypeName(JPAEdmNameBuilder.build(
							schemaView, complexType.getName()));
					break;
				default:
					break;
				}
				functionImport.setReturnType(functionReturnType);
			}
		}

		private String getReturnTypeName(Method method) {
			try {
				ParameterizedType pt = (ParameterizedType) method
						.getGenericReturnType();
				Type t = pt.getActualTypeArguments()[0];
				return ((Class<?>) t).getName();
			} catch (ClassCastException e) {
				return method.getReturnType().getName();
			}
		}
		
		private String getReturnTypeSimpleName(Method method) {
			try {
				ParameterizedType pt = (ParameterizedType) method
						.getGenericReturnType();
				Type t = pt.getActualTypeArguments()[0];
				return ((Class<?>) t).getSimpleName();
			} catch (ClassCastException e) {
				return method.getReturnType().getSimpleName();
			}
		}
	}
}
