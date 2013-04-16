package com.sap.core.odata.api;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is a container for the supported ODataServiceVersions
 * @author SAP AG
 *
 */
public class ODataServiceVersion {

  private static final Pattern DATASERVICEVERSIONPATTERN = Pattern.compile("(\\p{Digit}+\\.\\p{Digit}+)(:?;.*)?");

  /**
   * ODataServiceVersion 1.0
   */
  public static final String V10 = "1.0";
  /**
   * ODataServiceVersion 2.0
   */
  public static final String V20 = "2.0";
  /**
   * ODataServiceVersion 3.0
   */
  public static final String V30 = "3.0";

  public static boolean validateDataServiceVersion(String version)
  {
    Matcher matcher = DATASERVICEVERSIONPATTERN.matcher(version);
    if (!matcher.matches())
      throw new IllegalArgumentException(version);

    String possibleDataServiceVersion = matcher.group(1);

    if (V10.equals(possibleDataServiceVersion)) {
      return true;
    } else if (V20.equals(possibleDataServiceVersion)) {
      return true;
    } else if (V30.equals(possibleDataServiceVersion)) {
      return true;
    }

    return false;
  }

  /**
   * actual > comparedTo
   * @param actual
   * @param comparedTo
   * @return
   */
  public static boolean isBiggerThan(String actual, String comparedTo) {
    if (!validateDataServiceVersion(comparedTo) || !validateDataServiceVersion(actual))
      throw new IllegalArgumentException("Illegal arguments: " + comparedTo + " and " + actual);

    double me = Double.parseDouble(extractDataServiceVersionString(actual));
    double other = Double.parseDouble(extractDataServiceVersionString(comparedTo));

    return me > other;
  }
  
  public static String extractDataServiceVersionString(String rawDataServiceVersion){
    if(rawDataServiceVersion != null){
      String[] pattern = rawDataServiceVersion.split(";");
      return pattern[0];
    }
    
    return null;
  }

}
