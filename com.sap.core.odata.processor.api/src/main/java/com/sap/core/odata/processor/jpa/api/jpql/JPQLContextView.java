package com.sap.core.odata.processor.jpa.api.jpql;

public interface JPQLContextView {
	public String getJPAEntityName();
	public String getJPAEntityAlias( );
	public JPQLContextType getType();
}
