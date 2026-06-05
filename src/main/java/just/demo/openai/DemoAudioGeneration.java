package just.demo.openai;

import static java.lang.System.currentTimeMillis;

import static org.springframework.ai.openai.OpenAiAudioSpeechOptions.Voice.ALLOY;
import static org.springframework.boot.WebApplicationType.NONE;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.audio.tts.TextToSpeechResponse;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class DemoAudioGeneration {

  public static void main(String[] args) {
    new SpringApplicationBuilder(DemoAudioGeneration.class).web(NONE).run(args).close();
  }

  @Bean
  CommandLineRunner run(OpenAiAudioSpeechModel speechModel) {
    return args -> {
      TextToSpeechResponse response = speechModel.call(new TextToSpeechPrompt("Just a demo",
          OpenAiAudioSpeechOptions.builder()
              .model("tts-1")
              .voice(ALLOY.getValue())
              .build()));
      byte[] audioBytes = response.getResult().getOutput();
      Path audioPath = Path.of("data").resolve("audio-" + currentTimeMillis() + ".mp3");
      Files.write(audioPath, audioBytes);
      System.out.println(audioPath);
    };
  }
}
