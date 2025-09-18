package com.yami.moriscustomerservice.controller;

import com.yami.moriscustomerservice.service.ConsultantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@CrossOrigin(origins = "*")
public class ChatController {
    
    @Autowired
    private ConsultantService consultantService;
    
    @PostMapping(value = "/chat", produces = "text/html;charset=utf-8")
    public Flux<String> chat(String memoryId, String message){
        Flux<String> result = consultantService.chat(memoryId, message);
        return result;
    }
    
    @PostMapping(value = "/api/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestBody ChatRequest request) {
        return consultantService.streamChat(request.getMessage())
                .map(content -> String.format("data: %s\n\n", content))
                .concatWithValues("data: [DONE]\n\n");
    }
    
    static class ChatRequest {
        private String message;
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
}
