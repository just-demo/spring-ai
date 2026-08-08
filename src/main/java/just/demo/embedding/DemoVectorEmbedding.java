package just.demo.embedding;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@EnableAutoConfiguration
public class DemoVectorEmbedding {

    public static void main(String[] args) {
        SpringApplication.run(DemoVectorEmbedding.class, args);
    }

    @Bean
    CommandLineRunner run(EmbeddingModel embeddingModel) {
        return args -> {
            float[] vector = embeddingModel.embed("William Shakespeare");
            System.out.println(vector.length);
            System.out.println(Arrays.toString(vector));
        };
    }
}
