package just.demo.mcp.tool;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.mcp.McpToolFilter;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static java.nio.file.Files.createDirectories;

/**
 * A bean implementing {@link McpToolFilter} is auto-detected by Spring AI's MCP
 * client autoconfiguration and applied globally: it runs once per (server, tool)
 * pair discovered across every configured MCP connection, and any tool it rejects
 * never enters the {@link ToolCallbackProvider} handed to the ChatClient.
 *
 * <p>Connects to two MCP servers so filtering has something to do: the stdio
 * "filesystem" server (its MCP handshake name is "secure-filesystem-server" -
 * NOT "filesystem", which is only the connection key in mcp-servers.json) and the
 * streamable-HTTP custom server (handshake name "custom_server"). The filter below
 * blocks every tool from "custom_server" while leaving the filesystem server alone.
 *
 * <p><b>Prerequisite:</b> {@code DemoMcpCustomServer} must already be running on
 * port 8085 in a separate process before this class is run (same requirement as
 * {@code DemoMcpCustomClient}) - it is not started automatically.
 */
@Configuration
@EnableAutoConfiguration
public class DemoMcpToolFilter {

    // This demo depends on just.demo.mcp.remote.DemoMcpCustomServer, which should be started first
    public static void main(String[] args) throws IOException {
        createDirectories(Path.of("data").resolve("sandbox"));
        SpringApplication.run(DemoMcpToolFilter.class,
                "--spring.ai.mcp.client.stdio.servers-configuration=classpath:mcp-servers.json",
                "--spring.ai.mcp.client.streamable-http.connections.custom.url=http://localhost:8085",
                "--spring.ai.mcp.client.request-timeout=60s").close();
    }

    @Bean
    McpToolFilter mcpToolFilter() {
        return (connectionInfo, tool) -> !"demo_server".equals(connectionInfo.initializeResult().serverInfo().name());
    }

    @Bean
    CommandLineRunner run(List<McpSyncClient> mcpSyncClients, ToolCallbackProvider mcpToolCallbackProvider) {
        return args -> {
            System.out.println("Raw tools per MCP server (before McpToolFilter):");
            int rawCount = 0;
            for (McpSyncClient client : mcpSyncClients) {
                List<String> names = client.listTools().tools().stream().map(McpSchema.Tool::name).toList();
                rawCount += names.size();
                System.out.println("  " + client.getServerInfo().name() + ": " + names);
            }

            List<String> filteredNames = Arrays.stream(mcpToolCallbackProvider.getToolCallbacks())
                    .map(tc -> tc.getToolDefinition().name())
                    .toList();
            System.out.println();
            System.out.println("Tools registered with ChatClient (after McpToolFilter): " + filteredNames);
            System.out.println("Blocked " + (rawCount - filteredNames.size()) + " of " + rawCount + " tools");
        };
    }
}
