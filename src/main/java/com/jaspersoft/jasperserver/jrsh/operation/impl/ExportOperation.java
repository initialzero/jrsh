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

import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.importexport.exportservice.ExportParameter;
import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.importexport.exportservice.ExportService;
import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.importexport.exportservice.ExportTaskAdapter;
import com.jaspersoft.jasperserver.jaxrs.client.core.Session;
import com.jaspersoft.jasperserver.jaxrs.client.dto.importexport.StateDto;
import com.jaspersoft.jasperserver.jrsh.operation.Operation;
import com.jaspersoft.jasperserver.jrsh.operation.annotation.Master;
import com.jaspersoft.jasperserver.jrsh.operation.annotation.Parameter;
import com.jaspersoft.jasperserver.jrsh.operation.annotation.Value;
import com.jaspersoft.jasperserver.jrsh.operation.grammar.token.impl.FileNameToken;
import com.jaspersoft.jasperserver.jrsh.operation.grammar.token.impl.RepositoryToken;
import com.jaspersoft.jasperserver.jrsh.operation.grammar.token.impl.StringToken;
import com.jaspersoft.jasperserver.jrsh.operation.result.OperationResult;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.joda.time.DateTime;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.jaspersoft.jasperserver.jrsh.operation.result.ResultCode.FAILED;
import static com.jaspersoft.jasperserver.jrsh.operation.result.ResultCode.SUCCESS;
import static java.lang.String.format;
import static java.lang.System.getProperty;
import static org.joda.time.DateTimeZone.UTC;

/**
 * @author Alexander Krasnyanskiy
 * @since 2.0
 */
@Data
@Master(name = "export",
        usage = "export [context] [parameters]",
        description = "<export> is used to download JRS resources")
public class ExportOperation implements Operation {

    private static final String FORMATTED_OK_MSG =
            "Export status: Success (File has been created: %s)";
    private static final String FAILURE_MSG =
            "Export failed";
    private static final String FORMATTED_FAILURE_MSG =
            "Export failed (%s)";

    @Parameter(mandatory = true, dependsOn = "export",
            values = @Value(
                    tokenAlias = "RE", tokenClass = StringToken.class,
                    tokenValue = "repository"
            )
    )
    private String context;

    @Parameter(mandatory = true, dependsOn = "export",
            ruleGroups = "BRANCH",
            values = @Value(
                    tokenAlias = "OL", tokenClass = StringToken.class,
                    tokenValue = "all", tail = true
            )
    )
    private String all;

    @Parameter(mandatory = true, dependsOn = "RE",
            values = @Value(
                    tokenAlias = "RP", tokenClass = RepositoryToken.class,
                    tail = true
            )
    )
    private String repositoryPath;
    @Parameter(dependsOn = "RP",
            values = @Value(
                    tokenAlias = "->", tokenClass = StringToken.class,
                    tokenValue = "to"
            )
    )
    private String to;

    @Parameter(dependsOn = "->",
            values = @Value(
                    tokenAlias = "F", tokenClass = FileNameToken.class,
                    tail = true
            )
    )
    private String fileUri;

    @Parameter(dependsOn = {"F", "RP", "IUR", "IME", "RPP", "IAE"},
            values = @Value(
                    tokenAlias = "UR", tokenClass = StringToken.class,
                    tokenValue = "with-user-roles", tail = true
            )
    )
    private String withUserRoles;

    @Parameter(dependsOn = {"F", "RP", "UR", "IME", "RPP", "IAE"},
            values = @Value(
                    tokenAlias = "IUR", tokenClass = StringToken.class,
                    tokenValue = "with-include-audit-events", tail = true
            )
    )
    private String withIncludeAuditEvents;

    @Parameter(dependsOn = {"F", "RP", "UR", "IUR", "RPP", "IAE"},
            values = @Value(
                    tokenAlias = "IME", tokenClass = StringToken.class,
                    tokenValue = "with-include-monitoring-events",
                    tail = true
            )
    )
    private String withIncludeMonitoringEvents;

    @Parameter(dependsOn = {"F", "RP", "UR", "IUR", "IME", "IAE"},
            values = @Value(
                    tokenAlias = "RPP", tokenClass = StringToken.class,
                    tokenValue = "with-repository-permissions", tail = true
            )
    )
    private String withRepositoryPermissions;

    @Parameter(dependsOn = {"F", "RP", "UR", "IUR", "RPP", "IME"},
            values = @Value(
                    tokenAlias = "IAE", tokenClass = StringToken.class,
                    tokenValue = "with-include-access-events", tail = true
            )
    )
    private String withIncludeAccessEvents;

    @Override
    public OperationResult execute(Session session) {
        OperationResult result;
        try {
            ExportService exportService = session.exportService();
            ExportTaskAdapter task = exportService.newTask();
            if ("repository".equals(context)) {
                if (repositoryPath != null) {
                    task.uri(repositoryPath);
                }

                StateDto state = task
                        .parameters(convertExportParameters())
                        .create()
                        .getEntity();

                InputStream entity = session.exportService()
                        .task(state.getId())
                        .fetch()
                        .getEntity();

                if (to != null) {
                    if (fileUri != null) {
                        if (fileUri.startsWith("~")) {
                            fileUri = fileUri.replaceFirst("^~",
                                    getProperty("user.home"));
                            if (!SystemUtils.IS_OS_WINDOWS) {
                                fileUri = fileUri.replaceAll("\\\\", "");
                            }
                        }
                        File target = new File(fileUri);
                        FileUtils.copyInputStreamToFile(entity, target);
                    }
                } else {
                    DateTime dateTime = DateTime.now().toDateTime(UTC);
                    File target = new File(format("export_%s.zip", dateTime));
                    FileUtils.copyInputStreamToFile(entity, target);
                    fileUri = target.getAbsolutePath();
                }
            }
            if (all != null && !all.isEmpty()) {
                StateDto state = task
                        .parameter(ExportParameter.EVERYTHING)
                        .create()
                        .getEntity();

                InputStream entity = session.exportService()
                        .task(state.getId())
                        .fetch()
                        .getEntity();

                DateTime dateTime = DateTime.now().toDateTime(UTC);
                File target = new File(format("export_%s.zip", dateTime));
                FileUtils.copyInputStreamToFile(entity, target);
                fileUri = target.getAbsolutePath();
            }
            result = new OperationResult(
                    format(FORMATTED_OK_MSG, fileUri), SUCCESS,
                    this, null);
        } catch (Exception err) {
            result = new OperationResult(
                    FAILURE_MSG, FAILED,
                    this, null);
        }

        return result;
    }

    protected List<ExportParameter> convertExportParameters() {
        List<ExportParameter> parameters = new ArrayList<>();
        if (withIncludeAccessEvents != null) {
            parameters.add(ExportParameter.INCLUDE_ACCESS_EVENTS);
        }
        if (withIncludeAuditEvents != null) {
            parameters.add(ExportParameter.INCLUDE_AUDIT_EVENTS);
        }
        if (withRepositoryPermissions != null) {
            parameters.add(ExportParameter.REPOSITORY_PERMISSIONS);
        }
        if (withUserRoles != null) {
            parameters.add(ExportParameter.ROLE_USERS);
        }
        if (withIncludeMonitoringEvents != null) {
            parameters.add(ExportParameter.INCLUDE_MONITORING_EVENTS);
        }
        return parameters;
    }
}

