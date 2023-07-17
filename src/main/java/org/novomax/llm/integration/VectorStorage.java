package org.novomax.llm.integration;

import java.util.List;

public interface VectorStorage {
    List<SearchResult> search(double[] vector, int limit);

    void upcert(final String entityClass, final String entityId, String text, double[] embedding);

    void delete(final String entityClass, final String entityId);

    boolean shouldUpdateEmbedding(String entityClass, String entityId, String text);
}
