<template>
  <div class="chat-page">
    <!-- 侧边栏和主内容容器 -->
    <div class="chat-layout">
      <!-- 左侧边栏 -->
      <div class="sidebar" :class="{ 'sidebar-hidden': !showSidebar && windowWidth < 768 }">
        <div class="sidebar-header">
          <button class="new-chat-btn" type="button" @click="createNewSession">
            <van-icon name="plus" size="16" />
            <span>新建会话</span>
          </button>
          <button v-if="windowWidth < 768" class="close-sidebar-btn" type="button" @click="showSidebar = false">
            <van-icon name="cross" size="16" />
          </button>
        </div>
        
        <div class="sidebar-content">
          <div class="sessions-list">
            <div
              v-for="session in sessions"
              :key="session.id"
              class="session-item"
              :class="{ active: session.id === currentSessionId }"
              @click="switchSession(session.id)"
            >
              <div class="session-title">{{ session.title || '新会话' }}</div>
              <div class="session-actions">
                <van-icon name="more-o" size="14" @click.stop="showSessionMenu(session.id, $event)" />
              </div>
            </div>
          </div>
        </div>
        
        <div class="sidebar-footer">
          <button class="sidebar-footer-btn" type="button" @click="clearAllSessions">
            <van-icon name="trash" size="16" />
            <span>清空会话</span>
          </button>
        </div>
      </div>

      <!-- 主聊天区域 -->
      <div class="main-content">
        <!-- 顶部导航栏 -->
        <div class="header">
          <button v-if="windowWidth < 768" class="menu-btn" type="button" @click="showSidebar = true">
            <van-icon name="wap-nav" size="20" />
          </button>
          <div class="header-title">JayChou</div>
          <div class="header-actions">
            <van-icon name="user" size="20" />
          </div>
        </div>

        <!-- 聊天内容区域 -->
        <div class="chat-container" ref="chatContainer">
          <!-- 欢迎界面 -->
          <div v-if="currentMessages.length === 0" class="welcome-container">
            <div class="welcome-content">
              <div class="welcome-icon">
                <van-icon name="robot" size="80" color="#10a37f" />
              </div>
              <h1 class="welcome-title">JayChou</h1>
              <p class="welcome-subtitle">为你提供智能服务和答案</p>
              
              <div class="model-info">
                JayChou AI 助手
              </div>

              <div class="suggested-questions">
                <div v-for="question in suggestedQuestions" :key="question" class="suggestion-item">
                  <button class="suggestion-btn" type="button" @click="sendSuggestion(question)">
                    {{ question }}
                  </button>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 消息列表 -->
          <div class="messages-container" v-else>
            <div
              v-for="message in currentMessages"
              :key="message.id"
              class="message-item"
              :class="{ 
                'user-message': message.role === 'user', 
                'ai-message': message.role === 'assistant'
              }"
            >
              <div class="message-content">
                <div class="message-avatar">
                  <van-icon v-if="message.role === 'user'" name="user" size="32" color="#4A90E2" />
                  <van-icon v-else name="robot" size="32" color="#10a37f" />
                </div>
                <div class="message-bubble">
                  <div class="message-text" v-html="formatMessage(message.content)"></div>
                  <!-- 思考时的加载动画（在AI消息框内显示） -->
                  <div v-if="isStreaming && message.role === 'assistant' && message.id === currentMessages[currentMessages.length - 1]?.id" class="typing-indicator">
                    <div class="typing-dot"></div>
                    <div class="typing-dot"></div>
                    <div class="typing-dot"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 底部输入区域 -->
        <div class="input-area">
          <div class="input-container">
            <div class="input-wrapper">
              <textarea
                v-model="inputMessage"
                class="message-input"
                placeholder="发送消息..."
                rows="1"
                maxlength="2000"
                @keydown="handleKeydown"
                @input="handleInput"
              />
              <div class="input-actions">
                <van-icon name="image" size="20" @click="selectImage" />
              </div>
            </div>
            <button
              class="send-button"
              type="button"
              :disabled="!inputMessage.trim()"
              @click="sendMessage"
              title="发送消息"
            >
              <van-icon name="arrow-up" size="16" />
            </button>
          </div>
          <div class="input-hint">
            <span>按 Enter 发送，Shift + Enter 换行</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, nextTick, watch, onBeforeMount, onUnmounted, getCurrentInstance } from 'vue'
