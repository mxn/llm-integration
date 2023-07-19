package org.novomax.llm.integration.api;

import java.util.List;

public interface LlmIntegrationService {
    List<SearchResult> findByFreeText(String searchText, int limit);
}
