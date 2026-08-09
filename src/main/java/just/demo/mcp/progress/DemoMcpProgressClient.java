package just.demo.mcp.progress;

import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.annotation.McpProgress;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
@EnableAutoConfiguration
public class DemoMcpProgressClient {

    public static void main(String[] args) {
        SpringApplication.run(DemoMcpProgressClient.class,
                "--spring.ai.mcp.client.streamable-http.connections.demo.url=http://localhost:8085",
                "--spring.ai.mcp.client.request-timeout=60s");
    }

    @Bean
    CommandLineRunner run(ChatClient.Builder chatClientBuilder, ToolCallbackProvider mcpToolCallbackProvider) {
        return args -> {
            String result = chatClientBuilder.build().prompt()
                    .user("Generate a report on 'quarterly sales'")
                    .tools(tools -> tools.callbacks(mcpToolCallbackProvider).context("progressToken", UUID.randomUUID().toString()))
                    .call()
                    .content();
            System.out.println(result);
        };
    }

    @Bean
    DemoProgressListener progressListener() {
        return new DemoProgressListener();
    }

    @SuppressWarnings("unused")
    static class DemoProgressListener {

        @McpProgress(clients = "demo")
        void onProgress(McpSchema.ProgressNotification notification) {
            System.out.println("[progress] " + notification.progress() + "% - " + notification.message());
        }
    }
}
