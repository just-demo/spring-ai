package just.demo.mcp.elicitation;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.ai.mcp.annotation.context.StructuredElicitResult;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.modelcontextprotocol.spec.McpSchema.ElicitResult.Action.ACCEPT;

/**
 * Elicitation lets a tool ask the client (and, through it, the human user) for
 * additional structured input mid-execution via {@link McpSyncRequestContext#elicit}.
 */
@Configuration
@EnableAutoConfiguration
public class DemoMcpElicitationServer {

    public static void main(String[] args) {
        SpringApplication.run(DemoMcpElicitationServer.class,
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

        @McpTool(name = "create_ticket", description = "Create a support ticket for the given issue")
        String createTicket(@McpToolParam(description = "issue description") String issue, McpSyncRequestContext ctx) {
            if (!ctx.elicitEnabled()) {
                return "Ticket created for '" + issue + "' with default priority (client does not support elicitation)";
            }

            StructuredElicitResult<DemoContactInfo> elicitResult = ctx.elicit(
                    spec -> spec.message("Before opening the ticket, please provide a priority and a contact email."),
                    DemoContactInfo.class);

            if (elicitResult.action() == ACCEPT && elicitResult.structuredContent() != null) {
                DemoContactInfo info = elicitResult.structuredContent();
                return "Ticket #123 created for '" + issue + "' with priority " + info.priority() + ", contact email " + info.contactEmail();
            }

            return "Ticket creation for '" + issue + "' was declined by the user";
        }
    }
}
