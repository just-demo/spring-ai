package just.demo.openai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class DemoTool {

  public static void main(String[] args) {
    SpringApplication.run(DemoTool.class, args);
  }

  @SuppressWarnings("unused")
  static class UserRecordingTools {

    @Tool(description = "Tool to record user email address")
    String recordUserEmail(
        @ToolParam(description = "User email address") String email,
        @ToolParam(description = "User name", required = false) String name,
        @ToolParam(description = "Any extra details, like preferable time to contact", required = false) String extra) {
      System.out.println("Recording user: email = " + email + ", name = " + name + ", extra = " + extra);
      return "recorded: ok";
    }

    @Tool(description = "Tool to record user phone number")
    String recordUserPhone(
        @ToolParam(description = "User phone number") String phone,
        @ToolParam(description = "User name", required = false) String name,
        @ToolParam(description = "Any extra details, like preferable time to contact", required = false) String extra) {
      System.out.println("Recording user: phone = " + phone + ", name = " + name + ", extra = " + extra);
      return "recorded: ok";
    }

    @Tool(description = "Tool to record unknown question")
    String recordUnknownQuestion(@ToolParam(description = "Unknown question") String question) {
      System.out.println("Recording unknown question: " + question);
      return "recorded: ok";
    }
  }

  @Bean
  CommandLineRunner run(ChatClient.Builder chatClientBuilder) {
    return args -> {
      ChatClient chatClient = chatClientBuilder
          .defaultSystem("Your task is to answer questions.")
          .defaultTools(new UserRecordingTools())
          .build();

      String answer = chatClient.prompt()
          .user("My name is William Shakespeare and my email is test@test.com, what's up?")
          .call()
          .content();

      System.out.println(answer);
    };
  }
}
