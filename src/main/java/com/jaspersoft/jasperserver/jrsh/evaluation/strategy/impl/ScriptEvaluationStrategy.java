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
import com.jaspersoft.jasperserver.jrsh.evaluation.strategy.AbstractEvaluationStrategy;
import com.jaspersoft.jasperserver.jrsh.operation.Operation;
import com.jaspersoft.jasperserver.jrsh.operation.result.OperationResult;
import jline.console.ConsoleReader;

import java.io.IOException;
import java.util.List;

import static com.jaspersoft.jasperserver.jrsh.common.SessionFactory.getSharedSession;
import static com.jaspersoft.jasperserver.jrsh.operation.result.ResultCode.FAILED;

/**
 * @author Alexander Krasnyanskiy
 */
public class ScriptEvaluationStrategy extends AbstractEvaluationStrategy {

    public static final String ERROR_MSG = "error in line: %s (%s)%n";
    private int lineCounter = 1;
    private ConsoleReader console;

    public ScriptEvaluationStrategy() {
        console = new ConsoleBuilder().build();
    }

    @Override
    public OperationResult eval(List<String> source) {
        OperationResult result = null;
        Operation operation = null;
        try {
            for (String line : source) {
                if (!line.startsWith("#") && !line.isEmpty()) {
                    OperationResult temp = result;

                    Session session = getSharedSession();
                    operation = parser.parseOperation(line);
                    result = operation.execute(session);

                    console.println(" → " + result.getResultMessage());
                    console.flush();
                    result.setPrevious(temp);
                }
                lineCounter++;
            }
        } catch (Exception e) {
            String message = String.format(ERROR_MSG, lineCounter, e.getMessage());
            try {
                console.print(message);
                console.flush();
            } catch (IOException ignored) {
            }
            result = new OperationResult(message, FAILED, operation, result);
        }
        return result;
    }
}
