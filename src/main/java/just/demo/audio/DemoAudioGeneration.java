package just.demo.audio;

import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.audio.tts.TextToSpeechResponse;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.System.currentTimeMillis;
import static org.springframework.ai.openai.OpenAiAudioSpeechOptions.Voice.ALLOY;

@Configuration
@EnableAutoConfiguration
public class DemoAudioGeneration {

    static void main(String[] args) {
        SpringApplication.run(DemoAudioGeneration.class, args);
    }

    @Bean
    CommandLineRunner run(OpenAiAudioSpeechModel speechModel) {
        return _ -> {
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
