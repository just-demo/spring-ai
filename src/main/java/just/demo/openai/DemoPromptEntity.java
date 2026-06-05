package just.demo.openai;

import static org.springframework.boot.WebApplicationType.NONE;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class DemoPromptEntity {

  public static void main(String[] args) {
    new SpringApplicationBuilder(DemoPromptEntity.class).web(NONE).run(args).close();
  }

  @Bean
  CommandLineRunner run(ChatClient.Builder chatClientBuilder) {
    return args -> {
      ResponseEntity<ChatResponse, DemoEntity> response = chatClientBuilder.build()
          .prompt()
          .user("Who are you?")
          .call()
          .responseEntity(DemoEntity.class);

      ChatResponseMetadata metadata = response.getResponse().getMetadata();
      System.out.println("Model: " + metadata.getModel());
      System.out.println("Usage: " + metadata.getUsage());
      System.out.println("Entity: " + response.entity());
    };
  }

  record DemoEntity(String name, String version, String description) {

  }
}
