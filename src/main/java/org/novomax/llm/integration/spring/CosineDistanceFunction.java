package org.novomax.llm.integration.spring;

public class CosineDistanceFunction {
    public static double cosineDistance(double[] field, double[] array) {
        return 1 - cosineSimilarity(field, array);
    }

    public static double cosineSimilarity(double[] field, double[] array) {
        double dotProduct = 0;
        double fieldNorm = 0;
        double arrayNorm = 0;

        for (int i = 0; i < field.length; i++) {
            dotProduct += field[i] * array[i];
            fieldNorm += field[i] * field[i];
            arrayNorm += array[i] * array[i];
        }

        fieldNorm = Math.sqrt(fieldNorm);
        arrayNorm = Math.sqrt(arrayNorm);

        return dotProduct / (fieldNorm * arrayNorm);
    }

}
