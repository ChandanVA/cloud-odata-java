package com.sap.core.odata.testutils.helper;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XpathEngine;
import org.junit.Assert;

public class XMLUnitHelper {

  /**
   * initilaze XML namespaces of XPathEngine
   * @param namespaces prefix - namespace mapping 
   */
  public static void registerXmlNs(Map<String, String> namespaces) {
    NamespaceContext ctx = new SimpleNamespaceContext(namespaces);
    XMLUnit.setXpathNamespaceContext(ctx);
    XpathEngine engine = XMLUnit.newXpathEngine();
    engine.setNamespaceContext(ctx);
  }

  
  /**
   * Verify order of <code>tags</code> in given <code>xmlString</code>.
   * 
   * @param xmlString
   * @param toCheckTags
   */
  public static void verifyTagOrdering(String xmlString, String... toCheckTags) {
    int lastTagPos = -1;

    for (String tagName : toCheckTags) {
      Pattern p = Pattern.compile(tagName);
      Matcher m = p.matcher(xmlString);

      if (m.find()) {
        int currentTagPos = m.start();
        Assert.assertTrue("Tag with name '" + tagName + "' is not in correct order. Expected order is '" + Arrays.toString(toCheckTags) + "'.",
            lastTagPos < currentTagPos);
        lastTagPos = currentTagPos;
      } else {
        Assert.fail("Expected tag '" + tagName + "' was not found in input [\n\n" + xmlString + "\n\n].");
      }

    }
  }
}
