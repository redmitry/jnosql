/*
 *  Copyright (c) 2017 Otávio Santana and others
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.mapping.reflection;

import jakarta.nosql.mapping.MappingException;
import jakarta.nosql.tck.entities.Actor;
import jakarta.nosql.tck.entities.Director;
import jakarta.nosql.tck.entities.Machine;
import jakarta.nosql.tck.entities.NoConstructorEntity;
import jakarta.nosql.tck.entities.Person;
import jakarta.nosql.tck.entities.User;
import jakarta.nosql.tck.entities.Worker;
import jakarta.nosql.tck.entities.inheritance.EmailNotification;
import jakarta.nosql.tck.entities.inheritance.Notification;
import jakarta.nosql.tck.entities.inheritance.Project;
import jakarta.nosql.tck.entities.inheritance.SmallProject;
import jakarta.nosql.tck.entities.inheritance.SocialMediaNotification;
import jakarta.nosql.tck.test.CDIExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static jakarta.nosql.mapping.DiscriminatorColumn.DEFAULT_DISCRIMINATOR_COLUMN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@CDIExtension
public class ClassConverterTest {

    @Inject
    private ClassConverter classConverter;

    @Test
    public void shouldCreateClassMapping() {
        ClassMapping classMapping = classConverter.create(Person.class);

        assertEquals("Person", classMapping.getName());
        assertEquals(Person.class, classMapping.getClassInstance());
        assertEquals(4, classMapping.getFields().size());
        assertThat(classMapping.getFieldsName(), containsInAnyOrder("_id", "name", "age", "phones"));

    }

    @Test
    public void shouldCreateClassMapping2() {
        ClassMapping classMapping = classConverter.create(Actor.class);

        assertEquals("Actor", classMapping.getName());
        assertEquals(Actor.class, classMapping.getClassInstance());
        assertEquals(6, classMapping.getFields().size());
        assertThat(classMapping.getFieldsName(), containsInAnyOrder("_id", "name", "age", "phones", "movieCharacter", "movieRating"));

    }

    @Test
    public void shouldCreateClassMappingWithEmbeddedClass() {
        ClassMapping classMapping = classConverter.create(Director.class);
        assertEquals("Director", classMapping.getName());
        assertEquals(Director.class, classMapping.getClassInstance());
        assertEquals(5, classMapping.getFields().size());
        assertThat(classMapping.getFieldsName(), containsInAnyOrder("_id", "name", "age", "phones", "movie"));

    }

    @Test
    public void shouldReturnFalseWhenThereIsNotKey() {
        ClassMapping classMapping = classConverter.create(Worker.class);
        boolean allMatch = classMapping.getFields().stream().noneMatch(FieldMapping::isId);
        assertTrue(allMatch);
    }


    @Test
    public void shouldReturnTrueWhenThereIsKey() {
        ClassMapping classMapping = classConverter.create(User.class);
        List<FieldMapping> fields = classMapping.getFields();

        Predicate<FieldMapping> hasKeyAnnotation = FieldMapping::isId;
        assertTrue(fields.stream().anyMatch(hasKeyAnnotation));
        FieldMapping fieldMapping = fields.stream().filter(hasKeyAnnotation).findFirst().get();
        assertEquals("_id", fieldMapping.getName());
        assertEquals(FieldType.DEFAULT, fieldMapping.getType());

    }

    @Test
    public void shouldReturnErrorWhenThereIsNotConstructor() {
        Assertions.assertThrows(ConstructorException.class, () -> classConverter.create(NoConstructorEntity.class));
    }

    @Test
    public void shouldReturnWhenIsDefaultConstructor() {
        ClassMapping classMapping = classConverter.create(Machine.class);
        List<FieldMapping> fields = classMapping.getFields();
        assertEquals(1, fields.size());
    }

    @Test
    public void shouldReturnEmptyInheritance() {
        ClassMapping classMapping = classConverter.create(Person.class);
        Optional<InheritanceClassMapping> inheritance = classMapping.getInheritance();
        Assertions.assertTrue(inheritance.isEmpty());
    }

    @Test
    public void shouldInheritance() {
        ClassMapping entity = classConverter.create(SmallProject.class);
        Assertions.assertEquals(2, entity.getFields().size());
        Assertions.assertEquals(SmallProject.class, entity.getClassInstance());

        InheritanceClassMapping inheritance = entity.getInheritance()
                .orElseThrow(MappingException::new);

        assertEquals("size", inheritance.getDiscriminatorColumn());
        assertEquals("Small", inheritance.getDiscriminatorValue());
        assertEquals(Project.class, inheritance.getParent());
    }

    @Test
    public void shouldInheritanceNoDiscriminatorValue() {
        ClassMapping entity = classConverter.create(SocialMediaNotification.class);
        Assertions.assertEquals(4, entity.getFields().size());
        Assertions.assertEquals(SocialMediaNotification.class, entity.getClassInstance());

        InheritanceClassMapping inheritance = entity.getInheritance()
                .orElseThrow(MappingException::new);

        assertEquals(DEFAULT_DISCRIMINATOR_COLUMN, inheritance.getDiscriminatorColumn());
        assertEquals("SocialMediaNotification", inheritance.getDiscriminatorValue());
        assertEquals(Notification.class, inheritance.getParent());
    }

    @Test
    public void shouldInheritanceNoDiscriminatorColumn() {
        ClassMapping entity = classConverter.create(EmailNotification.class);
        Assertions.assertEquals(4, entity.getFields().size());
        Assertions.assertEquals(EmailNotification.class, entity.getClassInstance());

        InheritanceClassMapping inheritance = entity.getInheritance()
                .orElseThrow(MappingException::new);

        assertEquals(DEFAULT_DISCRIMINATOR_COLUMN, inheritance.getDiscriminatorColumn());
        assertEquals("Email", inheritance.getDiscriminatorValue());
        assertEquals(Notification.class, inheritance.getParent());
    }

}