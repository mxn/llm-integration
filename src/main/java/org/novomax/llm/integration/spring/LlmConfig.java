package org.novomax.llm.integration.spring;

import jakarta.jms.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.converter.SimpleMessageConverter;

import static org.novomax.llm.integration.spring.Constants.LLM_OPENAI_QUALIFIER;

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

    @Bean
    @Qualifier(LLM_OPENAI_QUALIFIER)
    public JmsTemplate jmsTemplate(ConnectionFactory factory) {
        return new JmsTemplate(factory);
    }

    @Bean
    @Qualifier(LLM_OPENAI_QUALIFIER)
    public MessageConverter messageConverter() {
        return new SimpleMessageConverter();
    }

    public static void main(String[] args) {
        //NOP
    }



}
