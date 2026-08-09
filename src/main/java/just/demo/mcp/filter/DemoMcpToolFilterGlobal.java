package just.demo.mcp.filter;

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
 */
@Configuration
@EnableAutoConfiguration
public class DemoMcpToolFilterGlobal {

    // This demo depends on just.demo.mcp.remote.DemoMcpCustomServer, which should be started first
    static void main() throws IOException {
        createDirectories(Path.of("data").resolve("sandbox"));
        SpringApplication.run(DemoMcpToolFilterGlobal.class,
                "--spring.ai.mcp.client.stdio.servers-configuration=classpath:mcp-servers.json",
                "--spring.ai.mcp.client.streamable-http.connections.custom.url=http://localhost:8085",
                "--spring.ai.mcp.client.request-timeout=60s").close();
    }

    @Bean
    CommandLineRunner run(List<McpSyncClient> mcpSyncClients, ToolCallbackProvider mcpToolCallbackProvider) {
        return _ -> {
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

    @Bean
    McpToolFilter mcpToolFilter() {
        return (connectionInfo, _) -> !"demo_server".equals(connectionInfo.initializeResult().serverInfo().name());
    }
}
