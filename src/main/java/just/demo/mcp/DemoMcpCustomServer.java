package just.demo.mcp;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.writeString;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

import static org.springframework.boot.WebApplicationType.SERVLET;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableAutoConfiguration
public class DemoMcpCustomServer {

  public static void main(String[] args) {
    new SpringApplicationBuilder(DemoMcpCustomServer.class)
        .web(SERVLET)
        .run(
            "--server.port=8085",
            "--spring.ai.mcp.server.enabled=true",
            "--spring.ai.mcp.server.protocol=STREAMABLE",
            "--spring.ai.mcp.server.name=custom_server",
            "--spring.ai.model.chat=none",
            "--spring.ai.model.embedding=none");
  }

  @RequiredArgsConstructor
  @SuppressWarnings("unused")
  static class FileTools {

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
  FileTools fileTools() {
    Path sandboxDir = Path.of("data").resolve("sandbox").toAbsolutePath();
    return new FileTools(sandboxDir);
  }
}
