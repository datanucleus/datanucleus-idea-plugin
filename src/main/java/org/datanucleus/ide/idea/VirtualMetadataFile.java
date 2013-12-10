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

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ModuleRootModel;
import com.intellij.openapi.vfs.VirtualFile;

import org.apache.commons.lang.Validate;

/**
 * Abstraction of persistence related metadata.<br/>
 * <br/>
 * Handles metadata originating from classes (annotations) or xml files.<br/>
 *
 * TODO: seems hacky, do a complete review and cleanup
 */
class VirtualMetadataFile {

    private static final char PATH_SEPARATOR_CHAR = '/';

    private final Module module;

    private final boolean annotationBasedOnly;

    private final VirtualFile file;

    private final Collection<String> classNames;

    private final Collection<VirtualFile> classFiles;

    private final String displayFilename;

    private final String displayPath;

    VirtualMetadataFile(final Module module,
                               final boolean annotationBasedOnly,
                               final VirtualFile file,
                               final Collection<String> classNames,
                               final Collection<VirtualFile> classFiles) {
        Validate.notNull(module, "module is null");
        Validate.notNull(file, "file is null");
        Validate.notNull(classNames, "classNames is null");
        Validate.notNull(classFiles, "classFiles is null");
        this.module = module;
        this.annotationBasedOnly = annotationBasedOnly;
        this.file = file;
        this.classNames = Collections.unmodifiableCollection(classNames);
        this.classFiles = Collections.unmodifiableCollection(classFiles);

        //
        // extract filename and path for displaying purposes

        final String filePath = file.getPath();
        final Project project = module.getProject();
        final VirtualFile projectBaseDir = project.getBaseDir();
        final String projectBaseDirPath = projectBaseDir == null ? null : projectBaseDir.getPath();
        final ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
        final ModuleRootModel rootModel = moduleRootManager.getModifiableModel();
        final VirtualFile[] contentRoots = rootModel.getContentRoots();

        String pathWithoutModuleName = filePath;
        if (contentRoots.length > 0) {
            for (final VirtualFile contentRoot : contentRoots) {
                final String contentRootPath = contentRoot.getPath();
                if (pathWithoutModuleName.contains(contentRootPath)) {
                    pathWithoutModuleName =
                            pathWithoutModuleName.substring(pathWithoutModuleName.indexOf(contentRootPath) + contentRootPath.length());
                    break;
                }
            }
        }

        if (projectBaseDirPath != null && pathWithoutModuleName.contains(projectBaseDirPath)) {
            pathWithoutModuleName =
                    pathWithoutModuleName.substring(pathWithoutModuleName.indexOf(projectBaseDirPath) + projectBaseDirPath.length());
        }

        if (pathWithoutModuleName.startsWith(String.valueOf(PATH_SEPARATOR_CHAR))) {
            pathWithoutModuleName = pathWithoutModuleName.substring(1);
        }

        if (pathWithoutModuleName.lastIndexOf(PATH_SEPARATOR_CHAR) > -1) {
            this.displayFilename = pathWithoutModuleName.substring(pathWithoutModuleName.lastIndexOf(PATH_SEPARATOR_CHAR) + 1);
            this.displayPath = pathWithoutModuleName.substring(0, pathWithoutModuleName.lastIndexOf(PATH_SEPARATOR_CHAR));
        } else {
            this.displayFilename = pathWithoutModuleName;
            this.displayPath = "";
        }
    }

    public Module getModule() {
        return this.module;
    }

    public boolean isAnnotationBasedOnly() {
        return this.annotationBasedOnly;
    }

    public VirtualFile getFile() {
        return this.file;
    }

    public Collection<String> getClassNames() {
        return new ArrayList<String>(this.classNames);
    }

    public String getDisplayFilename() {
        return this.displayFilename;
    }

    public String getDisplayPath() {
        return this.displayPath;
    }

    public Collection<EnhancerItem> toEnhancerItems() {
        final Collection<EnhancerItem> enhancerItems =
                new ArrayList<EnhancerItem>(this.classNames.size() + (this.annotationBasedOnly ? 0 : 1));
        if (!this.annotationBasedOnly) {
            enhancerItems.add(new EnhancerItem(this, this.file));
        }
        for (final VirtualFile classFile : this.classFiles) {
            if (classFile != null) {
                enhancerItems.add(new EnhancerItem(this, classFile));
            }
        }
        return enhancerItems;
    }

}
