package org.novomax.llm.integration.spring.openai;

// Welcome8.java

public class OpenAiEmbedResponse {
    private String object;
    private Datum[] data;
    private String model;
    private Usage usage;

    public String getObject() {
        return object;
    }

    public void setObject(String value) {
        this.object = value;
    }

    public Datum[] getData() {
        return data;
    }

    public void setData(Datum[] value) {
        this.data = value;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String value) {
        this.model = value;
    }

    public Usage getUsage() {
        return usage;
    }

    public void setUsage(Usage value) {
        this.usage = value;
    }


    public static class Datum {
        private String object;
        private double[] embedding;
        private long index;

        public String getObject() {
            return object;
        }

        public void setObject(String value) {
            this.object = value;
        }

        public double[] getEmbedding() {
            return embedding;
        }

        public void setEmbedding(double[] value) {
            this.embedding = value;
        }

        public long getIndex() {
            return index;
        }

        public void setIndex(long value) {
            this.index = value;
        }
    }

    static class Usage {
        private long promptTokens;
        private long totalTokens;

        public long getPromptTokens() {
            return promptTokens;
        }

        public void setPromptTokens(long value) {
            this.promptTokens = value;
        }

        public long getTotalTokens() {
            return totalTokens;
        }

        public void setTotalTokens(long value) {
            this.totalTokens = value;
        }
    }
}
