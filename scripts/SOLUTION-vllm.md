# vLLM 服务解决方案

## 当前状态

✅ **已创建启动脚本**：
- `scripts/setup-vllm.sh` - 安装和启动 vLLM
- `scripts/start-vllm.sh` - 启动 vLLM 服务
- `scripts/stop-vllm.sh` - 停止 vLLM 服务

⚠️ **问题**：vLLM 在 macOS 上安装复杂，需要 GPU 支持

## 推荐解决方案

### 方案 1: 使用 Ollama（推荐，最简单）

Ollama 是更适合 macOS 的本地 LLM 方案，已经在项目配置中支持。

#### macOS 安装：

```bash
# 使用 Homebrew 安装
brew install ollama

# 或下载安装包
# 访问 https://ollama.com/download 下载 macOS 版本
```

#### 启动服务：

```bash
# 启动 Ollama 服务
ollama serve

# 在另一个终端拉取模型
ollama pull llama2

# 测试
curl http://localhost:11434/api/tags
```

#### 更新配置：

应用已经配置了 Ollama，只需确保服务运行在 `http://localhost:11434`。

### 方案 2: 使用 Docker 运行 vLLM（如果有 GPU）

如果您有 NVIDIA GPU 或 Apple Silicon，可以使用 Docker：

```bash
# 安装 Docker Desktop for Mac
# 然后运行：

docker run --gpus all -p 8000:8000 \
  vllm/vllm-openai:latest \
  --model meta-llama/Llama-2-7b-chat-hf
```

### 方案 3: 使用云端 API（最简单，无需本地服务）

如果不需要本地部署，可以使用云端 API：

1. **OpenAI** - 需要 API Key
2. **Claude** - 需要 API Key  
3. **Compass** - 国内大模型

配置环境变量即可使用。

## 快速测试

### 测试 Ollama（推荐）

```bash
# 1. 安装 Ollama（如果未安装）
brew install ollama

# 2. 启动服务
ollama serve &

# 3. 拉取模型
ollama pull llama2

# 4. 测试
curl http://localhost:11434/api/tags

# 5. 测试应用
curl -X POST 'http://localhost:8080/api/script/analyze' \
  -H 'Content-Type: application/json' \
  --data-raw '{"script":"def keys = redis.keys(\"user:*\")\nreturn keys"}'
```

### 测试 vLLM（如果已安装）

```bash
# 1. 启动服务
./scripts/start-vllm.sh

# 2. 等待服务启动（可能需要几分钟）

# 3. 测试
curl http://localhost:8000/v1/models

# 4. 测试应用
curl -X POST 'http://localhost:8080/api/script/analyze' \
  -H 'Content-Type: application/json' \
  --data-raw '{"script":"def keys = redis.keys(\"user:*\")\nreturn keys","provider":"VLLM"}'
```

## 当前建议

由于 vLLM 在 macOS 上安装复杂，**强烈推荐使用 Ollama**：

1. ✅ 更简单易用
2. ✅ 更好的 macOS 支持
3. ✅ 已经在项目配置中
4. ✅ 无需 GPU（CPU 也能运行）

应用已经实现了**自动回退机制**：
- 如果指定的 provider 不可用，会自动使用其他可用的服务
- 如果 VLLM 不可用，会自动使用 Ollama（如果配置了）

## 下一步

1. **安装 Ollama**（推荐）：
   ```bash
   brew install ollama
   ollama serve
   ollama pull llama2
   ```

2. **或者继续尝试 vLLM**：
   - 需要 Apple Silicon (M1/M2/M3) 芯片
   - 需要足够的系统内存（16GB+）
   - 安装可能需要较长时间

3. **使用云端 API**：
   - 配置 `OPENAI_API_KEY` 或 `CLAUDE_API_KEY`
   - 无需本地服务

