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
package com.jaspersoft.jasperserver.jrsh.completion.completer;

import com.jaspersoft.jasperserver.dto.resources.ClientResourceLookup;
import com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.AuthenticationFailedException;
import com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.IllegalParameterValueException;
import com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.ResourceNotFoundException;
import com.jaspersoft.jasperserver.jrsh.common.SessionFactory;
import com.jaspersoft.jasperserver.jrsh.common.SessionUtil;
import jline.console.completer.Completer;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.jaspersoft.jasperserver.jaxrs.client.apiadapters.resources.ResourceSearchParameter.FOLDER_URI;
import static com.jaspersoft.jasperserver.jaxrs.client.apiadapters.resources.ResourceSearchParameter.RECURSIVE;
import static org.apache.commons.lang3.StringUtils.startsWith;

@Log4j
public class RepositoryNameCompleter implements Completer {

    private static int uniqueId = 0;
    private static List<CharSequence> bufCandidates = new ArrayList<CharSequence>();

    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        if (buffer != null && cursor < buffer.length()) {
            candidates.add("");
            return buffer.length();
        }

        if (uniqueId == 0) {
            uniqueId = hashCode();
        }

        if (buffer == null) {
            candidates.add("/");
            return 0;
        } else {
            if (uniqueId == hashCode()) {
                if (buffer.isEmpty()) {
                    return 0;
                }
                List<Pair<String, Boolean>> resources;
                List<String> filteredResources;
                try {
                    if (isResourceExist(buffer)) {
                        resources = download(buffer);
                        if (!resources.isEmpty() && !buffer.equals("/")) {
                            return buffer.length() + 1;
                        }
                        fillResources(candidates, resources);
                    } else {
                        String root = getPreviousPath(buffer);
                        if (isResourceExist(root)) {
                            resources = download(root);
                            List<Pair<String, Boolean>> temp = new ArrayList<>();

                            for (Pair<String, Boolean> pair : resources) {
                                String resource = pair.getKey();
                                Boolean isFolder = pair.getRight();
                                if (startsWith(resource, buffer)) {
                                    ImmutablePair<String, Boolean> newPair =
                                            new ImmutablePair<String, Boolean>(resource, isFolder);
                                    temp.add(newPair);
                                }
                            }
                            fillResources(candidates, temp);
                        } else {
                            String lastInput = getLastInput(buffer);
                            if ("".equals(lastInput)) {
                                List<Pair<String, Boolean>> temp = new ArrayList<>();
                                ImmutablePair<String, Boolean> newPair = new ImmutablePair<>("", false);
                                temp.add(newPair);
                                fillResources(candidates, temp);
                                return buffer.length();
                            }
                        }
                    }
                } catch (AuthenticationFailedException e3) {
                    SessionUtil.reopenSession();
                    complete(buffer, cursor, candidates);
                }
                if (candidates.size() == 1) {
                    return buffer.lastIndexOf("/") + 1;
                }
                if (candidates.size() > 1) {
                    String lastInput = getLastInput(buffer);
                    if (compareCandidatesWithLastInput(lastInput, candidates)) {
                        return buffer.length() - lastInput.length();
                    }
                }
                return buffer.length();
            } else {
                candidates.addAll(bufCandidates);
                if (candidates.size() > 0) {
                    String lastInput = getLastInput(buffer);
                    if (compareCandidatesWithLastInput(lastInput, candidates)) {
                        return buffer.length() - lastInput.length();
                    }
                }
                return buffer.length();
            }
        }
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void fillResources(List<CharSequence> candidates, List<Pair<String, Boolean>> resources) {
        List<String> filteredResources;
        filteredResources = reformatResources(resources);
        candidates.addAll(filteredResources);
        bufCandidates.clear();
        bufCandidates.addAll(filteredResources);
    }

    String getLastInput(String buffer) {
        int idx = buffer.lastIndexOf("/");
        if (idx == -1) {
            return "";
        }
        String s = buffer.substring(idx, buffer.length());
        if (s.equals("/")) s = "";
        if (s.startsWith("/") && s.length() > 1) s = s.substring(1, s.length());
        return s;
    }

    private boolean compareCandidatesWithLastInput(String last, List<CharSequence> candidates) {
        for (CharSequence candidate : candidates) {
            if (!candidate.toString().startsWith(last)) {
                return false;
            }
        }
        return true;
    }

    private List<String> reformatResources(List<Pair<String, Boolean>> resources) {
        List<String> list = new ArrayList<>();
        for (Pair<String, Boolean> pair : resources) {
            String resource = pair.getLeft();
            Boolean isFolder = pair.getRight();
            String last;
            if (isFolder) {
                last = lastName(resource) + "/";
            } else {
                last = lastName(resource);
            }
            list.add(last);
        }
        return list;
    }

    private String getPreviousPath(String path) {
        int idx = StringUtils.lastIndexOf(path, "/");
        return idx > 0 ? path.substring(0, idx) : path.substring(0, idx + 1);
    }

    private String lastName(String path) {
        return new File(path).getName();
    }

    public List<Pair<String, Boolean>> download(String path) {
        List<Pair<String, Boolean>> list = new ArrayList<>();
        if (!isResourceExist(path)) {
            return list;
        }

        List<ClientResourceLookup> lookups;
        lookups = SessionFactory.getSharedSession()
                .resourcesService()
                .resources()
                .parameter(FOLDER_URI, path)
                .parameter(RECURSIVE, "false")
                .search()
                .getEntity()
                .getResourceLookups();
        for (ClientResourceLookup lookup : lookups) {
            String uri = lookup.getUri();
            String type = lookup.getResourceType();

            if ("folder".equals(type)) {
                list.add(new ImmutablePair<String, Boolean>(uri, true));
            } else {
                list.add(new ImmutablePair<String, Boolean>(uri, false));
            }
        }
        return list;
    }

    public boolean isResourceExist(String path) {
        try {
            SessionFactory.getSharedSession()
                    .resourcesService()
                    .resources()
                    .parameter(FOLDER_URI, path)
                    .parameter(RECURSIVE, "false")
                    .search();
        } catch (ResourceNotFoundException e2) {
            log.debug("resource not found");
            return false;
        } catch (NullPointerException e) {
            log.debug("NPE, returning empty list");
            return false;
        } catch (IllegalParameterValueException e) {
            // bug #164 fix
            return false;
        }
        return true;
    }
}
