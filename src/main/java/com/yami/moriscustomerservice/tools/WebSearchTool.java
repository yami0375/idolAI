package com.yami.moriscustomerservice.tools;

import com.yami.moriscustomerservice.constants.SearchErrorMessages;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Web搜索工具类，为AI模型提供实时联网搜索能力
 */
@Component
public class WebSearchTool {
    
    private final WebClient webClient;
    
    public WebSearchTool() {
        this.webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
    
    /**
     * 实时网络搜索
     * @param query 搜索关键词
     * @return 搜索结果摘要
     */
    @Tool
    public String searchWeb(String query) {
        try {
            System.out.println("正在搜索: " + query);
            
            // 使用DuckDuckGo Instant Answer API进行搜索（主方案）
            return webClient.get()
                    .uri("https://api.duckduckgo.com/?q={query}&format=json&no_html=1&skip_disambig=1", query)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(java.time.Duration.ofSeconds(10))
                    .onErrorResume(WebClientResponseException.class, ex -> {
                        // 处理HTTP错误，尝试备用方案
                        System.out.println("DuckDuckGo API HTTP错误: " + ex.getMessage());
                        return tryAlternativeSearch(query);
                    })
                    .onErrorResume(Exception.class, ex -> {
                        // 处理其他错误，尝试备用方案
                        System.out.println("DuckDuckGo API 网络错误: " + ex.getMessage());
                        return tryAlternativeSearch(query);
                    })
                    .map(response -> {
                        String abstractText = (String) response.get("Abstract");
                        return abstractText != null && !abstractText.isEmpty() 
                                ? abstractText 
                                : SearchErrorMessages.NO_RELEVANT_INFO_FOUND;
                    })
                    .block();
        } catch (Exception e) {
            System.out.println("搜索过程异常: " + e.getMessage());
            return "网络好像有点卡～";
        }
    }
    
    /**
     * 备用搜索方案
     */
    private Mono<Map> tryAlternativeSearch(String query) {
        // 这里可以添加其他搜索API作为备用
        System.out.println("尝试备用搜索方案...");
        return Mono.just(Map.of("Abstract", "网络连接不太稳定，搜索功能暂时受限。不过我可以基于现有知识库回答你的问题！"));
    }
    
    /**
     * 获取当前时间信息
     * @return 当前日期和时间
     */
    @Tool
    public String getCurrentTime() {
        return LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss")
        );
    }
    
    /**
     * 获取当前日期
     * @return 当前日期
     */
    @Tool
    public String getCurrentDate() {
        return LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy年MM月dd日")
        );
    }
    
    /**
     * 获取当前星期几
     * @return 当前星期
     */
    @Tool
    public String getCurrentDayOfWeek() {
        return LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("EEEE")
        );
    }
}