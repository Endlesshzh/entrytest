# 大模型部署指南

本文档详细说明如何部署和配置各种大模型提供商。

## 📋 支持的大模型

| 提供商 | 类型 | 成本 | 性能 | 隐私 | 推荐场景 |
|--------|------|------|------|------|----------|
| **OpenAI** | 云端 | 💰💰💰 | ⭐⭐⭐⭐⭐ | ⭐⭐ | 生产环境，高质量分析 |
| **Claude** | 云端 | 💰💰💰 | ⭐⭐⭐⭐⭐ | ⭐⭐ | 生产环境，代码分析 |
| **Compass** | 云端 | 💰💰 | ⭐⭐⭐⭐ | ⭐⭐⭐ | 国内部署，合规要求 |
| **Ollama** | 本地 | 💰 | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | 开发环境，隐私保护 |
| **vLLM** | 本地 | 💰 | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | 生产环境，高性能 |

## 1️⃣ OpenAI 部署

### 获取API Key

1. 访问 https://platform.openai.com/
2. 注册/登录账号
3. 进入 API Keys 页面
4. 创建新的 API Key

### 配置

```yaml
llm:
  primary-provider: OPENAI
  openai:
    api-key: sk-...  # 你的API Key
    model: gpt-3.5-turbo  # 或 gpt-4
```

### 环境变量方式

```bash
export OPENAI_API_KEY="sk-..."
```

### 测试连接

```bash
curl https://api.openai.com/v1/models \
  -H "Authorization: Bearer $OPENAI_API_KEY"
```

### 模型选择

- **gpt-3.5-turbo**: 快速、便宜，适合大多数场景
- **gpt-4**: 更强大，适合复杂分析
- **gpt-4-turbo**: 性价比高，推荐

### 成本估算

- gpt-3.5-turbo: $0.0015/1K tokens (输入) + $0.002/1K tokens (输出)
- gpt-4: $0.03/1K tokens (输入) + $0.06/1K tokens (输出)

每次分析约消耗 500-1000 tokens，成本约 $0.001-0.05

## 2️⃣ Claude 部署

### 获取API Key

1. 访问 https://console.anthropic.com/
2. 注册/登录账号
3. 获取 API Key

### 配置

```yaml
llm:
  primary-provider: CLAUDE
  claude:
    api-key: sk-ant-...
    model: claude-3-sonnet-20240229
```

### 环境变量方式

```bash
export CLAUDE_API_KEY="sk-ant-..."
```

### 测试连接

```bash
curl https://api.anthropic.com/v1/messages \
  -H "x-api-key: $CLAUDE_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -H "content-type: application/json" \
  -d '{
    "model": "claude-3-sonnet-20240229",
    "max_tokens": 1024,
    "messages": [{"role": "user", "content": "Hello"}]
  }'
```

### 模型选择

- **claude-3-haiku**: 最快最便宜
- **claude-3-sonnet**: 平衡性能和成本（推荐）
- **claude-3-opus**: 最强大

## 3️⃣ Compass (国内大模型) 部署

### 支持的国内大模型

- **文心一言** (百度)
- **通义千问** (阿里)
- **讯飞星火**
- **智谱AI**

### 文心一言配置示例

```yaml
llm:
  primary-provider: COMPASS
  compass:
    api-url: https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions
    api-key: your-access-token
    model: ERNIE-Bot
```

### 获取Access Token

```bash
# 百度文心一言
curl -X POST \
  'https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=YOUR_API_KEY&client_secret=YOUR_SECRET_KEY'
```

### 通义千问配置

```yaml
llm:
  compass:
    api-url: https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation
    api-key: sk-...
    model: qwen-turbo
```

## 4️⃣ Ollama (本地部署)

### 安装

#### macOS
```bash
curl -fsSL https://ollama.com/install.sh | sh
```

#### Linux
```bash
curl -fsSL https://ollama.com/install.sh | sh
```

#### Windows
下载安装包: https://ollama.com/download

### 启动服务

```bash
ollama serve
```

### 拉取模型

```bash
# 推荐模型
ollama pull llama2          # 通用模型
ollama pull codellama       # 代码分析专用
ollama pull mistral         # 轻量级
ollama pull qwen            # 中文优化

# 查看已安装模型
ollama list
```

### 配置

```yaml
llm:
  primary-provider: OLLAMA
  ollama:
    api-url: http://localhost:11434/api/generate
    model: llama2
```

### 测试

```bash
curl http://localhost:11434/api/generate -d '{
  "model": "llama2",
  "prompt": "Hello, world!",
  "stream": false
}'
```

### 模型推荐

| 模型 | 大小 | 内存需求 | 速度 | 质量 | 适用场景 |
|------|------|----------|------|------|----------|
| llama2 | 7B | 8GB | ⭐⭐⭐ | ⭐⭐⭐⭐ | 通用 |
| codellama | 7B | 8GB | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | 代码分析 |
| mistral | 7B | 8GB | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | 快速响应 |
| qwen | 7B | 8GB | ⭐⭐⭐ | ⭐⭐⭐⭐ | 中文场景 |

