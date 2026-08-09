package just.demo.mcp.logging;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class DemoMcpLoggingServer {

    public static void main(String[] args) {
        SpringApplication.run(DemoMcpLoggingServer.class,
                "--server.port=8085",
                "--spring.main.web-application-type=servlet",
                "--spring.ai.mcp.server.enabled=true",
                "--spring.ai.mcp.server.protocol=STREAMABLE",
                "--spring.ai.mcp.server.name=demo_server",
                "--spring.ai.model.chat=none",
                "--spring.ai.model.embedding=none");
    }

    @Bean
    OrderTools orderTools() {
        return new OrderTools();
    }

    @SuppressWarnings("unused")
    static class OrderTools {

        @McpTool(name = "place_order", description = "Place an order for the given item")
        String placeOrder(@McpToolParam(description = "item name") String item, McpSyncRequestContext ctx) {
            ctx.info("Validating item: " + item);
            ctx.info("Checking inventory for: " + item);
            ctx.warn("Inventory is low for: " + item);
            ctx.info("Order placed for: " + item);
            return "Order placed for " + item;
        }
    }
}
