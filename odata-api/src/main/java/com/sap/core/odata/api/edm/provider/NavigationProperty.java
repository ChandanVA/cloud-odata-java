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
package com.sap.core.odata.api.edm.provider;

import java.util.List;

import com.sap.core.odata.api.edm.FullQualifiedName;

/**
 * Objects of this Class represent a navigation property
 * @author SAP AG
 */
public class NavigationProperty {

  private String name;
  private FullQualifiedName relationship;
  private String fromRole;
  private String toRole;
  private Documentation documentation;
  private Mapping mapping;
  private List<AnnotationAttribute> annotationAttributes;
  private List<AnnotationElement> annotationElements;

  /**
   * @return <b>String</b> name of this navigation property
   */
  public String getName() {
    return name;
  }

  /**
   * @return {@link FullQualifiedName} of the relationship
   */
  public FullQualifiedName getRelationship() {
    return relationship;
  }

  /**
   * @return <b>String</b> name of the role this navigation is comming from
   */
  public String getFromRole() {
    return fromRole;
  }

  /**
   * @return <b>String</b> name of the role this navigation is going to
   */
  public String getToRole() {
    return toRole;
  }

  /**
   * @return {@link Mapping} of this navigation property
   */
  public Mapping getMapping() {
    return mapping;
  }

  /**
   * @return {@link Documentation} documentation
   */
  public Documentation getDocumentation() {
    return documentation;
  }

  /**
   * @return List of {@link AnnotationAttribute} annotation attributes
   */
  public List<AnnotationAttribute> getAnnotationAttributes() {
    return annotationAttributes;
  }

  /**
   * @return List of {@link AnnotationElement} annotation elements
   */
  public List<AnnotationElement> getAnnotationElements() {
    return annotationElements;
  }

  /**
   * Sets the name of this {@link NavigationProperty}
   * @param name
   * @return {@link NavigationProperty} for method chaining
   */
  public NavigationProperty setName(final String name) {
    this.name = name;
    return this;
  }

  /**
   * Sets the {@link FullQualifiedName} for the relationship of this {@link NavigationProperty}
   * @param relationship
   * @return {@link NavigationProperty} for method chaining
   */
  public NavigationProperty setRelationship(final FullQualifiedName relationship) {
    this.relationship = relationship;
    return this;
  }

  /**
   * Sets the role this {@link NavigationProperty} is comming from
   * @param fromRole
   * @return {@link NavigationProperty} for method chaining
   */
  public NavigationProperty setFromRole(final String fromRole) {
    this.fromRole = fromRole;
    return this;
  }

  /**
   * Sets the role this {@link NavigationProperty} is going to
   * @param toRole
   * @return {@link NavigationProperty} for method chaining
   */
  public NavigationProperty setToRole(final String toRole) {
    this.toRole = toRole;
    return this;
  }

  /**
   * Sets the {@link Mapping} for this {@link NavigationProperty}
   * @param mapping
   * @return {@link NavigationProperty} for method chaining
   */
  public NavigationProperty setMapping(final Mapping mapping) {
    this.mapping = mapping;
    return this;
  }

  /**
   * Sets the {@link Documentation} for this {@link NavigationProperty}
   * @param documentation
   * @return {@link NavigationProperty} for method chaining
   */
  public NavigationProperty setDocumentation(final Documentation documentation) {
    this.documentation = documentation;
    return this;
  }

  /**
   * Sets the List of {@link AnnotationAttribute} for this {@link NavigationProperty}
   * @param annotationAttributes
   * @return {@link NavigationProperty} for method chaining
   */
  public NavigationProperty setAnnotationAttributes(final List<AnnotationAttribute> annotationAttributes) {
    this.annotationAttributes = annotationAttributes;
    return this;
  }

  /**
   * Sets the List of {@link AnnotationElement} for this {@link NavigationProperty}
   * @param annotationElements
   * @return {@link NavigationProperty} for method chaining
   */
  public NavigationProperty setAnnotationElements(final List<AnnotationElement> annotationElements) {
    this.annotationElements = annotationElements;
    return this;
  }
}
