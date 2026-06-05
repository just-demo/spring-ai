package just.demo.openai;

import static org.springframework.boot.WebApplicationType.NONE;

import java.util.List;

import org.springframework.ai.model.openai.autoconfigure.OpenAiCommonProperties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableAutoConfiguration
public class DemoFindModel {

  public static void main(String[] args) {
    new SpringApplicationBuilder(DemoFindModel.class).web(NONE).run(args).close();
  }

  @Bean
  CommandLineRunner run(OpenAiCommonProperties props) {
    return args -> {
      List<String> nanoModelIds = RestClient.create("https://api.openai.com")
          .get()
          .uri("/v1/models")
          .headers(headers -> headers.setBearerAuth(props.getApiKey()))
          .retrieve()
          .body(Models.class)
          .data().stream()
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
