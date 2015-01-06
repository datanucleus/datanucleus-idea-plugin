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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.intellij.openapi.vfs.VirtualFile;

import org.datanucleus.ide.idea.EnhancerSupportRegistry;
import org.datanucleus.ide.idea.PersistenceApi;
import org.datanucleus.ide.idea.integration.EnhancerSupport;
import org.datanucleus.ide.idea.integration.EnhancerSupportVersion;

/**
 * Holds temporary config data for the gui
 */
public class GuiState {

    private boolean indexReady = false;

    private boolean enhancerEnabled = true;

    private String metaDataExtensions = "jdo";

    /**
     * Indicator if {@link #metaDataExtensions} should be added to compiler resource patterns
     */
    private boolean addToCompilerResourcePatterns = true;

    private boolean includeTestClasses = true;

    private boolean enhancerInitialized = false;

    private PersistenceApi api;

    private EnhancerSupportRegistry enhancerSupportRegistry;

    private EnhancerSupport enhancerSupport;

    private List<AffectedModule> affectedModules;

    private final List<MetaDataOrClassFile> metadataFiles;

    private final List<MetaDataOrClassFile> annotatedClassFiles;

    private boolean dependenciesManual = false;

    private Map<String, Map<PersistenceApi, List<VirtualFile>>> dependencies;

    //
    // Constructors
    //

    public GuiState(final EnhancerSupportRegistry enhancerSupportRegistry) {
        this(false,
             false,
             "jdo",
             true,
             true,
             false,
             PersistenceApi.JDO,
             enhancerSupportRegistry,
             enhancerSupportRegistry.getDefaultEnhancerSupport(),
             new ArrayList<AffectedModule>(0),
             new ArrayList<MetaDataOrClassFile>(0),
             new ArrayList<MetaDataOrClassFile>(0),
             true,
             new LinkedHashMap<String, Map<PersistenceApi, List<VirtualFile>>>());
    }

    public GuiState(final boolean indexReady,
                    final boolean enhancerEnabled,
                    final String metaDataExtensions,
                    final boolean addToCompilerResourcePatterns,
                    final boolean includeTestClasses,
                    final boolean enhancerInitialized,
                    final PersistenceApi api,
                    final EnhancerSupportRegistry enhancerSupportRegistry,
                    final EnhancerSupport enhancerSupport,
                    final List<AffectedModule> affectedModules,
                    final List<MetaDataOrClassFile> metadataFiles,
                    final List<MetaDataOrClassFile> annotatedClassFiles,
                    final boolean dependenciesManual,
                    final Map<String, Map<PersistenceApi, List<VirtualFile>>> dependencies) {

        this.indexReady = indexReady;
        this.enhancerEnabled = enhancerEnabled;
        this.metaDataExtensions = metaDataExtensions;
        this.addToCompilerResourcePatterns = addToCompilerResourcePatterns;
        this.includeTestClasses = includeTestClasses;
        this.enhancerInitialized = enhancerInitialized;
        this.api = api;
        this.enhancerSupportRegistry = enhancerSupportRegistry;
        this.enhancerSupport = enhancerSupport;
        this.affectedModules = new ArrayList<AffectedModule>(affectedModules);
        this.annotatedClassFiles = new ArrayList<MetaDataOrClassFile>(annotatedClassFiles);
        this.metadataFiles = new ArrayList<MetaDataOrClassFile>(metadataFiles);
        this.dependenciesManual = enhancerSupport.getVersion() == EnhancerSupportVersion.V1_1_X && dependenciesManual;
        this.dependencies = deepCopyDependencies(dependencies);
    }

    public GuiState(final GuiState data) {
        this(data.isIndexReady(),
             data.isEnhancerEnabled(),
             data.getMetaDataExtensions(),
             data.isAddToCompilerResourcePatterns(),
             data.isIncludeTestClasses(),
             data.isEnhancerInitialized(),
             data.getApi(),
             data.getEnhancerSupportRegistry(),
             data.getEnhancerSupport(),
             deepCopyAffectedModules(data.getAffectedModules()),
             data.getMetadataFiles(),
             data.getAnnotatedClassFiles(),
             data.isDependenciesManual(),
             deepCopyDependencies(data.getDependencies()));
    }

    //
    // Methods
    //

    public boolean isIndexReady() {
        return this.indexReady;
    }

    public boolean isEnhancerEnabled() {
        return this.enhancerEnabled;
    }

    public void setEnhancerEnabled(final boolean enhancerEnabled) {
        this.enhancerEnabled = enhancerEnabled;
    }

    public String getMetaDataExtensions() {
        return this.metaDataExtensions;
    }

    public void setMetaDataExtensions(final String metaDataExtensions) {
        this.metaDataExtensions = metaDataExtensions;
    }

    public boolean isAddToCompilerResourcePatterns() {
        return this.addToCompilerResourcePatterns;
    }

    public void setAddToCompilerResourcePatterns(final boolean addToCompilerResourcePatterns) {
        this.addToCompilerResourcePatterns = addToCompilerResourcePatterns;
    }

    public boolean isIncludeTestClasses() {
        return this.includeTestClasses;
    }

