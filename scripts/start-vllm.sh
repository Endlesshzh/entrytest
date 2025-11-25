#!/bin/bash

# Start vLLM service (assumes it's already installed)
# Usage: ./start-vllm.sh [--cpu]
#   --cpu: Start vLLM in CPU-only mode

set -e

CPU_MODE=false
if [[ "$1" == "--cpu" ]]; then
    CPU_MODE=true
fi

VENV_DIR="vllm-env"

if [ ! -d "$VENV_DIR" ]; then
    echo "Error: vLLM virtual environment not found."
    echo "Please run ./scripts/setup-vllm.sh first to install vLLM."
    exit 1
fi

# Activate virtual environment
source "$VENV_DIR/bin/activate"

# Check if service is already running
if curl -s http://localhost:8000/v1/models &> /dev/null; then
    echo "vLLM service is already running on port 8000."
    exit 0
fi

# Get model from config or use default
MODEL="${VLLM_MODEL:-meta-llama/Llama-2-7b-chat-hf}"
PORT="${VLLM_PORT:-8000}"

echo "Starting vLLM service..."
echo "Model: $MODEL"
echo "Port: $PORT"
if [ "$CPU_MODE" = true ]; then
    echo "Mode: CPU-only (性能有限)"
fi
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

# Start vLLM in background
eval "$CMD" > vllm.log 2>&1 &

VLLM_PID=$!
echo $VLLM_PID > vllm.pid

echo "vLLM service started with PID: $VLLM_PID"
echo "Logs: vllm.log"
echo ""
echo "Waiting for service to be ready..."

# Wait for service to be ready (max 60 seconds)
for i in {1..60}; do
    if curl -s http://localhost:$PORT/v1/models &> /dev/null; then
        echo "✓ vLLM service is ready!"
        exit 0
    fi
    sleep 1
    echo -n "."
done

echo ""
echo "⚠ Service may still be starting. Check vllm.log for details."

