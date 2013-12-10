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

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * User: geri
 * Date: 11.09.12
 * Time: 05:42
 */
public abstract class ExceptionUtils {

    private ExceptionUtils() {
        // prohibit derivation
    }

    public static String stackTraceToString(final Throwable t) {
        // writer for stack trace printing
        final StringWriter writer = new StringWriter();
        // transform stack trace to string
        final PrintWriter printWriter = new PrintWriter(writer);
        try {
            t.printStackTrace(printWriter);
        } finally {
            printWriter.close();
        }
        return writer.toString();
    }

}
