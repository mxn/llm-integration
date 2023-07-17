package org.novomax.llm.integration.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "org.novomax.llm.integration.ui")
public class UiEntityUrlConfiguration {
    private Map<String, String> searchOneMapping;

    Map<String, String> getSearchOneMapping() {
        return searchOneMapping;
    }

    void setSearchOneMapping(final Map<String, String> searchOneMapping) {
        this.searchOneMapping = searchOneMapping;
    }
}
