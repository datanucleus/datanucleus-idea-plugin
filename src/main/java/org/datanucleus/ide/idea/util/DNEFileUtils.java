/*******************************************************************************
 * Copyright (c) 2010 Gerold Klinger and sourceheads Information Technology GmbH.
 * All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     ...
 ******************************************************************************/

package org.datanucleus.ide.idea.util;

import java.io.File;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;

/**
 * User: geri
 * Date: 11.09.12
 * Time: 01:17
 */
public abstract class DNEFileUtils {

    private static final Pattern WINDOWS_SEP = Pattern.compile("[\\\\]+");

    private static final Pattern MULTIPLE_SEP = Pattern.compile("[/]+");

    //
    // File separators
    //

    /**
     * Generic file separator as String
     */
    public static final String SEPARATOR_GENERIC = "/";

    /**
     * Generic file separator as char
     */
    public static final char SEPARATOR_GENERIC_CHAR = '/';

    /**
     * Platform specific file separator as String
     */
    public static final String SEPARATOR_PLATFORM = File.separator;

    /**
     * Platform specific file separator as String (duplicated)
     */
    public static final String SEPARATOR_PLATFORM_DUPLICATE = SEPARATOR_PLATFORM + SEPARATOR_PLATFORM;

    /**
     * Platform specific file separator as char
     */
    public static final char SEPARATOR_PLATFORM_CHAR = File.separatorChar;

    //
    // File extension separators
    //

    /**
     * Generic extension separator as String
     */
    public static final String EXTENSION_SEPARATOR_GENERIC = ".";

    /**
     * Generic extension separator as char
     */
    public static final char EXTENSION_SEPARATOR_GENERIC_CHAR = '.';

    //
    // File extension separators
    //

    private DNEFileUtils() {
        // prohibit derivation
    }

    //
    // Util methods
    //

    /**
     * Extracts the file base-name.<br>
     * <br>
     * Example:<br>
     * 'jcoma/test/index.html' -> 'index'<br>
     *
     * @param path the path to extract the file base-name from
     * @return base-name or null
     */
    public static String getFilenameBase(final String path) {
        Validate.notNull(path, "path is null!");
        String ret;

        if (path.isEmpty() || path.equals(EXTENSION_SEPARATOR_GENERIC)) {

            ret = null;

        } else {
            final String normalizedPath = normalizePath(path);
            ret = normalizedPath;

            final int indexOfSeparator = normalizedPath.lastIndexOf(SEPARATOR_GENERIC_CHAR);

            // remove separators
            final String withoutSeparators;
            if (indexOfSeparator > -1) {
                withoutSeparators = normalizedPath.substring(indexOfSeparator + 1);
            } else {
                withoutSeparators = normalizedPath;
            }

            // detect end index
            final int indexOfDotOrEnd;
            // index > 0 because of unix hidden files that start with a '.'
            if (withoutSeparators.lastIndexOf(EXTENSION_SEPARATOR_GENERIC_CHAR) > 0) {
                indexOfDotOrEnd = withoutSeparators.lastIndexOf(EXTENSION_SEPARATOR_GENERIC_CHAR);
            } else {
                indexOfDotOrEnd = withoutSeparators.length();
            }

            if (indexOfDotOrEnd > -1) {
                ret = withoutSeparators.substring(0, indexOfDotOrEnd);
            }
            if (ret.isEmpty()) {
                ret = null;
            }
        }

        return ret;
    }

    /**
     * remove duplicate and redundant separator characters
     *
     * @param p the path
     * @return fixed path
     */
    public static String normalizePath(final String p) {
        Validate.notEmpty(p, "p is null or empty!");

        return MULTIPLE_SEP.matcher(WINDOWS_SEP.matcher(p).replaceAll(SEPARATOR_GENERIC))
                .replaceAll(SEPARATOR_GENERIC);
    }

}
