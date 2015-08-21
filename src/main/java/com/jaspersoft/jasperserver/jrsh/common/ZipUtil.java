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

import com.jaspersoft.jasperserver.jrsh.common.exception.CouldNotZipFileException;
import com.jaspersoft.jasperserver.jrsh.common.exception.DirectoryDoesNotExistException;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.io.File.separator;
import static java.io.File.separatorChar;

/**
 * @author Alexander Krasnyanskiy
 */
@Deprecated
public class ZipUtil {

    // Use org.zeroturnaround.zip.ZipUtil instead

    public static File pack(String directory) {
        File dir = new File(directory);
        if (!dir.isDirectory()) {
            throw new DirectoryDoesNotExistException(directory);
        }
        directory = StringUtils.chomp(directory, separator);
        String outputFileName = directory.concat(".zip");
        try {
            File arch = new File(outputFileName);
            FileOutputStream fos = null;
            fos = new FileOutputStream(arch);
            ZipOutputStream zos = new ZipOutputStream(fos);
            addFiles(zos, directory, directory);
            zos.close();
            return arch;
        } catch (Exception unimportant) {
            throw new CouldNotZipFileException();
        }
    }

    //---------------------------------------------------------------------
    //                           Helper methods
    //---------------------------------------------------------------------

    protected static void addFiles(ZipOutputStream zos,
                                   String folder,
                                   String baseFolder) throws Exception {
        File file = new File(folder);
        if (file.exists()) {
            if (file.isDirectory()) {
                if (!folder.equalsIgnoreCase(baseFolder)) {
                    String entryName = folder.substring(baseFolder.length() + 1,
                            folder.length()) + separatorChar;
                    ZipEntry zipEntry = new ZipEntry(entryName);
                    zos.putNextEntry(zipEntry);
                }
                File files[] = file.listFiles();
                if (files != null) {
                    for (File f : files) {
                        addFiles(zos, f.getAbsolutePath(), baseFolder);
                    }
                }
            } else {
                String entryName = folder.substring(baseFolder.length() + 1,
                        folder.length());
                ZipEntry zipEntry = new ZipEntry(entryName);
                zos.putNextEntry(zipEntry);

                try (FileInputStream in = new FileInputStream(folder)) {
                    int len;
                    byte buf[] = new byte[1024];
                    while ((len = in.read(buf)) > 0) {
                        zos.write(buf, 0, len);
                    }
                    zos.closeEntry();
                }
            }
        }
    }
}
