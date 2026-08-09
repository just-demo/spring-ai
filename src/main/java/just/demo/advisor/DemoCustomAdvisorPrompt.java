package just.demo.advisor;

import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class DemoCustomAdvisorPrompt {

    static void main(String[] args) {
        SpringApplication.run(DemoCustomAdvisorPrompt.class, args);
    }

    @Bean
    CommandLineRunner run(ChatClient.Builder chatClientBuilder) {
        return _ -> {
            String answer = chatClientBuilder.build()
                    .prompt()
                    .advisors(new DemoCustomAdvisor(1), new DemoCustomAdvisor(2))
                    .user("Who are you?")
                    .call()
                    .content();
            System.out.println(answer);
        };
    }

    private record DemoCustomAdvisor(int order) implements CallAdvisor {
        @Override
        public @NonNull String getName() {
            return this.getClass().getSimpleName() + getOrder();
        }

        @Override
        public int getOrder() {
            return order;
        }

        @Override
        public @NonNull ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
            System.out.println(getName() + " before");
            ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);
            System.out.println(getName() + " after: " + chatClientResponse.chatResponse().getMetadata().getUsage());
            return chatClientResponse;
        }
    }
}
