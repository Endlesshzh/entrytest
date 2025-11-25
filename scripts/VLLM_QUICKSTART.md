# vLLM 快速开始指南

## 🚀 快速安装和启动

### Intel CPU 用户（推荐使用 CPU 模式）

```bash
# 1. 安装 vLLM（CPU 模式）
# ⚠️ 注意：从源码编译，可能需要 30-60 分钟
./scripts/setup-vllm.sh --cpu

# 2. 启动服务
./scripts/start-vllm.sh --cpu

# 3. 验证服务
curl http://localhost:8000/v1/models
```

### GPU 用户（NVIDIA/Apple Silicon）

```bash
# 1. 安装 vLLM（GPU 模式）
./scripts/setup-vllm.sh

# 2. 启动服务
./scripts/start-vllm.sh

# 3. 验证服务
curl http://localhost:8000/v1/models
```

## 📝 配置应用

编辑 `src/main/resources/application.yml`：

```yaml
llm:
  primary-provider: VLLM
  vllm:
    api-url: http://localhost:8000/v1/chat/completions
    model: meta-llama/Llama-2-7b-chat-hf
```

## 🧪 测试

```bash
# 测试 vLLM API
curl http://localhost:8000/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "meta-llama/Llama-2-7b-chat-hf",
    "messages": [{"role": "user", "content": "Hello"}]
  }'

# 测试应用集成
curl -X POST 'http://localhost:8080/api/script/analyze' \
  -H 'Content-Type: application/json' \
  --data-raw '{"script":"def keys = redis.keys(\"user:*\")\nreturn keys"}'
```

## 📚 更多信息

- **CPU 模式详细指南**：`scripts/VLLM_CPU_INSTALL.md`
- **完整文档**：`scripts/README-vllm.md`
- **常见问题**：查看上述文档的 FAQ 部分

## ⚠️ 重要提示

- **CPU 模式**：性能有限（20-50 tokens/s），仅适用于测试
- **GPU 模式**：性能最佳，推荐用于生产环境
- **替代方案**：如果性能不足，考虑使用 Ollama 或云端 API

