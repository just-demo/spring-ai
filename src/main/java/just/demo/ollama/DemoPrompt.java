package just.demo.ollama;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class DemoPrompt {

    static void main() {
        SpringApplication.run(DemoPrompt.class,
                "--spring.ai.model.chat=ollama",
                "--spring.ai.ollama.chat.options.model=llama3.2");
    }

    @Bean
    CommandLineRunner run(ChatClient.Builder chatClientBuilder) {
        return _ -> {
            String answer = chatClientBuilder.build()
                    .prompt()
                    .user("Who are you?")
                    .call()
                    .content();
            System.out.println(answer);
        };
    }
}
