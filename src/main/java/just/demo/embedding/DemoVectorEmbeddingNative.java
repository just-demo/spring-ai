package just.demo.embedding;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.embeddings.CreateEmbeddingResponse;
import com.openai.models.embeddings.Embedding;
import com.openai.models.embeddings.EmbeddingCreateParams;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.openai.models.embeddings.EmbeddingModel.TEXT_EMBEDDING_3_SMALL;
import static java.nio.file.Files.newBufferedReader;

public class DemoVectorEmbeddingNative {

    public static void main(String[] args) throws IOException {
        OpenAIClient client = OpenAIOkHttpClient.builder()
                .apiKey(loadApiKey())
                .build();

        List<Float> vector = embed(client, "William Shakespeare");
        System.out.println(vector.size());
        System.out.println(vector);

        Map<String, List<Float>> vectors = embedBatch(client, List.of(
                "William Shakespeare",
                "Miguel de Cervantes",
                "Johann Wolfgang von Goethe"));
        vectors.forEach((text, v) -> System.out.println(text + ": " + v.size() + " -> " + v));
    }

    private static List<Float> embed(OpenAIClient client, String text) {
        EmbeddingCreateParams params = EmbeddingCreateParams.builder()
                .input(text)
                .model(TEXT_EMBEDDING_3_SMALL)
                .build();

        CreateEmbeddingResponse response = client.embeddings().create(params);
        return response.data().getFirst().embedding();
    }

    private static Map<String, List<Float>> embedBatch(OpenAIClient client, List<String> texts) {
        EmbeddingCreateParams params = EmbeddingCreateParams.builder()
                .inputOfArrayOfStrings(texts)
                .model(TEXT_EMBEDDING_3_SMALL)
                .build();

        CreateEmbeddingResponse response = client.embeddings().create(params);
        List<Embedding> embeddings = response.data();
        Map<String, List<Float>> result = new HashMap<>();
        for (int i = 0; i < texts.size(); i++) {
            result.put(texts.get(i), embeddings.get(i).embedding());
        }
        return result;
    }

    private static String loadApiKey() throws IOException {
        Properties props = new Properties();
        try (Reader reader = newBufferedReader(Path.of(".env"))) {
            props.load(reader);
        }

        return props.getProperty("OPENAI_API_KEY");
    }

}
