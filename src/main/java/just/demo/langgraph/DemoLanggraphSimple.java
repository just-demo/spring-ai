package just.demo.langgraph;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

import java.util.List;
import java.util.Map;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.prebuilt.MessagesStateGraph;
import org.bsc.langgraph4j.spring.ai.serializer.std.SpringAIStateSerializer;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class DemoLanggraphSimple {

  public static void main(String[] args) {
    SpringApplication.run(DemoLanggraphSimple.class, args);
  }

  @Bean
  CommandLineRunner run(ChatModel chatModel) {
    return args -> {
      CompiledGraph<MessagesState<Message>> graph = new MessagesStateGraph<Message>(
          new SpringAIStateSerializer<>(MessagesState::new))
          .addNode("demo", node_async(state -> {
            // Don't really need to communicate with LLM here, e.g. could return a dummy result
            AssistantMessage response = chatModel.call(new Prompt(state.messages())).getResult().getOutput();
            return Map.of("messages", List.of(new AssistantMessage(response.getText())));
          }))
          .addEdge(START, "demo")
          .addEdge("demo", END)
          .compile();

      String question = "Who are you?";
      System.out.println(question);
      String answer = graph.invoke(Map.of("messages", new UserMessage(question)))
          .orElseThrow().lastMessage().orElseThrow().getText();
      System.out.println(answer);
    };
  }
}
