package just.demo.rag;

import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class DemoVectorSearch {

  public static void main(String[] args) {
    new SpringApplicationBuilder(DemoVectorSearch.class)
        // this model produces much better results than the default one
        .properties("spring.ai.openai.embedding.options.model=text-embedding-3-small")
        .run(args)
        .close();
  }

  private static final Map<String, List<String>> DOCUMENTS = Map.of(
          "literature", List.of("William Shakespeare was an English playwright, poet and actor."),
          "art", List.of(
                  "Leonardo di ser Piero da Vinci was an Italian polymath of the High Renaissance who was active as a "
                          + "painter, draughtsman, engineer, scientist, theorist, sculptor, and architect."),
          "film", List.of(
                  "Sir Charles Spencer Chaplin was an English comic actor, filmmaker, and composer who rose to fame in the "
                          + "era of silent film."),
          "sport", List.of("Muhammad Ali was an American professional boxer and social activist."));

  @Bean
  CommandLineRunner run(EmbeddingModel embeddingModel) {
    return args -> {
      VectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();

      DOCUMENTS.forEach((category, people) -> people.forEach(biography ->
          vectorStore.add(List.of(new Document(biography, Map.of("category", category))))));

      testSearch(vectorStore, "Literature");
      testSearch(vectorStore, "Football");
      testSearch(vectorStore, "Hiking");
      testSearch(vectorStore, "Cinema");
      testSearch(vectorStore, "Hamlet");
    };
  }

  private static void testSearch(VectorStore vectorStore, String text) {
    List<Document> results = vectorStore.similaritySearch(SearchRequest.builder()
        .query(text)
        .topK(10)
        .similarityThreshold(0)
        .build());
    System.out.println("\n" + text + ":");
    results.forEach(result -> System.out.printf("%.2f / %s / %s%n",
        result.getScore(), result.getMetadata().get("category"), result.getText()));
  }
}
