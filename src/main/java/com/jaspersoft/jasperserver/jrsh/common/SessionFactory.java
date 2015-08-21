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

import com.jaspersoft.jasperserver.jaxrs.client.core.AuthenticationCredentials;
import com.jaspersoft.jasperserver.jaxrs.client.core.RestClientConfiguration;
import com.jaspersoft.jasperserver.jaxrs.client.core.Session;
import com.jaspersoft.jasperserver.jaxrs.client.core.SessionStorage;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Alexander Krasnyanskiy
 */
public abstract class SessionFactory {
    private static Session sharedSession;

    public static Session getSharedSession() {
        return sharedSession;
    }

    public static Session createUnsharedSession(String serverUrl,
                                                String username,
                                                String password,
                                                String organization) {
        return createSession(serverUrl, username, password, organization);
    }

    public static Session createSharedSession(String serverUrl,
                                              String username,
                                              String password,
                                              String organization) {
        return sharedSession = createSession(serverUrl, username, password, organization);
    }

    protected static Session createSession(String serverUrl, String username,
                                           String password, String organization) {
        verify(serverUrl, username, password);

        if (organization == null) {
            username = username;
        } else {
            username = username.concat("|").concat(organization);
        }

        if (serverUrl.startsWith("http")) {
            serverUrl = serverUrl;
        } else {
            serverUrl = "http://".concat(serverUrl);
        }

        RestClientConfiguration configuration =
                new RestClientConfiguration(serverUrl);
        configuration.setConnectionTimeout(4500);
        configuration.setReadTimeout(4500);

        AuthenticationCredentials credentials =
                new AuthenticationCredentials(username, password);

        SessionStorage sessionStorage =
                new SessionStorage(configuration, credentials);

        return new Session(sessionStorage);
    }

    private static void verify(String url, String username, String password) {
        checkNotNull(username, "Username should not be 'null'");
        checkNotNull(password, "Password should not be 'null'");
        checkNotNull(url, "URL should not be 'null'");
    }

    public static void updateSharedSession(Session session) {
        sharedSession = session;
    }
}
