package com.sap.core.odata.processor.core.jpa.jpql;

import java.util.ArrayList;
import java.util.List;

import com.sap.core.odata.api.edm.EdmEntityType;
import com.sap.core.odata.api.edm.EdmException;
import com.sap.core.odata.api.edm.EdmMapping;
import com.sap.core.odata.api.edm.EdmNavigationProperty;
import com.sap.core.odata.api.edm.provider.Mapping;
import com.sap.core.odata.api.uri.NavigationSegment;
import com.sap.core.odata.processor.api.jpa.access.JPAJoinClause;
import com.sap.core.odata.processor.api.jpa.exception.ODataJPAModelException;
import com.sap.core.odata.processor.api.jpa.exception.ODataJPARuntimeException;
import com.sap.core.odata.processor.api.jpa.jpql.JPQLContext;
import com.sap.core.odata.processor.api.jpa.jpql.JPQLContextType;
import com.sap.core.odata.processor.api.jpa.jpql.JPQLJoinSelectSingleContextView;
import com.sap.core.odata.processor.core.jpa.access.data.ODataExpressionParser;

public class JPQLJoinSelectSingleContext extends JPQLSelectSingleContext
		implements JPQLJoinSelectSingleContextView {

	private List<JPAJoinClause> jpaJoinClauses = null;

	protected void setJPAJoinClause(List<JPAJoinClause> jpaJoinClauses) {
		this.jpaJoinClauses = jpaJoinClauses;
	}

	public class JPQLJoinSelectSingleContextBuilder extends
			JPQLSelectSingleContextBuilder {

		protected int relationShipAliasCounter = 0;

		@Override
		public JPQLContext build() throws ODataJPAModelException,
				ODataJPARuntimeException {
			try {
				JPQLJoinSelectSingleContext.this
						.setType(JPQLContextType.JOIN_SINGLE);
				JPQLJoinSelectSingleContext.this
						.setJPAJoinClause(generateJoinClauses());

				if (!jpaJoinClauses.isEmpty()) {
					JPAJoinClause joinClause = jpaJoinClauses
							.get(jpaJoinClauses.size() - 1);
					JPQLJoinSelectSingleContext.this
							.setJPAEntityName(joinClause.getEntityName());
					JPQLJoinSelectSingleContext.this
							.setJPAEntityAlias(joinClause
									.getEntityRelationShipAlias());
				}

				JPQLJoinSelectSingleContext.this.setKeyPredicates(entityView
						.getKeyPredicates());

				JPQLJoinSelectSingleContext.this
						.setSelectExpression(generateSelectExpression());

			} catch (EdmException e) {
				throw ODataJPARuntimeException.throwException(
						ODataJPARuntimeException.GENERAL, e);
			}

			return JPQLJoinSelectSingleContext.this;
		}

		protected List<JPAJoinClause> generateJoinClauses()
				throws ODataJPARuntimeException, EdmException {

			List<JPAJoinClause> jpaOuterJoinClauses = new ArrayList<JPAJoinClause>();
			JPAJoinClause jpaOuterJoinClause = null;
			String joinCondition = null;
			String entityAlias = generateJPAEntityAlias();
			joinCondition = ODataExpressionParser.parseKeyPredicates(
					entityView.getKeyPredicates(), entityAlias);

			EdmEntityType entityType = entityView.getStartEntitySet()
					.getEntityType();
			Mapping mapping = (Mapping) entityType.getMapping();
			String entityTypeName = null;
			if (mapping != null)
				entityTypeName = mapping.getInternalName();
			else
				entityTypeName = entityType.getName();

			jpaOuterJoinClause = new JPAJoinClause(entityTypeName, entityAlias,
					null, null, joinCondition, JPAJoinClause.JOIN.INNER);

			jpaOuterJoinClauses.add(jpaOuterJoinClause);

			for (NavigationSegment navigationSegment : entityView
					.getNavigationSegments()) {

				EdmNavigationProperty navigationProperty = navigationSegment
						.getNavigationProperty();

				String relationShipAlias = generateRelationShipAlias();

				joinCondition = ODataExpressionParser
						.parseKeyPredicates(
								navigationSegment.getKeyPredicates(),
								relationShipAlias);

				jpaOuterJoinClause = new JPAJoinClause(
						getFromEntityName(navigationProperty), entityAlias,
						getRelationShipName(navigationProperty),
						relationShipAlias, joinCondition,
						JPAJoinClause.JOIN.INNER);

				jpaOuterJoinClauses.add(jpaOuterJoinClause);

			}

			return jpaOuterJoinClauses;
		}

		private String getFromEntityName(
				EdmNavigationProperty navigationProperty) throws EdmException {

			String fromRole = navigationProperty.getFromRole();

			EdmEntityType fromEntityType = navigationProperty.getRelationship()
					.getEnd(fromRole).getEntityType();

			EdmMapping mapping = fromEntityType.getMapping();

			String entityName = null;
			if (mapping != null)
				entityName = mapping.getInternalName();
			else
				entityName = fromEntityType.getName();

			return entityName;

		}

		private String getRelationShipName(
				EdmNavigationProperty navigationProperty) throws EdmException {

			EdmMapping mapping = navigationProperty.getMapping();

			String relationShipName = null;
			if (mapping != null)
				relationShipName = mapping.getInternalName();
			else
				relationShipName = navigationProperty.getName();

			return relationShipName;
		}

		private String generateRelationShipAlias() {
			return new String("R" + ++this.relationShipAliasCounter);
		}
	}

	@Override
	public List<JPAJoinClause> getJPAJoinClauses() {
		return this.jpaJoinClauses;
	}

}
