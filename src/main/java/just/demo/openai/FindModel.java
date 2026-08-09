package just.demo.openai;

import org.springframework.ai.model.openai.autoconfigure.OpenAiCommonProperties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.util.List;

@Configuration
@EnableAutoConfiguration
public class FindModel {

    static void main(String[] args) {
        SpringApplication.run(FindModel.class, args);
    }

    @Bean
    CommandLineRunner run(OpenAiCommonProperties props) {
        return _ -> {
            List<String> nanoModelIds = RestClient.create("https://api.openai.com")
                    .get()
                    .uri("/v1/models")
                    .headers(headers -> headers.setBearerAuth(props.getApiKey()))
                    .retrieve()
                    .body(Models.class)
                    .data()
                    .stream()
                    .map(Model::id)
                    .filter(id -> id.contains("nano"))
                    .toList();
            System.out.println(nanoModelIds);
        };
    }

    record Models(List<Model> data) {
    }

    record Model(String id) {
    }
}
