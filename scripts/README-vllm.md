# vLLM 服务说明

## ⚠️ 重要提示

vLLM 是一个高性能的 LLM 推理服务，但有以下要求：

1. **GPU 支持**（推荐）：vLLM 最佳性能需要 NVIDIA GPU 或 Apple Silicon (M1/M2/M3) GPU
2. **CPU 支持**（有限）：vLLM 可以在 Intel CPU 上运行，但性能较低（约 20-50 tokens/s）
3. **大量内存**：模型需要 8GB+ 内存
4. **磁盘空间**：模型文件可能需要 10GB+ 空间

## 系统支持

### GPU 模式（推荐）
- **NVIDIA GPU**：CUDA 11.8+，性能最佳
- **Apple Silicon**：M1/M2/M3 芯片，macOS 12.0+，需要 16GB+ 内存

### CPU 模式（Intel CPU）
- **Intel CPU**：支持，但性能有限
- **安装方式**：需要从源码编译（`./setup-vllm.sh --cpu`）
- **性能**：约 20-50 tokens/s，仅适用于轻量级测试
- **优化**：可使用 Intel Extension for PyTorch (IPEX) 提升性能

## macOS 用户

在 macOS 上：
- **Apple Silicon (M1/M2/M3)**：支持 GPU 模式，性能较好
- **Intel Mac**：仅支持 CPU 模式，性能有限

### 如果您的 Mac 不支持 vLLM 或性能不足

**推荐使用 Ollama**，它是更简单、更适合 macOS 的本地 LLM 方案：

```bash
# 安装 Ollama
curl -fsSL https://ollama.com/install.sh | sh

# 启动服务
ollama serve

# 拉取模型
ollama pull llama2
```

Ollama 已经在项目的 `docker-compose.yml` 中配置好了。

## 安装和启动 vLLM

### 方法 1: 使用脚本（推荐）

#### GPU 模式安装（默认）
```bash
# 首次安装和启动（GPU 模式）
./scripts/setup-vllm.sh

# 后续启动（已安装）
./scripts/start-vllm.sh

# 停止服务
./scripts/stop-vllm.sh
```

#### CPU 模式安装（Intel CPU）
```bash
# 首次安装和启动（CPU 模式）
# ⚠️ 注意：从源码编译，可能需要 30-60 分钟
./scripts/setup-vllm.sh --cpu

# 后续启动（CPU 模式）
./scripts/start-vllm.sh --cpu

# 停止服务
./scripts/stop-vllm.sh
```

### 方法 2: 手动安装

#### GPU 模式
```bash
# 1. 创建虚拟环境
python3 -m venv vllm-env
source vllm-env/bin/activate

# 2. 安装 vLLM
pip install vllm

# 3. 启动服务
python3 -m vllm.entrypoints.openai.api_server \
  --model meta-llama/Llama-2-7b-chat-hf \
  --port 8000
```

#### CPU 模式（Intel CPU）
```bash
# 1. 创建虚拟环境
python3 -m venv vllm-env
source vllm-env/bin/activate
pip install --upgrade pip setuptools wheel

# 2. 安装 Intel Extension for PyTorch
pip install intel-extension-for-pytorch

# 3. 从源码安装 vLLM（CPU 模式）
git clone --recursive https://github.com/vllm-project/vllm.git vllm-source
cd vllm-source
pip install -e . --no-build-isolation
cd ..

# 4. 启动服务（CPU 模式）
export VLLM_USE_CPU=1
export VLLM_CPU_KVCACHE_SPACE=4
python3 -m vllm.entrypoints.openai.api_server \
  --model meta-llama/Llama-2-7b-chat-hf \
  --port 8000 \
  --device cpu \
  --max-num-seqs 4
```

### 方法 3: 使用 Docker（需要 NVIDIA GPU）

```bash
docker run --gpus all -p 8000:8000 \
  vllm/vllm-openai:latest \
  --model meta-llama/Llama-2-7b-chat-hf
```

## 验证服务

```bash
# 检查服务是否运行
curl http://localhost:8000/v1/models

# 测试 API
curl http://localhost:8000/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "meta-llama/Llama-2-7b-chat-hf",
    "messages": [{"role": "user", "content": "Hello"}]
  }'
```

## 常见问题

### 1. "No module named 'vllm'"
运行 `./scripts/setup-vllm.sh` 安装 vLLM

### 2. "CUDA out of memory" 或没有 GPU
- **有 GPU**：使用更小的模型，减少 `--max-num-seqs` 参数
- **无 GPU（Intel CPU）**：使用 CPU 模式：`./setup-vllm.sh --cpu`
- **性能要求不高**：考虑使用 Ollama

### 3. 服务启动很慢
- 首次启动需要下载模型，可能需要几分钟到几十分钟，取决于网络速度
- CPU 模式下启动和推理都会更慢

### 4. Intel CPU 上性能很低
- CPU 模式性能有限（20-50 tokens/s），这是正常的
- 可以尝试安装 Intel Extension for PyTorch 优化性能
- 如果性能要求高，建议使用 GPU 或云端 API

### 5. macOS 上无法运行
- **Apple Silicon**：支持 GPU 模式
- **Intel Mac**：仅支持 CPU 模式，性能有限，建议使用 Ollama

## 推荐配置

### 高性能（需要多 GPU）
```bash
python3 -m vllm.entrypoints.openai.api_server \
  --model meta-llama/Llama-2-7b-chat-hf \
  --port 8000 \
  --tensor-parallel-size 2 \
  --gpu-memory-utilization 0.9 \
  --max-num-seqs 256
```

### 基础配置（单 GPU）
```bash
python3 -m vllm.entrypoints.openai.api_server \
  --model meta-llama/Llama-2-7b-chat-hf \
  --port 8000
```

### CPU 模式配置（Intel CPU）
```bash
export VLLM_USE_CPU=1
export VLLM_CPU_KVCACHE_SPACE=4
export OMP_NUM_THREADS=8
export MKL_NUM_THREADS=8
python3 -m vllm.entrypoints.openai.api_server \
  --model meta-llama/Llama-2-7b-chat-hf \
  --port 8000 \
  --max-num-seqs 4
```

**性能提示**：
- CPU 模式性能约 20-50 tokens/s
- 建议使用较小的模型（如 7B）
- 可以安装 Intel Extension for PyTorch 提升性能

## 替代方案

如果 vLLM 无法在您的系统上运行，推荐使用：

1. **Ollama** - 最简单，支持 macOS/Windows/Linux
2. **LMDeploy** - 另一个高性能选项
3. **云端 API** - OpenAI, Claude 等

