package com.sap.core.odata.core.uri.enums;

import java.util.ArrayList;

public enum UriType {
  /**
   * Service document
   */
  URI0(SystemQueryOption.$format),
  /**
   * Entity set
   */
  URI1(SystemQueryOption.$format, SystemQueryOption.$filter, SystemQueryOption.$inlinecount, SystemQueryOption.$orderby, SystemQueryOption.$skiptoken, SystemQueryOption.$skip, SystemQueryOption.$top, SystemQueryOption.$expand, SystemQueryOption.$select),
  /**
   * Entity set with key predicate
   */
  URI2(SystemQueryOption.$format, SystemQueryOption.$filter, SystemQueryOption.$expand, SystemQueryOption.$select),
  /**
   * Complex property of an entity
   */
  URI3(SystemQueryOption.$format),
  /**
   * Simple property of a complex property of an entity
   */
  URI4(SystemQueryOption.$format),
  /**
   * Simple property of an entity
   */
  URI5(SystemQueryOption.$format),
  /**
   * Navigation property of an entity with target multiplicity '1' or '0..1'
   */
  URI6A(SystemQueryOption.$format, SystemQueryOption.$filter, SystemQueryOption.$expand, SystemQueryOption.$select),
  /**
   * Navigation property of an entity with target multiplicity '*'
   */
  URI6B(SystemQueryOption.$format, SystemQueryOption.$filter, SystemQueryOption.$inlinecount, SystemQueryOption.$orderby, SystemQueryOption.$skiptoken, SystemQueryOption.$skip, SystemQueryOption.$top, SystemQueryOption.$expand, SystemQueryOption.$select),
  /**
   * Link to a single entity
   */
  URI7A(SystemQueryOption.$format, SystemQueryOption.$filter, SystemQueryOption.$select),
  /**
   * Link to multiple entities
   */
  URI7B(SystemQueryOption.$format, SystemQueryOption.$filter, SystemQueryOption.$inlinecount, SystemQueryOption.$skiptoken, SystemQueryOption.$skip, SystemQueryOption.$top, SystemQueryOption.$expand),
  /**
   * Metadata document
   */
  URI8(),
  /**
   * Batch request
   */
  URI9(),
  /**
   * Function import returning a single entity
   */
  URI10(SystemQueryOption.$format),
  /**
   * Function import returning a collection of complex type values
   */
  URI11(SystemQueryOption.$format),
  /**
   * Function import returning a single instance of a complex type
   */
  URI12(SystemQueryOption.$format),
  /**
   * Function import returning a collection of primitive type values
   */
  URI13(SystemQueryOption.$format),
  /**
   * Function import returning a single primitive type value
   */
  URI14(SystemQueryOption.$format),
  /**
   * Count of an entity set
   */
  URI15(SystemQueryOption.$filter, SystemQueryOption.$orderby, SystemQueryOption.$skip, SystemQueryOption.$top, SystemQueryOption.$expand),
  /**
   * Count of a single entity
   */
  URI16(SystemQueryOption.$filter, SystemQueryOption.$expand),
  /**
   * Media resource of an entity
   */
  URI17(SystemQueryOption.$format, SystemQueryOption.$filter),
  /**
   * Count of link to a single entity
   */
  URI50A(SystemQueryOption.$format, SystemQueryOption.$select),
  /**
   * Count of links to multiple entities
   */
  URI50B(SystemQueryOption.$format, SystemQueryOption.$inlinecount, SystemQueryOption.$skiptoken, SystemQueryOption.$skip, SystemQueryOption.$top);

  private ArrayList<SystemQueryOption> whiteList = new ArrayList<SystemQueryOption>();

  private UriType(SystemQueryOption... compatibleQueryOptions) {
    for (SystemQueryOption queryOption : compatibleQueryOptions)
      whiteList.add(queryOption);
  }

  public boolean isCompatible(SystemQueryOption queryOption) {
    return whiteList.contains(queryOption);
  }

}
