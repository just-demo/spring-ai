package just.demo.rag;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
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

/**
 * Instead of QuestionAnswerAdvisor (see {@link DemoRag}), which always searches the vector store
 * and stuffs the results into every prompt, this exposes the search as a tool. The model decides
 * whether it needs to call it and supplies the search query itself as the tool argument.
 */
@Configuration
@EnableAutoConfiguration
public class DemoToolKnowledgeSearch {

    public static void main(String[] args) {
        SpringApplication.run(DemoToolKnowledgeSearch.class, args);
    }

    private static final List<String> DOCUMENTS = List.of(
            "JustDemo company is for educational purposes only.",
            "JustDemo has only one contributor.");

    @Bean
    CommandLineRunner run(ChatClient.Builder chatClientBuilder, EmbeddingModel embeddingModel) {
        return args -> {
            VectorStore vectorStore = getOrCreateVectorStore(embeddingModel);
            KnowledgeBaseTools tools = new KnowledgeBaseTools(vectorStore);

            ChatClient chatClient = chatClientBuilder
                    .defaultSystem("""
                            Answer the user's questions. You know nothing about JustDemo company on your own,
                            so call the searchKnowledgeBase tool whenever a question might be about it, and
                            base your answer on reasonable inferences from the facts it returns.
                            """)
                    .defaultTools(tools)
                    .build();

            testQuestion(chatClient, "Is JustDemo suitable for commercial use?");
            testQuestion(chatClient, "Is the team big?");
            testQuestion(chatClient, "What is 7 * 6?");
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

    @RequiredArgsConstructor
    private static class KnowledgeBaseTools {
        private final VectorStore vectorStore;

        @SuppressWarnings("unused")
        @Tool(description = "Search the JustDemo knowledge base for facts needed to answer a question about the company")
        String searchKnowledgeBase(@ToolParam(description = "search query") String query) {
            List<String> facts = vectorStore.similaritySearch(SearchRequest.builder()
                            .query(query)
                            .topK(3)
                            .build())
                    .stream()
                    .map(Document::getText)
                    .toList();
            System.out.println("searchKnowledgeBase(\"" + query + "\") -> " + facts);
            return String.join("\n", facts);
        }
    }
}
