package org.novomax.llm.integration.api;

public interface SearchResult {
    String entityClass();

    String entityId();

    float score();

}
