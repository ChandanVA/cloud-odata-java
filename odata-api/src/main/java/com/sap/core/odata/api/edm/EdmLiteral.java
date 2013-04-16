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
package com.sap.core.odata.api.edm;

/**
 * @com.sap.core.odata.DoNotImplement
 * <p>Representation of a simple-typed literal</p>
 * <p>The literal is in default representation. The URI representation differs
 * from the default representation mainly in the additional presence of type
 * indicators (prefixes or suffixes, respectively); since the type information
 * is stored here separately, the default representation is more appropriate.
 * Should the URI representation be needed, it can be re-created by calling
 * {@link EdmSimpleType#toUriLiteral}.</p>
 * @author SAP AG
 * @see EdmLiteralKind
 */
public final class EdmLiteral {
  private final EdmSimpleType type;
  private final String literal;

  /**
   * Creates an {@link EdmLiteral} object out of the simple type and the literal string.
   * @param type {@link EdmSimpleType} simple type
   * @param literal String literal in default (<em>not</em> URI) representation
   */
  public EdmLiteral(final EdmSimpleType type, final String literal) {
    this.type = type;
    this.literal = literal;
  }

  /**
   * Gets the simple type of the literal.
   * @return {@link EdmSimpleType} object
   */
  public EdmSimpleType getType() {
    return type;
  }

  /**
   * Gets the literal String.
   * @return {@link String} literal in default (<em>not</em> URI) representation
   */
  public String getLiteral() {
    return literal;
  }

  @Override
  public String toString() {
    return "type=" + type + ", literal=" + literal;
  }
}
