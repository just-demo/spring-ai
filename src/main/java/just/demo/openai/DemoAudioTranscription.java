package just.demo.openai;

import static org.springframework.boot.WebApplicationType.NONE;

import static com.openai.models.audio.AudioResponseFormat.TEXT;

import java.nio.file.Path;

import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

@Configuration
@EnableAutoConfiguration
public class DemoAudioTranscription {

  public static void main(String[] args) {
    new SpringApplicationBuilder(DemoAudioTranscription.class).web(NONE).run(args).close();
  }

  @Bean
  CommandLineRunner run(TranscriptionModel transcriptionModel) {
    return args -> {
      Path audioPath = Path.of("data").resolve("audio.mp3");
      AudioTranscriptionResponse transcription = transcriptionModel.call(new AudioTranscriptionPrompt(
          new FileSystemResource(audioPath),
          OpenAiAudioTranscriptionOptions.builder()
              .model("whisper-1")
              .responseFormat(TEXT)
              .build()));
      System.out.println(transcription.getResult().getOutput());
    };
  }
}
