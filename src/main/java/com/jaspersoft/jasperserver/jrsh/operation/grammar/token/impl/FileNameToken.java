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
package com.jaspersoft.jasperserver.jrsh.operation.grammar.token.impl;

import com.google.common.io.Files;
import com.jaspersoft.jasperserver.jrsh.completion.completer.FileNameCompleter;
import com.jaspersoft.jasperserver.jrsh.operation.grammar.token.AbstractToken;
import jline.console.completer.Completer;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.File;

/**
 * @author Alexander Krasnyanskiy
 * @since 2.0
 */
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FileNameToken extends AbstractToken {

    /**
     * Constructs a new {@link FileNameToken}.
     *
     * @param name       token name
     * @param value      token value
     * @param mandatory  true if mandatory
     * @param tailOfRule is last token in rule
     */
    public FileNameToken(String name, String value, boolean mandatory, boolean tailOfRule) {
        super(name, value, mandatory, tailOfRule);
    }

    @Override
    public Completer getCompleter() {
        //return new FileNameCompleter();
        return new FileNameCompleter();
    }

    @Override
    public boolean match(String input) {
        Files.isFile().apply(new File(input));
        return true;
    }

    @Override
    public String toString() {
        return "<" + name + ">";
    }
}
