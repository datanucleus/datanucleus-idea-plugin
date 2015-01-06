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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.intellij.openapi.vfs.VirtualFile;

import org.datanucleus.ide.idea.integration.EnhancerSupport;
import org.datanucleus.ide.idea.integration.EnhancerSupportVersion;

/**
 * Holds plugin's actual state.<br/>
 * This class provides the central state instance where persistent state is copied to-
 * and GUI state is copied from (forth and back, in case of config change or IDEA close).
 */
class DNEState {

    /**
     * Default file extension for metadata files (fallback)
     */
    static final Set<String> DEFAULT_METADATA_EXTENSIONS = new LinkedHashSet<String>(Arrays.asList("orm", "jdo"));

    private boolean enhancerEnabled = true;

    private Set<String> metaDataExtensions = new LinkedHashSet<String>(Arrays.asList("jdo", "orm"));

    /**
     * Indicator if {@link #metaDataExtensions} should be added to compiler resource patterns
     */
    private boolean addToCompilerResourcePatterns = true;

    private boolean includeTestClasses = true;

    private Set<String> enabledModules = new HashSet<String>();

    private PersistenceApi api = null;

    private final EnhancerSupportRegistry enhancerSupportRegistry = EnhancerSupportRegistryDefault.getInstance();

    private EnhancerSupport enhancerSupport = EnhancerSupportRegistryDefault.getInstance().getDefaultEnhancerSupport();

    private boolean dependenciesManual = false;

    private Map<String, Map<PersistenceApi, List<VirtualFile>>> dependencies =
            new LinkedHashMap<String, Map<PersistenceApi, List<VirtualFile>>>();

    DNEState() {
    }

    DNEState(final boolean enhancerEnabled,
             final Set<String> metaDataExtensions,
             final boolean addToCompilerResourcePatterns,
             final boolean includeTestClasses,
             final Set<String> enabledModules,
             final PersistenceApi api,
             final EnhancerSupport enhancerSupport,
             final boolean dependenciesManual,
             final Map<String, Map<PersistenceApi, List<VirtualFile>>> dependencies) {
        this.enhancerEnabled = enhancerEnabled;
        this.metaDataExtensions = new LinkedHashSet<String>(metaDataExtensions);
        this.addToCompilerResourcePatterns = addToCompilerResourcePatterns;
        this.includeTestClasses = includeTestClasses;
        this.enabledModules = new LinkedHashSet<String>(enabledModules);
        this.api = api;
        this.enhancerSupport = enhancerSupport;
        this.dependenciesManual = dependenciesManual;
        this.dependencies = new LinkedHashMap<String, Map<PersistenceApi, List<VirtualFile>>>(dependencies);
    }

    public boolean isEnhancerEnabled() {
        return this.enhancerEnabled;
    }

    public void setEnhancerEnabled(final boolean enhancerEnabled) {
        this.enhancerEnabled = enhancerEnabled;
    }

    public Set<String> getMetaDataExtensions() {
        return new LinkedHashSet<String>(this.metaDataExtensions);
    }

    void setMetaDataExtensions(final Collection<String> metaDataExtensions) {
        this.metaDataExtensions.clear();
        if (metaDataExtensions != null && !metaDataExtensions.isEmpty()) {
            this.metaDataExtensions.addAll(metaDataExtensions);
        }
    }

    public boolean isAddToCompilerResourcePatterns() {
        return this.addToCompilerResourcePatterns;
    }

    void setAddToCompilerResourcePatterns(final boolean addToCompilerResourcePatterns) {
        this.addToCompilerResourcePatterns = addToCompilerResourcePatterns;
    }

    public boolean isIncludeTestClasses() {
        return this.includeTestClasses;
    }

    public void setIncludeTestClasses(final boolean includeTestClasses) {
        this.includeTestClasses = includeTestClasses;
    }

    public Set<String> getEnabledModules() {
        return new LinkedHashSet<String>(this.enabledModules);
    }

    public void setEnabledModules(final Collection<String> enabledModules) {
        this.enabledModules.clear();
        if (enabledModules != null && !enabledModules.isEmpty()) {
            this.enabledModules.addAll(enabledModules);
        } else {
            if (this.enabledModules != null) {
                this.enabledModules.clear();
            }
        }
    }

    public EnhancerSupportRegistry getEnhancerSupportRegistry() {
        return this.enhancerSupportRegistry;
    }

    public EnhancerSupport getEnhancerSupport() {
        return this.enhancerSupport;
    }

    void setEnhancerSupport(final EnhancerSupport enhancerSupport) {
        this.enhancerSupport = enhancerSupport;
    }

    public PersistenceApi getApi() {
        return this.api;
    }

    void setApi(final PersistenceApi api) {
        this.api = api;
    }

