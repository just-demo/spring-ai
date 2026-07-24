package just.demo.rag;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.embeddings.CreateEmbeddingResponse;
import com.openai.models.embeddings.EmbeddingCreateParams;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.List;
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
        Properties props = new Properties();
        try (Reader reader = newBufferedReader(Path.of(".env"))) {
            props.load(reader);
        }

        return props.getProperty("OPENAI_API_KEY");
    }

}
