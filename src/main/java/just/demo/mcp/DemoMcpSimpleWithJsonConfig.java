package just.demo.mcp;

import static java.nio.file.Files.createDirectories;

import java.io.IOException;
import java.nio.file.Path;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class DemoMcpSimpleWithJsonConfig {

  public static void main(String[] args) throws IOException {
    createDirectories(Path.of("data").resolve("sandbox"));
    SpringApplication.run(DemoMcpSimpleWithJsonConfig.class,
            "--spring.ai.mcp.client.stdio.servers-configuration=classpath:mcp-servers.json",
            "--spring.ai.mcp.client.request-timeout=60s").close();
  }

  @Bean
  CommandLineRunner run(ChatClient.Builder chatClientBuilder, ToolCallbackProvider mcpToolCallbackProvider) {
    return args -> {
      String result = chatClientBuilder.build().prompt()
          // The MCP filesystem server root is already data/sandbox, so paths are relative to it
          .system("Respond to user. If the user message contains email write it to emails.txt")
          .user("Here is my email test@test.com")
          .tools(tools -> tools.callbacks(mcpToolCallbackProvider))
          .call()
          .content();
      System.out.println(result);
    };
  }
}
