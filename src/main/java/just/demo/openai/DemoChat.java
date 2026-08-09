package just.demo.openai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Configuration
@EnableAutoConfiguration
public class DemoChat {

    static void main(String[] args) {
        SpringApplication.run(DemoChat.class, args);
    }

    @Bean
    CommandLineRunner run(ChatClient.Builder chatClientBuilder) {
        return _ -> {
            ChatClient chatClient = chatClientBuilder
                    .defaultSystem("Respond to user")
                    .build();
            Scanner scanner = new Scanner(System.in);
            List<Message> history = new ArrayList<>();
            while (true) {
                System.out.print("You: ");
                String message = scanner.nextLine();
                if (message.isBlank()) {
                    break;
                }

                history.add(new UserMessage(message));
                String response = chatClient.prompt()
                        .messages(history)
                        .call()
                        .content();
                history.add(new AssistantMessage(response));
                System.out.println("Assistant: " + response);
            }
        };
    }
}
