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

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.intellij.compiler.CompilerConfiguration;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import org.datanucleus.ide.idea.integration.EnhancerSupport;
import org.datanucleus.ide.idea.ui.AffectedModule;
import org.datanucleus.ide.idea.ui.ConfigForm;
import org.datanucleus.ide.idea.ui.DNEConfigFormFactory;
import org.datanucleus.ide.idea.ui.GuiState;
import org.datanucleus.ide.idea.ui.MetaDataOrClassFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Component registering the enhancer computable and handling the plugin's state
 * (Interacting with configuration GUI by converting between the different state models)
 */
@State(name = "DataNucleusConfiguration",
       storages = {@Storage(id = "default", file = "$PROJECT_FILE$"),
                   @Storage(id = "dir", file = "$PROJECT_CONFIG_DIR$/datanucleus-plugin.xml", scheme = StorageScheme.DIRECTORY_BASED)})
public class DNEProjectComponent extends AbstractProjectComponent implements Configurable, PersistentStateComponent<DNEPersistentState> {

    public static final ExtensionPointName<EnhancerSupport> EP_NAME = ExtensionPointName.create(EnhancerSupport.EXTENSION_POINT_NAME);

    private static final String RESOURCE_PATTERN_PREFIX = "?*.";

    private static final Pattern REPLACE_PATTERN_WILDCARD_ALL = Pattern.compile("\\*");

    private static final Pattern REPLACE_PATTERN_WILDCARD_DOT = Pattern.compile("\\.");

    private static final Pattern PATTERM_EXTENSION_SEPARATOR = Pattern.compile(";");

    //
    // Members
    //

    /**
     * Current project
     */
    private final Project project;

    /**
     * Persistent configuration
     */
    private final DNEState state = new DNEState();

    /**
     * Enhancer instance (created on first build run)
     */
    private DNEComputable dNEComputable = null;

    private ConfigForm configGuiForm = null;

    //
    // Constructor
    //

    public DNEProjectComponent(final Project p) {
        super(p);
        this.project = p;
        // TODO: without: no idea logs? why?
        org.apache.log4j.BasicConfigurator.configure();
        final EnhancerSupportRegistry enhancerSupportRegistry = this.state.getEnhancerSupportRegistry();
        enhancerSupportRegistry.registerEnhancerSupport(EnhancerSupportRegistryDefault.DEFAULT_ENHANCER_SUPPORT);

        // load extensions
        final EnhancerSupport[] enhancerSupports = Extensions.getExtensions(EP_NAME);
        for (final EnhancerSupport enhancerSupport : enhancerSupports) {
            enhancerSupportRegistry.registerEnhancerSupport(enhancerSupport);
        }
    }

    //
    // ProjectComponent Interface implementation
    //

    @Override
    public void projectOpened() {
        super.projectOpened();
        this.dNEComputable = new DNEComputable(this.project, DNEProjectComponent.this.state);
        // run enhancer after compilation
        final CompilerManager compilerManager = CompilerManager.getInstance(this.project);
        compilerManager.addCompiler(this.dNEComputable);
    }

    @SuppressWarnings("RefusedBequest")
    @NonNls
    @NotNull
    @Override
    public String getComponentName() {
        return "Datanucleus Enhancer";
    }

    //
    // ToggleEnableAction methods
    //

    public void setEnhancerEnabled(final boolean enabled) {
        this.state.setEnhancerEnabled(enabled);
    }

    //
    // PersistentStateComponent Interface implementation
    //

    @Override
    public DNEPersistentState getState() {
        final DNEPersistentState dnePersistentState = new DNEPersistentState();
        return dnePersistentState.copyFrom(this.state);
    }

    @SuppressWarnings("FeatureEnvy")
    @Override
    public void loadState(final DNEPersistentState state) {
        this.state.copyFrom(state);

        //
        // Validate configuration

        final EnhancerSupport stateEnhancerSupport = this.state.getEnhancerSupport();
        final String stateEnhancerSupportId = stateEnhancerSupport.getId();
        final String persistentStateEnhancerSupportId = state.getEnhancerSupport();

        if (!stateEnhancerSupportId.equals(persistentStateEnhancerSupportId)) {
            JOptionPane.showMessageDialog(null, "Settings Error: Reverted DataNucleus enhancer support from "
                                                + persistentStateEnhancerSupportId
                                                + " to " + stateEnhancerSupportId
                                                + ".\nPlease reset plugin configuration.");
        }
        final PersistenceApi api = this.state.getApi();
        final String stateApi = api.name();
        final String persistentStateApi = state.getApi();
        if (!stateApi.equals(persistentStateApi)) {
            JOptionPane.showMessageDialog(null, "Settings Error: Reverted DataNucleus enhancer api from "
                                                + persistentStateApi
                                                + " to " + stateApi
                                                + ".\nPlease reset plugin configuration.");
        }
    }

    //
    // Configurable interface implementation
    //

