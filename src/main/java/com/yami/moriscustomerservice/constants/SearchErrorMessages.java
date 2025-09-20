package com.yami.moriscustomerservice.constants;

/**
 * 搜索错误消息常量
 * 集中管理所有搜索相关的错误消息，便于统一维护和多语言支持
 */
public final class SearchErrorMessages {
    
    private SearchErrorMessages() {
        // 防止实例化
    }
    
    // API错误消息（周杰伦风格）
    public static final String SEARCH_SERVICE_UNAVAILABLE = "哎哟，搜索服务暂时不太稳定～稍后再试试看吧！";
    public static final String NETWORK_CONNECTION_ISSUE = "网络好像有点卡～不过没关系，我可以先回答我知道的问题！";
    public static final String NO_RELEVANT_INFO_FOUND = "这个信息暂时没找到耶～要不要换个关键词试试？";
    public static final String SEARCH_PROCESS_ERROR_PREFIX = "搜索过程中发生错误: ";
    
    // 时间相关错误消息
    public static final String TIME_RETRIEVAL_ERROR = "获取时间信息失败";
    
    // 通用错误消息
    public static final String SERVICE_TEMPORARILY_UNAVAILABLE = "服务暂时不可用，请稍后重试";
    public static final String UNEXPECTED_ERROR = "发生意外错误";
}