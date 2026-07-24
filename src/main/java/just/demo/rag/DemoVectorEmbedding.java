package just.demo.rag;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

import static org.springframework.boot.WebApplicationType.NONE;

@Configuration
@EnableAutoConfiguration
public class DemoVectorEmbedding {

    public static void main(String[] args) {
        new SpringApplicationBuilder(DemoVectorEmbedding.class)
                .web(NONE)
                // this model produces much better results than the default one
                .properties("spring.ai.openai.embedding.options.model=text-embedding-3-small")
                .run(args)
                .close();
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
