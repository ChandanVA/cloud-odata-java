package com.sap.core.odata.processor.core.jpa.access.data;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.sap.core.odata.api.edm.EdmException;
import com.sap.core.odata.api.uri.info.GetEntitySetUriInfo;
import com.sap.core.odata.api.uri.info.GetEntityUriInfo;
import com.sap.core.odata.processor.api.ODataJPAContext;
import com.sap.core.odata.processor.api.access.JPAProcessor;
import com.sap.core.odata.processor.api.exception.ODataJPAModelException;
import com.sap.core.odata.processor.api.exception.ODataJPARuntimeException;
import com.sap.core.odata.processor.api.jpql.JPQLContext;
import com.sap.core.odata.processor.api.jpql.JPQLContextType;
import com.sap.core.odata.processor.api.jpql.JPQLStatement;

public class JPAProcessorImpl implements JPAProcessor {

	ODataJPAContext oDataJPAContext;
	EntityManager em;

	public JPAProcessorImpl(ODataJPAContext oDataJPAContext) {
		this.oDataJPAContext = oDataJPAContext;
		em = oDataJPAContext.getEntityManagerFactory().createEntityManager();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> process(GetEntitySetUriInfo uriParserResultView)
			throws ODataJPAModelException, ODataJPARuntimeException {

		JPQLContextType contextType = null;
		try {
			if (!uriParserResultView.getStartEntitySet().getName()
					.equals(uriParserResultView.getTargetEntitySet().getName()))
				contextType = JPQLContextType.JOIN;
			else
				contextType = JPQLContextType.SELECT;

		} catch (EdmException e) {
			ODataJPARuntimeException.throwException(
					ODataJPARuntimeException.GENERAL, e);
		}

		// Build JPQL Context
		JPQLContext jpqlContext = JPQLContext.createBuilder(contextType,
				uriParserResultView).build();

		// Build JPQL Statement
		JPQLStatement jpqlStatement = JPQLStatement.createBuilder(jpqlContext)
				.build();

		// Instantiate JPQL
		Query query = em.createQuery(jpqlStatement.toString());
		if (uriParserResultView.getSkip() != null)
			query.setFirstResult(uriParserResultView.getSkip());

		if (uriParserResultView.getTop() != null){
			if(uriParserResultView.getTop() == 0){
				List<T> resultList = new ArrayList<T>();
				return resultList;
			}else{
				query.setMaxResults(uriParserResultView.getTop());
			}
		}
			
		return query.getResultList();

	}

	@Override
	public Object process(GetEntityUriInfo uriParserResultView)
			throws ODataJPAModelException, ODataJPARuntimeException {

		JPQLContextType contextType = null;		
		try {
			if (!uriParserResultView.getStartEntitySet().getName()
					.equals(uriParserResultView.getTargetEntitySet().getName()))
				contextType = JPQLContextType.JOIN_SINGLE;
			else
				contextType = JPQLContextType.SELECT_SINGLE;

		} catch (EdmException e) {
			ODataJPARuntimeException.throwException(
					ODataJPARuntimeException.GENERAL, e);
		}
		
		// Build JPQL Context
		JPQLContext singleSelectContext = JPQLContext.createBuilder(
				contextType, uriParserResultView).build();

		// Build JPQL Statement
		JPQLStatement selectStatement = JPQLStatement.createBuilder(
				singleSelectContext).build();

		// Instantiate JPQL
		Query query = em.createQuery(selectStatement.toString());
		
		if(query.getResultList().isEmpty())
			return null;
		
		return query.getResultList().get(0);
	}

}
