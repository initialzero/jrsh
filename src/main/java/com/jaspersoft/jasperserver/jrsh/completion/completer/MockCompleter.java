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
package com.jaspersoft.jasperserver.jrsh.completion.completer;

import jline.console.completer.Completer;

import java.util.List;

/**
 * This completer is involved in chained completion
 * as a part of argument completer, although it's do
 * nothing useful.
 * <p/>
 * We need it only to mark user input and to continue the
 * search of the next proper completer in the completion chain.
 *
 * @author Alexander Krasnyanskiy
 */
public class MockCompleter implements Completer {
    @Override
    public int complete(String s, int i, List<CharSequence> list) {
        return 0;
    }
}
