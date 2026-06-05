package just.demo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import just.demo.model.PersonaModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
@SuppressWarnings("DataFlowIssue")
public class DemoService {

  private final ChatClient chatClient;

  @GetMapping("prompt")
  public String prompt() {
    return chatClient.prompt()
        .user("Who are you?")
        .call()
        .content();
  }

  @GetMapping("prompt-entity")
  public PersonaModel promptEntity() {
    ResponseEntity<ChatResponse, PersonaModel> response = chatClient.prompt()
        .user("Who are you?")
        .call()
        .responseEntity(PersonaModel.class);

    ChatResponseMetadata metadata = response.getResponse().getMetadata();
    log.info("Model used: {}", metadata.getModel());
    log.info("Usage: {}", metadata.getUsage());
    return response.entity();
  }
}
