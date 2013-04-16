/*******************************************************************************
 * Copyright 2013 SAP AG
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.sap.core.odata.api.ep;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import com.sap.core.odata.api.ODataCallback;
import com.sap.core.odata.api.commons.InlineCount;
import com.sap.core.odata.api.uri.ExpandSelectTreeNode;

/**
 * {@link EntityProviderWriteProperties} contains all additional properties which are necessary to <b>write (serialize)</b> an
 * {@link com.sap.core.odata.api.ep.entry.ODataEntry} into an specific format (e.g. <code>XML</code> or <code>JSON</code> or ...).
 */
public class EntityProviderWriteProperties {

  private URI serviceRoot;
  private String mediaResourceMimeType;
  private InlineCount inlineCountType;
  private Integer inlineCount;
  private String nextLink;
  private ExpandSelectTreeNode expandSelectTree;
  private Map<String, ODataCallback> callbacks = Collections.emptyMap();
  private URI selfLink;

  private EntityProviderWriteProperties() {}

  /**
   * Gets the self link from an application. May be null.
   * @return the self link
   */
  public final URI getSelfLink() {
    return selfLink;
  }

  /**
   * Gets the service root.
   * @return the service root
   */
  public final URI getServiceRoot() {
    return serviceRoot;
  }

  /**
   * Gets the MIME type of the media resource.
   * @return the MIME type of the media resource
   */
  public final String getMediaResourceMimeType() {
    return mediaResourceMimeType;
  }

  /**
  * Gets the type of the inlinecount request from the system query option.
  * @return the type of the inlinecount request from the system query option
  */
  public final InlineCount getInlineCountType() {
    return inlineCountType;
  }

  public final Map<String, ODataCallback> getCallbacks() {
    return callbacks;
  }

  /**
   * Gets the expand select tree data structure resulting from $expand and $select query options.
   * @return a paresed tree structure representing the $expand and $select
   */
  public final ExpandSelectTreeNode getExpandSelectTree() {
    return expandSelectTree;
  }

  /**
  * Gets the inlinecount.
   * @return the inlinecount as Integer
   * @see #getInlineCountType
   */
  public final Integer getInlineCount() {
    return inlineCount;
  }

  /**
   * Gets the next link used for server-side paging of feeds.
   * @return the next link
   */
  public final String getNextLink() {
    return nextLink;
  }

  public static ODataEntityProviderPropertiesBuilder serviceRoot(final URI serviceRoot) {
    return new ODataEntityProviderPropertiesBuilder().serviceRoot(serviceRoot);
  }

  public static class ODataEntityProviderPropertiesBuilder {
    private final EntityProviderWriteProperties properties = new EntityProviderWriteProperties();

    /**
     * @param mediaResourceMimeType  the mediaResourceMimeType to set
     */
    public final ODataEntityProviderPropertiesBuilder mediaResourceMimeType(final String mediaResourceMimeType) {
      properties.mediaResourceMimeType = mediaResourceMimeType;
      return this;
    }

    /**
     * @param inlineCountType  the inlineCountType to set
     */
    public final ODataEntityProviderPropertiesBuilder inlineCountType(final InlineCount inlineCountType) {
      properties.inlineCountType = inlineCountType;
      return this;
    }

    /**
     * @param inlineCount  the inlineCount to set
     */
    public final ODataEntityProviderPropertiesBuilder inlineCount(final Integer inlineCount) {
      properties.inlineCount = inlineCount;
      return this;
    }

    /**
     * @param serviceRoot
     */
    public final ODataEntityProviderPropertiesBuilder serviceRoot(final URI serviceRoot) {
      properties.serviceRoot = serviceRoot;
      return this;
    }

    /**
     * @param nextLink Next link to render feeds with server side paging. Should usually contain a skiptoken.
     */
    public ODataEntityProviderPropertiesBuilder nextLink(final String nextLink) {
      properties.nextLink = nextLink;
      return this;
    }

    /**
     * Build properties object.
     * @return assembled properties object
     */
    public final EntityProviderWriteProperties build() {
      return properties;
    }

    /**
     * Set a expand select tree which results from $expand and $select query parameter. Usually the data structure is constructed 
     * by the uri parser.
     * @param expandSelectTree data structure
     * @return properties builder
     */
    public ODataEntityProviderPropertiesBuilder expandSelectTree(final ExpandSelectTreeNode expandSelectTree) {
      properties.expandSelectTree = expandSelectTree;
      return this;
    }

    public ODataEntityProviderPropertiesBuilder callbacks(final Map<String, ODataCallback> callbacks) {
      properties.callbacks = callbacks;
      return this;
    }

    public ODataEntityProviderPropertiesBuilder selfLink(final URI selfLink) {
      properties.selfLink = selfLink;
      return this;
    }

    public ODataEntityProviderPropertiesBuilder fromProperties(final EntityProviderWriteProperties properties) {
      this.properties.mediaResourceMimeType = properties.getMediaResourceMimeType();
      this.properties.inlineCountType = properties.getInlineCountType();
      this.properties.inlineCount = properties.getInlineCount();
      this.properties.nextLink = properties.getNextLink();
      this.properties.expandSelectTree = properties.getExpandSelectTree();
      this.properties.callbacks = properties.getCallbacks();
      this.properties.selfLink = properties.getSelfLink();
      return this;
    }

  }

  public static ODataEntityProviderPropertiesBuilder fromProperties(final EntityProviderWriteProperties properties) {
    final ODataEntityProviderPropertiesBuilder b = EntityProviderWriteProperties.serviceRoot(properties.getServiceRoot());
    b.fromProperties(properties);
    return b;
  }
}
