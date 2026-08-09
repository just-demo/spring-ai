package just.demo.mcp.local;

import io.modelcontextprotocol.client.McpClient;
import just.demo.mcp.remote.DemoMcpRemoteClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.customizer.McpClientCustomizer;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.time.Duration;

/**
 * Spawns {@link DemoMcpLocalServer} as a stdio subprocess using the current JVM's own
 * classpath (server and client live in the same module, so this avoids needing a
 * pre-built/pre-started separate process, unlike {@link DemoMcpRemoteClient}).
 */
@Configuration
@EnableAutoConfiguration
public class DemoMcpLocalClient {

    public static void main(String[] args) {
        String javaBin = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        String classpath = System.getProperty("java.class.path");
        SpringApplication.run(DemoMcpLocalClient.class,
                "--spring.ai.mcp.client.stdio.connections.local.command=" + javaBin,
                "--spring.ai.mcp.client.stdio.connections.local.args[0]=-cp",
                "--spring.ai.mcp.client.stdio.connections.local.args[1]=" + classpath,
                "--spring.ai.mcp.client.stdio.connections.local.args[2]=" + DemoMcpLocalServer.class.getName(),
                "--spring.ai.mcp.client.request-timeout=60s").close();
    }

    @Bean
    CommandLineRunner run(ChatClient.Builder chatClientBuilder, ToolCallbackProvider mcpToolCallbackProvider) {
        return args -> {
            String result = chatClientBuilder.build().prompt()
                    .user("Reverse the text 'spring ai'")
                    .tools(tools -> tools.callbacks(mcpToolCallbackProvider))
                    .call()
                    .content();
            System.out.println(result);
        };
    }

    @Bean
    McpClientCustomizer<McpClient.SyncSpec> mcpClientCustomizer() {
        return (name, spec) -> spec.initializationTimeout(Duration.ofSeconds(60));
    }
}
