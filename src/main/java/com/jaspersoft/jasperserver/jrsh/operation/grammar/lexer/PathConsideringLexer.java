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
package com.jaspersoft.jasperserver.jrsh.operation.grammar.lexer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexander Krasnyanskiy
 * @since 2.0.3
 */
public class PathConsideringLexer implements Lexer {

    @Override
    public List<String> convert(String line) {
        String word = "";
        ArrayList<String> tokens = new ArrayList<>();
        for (String part : line.split("\\s+")) {
            if (part.endsWith("\\")) {
                word = word.concat(part).concat(" ");
            } else {
                if (word.isEmpty()) {
                    tokens.add(part);
                } else {
                    word = word.concat(part);
                    tokens.add(word);
                    word = "";
                }
            }
        }
        return tokens;
    }
}
