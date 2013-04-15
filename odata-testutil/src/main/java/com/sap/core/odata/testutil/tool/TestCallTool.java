/**
 * (c) 2013 by SAP AG
 */
package com.sap.core.odata.testutil.tool;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.http.HttpHeaders;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.sap.core.odata.testutil.TestUtilRuntimeException;
import com.sap.core.odata.testutil.tool.core.AcceptHeaderCaller;
import com.sap.core.odata.testutil.tool.core.CallerConfig;
import com.sap.core.odata.testutil.tool.core.CallerResultHandler;
import com.sap.core.odata.testutil.tool.core.TestPath;
import com.sap.core.odata.testutil.tool.core.TestResult;
import com.sap.core.odata.testutil.tool.core.TestResultFilter;

/**
 * Simple tool to define and process calls against an OData service and collect the result of these calls.
 * Currently this is only used and configured to do ContentNegotiation test calls for different URI-Types and collect/print the
 * result in JIRA compatible markup syntax.
 * @author SAP AG
 */
public class TestCallTool {

  public static final TestResultFilter SPECIAL_FILTER = new TestResultFilter() {
    @Override
    public boolean filterResults(final Set<TestResult> results) {
      if (results.size() > 1) {
        TestResult last = null;
        for (TestResult testResult : results) {
          if (contains(testResult.getPath(), "json") || contains(testResult.getRequestHeader(HttpHeaders.ACCEPT), "json")) {
            return false;
          }
          //
          if (last != null) {
            String lastStatusCode = last.getSomeValue(CallerResultHandler.RESPONSE_STATUS_CODE);
            String currentStatusCode = testResult.getSomeValue(CallerResultHandler.RESPONSE_STATUS_CODE);

            if (isDifferent(lastStatusCode, currentStatusCode)) {
              return true;
            } else {
              String lastResponseHeader = last.getResponseHeader(HttpHeaders.CONTENT_TYPE, true);
              String currentResponseHeader = testResult.getResponseHeader(HttpHeaders.CONTENT_TYPE, true);

              if (lastResponseHeader == null || currentResponseHeader == null) {
                return false;
              } else if (isDifferent(lastResponseHeader, currentResponseHeader)) {
                return true;
              }
            }
          }
          last = testResult;
        }
      }
      return false;
    }

    private boolean contains(final String value, final String containing) {
      if (value == null) {
        return false;
      }
      return value.contains(containing.toLowerCase(Locale.ENGLISH));
    }

    private boolean isDifferent(final String first, final String second) {
      if (first == null && second == null) {
        return false;
      } else if (first == null) {
        return true;
      } else if (first.equals(second)) {
        return false;
      }
      return true;
    }
  };

  private static final String ACCEPT_HEADER_REQUEST_JIRA_FILENAME = "./target/AcceptHeaderRequest.jira";
  private static final Logger LOG = Logger.getLogger(TestCallTool.class);

  /**
   * simple main to start the tool
   * 
   * @param args not used
   */
  public static void main(final String[] args) {
    try {
      activateLoggingToJiraMarkupFile(ACCEPT_HEADER_REQUEST_JIRA_FILENAME);

      final List<String> paths = Arrays.asList(
          "/", // URI0 et seq.
          "/?$format=xml",
          "/?$format=atom",
          "/?$format=json",
          "/Employees", // URI1 et seq.
          // "/Managers", "/Rooms",
          "/Employees?$format=xml",
          "/Employees?$format=atom", // 
          "/Employees?$format=json", // 
          "/Employees('1')", // URI2 et seq.
          "/Employees('1')?$format=xml",
          "/Employees('1')?$format=atom",
          "/Employees('1')?$format=json",
          "/Employees('1')/Location", // URI3
          "/Employees('1')/Location/Country", // URI4
          "/Employees('1')/Age", // URI5
          "/Employees('1')/ne_Room", // URI6A
          "/Employees('1')/$links/ne_Room", // URI7
          "/$metadata", // URI8
          "/Employees('1')/$value", // URI17 (not supported?)
          "/Employees('1')/Age/$value" // no specific URI-Type (variation of URI17 ?)
      );

      final List<String> headerValues = Arrays.asList(
          "", // for request with none 'Accept-Header' set
          "text/plain",
          "application/xml",
          "application/json",
          "application/atom+xml",
          "application/atomsvc+xml",
          "text/plain; charset=utf-8",
          "application/xml; charset=utf-8",
          "application/json; charset=utf-8",
          "application/atom+xml; charset=utf-8",
          "application/atomsvc+xml; charset=utf-8"
          );
      final List<TestPath> testPaths = TestPath.createTestPaths(paths, headerValues);
      // for a reduced test set
      //    List<TestPath> testPaths = TestPath.createTestPaths(Arrays.asList("/", "/Employees"), 
      //        Arrays.asList("", "application/xml"));

      final CallerResultHandler handler = new CallerResultHandler();

      final String localBaseUrl = "http://localhost:8080/com.sap.core.odata.ref.web/ReferenceScenario.svc";
      final CallerConfig localConfig = new CallerConfig(localBaseUrl, handler, testPaths);
      AcceptHeaderCaller.create(localConfig).call();

      //      String nwcBaseUrl = "https://refodata.prod.jpaas.sapbydesign.com/com.sap.core.odata.ref.web/ReferenceScenario.svc";
      //      CallerConfig nwcConfig = new CallerConfig(nwcBaseUrl, handler, testPaths).setProxy("proxy:8080");
      //      AcceptHeaderCaller.create(nwcConfig).call();

      //      String gmdUrl = "http://ldcigmd.wdf.sap.corp:50055/sap/bc/odata";
      //      CallerConfig gmdConfig = new CallerConfig(gmdUrl, handler, testPaths).setBasicAuthCredentials("user:pwd");
      //      AcceptHeaderCaller.create(gmdConfig).call();

      final String result = handler.getJiraResult(SPECIAL_FILTER);
      LOG.info("h2. Accept-Header (executed  at: " + new SimpleDateFormat().format(new Date()) + ")\n");
      LOG.info(result);
    } catch (Exception e) {
      throw new TestUtilRuntimeException(e);
    }
  }

  /**
   * Configure LOG4J logger which log the pure log message to given filename.
   * In these context this is used to simple log (write) the generated jira markup to a file.
   * 
   * @param filename
   * @throws IOException
   */
  private static void activateLoggingToJiraMarkupFile(String filename) throws IOException {
    final Layout layout = new PatternLayout("%m");
    if (filename == null) {
      filename = ACCEPT_HEADER_REQUEST_JIRA_FILENAME;
    }
    final Appender fileAppender = new FileAppender(layout, filename, false);
    LOG.addAppender(fileAppender);
    LOG.setLevel(Level.INFO);
  }
}
