package org.novomax.llm.integration.spring.lucenevdb;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.novomax.llm.integration.SearchResult;

import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LuceneStorageTest {

    private LuceneStorage testee;

    @BeforeEach
    void setUp() throws IOException {
        String pathStr = Files.createTempDirectory("temp").toAbsolutePath().toString();
        this.testee = new LuceneStorage(pathStr);
    }

    @Test
    void test() {
        double[] arr1 = genDoubleArray(1024);
        String text1 = "Hello World!";
        String text2 = "Hallo Welt!";
        String id1 = "123435";
        testee.upcert("entity", id1, text1, arr1);
        testee.upcert("entity", "234556", text2, genDoubleArray(1024));
        arr1[312] *= 0.97;
        SearchResult result = testee.search(arr1, 1).get(0);
        assertEquals(id1, result.entityId());
        assertEquals("entity", result.entityClass());

    }

    private double[] genDoubleArray(final int dim) {
        double[] result = new double[dim];
        IntStream.range(0, dim).forEach(i -> result[i] = Math.random());
        return result;
    }
}