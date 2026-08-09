package just.demo.openai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class DemoPromptStream {

    static void main(String[] args) {
        SpringApplication.run(DemoPromptStream.class, args);
    }

    @Bean
    CommandLineRunner run(ChatClient.Builder chatClientBuilder) {
        return _ -> {
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
