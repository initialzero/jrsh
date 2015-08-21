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
package com.jaspersoft.jasperserver.jrsh.evaluation.strategy.impl;

import com.jaspersoft.jasperserver.jaxrs.client.core.Session;
import com.jaspersoft.jasperserver.jrsh.common.ConsoleBuilder;
import com.jaspersoft.jasperserver.jrsh.common.SessionFactory;
import com.jaspersoft.jasperserver.jrsh.completion.CompleterFactory;
import com.jaspersoft.jasperserver.jrsh.completion.CustomCompletionHandler;
import com.jaspersoft.jasperserver.jrsh.evaluation.strategy.AbstractEvaluationStrategy;
import com.jaspersoft.jasperserver.jrsh.operation.Operation;
import com.jaspersoft.jasperserver.jrsh.operation.annotation.Master;
import com.jaspersoft.jasperserver.jrsh.operation.impl.LoginOperation;
import com.jaspersoft.jasperserver.jrsh.operation.parser.exception.OperationParseException;
import com.jaspersoft.jasperserver.jrsh.operation.result.OperationResult;
import jline.console.ConsoleReader;
import jline.console.UserInterruptException;
import jline.console.completer.Completer;

import java.io.IOException;
import java.util.List;

import static com.jaspersoft.jasperserver.jrsh.common.SessionFactory.getSharedSession;
import static com.jaspersoft.jasperserver.jrsh.operation.result.ResultCode.FAILED;
import static com.jaspersoft.jasperserver.jrsh.operation.result.ResultCode.INTERRUPTED;

/**
 * @author Alexander Krasnyanskiy
 */
public class ShellEvaluationStrategy extends AbstractEvaluationStrategy {

    private ConsoleReader console;

    public ShellEvaluationStrategy() {
        this.console = new ConsoleBuilder()
                .withPrompt("$> ")
                .withHandler(new CustomCompletionHandler())
                .withInterruptHandling()
                .withCompleter(getCompleter())
                .build();
    }

    @Override
    public OperationResult eval(List<String> source) {
        String line = source.get(0);
        Operation operation = null;
        OperationResult result = null;
        while (true) {
            try {
                Session session = getSharedSession();
                if (line == null) {
                    line = console.readLine();
                }
                if (line.isEmpty()) {
                    print("");
                } else {
                    OperationResult temp = result;
                    operation = parser.parseOperation(line);
                    result = operation.execute(session);
                    result.setPrevious(temp);
                    print(result.getResultMessage());

                    if (result.getResultCode() == FAILED) {
                        if (operation instanceof LoginOperation) {
                            return new OperationResult(
                                    result.getResultMessage(),
                                    FAILED,
                                    operation,
                                    null);
                        } else {
                            // fixme {should be delegated to the Help operation (!)}
                            Master master = operation.getClass().getAnnotation(Master.class);
                            String usage = master.usage();
                            print("usage: " + usage);
                        }
                    }
                }
                line = null;
            } catch (UserInterruptException unimportant) {
                logout();
                return new OperationResult(
                        "Interrupted by user",
                        INTERRUPTED,
                        operation,
                        null);
            } catch (OperationParseException err) {
                try {
                    print(err.getMessage());
                } finally {
                    line = null;
                }
            } catch (IOException ignored) {
            } finally {
                operation = null;
            }
        }
    }

    // ---------------------------------------------------------------------
    //                           Helper methods
    // ---------------------------------------------------------------------

    protected void print(String message) {
        try {
            console.println(message);
            console.flush();
        } catch (IOException ignored) {
        }
    }

    protected Completer getCompleter() {
        return CompleterFactory.create();
    }

    protected void logout() {
        try {
            SessionFactory.getSharedSession().logout();
        } catch (Exception ignored) {
        }
    }

    public void setConsole(ConsoleReader console) {
        this.console = console;
    }
}
