package com.sap.core.odata.api.edm.provider;

import com.sap.core.odata.api.edm.EdmFacets;

public class Property {

  private String name;
  private FullQualifiedName type;
  private EdmFacets facets;
  private CustomizableFeedMappings customizableFeedMappings;
  //TODO Mapping
  private Documentation documentation;
  private Annotations annotations;

  public Property(String name, FullQualifiedName type, EdmFacets facets, CustomizableFeedMappings customizableFeedMappings, Documentation documentation, Annotations annotations) {
    this.name = name;
    this.type = type;
    this.facets = facets;
    this.customizableFeedMappings = customizableFeedMappings;
    this.documentation = documentation;
    this.annotations = annotations;
  }

  public String getName() {
    return name;
  }

  public FullQualifiedName getType() {
    return type;
  }

  public EdmFacets getFacets() {
    return facets;
  }

  public CustomizableFeedMappings getCustomizableFeedMappings() {
    return customizableFeedMappings;
  }

  public Documentation getDocumentation() {
    return documentation;
  }

  public Annotations getAnnotations() {
    return annotations;
  }
}