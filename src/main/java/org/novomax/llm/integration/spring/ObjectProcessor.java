package org.novomax.llm.integration.spring;

import jakarta.persistence.Id;
import org.novomax.llm.integration.LlmService;
import org.novomax.llm.integration.VectorStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class ObjectProcessor {
    private final LlmConfig llmConfig;
    static final String ENTITY_CLASS_KEY = "entityClass";
    static final String ENTITY_ID_KEY = "id";
    static final String TEXT_KEY = "text";

    static final String ACTION_KEY = "action";

    private static JmsTemplate jmsTemplate;
    private final Logger logger = LoggerFactory.getLogger(ObjectProcessor.class);
    private final VectorStorage vectorStorage;

    private final LlmService llmService;

    public ObjectProcessor(LlmConfig llmConfig, VectorStorage vectorStorage, LlmService llmService) {
        this.llmConfig = llmConfig;
        this.vectorStorage = vectorStorage;
        this.llmService = llmService;
    }

    private static String getText(Object candidate) {
        return Arrays.stream(candidate.getClass().getDeclaredMethods()) //
                .filter(method -> method.getAnnotation(LlmText.class) != null) //
                .map(method -> {
                    try {
                        Object methodeResult = method.invoke(candidate);
                        return (String) methodeResult;
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new IllegalStateException(e);
                    }
                }) //
                .filter(Objects::nonNull) //
                .collect(Collectors.joining("\\n\\n"));
    }

    private static Object getIdValue(Object candidate) {
        Field idField = Arrays.stream(candidate.getClass().getDeclaredFields()) //
                .filter(field -> field.getAnnotation(Id.class) != null) //
                .findAny() //
                .orElseThrow(() -> new IllegalStateException(candidate.getClass() + " has no Id Field!"));
        idField.setAccessible(true);
        try {
            return idField.get(candidate);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @Autowired
    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        ObjectProcessor.jmsTemplate = jmsTemplate;
    }

    Map<String, String> toMap(final Object candidate, Action action) {
        Map<String, String> res = new HashMap<>();
        res.put(ENTITY_CLASS_KEY, candidate.getClass().getName());
        Object idValue = getIdValue(candidate);
        res.put(ENTITY_ID_KEY, idValue.toString());
        String text = getText(candidate);
        res.put(TEXT_KEY, text);
        res.put(ACTION_KEY, action.name());
        return res;
    }

    public void upcert(Object object) {
        jmsTemplate.convertAndSend(llmConfig.getDestinationName(), toMap(object, Action.UPSERT));
    }


    @JmsListener(destination = "#{llmConfig.destinationName}")
    void onMessage(Map<String, String> message) {
        logger.debug("got {}: {} {} {}", message.get(ENTITY_CLASS_KEY), message.get(ENTITY_ID_KEY),
                message.get(TEXT_KEY), message.get(ACTION_KEY));
        switch (Action.valueOf(message.get(ACTION_KEY))) {
            case UPSERT -> {
                if (message.get(TEXT_KEY) == null || "".equals(message.get(TEXT_KEY).trim())) {
                    logger.debug("Delete {}:{}, text is empty", message.get(ENTITY_CLASS_KEY), message.get(ENTITY_ID_KEY));
                    vectorStorage.delete(message.get(ENTITY_CLASS_KEY), message.get(ENTITY_ID_KEY));
                } else if (vectorStorage.shouldUpdateEmbedding(message.get(ENTITY_CLASS_KEY), message.get(ENTITY_ID_KEY), message.get(TEXT_KEY))) {
                    logger.debug("Update  {}:{} vector db entry", message.get(ENTITY_CLASS_KEY), message.get(ENTITY_ID_KEY));
                    vectorStorage.upcert(message.get(ENTITY_CLASS_KEY), message.get(ENTITY_ID_KEY), message.get(TEXT_KEY),
                            llmService.getEmbeddingVector(message.get(TEXT_KEY)));
                } else {
                    logger.debug("No action in vector db  {}:{}", message.get(ENTITY_CLASS_KEY), message.get(ENTITY_ID_KEY));
                }
            }
            case DELETE -> {
                logger.debug("Delete {}:{}: action DELETE", message.get(ENTITY_CLASS_KEY), message.get(ENTITY_ID_KEY));
                vectorStorage.delete(message.get(ENTITY_CLASS_KEY), message.get(ENTITY_ID_KEY));
            }

        }
    }

    public void remove(Object object) {
        jmsTemplate.convertAndSend(llmConfig.getDestinationName(), toMap(object, Action.DELETE));
    }
}
