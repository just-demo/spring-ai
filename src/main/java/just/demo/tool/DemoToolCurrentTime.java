package just.demo.tool;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static java.util.Optional.ofNullable;

@Configuration
@EnableAutoConfiguration
public class DemoToolCurrentTime {

    static void main(String[] args) {
        SpringApplication.run(DemoToolCurrentTime.class, args);
    }

    @Bean
    CommandLineRunner run(ChatClient.Builder chatClientBuilder) {
        return _ -> {
            ChatClient chatClient = chatClientBuilder
                    .defaultTools(new DemoTools())
                    .build();

            testQuestion(chatClient, "What time is it right now?");
            testQuestion(chatClient, "What time is it in Tokyo?");
        };
    }

    private static void testQuestion(ChatClient chatClient, String question) {
        System.out.println(question);
        String answer = chatClient.prompt()
                .user(question)
                .call()
                .content();
        System.out.println(answer);
        System.out.println();
    }

    @SuppressWarnings("unused")
    private static class DemoTools {
        @Tool(description = "Get the current date and time, optionally in a specific time zone")
        String getCurrentTime(@ToolParam(description = "IANA time zone id, e.g. 'Asia/Tokyo'. Omit for the system default time zone.", required = false) String timeZone) {
            ZoneId zoneId = ofNullable(timeZone)
                    .map(ZoneId::of)
                    .orElseGet(ZoneId::systemDefault);
            LocalDateTime now = LocalDateTime.now(zoneId);
            System.out.println("  getCurrentTime(" + timeZone + ") -> " + now);
            return now.toString();
        }
    }
}