## 5️⃣ vLLM (高性能本地部署)

### 安装

```bash
# 创建虚拟环境
python -m venv vllm-env
source vllm-env/bin/activate

# 安装vLLM
pip install vllm

# 或使用conda
conda create -n vllm python=3.10
conda activate vllm
pip install vllm
```

### 启动服务

```bash
# 基础启动
python -m vllm.entrypoints.openai.api_server \
  --model meta-llama/Llama-2-7b-chat-hf \
  --port 8000

# 高性能配置
python -m vllm.entrypoints.openai.api_server \
  --model meta-llama/Llama-2-7b-chat-hf \
  --port 8000 \
  --tensor-parallel-size 2 \
  --gpu-memory-utilization 0.9 \
  --max-num-seqs 256
```

### 配置

```yaml
llm:
  primary-provider: VLLM
  vllm:
    api-url: http://localhost:8000/v1/chat/completions
    model: meta-llama/Llama-2-7b-chat-hf
```

### 测试

```bash
curl http://localhost:8000/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "meta-llama/Llama-2-7b-chat-hf",
    "messages": [{"role": "user", "content": "Hello"}]
  }'
```

### 性能优化

```bash
# 使用多GPU
--tensor-parallel-size 4

# 优化内存使用
--gpu-memory-utilization 0.95

# 增加并发
--max-num-seqs 512

# 启用量化
--quantization awq
```

### 推荐模型

- **Llama-2-7b-chat-hf**: 平衡性能
- **Llama-2-13b-chat-hf**: 更好质量
- **CodeLlama-7b-Instruct-hf**: 代码分析
- **Mistral-7B-Instruct-v0.2**: 高性能

## 6️⃣ LMDeploy (可选)

### 安装

```bash
pip install lmdeploy
```

### 启动

```bash
lmdeploy serve api_server \
  --model-path /path/to/model \
  --server-port 8000
```

### 配置

使用与vLLM相同的配置格式（OpenAI兼容）

## 7️⃣ SGLang (可选)

### 安装

```bash
pip install sglang
```

### 启动

```bash
python -m sglang.launch_server \
  --model-path meta-llama/Llama-2-7b-chat-hf \
  --port 8000
```

## 🔄 切换大模型

### 运行时切换

修改 `application.yml`:

```yaml
llm:
  primary-provider: OPENAI  # 改为其他提供商
```

重启应用即可。

### 多提供商配置

可以同时配置多个提供商，系统会自动fallback：

```yaml
llm:
  primary-provider: OPENAI

  openai:
    api-key: sk-...

  ollama:
    api-url: http://localhost:11434/api/generate
```

如果OpenAI不可用，会自动使用Ollama。

## 📊 性能对比

| 提供商 | 响应时间 | QPS | 成本/1K请求 |
|--------|----------|-----|-------------|
| OpenAI | 2-5s | 50 | $1-5 |
| Claude | 2-5s | 50 | $1-5 |
| Compass | 3-6s | 30 | ¥5-20 |
| Ollama | 5-15s | 10 | $0 |
| vLLM | 1-3s | 100+ | $0 |

## 🎯 选择建议

### 生产环境

**高质量要求**:
- 首选: OpenAI (gpt-4) 或 Claude (opus)
- 备选: vLLM (大模型)

**成本敏感**:
- 首选: vLLM
- 备选: OpenAI (gpt-3.5-turbo)

**国内部署**:
- 首选: Compass (文心一言/通义千问)
- 备选: vLLM

### 开发环境

- 首选: Ollama (免费、隐私)
- 备选: OpenAI (gpt-3.5-turbo)

### 隐私要求高

- 必选: Ollama 或 vLLM (本地部署)

## 🔧 故障排查

### OpenAI连接失败

```bash
# 检查API Key
curl https://api.openai.com/v1/models \
  -H "Authorization: Bearer $OPENAI_API_KEY"

# 检查网络
ping api.openai.com
```

### Ollama无法启动

```bash
# 检查端口占用
lsof -i :11434

# 查看日志
ollama serve --debug

# 重启服务
pkill ollama
ollama serve
```

### vLLM内存不足

```bash
# 减少GPU内存使用
--gpu-memory-utilization 0.7

# 使用量化模型
--quantization awq

# 减少并发
--max-num-seqs 64
```

## 📚 参考资源

- OpenAI文档: https://platform.openai.com/docs
- Claude文档: https://docs.anthropic.com/
- Ollama文档: https://ollama.com/docs
- vLLM文档: https://docs.vllm.ai/
- 文心一言: https://cloud.baidu.com/doc/WENXINWORKSHOP/
- 通义千问: https://help.aliyun.com/zh/dashscope/
