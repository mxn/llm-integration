package org.novomax.llm.integration;

import java.util.List;

public interface AiService {
    List<SearchResult> findByFreeText(String searchText, int limit);
}
