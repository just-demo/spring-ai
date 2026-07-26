package just.demo.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

/**
 * Lists the tools exposed by {@link DemoMcpCustomServer} by speaking the MCP
 * Streamable HTTP JSON-RPC protocol directly, without any Spring AI MCP client.
 */
public class DemoMcpCustomServerDescribe {

  private static final String BASE_URL = "http://localhost:8085/mcp";
  private static final String PROTOCOL_VERSION = "2025-06-18";
  private static final String SESSION_HEADER = "Mcp-Session-Id";

  private final HttpClient httpClient = HttpClient.newHttpClient();
  private final ObjectMapper objectMapper = new ObjectMapper();

  public static void main(String[] args) throws IOException, InterruptedException {
    new DemoMcpCustomServerDescribe().run();
  }

  private void run() throws IOException, InterruptedException {
    String sessionId = initialize();
    sendInitializedNotification(sessionId);

    String cursor = null;
    do {
      ObjectNode params = objectMapper.createObjectNode();
      if (cursor != null) {
        params.put("cursor", cursor);
      }
      JsonNode result = sendRequest(sessionId, 2, "tools/list", params);

      for (JsonNode tool : result.path("tools")) {
        System.out.println("- " + tool.path("name").asText());
        System.out.println("    description: " + tool.path("description").asText());
        System.out.println("    inputSchema: " + tool.path("inputSchema"));
      }

      cursor = result.hasNonNull("nextCursor") ? result.get("nextCursor").asText() : null;
    } while (cursor != null);
  }

  private String initialize() throws IOException, InterruptedException {
    ObjectNode params = objectMapper.createObjectNode();
    params.put("protocolVersion", PROTOCOL_VERSION);
    params.putObject("capabilities");
    ObjectNode clientInfo = params.putObject("clientInfo");
    clientInfo.put("name", "demo-mcp-view");
    clientInfo.put("version", "1.0.0");

    HttpResponse<String> response = post(null, 1, "initialize", params);
    String sessionId = response.headers().firstValue(SESSION_HEADER)
        .orElseThrow(() -> new IllegalStateException("Server did not return a " + SESSION_HEADER + " header"));
    readJsonRpcResult(response);
    return sessionId;
  }

  private void sendInitializedNotification(String sessionId) throws IOException, InterruptedException {
    ObjectNode notification = objectMapper.createObjectNode();
    notification.put("jsonrpc", "2.0");
    notification.put("method", "notifications/initialized");
    sendRaw(sessionId, notification);
  }

  private JsonNode sendRequest(String sessionId, int id, String method, ObjectNode params)
      throws IOException, InterruptedException {
    HttpResponse<String> response = post(sessionId, id, method, params);
    return readJsonRpcResult(response);
  }

  private HttpResponse<String> post(String sessionId, int id, String method, ObjectNode params)
      throws IOException, InterruptedException {
    ObjectNode request = objectMapper.createObjectNode();
    request.put("jsonrpc", "2.0");
    request.put("id", id);
    request.put("method", method);
    request.set("params", params);
    return sendRaw(sessionId, request);
  }

  private HttpResponse<String> sendRaw(String sessionId, ObjectNode body)
      throws IOException, InterruptedException {
    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(URI.create(BASE_URL))
        .header("Content-Type", "application/json")
        .header("Accept", "application/json, text/event-stream")
        .POST(BodyPublishers.ofString(body.toString()));
    if (sessionId != null) {
      requestBuilder.header(SESSION_HEADER, sessionId);
    }

    HttpResponse<String> response = httpClient.send(requestBuilder.build(), BodyHandlers.ofString());
    if (response.statusCode() >= 300) {
      throw new IllegalStateException(
          "Request " + body.path("method").asText() + " failed with status " + response.statusCode()
              + ": " + response.body());
    }
    return response;
  }

  private JsonNode readJsonRpcResult(HttpResponse<String> response) throws IOException {
    String body = response.body();
    if (body == null || body.isBlank()) {
      return null;
    }

    String contentType = response.headers().firstValue("Content-Type").orElse("");
    JsonNode message = contentType.contains("text/event-stream")
        ? objectMapper.readTree(extractSseData(body))
        : objectMapper.readTree(body);

    if (message.has("error")) {
      throw new IllegalStateException("MCP error response: " + message.get("error"));
    }
    return message.path("result");
  }

  private String extractSseData(String sseBody) {
    for (String line : sseBody.split("\n")) {
      if (line.startsWith("data:")) {
        return line.substring("data:".length()).trim();
      }
    }
    throw new IllegalStateException("No 'data:' line found in SSE response: " + sseBody);
  }
}
