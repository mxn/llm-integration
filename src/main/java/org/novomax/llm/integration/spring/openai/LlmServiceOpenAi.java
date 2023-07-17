package org.novomax.llm.integration.spring.openai;

import net.minidev.json.JSONObject;
import org.novomax.llm.integration.LlmService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static org.novomax.llm.integration.spring.openai.Constants.LLM_OPENAI_QUALIFIER;

@Service
public class LlmServiceOpenAi implements LlmService {

    private final String urlExternalEmbedService;
    private final RestTemplate restTemplateOpenAi;

    public LlmServiceOpenAi(//
                            @Value("${org.novomax.llm.integration.spring.openai.urlExternalEmbedService:https://api.openai.com/v1/embeddings}")
                            String urlExternalEmbedService,
                            @Qualifier(LLM_OPENAI_QUALIFIER)
                            RestTemplate restTemplateOpenAi) {
        this.urlExternalEmbedService = urlExternalEmbedService;
        this.restTemplateOpenAi = restTemplateOpenAi;
    }

    @Override
    public double[] getEmbeddingVector(String document) {
        JSONObject request = new JSONObject();
        request.put("model", "text-embedding-ada-002");
        request.put("input", document);
        OpenAiEmbedResponse openAiEmbedResponse = restTemplateOpenAi.postForObject(urlExternalEmbedService, request, OpenAiEmbedResponse.class);
        return openAiEmbedResponse.getData()[0].getEmbedding();
    }
}
