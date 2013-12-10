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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: geri
 * Date: 10.09.12
 * Time: 16:37
 */
final class EnhancerModuleContext {

    private final Module module;
    private final VirtualFile outputDirectory;
    private final VirtualFile testOutputDirectory;
    private final Collection<VirtualMetadataFile> metadataFiles;
    private final Collection<VirtualMetadataFile> annotatedClassFiles;
    private final Collection<String> excludedDependencies;
    private final Collection<String> enhancerDependencies;

    EnhancerModuleContext(@NotNull final Module module,
                          @NotNull final VirtualFile outputDirectory,
                          @Nullable final VirtualFile testOutputDirectory,
                          @Nullable final Collection<VirtualMetadataFile> metadataFiles,
                          @Nullable final Collection<VirtualMetadataFile> annotatedClassFiles,
                          @Nullable final Collection<String> excludedDependencies,
                          @Nullable final Collection<String> enhancerDependencies) {
        this.module = module;
        this.outputDirectory = outputDirectory;
        this.testOutputDirectory = testOutputDirectory;

        this.excludedDependencies = excludedDependencies == null
                ? null : new LinkedHashSet<String>(excludedDependencies);

        this.metadataFiles = metadataFiles == null
                ? null : new ArrayList<VirtualMetadataFile>(metadataFiles);

        this.annotatedClassFiles = annotatedClassFiles == null
                ? null : new ArrayList<VirtualMetadataFile>(annotatedClassFiles);

        this.enhancerDependencies = enhancerDependencies == null ? null : new ArrayList<String>(enhancerDependencies);
    }

    @NotNull
    public Module getModule() {
        return this.module;
    }

    @NotNull
    public VirtualFile getOutputDirectory() {
        return this.outputDirectory;
    }

    @Nullable
    public VirtualFile getTestOutputDirectory() {
        return this.testOutputDirectory;
    }

    @NotNull
    public Collection<VirtualMetadataFile> getMetadataFiles() {
        return this.metadataFiles == null
                ? Collections.<VirtualMetadataFile>emptyList()
                : Collections.unmodifiableCollection(this.metadataFiles);
    }

    @NotNull
    public Collection<VirtualMetadataFile> getAnnotatedClassFiles() {
        return this.annotatedClassFiles == null
                ? Collections.<VirtualMetadataFile>emptyList()
                : Collections.unmodifiableCollection(this.annotatedClassFiles);
    }

    /**
     * Manually excluded dependencies for enhancing process.
     *
     * @return .
     */
    @NotNull
    public Collection<String> getExcludedDependencies() {
        return this.excludedDependencies == null
                ? Collections.<String>emptyList() : Collections.unmodifiableCollection(this.excludedDependencies);
    }

    /**
     * Manually set enhancer dependencies.<br/>
     * <br/>
     * IMPORTANT: those have to be used instead of the project module ones - which in turn have to be excluded.
     *
     * @return .
     */
    @NotNull
    public Collection<String> getEnhancerDependencies() {
        return this.enhancerDependencies == null
                ? Collections.<String>emptyList() : Collections.unmodifiableCollection(this.enhancerDependencies);
    }

}
