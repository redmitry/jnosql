/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 */
package org.eclipse.jnosql.mapping.semistructured;

import jakarta.nosql.PreparedStatement;
import jakarta.nosql.Template;

import java.util.Optional;
import java.util.stream.Stream;



/**
 * Interface representing a template for accessing a semi-structured database.
 * It extends the {@link Template} interface.
 * This interface provides methods for executing queries, counting elements, and preparing statements.
 */
public interface SemistructuredTemplate extends Template {



    /**
     * Returns the number of elements in a specified column family.
     *
     * @param entity the name of the entity (column family)
     * @return the number of elements
     * @throws NullPointerException          if the column family name is null
     * @throws UnsupportedOperationException if the database does not support this operation
     */
    long count(String entity);

    /**
     * Returns the number of elements of a specified entity type.
     *
     * @param <T>  the entity type
     * @param type the class representing the entity type (column family)
     * @return the number of elements
     * @throws NullPointerException          if the entity type is null
     * @throws UnsupportedOperationException if the database does not support this operation
     */
    <T> long count(Class<T> type);

    /**
     * Executes a native query on the database and returns the result as a {@link Stream}.
     *
     * <p>
     * The query syntax is specific to each provider and may vary between implementations and NoSQL providers.
     * </p>
     *
     * @param query the native query
     * @param <T>   the type of the entities in the result stream
     * @return the result as a {@link Stream}
     * @throws NullPointerException          if the query is null
     * @throws UnsupportedOperationException if the provider does not support query by text
     */
    <T> Stream<T> query(String query);

    /**
     * Executes a query on the database and returns the result as a single unique result wrapped in an {@link Optional}.
     *
     * <p>
     * The query syntax is specific to each provider and may vary between implementations and NoSQL providers.
     * </p>
     *
     * @param query the query
     * @param <T>   the type of the entity in the result
     * @return the result as an {@link Optional}
     * @throws NullPointerException          if the query is null
     * @throws UnsupportedOperationException if the provider does not support query by text
     */
    <T> Optional<T> singleResult(String query);

    /**
     * Creates a {@link PreparedStatement} from the specified query.
     *
     * <p>
     * The query syntax is specific to each provider and may vary between implementations and NoSQL providers.
     * </p>
     *
     * @param query the query
     * @return a {@link PreparedStatement} instance
     * @throws NullPointerException          if the query is null
     * @throws UnsupportedOperationException if the provider does not support query by text
     */
    PreparedStatement prepare(String query);
}
