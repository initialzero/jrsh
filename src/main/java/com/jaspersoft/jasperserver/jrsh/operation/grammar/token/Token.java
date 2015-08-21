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
package com.jaspersoft.jasperserver.jrsh.operation.grammar.token;

import com.jaspersoft.jasperserver.jrsh.operation.Operation;
import jline.console.completer.Completer;

/**
 * A token is a structure representing a lexeme that explicitly
 * indicates its categorization for the purpose of parsing.
 * For more info please visit wiki page
 * <a href>https://en.wikipedia.org/wiki/Lexical_analysis#Token</a>
 *
 * @author Alexander Krasnyanskiy
 * @since 2.0
 */
public interface Token {

    /**
     * Returns representative name of alias from
     * {@link Operation} metadata.
     *
     * @return operation alias
     */
    String getName();

    /**
     *
     */
    String getValue();

    /**
     *
     */
    boolean isMandatory();

    /**
     *
     */
    boolean isTailOfRule();

    /**
     * Returns a completer of this particular token.
     *
     * @return completer
     */
    Completer getCompleter();

    /**
     *
     */
    boolean match(String input);

}
