package org.novomax.llm.integration;

public interface SearchResult {
    String entityClass();

    String entityId();

    float score();

}
