package just.demo.mcp;

import static java.nio.file.Files.createDirectories;

import java.io.IOException;
import java.nio.file.Path;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class DemoMcpSimple {

  public static void main(String[] args) throws IOException {
    Path sandboxDir = createDirectories(Path.of("data").resolve("sandbox")).toAbsolutePath();
    new SpringApplicationBuilder(DemoMcpSimple.class)
        .properties(
            "spring.ai.mcp.client.stdio.connections.filesystem.command=npx",
            "spring.ai.mcp.client.stdio.connections.filesystem.args[0]=-y",
            "spring.ai.mcp.client.stdio.connections.filesystem.args[1]=@modelcontextprotocol/server-filesystem",
            "spring.ai.mcp.client.stdio.connections.filesystem.args[2]=" + sandboxDir,
            "spring.ai.mcp.client.request-timeout=60s")
        .run(args)
        .close();
  }

  @Bean
  CommandLineRunner run(ChatClient.Builder chatClientBuilder, ToolCallbackProvider mcpToolCallbackProvider) {
    return args -> {
      String result = chatClientBuilder.build().prompt()
          // The path should be correctly specified to make it work
          .system("Respond to user. If the user message contains email write it to data/sandbox/emails.txt")
          .user("Here is my email test@test.com")
          .tools(tools -> tools.callbacks(mcpToolCallbackProvider))
          .call()
          .content();
      System.out.println(result);
    };
  }
}
