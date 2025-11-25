package org.example.service.llm;

/**
 * LLM 接口，所有类型 LLM provider 均实现此
 */
public interface LlmService {

    /**
     * 使用 LLM 分析脚本
     *
     * @param script The script to analyze
     * @return Analysis result
     */
    String analyzeScript(String script) throws Exception;

    /**
     * 获取 provider name
     *
     * @return Provider name
     */
    LlmProvider getProvider();

    /**
     * 检查服务是否可用
     *
     * @return true if available
     */
    boolean isAvailable();
}
