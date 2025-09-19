import { defineStore } from 'pinia'
import { generateUUID, formatSessionTitle } from '@/utils/utils'
import { ref, computed } from 'vue'
import { fetchEventSource } from '@microsoft/fetch-event-source';

// 后端API地址
const apiUrl = 'http://localhost:8081'

export const useChatStore = defineStore('chat', () => {
  // 聊天会话列表
  const sessions = ref([])
  
  // 当前会话ID
  const currentSessionId = ref(null)
  
  // 获取当前会话
  const currentSession = computed(() => {
    return sessions.value.find(session => session.id === currentSessionId.value) || null
  })

  // 获取当前会话的消息
  const currentMessages = computed(() => {
    return currentSession.value ? currentSession.value.messages : []
  })

  // 创建新会话
  const createSession = () => {
    const sessionId = generateUUID()
    const newSession = {
      id: sessionId,
      memoryId: generateUUID(),
      title: '新会话',
      messages: [],
      createTime: Date.now(),
      lastMessageTime: Date.now()
    }
    
    sessions.value.push(newSession)
    currentSessionId.value = sessionId
  }

  // 切换会话
  const switchSession = (sessionId) => {
    currentSessionId.value = sessionId
  }

  // 发送消息
  const sendMessage = async (content) => {
    if (!currentSession.value) {
      createSession()
    }
  
    // 添加用户消息
    const userMessage = {
      id: generateUUID(),
      role: 'user',
      content: content,
      timestamp: Date.now()
    }
    
    currentSession.value.messages.push(userMessage)
    currentSession.value.lastMessageTime = Date.now()
    
    // 更新会话标题
    if (currentSession.value.messages.length === 1) {
      currentSession.value.title = formatSessionTitle(content)
    }
    
    // 保存到本地存储
    saveToLocalStorage()
    
    // 在函数开头定义aiMessage变量，确保在catch块中可用
    let aiMessage = null
    
    try {
      // 添加AI回复消息占位符
      aiMessage = {
        id: generateUUID(),
        role: 'assistant',
        content: '',
        timestamp: Date.now()
      }
      currentSession.value.messages.push(aiMessage)
      saveToLocalStorage()
  
      // 使用普通fetch API处理text/html响应
      const params = new URLSearchParams({
        message: content,
        memoryId: currentSession.value.memoryId
      });
      
      const response = await fetch(`${apiUrl}/chat?${params.toString()}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
        }
      });
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      // 读取响应内容
      const responseText = await response.text();
      if (responseText && responseText.trim()) {
        // 直接设置AI消息内容
        aiMessage.content = responseText;
        // 实时更新UI
        saveToLocalStorage();
        // 触发滚动到底部事件
        window.dispatchEvent(new CustomEvent('scrollToBottom'));
      }
      
      currentSession.value.lastMessageTime = Date.now();
      saveToLocalStorage();
      // 触发最终滚动
      window.dispatchEvent(new CustomEvent('scrollToBottom'));
    } catch (error) {
      console.error('发送消息失败:', error);
      
      // 如果没有创建aiMessage，说明在响应处理前就出错了
      if (!aiMessage) {
        aiMessage = {
          id: generateUUID(),
          role: 'assistant',
          content: '',
          timestamp: Date.now()
        }
        currentSession.value.messages.push(aiMessage)
      }
      
      // 如果没有收到任何内容，显示错误消息
      if (!aiMessage.content) {
        // 根据错误类型提供不同的错误信息
        if (error.name === 'AbortError') {
          aiMessage.content = '请求超时，请检查网络连接后重试。';
        } else if (error.message.includes('Failed to fetch')) {
          aiMessage.content = '网络连接失败，请检查后端服务是否正常运行。';
        } else {
          aiMessage.content = '抱歉，我暂时无法回复您的消息，请稍后再试。';
        }
      }
      
      currentSession.value.lastMessageTime = Date.now();
      saveToLocalStorage();
      // 抛出错误供上层处理
      throw error;
    }
  }

  // 从本地存储加载数据
  const loadFromLocalStorage = () => {
    try {
      const savedData = localStorage.getItem('chatSessions')
      if (savedData) {
        const parsedData = JSON.parse(savedData)
        sessions.value = parsedData.sessions || []
        currentSessionId.value = parsedData.currentSessionId || null
        
        // 如果没有会话，创建一个新会话
        if (sessions.value.length === 0) {
          createSession()
        }
      } else {
        // 如果没有保存的数据，创建一个新会话
        createSession()
      }
    } catch (error) {
      console.error('从本地存储加载数据失败:', error)
      // 出错时创建一个新会话
      createSession()
    }
  }

  // 保存到本地存储
  const saveToLocalStorage = () => {
    try {
      const dataToSave = {
        sessions: sessions.value,
        currentSessionId: currentSessionId.value
      }
      localStorage.setItem('chatSessions', JSON.stringify(dataToSave))
    } catch (error) {
      console.error('保存到本地存储失败:', error)
    }
  }

  // 删除会话
  const deleteSession = (sessionId) => {
    const index = sessions.value.findIndex(session => session.id === sessionId)
    if (index !== -1) {
      sessions.value.splice(index, 1)
      
      // 如果删除的是当前会话，切换到第一个会话或创建新会话
      if (currentSessionId.value === sessionId) {
        if (sessions.value.length > 0) {
          currentSessionId.value = sessions.value[0].id
        } else {
          createSession()
        }
      }
      
      saveToLocalStorage()
    }
  }

  // 清空所有会话
  const clearAllSessions = () => {
    sessions.value = []
    currentSessionId.value = null
    createSession()
    saveToLocalStorage()
  }

  return {
    sessions,
    currentSessionId,
    currentSession,
    currentMessages,
    createSession,
    switchSession,
    sendMessage,
    loadFromLocalStorage,
    deleteSession,
    clearAllSessions
  }
})