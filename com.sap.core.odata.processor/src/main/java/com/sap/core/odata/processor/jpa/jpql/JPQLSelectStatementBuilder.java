package com.sap.core.odata.processor.jpa.jpql;

import java.util.Iterator;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.core.odata.api.exception.ODataException;
import com.sap.core.odata.processor.jpa.access.ODataExpressionParser;
import com.sap.core.odata.processor.jpa.api.jpql.JPQLContext;
import com.sap.core.odata.processor.jpa.api.jpql.JPQLContextView;
import com.sap.core.odata.processor.jpa.api.jpql.JPQLSelectContextView;
import com.sap.core.odata.processor.jpa.api.jpql.JPQLStatement;
import com.sap.core.odata.processor.jpa.api.jpql.JPQLStatement.JPQLStatementBuilder;
import com.sap.core.odata.processor.jpa.exception.ODataJPARuntimeException;


public class JPQLSelectStatementBuilder extends JPQLStatementBuilder{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(JPQLSelectStatementBuilder.class);
	
	JPQLStatement jpqlStatement;
	private JPQLSelectContextView context;
	
	public JPQLSelectStatementBuilder(JPQLContextView context) {
		this.context = (JPQLSelectContextView) context;
	}

	@Override
	public JPQLStatement build() throws ODataJPARuntimeException {
		this.jpqlStatement = createStatement(createJPQLQuery());
		return this.jpqlStatement;
		
	}

	private String createJPQLQuery() throws ODataJPARuntimeException {

		StringBuilder jpqlQuery = new StringBuilder();
		String tableAlias = ODataExpressionParser.TABLE_ALIAS;
		String fromClause = ((JPQLContext) context).getJPAEntityName() + " " + tableAlias;
		String query = "SELECT %s FROM %s";
		StringBuilder orderByBuilder = new StringBuilder();
		
		jpqlQuery.append(String.format(query, tableAlias, fromClause));
		
		try {
			if(context.getWhereExpression()!=null)
				jpqlQuery.append(" WHERE ").append(ODataExpressionParser.parseToJPAWhereExpression(context.getWhereExpression()));
		} catch (ODataException e) {
			LOGGER.error(e.getMessage(), e);
			throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.GENERAL.addContent(e.getMessage()),e);
		}
		
				
		if(context.getOrderByCollection() != null && context.getOrderByCollection().size()>0){
			Iterator<Entry<String,String>> orderItr = context.getOrderByCollection().entrySet().iterator();
			int i = 0;
			while(orderItr.hasNext()){				
				if(i != 0){
					orderByBuilder.append(", ");
				}
				Entry<String, String> entry = orderItr.next();
				orderByBuilder.append(tableAlias+"."+entry.getKey()).append(" "+entry.getValue());				
				i++;
			}
			jpqlQuery.append(" ORDER BY ").append(orderByBuilder);
		}
		
		LOGGER.info("JPQL Select Statement formed : "+ jpqlQuery.toString());
		
		return jpqlQuery.toString();
	
	}


}
