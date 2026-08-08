package just.demo.openai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class DemoPrompt {

  public static void main(String[] args) {
    SpringApplication.run(DemoPrompt.class, args);
  }

  @Bean
  CommandLineRunner run(ChatClient.Builder chatClientBuilder) {
    return args -> {
      String answer = chatClientBuilder.build()
          .prompt()
          .user("Who are you?")
          .call()
          .content();
      System.out.println(answer);
    };
  }
}
