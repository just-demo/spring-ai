package just.demo.mcp.filter;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static java.nio.file.Files.createDirectories;

/**
 * Unlike {@link DemoMcpToolFilterGlobal}, which filters globally via an {@code McpToolFilter}
 * bean applied once for the whole app, this demo filters the injected {@code List<McpSyncClient>}
 * inline, inside the {@link CommandLineRunner}, and builds the {@link ToolCallback} list for that
 * one invocation only - no {@code McpToolFilter} bean is declared at all.
 */
@Configuration
@EnableAutoConfiguration
public class DemoMcpToolFilterPerRequest {

    // This demo depends on just.demo.mcp.remote.DemoMcpRemoteServer, which should be started first
    static void main() throws IOException {
        createDirectories(Path.of("data").resolve("sandbox"));
        SpringApplication.run(DemoMcpToolFilterPerRequest.class,
                "--spring.ai.mcp.client.stdio.servers-configuration=classpath:mcp-servers.json",
                "--spring.ai.mcp.client.streamable-http.connections.custom.url=http://localhost:8085",
                "--spring.ai.mcp.client.request-timeout=60s").close();
    }

    @Bean
    CommandLineRunner run(ChatClient.Builder chatClientBuilder, List<McpSyncClient> mcpSyncClients) {
        return _ -> {
            System.out.println("Raw tools per MCP server:");
            for (McpSyncClient client : mcpSyncClients) {
                List<String> names = client.listTools().tools().stream().map(McpSchema.Tool::name).toList();
                System.out.println("  " + client.getServerInfo().name() + ": " + names);
            }

            // Filtering happens here, inline, instead of in a globally-applied McpToolFilter bean
            List<McpSyncClient> allowedClients = mcpSyncClients.stream()
                    .filter(client -> !"demo_server".equals(client.getServerInfo().name()))
                    .toList();
            List<ToolCallback> toolCallbacks = SyncMcpToolCallbackProvider.syncToolCallbacks(allowedClients);

            System.out.println();
            System.out.println("Tools available to this request (after inline filtering): "
                    + toolCallbacks.stream().map(tc -> tc.getToolDefinition().name()).toList());

            String result = chatClientBuilder.build().prompt()
                    .system("Respond to user. If the user message contains email write it to emails.txt")
                    .user("Here is my email test@test.com")
                    .tools(tools -> tools.callbacks(toolCallbacks))
                    .call()
                    .content();
            System.out.println(result);
        };
    }
}
