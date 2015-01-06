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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xmlb.annotations.AbstractCollection;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.datanucleus.ide.idea.integration.EnhancerSupport;
import org.datanucleus.ide.idea.util.VirtualFileUtils;

/**
 * Holds plugin's persistent state.
 */
public class DNEPersistentState { // has to be public (for IDEA configuration access)

    //
    // Members
    //

    private boolean enhancerEnabled = true;

    private Collection<String> metaDataExtensions = new ArrayList<String>(Arrays.asList("jdo", "orm"));

    /**
     * Indicator if {@link #metaDataExtensions} should be added to compiler resource patterns
     */
    private boolean addToCompilerResourcePatterns = true;

    private boolean includeTestClasses = true;

    private Collection<String> enabledModules = new ArrayList<String>();

    private String api = "JPA";

    private String enhancerSupport = "DATANUCLEUS";

    private boolean dependenciesManual = false;

    private Collection<String> dependencies = new ArrayList<String>();

    //
    // Accessors
    //

    public boolean isEnhancerEnabled() {
        return this.enhancerEnabled;
    }

    public void setEnhancerEnabled(final boolean enhancerEnabled) {
        this.enhancerEnabled = enhancerEnabled;
    }

    @AbstractCollection(elementTypes = String.class)
    public Collection<String> getMetaDataExtensions() {
        return new LinkedHashSet<String>(this.metaDataExtensions);
    }

