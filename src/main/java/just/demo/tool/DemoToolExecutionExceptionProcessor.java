package just.demo.tool;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.execution.DefaultToolExecutionExceptionProcessor;
import org.springframework.ai.tool.execution.ToolExecutionExceptionProcessor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class DemoToolExecutionExceptionProcessor {

    static void main(String[] args) {
        SpringApplication.run(DemoToolExecutionExceptionProcessor.class, args);
    }

    @Bean
    ToolExecutionExceptionProcessor toolExecutionExceptionProcessor() {
        // The default value alwaysThrow = false, change the value to see the difference
        return new DefaultToolExecutionExceptionProcessor(true);
    }

    @Bean
    CommandLineRunner run(ChatClient.Builder chatClientBuilder, DemoTools demoTools) {
        return _ -> {
            ChatClient chatClient = chatClientBuilder.defaultTools(demoTools).build();
            try {
                String answer = chatClient.prompt()
                        .user("What is the balance of account 'demo'?")
                        .call()
                        .content();
                System.out.println("Model answer: " + answer); // when alwaysThrow = false
            } catch (RuntimeException e) {
                System.out.println("Tool exception propagated to caller: " + e); // when alwaysThrow = true
            }
        };
    }

    @Bean
    DemoTools demoTools() {
        return new DemoTools();
    }

    @SuppressWarnings("unused")
    static class DemoTools {
        @Tool(description = "Look up the account balance for the given account id")
        String getBalance(String accountId) {
            throw new IllegalStateException("Account " + accountId + " is locked");
        }
    }
}
