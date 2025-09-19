package com.yami.moriscustomerservice.repository;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;

@Repository
public class RedisChatMemoryStore implements ChatMemoryStore {
    private static final Logger logger = LoggerFactory.getLogger(RedisChatMemoryStore.class);
    
    //注入RedisTemplate
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        //获取会话消息
        String key = memoryId.toString();
        logger.info("尝试从Redis获取消息，key: {}", key);
        
        String json = redisTemplate.opsForValue().get(key);
        logger.info("从Redis获取到的消息长度: {}", json != null ? json.length() : 0);
        
        //把json字符串转换成List<ChatMessage>
        List<ChatMessage> list = ChatMessageDeserializer.messagesFromJson(json);
        return list;
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> list) {
        //更新会话消息
        String key = memoryId.toString();
        logger.info("尝试向Redis存储消息，key: {}, 消息数量: {}", key, list.size());
        
        //1.把list转换成json数据
        String json = ChatMessageSerializer.messagesToJson(list);
        logger.info("序列化后的JSON长度: {}", json.length());
        
        //2.把json数据存储到redis中
        redisTemplate.opsForValue().set(key, json, Duration.ofDays(1));
        logger.info("消息已存储到Redis，过期时间: 1天");
    }

    @Override
    public void deleteMessages(Object memoryId) {
        String key = memoryId.toString();
        logger.info("尝试从Redis删除消息，key: {}", key);
        
        Boolean result = redisTemplate.delete(key);
        logger.info("删除结果: {}", result);
    }
}
