package just.demo.rag;

import static java.lang.Math.sqrt;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class DemoVectorSimilarity {

  public static void main(String[] args) {
    new SpringApplicationBuilder(DemoVectorSimilarity.class)
        // this model produces much better results than the default one
        .properties("spring.ai.openai.embedding.options.model=text-embedding-3-small")
        .run(args)
        .close();
  }

  @Bean
  CommandLineRunner run(EmbeddingModel embeddingModel) {
    return args -> {
      testSimilarity(embeddingModel, "William Shakespeare", "Shakespeare");
      testSimilarity(embeddingModel, "William Shakespeare", "Shakespeare William");
      testSimilarity(embeddingModel, "William Shakespeare", "Shakespeare Will");
      testSimilarity(embeddingModel, "William Shakespeare", "Will Shakespeare");
      testSimilarity(embeddingModel, "William Shakespeare", "Hamlet");
      testSimilarity(embeddingModel, "William Shakespeare", "literature");
      testSimilarity(embeddingModel, "William Shakespeare", "boxing");
      testSimilarity(embeddingModel, "Muhammad Ali", "Hamlet");
      testSimilarity(embeddingModel, "Muhammad Ali", "literature");
      testSimilarity(embeddingModel, "Muhammad Ali", "boxing");
    };
  }

  private static void testSimilarity(EmbeddingModel embeddingModel, String text1, String text2) {
    float[] vector1 = embeddingModel.embed(text1);
    float[] vector2 = embeddingModel.embed(text2);
    System.out.println(text1 + " + " + text2 + " = " + cosineSimilarity(vector1, vector2) * 100 + "%");
  }

  // TODO: try to replace with a third-party library
  private static double cosineSimilarity(float[] vector1, float[] vector2) {
    double dotProduct = 0;
    double norm1 = 0;
    double norm2 = 0;
    for (int i = 0; i < vector1.length; i++) {
      dotProduct += vector1[i] * vector2[i];
      norm1 += vector1[i] * vector1[i];
      norm2 += vector2[i] * vector2[i];
    }
    return dotProduct / (sqrt(norm1) * sqrt(norm2));
  }
}
