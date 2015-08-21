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
package com.jaspersoft.jasperserver.jrsh.common;

import com.jaspersoft.jasperserver.jrsh.common.exception.CouldNotOpenScriptFileException;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.google.common.base.Joiner.on;
import static com.jaspersoft.jasperserver.jrsh.operation.grammar.token.TokenPreconditions.isConnectionString;
import static com.jaspersoft.jasperserver.jrsh.operation.grammar.token.TokenPreconditions.isScriptFileName;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.copyOfRange;
import static java.util.Collections.singletonList;
import static org.apache.commons.io.FileUtils.readLines;

/**
 * Convenience class for converting arguments.
 *
 * @author Alexander Krasnyanskiy
 */
public abstract class ArgumentUtil {

    public static List<String> convertToScript(String[] arguments) {
        List<String> script;

        switch (arguments.length) {
            case 0: {
                script = singletonList("help");
                break;
            }

            case 1: {
                String line = arguments[0];
                script = isConnectionString(line)
                        ? singletonList(format("login %s", line))
                        : singletonList(line);
                break;
            }

            default: {
                if ("--script".equals(arguments[0]) && isScriptFileName(arguments[1])) {
                    try {
                        script = readLines(new File(arguments[1]));
                    } catch (IOException ignored) {
                        throw new CouldNotOpenScriptFileException(arguments[1]);
                    }
                }
                else if (isConnectionString(arguments[0])) {
                    String login = "login " + arguments[0];
                    String nextLine = on(" ").join(copyOfRange(arguments, 1, arguments.length));
                    script = asList(login, nextLine);
                }
                else {
                    String line = on(" ").join(arguments);
                    script = singletonList(line);
                }
            }
        }
        return script;
    }
}