    public void setIncludeTestClasses(final boolean includeTestClasses) {
        this.includeTestClasses = includeTestClasses;
    }

    public boolean isEnhancerInitialized() {
        return this.enhancerInitialized;
    }

    public PersistenceApi getApi() {
        return this.api;
    }

    public void setApi(final PersistenceApi api) {
        this.api = api;
    }

    public EnhancerSupportRegistry getEnhancerSupportRegistry() {
        return this.enhancerSupportRegistry;
    }

    public void setEnhancerSupportRegistry(final EnhancerSupportRegistry enhancerSupportRegistry) {
        this.enhancerSupportRegistry = enhancerSupportRegistry;
    }

    public EnhancerSupport getEnhancerSupport() {
        return this.enhancerSupport;
    }

    public void setEnhancerSupport(final EnhancerSupport enhancerSupport) {
        this.enhancerSupport = enhancerSupport;
    }

    public List<AffectedModule> getAffectedModules() {
        return new ArrayList<AffectedModule>(this.affectedModules);
    }

    public void setAffectedModules(final List<AffectedModule> affectedModules) {
        this.affectedModules = new ArrayList<AffectedModule>(affectedModules);
    }

    public List<MetaDataOrClassFile> getMetadataFiles() {
        return new ArrayList<MetaDataOrClassFile>(this.metadataFiles);
    }

    public List<MetaDataOrClassFile> getAnnotatedClassFiles() {
        return new ArrayList<MetaDataOrClassFile>(this.annotatedClassFiles);
    }

    public boolean isDependenciesManual() {
        return this.dependenciesManual;
    }

    public void setDependenciesManual(final boolean dependenciesManual) {
        this.dependenciesManual = dependenciesManual;
    }

    public Map<String, Map<PersistenceApi, List<VirtualFile>>> getDependencies() {
        return deepCopyDependencies(this.dependencies);
    }

    public void setDependencies(final Map<String, Map<PersistenceApi, List<VirtualFile>>> dependencies) {
        this.dependencies = deepCopyDependencies(dependencies);
    }
//
    // java.lang.Object overrides
    //

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        final GuiState guiState = (GuiState) o;

        if (this.enhancerEnabled != guiState.enhancerEnabled) { return false; }
        if (this.api != null ? this.api != guiState.api : guiState.api != null) {
            if (this.affectedModules != null ? !this.affectedModules.equals(guiState.affectedModules) : guiState.affectedModules != null) {
                return false;
            }
        }
        if (this.metaDataExtensions != null ? !this.metaDataExtensions.equals(guiState.metaDataExtensions)
                : guiState.metaDataExtensions != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = (this.enhancerEnabled ? 1 : 0);
        result = 31 * result + (this.api != null ? this.api.hashCode() : 0);
        result = 31 * result + (this.metaDataExtensions != null ? this.metaDataExtensions.hashCode() : 0);
        result = 31 * result + (this.affectedModules != null ? this.affectedModules.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GuiState{" +
               "affectedModules=" +
               this.affectedModules +
               ", enhancerInitialized=" +
               this.enhancerInitialized +
               ", api=" +
               this.api +
               ", metaDataExtensions='" +
               this.metaDataExtensions +
               '\'' +
               ", enhancerEnabled=" +
               this.enhancerEnabled +
               '}';
    }

    //
    // Helper methods
    //

    private static List<AffectedModule> deepCopyAffectedModules(final Collection<AffectedModule> affectedModules) {
        final List<AffectedModule> copyAffectedModules = new ArrayList<AffectedModule>(affectedModules.size());
        for (final AffectedModule affectedModule : affectedModules) {
            copyAffectedModules.add(new AffectedModule(affectedModule.isEnabled(), affectedModule.getName()));
        }
        return copyAffectedModules;
    }

    private static Map<String, Map<PersistenceApi, List<VirtualFile>>> deepCopyDependencies(final Map<String, Map<PersistenceApi, List<VirtualFile>>> dependencies) {
        final Map<String, Map<PersistenceApi, List<VirtualFile>>> bySuppId = new LinkedHashMap<String, Map<PersistenceApi, List<VirtualFile>>>();

        final Set<String> suppIds = dependencies.keySet();
        if (suppIds != null && !suppIds.isEmpty()) {
            for (final String suppId : suppIds) {
                final Map<PersistenceApi, List<VirtualFile>> byPersApi = dependencies.get(suppId);
                final Set<PersistenceApi> persApis = byPersApi.keySet();
                if (persApis != null && !persApis.isEmpty()) {
                    final Map<PersistenceApi, List<VirtualFile>> byPersApiCopy = new LinkedHashMap<PersistenceApi, List<VirtualFile>>();
                    for (final PersistenceApi persApi : persApis) {
                        final List<VirtualFile> depFiles = byPersApi.get(persApi);
                        if (depFiles != null) {
                            final List<VirtualFile> depFilesCopy = new ArrayList<VirtualFile>(depFiles);
                            byPersApiCopy.put(persApi, depFilesCopy);
                        }
                    }
                    bySuppId.put(suppId, byPersApiCopy);
                }
            }
        }

        return bySuppId;
    }

}

