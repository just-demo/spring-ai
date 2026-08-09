package just.demo.mcp.progress;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.stream.IntStream;

@Configuration
@EnableAutoConfiguration
public class DemoMcpProgressServer {

    static void main() {
        SpringApplication.run(DemoMcpProgressServer.class,
                "--server.port=8085",
                "--spring.main.web-application-type=servlet",
                "--spring.ai.mcp.server.enabled=true",
                "--spring.ai.mcp.server.protocol=STREAMABLE",
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

        @McpTool(name = "generate_report", description = "Generate a report for the given topic")
        String generateReport(@McpToolParam(description = "report topic") String topic, McpSyncRequestContext ctx) {
            IntStream.of(20, 40, 60, 80, 100).forEach(percent ->
                    // You could also sleep here to emulate processing delay
                    ctx.progress(spec -> spec.percentage(percent).message("Generating report on " + topic)));
            return "Report on " + topic + " is ready";
        }
    }
}
