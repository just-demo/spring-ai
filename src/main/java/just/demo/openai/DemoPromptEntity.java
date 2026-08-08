package just.demo.openai;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class DemoPromptEntity {

  public static void main(String[] args) {
    SpringApplication.run(DemoPromptEntity.class, args);
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

  // Description annotations are not necessary, putting them just for visibility
  record DemoEntity(
      @JsonPropertyDescription("Name of the assistant or model") String name,
      @JsonPropertyDescription("Version of the assistant or model") String version,
      @JsonPropertyDescription("Short description of what the assistant or model is") String description) {
  }
}
