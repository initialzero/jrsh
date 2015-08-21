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
package com.jaspersoft.jasperserver.jrsh.operation;

import com.jaspersoft.jasperserver.jrsh.common.MetadataScannerConfig;
import lombok.val;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.FilterBuilder;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.chomp;

/**
 * Scans packages for operation classes. It requires a config file
 * in the classpath which contains package names to scan and/or
 * operation classes with package name.
 * <p/>
 * Config example:
 * ---
 * packages:
 * - com.test.pack.operation.*
 * - org.my.app.operation.*
 * classes:
 * - some.pack.operation.impl.ReadOperation
 *
 * @author Alexander Krasnyanskiy
 * @since 2.0.5
 */
public abstract class PackageScanClassResolver {

    /**
     * Discovers the packages retrieves operation types.
     *
     * @param basePackage package to scan
     * @return operation classes
     */
    public static Set<Class<? extends Operation>> findOperationClasses(
            String basePackage) {
        val operationTypes = new HashSet<Class<? extends Operation>>();

        MetadataScannerConfig config = readConfig();
        List<String> externalPackagesToScan = config.getPackagesToScan();
        List<String> classes = config.getClasses();
        FilterBuilder filter =
                new FilterBuilder().includePackage(basePackage);
        //
        // Discover external operation types from configuration file
        //
        if (classes != null) {
            for (String aClass : classes) {
                try {
                    Class clz = Class.forName(aClass);
                    if (!Modifier.isAbstract(clz.getModifiers())
                            && Operation.class.isAssignableFrom(clz)) {
                        operationTypes.add(clz);
                    }
                } catch (ClassNotFoundException ignored) {
                }
            }
        }
        //
        // Prepare package filter to avoid unnecessary CP scanning
        //
        if (externalPackagesToScan != null) {
            for (String aPackage : externalPackagesToScan) {
                aPackage = chomp(aPackage, ".*");
                filter.includePackage(aPackage);
            }
        }
        //
        // Retrieve internal operation types
        //
        Reflections ref = new Reflections(new SubTypesScanner(), filter);
        for (val subType : ref.getSubTypesOf(Operation.class)) {
            if (!Modifier.isAbstract(subType.getModifiers())) {
                operationTypes.add(subType);
            }
        }
        return operationTypes;
    }

    /**
     * Reads a config file. The config should be located in classpath.
     *
     * @return config model
     */
    private static MetadataScannerConfig readConfig() {
        InputStream scanner = PackageScanClassResolver.class
                .getClassLoader()
                .getResourceAsStream("scanner.yml");
        return new Yaml().loadAs(scanner, MetadataScannerConfig.class);
    }

}
