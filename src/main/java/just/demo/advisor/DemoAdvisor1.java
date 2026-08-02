package just.demo.advisor;

import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;

public class DemoAdvisor1 implements CallAdvisor {
    @Override
    public @NonNull String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public @NonNull ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        System.out.println(getName() + " before");
        ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);
        System.out.println(getName() + "after: " + chatClientResponse.chatResponse().getMetadata().getUsage());
        return chatClientResponse;
    }
}
