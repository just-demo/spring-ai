package just.demo.openai;

import static org.springframework.boot.WebApplicationType.NONE;

import java.util.Scanner;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class DemoChatMemory {

  public static void main(String[] args) {
    new SpringApplicationBuilder(DemoChatMemory.class).web(NONE).run(args).close();
  }

  @Bean
  CommandLineRunner run(ChatClient.Builder chatClientBuilder) {
    return args -> {
      ChatClient chatClient = chatClientBuilder
          .defaultSystem("Respond to user")
          .defaultAdvisors(MessageChatMemoryAdvisor.builder(MessageWindowChatMemory.builder().build()).build())
          .build();
      Scanner scanner = new Scanner(System.in);
      while (true) {
        System.out.print("You: ");
        String message = scanner.nextLine();
        if (message.isBlank()) {
          break;
        }
        String answer = chatClient.prompt().user(message).call().content();
        System.out.println("Assistant: " + answer);
      }
    };
  }
}
