/*
 * Copyright (C) 2005 - 2015 TIBCO Software Inc. All rights reserved.
 * http://www.jaspersoft.com.
 *
 * Unless you have purchased  a commercial license agreement from Jaspersoft,
 * the following license terms  apply:
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License  as
 * published by the Free Software Foundation, either version 3 of  the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero  General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public  License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.jaspersoft.jasperserver.jrsh.operation;

import com.jaspersoft.jasperserver.jrsh.operation.annotation.Master;
import com.jaspersoft.jasperserver.jrsh.operation.parser.exception.CouldNotCreateOperationInstanceException;
import com.jaspersoft.jasperserver.jrsh.operation.parser.exception.OperationNotFoundException;
import lombok.val;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.jaspersoft.jasperserver.jrsh.operation.PackageScanClassResolver.findOperationClasses;

/**
 * Factory class to create an {@link Operation}.
 *
 * @author Alexander Krasnyanskiy
 * @since 2.0
 */
public abstract class OperationFactory {
    //
    // Available operations container
    //
    private static final Map<String, Class<? extends Operation>> operations =
            new HashMap<>();
    //
    // Base operation impl package
    //
    private static final String basePackage =
            "com.jaspersoft.jasperserver.jrsh.operation.impl";

    static {
        //
        // Initializer
        //
        for (val operationType : findOperationClasses(basePackage)) {
            Master annotation = operationType.getAnnotation(Master.class);
            if (annotation != null) {
                String operationName = annotation.name();
                operations.put(operationName, operationType);
            }
        }
    }

    /**
     * Returns the {@link Operation} corresponding to the operation name.
     *
     * @param operationName name of operation
     * @return operation
     */
    public static Operation createOperationByName(String operationName) {
        val operationType = operations.get(operationName);
        if (operationType == null) {
            throw new OperationNotFoundException();
        }
        return createInstance(operationType);
    }

    /**
     * Creates a set of operation instances by their types.
     *
     * @return a set of operations
     */
    public static Set<Operation> createOperationsByTypes() {
        HashSet<Operation> setOfOperations = new HashSet<>();
        for (val type : operations.values()) {
            setOfOperations.add(createInstance(type));
        }
        return setOfOperations;
    }

    /**
     * Creates an instance of {@link Operation} by operation class.
     *
     * @param operationType the type of operation
     * @return an operation instance
     */
    private static <T extends Operation> T createInstance(
            Class<T> operationType)
    {
        try {
            return operationType.newInstance();
        } catch (Exception err) {
            throw new CouldNotCreateOperationInstanceException(err);
        }
    }
}
