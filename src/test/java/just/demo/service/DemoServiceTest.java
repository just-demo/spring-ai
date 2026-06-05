package just.demo.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = NONE)
class DemoServiceTest {

  @Autowired
  private DemoService demoService;

  @Test
  void prompt() {
    String result = demoService.prompt();
    assertNotNull(result);
  }
}