package com.sap.core.odata.api.uri;

import java.util.List;
import java.util.Map;

import com.sap.core.odata.api.edm.EdmException;
import com.sap.core.odata.api.processor.ODataPathSegment;

/**
 * Uri Parser interface
 * @author SAP AG
 */
public interface UriParser {
  
  /**
   * parses the given path segments and query parameter
   * @param pathSegments to be parsed
   * @param queryParameters to be parsed
   * @return {@link UriParserResult} the parsing result
   * @throws UriSyntaxException
   */
  public UriParserResult parse(List<ODataPathSegment> pathSegments, Map<String, String> queryParameters) throws UriSyntaxException, UriNotMatchingException, EdmException;

}
