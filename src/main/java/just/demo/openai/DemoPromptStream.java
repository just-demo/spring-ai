package just.demo.openai;

import static org.springframework.boot.WebApplicationType.NONE;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class DemoPromptStream {

  public static void main(String[] args) {
    new SpringApplicationBuilder(DemoPromptStream.class).web(NONE).run(args).close();
  }

  @Bean
  CommandLineRunner run(ChatClient.Builder chatClientBuilder) {
    return args -> {
      chatClientBuilder.build().prompt()
          .user("Who are you?")
          .stream()
          .content()
          .doOnNext(System.out::print)
          .blockLast();
      // Forcing the exit to prevet handing for a minute
      System.exit(0);
    };
  }
}
