package just.demo.embedding.rag;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class DemoRag {

  public static void main(String[] args) {
    SpringApplication.run(DemoRag.class, args);
  }

  private static final List<String> DOCUMENTS = List.of(
          "JustDemo company is for educational purposes only.",
          "It has only one contributor.");

  @Bean
  CommandLineRunner run(ChatClient.Builder chatClientBuilder, EmbeddingModel embeddingModel) {
    return args -> {
      VectorStore vectorStore = getOrCreateVectorStore(embeddingModel);
      ChatClient chatClient = chatClientBuilder
          .defaultOptions(ChatOptions.builder().temperature(0.7))
          .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore).build())
          .build();

      testQuestion(chatClient, "Is JustDemo suitable for commercial use?");
      testQuestion(chatClient, "Is the team big?");
      testQuestion(chatClient, "When was the first release?");
    };
  }

  private static void testQuestion(ChatClient chatClient, String question) {
    System.out.println(question);
    String answer = chatClient.prompt()
        .user(question)
        .call()
        .content();
    System.out.println(answer);
  }

  private static VectorStore getOrCreateVectorStore(EmbeddingModel embeddingModel) {
    File dbFile = Path.of("data").resolve("rag_db.json").toFile();
    SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();
    if (dbFile.exists()) {
      vectorStore.load(dbFile);
    } else {
      vectorStore.add(DOCUMENTS.stream().map(Document::new).toList());
      vectorStore.save(dbFile);
    }

    return vectorStore;
  }
}
