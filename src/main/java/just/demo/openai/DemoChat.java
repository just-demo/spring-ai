package just.demo.openai;

import static org.springframework.boot.WebApplicationType.NONE;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class DemoChat {

  public static void main(String[] args) {
    new SpringApplicationBuilder(DemoChat.class).web(NONE).run(args).close();
  }

  @Bean
  CommandLineRunner run(ChatClient.Builder chatClientBuilder) {
    return args -> {
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
