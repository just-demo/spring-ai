package just.demo.azure;

import com.openai.azure.AzureOpenAIServiceVersion;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class DemoPromptEntity {

    static void main(String[] args) {
        SpringApplication.run(DemoPromptEntity.class, args);
    }

    @Bean
    CommandLineRunner run(ChatClient.Builder chatClientBuilder) {
        return _ -> {
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

    @Bean
    OpenAiChatModel openAiChatModel() {
        return OpenAiChatModel.builder()
                .options(OpenAiChatOptions.builder()
                        // Here are examples how the config values may look like.
                        // It has not been verified with real values.
                        .baseUrl("https://<...>.openai.azure.com/")
                        .apiKey("<...>")
                        .deploymentName("gpt-4-turbo-<...>")
                        .azureOpenAIServiceVersion(AzureOpenAIServiceVersion.latestStableVersion())
                        .build())
                .build();
    }

    record DemoEntity(String name, String version, String description) {
    }
}
