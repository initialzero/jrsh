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
import com.jaspersoft.jasperserver.jrsh.operation.result.OperationResult;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.Set;

import static com.jaspersoft.jasperserver.jrsh.operation.OperationFactory.createOperationsByTypes;
import static com.jaspersoft.jasperserver.jrsh.operation.result.ResultCode.SUCCESS;

/**
 * @author Alexander Krasnyanskiy
 * @since 2.0
 */
@Data
@Master(name = "help",
        tail = true,
        usage = "help [operation]",
        description = "<help> demonstrates how to use cli")
public class HelpOperation implements Operation {

    private static final String PREFIX = StringUtils.repeat(" ", 3);
    private static final String LF = "\n";
    private static final String DLF = "\n\n";

    @Override
    public OperationResult execute(Session session) {
        Set<Operation> operations = createOperationsByTypes();

        StringBuilder builder = new StringBuilder("Available operations:").append(DLF);

        for (Operation operation : operations) {
            Master master = operation.getClass().getAnnotation(Master.class);

            Field field;
            String description;

            if (master != null) {
                builder
                        .append(PREFIX)
                        .append(master.description())
                        .append(LF)
                        .append(PREFIX)
                        .append("usage: ")
                        .append(master.usage())
                        .append(DLF);
            }
        }

        return new OperationResult(builder.toString(), SUCCESS, this, null);
    }
}
