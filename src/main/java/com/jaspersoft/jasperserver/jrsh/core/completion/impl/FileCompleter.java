package com.jaspersoft.jasperserver.jrsh.core.completion.impl;

import com.google.common.base.Preconditions;
import jline.console.completer.Completer;
import jline.internal.Configuration;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author Alexander Krasnyanskiy
 */
public class FileCompleter implements Completer {

    private String root;

    public int complete(String buffer, final int cursor, final List<CharSequence> candidates) {
        return SystemUtils.IS_OS_WINDOWS
                ? completeFileForWindows(buffer, candidates)
                : completeFileForUnix(buffer, candidates);
    }

    private int completeFileForWindows(String buffer, List<CharSequence> candidates) {
        if (buffer == null) {
            buffer = getRoot();
            candidates.add(buffer);
            return buffer.length();
        }

        String translated = buffer;

        File file = new File(translated);
        final File dir;

        if (translated.endsWith(separator())) {
            dir = file;
        } else {
            dir = file.getParentFile();
        }

        File[] entries;

        if (dir == null) {
            entries = new File[0];
        } else {
            entries = dir.listFiles();
        }

        return matchFiles(buffer, translated, entries, candidates);
    }

    private int completeFileForUnix(String buffer, List<CharSequence> candidates) {
        Preconditions.checkNotNull(candidates);

        if (buffer == null) {
            buffer = "";
        }

        String translated = buffer;
        File homeDir = getUserHome();

        if (translated.startsWith("~" + separator())) {
            translated = homeDir.getPath() + translated.substring(1);
        } else if (translated.startsWith("~")) {
            translated = homeDir.getParentFile().getAbsolutePath();
        } else if (!(new File(translated).isAbsolute())) {
            String cwd = getUserDir().getAbsolutePath();
            translated = cwd + separator() + translated;
        }

        File file = new File(translated);
        final File dir;

        if (translated.endsWith(separator())) {
            dir = file;
        } else {
            dir = file.getParentFile();
        }

        File[] entries = dir == null ? new File[0] : dir.listFiles();

        return matchFiles(buffer, translated, entries, candidates);
    }

    protected String separator() {
        return File.separator;
    }

    protected File getUserHome() {
        return Configuration.getUserHome();
    }

    protected File getUserDir() {
        return new File(".");
    }

    protected int matchFiles(final String buffer,
                             final String translated,
                             final File[] files,
                             final List<CharSequence> candidates) {
        if (files == null) {
            return -1;
        }

        int matches = 0;

        for (File file : files) {
            if (file.getAbsolutePath().startsWith(translated)) {
                matches++;
            }
        }

        for (File file : files) {
            if (file.getAbsolutePath().startsWith(translated)) {
                CharSequence name;
                if (matches == 1 && file.isDirectory()) {
                    if (SystemUtils.IS_OS_WINDOWS) {
                        name = file.getName() + (separator() + separator());
                    } else {
                        name = file.getName() + separator();
                    }
                } else {
                    name = file.getName() + " ";
                }
                candidates.add(render(name).toString());
            }
        }

        if (matches == 0 && candidates.isEmpty()) {
            candidates.add("");
            return buffer.length();
        }

        int idx = buffer.lastIndexOf(separator());
        return idx + separator().length();
    }

    protected CharSequence render(final CharSequence name) {
        return name;
    }

    public String getRoot() {
        Path root = Paths.get(System.getProperty("user.dir")).getRoot();
        String vol = root.normalize().toString();
        return vol.endsWith(separator())
                ? vol + separator()
                : vol + separator() + separator();
    }
}
