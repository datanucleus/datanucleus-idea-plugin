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

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;

import org.datanucleus.ide.idea.ui.v10x.DNEConfigFormV10x;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: geri
 * Date: 10.09.12
 * Time: 21:02
 */
public abstract class DNEConfigFormFactory {

    private static final Logger LOGGER = Logger.getInstance(DNEConfigFormFactory.class.getName());

    private DNEConfigFormFactory() {
        // prohibit instantiation
    }

    /**
     * Safely (backwards compatible) create a config form.<br/>
     * <br/>
     * Fallback to the old gui version in case of an instantiation error (missing classes).
     *
     * @param guiState    .
     * @param projectBaseDir .
     * @return .
     */
    public static ConfigForm createConfigForm(@NotNull final GuiState guiState, @Nullable final VirtualFile projectBaseDir) {
        ConfigForm configForm = null;

        // new gui version
        try {
            configForm = new DNEConfigForm(guiState, projectBaseDir);
        } catch (Throwable e) {
            LOGGER.warn("IDEA version seems outdated, using fallback config form.", e);
        }

        if (configForm == null) {
            LOGGER.info("Instantiating fallback config form.");
            configForm = new DNEConfigFormV10x();
        }

        return configForm;
    }

}
