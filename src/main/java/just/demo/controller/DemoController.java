package just.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import just.demo.model.PersonaModel;
import just.demo.service.DemoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequiredArgsConstructor
public class DemoController {

  private final DemoService demoService;

  @GetMapping("prompt")
  public String prompt() {
    return demoService.prompt();
  }

  @GetMapping("prompt-entity")
  public PersonaModel promptEntity() {
    return demoService.promptEntity();
  }
}
