package org.novomax.llm.integration.spring;

import org.novomax.llm.integration.AiService;
import org.novomax.llm.integration.LlmService;
import org.novomax.llm.integration.SearchResult;
import org.novomax.llm.integration.VectorStorage;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiServiceImpl implements AiService {
    private final VectorStorage vectorStorage;
    private final LlmService llmService;

    public AiServiceImpl(VectorStorage vectorStorage, LlmService llmService) {
        this.vectorStorage = vectorStorage;
        this.llmService = llmService;
    }


    @Override
    public List<SearchResult> findByFreeText(String searchText, int limit) {
        return vectorStorage.search(llmService.getEmbeddingVector(searchText), limit);
    }
}
