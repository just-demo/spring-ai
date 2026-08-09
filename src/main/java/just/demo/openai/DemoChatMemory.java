package just.demo.openai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Scanner;

import static java.util.UUID.randomUUID;
import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@Configuration
@EnableAutoConfiguration
public class DemoChatMemory {

    static void main(String[] args) {
        SpringApplication.run(DemoChatMemory.class, args);
    }

    @Bean
    CommandLineRunner run(ChatClient.Builder chatClientBuilder) {
        return _ -> {
            ChatClient chatClient = chatClientBuilder
                    .defaultSystem("Respond to user")
                    .defaultAdvisors(MessageChatMemoryAdvisor.builder(MessageWindowChatMemory.builder().build()).build())
                    .build();
            String conversationId = randomUUID().toString();
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("You: ");
                String message = scanner.nextLine();
                if (message.isBlank()) {
                    break;
                }

                String answer = chatClient.prompt()
                        .user(message)
                        .advisors(advisor -> advisor.param(CONVERSATION_ID, conversationId))
                        .call()
                        .content();
                System.out.println("Assistant: " + answer);
            }
        };
    }
}
