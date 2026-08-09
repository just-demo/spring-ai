package just.demo.embedding.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

@Configuration
@EnableAutoConfiguration
public class DemoRagQueryTransformer {

    static void main(String[] args) {
        SpringApplication.run(DemoRagQueryTransformer.class, args);
    }

    private static final List<String> DOCUMENTS = List.of(
            "JustDemo company is for educational purposes only.",
            "JustDemo has only one contributor.");

    @Bean
    CommandLineRunner run(ChatClient.Builder chatClientBuilder, EmbeddingModel embeddingModel) {
        return _ -> {
            VectorStore vectorStore = getOrCreateVectorStore(embeddingModel);
            RetrievalAugmentationAdvisor retrievalAdvisor = RetrievalAugmentationAdvisor.builder()
                    // As an example, we use query translation as a pre-processor before document retrieval.
                    // Another examples could be query compressing, rewriting, filtering, and so on.
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

            testQuestion(chatClient, "¿Es JustDemo adecuado para uso comercial?");
            testQuestion(chatClient, "¿El equipo es grande?");
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
