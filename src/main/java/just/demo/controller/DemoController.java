package just.demo.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import just.demo.model.PersonaModel;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class DemoController {

  private final ChatClient chatClient;

  @GetMapping("text")
  public String text() {
    return chatClient.prompt()
        .user("Who are you?")
        .call()
        .content();
  }

  @GetMapping("entity")
  public PersonaModel entity() {
    return chatClient.prompt()
        .user("Who are you?")
        .call()
        .entity(PersonaModel.class);
  }

}
