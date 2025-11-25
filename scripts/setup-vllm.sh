#!/bin/bash

# Setup and start vLLM service
# Usage: ./setup-vllm.sh [--cpu]
#   --cpu: Install vLLM for CPU-only mode (Intel CPU support)

set -e

CPU_MODE=false
if [[ "$1" == "--cpu" ]]; then
    CPU_MODE=true
    echo "⚠️  CPU 模式：性能有限（约 20-50 tokens/s），仅适用于测试"
    echo ""
fi

echo "Setting up vLLM LLM service..."

# Check if Python 3 is installed
if ! command -v python3 &> /dev/null; then
    echo "Error: Python 3 is not installed. Please install Python 3.8+ first."
    exit 1
fi

PYTHON_VERSION=$(python3 --version 2>&1 | awk '{print $2}')
echo "Found Python $PYTHON_VERSION"

# Check if virtual environment exists
VENV_DIR="vllm-env"
if [ ! -d "$VENV_DIR" ]; then
    echo "Creating Python virtual environment..."
    python3 -m venv "$VENV_DIR"
fi

# Activate virtual environment
echo "Activating virtual environment..."
source "$VENV_DIR/bin/activate"

# Upgrade pip
pip install --upgrade pip

# Check if vLLM is installed
if ! python3 -m vllm.entrypoints.openai.api_server --help &> /dev/null; then
    echo "vLLM is not installed. Installing vLLM..."
    echo "Note: This may take several minutes and requires significant disk space."
    
    if [ "$CPU_MODE" = true ]; then
        echo ""
        echo "🔧 安装 CPU 模式 vLLM（从源码编译）..."
        echo "这可能需要 30-60 分钟，请耐心等待..."
        echo ""
        
        # Install build dependencies
        echo "安装编译依赖..."
        pip install --upgrade pip setuptools wheel
        
        # Install Intel Extension for PyTorch (IPEX) for better CPU performance
        echo "安装 Intel Extension for PyTorch..."
        pip install intel-extension-for-pytorch
        
        # Install vLLM from source with CPU support
        echo "从源码编译 vLLM（CPU 模式）..."
        echo "这需要较长时间，请耐心等待..."
        
        # Clone and install from source
        if [ ! -d "vllm-source" ]; then
            echo "克隆 vLLM 源码..."
            git clone --recursive https://github.com/vllm-project/vllm.git vllm-source
        fi
        
        cd vllm-source
        
        # Install CPU dependencies
        pip install -e . --no-build-isolation
        
        cd ..
        
        echo "✓ vLLM CPU 模式安装完成！"
    else
        echo "vLLM 默认需要 CUDA/GPU 支持。"
        echo "如果您的系统没有 GPU，请使用: ./setup-vllm.sh --cpu"
        echo ""
        
        # Try to install standard vLLM (will fail on CPU-only systems)
        pip install vllm || {
            echo ""
            echo "⚠️  标准 vLLM 安装失败（可能因为没有 GPU）"
            echo "请使用 CPU 模式安装: ./setup-vllm.sh --cpu"
            exit 1
        }
        
        echo "vLLM installed successfully!"
    fi
else
    echo "vLLM is already installed."
fi

# Check if service is already running
if curl -s http://localhost:8000/v1/models &> /dev/null; then
    echo "vLLM service is already running on port 8000."
    exit 0
fi

# Start vLLM service
echo ""
echo "Starting vLLM service..."
echo "This will download the model on first run (may take a while)..."
echo ""

# Get model from config or use default
MODEL="${VLLM_MODEL:-meta-llama/Llama-2-7b-chat-hf}"
PORT="${VLLM_PORT:-8000}"

echo "Starting vLLM with model: $MODEL on port: $PORT"
if [ "$CPU_MODE" = true ]; then
    echo "Mode: CPU-only (性能有限)"
fi
echo "Press Ctrl+C to stop the service"
echo ""

# Build command
CMD="python3 -m vllm.entrypoints.openai.api_server --model \"$MODEL\" --port \"$PORT\" --host 0.0.0.0"

# Add CPU mode parameters if needed
if [ "$CPU_MODE" = true ]; then
    # Use smaller batch size for CPU
    CMD="$CMD --max-num-seqs 4"
    # Enable CPU optimizations via environment variables
    export VLLM_USE_CPU=1
    export VLLM_CPU_KVCACHE_SPACE=4  # GB
    # Optional: Set thread count for better CPU utilization
    export OMP_NUM_THREADS=${OMP_NUM_THREADS:-8}
    export MKL_NUM_THREADS=${MKL_NUM_THREADS:-8}
fi

# Start vLLM in background and save PID
eval "$CMD" > vllm.log 2>&1 &

VLLM_PID=$!
echo $VLLM_PID > vllm.pid

echo "vLLM service started with PID: $VLLM_PID"
echo "Logs are being written to: vllm.log"
echo ""

# Wait a bit and check if service is up
sleep 5

if curl -s http://localhost:$PORT/v1/models &> /dev/null; then
    echo "✓ vLLM service is running successfully!"
    echo ""
    echo "Test the service with:"
    echo "  curl http://localhost:$PORT/v1/models"
    echo ""
    echo "To stop the service, run:"
    echo "  kill \$(cat vllm.pid)"
    echo "  or"
    echo "  ./scripts/stop-vllm.sh"
else
    echo "⚠ vLLM service may still be starting up. Check vllm.log for details."
    echo "  tail -f vllm.log"
fi

