package org.novomax.llm.integration;

public interface LlmService {
    double[] getEmbeddingVector(String document);
}
