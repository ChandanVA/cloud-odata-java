package com.sap.core.odata.core.debug;

import java.io.IOException;
import java.util.Map;

import com.sap.core.odata.api.commons.HttpStatusCodes;
import com.sap.core.odata.core.ep.util.JsonStreamWriter;

/**
 * @author SAP AG
 */
public class DebugInfoResponse implements DebugInfo {

  private final HttpStatusCodes status;
  private final Map<String, String> headers;

  public DebugInfoResponse(final HttpStatusCodes status, final Map<String, String> headers) {
    this.status = status;
    this.headers = headers;
  }

  @Override
  public String getName() {
    return "Response";
  }

  @Override
  public void appendJson(JsonStreamWriter jsonStreamWriter) throws IOException {
    jsonStreamWriter.beginObject();

    if (status != null)
      jsonStreamWriter.name("status")
          .beginObject()
          .name("code").unquotedValue(Integer.toString(status.getStatusCode())).separator()
          .namedStringValueRaw("info", status.getInfo())
          .endObject();

    if (!headers.isEmpty()) {
      if (status != null)
        jsonStreamWriter.separator();

      jsonStreamWriter.name("headers")
          .beginObject();
      boolean first = true;
      for (final String name : headers.keySet()) {
        if (!first)
          jsonStreamWriter.separator();
        first = false;
        jsonStreamWriter.namedStringValue(name, headers.get(name));
      }
      jsonStreamWriter.endObject();
    }

    jsonStreamWriter.endObject();
  }
}
