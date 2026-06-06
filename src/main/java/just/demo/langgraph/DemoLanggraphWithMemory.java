package just.demo.langgraph;

import static java.util.UUID.randomUUID;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import static org.springframework.boot.WebApplicationType.NONE;

import java.util.Map;

import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.prebuilt.MessagesStateGraph;
import org.bsc.langgraph4j.spring.ai.serializer.std.SpringAIStateSerializer;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class DemoLanggraphWithMemory {

  public static void main(String[] args) {
    new SpringApplicationBuilder(DemoLanggraphWithMemory.class).web(NONE).run(args).close();
  }

  @Bean
  CommandLineRunner run(ChatModel chatModel) {
    return args -> {
      CompiledGraph<MessagesState<Message>> graph =
          new MessagesStateGraph<Message>(new SpringAIStateSerializer<>(MessagesState::new))
              .addNode("demo", node_async(state -> {
                AssistantMessage response = chatModel.call(new Prompt(state.messages())).getResult().getOutput();
                return Map.of("messages", new AssistantMessage(response.getText()));
              }))
              .addEdge(START, "demo")
              .addEdge("demo", END)
              .compile(CompileConfig.builder().checkpointSaver(new MemorySaver()).build());

      RunnableConfig config = RunnableConfig.builder().threadId(randomUUID().toString()).build();

      testQuestion(graph, config, "My name is William Shakespeare");
      testQuestion(graph, config, "What is my name?");
    };
  }

  private static void testQuestion(CompiledGraph<MessagesState<Message>> graph, RunnableConfig config, String
      question) {
    System.out.println(question);
    String answer = graph.invoke(Map.of("messages", new UserMessage(question)), config)
        .orElseThrow().lastMessage().orElseThrow().getText();
    System.out.println(answer);
  }
}