import { useChatStore } from '@/stores/chat'

// 移除Toast导入，使用全局的$toast
// import { Toast } from 'vant'

export default {
  name: 'ChatPage',
  setup() {
    const chatStore = useChatStore()
    const { proxy } = getCurrentInstance()
    const inputMessage = ref('')
    const isStreaming = ref(false)
    const showSidebar = ref(true)
    const chatContainer = ref(null)
    const textareaHeight = ref('auto')
    const windowWidth = ref(window.innerWidth)

    // 推荐问题
    const suggestedQuestions = ref([
      '周杰伦的最新专辑是什么？',
      '周杰伦的代表作有哪些？',
      'Jay Chou的音乐风格特点',
      '周杰伦的电影作品',
      '双截棍的歌词是什么？',
      '青花瓷的创作背景'
    ])

    // 计算属性
    const sessions = computed(() => chatStore.sessions)
    const currentSessionId = computed(() => chatStore.currentSessionId)
    const currentMessages = computed(() => chatStore.currentMessages)

    // 初始化时加载会话
    onBeforeMount(() => {
      chatStore.loadFromLocalStorage()
    })

    // 窗口尺寸变化时更新侧边栏状态
    onMounted(() => {
      const handleResize = () => {
        windowWidth.value = window.innerWidth
        if (windowWidth.value >= 768) {
          showSidebar.value = true
        }
        adjustTextareaHeight()
      }
      
      window.addEventListener('resize', handleResize)
      handleResize()
      scrollToBottom()
      
      return () => {
        window.removeEventListener('resize', handleResize)
      }
    })

    // 创建新会话
    const createNewSession = () => {
      chatStore.createSession()
      inputMessage.value = ''
      textareaHeight.value = 'auto'
      scrollToBottom()
    }

    // 切换会话
    const switchSession = (sessionId) => {
      chatStore.switchSession(sessionId)
      if (window.innerWidth < 768) {
        showSidebar.value = false
      }
      nextTick(() => {
        scrollToBottom()
      })
    }

    // 发送消息
    const sendMessage = async () => {
      if (!inputMessage.value.trim()) return

      const message = inputMessage.value.trim()
      inputMessage.value = ''
      textareaHeight.value = 'auto'

      try {
        isStreaming.value = true
        await chatStore.sendMessage(message)
      } catch (error) {
        console.error('发送消息失败:', error)
        // 使用正确的Toast调用方式
        proxy.$toast.fail('发送消息失败，请重试')
      } finally {
        isStreaming.value = false
        scrollToBottom()
      }
    }

    // 发送建议问题
    const sendSuggestion = (suggestion) => {
      inputMessage.value = suggestion
      sendMessage()
    }

    // 键盘事件处理
    const handleKeydown = (event) => {
      if (event.key === 'Enter' && !event.shiftKey) {
        event.preventDefault()
        sendMessage()
      }
    }

    // 输入事件处理 - 调整文本框高度
    const handleInput = () => {
      adjustTextareaHeight()
    }

    // 调整文本框高度
    const adjustTextareaHeight = () => {
      const textarea = document.querySelector('.message-input')
      if (textarea) {
        textarea.style.height = 'auto'
        const newHeight = Math.min(textarea.scrollHeight, 150)
        textarea.style.height = `${newHeight}px`
      }
    }

    // 选择图片
    const selectImage = () => {
      proxy.$toast.fail('图片功能开发中...')
    }

    // 显示会话菜单
    const showSessionMenu = (sessionId, event) => {
      // 这里可以实现会话菜单逻辑
      console.log('显示会话菜单:', sessionId)
    }

    // 清空所有会话
    const clearAllSessions = () => {
      chatStore.clearAllSessions()
      inputMessage.value = ''
      textareaHeight.value = 'auto'
      scrollToBottom()
    }

    // 格式化消息内容
    const formatMessage = (content) => {
      if (!content) return ''
      return content
        .replace(/\n/g, '<br>')
        .replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>')
        .replace(/`([^`]+)`/g, '<code>$1</code>')
    }

    // 滚动到底部
    const scrollToBottom = () => {
      nextTick(() => {
        if (chatContainer.value) {
          chatContainer.value.scrollTop = chatContainer.value.scrollHeight
        }
      })
    }

    // 监听消息变化
    watch(currentMessages, () => {
      scrollToBottom()
    }, { deep: true })

    // 监听滚动到底部事件（用于流式输出时的实时滚动）
    onMounted(() => {
      window.addEventListener('scrollToBottom', scrollToBottom)
    })

    // 清理事件监听器
    onUnmounted(() => {
      window.removeEventListener('scrollToBottom', scrollToBottom)
    })

    return {
      inputMessage,
      isStreaming,
      showSidebar,
      chatContainer,
      textareaHeight,
      windowWidth,
      sessions,
      currentSessionId,
      currentMessages,
      suggestedQuestions,
      createNewSession,
      switchSession,
      sendMessage,
      sendSuggestion,
      handleKeydown,
      handleInput,
      selectImage,
      showSessionMenu,
      clearAllSessions,
      formatMessage
    }
  }
}
</script>

<style scoped lang="scss">
.chat-page {
  height: 100vh;
  background-color: #f7f7f7;
  display: flex;
  flex-direction: column;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Microsoft YaHei', Roboto, sans-serif;
  overflow: hidden;
}

.chat-layout {
  display: flex;
  height: 100%;
  overflow: hidden;
}

// 侧边栏
.sidebar {
  width: 260px;
  background-color: #ffffff;
  border-right: 1px solid #e5e5e5;
  display: flex;
  flex-direction: column;
  transition: transform 0.3s ease;
  z-index: 100;
}

.sidebar-hidden {
  transform: translateX(-100%);
  position: absolute;
  height: 100%;
}

.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid #e5e5e5;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.new-chat-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background-color: #ffffff;
  border: 1px solid #e5e5e5;
  border-radius: 6px;
  font-size: 14px;
  color: #333333;
  cursor: pointer;
  transition: all 0.2s ease;
  flex: 1;
  justify-content: center;
}

.new-chat-btn:hover {
  background-color: #f0f0f0;
}

.close-sidebar-btn {
  padding: 4px;
  background: none;
  border: none;
  cursor: pointer;
  color: #666666;
}

.sidebar-content {
  flex: 1;
  overflow-y: auto;
}

.sessions-list {
  padding: 8px;
}

.session-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  margin-bottom: 4px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.session-item:hover {
  background-color: #f5f5f5;
}

.session-item.active {
  background-color: #e6f7ef;
  color: #10a37f;
}

.session-title {
  font-size: 14px;
  color: #333333;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-item.active .session-title {
  color: #10a37f;
}

.session-actions {
  opacity: 0;
  transition: opacity 0.2s ease;
}

.session-item:hover .session-actions {
  opacity: 1;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid #e5e5e5;
}

.sidebar-footer-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: none;
  border: none;
  font-size: 14px;
  color: #666666;
  cursor: pointer;
  transition: all 0.2s ease;
  width: 100%;
  justify-content: center;
}

.sidebar-footer-btn:hover {
  color: #333333;
  background-color: #f5f5f5;
  border-radius: 6px;
}

// 主内容区域
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

// 顶部导航栏
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background-color: #ffffff;
  border-bottom: 1px solid #e5e5e5;
}

.menu-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: #333333;
  margin-right: 16px;
}

.header-title {
  font-size: 20px;
  font-weight: 600;
  color: #333333;
  flex: 1;
  text-align: center;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

// 聊天容器
.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background-color: #f7f7f7;
}

// 欢迎界面
.welcome-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100%;
}

.welcome-content {
  text-align: center;
  max-width: 500px;
}

.welcome-icon {
  margin-bottom: 24px;
}

.welcome-title {
  font-size: 48px;
  font-weight: 700;
  color: #333333;
  margin-bottom: 8px;
}

.welcome-subtitle {
  font-size: 18px;
  color: #666666;
  margin-bottom: 24px;
}

.model-info {
  display: inline-block;
  padding: 4px 12px;
  background-color: #e6f7ef;
  color: #10a37f;
  border-radius: 16px;
  font-size: 14px;
  margin-bottom: 40px;
}

.suggested-questions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.suggestion-item {
  width: 100%;
}

.suggestion-btn {
  width: 100%;
  padding: 16px;
  background-color: #ffffff;
  border: 1px solid #e5e5e5;
  border-radius: 8px;
  font-size: 14px;
  color: #333333;
  text-align: left;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.suggestion-btn:hover {
  background-color: #f5f5f5;
  border-color: #d0d0d0;
}

// 消息容器
.messages-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding-bottom: 24px;
}

// 消息项
.message-item {
  display: flex;
  animation: fadeIn 0.3s ease-out;
}

.message-item.user-message {
  justify-content: flex-end;
}

.message-item.ai-message {
  justify-content: flex-start;
}

.message-content {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  max-width: 70%;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.message-bubble {
  padding: 16px 20px;
  border-radius: 8px;
  font-size: 16px;
  line-height: 1.6;
  position: relative;
}

.message-item.user-message .message-bubble {
  background-color: #10a37f;
  color: #ffffff;
}

.message-item.ai-message .message-bubble {
  background-color: #ffffff;
  color: #333333;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.message-text {
  word-break: break-word;
}

.message-text pre {
  background-color: rgba(0, 0, 0, 0.05);
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
  margin: 12px 0;
  font-size: 14px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
}

.message-text code {
  background-color: rgba(0, 0, 0, 0.05);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 14px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
}

.message-item.user-message .message-text pre,
.message-item.user-message .message-text code {
  background-color: rgba(255, 255, 255, 0.1);
}

// 打字指示器
.typing-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
}

.typing-dot {
  width: 8px;
  height: 8px;
  background-color: #666666;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out both;
}

.typing-dot:nth-child(1) {
  animation-delay: -0.32s;
}

.typing-dot:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes typing {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1.0);
  }
}

// 输入区域
.input-area {
  background-color: #ffffff;
  border-top: 1px solid #e5e5e5;
  padding: 16px 24px;
}

.input-container {
  display: flex;
  align-items: flex-end;
  gap: 12px;
}

.input-wrapper {
  flex: 1;
  background-color: #f5f5f5;
  border-radius: 8px;
  padding: 8px 16px;
  display: flex;
  align-items: flex-end;
  border: 1px solid transparent;
  transition: all 0.2s ease;
}

.input-wrapper:focus-within {
  border-color: #10a37f;
  box-shadow: 0 0 0 2px rgba(16, 163, 127, 0.1);
}

.message-input {
  flex: 1;
  background: transparent;
  border: none;
  padding: 8px 0;
  font-size: 16px;
  resize: none;
  outline: none;
  font-family: inherit;
  line-height: 1.5;
}

.message-input::placeholder {
  color: #999999;
}

.input-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
}

.send-button {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background-color: #10a37f;
  color: #ffffff;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  flex-shrink: 0;
  z-index: 1000; /* 提高z-index确保按钮在最上层 */
  position: relative;
}

.send-button:hover:not(:disabled) {
  background-color: #0e9271;
}

.send-button:disabled {
  opacity: 0.5;
  cursor: pointer;
}

.input-hint {
  text-align: center;
  margin-top: 8px;
  font-size: 12px;
  color: #999999;
}

// 响应式设计
@media (max-width: 768px) {
  .sidebar {
    width: 100%;
    position: absolute;
    height: 100%;
  }
  
  .message-content {
    max-width: 85%;
  }
  
  .chat-container {
    padding: 16px;
  }
  
  .input-area {
    padding: 12px 16px;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>