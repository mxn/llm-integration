package org.novomax.llm.integration.spring.server;

import org.novomax.llm.integration.api.LlmService;
import org.novomax.llm.integration.api.VectorStorage;
import org.novomax.llm.integration.spring.Constants;
import org.novomax.llm.integration.spring.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.novomax.llm.integration.spring.Constants.ACTION_KEY;

@Component
public class MessageListener {
    private final Logger logger = LoggerFactory.getLogger(MessageListener.class);
    private final VectorStorage vectorStorage;

    private final LlmService llmService;

    public MessageListener(final VectorStorage vectorStorage, final LlmService llmService) {
        this.vectorStorage = vectorStorage;
        this.llmService = llmService;
    }

    @JmsListener(destination = "#{llmConfig.destinationName}")
    void onMessage(Map<String, String> message) {
        logger.debug("got {}: {} {} {}", message.get(Constants.ENTITY_CLASS_KEY), message.get(Constants.ENTITY_ID_KEY),
                message.get(Constants.TEXT_KEY), message.get(ACTION_KEY));
        switch (Action.valueOf(message.get(ACTION_KEY))) {
            case UPSERT -> {
                if (message.get(Constants.TEXT_KEY) == null || "".equals(message.get(Constants.TEXT_KEY).trim())) {
                    logger.debug("Delete {}:{}, text is empty", message.get(Constants.ENTITY_CLASS_KEY), message.get(Constants.ENTITY_ID_KEY));
                    vectorStorage.delete(message.get(Constants.ENTITY_CLASS_KEY), message.get(Constants.ENTITY_ID_KEY));
                } else if (vectorStorage.shouldUpdateEmbedding(message.get(Constants.ENTITY_CLASS_KEY), message.get(Constants.ENTITY_ID_KEY), message.get(Constants.TEXT_KEY))) {
                    logger.debug("Update  {}:{} vector db entry", message.get(Constants.ENTITY_CLASS_KEY), message.get(Constants.ENTITY_ID_KEY));
                    vectorStorage.upcert(message.get(Constants.ENTITY_CLASS_KEY), message.get(Constants.ENTITY_ID_KEY), message.get(Constants.TEXT_KEY),
                            llmService.getEmbeddingVector(message.get(Constants.TEXT_KEY)));
                } else {
                    logger.debug("No action in vector db  {}:{}", message.get(Constants.ENTITY_CLASS_KEY), message.get(Constants.ENTITY_ID_KEY));
                }
            }
            case DELETE -> {
                logger.debug("Delete {}:{}: action DELETE", message.get(Constants.ENTITY_CLASS_KEY), message.get(Constants.ENTITY_ID_KEY));
                vectorStorage.delete(message.get(Constants.ENTITY_CLASS_KEY), message.get(Constants.ENTITY_ID_KEY));
            }

        }
    }

}
