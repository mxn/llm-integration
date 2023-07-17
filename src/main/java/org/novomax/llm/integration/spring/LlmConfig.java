package org.novomax.llm.integration.spring;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;

@EnableJms
@ComponentScan
@EnableAutoConfiguration
@EntityScan
@Configuration
public class LlmConfig {
    private final String destinationName;

    private final int embedVectorDim;

    public LlmConfig(
            @Value("${org.novomax.llm.integration.destinationName:llmProcessing}")
            String destinationName,
            @Value("${org.novomax.llm.integration.embedVectorDim:1536}")
            int embedVectorDim
    ) {
        this.destinationName = destinationName;
        this.embedVectorDim = embedVectorDim;
    }

    @Bean
    public String getDestinationName() {
        return destinationName;
    }

    public int getEmbedVectorDim() {
        return embedVectorDim;
    }

    public static void main(String[] args) {
        //NOP
    }

}
