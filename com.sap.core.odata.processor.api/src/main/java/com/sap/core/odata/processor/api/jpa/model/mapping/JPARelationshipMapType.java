package com.sap.core.odata.processor.api.jpa.model.mapping;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 * 
 * The default name for EDM navigation property is derived from JPA relationship
 * name. This can be overriden using JPARelationshipMapType.
 * 
 * 
 * <p>
 * Java class for JPARelationshipMapType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="JPARelationshipMapType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="JPARelationship" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;simpleContent>
 *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *                 &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/extension>
 *             &lt;/simpleContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "JPARelationshipMapType", propOrder = { "jpaRelationship" })
public class JPARelationshipMapType {

  @XmlElement(name = "JPARelationship")
  protected List<JPARelationshipMapType.JPARelationship> jpaRelationship;

  /**
   * Gets the value of the jpaRelationship property.
   * 
   * <p>
   * This accessor method returns a reference to the live list, not a
   * snapshot. Therefore any modification you make to the returned list will
   * be present inside the JAXB object. This is why there is not a
   * <CODE>set</CODE> method for the jpaRelationship property.
   * 
   * <p>
   * For example, to add a new item, do as follows:
   * 
   * <pre>
   * getJPARelationship().add(newItem);
   * </pre>
   * 
   * 
   * <p>
   * Objects of the following type(s) are allowed in the list
   * {@link JPARelationshipMapType.JPARelationship }
   * 
   * 
   */
  public List<JPARelationshipMapType.JPARelationship> getJPARelationship() {
    if (jpaRelationship == null) {
      jpaRelationship = new ArrayList<JPARelationshipMapType.JPARelationship>();
    }
    return jpaRelationship;
  }

  /**
   * <p>
   * Java class for anonymous complex type.
   * 
   * <p>
   * The following schema fragment specifies the expected content contained
   * within this class.
   * 
   * <pre>
   * &lt;complexType>
   *   &lt;simpleContent>
   *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
   *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
   *     &lt;/extension>
   *   &lt;/simpleContent>
   * &lt;/complexType>
   * </pre>
   * 
   * 
   */
  @XmlAccessorType(XmlAccessType.FIELD)
  @XmlType(name = "", propOrder = { "value" })
  public static class JPARelationship {

    @XmlValue
    protected String value;
    @XmlAttribute(name = "name", required = true)
    protected String name;

    /**
     * Gets the value of the value property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getValue() {
      return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setValue(final String value) {
      this.value = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getName() {
      return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setName(final String value) {
      name = value;
    }

  }

}
