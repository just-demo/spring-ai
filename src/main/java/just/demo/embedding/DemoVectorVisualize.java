package just.demo.embedding;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.jsoup.JsoupDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static org.apache.commons.lang3.StringUtils.truncate;

@Configuration
@EnableAutoConfiguration
public class DemoVectorVisualize {

    static void main(String[] args) {
        // Wikipedia rejects the default "Java/xx" User-Agent with 403, JsoupDocumentReader loads URLs via HttpURLConnection
        System.setProperty("http.agent", "Mozilla/5.0 (SpringAiDemo)");
        SpringApplication.run(DemoVectorVisualize.class, args);
    }

    private static final List<String> DOCUMENTS = List.of(
            "https://en.wikipedia.org/wiki/William_Shakespeare",
            "https://en.wikipedia.org/wiki/Leonardo_da_Vinci");

    @Bean
    CommandLineRunner run(EmbeddingModel embeddingModel) {
        return _ -> {
            List<Document> chunks = generateDocuments();
            List<float[]> vectors = chunks.stream()
                    .map(chunk -> embeddingModel.embed(chunk.getText()))
                    .toList();
            System.out.println("Number of vectors: " + vectors.size());
            System.out.println("Vector dimensions: " + vectors.getFirst().length);
            chunks.stream()
                    .collect(groupingBy(chunk -> String.valueOf(chunk.getMetadata().get("title")), counting()))
                    .forEach((title, count) -> System.out.println("Title: " + title + ", chunks: " + count));
            // TODO: find a way to visualize vectors
        };
    }

    private static List<Document> generateDocuments() {
        List<Document> documents = new ArrayList<>();
        for (String url : DOCUMENTS) {
            new JsoupDocumentReader(url).get().stream()
                    .map(document -> Document.builder()
                            // Just to prevent unexpected openai costs if the page size becomes too big
                            .text(truncate(document.getText(), 100_000))
                            .metadata(document.getMetadata())
                            .build())
                    .forEach(documents::add);
        }

        return TokenTextSplitter.builder().withChunkSize(250).build().apply(documents);
    }
}
