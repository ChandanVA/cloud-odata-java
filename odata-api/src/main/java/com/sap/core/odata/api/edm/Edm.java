package com.sap.core.odata.api.edm;

/**
 * Entity Data Model (EDM)
 * 
 * Do not implement this interface. This interface is intended for usage only.
 * 
 * @author SAP AG
 */
public interface Edm {

  public static final String NAMESPACE_EDM = "http://schemas.microsoft.com/ado/2008/09/edm";
  public static final String NAMESPACE_APP = "http://www.w3.org/2007/app";
  public static final String NAMESPACE_ATOM = "http://www.w3.org/2005/Atom";
  public static final String NAMESPACE_D = "http://schemas.microsoft.com/ado/2007/08/dataservices";
  public static final String NAMESPACE_M = "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata";
  public static final String NAMESPACE_EDMX = "http://schemas.microsoft.com/ado/2007/06/edmx";
  public static final String NAMESPACE_REL = "http://schemas.microsoft.com/ado/2007/08/dataservices/related/";
  public static final String NAMESPACE_REL_ASSOC = "http://schemas.microsoft.com/ado/2007/08/dataservices/relatedlinks/";
  public static final String NAMESPACE_SCHEME = "http://schemas.microsoft.com/ado/2007/08/dataservices/scheme";
  public static final String PREFIX_EDM = "edm";
  public static final String PREFIX_APP = "app";
  public static final String PREFIX_ATOM = "atom";
  public static final String PREFIX_D = "d";
  public static final String PREFIX_M = "m";
  public static final String PREFIX_EDMX = "edmx";
  public static final String LINK_REL_SELF = "self";
  public static final String LINK_REL_EDIT_MEDIA = "edit-media";
  public static final String LINK_REL_EDIT = "edit";
  public static final String LINK_REL_NEXT = "next";
  public static final String DATA_SERVICE_VERSION_10 = "1.0";
  public static final String DATA_SERVICE_VERSION_20 = "2.0";
  public static final String DELIMITER = ".";
  
  /**
   * Get entity container by name
   * 
   * @param name
   * @return {@link EdmEntityContainer}
   * @throws EdmException
   */
  EdmEntityContainer getEntityContainer(String name) throws EdmException;

  /**
   * Get entity type by full qualified name
   * 
   * @param namespace
   * @param name
   * @return {@link EdmEntityType}
   * @throws EdmException
   */
  EdmEntityType getEntityType(String namespace, String name) throws EdmException;

  /**
   * Get complex type by full qualified name
   * 
   * @param namespace
   * @param name
   * @return {@link EdmComplexType}
   * @throws EdmException
   */
  EdmComplexType getComplexType(String namespace, String name) throws EdmException;

  /**
   * Get association by full qualified name
   * 
   * @param namespace
   * @param name
   * @return {@link EdmAssociation}
   * @throws EdmException
   */
  EdmAssociation getAssociation(String namespace, String name) throws EdmException;

  /**
   * Get service metadata
   * 
   * @return {@link EdmServiceMetadata}
   */
  EdmServiceMetadata getServiceMetadata();

  /**
   * Get default entity container
   * 
   * @return {@link EdmEntityContainer}
   * @throws EdmException
   */
  EdmEntityContainer getDefaultEntityContainer() throws EdmException;
}