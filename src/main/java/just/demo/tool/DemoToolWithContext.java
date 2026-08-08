package just.demo.tool;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * The username is not a tool argument the model fills in - it comes from trusted
 * application code via {@link ChatClient.ToolSpec#context(String, Object)} and is
 * read back inside the tool via {@link ToolContext}. The model can never see or
 * choose it, so it can't be tricked into acting as a different user.
 */
@Configuration
@EnableAutoConfiguration
public class DemoToolWithContext {

    private static final Map<String, List<String>> ORDERS_BY_USER = Map.of(
            "alice", List.of("Order #1001: Espresso Machine", "Order #1002: Coffee Beans 1kg"),
            "bob", List.of("Order #2001: Standing Desk"));

    public static void main(String[] args) {
        SpringApplication.run(DemoToolWithContext.class, args);
    }

    @Bean
    CommandLineRunner run(ChatClient.Builder chatClientBuilder) {
        return args -> {
            ChatClient chatClient = chatClientBuilder
                    .defaultTools(new OrderTools())
                    .build();

            testQuestion(chatClient, "alice", "What are my recent orders?");
            testQuestion(chatClient, "bob", "What are my recent orders?");
        };
    }

    private static void testQuestion(ChatClient chatClient, String username, String question) {
        System.out.println("[" + username + "] " + question);
        String answer = chatClient.prompt()
                .user(question)
                .tools(tools -> tools.context("username", username))
                .call()
                .content();
        System.out.println(answer);
        System.out.println();
    }

    @SuppressWarnings("unused")
    private static class OrderTools {

        @Tool(description = "Get the current user's recent orders")
        List<String> getMyOrders(ToolContext toolContext) {
            String username = (String) toolContext.getContext().get("username");
            List<String> orders = ORDERS_BY_USER.getOrDefault(username, List.of());
            System.out.println("  getMyOrders() [username=" + username + "] -> " + orders);
            return orders;
        }
    }
}