    @AbstractCollection(elementTypes = String.class)
    public void setMetaDataExtensions(final Collection<String> metaDataExtensions) {
        this.metaDataExtensions = new LinkedHashSet<String>(metaDataExtensions);
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

    @AbstractCollection(elementTypes = String.class)
    public Collection<String> getEnabledModules() {
        return new LinkedHashSet<String>(this.enabledModules);
    }

    @AbstractCollection(elementTypes = String.class)
    public void setEnabledModules(final Collection<String> enabledModules) {
        this.enabledModules = new LinkedHashSet<String>(enabledModules);
    }

    public String getApi() {
        return this.api;
    }

    public void setApi(final String api) {
        this.api = api;
    }

    public String getEnhancerSupport() {
        return this.enhancerSupport;
    }

    public void setEnhancerSupport(final String enhancerSupport) {
        this.enhancerSupport = enhancerSupport;
    }

    public boolean isDependenciesManual() {
        return this.dependenciesManual;
    }

    public void setDependenciesManual(final boolean dependenciesManual) {
        this.dependenciesManual = dependenciesManual;
    }

    @AbstractCollection(elementTypes = String.class)
    public Collection<String> getDependencies() {
        return new LinkedHashSet<String>(this.dependencies);
    }

    @AbstractCollection(elementTypes = String.class)
    public void setDependencies(final Collection<String> dependencies) {
        this.dependencies = new LinkedHashSet<String>(dependencies == null ? Collections.<String>emptyList() : dependencies);
    }

    Collection<DNEPersistentState.DependencyEntry> getDependencyEntries() {
        final Collection<DNEPersistentState.DependencyEntry> dependencyEntries =
                new LinkedHashSet<DNEPersistentState.DependencyEntry>();

        for (final String dependency : this.dependencies) {
            dependencyEntries.add(DNEPersistentState.DependencyEntry.fromString(dependency));
        }

        return dependencyEntries;
    }

    void setDependencyEntries(final Iterable<DNEPersistentState.DependencyEntry> dependencyEntries) {
        final Collection<String> dependenciesToSet = new LinkedHashSet<String>();
        if (dependencyEntries != null) {
            for (final DNEPersistentState.DependencyEntry dependencyEntry : dependencyEntries) {
                dependenciesToSet.add(dependencyEntry.toString());
            }
        }
        this.dependencies = dependenciesToSet;
    }

    //
    // Additional implementation
    //

    /**
     * Copy method used to update persistent state with plugin's internal state.
     *
     * @param state plugin's internal state
     * @return persistent copy of plugin's internal state
     */
    public DNEPersistentState copyFrom(final DNEState state) {
        this.enhancerEnabled = state.isEnhancerEnabled();

        if (this.metaDataExtensions == null) {
            this.metaDataExtensions = new ArrayList<String>();
        } else {
            this.metaDataExtensions.clear();
        }
        this.metaDataExtensions.addAll(state.getMetaDataExtensions());

        this.includeTestClasses = state.isIncludeTestClasses();

        if (this.enabledModules == null) {
            this.enabledModules = new ArrayList<String>();
        } else {
            this.enabledModules.clear();
        }
        this.enabledModules.addAll(state.getEnabledModules());

        final EnhancerSupport configuredEnhancerSupport = state.getEnhancerSupport();
        final EnhancerSupport usedEnhancerSupport;
        if (configuredEnhancerSupport == null) {
            usedEnhancerSupport = state.getEnhancerSupportRegistry().getDefaultEnhancerSupport();
            this.enhancerSupport = usedEnhancerSupport.getId();
        } else {
            usedEnhancerSupport = configuredEnhancerSupport;
            this.enhancerSupport = configuredEnhancerSupport.getId();
        }

        final PersistenceApi api = state.getApi();
        if (api == null || !usedEnhancerSupport.isSupported(api)) {
            this.api = usedEnhancerSupport.getDefaultPersistenceApi().name();
        } else {
            this.api = api.name();
        }

        this.dependenciesManual = state.isDependenciesManual();

        final Map<String, Map<PersistenceApi, List<VirtualFile>>> dependencies = state.getDependencies();

        final Collection<DNEPersistentState.DependencyEntry> dependencyEntries =
                new ArrayList<DNEPersistentState.DependencyEntry>();
        if (dependencies != null) {
            for (final String enhancerSupportId : dependencies.keySet()) {
                final Map<PersistenceApi, List<VirtualFile>> depBySuppId = dependencies.get(enhancerSupportId);
                if (depBySuppId != null) {
                    for (final PersistenceApi persistenceApi : depBySuppId.keySet()) {
                        final List<VirtualFile> depFiles = depBySuppId.get(persistenceApi);
                        if (depFiles != null) {
                            for (final VirtualFile depFile : depFiles) {
                                final DNEPersistentState.DependencyEntry depEntry =
                                        new DNEPersistentState.DependencyEntry(enhancerSupportId,
                                                                               persistenceApi,
                                                                               depFile);
                                dependencyEntries.add(depEntry);
                            }    
                        }
                    }
                }
            }
        }

        this.setDependencyEntries(dependencyEntries);
        
        return this;
    }

    //
    // Helper classes
    //

    static class DependencyEntry {

        private static final String ENTRY_SEP = "::";
        private static final Pattern SPLIT_PATTERN = Pattern.compile(ENTRY_SEP);

        private final String enhancerSupportId;

        private final PersistenceApi persistenceApi;

        private final VirtualFile file;

        DependencyEntry(final String enhancerSupportId, final PersistenceApi persistenceApi, final VirtualFile file) {
            this.enhancerSupportId = enhancerSupportId;
            this.persistenceApi = persistenceApi;
            this.file = file;
        }

        public String getEnhancerSupportId() {
            return this.enhancerSupportId;
        }

        public PersistenceApi getPersistenceApi() {
            return this.persistenceApi;
        }

        public VirtualFile getFile() {
            return this.file;
        }

        @Override
        public String toString() {
            return this.enhancerSupportId
                    + ENTRY_SEP
                    + this.persistenceApi.name()
                    + ENTRY_SEP
                    + this.file.getPresentableUrl();
        }

        public static DNEPersistentState.DependencyEntry fromString(final String entryString) {
            Validate.notEmpty(entryString, "entryString is null or empty!");
            Validate.isTrue(StringUtils.countMatches(entryString, ENTRY_SEP) == 2, "entryString is invalid: " + entryString);
            final String[] entryStringParts = SPLIT_PATTERN.split(entryString.trim());

            final String entryEnhancerSupportId = entryStringParts[0];
            final String entryPersistenceApi = entryStringParts[1];
            final String entryFile = entryStringParts[2];

            final PersistenceApi parsedPersistenceApi = PersistenceApi.valueOf(entryPersistenceApi);

            final VirtualFile parsedVirtualFile = VirtualFileUtils.getVirtualFileForPath(entryFile);

            return new DNEPersistentState.DependencyEntry(entryEnhancerSupportId,
                                                          parsedPersistenceApi,
                                                          parsedVirtualFile);
        }
    }
}
