# vLLM CPU 模式安装指南（Intel CPU）

## 📋 概述

本指南介绍如何在 Intel CPU 上安装和配置 vLLM 的 CPU 模式。

⚠️ **重要提示**：
- CPU 模式性能有限：约 20-50 tokens/s
- 仅适用于轻量级测试和开发环境
- 生产环境建议使用 GPU 或云端 API

## 🔧 系统要求

- **CPU**：Intel x86_64（支持 AVX-512 更佳）
- **内存**：16GB+（推荐 32GB）
- **Python**：3.8 - 3.11
- **磁盘空间**：20GB+（用于模型和编译）

## 📦 快速安装（推荐）

使用项目提供的脚本：

```bash
# 1. 进入项目目录
cd /path/to/entrytest

# 2. 安装 vLLM（CPU 模式）
# ⚠️ 注意：从源码编译，可能需要 30-60 分钟
./scripts/setup-vllm.sh --cpu

# 3. 启动服务
./scripts/start-vllm.sh --cpu

# 4. 验证服务
curl http://localhost:8000/v1/models
```

## 🛠️ 手动安装步骤

### 步骤 1: 创建虚拟环境

```bash
python3 -m venv vllm-env
source vllm-env/bin/activate
pip install --upgrade pip setuptools wheel
```

### 步骤 2: 安装编译依赖

```bash
# 安装基础依赖
pip install torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cpu

# 安装 Intel Extension for PyTorch（可选，但推荐）
pip install intel-extension-for-pytorch
```

### 步骤 3: 从源码安装 vLLM

```bash
# 克隆 vLLM 源码
git clone --recursive https://github.com/vllm-project/vllm.git vllm-source
cd vllm-source

# 安装 vLLM（CPU 模式）
pip install -e . --no-build-isolation

cd ..
```

### 步骤 4: 验证安装

```bash
python3 -m vllm.entrypoints.openai.api_server --help
```

如果看到帮助信息，说明安装成功。

## 🚀 启动服务

### 方法 1: 使用脚本

```bash
./scripts/start-vllm.sh --cpu
```

### 方法 2: 手动启动

```bash
source vllm-env/bin/activate

# 设置环境变量
export VLLM_USE_CPU=1
export VLLM_CPU_KVCACHE_SPACE=4  # KV 缓存空间（GB）

# 启动服务
python3 -m vllm.entrypoints.openai.api_server \
  --model meta-llama/Llama-2-7b-chat-hf \
  --port 8000 \
  --host 0.0.0.0 \
  --device cpu \
  --max-num-seqs 4
```

## ⚙️ 配置参数说明

### CPU 模式关键参数

- `--max-num-seqs 4`：CPU 模式下建议使用较小的批处理大小
- `VLLM_USE_CPU=1`：环境变量，启用 CPU 模式（必需）
- `VLLM_CPU_KVCACHE_SPACE=4`：KV 缓存空间（GB），根据内存调整
- `OMP_NUM_THREADS=8`：OpenMP 线程数，根据 CPU 核心数调整
- `MKL_NUM_THREADS=8`：Intel MKL 线程数，根据 CPU 核心数调整

### 性能优化参数

```bash
# 使用 Intel Extension for PyTorch 优化
export IPEX_TORCH_CPU_LAUNCH=1

# 设置线程数（根据 CPU 核心数调整）
export OMP_NUM_THREADS=8
export MKL_NUM_THREADS=8
```

## 📊 性能参考

| 配置 | 性能 | 说明 |
|------|------|------|
| Intel CPU（无优化） | 20-30 tokens/s | 基础性能 |
| Intel CPU + IPEX | 30-50 tokens/s | 使用 Intel 扩展优化 |
| GPU（参考） | 100+ tokens/s | GPU 模式性能 |

## 🧪 测试服务

### 1. 检查服务状态

```bash
curl http://localhost:8000/v1/models
```

### 2. 测试 API

```bash
curl http://localhost:8000/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "meta-llama/Llama-2-7b-chat-hf",
    "messages": [{"role": "user", "content": "Hello"}],
    "max_tokens": 100
  }'
```

### 3. 测试应用集成

```bash
# 确保 application.yml 中配置了 vLLM
# primary-provider: VLLM

curl -X POST 'http://localhost:8080/api/script/analyze' \
  -H 'Content-Type: application/json' \
  --data-raw '{"script":"def keys = redis.keys(\"user:*\")\nreturn keys"}'
```

## ❌ 常见问题

### 1. 编译失败

**问题**：`error: failed to build wheel`

**解决方案**：
```bash
# 确保安装了所有编译工具
# macOS
xcode-select --install

# Linux
sudo apt-get install build-essential cmake
```

### 2. 内存不足

**问题**：`Out of memory`

**解决方案**：
- 使用更小的模型（如 7B）
- 减少 `VLLM_CPU_KVCACHE_SPACE` 值
- 减少 `--max-num-seqs` 参数

### 3. 性能太低

**问题**：推理速度很慢

**解决方案**：
- 这是 CPU 模式的正常表现
- 安装 Intel Extension for PyTorch
- 使用更小的模型
- 考虑使用 GPU 或云端 API

### 4. 模型下载失败

**问题**：无法下载模型

**解决方案**：
```bash
# 手动下载模型到本地
# 然后使用本地路径
python3 -m vllm.entrypoints.openai.api_server \
  --model /path/to/local/model \
  --device cpu
```

## 🔄 与项目集成

### 更新 application.yml

确保配置了 vLLM：

```yaml
llm:
  primary-provider: VLLM
  vllm:
    api-url: http://localhost:8000/v1/chat/completions
    model: meta-llama/Llama-2-7b-chat-hf
```

### 切换提供商

如果需要切换回其他提供商：

```yaml
llm:
  primary-provider: OLLAMA  # 或其他提供商
```

## 📚 参考资源

- [vLLM 官方文档](https://docs.vllm.ai/)
- [vLLM CPU 安装指南](https://docs.vllm.ai/en/latest/getting_started/installation/cpu.html)
- [Intel Extension for PyTorch](https://intel.github.io/intel-extension-for-pytorch/)

## 💡 替代方案

如果 CPU 模式性能无法满足需求，考虑：

1. **Ollama**：更适合 CPU 的本地 LLM 方案
2. **云端 API**：OpenAI、Claude 等
3. **GPU 服务器**：使用云 GPU 服务

