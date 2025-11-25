#!/bin/bash

# Stop vLLM service
# Usage: ./stop-vllm.sh

PID_FILE="vllm.pid"

if [ ! -f "$PID_FILE" ]; then
    echo "vLLM service PID file not found. Service may not be running."
    exit 1
fi

PID=$(cat "$PID_FILE")

if ! ps -p "$PID" > /dev/null 2>&1; then
    echo "vLLM service (PID: $PID) is not running."
    rm -f "$PID_FILE"
    exit 1
fi

echo "Stopping vLLM service (PID: $PID)..."
kill "$PID"

# Wait for process to stop
sleep 2

if ps -p "$PID" > /dev/null 2>&1; then
    echo "Service did not stop gracefully, forcing termination..."
    kill -9 "$PID"
fi

rm -f "$PID_FILE"
echo "vLLM service stopped."