    public boolean isDependenciesManual() {
        return this.dependenciesManual;
    }

    public void setDependenciesManual(final boolean dependenciesManual) {
        this.dependenciesManual = dependenciesManual;
    }

    public Map<String, Map<PersistenceApi, List<VirtualFile>>> getDependencies() {
        return new LinkedHashMap<String, Map<PersistenceApi, List<VirtualFile>>>(this.dependencies);
    }

    public void setDependencies(final Map<String, Map<PersistenceApi, List<VirtualFile>>> dependencies) {
        this.dependencies = new LinkedHashMap<String, Map<PersistenceApi, List<VirtualFile>>>(dependencies);
    }

    /**
     * Copy method for instances of this class.
     *
     * @param state the instance to retrieve the values from
     */
    public void copyFrom(final DNEState state) {
        this.enhancerEnabled = state.enhancerEnabled;
        this.setMetaDataExtensions(state.metaDataExtensions);
        this.setAddToCompilerResourcePatterns(state.addToCompilerResourcePatterns);
        this.includeTestClasses = state.includeTestClasses;
        this.setEnabledModules(state.enabledModules);
        this.setApi(state.api);
        this.setEnhancerSupport(state.enhancerSupport);
        this.dependenciesManual = state.dependenciesManual;
        this.setDependencies(state.dependencies);
    }

    /**
     * Update method from persistent state.
     *
     * @param state the instance to retrieve the values from
     */
    public void copyFrom(final DNEPersistentState state) {
        this.enhancerEnabled = state.isEnhancerEnabled();

        final Collection<String> metaDataExtensions = state.getMetaDataExtensions();
        if (metaDataExtensions == null || metaDataExtensions.isEmpty()) {
            this.setMetaDataExtensions(new LinkedHashSet<String>());
        } else {
            this.setMetaDataExtensions(new LinkedHashSet<String>(metaDataExtensions));
        }

        this.addToCompilerResourcePatterns = state.isAddToCompilerResourcePatterns();
        this.includeTestClasses = state.isIncludeTestClasses();

        final Collection<String> enabledModules1 = state.getEnabledModules();
        if (enabledModules1 == null || enabledModules1.isEmpty()) {
            this.setEnabledModules(new HashSet<String>());
        } else {
            this.setEnabledModules(new HashSet<String>(enabledModules1));
        }

        final EnhancerSupportRegistry eSR = this.enhancerSupportRegistry;
        final String enhancerSupportString = state.getEnhancerSupport();
        final EnhancerSupport enhancerSupport;

        if (enhancerSupportString == null || enhancerSupportString.trim().isEmpty() || !eSR.isRegistered(enhancerSupportString)) {
            enhancerSupport = eSR.getDefaultEnhancerSupport();
        } else {
            enhancerSupport = eSR.getEnhancerSupportById(enhancerSupportString);
        }
        this.setEnhancerSupport(enhancerSupport);

        final String persistenceApiString = state.getApi();
        final PersistenceApi configuredApi =
                persistenceApiString == null ? PersistenceApi.JPA : PersistenceApi.valueOf(persistenceApiString.toUpperCase());

        final PersistenceApi validForEnhancerSupport = enhancerSupport.isSupported(configuredApi)
                ? configuredApi : enhancerSupport.getDefaultPersistenceApi();

        this.setApi(validForEnhancerSupport);

        this.setDependenciesManual(enhancerSupport.getVersion() == EnhancerSupportVersion.V1_1_X && state.isDependenciesManual());

        final Map<String, Map<PersistenceApi, List<VirtualFile>>> newDependencies =
                new LinkedHashMap<String, Map<PersistenceApi, List<VirtualFile>>>();

        final Collection<DNEPersistentState.DependencyEntry> dependencyEntries = state.getDependencyEntries();
        if (dependencyEntries != null) {
            for (final DNEPersistentState.DependencyEntry dependencyEntry : dependencyEntries) {
                final String enhancerSupportId = dependencyEntry.getEnhancerSupportId();
                Map<PersistenceApi, List<VirtualFile>> byEnhSupp = newDependencies.get(enhancerSupportId);
                if (byEnhSupp == null) {
                    byEnhSupp = new LinkedHashMap<PersistenceApi, List<VirtualFile>>();
                    newDependencies.put(enhancerSupportId, byEnhSupp);
                }
                final PersistenceApi persistenceApi = dependencyEntry.getPersistenceApi();
                List<VirtualFile> depFiles = byEnhSupp.get(persistenceApi);
                if (depFiles == null) {
                    depFiles = new ArrayList<VirtualFile>();
                    byEnhSupp.put(persistenceApi, depFiles);
                }
                depFiles.add(dependencyEntry.getFile());
            }
        }
        this.dependencies = newDependencies;
    }

}
