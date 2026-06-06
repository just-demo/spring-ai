package just.demo.ollama;

import static org.springframework.boot.WebApplicationType.NONE;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class DemoPrompt {

  public static void main(String[] args) {
    new SpringApplicationBuilder(DemoPrompt.class)
        .web(NONE)
        .run("--spring.ai.model.chat=ollama", "--spring.ai.ollama.chat.options.model=llama3.2")
        .close();
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
