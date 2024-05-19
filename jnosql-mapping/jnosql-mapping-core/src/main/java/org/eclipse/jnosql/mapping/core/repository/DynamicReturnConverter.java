/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.core.repository;

import org.eclipse.jnosql.mapping.PreparedStatement;


import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * The converter within the return method at Repository class.
 */
enum DynamicReturnConverter {

    INSTANCE;

    private final RepositoryReturn defaultReturn = new DefaultRepositoryReturn();

    /**
     * Converts the entity from the Method return type.
     *
     * @param dynamic the information about the method and return source
     * @return the conversion result
     * @throws NullPointerException when the dynamic is null
     */
    public Object convert(DynamicReturn<?> dynamic) {

        Method method = dynamic.getMethod();
        Class<?> typeClass = dynamic.typeClass();
        Class<?> returnType = method.getReturnType();

        RepositoryReturn repositoryReturn = ServiceLoader.load(RepositoryReturn.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .filter(RepositoryReturn.class::isInstance)
                .map(RepositoryReturn.class::cast)
                .filter(r -> r.isCompatible(typeClass, returnType))
                .findFirst().orElse(defaultReturn);

        if (dynamic.hasPagination()) {
            return repositoryReturn.convertPageRequest(dynamic);
        } else {
            return repositoryReturn.convert(dynamic);
        }
    }

    /**
     * Reads and execute JNoSQL query from the Method that has the {@link jakarta.data.repository.Query} annotation
     *
     * @return the result from the query annotation
     */
    @SuppressWarnings("unchecked")
    public Object convert(DynamicQueryMethodReturn dynamicQueryMethod) {
        Method method = dynamicQueryMethod.method();
        Object[] args = dynamicQueryMethod.args();
        Function<String, Stream<?>> queryConverter = dynamicQueryMethod.queryConverter();
        Function<String, PreparedStatement> prepareConverter = dynamicQueryMethod.prepareConverter();
        Class<?> typeClass = dynamicQueryMethod.typeClass();

        String value = RepositoryReflectionUtils.INSTANCE.getQuery(method);

        if(dynamicQueryMethod.isCount()){
            Map<String, Object> params = RepositoryReflectionUtils.INSTANCE.getParams(method, args);
            if (params.isEmpty()) {
                return queryConverter.apply(value).count();
            } else {
                var prepare = prepareConverter.apply(value);
                params.forEach(prepare::bind);
                return prepare.count();
            }
        }

        Map<String, Object> params = RepositoryReflectionUtils.INSTANCE.getParams(method, args);
        Stream<?> entities;
        if (params.isEmpty()) {
            entities = queryConverter.apply(value);
        } else {
            PreparedStatement prepare = prepareConverter.apply(value);
            params.forEach(prepare::bind);
            entities = prepare.result();
        }

        Supplier<Stream<?>> streamSupplier = () -> entities;
        Supplier<Optional<?>> singleSupplier = DynamicReturn.toSingleResult(method).apply(streamSupplier);

        DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                .withClassSource(typeClass)
                .withMethodSource(method)
                .withResult(streamSupplier)
                .withSingleResult(singleSupplier)
                .build();

        return convert(dynamicReturn);
    }

}