    @Nls
    @Override
    public String getDisplayName() {
        return "DataNucleus Enhancer";
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public String getHelpTopic() {
        return null;
    }

    @Override
    public JComponent createComponent() {
        if (this.configGuiForm == null) {
            this.configGuiForm = DNEConfigFormFactory.createConfigForm(this.getGuiState(), this.project.getBaseDir());
        }
        return this.configGuiForm.getRootComponent();
    }

    @Override
    public boolean isModified() {
        return this.configGuiForm.isModified();
    }

    @SuppressWarnings("FeatureEnvy")
    @Override
    public void apply() throws ConfigurationException {
        final GuiState guiState = new GuiState(this.state.getEnhancerSupportRegistry());
        this.configGuiForm.getData(guiState);
        this.setGuiState(guiState);

        //
        // update compiler resource patterns
        if (this.state.isAddToCompilerResourcePatterns()) {
            final Collection<String> metadataExtensions = this.state.getMetaDataExtensions();
            if (!metadataExtensions.isEmpty()) {
                final CompilerConfiguration cConfig = CompilerConfiguration.getInstance(this.project);
                for (final String metadataExtension : metadataExtensions) {
                    if (!cConfig.isResourceFile("test." + metadataExtension)) {
                        cConfig.addResourceFilePattern(RESOURCE_PATTERN_PREFIX + metadataExtension);
                    }
                }
            }
        }

        this.reset();
    }

    @Override
    public void reset() {
        this.configGuiForm.setData(this.getGuiState());
    }

    @Override
    public void disposeUIResources() {
        this.configGuiForm = null;
    }

    //
    // Gui interface
    //

    @SuppressWarnings("FeatureEnvy")
    private GuiState getGuiState() {
        boolean indexReady = false;
        final boolean enhancerEnabled = this.state.isEnhancerEnabled();
        final String metaDataExtension = getMetaDataExtensionsString(this.state.getMetaDataExtensions());
        final boolean addToCompilerResourcePatterns = this.state.isAddToCompilerResourcePatterns();
        final boolean includeTestClasses = this.state.isIncludeTestClasses();
        final boolean enhancerInitialized = this.dNEComputable != null;
        final EnhancerSupport enhancerSupport = this.state.getEnhancerSupport();
        final PersistenceApi api = this.state.getApi() == null
                ? enhancerSupport.getDefaultPersistenceApi() : this.state.getApi();
        List<AffectedModule> affectedModules;
        List<MetaDataOrClassFile> metaDataFiles;
        List<MetaDataOrClassFile> annotatedClassFiles;
        try {
            affectedModules = this.getAffectedModulesGuiModel();
            metaDataFiles = this.createMetadataFilesGuiModel();
            annotatedClassFiles = this.createAnnotatedClassFilesGuiModel();
            indexReady = true;
        } catch (IndexNotReadyException ignored) {
            affectedModules = new ArrayList<AffectedModule>(0);
            metaDataFiles = new ArrayList<MetaDataOrClassFile>(0);
            annotatedClassFiles = new ArrayList<MetaDataOrClassFile>(0);
        }
        final boolean dependenciesManual = this.state.isDependenciesManual();
        final Map<String, Map<PersistenceApi, List<VirtualFile>>> dependencies = this.state.getDependencies();
        return new GuiState(indexReady,
                            enhancerEnabled,
                            metaDataExtension,
                            addToCompilerResourcePatterns,
                            includeTestClasses,
                            enhancerInitialized,
                            api,
                            this.state.getEnhancerSupportRegistry(),
                            enhancerSupport,
                            affectedModules,
                            metaDataFiles,
                            annotatedClassFiles,
                            dependenciesManual,
                            dependencies);
    }

    @SuppressWarnings("FeatureEnvy")
    private void setGuiState(final GuiState guiState) {
        final boolean enhancerEnabled = guiState.isEnhancerEnabled();
        final LinkedHashSet<String> metaDataExtensions = getMetaDataExtensionsSet(guiState.getMetaDataExtensions());
        final boolean addToCompilerResourcePatterns = guiState.isAddToCompilerResourcePatterns();
        final boolean includeTestClasses = guiState.isIncludeTestClasses();
        final PersistenceApi api = guiState.getApi();
        final EnhancerSupport enhancerSupport = guiState.getEnhancerSupport();
        final Set<String> enabledModules = getEnabledModulesFromGuiModel(guiState.getAffectedModules());
        final boolean dependenciesManual = guiState.isDependenciesManual();
        final Map<String, Map<PersistenceApi, List<VirtualFile>>> dependencies = guiState.getDependencies();
        final DNEState updateState =
                new DNEState(enhancerEnabled,
                             metaDataExtensions,
                             addToCompilerResourcePatterns,
                             includeTestClasses,
                             enabledModules,
                             api,
                             enhancerSupport,
                             dependenciesManual,
                             dependencies);
        this.state.copyFrom(updateState);
        // TODO: hack to filter modules not supported by enhancer (filtering only possible after updating the state with enhancer settings)
        this.filterEnhancerSupportedModules();
    }

    //
    // Gui model helper methods
    //

    @SuppressWarnings("FeatureEnvy")
    private void filterEnhancerSupportedModules() {
        // TODO: hack to filter modules not supported by enhancer (filtering only possible after updating the state with enhancer settings)
        final List<AffectedModule> affectedModulesGuiModel = getAffectedModulesGuiModel();
        final Collection<String> filter = new HashSet<String>(affectedModulesGuiModel.size());
        for (final AffectedModule affectedModule : affectedModulesGuiModel) {
            filter.add(affectedModule.getName());
        }

        final Collection<String> enhancerSupportedModules = new LinkedHashSet<String>(affectedModulesGuiModel.size());
        for (final String enabledModule : this.state.getEnabledModules()) {
            if (this.state.isDependenciesManual() || filter.contains(enabledModule)) {
                enhancerSupportedModules.add(enabledModule);
            }
        }

        this.state.setEnabledModules(enhancerSupportedModules);
    }

    @SuppressWarnings("FeatureEnvy")
    private List<AffectedModule> getAffectedModulesGuiModel() {
        final List<AffectedModule> moduleList = new ArrayList<AffectedModule>();
        final List<Module> affectedModules = IdeaProjectUtils.getDefaultAffectedModules(this.state.getEnhancerSupport(), this.project, this.state.isDependenciesManual());

        for (final Module module : affectedModules) {
            final Set<String> enabledModules = this.state.getEnabledModules();
            final boolean enabled = enabledModules != null && enabledModules.contains(module.getName());
            moduleList.add(new AffectedModule(enabled, module.getName()));
        }
        return moduleList;
    }

    private static Set<String> getEnabledModulesFromGuiModel(final Iterable<AffectedModule> affectedModules) {
        final Set<String> enabledModules = new HashSet<String>();
        if (affectedModules != null) {
            for (final AffectedModule affectedModule : affectedModules) {
                if (affectedModule.isEnabled()) {
                    enabledModules.add(affectedModule.getName());
                }
            }
        }
        return enabledModules;
    }

    private List<MetaDataOrClassFile> createMetadataFilesGuiModel() {
        final Map<Module, List<VirtualMetadataFile>> metaDataFiles =
                this.dNEComputable == null ? new LinkedHashMap<Module, List<VirtualMetadataFile>>()
                        : this.dNEComputable.getMetadataFiles(null);
        return createFilesGuiModel(metaDataFiles);
    }

    private List<MetaDataOrClassFile> createAnnotatedClassFilesGuiModel() {
        final Map<Module, List<VirtualMetadataFile>> annotatedClassFiles =
                this.dNEComputable == null ? new LinkedHashMap<Module, List<VirtualMetadataFile>>()
                        : this.dNEComputable.getAnnotatedClassFiles(null);
        return createFilesGuiModel(annotatedClassFiles);
    }

    @SuppressWarnings("FeatureEnvy")
    private static List<MetaDataOrClassFile> createFilesGuiModel(final Map<Module,
            List<VirtualMetadataFile>> metaDataOrAnnotatedClassFiles) {

        final List<MetaDataOrClassFile> metaDataOrClassFiles = new ArrayList<MetaDataOrClassFile>();
        for (final Map.Entry<Module, List<VirtualMetadataFile>> moduleListEntry : metaDataOrAnnotatedClassFiles.entrySet()) {
            for (final VirtualMetadataFile vf : moduleListEntry.getValue()) {
                for (final String mfClassName : vf.getClassNames()) {
                    final Module moduleListEntryKey = moduleListEntry.getKey();
                    metaDataOrClassFiles.add(new MetaDataOrClassFile(moduleListEntryKey.getName(),
                                                                     vf.getDisplayFilename(),
                                                                     vf.getDisplayPath(),
                                                                     mfClassName));
                }
            }
        }
        return metaDataOrClassFiles;
    }

    private static String getMetaDataExtensionsString(final Collection<String> extensions) {
        final StringBuilder sb = new StringBuilder();
        int count = 0;
        for (final String extension : extensions) {
            sb.append(extension);
            ++count;
            if (count < extensions.size()) {
                sb.append("; ");
            }
        }
        return sb.toString();
    }

    private static LinkedHashSet<String> getMetaDataExtensionsSet(final String extensions) {
        final LinkedHashSet<String> retExtensions = new LinkedHashSet<String>();
        if (extensions != null && !extensions.isEmpty()) {
            final Matcher replacePatternWildCardAll = REPLACE_PATTERN_WILDCARD_ALL.matcher(extensions);
            final Matcher replacePatternWildCardDot = REPLACE_PATTERN_WILDCARD_DOT.matcher(replacePatternWildCardAll.replaceAll(""));
            final String cleanedExtensions = replacePatternWildCardDot.replaceAll("");
            final String[] rawExtensions = PATTERM_EXTENSION_SEPARATOR.split(cleanedExtensions);
            for (final String rawExtension : rawExtensions) {
                retExtensions.add(rawExtension.trim());
            }
        }
        return retExtensions;
    }

}
