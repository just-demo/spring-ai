package just.demo.langgraph;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.action.Command;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.prebuilt.MessagesStateGraph;
import org.bsc.langgraph4j.spring.ai.serializer.std.SpringAIStateSerializer;
import org.bsc.langgraph4j.spring.ai.tool.SpringAIToolService;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Configuration
@EnableAutoConfiguration
public class DemoLanggraphWithTools {

    static void main(String[] args) {
        SpringApplication.run(DemoLanggraphWithTools.class, args);
    }

    @Bean
    CommandLineRunner run(ChatModel chatModel) {
        return _ -> {
            List<ToolCallback> tools = List.of(ToolCallbacks.from(new DemoTools()));
            SpringAIToolService toolService = new SpringAIToolService(tools);
            ToolCallingChatOptions chatOptions = ToolCallingChatOptions.builder()
                    .toolCallbacks(tools)
                    .internalToolExecutionEnabled(false)
                    .build();
            CompiledGraph<MessagesState<Message>> graph =
                    new MessagesStateGraph<Message>(new SpringAIStateSerializer<>(MessagesState::new))
                            .addNode("demo", node_async(state -> {
                                AssistantMessage response =
                                        chatModel.call(new Prompt(state.messages(), chatOptions)).getResult().getOutput();
                                return Map.of("messages", AssistantMessage.builder()
                                        .content(response.getText())
                                        .toolCalls(response.getToolCalls())
                                        .build());
                            }))
                            .addNode("tools", (AsyncNodeAction<MessagesState<Message>>) state -> toolService.executeFunctions(
                                            ((AssistantMessage) state.lastMessage().orElseThrow()).getToolCalls(), Map.of())
                                    .thenApply(Command::update))
                            .addConditionalEdges("demo", edge_async(state ->
                                            ((AssistantMessage) state.lastMessage().orElseThrow()).hasToolCalls() ? "tools" : "END"),
                                    Map.of("tools", "tools", "END", END))
                            .addEdge("tools", "demo")
                            .addEdge(START, "demo")
                            .compile();

            String question = "Here is my email test@test.com";
            System.out.println(question);
            String answer = graph.invoke(Map.of("messages", new UserMessage(question)))
                    .orElseThrow().lastMessage().orElseThrow().getText();
            System.out.println(answer);
        };
    }

    @SuppressWarnings("unused")
    static class DemoTools {

        @Tool(name = "record_user_email", description = "Record user email")
        String recordUserEmail(@ToolParam(description = "User email") String email) {
            System.out.println("Recording user email = " + email);
            return "ok";
        }
    }
}
