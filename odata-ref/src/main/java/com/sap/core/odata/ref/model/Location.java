/**
 * (c) 2013 by SAP AG
 */
package com.sap.core.odata.ref.model;

/**
 * @author SAP AG
 */
public class Location {
  private String country;
  private City city;

  public Location(final String country, final String postalCode, final String cityName) {
    this.country = country;
    city = new City(postalCode, cityName);
  }

  public void setCountry(final String country) {
    this.country = country;
  }

  public String getCountry() {
    return country;
  }

  public void setCity(final City city) {
    this.city = city;
  }

  public City getCity() {
    return city;
  }

  @Override
  public String toString() {
    return String.format("%s, %s", country, city.toString());
  }

}
