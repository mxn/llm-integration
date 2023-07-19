package org.novomax.llm.integration.spring.client;

import jakarta.persistence.Id;
import org.novomax.llm.integration.spring.Constants;
import org.novomax.llm.integration.spring.LlmConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.novomax.llm.integration.spring.Constants.LLM_OPENAI_QUALIFIER;

@Component
public class ObjectProcessor {
    private final LlmConfig llmConfig;

    private static JmsTemplate jmsTemplate;

    public ObjectProcessor(LlmConfig llmConfig) {
        this.llmConfig = llmConfig;
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
    @Qualifier(LLM_OPENAI_QUALIFIER)
    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        ObjectProcessor.jmsTemplate = jmsTemplate;
    }

    Map<String, String> toMap(final Object candidate, Action action) {
        Map<String, String> res = new HashMap<>();
        res.put(Constants.ENTITY_CLASS_KEY, candidate.getClass().getName());
        Object idValue = getIdValue(candidate);
        res.put(Constants.ENTITY_ID_KEY, idValue.toString());
        String text = getText(candidate);
        res.put(Constants.TEXT_KEY, text);
        res.put(Constants.ACTION_KEY, action.name());
        return res;
    }

    public void upcert(Object object) {
        jmsTemplate.convertAndSend(llmConfig.getDestinationName(), toMap(object, Action.UPSERT));
    }



    public void remove(Object object) {
        jmsTemplate.convertAndSend(llmConfig.getDestinationName(), toMap(object, Action.DELETE));
    }
}
