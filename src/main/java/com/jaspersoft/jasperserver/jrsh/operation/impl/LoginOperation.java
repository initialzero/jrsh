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
package com.jaspersoft.jasperserver.jrsh.operation.impl;

import com.jaspersoft.jasperserver.jaxrs.client.core.Session;
import com.jaspersoft.jasperserver.jrsh.operation.Operation;
import com.jaspersoft.jasperserver.jrsh.operation.annotation.Master;
import com.jaspersoft.jasperserver.jrsh.operation.annotation.Parameter;
import com.jaspersoft.jasperserver.jrsh.operation.annotation.Value;
import com.jaspersoft.jasperserver.jrsh.operation.grammar.token.TokenPreconditions;
import com.jaspersoft.jasperserver.jrsh.operation.parser.exception.WrongConnectionStringFormatException;
import com.jaspersoft.jasperserver.jrsh.operation.result.OperationResult;
import lombok.Data;

import static com.jaspersoft.jasperserver.jrsh.common.SessionFactory.createSharedSession;
import static com.jaspersoft.jasperserver.jrsh.operation.result.ResultCode.FAILED;
import static com.jaspersoft.jasperserver.jrsh.operation.result.ResultCode.SUCCESS;
import static java.lang.String.format;

/**
 * @author Alexander Krasnyanskiy
 * @since 2.0
 */
@Data
@Master(name = "login",
        usage = "login [username]|[organization]%[password]@[url]",
        description = "<login> is used to login into JRS")
public class LoginOperation implements Operation {

    private static final String OK_MSG =
            "You have logged in";
    private static final String FORMATTED_OK_MSG =
            "You have logged in as %s";
    private static final String FAILURE_MSG =
            "Login failed";
    private static final String FORMATTED_FAILURE_MSG =
            "Login failed (%s)";

    private String server;
    private String username;
    private String password;
    private String organization;

    @Parameter(
            mandatory = true, dependsOn = "login",
            values = @Value(tokenAlias = "CS", tail = true)
    )
    private String connectionString;

    @Override
    public OperationResult execute(Session ignored) {
        OperationResult result;
        try {
            createSharedSession(server,
                                username,
                                password,
                                organization);

            result = new OperationResult(
                    format(FORMATTED_OK_MSG, username), SUCCESS,
                    this, null
            );
        } catch (Exception err) {
            result = new OperationResult(
                    format(FORMATTED_FAILURE_MSG, err.getMessage()),
                    FAILED, this, null
            );
        }
        return result;
    }

    public void setConnectionString(String connectionString) {
        if (!TokenPreconditions.isConnectionString(connectionString)) {
            throw new WrongConnectionStringFormatException();
        }
        this.connectionString = connectionString;
        String[] tokens = connectionString.split("[@]");
        switch (tokens.length) {
            case 2:
                server = tokens[1].trim();
                tokens = tokens[0].split("[%]");
                switch (tokens.length) {
                    case 2:
                        password = tokens[1].trim();
                        tokens = tokens[0].split("[|]");
                        switch (tokens.length) {
                            case 2:
                                username = tokens[0].trim();
                                organization = tokens[1].trim();
                                break;
                            case 1:
                                username = tokens[0].trim();
                                break;
                        }
                        break;
                    case 1:
                        tokens = tokens[0].split("[|]");
                        switch (tokens.length) {
                            case 2:
                                username = tokens[0].trim();
                                organization = tokens[1].trim();
                                break;
                            default:
                                username = tokens[0].trim();
                                break;
                        }
                        break;
                }
                break;
        }
    }

    public String getConnectionString() {
        return connectionString;
    }
}