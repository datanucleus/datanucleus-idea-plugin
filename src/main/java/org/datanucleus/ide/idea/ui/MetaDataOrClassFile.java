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

package org.datanucleus.ide.idea.ui;

import org.apache.commons.lang.Validate;

/**
 */
public class MetaDataOrClassFile {

    private final String moduleName;

    private final String fileName;

    private final String path;

    private final String className;

    public MetaDataOrClassFile(final String moduleName, final String fileName, final String path, final String className) {
        Validate.notNull(moduleName, "moduleName is null!");
        Validate.notNull(fileName, "fileName is null!");
        Validate.notNull(className, "className is null!");

        this.moduleName = moduleName;
        this.fileName = fileName;
        this.path = path;
        this.className = className;
    }

    public String getModuleName() {
        return this.moduleName;
    }

    public String getFileName() {
        return this.fileName;
    }

    public String getPath() {
        return this.path;
    }

    public String getClassName() {
        return this.className;
    }

}
