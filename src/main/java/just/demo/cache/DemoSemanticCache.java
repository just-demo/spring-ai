package just.demo.cache;

import java.util.List;

import org.springframework.ai.chat.cache.semantic.SemanticCache;
import org.springframework.ai.chat.cache.semantic.SemanticCacheAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.ai.vectorstore.redis.cache.semantic.DefaultSemanticCache;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import redis.clients.jedis.JedisPooled;

@Configuration
@EnableAutoConfiguration
public class DemoSemanticCache {

  public static void main(String[] args) {
    SpringApplication.run(DemoSemanticCache.class, args);
  }

  // overrides the auto-configured RedisVectorStore bean (@ConditionalOnMissingBean) so this demo
  // can force initializeSchema(true) and apply consistentEmbeddingModel() without affecting other
  // demos, which keep getting the inert, schema-less auto-configured bean
  @Bean
  RedisVectorStore vectorStore(JedisConnectionFactory connectionFactory, EmbeddingModel embeddingModel) {
    JedisPooled jedisClient = new JedisPooled(connectionFactory.getHostName(), connectionFactory.getPort());
    return RedisVectorStore.builder(jedisClient, consistentEmbeddingModel(embeddingModel))
        .indexName("semantic-cache-index")
        .prefix("semantic-cache:")
        // DefaultSemanticCache stores the cached response as "response"/"response_text" metadata and
        // reads it back on a cache hit; without declaring these fields Redis never returns them on
        // search, so get() silently falls through to a fresh LLM call even when a similar doc is found
        .metadataFields(RedisVectorStore.MetadataField.text("response"),
            RedisVectorStore.MetadataField.text("response_text"),
            RedisVectorStore.MetadataField.numeric("ttl"),
            RedisVectorStore.MetadataField.tag("context_hash"))
        .initializeSchema(true)
        .build();
  }

  @Bean
  CommandLineRunner run(ChatClient.Builder chatClientBuilder, RedisVectorStore vectorStore) {
    return args -> {
      SemanticCache semanticCache = DefaultSemanticCache.builder()
          .vectorStore(vectorStore)
          .similarityThreshold(0.85)
          .build();
      // start each run with an empty cache so the demo always shows a clean miss -> hit -> miss cycle
      semanticCache.clear();

      SemanticCacheAdvisor semanticCacheAdvisor = SemanticCacheAdvisor.builder().cache(semanticCache).build();
      ChatClient chatClient = chatClientBuilder.defaultAdvisors(semanticCacheAdvisor).build();

      ask(chatClient, "What is the capital of France?");
      ask(chatClient, "What's the capital city of France?"); // semantically similar -> served from cache
      ask(chatClient, "What is the capital of Germany?"); // unrelated -> cache miss again
    };
  }

  private static void ask(ChatClient chatClient, String question) {
    long start = System.currentTimeMillis();
    String answer = chatClient.prompt().user(question).call().content();
    long elapsed = System.currentTimeMillis() - start;
    System.out.printf("[%5dms] Q: %s%nA: %s%n%n", elapsed, question, answer);
  }

  /**
   * RedisVectorStore.doAdd() embeds documents via embed(List, EmbeddingOptions, BatchingStrategy)
   * with an empty EmbeddingOptions, which silently drops the configured embedding model and falls
   * back to OpenAI's API-side default - a different model than query-time embed(String) calls use.
   * Routing storage through embed(List of String) keeps storage/query embeddings on the same model.
   */
  private static EmbeddingModel consistentEmbeddingModel(EmbeddingModel embeddingModel) {
    return new EmbeddingModel() {
      @Override
      public EmbeddingResponse call(EmbeddingRequest request) {
        return embeddingModel.call(request);
      }

      @Override
      public float[] embed(Document document) {
        return embeddingModel.embed(document);
      }

      @Override
      public List<float[]> embed(List<Document> documents, EmbeddingOptions options, BatchingStrategy batchingStrategy) {
        return embeddingModel.embed(documents.stream().map(Document::getText).toList());
      }
    };
  }
}
