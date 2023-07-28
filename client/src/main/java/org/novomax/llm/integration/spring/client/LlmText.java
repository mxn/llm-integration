package org.novomax.llm.integration.spring.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Not {@link java.lang.annotation.Inherited} while {@link jakarta.persistence.EntityListeners}
 * Annotation is not.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LlmText {
}
