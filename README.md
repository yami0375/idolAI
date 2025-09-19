# Moris Customer Service

智能客服系统，基于Spring Boot后端和Vue.js前端，集成AI对话能力和知识库检索功能。

## 项目介绍

本项目是一个现代化的智能客服系统，具有以下特性：

- **后端技术**: Spring Boot + WebFlux + Redis + LangChain4j
- **前端技术**: Vue 3 + Vite + Pinia
- **AI能力**: 集成阿里云大模型API
- **知识库**: 支持自定义知识库内容检索
- **会话管理**: 基于Redisearch的持久化会话存储

## 环境要求

- Java 17+
- Node.js 16+
- Python 3.8+
- Redisearch latest

## 快速开始

### 1. 知识库爬取与处理

#### 运行周杰伦知识库爬取脚本
```bash
cd crawl4ai-deployment
python jaychou_crawl_knowledge_base.py
```

#### 处理爬取内容
```bash
cd crawl4ai-deployment
python process_jaychou_content.py
```

处理后的知识库文件将保存在 `src/main/resources/content/` 目录下。

### 2. 后端服务部署

#### 配置环境变量
在 `src/main/resources/application.yml` 中配置：
- Redis连接信息
- 阿里云DashScope API密钥
- 模型参数

#### 启动后端服务
```bash
# 使用Maven编译运行
mvn clean spring-boot:run

# 或者打包后运行
mvn clean package
java -jar target/moriscustomerservice-0.0.1-SNAPSHOT.jar
```

后端服务默认运行在 `http://localhost:8081`

### 3. 前端服务部署

#### 安装依赖
```bash
cd frontend
npm install
```

#### 开发模式运行
```bash
npm run dev
```

#### 生产环境构建
```bash
npm run build
```

前端服务默认运行在 `http://localhost:3000`

## 项目结构

```
MorisCustomerService/
├── crawl4ai-deployment/          # 知识库爬取脚本
│   ├── jaychou_crawl_knowledge_base.py
│   ├── process_jaychou_content.py
│   └── kb_raw/                   # 原始知识库文件
├── frontend/                     # 前端项目
│   ├── src/
│   │   ├── stores/               # 状态管理
│   │   ├── views/                # 页面组件
│   │   └── utils/                # 工具函数
│   └── vite.config.js
├── src/main/java/                # 后端Java代码
│   └── com/yami/moriscustomerservice/
│       ├── controller/           # 控制器层
│       ├── service/              # 服务层
│       ├── config/               # 配置类
│       └── model/               # 数据模型
├── src/main/resources/           # 资源文件
│   ├── application.yml           # 应用配置
│   ├── content/                  # 知识库内容
│   └── system.txt               # 系统提示词
└── pom.xml                       # Maven配置
```

## 功能特性

### 核心功能
- 实时AI对话
- 多轮会话管理
- 知识库检索增强
- 流式响应输出

### 技术特性
- 响应式编程（WebFlux）
- Redis会话持久化
- 向量化检索
- 热重载开发

## 配置说明

### 后端配置 (application.yml)

```yaml
langchain4j:
  open-ai:
    chat-model:
      api-key: ${DASHSCOPE_API_KEY}
      model-name: qwen-plus
      base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
      temperature: 0.7
      max-tokens: 2000
      timeout: 60s
    streaming-chat-model:
      api-key: ${DASHSCOPE_API_KEY}
      model-name: qwen-plus
      base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
      temperature: 0.7
      max-tokens: 2000
      timeout: 60s

spring:
  data:
    redis:
      host: localhost
      port: 6380
      database: 0
```

### 前端配置

API端点配置在 `frontend/src/stores/chat.js` 中：
```javascript
const apiUrl = 'http://localhost:8081';
```

## 常见问题

### Q: 知识库爬取失败
A: 检查网络连接和爬取目标的访问权限

### Q: 后端启动报错
A: 确认Redis服务已启动，API密钥配置正确

### Q: 前端无法连接后端
A: 检查CORS配置和后端服务地址

### Q: AI响应缓慢
A: 检查网络连接和API调用限制

## 开发说明

### 添加新的知识库
1. 在 `crawl4ai-deployment/kb_raw/` 添加原始文件
2. 运行处理脚本生成格式化内容
3. 将处理后的文件放入 `src/main/resources/content/`

### 修改系统提示词
编辑 `src/main/resources/system.txt` 文件

### 自定义对话流程
修改 `src/main/java/com/yami/moriscustomerservice/service/` 中的服务实现

## 部署到生产环境

### 后端部署
1. 设置生产环境配置文件
2. 配置环境变量
3. 使用Docker或直接运行jar包

### 前端部署
1. 运行 `npm run build`
2. 部署 `dist/` 目录到Web服务器
3. 配置Nginx反向代理

## 许可证

本项目基于MIT许可证开源。

## 支持与贡献

欢迎提交Issue和Pull Request来改进本项目。