package com.yami.moriscustomerservice.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
        chatModel = "openAiChatModel",
        streamingChatModel = "openAiStreamingChatModel",
        chatMemoryProvider = "chatMemoryProvider",
        contentRetriever = "contentRetriever",
        tools = "playerOrderTool"
)
public interface ConsultantService {

    @SystemMessage(fromResource = "system.md")
    Flux<String> chat(@MemoryId String memoryId, @UserMessage String message);
    
    @SystemMessage(fromResource = "system.md")
    Flux<String> streamChat(@UserMessage String message);
}
