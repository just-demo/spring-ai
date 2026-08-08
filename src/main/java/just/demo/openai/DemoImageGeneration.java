package just.demo.openai;

import static java.lang.System.currentTimeMillis;

import static org.springframework.boot.WebApplicationType.NONE;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class DemoImageGeneration {

  public static void main(String[] args) {
    new SpringApplicationBuilder(DemoImageGeneration.class).web(NONE).run(args).close();
  }

  @Bean
  CommandLineRunner run(ImageModel imageModel) {
    return args -> {
      ImageResponse response = imageModel.call(new ImagePrompt(
          "Happy futuristic world",
          OpenAiImageOptions.builder()
              .model("gpt-image-1-mini")
              .width(1024)
              .height(1024)
              .quality("low")
              .build()));

      byte[] imageBytes = Base64.getDecoder().decode(response.getResult().getOutput().getB64Json());
      Path imagePath = Path.of("data").resolve("image-" + currentTimeMillis() + ".png");
      Files.write(imagePath, imageBytes);
      System.out.println(imagePath);
    };
  }
}
