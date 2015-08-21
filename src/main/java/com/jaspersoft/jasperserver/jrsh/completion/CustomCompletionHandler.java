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
package com.jaspersoft.jasperserver.jrsh.completion;

import jline.console.ConsoleReader;
import jline.console.CursorBuffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Alexander Krasnyanskiy
 */
public class CustomCompletionHandler implements jline.console.completer.CompletionHandler {

    public boolean complete(final ConsoleReader reader, final List<CharSequence> candidates, final int pos)
            throws IOException {
        CursorBuffer buf = reader.getCursorBuffer();
        if (candidates.size() == 1) {
            CharSequence value = candidates.get(0);
            if (value.equals(buf.toString())) {
                return false;
            }
            setBuffer(reader, value, pos);
            return true;
        } else if (candidates.size() > 1) {
            String value = getUnambiguousCompletions(candidates);
            setBuffer(reader, value, pos);
        }
        printCandidates(reader, candidates);
        reader.drawLine();
        return true;
    }

    public static void setBuffer(final ConsoleReader reader, final CharSequence value, final int offset)
            throws IOException {
        while ((reader.getCursorBuffer().cursor > offset) && reader.backspace()) {}
        reader.putString(value);
        reader.setCursorPosition(offset + value.length());
    }

    public static void printCandidates(final ConsoleReader reader, Collection<CharSequence> candidates)
            throws IOException {
        Set<CharSequence> distinct = new HashSet<CharSequence>(candidates);
        if (distinct.size() > reader.getAutoprintThreshold()) {
            reader.print(String.format("Display all %d possibilities? (y or n)", candidates.size()));
            reader.flush();
            int c;
            String noOpt = "y";
            String yesOpt = "n";
            char[] allowed = {yesOpt.charAt(0), noOpt.charAt(0)};
            while ((c = reader.readCharacter(allowed)) != -1) {
                String tmp = new String(new char[]{(char) c});
                if (noOpt.startsWith(tmp)) {
                    reader.println();
                    return;
                } else if (yesOpt.startsWith(tmp)) {
                    break;
                } else {
                    reader.beep();
                }
            }
        }
        if (distinct.size() != candidates.size()) {
            Collection<CharSequence> copy = new ArrayList<CharSequence>();
            for (CharSequence next : candidates) {
                if (!copy.contains(next)) {
                    copy.add(next);
                }
            }

            candidates = copy;
        }

        // Let's skip a new line
        if (candidates.size() > 1) {
            reader.println();
            reader.printColumns(candidates);
        } else {
            reader.print("\r");
        }
    }

    private String getUnambiguousCompletions(final List<CharSequence> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        String[] strings = candidates.toArray(new String[candidates.size()]);

        String first = strings[0];
        StringBuilder candidate = new StringBuilder();

        for (int i = 0; i < first.length(); i++) {
            if (startsWith(first.substring(0, i + 1), strings)) {
                candidate.append(first.charAt(i));
            } else {
                break;
            }
        }

        return candidate.toString();
    }

    private boolean startsWith(final String starts, final String[] candidates) {
        for (String candidate : candidates) {
            if (!candidate.startsWith(starts)) {
                return false;
            }
        }
        return true;
    }
}
