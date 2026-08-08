package just.demo.mcp;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class DemoMcpCustomClient {

  public static void main(String[] args) {
    new SpringApplicationBuilder(DemoMcpCustomClient.class)
        .properties(
            "spring.ai.mcp.client.streamable-http.connections.custom.url=http://localhost:8085",
            "spring.ai.mcp.client.request-timeout=60s")
        .run(args)
        .close();
  }

  @Bean
  CommandLineRunner run(ChatClient.Builder chatClientBuilder, ToolCallbackProvider mcpToolCallbackProvider) {
    return args -> {
      String result = chatClientBuilder.build().prompt()
          .system("Respond to user. If the user message contains email write it to emails.txt")
          .user("Here is my email test@test.com")
          .tools(tools -> tools.callbacks(mcpToolCallbackProvider))
          .call()
          .content();
      System.out.println(result);
    };
  }
}
