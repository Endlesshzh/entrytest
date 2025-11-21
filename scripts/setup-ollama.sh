#!/bin/bash

# Setup Ollama and pull recommended models
# Usage: ./setup-ollama.sh

echo "Setting up Ollama LLM..."

# Check if Ollama is installed
if ! command -v ollama &> /dev/null; then
    echo "Ollama is not installed. Installing..."

    # Detect OS
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        echo "Installing Ollama on macOS..."
        curl -fsSL https://ollama.com/install.sh | sh
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        # Linux
        echo "Installing Ollama on Linux..."
        curl -fsSL https://ollama.com/install.sh | sh
    else
        echo "Unsupported OS. Please install Ollama manually from https://ollama.com"
        exit 1
    fi
fi

echo "Ollama installed successfully!"

# Check if Ollama service is running
if ! curl -s http://localhost:11434/api/tags &> /dev/null; then
    echo "Starting Ollama service..."
    ollama serve &
    sleep 5
fi

echo "Pulling recommended models..."

# Pull llama2 (general purpose)
echo "Pulling llama2 model (this may take a while)..."
ollama pull llama2

# Optionally pull codellama (better for code analysis)
read -p "Do you want to pull codellama model? (recommended for better code analysis) [y/N]: " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "Pulling codellama model..."
    ollama pull codellama
fi

# Optionally pull mistral (lightweight alternative)
read -p "Do you want to pull mistral model? (lightweight, faster) [y/N]: " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "Pulling mistral model..."
    ollama pull mistral
fi

echo ""
echo "Ollama setup completed!"
echo ""
echo "Available models:"
ollama list
echo ""
echo "To test Ollama, run:"
echo "  curl http://localhost:11434/api/generate -d '{\"model\": \"llama2\", \"prompt\": \"Hello\", \"stream\": false}'"
echo ""
