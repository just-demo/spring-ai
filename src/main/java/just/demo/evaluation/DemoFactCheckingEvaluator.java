package just.demo.evaluation;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.document.Document;
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
public class DemoFactCheckingEvaluator {

    static void main(String[] args) {
        SpringApplication.run(DemoFactCheckingEvaluator.class, args);
    }

    private static final String DOCUMENT = "JustDemo company was founded in 2016.";

    @Bean
    CommandLineRunner run(ChatClient.Builder chatClientBuilder) {
        return _ -> {
            FactCheckingEvaluator evaluator = FactCheckingEvaluator.builder(chatClientBuilder).build();

            evaluate(evaluator, "JustDemo was founded in 2024.");
            evaluate(evaluator, "JustDemo was founded in 2016.");
            evaluate(evaluator, "JustDemo was founded this century");
            evaluate(evaluator, "JustDemo was founded last century");
        };
    }

    private static void evaluate(FactCheckingEvaluator evaluator, String claim) {
        System.out.println("Claim: " + claim);
        EvaluationResponse response = evaluator.evaluate(
                new EvaluationRequest(List.of(new Document(DOCUMENT)), claim));
        System.out.println("Supported by document: " + response.isPass());
        System.out.println("Evaluation response: " + response);
        System.out.println();
    }
}
