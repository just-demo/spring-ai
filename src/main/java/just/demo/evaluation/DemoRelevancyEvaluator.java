package just.demo.evaluation;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableAutoConfiguration
public class DemoRelevancyEvaluator {

    public static void main(String[] args) {
        SpringApplication.run(DemoRelevancyEvaluator.class, args);
    }

    @Bean
    CommandLineRunner run(ChatClient.Builder chatClientBuilder) {
        return args -> {
            ChatClient chatClient = chatClientBuilder.build();
            RelevancyEvaluator evaluator = RelevancyEvaluator.builder()
                    .chatClientBuilder(chatClientBuilder)
                    .build();

            evaluate(evaluator, "What is the capital of France?",
                    chatClient.prompt("What is the capital of France?").call().content());

            // Deliberately irrelevant answer, to show the evaluator failing it.
            evaluate(evaluator, "What is the capital of France?", "Bananas are a good source of potassium.");
        };
    }

    private static void evaluate(RelevancyEvaluator evaluator, String question, String answer) {
        System.out.println("Q: " + question);
        System.out.println("A: " + answer);
        EvaluationResponse response = evaluator.evaluate(new EvaluationRequest(question, List.of(), answer));
        System.out.println("Relevant: " + response.isPass() + " (score=" + response.getScore() + ")");
        System.out.println();
    }
}
