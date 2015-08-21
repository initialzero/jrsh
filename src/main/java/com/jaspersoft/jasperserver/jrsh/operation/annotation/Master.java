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

import com.jaspersoft.jasperserver.jrsh.operation.Operation;
import com.jaspersoft.jasperserver.jrsh.operation.grammar.token.Token;
import com.jaspersoft.jasperserver.jrsh.operation.grammar.token.impl.StringToken;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation used to mark any {@link Operation}'s
 * successor. In that case, using this annotation, you can
 * make successor embedded into JRSH as business command.
 * <p/>
 * This annotation can also be used to set metadata of
 * the {@link Operation}.
 *
 * @author Alexander Krasnyanskiy
 * @since 2.0
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface Master {

    /**
     * Returns operation name.
     *
     * @return operation name which is used for
     *         parsing purposes
     */
    String name()
            default "";

    /**
     * Signals if operation name is a tail.
     *
     * @return true
     *              if operation name is tailed
     */
    boolean tail()
            default false;

    /**
     * Returns operation description.
     *
     * @return operation description
     */
    String description()
            default "Not specified";

    /**
     * Returns operation usage.
     *
     * @return usage info
     */
    String usage()
            default "Not specified";

    /**
     * Defines a token class.
     *
     * @return
     *         a token type
     */
    Class<? extends Token> tokenClass()
            default StringToken.class;

}
