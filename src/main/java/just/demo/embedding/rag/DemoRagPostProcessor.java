package just.demo.embedding.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;
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
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

@Configuration
@EnableAutoConfiguration
public class DemoRagPostProcessor {

    public static void main(String[] args) {
        SpringApplication.run(DemoRagPostProcessor.class, args);
    }

    private static final List<String> DOCUMENTS = List.of(
            "JustDemo company is for educational purposes only.",
            "JustDemo has only one contributor.",
            "JustDemo contact email is test@test.com.");

    private static final Pattern EMAIL_PATTERN = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b", CASE_INSENSITIVE);

    @Bean
    CommandLineRunner run(ChatClient.Builder chatClientBuilder, EmbeddingModel embeddingModel) {
        return args -> {
            VectorStore vectorStore = getOrCreateVectorStore(embeddingModel);
            VectorStoreDocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                    .vectorStore(vectorStore)
                    .topK(2)
                    .build();

            // As an example, we mask emails found in retrieved documents as a post-processor,
            // after retrieval and before they reach the prompt.
            DocumentPostProcessor emailMasker = (query, documents) -> documents.stream()
                    .map(document -> document.mutate()
                            .text(maskEmails(document.getText()))
                            .build())
                    .toList();


            RetrievalAugmentationAdvisor retrievalAdvisor = RetrievalAugmentationAdvisor.builder()
                    .documentRetriever(documentRetriever)
                    .documentPostProcessors(emailMasker)
                    .build();

            ChatClient chatClient = chatClientBuilder
                    .defaultAdvisors(retrievalAdvisor)
                    .build();

            testQuestion(chatClient, "What is the contact email for JustDemo?");
            testQuestion(chatClient, "Is JustDemo suitable for commercial use?"); // for some reason the answer is always "I don't know." here
            testQuestion(chatClient, "Is JustDemo suitable for educational purposes?");
        };
    }

    private static String maskEmails(String text) {
        return EMAIL_PATTERN.matcher(text).replaceAll("[REDACTED_EMAIL]");
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
        File dbFile = Path.of("data").resolve("rag_postretrieval_db.json").toFile();
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
