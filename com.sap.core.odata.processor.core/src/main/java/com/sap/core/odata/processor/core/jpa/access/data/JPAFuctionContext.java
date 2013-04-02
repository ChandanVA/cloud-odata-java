package com.sap.core.odata.processor.core.jpa.access.data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sap.core.odata.api.edm.EdmException;
import com.sap.core.odata.api.edm.EdmFacets;
import com.sap.core.odata.api.edm.EdmFunctionImport;
import com.sap.core.odata.api.edm.EdmLiteral;
import com.sap.core.odata.api.edm.EdmLiteralKind;
import com.sap.core.odata.api.edm.EdmMapping;
import com.sap.core.odata.api.edm.EdmParameter;
import com.sap.core.odata.api.edm.EdmSimpleType;
import com.sap.core.odata.api.edm.EdmSimpleTypeException;
import com.sap.core.odata.api.uri.info.GetFunctionImportUriInfo;
import com.sap.core.odata.processor.api.jpa.access.JPAFunction;
import com.sap.core.odata.processor.api.jpa.access.JPAMethodContext;
import com.sap.core.odata.processor.api.jpa.exception.ODataJPAModelException;
import com.sap.core.odata.processor.api.jpa.exception.ODataJPARuntimeException;
import com.sap.core.odata.processor.api.jpa.model.JPAEdmMapping;

public class JPAFuctionContext extends JPAMethodContext {

	public class JPAFunctionContextBuilder extends JPAMethodContextBuilder {

		protected GetFunctionImportUriInfo functiontView;
		private EdmFunctionImport functionImport;
		private EdmMapping mapping;

		@Override
		public JPAMethodContext build() throws ODataJPAModelException,
				ODataJPARuntimeException {
			if (functiontView != null) {

				functionImport = functiontView.getFunctionImport();
				try {
					mapping = functionImport.getMapping();

					List<JPAFunction> jpaFunctionList = new ArrayList<JPAFunction>();
					jpaFunctionList.add(generateJPAFunction());
					JPAFuctionContext.this.setJpaFunction(jpaFunctionList);
					JPAFuctionContext.this
							.setEnclosingObject(generateEnclosingObject());
				} catch (EdmException e) {
					throw ODataJPARuntimeException.throwException(
							ODataJPARuntimeException.GENERAL.addContent(e
									.getMessage()), e);
				} catch (InstantiationException e) {
					throw ODataJPARuntimeException.throwException(
							ODataJPARuntimeException.GENERAL.addContent(e
									.getMessage()), e);
				} catch (IllegalAccessException e) {
					throw ODataJPARuntimeException.throwException(
							ODataJPARuntimeException.GENERAL.addContent(e
									.getMessage()), e);
				} catch (IllegalArgumentException e) {
					throw ODataJPARuntimeException.throwException(
							ODataJPARuntimeException.GENERAL.addContent(e
									.getMessage()), e);
				} catch (InvocationTargetException e) {
					throw ODataJPARuntimeException.throwException(
							ODataJPARuntimeException.GENERAL.addContent(e
									.getMessage()), e);
				} catch (NoSuchMethodException e) {
					throw ODataJPARuntimeException.throwException(
							ODataJPARuntimeException.GENERAL.addContent(e
									.getMessage()), e);
				} catch (SecurityException e) {
					throw ODataJPARuntimeException.throwException(
							ODataJPARuntimeException.GENERAL.addContent(e
									.getMessage()), e);
				}
			}

			return JPAFuctionContext.this;
		}

		private JPAFunction generateJPAFunction() throws EdmException,
				NoSuchMethodException, SecurityException,
				ODataJPAModelException, ODataJPARuntimeException {

			Class<?>[] parameterTypes = getParameterTypes();
			Method method = getMethod(parameterTypes);
			Type returnType = getReturnType();
			Object[] args = getAruguments();

			JPAFunction jpafunction = new JPAFunction(method, parameterTypes,
					returnType, args);

			return jpafunction;
		}

		private Object[] getAruguments() throws EdmException {
			Map<String, EdmLiteral> edmArguements = functiontView
					.getFunctionImportParameters();

			if (edmArguements == null) {
				return null;
			} else {
				Object[] args = new Object[edmArguements.size()];
				int i = 0;
				for (String paramName : functionImport.getParameterNames()) {
					EdmLiteral literal = edmArguements.get(paramName);
					EdmParameter parameter = functionImport
							.getParameter(paramName);
					JPAEdmMapping mapping = (JPAEdmMapping) parameter
							.getMapping();
					args[i] = convertArguement(literal, parameter.getFacets(),
							mapping.getJPAType());
					i++;
				}
				return args;
			}

		}

		private Object convertArguement(EdmLiteral edmLiteral,
				EdmFacets facets, Class<?> targetType)
				throws EdmSimpleTypeException {
			EdmSimpleType edmType = edmLiteral.getType();
			Object value = edmType.valueOfString(edmLiteral.getLiteral(),
					EdmLiteralKind.DEFAULT, facets, targetType);

			return value;
		}

		private Class<?>[] getParameterTypes() throws EdmException {

			Class<?>[] parameterTypes = new Class<?>[functionImport
					.getParameterNames().size()];
			int i = 0;
			for (String parameterName : functionImport.getParameterNames()) {
				EdmParameter parameter = functionImport
						.getParameter(parameterName);
				parameterTypes[i] = ((JPAEdmMapping) parameter.getMapping())
						.getJPAType();
				i++;
			}

			return parameterTypes;
		}

		private Method getMethod(Class<?>[] parameterTypes)
				throws NoSuchMethodException, SecurityException {

			Class<?> type = ((JPAEdmMapping) mapping).getJPAType();
			Method method;
			method = type.getMethod(mapping.getInternalName(), parameterTypes);

			return method;
		}

		private Type getReturnType() throws ODataJPAModelException,
				ODataJPARuntimeException, EdmException {
			return null;
		}

		private Object generateEnclosingObject() throws InstantiationException,
				IllegalAccessException, IllegalArgumentException,
				InvocationTargetException, NoSuchMethodException,
				SecurityException {

			Class<?> type = ((JPAEdmMapping) mapping).getJPAType();
			Object[] params = null;

			return type.getConstructor((Class<?>[]) params).newInstance(params);

		}

		@Override
		protected void setResultsView(Object resultsView) {
			if (resultsView instanceof GetFunctionImportUriInfo) {
				this.functiontView = (GetFunctionImportUriInfo) resultsView;
			}

		}

	}
}
