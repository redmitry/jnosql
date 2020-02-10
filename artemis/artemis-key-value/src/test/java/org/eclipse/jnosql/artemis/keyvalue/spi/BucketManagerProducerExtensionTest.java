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
package org.eclipse.jnosql.artemis.keyvalue.spi;

import jakarta.nosql.mapping.Database;
import jakarta.nosql.mapping.DatabaseType;
import jakarta.nosql.mapping.keyvalue.KeyValueTemplate;
import org.eclipse.jnosql.artemis.UserRepository;
import org.eclipse.jnosql.artemis.model.Person;
import org.eclipse.jnosql.artemis.model.User;
import org.eclipse.jnosql.artemis.test.CDIExtension;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;

@CDIExtension
public class BucketManagerProducerExtensionTest {

    @Inject
    private KeyValueTemplate repository;

    @Inject
    @Database(value = DatabaseType.KEY_VALUE, provider = "keyvalueMock")
    private KeyValueTemplate repositoryMock;

    @Inject
    private UserRepository userRepository;

    @Inject
    @Database(value = DatabaseType.KEY_VALUE)
    private UserRepository userRepositoryDefault;

    @Inject
    @Database(value = DatabaseType.KEY_VALUE, provider = "keyvalueMock")
    private UserRepository userRepositoryMock;

    @Test
    public void shouldUseMock() {
        Person person = repository.get(10L, Person.class).get();

        Person personMock = repositoryMock.get(10L, Person.class).get();

        assertEquals("Default", person.getName());
        assertEquals("keyvalueMock", personMock.getName());

    }


    @Test
    public void shouldUseRepository() {
        User user = userRepository.findById("user").get();
        User userDefault = userRepositoryDefault.findById("user").get();
        User userMock = userRepositoryMock.findById("user").get();
        assertEquals("Default", user.getName());
        assertEquals("Default", userDefault.getName());
        assertEquals("keyvalueMock", userMock.getName());
    }

}