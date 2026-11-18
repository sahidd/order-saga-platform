package com.sahidanwar.orders.infrastructure.ai;

import com.sahidanwar.orders.application.TextGenerator;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "spring.ai.model.chat", havingValue = "openai")
public class OpenAiTextGeneratorConfig {

    @Bean
    TextGenerator openAiTextGenerator(ChatModel chatModel) {
        ChatClient client = ChatClient.builder(chatModel).build();
        return (system, user) -> client.prompt().system(system).user(user).call().content();
    }
}
