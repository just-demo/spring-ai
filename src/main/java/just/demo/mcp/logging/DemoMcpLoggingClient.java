package just.demo.mcp.logging;

import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.annotation.McpLogging;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class DemoMcpLoggingClient {

    static void main() {
        SpringApplication.run(DemoMcpLoggingClient.class,
                "--spring.ai.mcp.client.streamable-http.connections.demo.url=http://localhost:8085",
                "--spring.ai.mcp.client.request-timeout=60s");
    }

    @Bean
    CommandLineRunner run(ChatClient.Builder chatClientBuilder, ToolCallbackProvider mcpToolCallbackProvider) {
        return _ -> {
            String result = chatClientBuilder.build().prompt()
                    .user("Place an order for 'coffee beans'")
                    .tools(tools -> tools.callbacks(mcpToolCallbackProvider))
                    .call()
                    .content();
            System.out.println(result);
        };
    }

    @Bean
    DemoLoggingHandler demoLoggingHandler() {
        return new DemoLoggingHandler();
    }

    @SuppressWarnings("unused")
    static class DemoLoggingHandler {

        @McpLogging(clients = "demo")
        void onServerLog(McpSchema.LoggingLevel level, String source, String message) {
            System.out.println("[server log] " + level + " " + source + ": " + message);
        }

    }
}
