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

import com.jaspersoft.jasperserver.jrsh.operation.annotation.Parameter;
import com.jaspersoft.jasperserver.jrsh.operation.annotation.Value;
import com.jaspersoft.jasperserver.jrsh.operation.grammar.token.Token;
import com.jaspersoft.jasperserver.jrsh.operation.parser.exception.CannotFindAccessorException;
import com.jaspersoft.jasperserver.jrsh.operation.parser.exception.NoSuitableSetterException;
import com.jaspersoft.jasperserver.jrsh.operation.parser.exception.OperationParseException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Initializer for setting the operation state.
 *
 * @author Alexander Krasnyanskiy
 * @since 2.0
 */
public abstract class OperationStateInitializer {

    /**
     * Initialize the given operation.
     *
     * @param operation           operation
     * @param operationRuleTokens operation tokens
     * @param userInputTokens     user input
     */
    public static void initialize(Operation operation,
                                  List<Token> operationRuleTokens,
                                  List<String> userInputTokens) {
        Class<? extends Operation> clz = operation.getClass();

        for (Field fld : clz.getDeclaredFields()) {
            Parameter param = fld.getAnnotation(Parameter.class);

            if (param != null) {
                Value[] values = param.values();
                for (Value val : values) {
                    String alias = val.tokenAlias();
                    int idx = getTokenIndex(operationRuleTokens, alias);

                    if (idx >= 0) {
                        //
                        // Setup accessibility
                        //
                        fld.setAccessible(true);
                        //
                        // Set operation parameters via accessors
                        //
                        Method accessor = findAccessor(clz.getMethods(), fld.getName());
                        if (accessor == null) {
                            throw new CannotFindAccessorException(fld.getName());
                        } else try {
                            accessor.invoke(operation, userInputTokens.get(idx));
                        } catch (Exception e) {
                            rethrowException(e);
                        }
                        //
                        // Restore access
                        //
                        fld.setAccessible(false);
                    }
                }
            }
        }
    }

    // ---------------------------------------------------------------------
    //                            Helper methods
    // ---------------------------------------------------------------------

    /**
     * Reflection wraps any custom exceptions, however we can get them
     * through the cause and throw it.
     *
     * @param e exception
     */
    protected static void rethrowException(Exception e) {
        Throwable cause = e.getCause();
        if (OperationParseException.class.isAssignableFrom(cause.getClass())) {
            throw (RuntimeException) cause;
        }
    }

    /**
     * Returns a token index.
     *
     * @param tokens
     * @param tokenAlias
     * @return token index
     */
    protected static int getTokenIndex(List<Token> tokens, String tokenAlias) {
        for (int idx = 0; idx < tokens.size(); idx++) {
            Token token = tokens.get(idx);
            if (tokenAlias.equals(token.getName())) {
                return idx;
            }
        }
        return -1;
    }

    /**
     * Searches for field setter.
     *
     * @param methods   the given methods
     * @param fieldName name of field
     * @return a setter
     */
    protected static Method findAccessor(Method[] methods, String fieldName) {
        for (Method method : methods) {
            String methodName = method.getName();
            if ("set".concat(fieldName.toLowerCase())
                    .equals(methodName.toLowerCase())) {
                return method;
            }
        }
        throw new NoSuitableSetterException();
    }
}
