/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.core.query;

import jakarta.data.exceptions.MappingException;
import jakarta.enterprise.inject.spi.CDI;
import org.eclipse.jnosql.mapping.core.repository.ThrowingSupplier;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.eclipse.jnosql.mapping.core.query.AnnotationOperation.DELETE;
import static org.eclipse.jnosql.mapping.core.query.AnnotationOperation.INSERT;
import static org.eclipse.jnosql.mapping.core.query.AnnotationOperation.SAVE;
import static org.eclipse.jnosql.mapping.core.query.AnnotationOperation.UPDATE;

/**
 * Abstract class that serves as a proxy for repository interfaces.
 * It implements the InvocationHandler interface to handle method invocations.
 *
 * @param <T> The type of the entity managed by the repository.
 * @param <K> The type of the entity's ID.
 */
public abstract class AbstractRepositoryProxy<T, K> implements InvocationHandler {

    /**
     * Retrieves the underlying repository associated with this proxy.
     *
     * @return The underlying repository.
     */
    protected abstract AbstractRepository<T, K> repository();

    /**
     * Retrieves the type of the repository interface.
     *
     * @return The repository interface type.
     */
    protected abstract Class<?> repositoryType();

    /**
     * Retrieves the metadata information about the entity managed by this repository.
     *
     * @return The entity metadata information.
     */
    protected abstract EntityMetadata entityMetadata();

    /**
     * Executes a query based on the method and parameters.
     *
     * @param instance The instance on which the method was invoked.
     * @param method   The method being invoked.
     * @param params   The parameters of the method.
     * @return The result of the query execution.
     */
    protected abstract Object executeQuery(Object instance, Method method, Object[] params);

    /**
     * Executes a delete operation based on the method and parameters.
     *
     * @param instance The instance on which the method was invoked.
     * @param method   The method being invoked.
     * @param params   The parameters of the method.
     * @return The result of the delete operation.
     */
    protected abstract Object executeDeleteByAll(Object instance, Method method, Object[] params);

    /**
     * Executes a find-all operation based on the method and parameters.
     *
     * @param instance The instance on which the method was invoked.
     * @param method   The method being invoked.
     * @param params   The parameters of the method.
     * @return The result of the find-all operation.
     */
    protected abstract Object executeFindAll(Object instance, Method method, Object[] params);

    /**
     * Executes an existence check query based on the method and parameters.
     *
     * @param instance The instance on which the method was invoked.
     * @param method   The method being invoked.
     * @param params   The parameters of the method.
     * @return The result of the existence check query.
     */
    protected abstract Object executeExistByQuery(Object instance, Method method, Object[] params);

    /**
     * Executes a count query based on the method and parameters.
     *
     * @param instance The instance on which the method was invoked.
     * @param method   The method being invoked.
     * @param params   The parameters of the method.
     * @return The result of the count query.
     */
    protected abstract Object executeCountByQuery(Object instance, Method method, Object[] params);

    /**
     * Executes a find-by query based on the method and parameters.
     *
     * @param instance The instance on which the method was invoked.
     * @param method   The method being invoked.
     * @param params   The parameters of the method.
     * @return The result of the find-by query.
     */
    protected abstract Object executeFindByQuery(Object instance, Method method, Object[] params);

    @Override
    public Object invoke(Object instance, Method method, Object[] params) throws Throwable {

        RepositoryType type = RepositoryType.of(method, repositoryType());

        switch (type) {
            case DEFAULT -> {
                return unwrapInvocationTargetException(() -> method.invoke(repository(), params));
            }
            case FIND_BY -> {
                return executeFindByQuery(instance, method, params);
            }
            case COUNT_BY -> {
                return executeCountByQuery(instance, method, params);
            }
            case EXISTS_BY -> {
                return executeExistByQuery(instance, method, params);
            }
            case FIND_ALL -> {
                return executeFindAll(instance, method, params);
            }
            case DELETE_BY -> {
                return executeDeleteByAll(instance, method, params);
            }
            case OBJECT_METHOD -> {
                return unwrapInvocationTargetException(() -> method.invoke(this, params));
            }
            case DEFAULT_METHOD -> {
                return unwrapInvocationTargetException(() -> InvocationHandler.invokeDefault(instance, method, params));
            }
            case ORDER_BY ->
                    throw new MappingException("Eclipse JNoSQL has not support for method that has OrderBy annotation");
            case QUERY -> {
                return executeQuery(instance, method, params);
            }
            case CUSTOM_REPOSITORY -> {
                Object customRepository = CDI.current().select(method.getDeclaringClass()).get();
                return unwrapInvocationTargetException(() -> method.invoke(customRepository, params));
            }
            case SAVE -> {
                return unwrapInvocationTargetException(() -> SAVE.invoke(new AnnotationOperation.Operation(method, params, repository())));
            }
            case INSERT -> {
                return unwrapInvocationTargetException(() -> INSERT.invoke(new AnnotationOperation.Operation(method, params, repository())));
            }
            case DELETE -> {
                return unwrapInvocationTargetException(() -> DELETE.invoke(new AnnotationOperation.Operation(method, params, repository())));
            }
            case UPDATE -> {
                return unwrapInvocationTargetException(() -> UPDATE.invoke(new AnnotationOperation.Operation(method, params, repository())));
            }
            default -> {
                return Void.class;
            }
        }
    }

    /**
     * Unwraps the InvocationTargetException and throws the original cause.
     *
     * @param supplier The supplier that may throw an InvocationTargetException.
     * @return The result of the supplier.
     * @throws Throwable If the original cause of the InvocationTargetException is not null.
     */
    protected Object unwrapInvocationTargetException(ThrowingSupplier<Object> supplier) throws Throwable {
        try {
            return supplier.get();
        } catch (InvocationTargetException ex) {
            throw ex.getCause();
        }
    }
}