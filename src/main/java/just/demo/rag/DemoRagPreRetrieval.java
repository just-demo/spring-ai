package just.demo.rag;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Unlike {@link DemoRag}, which always searches the vector store with the user's question exactly
 * as typed, this rewrites the question before retrieval happens. The knowledge base below is
 * English-only, so a Spanish question would otherwise match nothing; {@link TranslationQueryTransformer}
 * translates it to English first, and only then is the vector store searched. The demo proves this
 * matters by first running the raw, untranslated search (weak/no matches), then asking the same
 * question through a {@link ChatClient} wired with {@link RetrievalAugmentationAdvisor} (translated
 * first, so retrieval succeeds).
 */
@Configuration
@EnableAutoConfiguration
public class DemoRagPreRetrieval {

    public static void main(String[] args) {
        SpringApplication.run(DemoRagPreRetrieval.class, args);
    }

    private static final List<String> DOCUMENTS = List.of(
            "JustDemo company is for educational purposes only.",
            "JustDemo has only one contributor.");

    private static final String SPANISH_QUESTION = "¿Es JustDemo adecuado para uso comercial?";

    @Bean
    CommandLineRunner run(ChatClient.Builder chatClientBuilder, EmbeddingModel embeddingModel) {
        return args -> {
            VectorStore vectorStore = getOrCreateVectorStore(embeddingModel);

            System.out.println("Raw similarity search with untranslated Spanish question:");
            List<Document> rawResults = vectorStore.similaritySearch(SearchRequest.builder()
                    .query(SPANISH_QUESTION)
                    .topK(3)
                    .similarityThreshold(0.5)
                    .build());
            System.out.println(rawResults.isEmpty() ? "(no matches)" : rawResults.stream().map(Document::getText).toList());
            System.out.println();

            RetrievalAugmentationAdvisor retrievalAdvisor = RetrievalAugmentationAdvisor.builder()
                    .queryTransformers(TranslationQueryTransformer.builder()
                            .chatClientBuilder(chatClientBuilder.clone())
                            .targetLanguage("english")
                            .build())
                    .documentRetriever(VectorStoreDocumentRetriever.builder()
                            .vectorStore(vectorStore)
                            .topK(3)
                            .build())
                    .build();

            ChatClient chatClient = chatClientBuilder
                    .defaultAdvisors(retrievalAdvisor)
                    .build();

            testQuestion(chatClient, SPANISH_QUESTION);
            testQuestion(chatClient, "¿El equipo es grande?");
        };
    }

    private static void testQuestion(ChatClient chatClient, String question) {
        System.out.println("Q: " + question);
        String answer = chatClient.prompt()
                .user(question)
                .call()
                .content();
        System.out.println("A: " + answer);
        System.out.println();
    }

    private static VectorStore getOrCreateVectorStore(EmbeddingModel embeddingModel) {
        File dbFile = Path.of("data").resolve("rag_preretrieval_db.json").toFile();
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
