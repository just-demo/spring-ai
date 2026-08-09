package just.demo.openai;

import org.springframework.ai.chat.cache.semantic.SemanticCache;
import org.springframework.ai.chat.cache.semantic.SemanticCacheAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.cache.semantic.DefaultSemanticCache;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.lang.System.currentTimeMillis;

@Configuration
@EnableAutoConfiguration
public class DemoSemanticCache {

    static void main(String[] args) {
        SpringApplication.run(DemoSemanticCache.class, args);
    }

    // Not using Redis cache here because its vector store implementation has some bugs that require non-trivial fixes.
    // Using textOnlyEmbeddingModel() here because OpenAiEmbeddingModel.embed(Document)
    // defaults to MetadataMode.EMBED, which folds ALL document metadata into the embedded text. Since
    // DefaultSemanticCache stores the serialized ChatResponse as "response"/"response_text" metadata,
    // that would embed "response: {huge json}...\n\n<question>" at storage time against a plain
    // "<question>" at query time, tanking cosine similarity below the threshold on every lookup.
    // TODO: drop this wrapper once every EmbeddingModel embeds Document/String consistently.
    // https://github.com/spring-projects/spring-ai/issues/2579
    @Bean
    VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(textOnlyEmbeddingModel(embeddingModel)).build();
    }

    @Bean
    CommandLineRunner run(ChatClient.Builder chatClientBuilder, VectorStore vectorStore, EmbeddingModel embeddingModel) {
        return _ -> {
            SemanticCache semanticCache = DefaultSemanticCache.builder()
                    .vectorStore(vectorStore)
                    .embeddingModel(embeddingModel) // required by the builder, but unused once vectorStore is set explicitly
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

    private static EmbeddingModel textOnlyEmbeddingModel(EmbeddingModel embeddingModel) {
        return new EmbeddingModel() {
            @Override
            public EmbeddingResponse call(EmbeddingRequest request) {
                return embeddingModel.call(request);
            }

            @Override
            public float[] embed(Document document) {
                return embeddingModel.embed(document.getText());
            }
        };
    }

    private static void ask(ChatClient chatClient, String question) {
        long start = currentTimeMillis();
        String answer = chatClient.prompt().user(question).call().content();
        System.out.println("Q: " + question);
        System.out.println("A: " + answer);
        System.out.println("T: " + (currentTimeMillis() - start) + "ms\n");
    }
}
