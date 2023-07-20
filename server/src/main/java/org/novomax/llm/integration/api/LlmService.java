package org.novomax.llm.integration.api;

public interface LlmService {
    double[] getEmbeddingVector(String document);
}
