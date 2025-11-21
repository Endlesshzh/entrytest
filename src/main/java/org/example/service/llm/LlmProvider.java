package org.example.service.llm;

/**
 * LLM Provider Enum
 * Supported LLM providers
 */
public enum LlmProvider {
    OPENAI("OpenAI"),
    CLAUDE("Claude"),
    COMPASS("Compass"),
    OLLAMA("Ollama (Local)"),
    VLLM("vLLM (Local)"),
    LLMDEPLOY("LMDeploy (Local)"),
    SGLANG("SGLang (Local)");

    private final String displayName;

    LlmProvider(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isLocal() {
        return this == OLLAMA || this == VLLM || this == LLMDEPLOY || this == SGLANG;
    }
}
