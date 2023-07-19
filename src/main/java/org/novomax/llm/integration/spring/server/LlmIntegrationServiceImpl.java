package org.novomax.llm.integration.spring.server;

import org.novomax.llm.integration.api.LlmIntegrationService;
import org.novomax.llm.integration.api.LlmService;
import org.novomax.llm.integration.api.SearchResult;
import org.novomax.llm.integration.api.VectorStorage;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LlmIntegrationServiceImpl implements LlmIntegrationService {
    private final VectorStorage vectorStorage;
    private final LlmService llmService;

    public LlmIntegrationServiceImpl(VectorStorage vectorStorage, LlmService llmService) {
        this.vectorStorage = vectorStorage;
        this.llmService = llmService;
    }


    @Override
    public List<SearchResult> findByFreeText(String searchText, int limit) {
        return vectorStorage.search(llmService.getEmbeddingVector(searchText), limit);
    }
}
