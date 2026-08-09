package just.demo.mcp;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Path;

import static java.nio.file.Files.createDirectories;

@Configuration
@EnableAutoConfiguration
public class DemoMcpSimple {

    static void main() throws IOException {
        Path sandboxDir = createDirectories(Path.of("data").resolve("sandbox")).toAbsolutePath();
        SpringApplication.run(DemoMcpSimple.class,
                "--spring.ai.mcp.client.stdio.connections.filesystem.command=npx",
                "--spring.ai.mcp.client.stdio.connections.filesystem.args[0]=-y",
                "--spring.ai.mcp.client.stdio.connections.filesystem.args[1]=@modelcontextprotocol/server-filesystem",
                "--spring.ai.mcp.client.stdio.connections.filesystem.args[2]=" + sandboxDir,
                "--spring.ai.mcp.client.request-timeout=60s").close();
    }

    @Bean
    CommandLineRunner run(ChatClient.Builder chatClientBuilder, ToolCallbackProvider mcpToolCallbackProvider) {
        return _ -> {
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
