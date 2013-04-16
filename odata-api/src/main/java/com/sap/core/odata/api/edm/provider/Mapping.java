package com.sap.core.odata.api.edm.provider;

import com.sap.core.odata.api.edm.EdmMapping;

/**
 * Object of this class represent the mapping of a value to a mime type
 * @author SAP AG
 */
public class Mapping implements EdmMapping {

  private String value;
  private String mimeType;
  private Object dataContainer;

  /* (non-Javadoc)
   * @see com.sap.core.odata.api.edm.EdmMapping#getValue()
   */
  @Override
  public String getInternalName() {
    return value;
  }

  /* (non-Javadoc)
   * @see com.sap.core.odata.api.edm.EdmMapping#getMimeType()
   */
  @Override
  public String getMimeType() {
    return mimeType;
  }
  
  /* (non-Javadoc)
   * @see com.sap.core.odata.api.edm.EdmMapping#getObject()
   */
  @Override
  public Object getDataContainer() {
    return dataContainer;
  }

  /**
   * MANDATORY
   * <p>Sets the value for this {@link Mapping}
   * @param value
   * @return {@link Mapping} for method chaining
   */
  public Mapping setInternalName(String value) {
    this.value = value;
    return this;
  }

  /**
   * Sets the mime type for this {@link Mapping}
   * @param mimeType
   * @return {@link Mapping} for method chaining
   */
  public Mapping setMimeType(String mimeType) {
    this.mimeType = mimeType;
    return this;
  }
  
  /**
   * Sets the data container for this mapping.
   * @param dataContainer
   * @return {@link Mapping} for method chaining
   */
  public Mapping setDataContainer(Object dataContainer){
    this.dataContainer = dataContainer;
    return this;
  }
}
