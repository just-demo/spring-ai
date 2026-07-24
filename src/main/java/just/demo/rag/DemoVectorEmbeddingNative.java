package just.demo.rag;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.embeddings.CreateEmbeddingResponse;
import com.openai.models.embeddings.EmbeddingCreateParams;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.openai.models.embeddings.EmbeddingModel.TEXT_EMBEDDING_3_SMALL;

public class DemoVectorEmbeddingNative {

    public static void main(String[] args) throws IOException {
        OpenAIClient client = OpenAIOkHttpClient.builder()
                .apiKey(loadApiKey())
                .build();

        List<Float> vector = embed(client, "William Shakespeare");
        System.out.println(vector.size());
        System.out.println(vector);
    }

    private static List<Float> embed(OpenAIClient client, String text) {
        EmbeddingCreateParams params = EmbeddingCreateParams.builder()
                .input(text)
                .model(TEXT_EMBEDDING_3_SMALL)
                .build();

        CreateEmbeddingResponse response = client.embeddings().create(params);
        return response.data().getFirst().embedding();
    }

    private static String loadApiKey() throws IOException {
        String fromEnv = System.getenv("OPENAI_API_KEY");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        for (String line : Files.readAllLines(Path.of(".env"))) {
            if (line.startsWith("OPENAI_API_KEY=")) {
                return line.substring("OPENAI_API_KEY=".length()).trim();
            }
        }
        throw new IllegalStateException("OPENAI_API_KEY not found in environment or .env file");
    }

}
