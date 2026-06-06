package just.demo.langgraph;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import static org.bsc.langgraph4j.prebuilt.MessagesState.SCHEMA;
import static org.springframework.ai.chat.messages.MessageType.ASSISTANT;
import static org.springframework.ai.chat.messages.MessageType.USER;
import static org.springframework.boot.WebApplicationType.NONE;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.spring.ai.serializer.std.SpringAIStateSerializer;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
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
public class DemoLanggraphConditionalEdge {

  public static void main(String[] args) {
    new SpringApplicationBuilder(DemoLanggraphConditionalEdge.class).web(NONE).run(args).close();
  }

  @Bean
  CommandLineRunner run(ChatModel chatModel) {
    return args -> {
      CompiledGraph<State> graph = new StateGraph<>(SCHEMA, new SpringAIStateSerializer<>(State::new))
          .addNode("answerer", node_async(state -> {
            List<Message> messages = new ArrayList<>(state.messages());
            state.evaluation()
                .filter(evaluation -> !evaluation.acceptable())
                .ifPresent(evaluation ->
                    messages.add(new SystemMessage("Your previous answer was rejected: " + evaluation.feedback())));
            AssistantMessage answer = chatModel.call(new Prompt(messages)).getResult().getOutput();
            System.out.println("Answer: " + answer.getText());
            return Map.of("messages", new AssistantMessage(answer.getText()));
          }))
          .addNode("evaluator", node_async(state -> {
            String userMessage = "Here is the full conversation: \n\n" + getFormattedMessages(state) + "\n\n"
                + "Here is the question: \n\n" + getLastMessageOfType(state, USER) + "\n\n"
                + "Here is the answer: \n\n" + getLastMessageOfType(state, ASSISTANT) + "\n\n";
            Evaluation evaluation = ChatClient.create(chatModel).prompt()
                .system("Evaluate the answer, replying with whether it is acceptable, your feedback and score.")
                .user(userMessage)
                .call()
                .entity(Evaluation.class);
            System.out.println("Evaluation: " + evaluation);
            return Map.of("evaluation", evaluation);
          }))
          .addEdge(START, "answerer")
          .addEdge("answerer", "evaluator")
          // There is a default recursionLimit of 25 that prevents endless loop if the condition appears to always fail
          .addConditionalEdges("evaluator", edge_async(
                  state -> state.evaluation().orElseThrow().acceptable() ? "END" : "answerer"),
              Map.of("answerer", "answerer", "END", END))
          .compile();

      MessagesState<Message> result = graph.invoke(Map.of("messages", new UserMessage("Who are you?"))).orElseThrow();
      AssistantMessage answer = (AssistantMessage) result.lastMessage().orElseThrow();
      System.out.println(answer.getText());
    };
  }

  private static String getFormattedMessages(State state) {
    return state.messages().stream()
        .filter(message -> message.getMessageType() == USER || message.getMessageType() == ASSISTANT)
        .map(message -> (message.getMessageType() == USER ? "User: " : "Assistant: ") + message.getText())
        .reduce((first, second) -> first + "\n" + second)
        .orElse("");
  }

  private static String getLastMessageOfType(State state, MessageType messageType) {
    List<Message> messages = state.messages();
    return messages.reversed().stream()
        .filter(message -> message.getMessageType() == messageType)
        .map(Message::getText)
        .findFirst()
        .orElse(null);
  }

  record Evaluation(boolean acceptable, int score, String feedback) implements Serializable {

  }

  static class State extends MessagesState<Message> {

    State(Map<String, Object> initData) {
      super(initData);
    }

    Optional<Evaluation> evaluation() {
      return value("evaluation");
    }
  }
}
