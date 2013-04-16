package com.sap.core.odata.api.edm.provider;

import com.sap.core.odata.api.edm.EdmContentKind;
import com.sap.core.odata.api.edm.EdmCustomizableFeedMappings;
import com.sap.core.odata.api.edm.EdmTargetPath;

/**
 * @author SAP AG
 * <p>
 * Objects of this class represent customizable feed mappings in the EDM
 */
public class CustomizableFeedMappings implements EdmCustomizableFeedMappings {

  //TODO: Revisit Javadoc for descriptions
  private Boolean fcKeepInContent;
  private EdmContentKind fcContentKind;
  private String fcNsPrefix;
  private String fcNsUri;
  private String fcSourcePath;
  private EdmTargetPath fcTargetPath;
  private String customTargetPath;

  /* (non-Javadoc)
   * @see com.sap.core.odata.api.edm.EdmCustomizableFeedMappings#isFcKeepInContent()
   */
  public Boolean isFcKeepInContent() {
    return fcKeepInContent;
  }

  /* (non-Javadoc)
   * @see com.sap.core.odata.api.edm.EdmCustomizableFeedMappings#getFcContentKind()
   */
  public EdmContentKind getFcContentKind() {
    return fcContentKind;
  }

  /* (non-Javadoc)
   * @see com.sap.core.odata.api.edm.EdmCustomizableFeedMappings#getFcNsPrefix()
   */
  public String getFcNsPrefix() {
    return fcNsPrefix;
  }

  /* (non-Javadoc)
   * @see com.sap.core.odata.api.edm.EdmCustomizableFeedMappings#getFcNsUri()
   */
  public String getFcNsUri() {
    return fcNsUri;
  }

  /* (non-Javadoc)
   * @see com.sap.core.odata.api.edm.EdmCustomizableFeedMappings#getFcSourcePath()
   */
  public String getFcSourcePath() {
    return fcSourcePath;
  }

  /* (non-Javadoc)
   * @see com.sap.core.odata.api.edm.EdmCustomizableFeedMappings#getFcTargetPath()
   */
  public EdmTargetPath getFcTargetPath() {
    return fcTargetPath;
  }

  /**
   * @return <b>String</b> the custom target path
   */
  public String getCustomTargetPath() {
    return customTargetPath;
  }

  /**
   * @return <b>boolean</b>
   */
  public Boolean getFcKeepInContent() {
    return fcKeepInContent;
  }

  /**
   * Sets if this is kept in content
   * @param fcKeepInContent
   * @return {@link CustomizableFeedMappings} for method chaining
   */
  public CustomizableFeedMappings setFcKeepInContent(Boolean fcKeepInContent) {
    this.fcKeepInContent = fcKeepInContent;
    return this;
  }

  /**
   * Sets the {@link EdmContentKind}
   * @param fcContentKind
   * @return {@link CustomizableFeedMappings} for method chaining
   */
  public CustomizableFeedMappings setFcContentKind(EdmContentKind fcContentKind) {
    this.fcContentKind = fcContentKind;
    return this;
  }

  /**
   * Sets the prefix
   * @param fcNsPrefix
   * @return {@link CustomizableFeedMappings} for method chaining
   */
  public CustomizableFeedMappings setFcNsPrefix(String fcNsPrefix) {
    this.fcNsPrefix = fcNsPrefix;
    return this;
  }

  /**
   * Sets the Uri
   * @param fcNsUri
   * @return {@link CustomizableFeedMappings} for method chaining
   */
  public CustomizableFeedMappings setFcNsUri(String fcNsUri) {
    this.fcNsUri = fcNsUri;
    return this;
  }

  /**
   * Sets the source path
   * @param fcSourcePath
   * @return {@link CustomizableFeedMappings} for method chaining
   */
  public CustomizableFeedMappings setFcSourcePath(String fcSourcePath) {
    this.fcSourcePath = fcSourcePath;
    return this;
  }

  /**
   * Sets the target path
   * @param fcTargetPath
   * @return {@link CustomizableFeedMappings} for method chaining
   */
  public CustomizableFeedMappings setFcTargetPath(EdmTargetPath fcTargetPath) {
    this.fcTargetPath = fcTargetPath;
    return this;
  }

  /**
   * Sets the custom target path
   * @param customTargetPath
   * @return {@link CustomizableFeedMappings} for method chaining
   */
  public CustomizableFeedMappings setCustomTargetPath(String customTargetPath) {
    this.customTargetPath = customTargetPath;
    return this;
  }

}