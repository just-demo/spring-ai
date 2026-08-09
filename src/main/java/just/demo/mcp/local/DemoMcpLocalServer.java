package just.demo.mcp.local;

import just.demo.mcp.remote.DemoMcpRemoteServer;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * An MCP server exposed over stdio ("local" transport: the server is spawned as a
 * subprocess and talks to the client over stdin/stdout), as opposed to streamable-HTTP
 * ("remote" transport, see {@link DemoMcpRemoteServer}).
 */
@Configuration
@EnableAutoConfiguration
public class DemoMcpLocalServer {

    static void main() {
        SpringApplication.run(DemoMcpLocalServer.class,
                "--spring.main.web-application-type=none",
                // A stdio server must not write anything but MCP protocol frames to stdout, hence disabling the web server,
                // the startup banner and console logging.
                "--spring.main.banner-mode=off",
                "--logging.level.root=ERROR",
                "--spring.ai.mcp.server.enabled=true",
                "--spring.ai.mcp.server.stdio=true",
                "--spring.ai.mcp.server.name=demo_server",
                "--spring.ai.model.chat=none",
                "--spring.ai.model.embedding=none");
    }

    @Bean
    DemoTools demoTools() {
        return new DemoTools();
    }

    @SuppressWarnings("unused")
    static class DemoTools {
        @McpTool(name = "reverse_text", description = "Reverse the given text")
        String reverseText(@McpToolParam(description = "text to reverse") String text) {
            return new StringBuilder(text).reverse().toString();
        }
    }
}
