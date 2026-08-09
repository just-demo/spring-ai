package just.demo.mcp.sampling;

import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.mcp.annotation.McpSampling;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

import static io.modelcontextprotocol.spec.McpSchema.Role.ASSISTANT;
import static io.modelcontextprotocol.spec.McpSchema.Role.USER;
import static java.util.stream.Collectors.joining;

@Configuration
@EnableAutoConfiguration
public class DemoMcpSamplingClient {

    static void main() {
        SpringApplication.run(DemoMcpSamplingClient.class,
                "--spring.ai.mcp.client.streamable-http.connections.demo.url=http://localhost:8085",
                "--spring.ai.mcp.client.request-timeout=60s");
    }

    @Bean
    CommandLineRunner run(ChatClient.Builder chatClientBuilder, ToolCallbackProvider mcpToolCallbackProvider) {
        return _ -> {
            String result = chatClientBuilder.build().prompt()
                    .user("Summarize these notes: 'Met with the team, discussed Q3 roadmap, "
                            + "agreed to prioritize the mobile app redesign, next check-in in two weeks.'")
                    .tools(tools -> tools.callbacks(mcpToolCallbackProvider))
                    .call()
                    .content();
            System.out.println(result);
        };
    }

    @Bean
    DemoSamplingHandler demoSamplingHandler(ChatModel chatModel) {
        return new DemoSamplingHandler(chatModel);
    }

    @SuppressWarnings("unused")
    @RequiredArgsConstructor
    static class DemoSamplingHandler {

        private final ChatModel chatModel;

        @McpSampling(clients = "demo")
        CreateMessageResult handleSamplingRequest(McpSchema.CreateMessageRequest request) {
            List<Message> messages = new ArrayList<>();
            if (request.systemPrompt() != null) {
                messages.add(new SystemMessage(request.systemPrompt()));
            }
            String userText = request.messages().stream()
                    .filter(m -> m.content() instanceof McpSchema.TextContent && m.role() == USER)
                    .map(m -> ((McpSchema.TextContent) m.content()).text())
                    .collect(joining("\n"));
            messages.add(new UserMessage(userText));

            System.out.println("[sampling] request received: " + userText);
            ChatResponse response = chatModel.call(new Prompt(messages));
            return CreateMessageResult.builder(ASSISTANT,
                            response.getResult().getOutput().getText(),
                            response.getMetadata().getModel())
                    .build();
        }
    }
}
