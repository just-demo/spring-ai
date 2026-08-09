package just.demo.mcp.remote;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.writeString;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

@Configuration
@EnableAutoConfiguration
public class DemoMcpRemoteServer {

    static void main() {
        SpringApplication.run(DemoMcpRemoteServer.class,
                "--server.port=8085",
                "--spring.main.web-application-type=servlet",
                "--spring.ai.mcp.server.enabled=true",
                "--spring.ai.mcp.server.protocol=streamable",
                "--spring.ai.mcp.server.name=demo_server",
                "--spring.ai.model.chat=none",
                "--spring.ai.model.embedding=none");
    }

    @RequiredArgsConstructor
    @SuppressWarnings("unused")
    static class DemoTools {

        private final Path sandboxDir;

        @McpTool(
                name = "write_file",
                description = "Write the given content to a file. If the file already exists, the content will be appended.")
        String writeFile(
                @McpToolParam(description = "file name to write to") String file,
                @McpToolParam(description = "file content to write") String content) {
            try {
                Path filePath = sandboxDir.resolve(file);
                createDirectories(filePath.getParent());
                writeString(filePath, content + "\n", CREATE, APPEND);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return "ok";
        }
    }

    @Bean
    DemoTools demoTools() {
        Path sandboxDir = Path.of("data").resolve("sandbox").toAbsolutePath();
        return new DemoTools(sandboxDir);
    }
}
