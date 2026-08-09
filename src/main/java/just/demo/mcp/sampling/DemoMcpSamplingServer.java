package just.demo.mcp.sampling;

import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class DemoMcpSamplingServer {

    static void main() {
        SpringApplication.run(DemoMcpSamplingServer.class,
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

        @McpTool(name = "summarize_notes", description = "Summarize the given notes using the client's LLM")
        String summarizeNotes(@McpToolParam(description = "raw notes text") String notes, McpSyncRequestContext ctx) {
            if (!ctx.sampleEnabled()) {
                return "Client does not support sampling. Raw notes: " + notes;
            }

            McpSchema.CreateMessageResult result = ctx.sample(spec -> spec
                    .systemPrompt("You are a helpful assistant that summarizes text in one short sentence.")
                    .message(notes));
            return ((McpSchema.TextContent) result.content()).text();
        }
    }
}
