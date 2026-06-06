package just.demo.openai;

import static org.springframework.boot.WebApplicationType.NONE;
import static org.springframework.util.MimeTypeUtils.IMAGE_JPEG;

import java.nio.file.Path;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

@Configuration
@EnableAutoConfiguration
public class DemoImageRecognition {

  public static void main(String[] args) {
    new SpringApplicationBuilder(DemoImageRecognition.class).web(NONE).run(args).close();
  }

  @Bean
  CommandLineRunner run(ChatClient.Builder chatClientBuilder) {
    return args -> {
      Path imagePath = Path.of("data").resolve("image.jpeg");
      ImageDescription description = chatClientBuilder.build()
          .prompt()
          .system("Describe image")
          .user(user -> user
              .text("Describe the content of the image")
              .media(IMAGE_JPEG, new FileSystemResource(imagePath)))
          .call()
          .entity(ImageDescription.class);
      System.out.println("Scene: " + description.scene());
      System.out.println("Mood: " + description.mood());
      System.out.println("Country: " + description.country());
      System.out.println("Year: " + description.year());
    };
  }

  record ImageDescription(
      @JsonPropertyDescription("Scene of the image very briefly") String scene,
      @JsonPropertyDescription("Mood of the image") String mood,
      @JsonPropertyDescription("Guessed country in the image") String country,
      @JsonPropertyDescription("Guessed year in the image") String year) {

  }
}
