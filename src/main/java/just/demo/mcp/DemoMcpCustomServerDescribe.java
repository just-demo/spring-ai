package just.demo.mcp;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema.Tool;

/**
 * Lists the tools exposed by {@link DemoMcpCustomServer} using the official MCP Java SDK's
 * streamable-http client, without any Spring AI MCP client.
 */
public class DemoMcpCustomServerDescribe {

  public static void main(String[] args) {
    HttpClientStreamableHttpTransport transport =
        HttpClientStreamableHttpTransport.builder("http://localhost:8085").build();

    try (McpSyncClient client = McpClient.sync(transport).build()) {
      client.initialize();
      for (Tool tool : client.listTools().tools()) {
        System.out.println("- " + tool.name());
        System.out.println("    description: " + tool.description());
        System.out.println("    inputSchema: " + tool.inputSchema());
      }
    }
  }
}
