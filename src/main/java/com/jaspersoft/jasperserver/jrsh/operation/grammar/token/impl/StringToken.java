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

import com.jaspersoft.jasperserver.jrsh.completion.completer.StringsCompleter;
import com.jaspersoft.jasperserver.jrsh.operation.grammar.token.AbstractToken;
import jline.console.completer.Completer;
import lombok.EqualsAndHashCode;

/**
 * @author Alexander Krasnyanskiy
 */
@EqualsAndHashCode(callSuper = true)
public class StringToken extends AbstractToken {

    public StringToken(String name, String value, boolean mandatory, boolean tailOfRule) {
        super(name, value, mandatory, tailOfRule);
    }

    @Override
    public boolean isTailOfRule() {
        return tailOfRule;
    }

    @Override
    public Completer getCompleter() {
        return new StringsCompleter(value);
    }

    @Override
    public boolean match(String input) {
        return input.equals(value);
    }

    @Override
    public String toString() {
        return "<" + name + ">";
    }
}
