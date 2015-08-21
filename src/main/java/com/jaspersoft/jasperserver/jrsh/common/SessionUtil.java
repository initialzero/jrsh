package com.jaspersoft.jasperserver.jrsh.common;

import com.jaspersoft.jasperserver.jaxrs.client.core.AuthenticationCredentials;
import com.jaspersoft.jasperserver.jaxrs.client.core.JasperserverRestClient;
import com.jaspersoft.jasperserver.jaxrs.client.core.RestClientConfiguration;
import com.jaspersoft.jasperserver.jaxrs.client.core.Session;
import com.jaspersoft.jasperserver.jaxrs.client.core.SessionStorage;
import com.jaspersoft.jasperserver.jrsh.common.exception.NoActiveSessionAvailableException;

import static com.jaspersoft.jasperserver.jrsh.common.SessionFactory.updateSharedSession;

/**
 * @author Alexander Krasnyanskiy
 */
public abstract class SessionUtil {

    public static void reopenSession() {
        Session session = SessionFactory.getSharedSession();

        if (session == null)
            throw new NoActiveSessionAvailableException();

        SessionStorage storage = session.getStorage();
        AuthenticationCredentials credentials = storage.getCredentials();
        RestClientConfiguration cfg = storage.getConfiguration();

        session = new JasperserverRestClient(cfg)
                .authenticate(
                        credentials.getUsername(),
                        credentials.getPassword());

        updateSharedSession(session);
    }

}
