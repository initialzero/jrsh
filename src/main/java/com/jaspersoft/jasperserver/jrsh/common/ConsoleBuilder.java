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

import com.jaspersoft.jasperserver.jrsh.common.exception.CouldNotCreateJLineConsoleException;
import jline.console.ConsoleReader;
import jline.console.completer.Completer;
import jline.console.completer.CompletionHandler;

import java.io.IOException;

/**
 * Builder for {@link ConsoleReader} instances with convenient fluent API.
 *
 * @author Alexander Krasnyanskiy
 * @since 2.0
 */
public class ConsoleBuilder {
    private ConsoleReader console;

    public ConsoleBuilder() {
        try {
            this.console = new ConsoleReader();
        } catch (IOException e) {
            throw new CouldNotCreateJLineConsoleException();
        }
    }

    public ConsoleBuilder withPrompt(String prompt) {
        console.setPrompt(prompt);
        return this;
    }

    public ConsoleBuilder withCompleter(Completer completer) {
        console.addCompleter(completer);
        return this;
    }

    public ConsoleBuilder withHandler(CompletionHandler handler) {
        console.setCompletionHandler(handler);
        return this;
    }

    public ConsoleBuilder withInterruptHandling() {
        console.setHandleUserInterrupt(true);
        return this;
    }

    public ConsoleReader build() {
        return console;
    }
}
