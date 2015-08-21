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

import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.importexport.importservice.ImportParameter;
import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.importexport.importservice.ImportTaskRequestAdapter;
import com.jaspersoft.jasperserver.jaxrs.client.core.Session;
import com.jaspersoft.jasperserver.jaxrs.client.dto.importexport.StateDto;
import com.jaspersoft.jasperserver.jrsh.common.ZipUtil;
import com.jaspersoft.jasperserver.jrsh.operation.Operation;
import com.jaspersoft.jasperserver.jrsh.operation.annotation.Master;
import com.jaspersoft.jasperserver.jrsh.operation.annotation.Parameter;
import com.jaspersoft.jasperserver.jrsh.operation.annotation.Value;
import com.jaspersoft.jasperserver.jrsh.operation.grammar.token.impl.FileNameToken;
import com.jaspersoft.jasperserver.jrsh.operation.grammar.token.impl.StringToken;
import com.jaspersoft.jasperserver.jrsh.operation.result.OperationResult;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.jaspersoft.jasperserver.jrsh.operation.result.ResultCode.FAILED;
import static com.jaspersoft.jasperserver.jrsh.operation.result.ResultCode.SUCCESS;
import static java.lang.String.format;
import static java.lang.System.getProperty;

/**
 * @author Alexander Krasnyanskiy
 * @since 2.0
 */
@Data
@Log4j
@Master(name = "import",
        usage = "import [file]",
        description = "<import> is used to import resources to JRS")
public class ImportOperation implements Operation {

    private static final String OK_MSG =
            "Import status: Success";
    private static final String FAILURE_MSG =
            "Import failed";
    private static final String FORMATTED_FAILURE_MSG =
            "Import failed: %s";
    private static final String UNKNOWN_CONTENT_MSG =
            "Neither a zip file nor a directory";
    private static final String IO_WARNING_MSG =
            "Could not delete a temporary file";

    @Parameter(mandatory = true, dependsOn = {"import"}, values =
    @Value(tail = true, tokenClass = FileNameToken.class, tokenAlias = "IPTH"))
    private String path;

    @Parameter(dependsOn = {"IPTH", "IIME", "IIAE", "IISS", "ISUU", "IWA"},
            values = @Value(
                    tokenAlias = "IIUR", tokenClass = StringToken.class,
                    tokenValue = "with-include-audit-events", tail = true
            )
    )
    private String withIncludeAuditEvents;
    @Parameter(dependsOn = {"IPTH", "IIUR", "IIAE", "IISS", "ISUU", "IWA"},
            values = @Value(
                    tokenAlias = "IIME", tokenClass = StringToken.class,
                    tokenValue = "with-include-monitoring-events", tail = true))
    private String withIncludeMonitoringEvents;
    @Parameter(dependsOn = {"IPTH", "IIUR", "IIME", "IISS", "ISUU", "IWA"},
            values = @Value(
                    tokenAlias = "IIAE", tokenClass = StringToken.class,
                    tokenValue = "with-include-access-events", tail = true))
    private String withIncludeAccessEvents;
    @Parameter(dependsOn = {"IWA", "ISUU", "IIAE", "IIME", "IIUR", "IPTH"},
            values = @Value(
                    tokenAlias = "IISS", tokenClass = StringToken.class,
                    tokenValue = "with-include-server-settings", tail = true))
    private String withIncludeServerSettings;
    @Parameter(dependsOn = {"IWA", "IISS", "IIAE", "IIME", "IIUR", "IPTH"},
            values = @Value(
                    tokenAlias = "ISUU", tokenClass = StringToken.class,
                    tokenValue = "with-skip-user-update", tail = true))
    private String withSkipUserUpdate;
    @Parameter(dependsOn = {"ISUU", "IISS", "IIAE", "IIME", "IIUR", "IPTH"},
            values = @Value(
                    tokenAlias = "IWA", tokenClass = StringToken.class,
                    tokenValue = "with-update", tail = true))
    private String withUpdate;

    @Override
    public OperationResult execute(Session session) {
        OperationResult result;
        try {
            if (path.startsWith("~")) {
                path = path.replaceFirst("^~", getProperty("user.home"));
            }

            if (path.contains("\\") && !SystemUtils.IS_OS_WINDOWS) {
                path = path.replaceAll("\\\\", "");
            }

            File content = new File(path);
            if (content.isDirectory()) {
                File importFile = /*new File("import.zip")*/
                        ZipUtil.pack(path);
                //ZipUtil.pack(content, importFile);
                ImportTaskRequestAdapter task =
                        session.importService().newTask();

                for (ImportParameter p : convertImportParameters()) {
                    task.parameter(p, true);
                }

                StateDto entity = task.create(importFile).getEntity();
                String phase = waitAndGetStatus(entity, session);

                if (importFile.exists()) {
                    boolean isDeleted = importFile.delete();
                    if (!isDeleted) {
                        log.warn(IO_WARNING_MSG);
                    }
                }
                if (phase.equals("finished")) {
                    result = new OperationResult(
                            OK_MSG, SUCCESS, this, null);
                } else {
                    result = new OperationResult(
                            FAILURE_MSG, FAILED, this, null);
                }
            } else if (content.isFile()) {
                ImportTaskRequestAdapter task =
                        session.importService().newTask();

                for (ImportParameter p : convertImportParameters()) {
                    task.parameter(p, true);
                }

                StateDto entity = task.create(new File(path)).getEntity();
                String status = waitAndGetStatus(entity, session);

                if ("failed".equals(status)) {
                    result = new OperationResult(
                            FAILURE_MSG, FAILED, this, null);
                } else {
                    result = new OperationResult(
                            OK_MSG, SUCCESS, this, null);
                }
            } else {
                result = new OperationResult(
                        UNKNOWN_CONTENT_MSG, FAILED, this, null);
            }
        } catch (Exception e) {
            result = new OperationResult(
                    format(FORMATTED_FAILURE_MSG, e.getMessage()),
                    FAILED, this, null);
        }
        return result;
    }

    protected String waitAndGetStatus(StateDto state, Session session) {
        String phase;
        while (true) {
            phase = getPhase(state, session);
            if ("finished".equals(phase) || "failed".equals(phase)) {
                break;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(250);
            } catch (InterruptedException ignored) {
                break;
            }
        }
        return phase;
    }

    protected String getPhase(StateDto state, Session session) {
        return session
                .exportService()
                .task(state.getId())
                .state()
                .getEntity()
                .getPhase();
    }

    // fixme
    protected List<ImportParameter> convertImportParameters() {
        List<ImportParameter> parameters = new ArrayList<>();
        if (withIncludeAccessEvents != null) {
            parameters.add(ImportParameter.INCLUDE_ACCESS_EVENTS);
        }
        if (withIncludeAuditEvents != null) {
            parameters.add(ImportParameter.INCLUDE_AUDIT_EVENTS);
        }
        if (withIncludeMonitoringEvents != null) {
            parameters.add(ImportParameter.INCLUDE_MONITORING_EVENTS);
        }
        if (withIncludeServerSettings != null) {
            parameters.add(ImportParameter.INCLUDE_SERVER_SETTINGS);
        }
        if (withUpdate != null) {
            parameters.add(ImportParameter.UPDATE);
        }
        if (withSkipUserUpdate != null) {
            parameters.add(ImportParameter.SKIP_USER_UPDATE);
        }
        return parameters;
    }
}