/*
 *
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
 *
 */
package org.eclipse.jnosql.communication.document;


import java.util.function.Function;

/**
 * The manager factory instance creates a manager instance from the database name.
 * It should return a {@link NullPointerException} when the {@link String} parameter is null.
 *
 * @see DocumentManager
 */
public interface DocumentManagerFactory extends Function<String, DocumentManager>, AutoCloseable {

    /**
     * closes a resource
     */
    void close();
}
