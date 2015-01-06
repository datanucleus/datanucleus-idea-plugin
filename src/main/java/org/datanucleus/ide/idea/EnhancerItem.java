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

package org.datanucleus.ide.idea;

import com.intellij.openapi.compiler.FileProcessingCompiler;
import com.intellij.openapi.compiler.TimestampValidityState;
import com.intellij.openapi.compiler.ValidityState;
import com.intellij.openapi.vfs.VirtualFile;

import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

/**
 * File that is target or metadata source for the enhancement process.<br/>
 * This can be either a class file or a xml file containing persistence metadata.<br/>
 * <br/>
 * Acts as wrapper to handle validity state for incremental compilation/enhancement.
 * <p/>
 * TODO: seems hacky, do a complete review and cleanup
 */
class EnhancerItem implements FileProcessingCompiler.ProcessingItem {

    private final VirtualMetadataFile virtualMetadata;

    private final VirtualFile classFile;

    EnhancerItem(final VirtualMetadataFile virtualMetadata, final VirtualFile classFile) {
        Validate.notNull(classFile, "classFile is null!");
        this.virtualMetadata = virtualMetadata;
        this.classFile = classFile;
    }

    @NotNull
    public VirtualFile getFile() {
        return this.classFile;
    }

    public ValidityState getValidityState() {
        return new TimestampValidityState(this.classFile.getTimeStamp());
    }

    public VirtualMetadataFile getVirtualMetadata() {
        return this.virtualMetadata;
    }

    @Override
    public String toString() {
        return "EnhancerItem{" +
                "virtualMetadata=" + this.virtualMetadata +
                ", classFile=" + this.classFile +
                '}';
    }

}
