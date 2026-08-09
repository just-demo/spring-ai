package just.demo.mcp.elicitation;

import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.annotation.McpElicitation;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static io.modelcontextprotocol.spec.McpSchema.ElicitResult.Action.ACCEPT;

/**
 * Connects to {@link DemoMcpElicitationServer} (start it first) and answers its
 * elicitation request with a bean method annotated with {@link McpElicitation}. A real
 * UI would prompt the human user here; this demo just returns a canned answer.
 */
@Configuration
@EnableAutoConfiguration
public class DemoMcpElicitationClient {

    public static void main(String[] args) {
        SpringApplication.run(DemoMcpElicitationClient.class,
                "--spring.ai.mcp.client.streamable-http.connections.demo.url=http://localhost:8085",
                "--spring.ai.mcp.client.request-timeout=60s");
    }

    @Bean
    CommandLineRunner run(ChatClient.Builder chatClientBuilder, ToolCallbackProvider mcpToolCallbackProvider) {
        return args -> {
            String result = chatClientBuilder.build().prompt()
                    .system("Include every detail from the tool result in your answer, including ticket number, priority and contact info.")
                    .user("Create a support ticket: the printer on the 3rd floor is out of toner")
                    .tools(tools -> tools.callbacks(mcpToolCallbackProvider))
                    .call()
                    .content();
            System.out.println(result);
        };
    }

    @Bean
    DemoElicitationHandler demoElicitationHandler() {
        return new DemoElicitationHandler();
    }

    @SuppressWarnings("unused")
    static class DemoElicitationHandler {

        @McpElicitation(clients = "demo")
        McpSchema.ElicitResult handleElicitationRequest(McpSchema.ElicitRequest request) {
            System.out.println("[elicitation request] " + request.message());
            return McpSchema.ElicitResult.builder(ACCEPT)
                    .content(Map.of("priority", "HIGH", "contactEmail", "test@test.com"))
                    .build();
        }
    }

}
