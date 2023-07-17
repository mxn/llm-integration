package org.novomax.llm.integration.spring.lucenevdb;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.KnnFloatVectorField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.MMapDirectory;
import org.novomax.llm.integration.SearchResult;
import org.novomax.llm.integration.VectorStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Math.min;

@Service
public class LuceneStorage implements VectorStorage {
    public static final String ENTITY_CLASS_FIELD_NAME = "entityClass";
    public static final String ENTITY_ID_FIELD_NAME = "entityId";
    private static final String EMBEDDING_FIELD_NAME = "embedding";
    private static final String MD5_TEXT_HASH_FIELD_NAME = "md5TextHash";
    private static Logger LOGGER = LoggerFactory.getLogger(LuceneStorage.class);
    private final Path luceneIndexDirectory;
    private final AtomicReference<IndexWriter> indexWriter = new AtomicReference<>();

    private final AtomicReference<SearcherManager> searchManager = new AtomicReference<>();


    public LuceneStorage(
            @Value("${org.novomax.llm.integration.spring.lucenevdb.uri:lucene_storage}")
            String luceneIndexDirectory) {
        this.luceneIndexDirectory = Path.of(luceneIndexDirectory);
    }

    private static void deleteWithIndexWriter(IndexWriter indexWriter, String entityClass, String entityId) throws IOException {
        indexWriter.deleteDocuments(makeEntityClassIdBuilder(entityClass, entityId) //
                .build());
    }

    private static BooleanQuery.Builder makeEntityClassIdBuilder(String entityClass, String entityId) {
        return new BooleanQuery.Builder() //
                .add(new BooleanClause(new TermQuery(new Term(ENTITY_CLASS_FIELD_NAME, entityClass)), BooleanClause.Occur.MUST)) //
                .add(new BooleanClause(new TermQuery(new Term(ENTITY_ID_FIELD_NAME, entityId)), BooleanClause.Occur.MUST));
    }

    private static float[] castToFloatArray(double[] array) {
        float[] result = new float[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = (float) array[i];
        }
        return result;
    }

    @Override
    public List<SearchResult> search(double[] vector, int limit) {
        try {
            KnnFloatVectorQuery knnFloatVectorQuery = new KnnFloatVectorQuery(EMBEDDING_FIELD_NAME, castToFloatArray(vector), limit);
            IndexSearcher searcher = getIndexSearcher();
            TopDocs topDocs = searcher.search(knnFloatVectorQuery, limit);
            List<SearchResult> result = new ArrayList<>();
            for (int i = 0; i < min(topDocs.totalHits.value, limit); i++) {
                Document document = searcher.getIndexReader().storedFields().document(topDocs.scoreDocs[i].doc);
                result.add(new SearchResultImpl(document.get(ENTITY_CLASS_FIELD_NAME), document.get(ENTITY_ID_FIELD_NAME), topDocs.scoreDocs[i].score));
            }
            return result;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void upcert(String entityClass, String entityId, String text, double[] embedding) {
        try {
            IndexWriter indexWriter = getIndexWriter();
            deleteWithIndexWriter(indexWriter, entityClass, entityId);
            indexWriter.addDocument(createDocument(entityClass, entityId, getMd5Hash(text), castToFloatArray(embedding)));
            indexWriter.commit();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void delete(String entityClass, String entityId) {
        try {
            IndexWriter indexWriter = getIndexWriter();
            deleteWithIndexWriter(indexWriter, entityClass, entityId);
            indexWriter.commit();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private String getMd5Hash(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(text.getBytes());
            return new String(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private IndexWriter getIndexWriter() throws IOException {
        if (indexWriter.get() == null || !indexWriter.get().isOpen()) {
            indexWriter.set(new IndexWriter(new MMapDirectory(luceneIndexDirectory),
                    new IndexWriterConfig()));
        }
        return indexWriter.get();
    }

    @Override
    public boolean shouldUpdateEmbedding(String entityClass, String entityId, String text) {
        try {
            return getIndexSearcher().search(makeEntityClassIdBuilder(entityClass, entityId) //
                    .add(new BooleanClause(new TermQuery(new Term(MD5_TEXT_HASH_FIELD_NAME, getMd5Hash(text))), BooleanClause.Occur.MUST)) //
                    .build(), 1).scoreDocs.length == 0;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private IndexSearcher getIndexSearcher() {
        try {
            if (searchManager.get() == null) {
                searchManager.set(new SearcherManager(getIndexWriter(), null));
            }
            searchManager.get().maybeRefresh();
            return searchManager.get().acquire();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Document createDocument(String entityType, String entityId, String text, float[] embedding) {
        Document document = new Document();
        document.add(new StringField(ENTITY_CLASS_FIELD_NAME, entityType, Field.Store.YES));
        document.add(new StringField(ENTITY_ID_FIELD_NAME, entityId, Field.Store.YES));
        document.add(new StringField(MD5_TEXT_HASH_FIELD_NAME, getMd5Hash(text), Field.Store.YES));
        document.add(new KnnFloatVectorField(EMBEDDING_FIELD_NAME, embedding));
        return document;
    }

    @PreDestroy
    public void preDestroy() {
        if (indexWriter.get() != null && indexWriter.get().isOpen()) {
            try {
                indexWriter.get().close();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    record SearchResultImpl(String entityClass, String entityId, float score) implements SearchResult {
    }
}
