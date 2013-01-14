package com.sap.core.odata.processor.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.core.odata.api.commons.HttpStatusCodes;
import com.sap.core.odata.api.commons.InlineCount;
import com.sap.core.odata.api.edm.EdmEntityType;
import com.sap.core.odata.api.edm.EdmException;
import com.sap.core.odata.api.ep.EntityProvider;
import com.sap.core.odata.api.ep.EntityProviderException;
import com.sap.core.odata.api.ep.EntityProviderProperties;
import com.sap.core.odata.api.exception.ODataException;
import com.sap.core.odata.api.processor.ODataResponse;
import com.sap.core.odata.api.uri.info.GetEntitySetUriInfo;
import com.sap.core.odata.api.uri.info.GetEntityUriInfo;
import com.sap.core.odata.processor.jpa.api.ODataJPAContext;
import com.sap.core.odata.processor.jpa.exception.ODataJPARuntimeException;

public final class ODataJPAResponseBuilder {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ODataJPAResponseBuilder.class);

	public static ODataResponse build(List<Object> jpaEntities,
			GetEntitySetUriInfo resultsView, String contentType,ODataJPAContext odataJPAContext) throws ODataJPARuntimeException {
				
		EdmEntityType edmEntityType = null;
		ODataResponse odataResponse = null;

		try {
			edmEntityType = resultsView.getTargetEntitySet().getEntityType();

			List<Map<String, Object>> edmEntityList = new ArrayList<Map<String, Object>>();
			Map<String, Object> edmPropertyValueMap = null;
			
			JPAResultParser jpaResultParser = JPAResultParser.create( );
			for (Object jpaEntity : jpaEntities) {
				edmPropertyValueMap = jpaResultParser.parse2EdmPropertyValueMap(jpaEntity,edmEntityType);
				edmEntityList.add(edmPropertyValueMap);
			}
			
		    EntityProviderProperties feedProperties = null;;
			try {
				 final Integer count = resultsView.getInlineCount() == InlineCount.ALLPAGES ? edmEntityList.size() : null;
				feedProperties = EntityProviderProperties
				        .baseUri(odataJPAContext.getODataContext().getPathInfo().getServiceRoot())
				        .inlineCount(count)
				        .skipToken("")
				        .build();
			} catch (ODataException e) { 
				LOGGER.error(e.getMessage(), e);
				throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.GENERAL.addContent(e.getMessage()),e);
			}
		    
			odataResponse = ODataResponse.fromResponse(EntityProvider.create(contentType).writeFeed(resultsView.getTargetEntitySet(), edmEntityList, feedProperties))
			        .status(HttpStatusCodes.OK)
			        .build();
			
			
		} catch (EntityProviderException e) {
			LOGGER.error(e.getMessage(), e);
			throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.GENERAL.addContent(e.getMessage()),e);
		} catch (EdmException e) {
			LOGGER.error(e.getMessage(), e);
			throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.GENERAL.addContent(e.getMessage()),e);
		}

		return odataResponse;
	}

	public static ODataResponse build(Object jpaEntity,
			GetEntityUriInfo resultsView, String contentType,
			ODataJPAContext oDataJPAContext) throws ODataJPARuntimeException {
		
		EdmEntityType edmEntityType = null;
		ODataResponse odataResponse = null;

		try {
			
			edmEntityType = resultsView.getTargetEntitySet().getEntityType();
			Map<String, Object> edmPropertyValueMap = null;
			
			JPAResultParser jpaResultParser = JPAResultParser.create( );
			edmPropertyValueMap = jpaResultParser.parse2EdmPropertyValueMap(jpaEntity,edmEntityType);

			
		    EntityProviderProperties feedProperties = null;
			try {
				feedProperties = EntityProviderProperties
				        .baseUri(oDataJPAContext.getODataContext().getPathInfo().getServiceRoot())
				        .build();
			} catch (ODataException e) {
				LOGGER.error(e.getMessage(), e);
				throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.GENERAL.addContent(e.getMessage()),e);
			}
		    
			odataResponse = ODataResponse.fromResponse(EntityProvider.create(contentType).writeEntry(resultsView.getTargetEntitySet(), edmPropertyValueMap, feedProperties))
			        .status(HttpStatusCodes.OK)
			        .build();
			
			
		} catch (EntityProviderException e) {
			LOGGER.error(e.getMessage(), e);
			throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.GENERAL.addContent(e.getMessage()),e);
		} catch (EdmException e) {
			LOGGER.error(e.getMessage(), e);
			throw ODataJPARuntimeException.throwException(ODataJPARuntimeException.GENERAL.addContent(e.getMessage()),e);
		}

		return odataResponse;
	}
}
