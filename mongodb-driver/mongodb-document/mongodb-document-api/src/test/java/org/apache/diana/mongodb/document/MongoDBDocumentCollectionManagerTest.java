package org.apache.diana.mongodb.document;

import org.apache.diana.api.document.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.diana.mongodb.document.DocumentConfigurationUtils.getConfiguration;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.*;


public class MongoDBDocumentCollectionManagerTest {

    public static final String COLLECTION_NAME = "person";
    private DocumentCollectionManager entityManager;

    @Before
    public void setUp() {
        entityManager = getConfiguration().getDocumentEntityManager("database");
    }

    @Test
    public void shouldSave() {
        DocumentCollectionEntity entity = getEntity();
        DocumentCollectionEntity documentEntity = entityManager.save(entity);
        assertTrue(documentEntity.getDocuments().stream().map(Document::getName).anyMatch(s -> s.equals("_id")));
    }

    @Test
    public void shouldUpdateSave() {
        DocumentCollectionEntity entity = getEntity();
        DocumentCollectionEntity documentEntity = entityManager.save(entity);
        Document newField = Documents.of("newField", "10");
        entity.add(newField);
        DocumentCollectionEntity updated = entityManager.update(entity);
        assertEquals(newField, updated.find("newField").get());
    }

    @Test
    public void shouldRemoveEntity() {
        DocumentCollectionEntity documentEntity = entityManager.save(getEntity());
        DocumentQuery query = DocumentQuery.of(COLLECTION_NAME);
        Optional<Document> id = documentEntity.find("_id");
        query.addCondition(DocumentCondition.eq(id.get()));
        entityManager.delete(query);
        assertTrue(entityManager.find(query).isEmpty());
    }

    @Test
    public void shouldFindDocument() {
        DocumentCollectionEntity entity = entityManager.save(getEntity());
        DocumentQuery query = DocumentQuery.of(COLLECTION_NAME);
        Optional<Document> id = entity.find("_id");
        query.addCondition(DocumentCondition.eq(id.get()));
        List<DocumentCollectionEntity> entities = entityManager.find(query);
        assertFalse(entities.isEmpty());
        assertThat(entities, contains(entity));
    }

    @Test
    public void shouldSaveAsync() {
        DocumentCollectionEntity entity = getEntity();
        entityManager.saveAsync(entity);

    }

    @Test
    public void shouldUpdateAsync() {
        DocumentCollectionEntity entity = getEntity();
        DocumentCollectionEntity documentEntity = entityManager.save(entity);
        Document newField = Documents.of("newField", "10");
        entity.add(newField);
        entityManager.updateAsync(entity);
    }

    @Test
    public void shouldRemoveEntityAsync() {
        DocumentCollectionEntity documentEntity = entityManager.save(getEntity());
        DocumentQuery query = DocumentQuery.of(COLLECTION_NAME);
        Optional<Document> id = documentEntity.find("_id");
        query.addCondition(DocumentCondition.eq(id.get()));
        entityManager.deleteAsync(query);

    }

    private DocumentCollectionEntity getEntity() {
        DocumentCollectionEntity entity = DocumentCollectionEntity.of(COLLECTION_NAME);
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Poliana");
        map.put("city", "Salvador");
        List<Document> documents = Documents.of(map);
        documents.forEach(entity::add);
        return entity;
    }

}