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
package com.jaspersoft.jasperserver.jrsh.operation.annotation;

import com.jaspersoft.jasperserver.jrsh.operation.grammar.token.Token;
import com.jaspersoft.jasperserver.jrsh.operation.grammar.token.impl.AnyStringToken;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Alexander Krasnyanskiy
 * @since 2.0
 */
@Target(FIELD)
@Retention(RUNTIME)
@Documented
public @interface Value {

    /**
     * Used for token binding.
     *
     * @return a token alias
     */
    String tokenAlias()
            default "";

    /**
     * Returns a value of token defined in the operation.
     *
     * @return token value
     */
    String tokenValue()
            default "";

    /**
     * Define if value is a tail in the rule.
     *
     * @return true if tail
     */
    boolean tail()
            default false;

    /**
     * Define a class of corresponding token
     *
     * @return type
     */
    Class<? extends Token> tokenClass()
            default AnyStringToken.class;

}
