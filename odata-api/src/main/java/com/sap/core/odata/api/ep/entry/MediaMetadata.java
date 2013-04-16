package com.sap.core.odata.api.ep.entry;

/**
 * @author SAP AG
 */
public interface MediaMetadata {

  public abstract String getEditLink();

  public abstract String getContentType();

  public abstract String getEtag();

  public abstract String getSourceLink();
}
