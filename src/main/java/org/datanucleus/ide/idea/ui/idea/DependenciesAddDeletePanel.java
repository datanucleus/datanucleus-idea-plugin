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

package org.datanucleus.ide.idea.ui.idea;

import javax.swing.DefaultListModel;
import javax.swing.ListCellRenderer;
import java.awt.Component;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.VirtualFile;

import org.datanucleus.ide.idea.PersistenceApi;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Panel with "Add" and "Delete" File buttons on the right side.<br/>
 * <br/>
 * Base implementation copied from {@link com.intellij.ui.AddDeleteListPanel}
 */
public class DependenciesAddDeletePanel extends AbstractAddDeletePanel<VirtualFile> {

    private static final FileChooserDescriptor FILE_CHOOSER_DESCRIPTOR =
            new FileChooserDescriptor(false, false, true, false, false, true);

    //
    // Members
    //

    private final Component parentComponent;
    private final VirtualFile projectRootDir;
    private final Map<String, Map<PersistenceApi, List<VirtualFile>>> dependencies =
            new LinkedHashMap<String, Map<PersistenceApi, List<VirtualFile>>>();

    private String enhancerSupportId;
    private PersistenceApi persistenceApi;

    //
    // Constructor
    //

    public DependenciesAddDeletePanel(@NotNull final String enhancerSupportId,
                                      @NotNull final PersistenceApi persistenceApi,
                                      @Nullable final Map<String, Map<PersistenceApi, List<VirtualFile>>> initialDependencies,
                                      @NotNull final Component parentComponent,
                                      @Nullable final VirtualFile projectRootDir) {
        super(null, getDependencyList(initialDependencies, enhancerSupportId, persistenceApi));
        this.enhancerSupportId = enhancerSupportId;
        this.persistenceApi = persistenceApi;
        this.parentComponent = parentComponent;
        this.projectRootDir = projectRootDir;
    }

    //
    // Update method for switching the dependency list
    //

    public void updateDependencyList(final String enhancerSupportId, final PersistenceApi persistenceApi) {
        // move previous list back into dependencies map
        if (!this.enhancerSupportId.equals(enhancerSupportId) || this.persistenceApi != persistenceApi) {
            this.storeDependenciesForPreviousGuiState();
        }

        // update to new values
        this.enhancerSupportId = enhancerSupportId;
        this.persistenceApi = persistenceApi;
        final Collection<VirtualFile> newList = getDependencyList(this.dependencies, enhancerSupportId, persistenceApi);
        super.resetListModel(newList);
    }

    public void resetDependencyList(final String enhancerSupportId,
                                    final PersistenceApi persistenceApi,
                                    final Map<String, Map<PersistenceApi, List<VirtualFile>>> dependencies) {

        // update to new values
        this.enhancerSupportId = enhancerSupportId;
        this.persistenceApi = persistenceApi;
        this.dependencies.clear();
        this.dependencies.putAll(dependencies);
        final Collection<VirtualFile> newList = getDependencyList(dependencies, enhancerSupportId, persistenceApi);
        this.resetListModel(newList);
    }

    public Map<String, Map<PersistenceApi, List<VirtualFile>>> getDependencies() {
        this.storeDependenciesForPreviousGuiState();
        return Collections.unmodifiableMap(this.dependencies);
    }



    //
    // AbstractAddDeletePanel abstract methods implementation
    //

    @Nullable
    @Override
    protected List<VirtualFile> findItemsToAdd() {
        final FileChooserDescriptor fileChooserDescriptor = this.getFileChooserDescriptor();
        final VirtualFile[] files = this.projectRootDir == null
                ? FileChooser.chooseFiles(this.parentComponent, fileChooserDescriptor)
                : FileChooser.chooseFiles(this.parentComponent, fileChooserDescriptor, this.projectRootDir);
        if (files.length == 0) {
            return null;
        }
        return Arrays.asList(files);
    }

    @NotNull
    @Override
    protected ListCellRenderer createListCellRenderer() {
        return new EnhancedFileListRenderer();
    }

    @NotNull
    @Override
    protected DefaultListModel createListModel() {
        return new DefaultListModel();
    }

    //
    // Methods overridable for further customization
    //

    @SuppressWarnings("MethodMayBeStatic")
    @NotNull
    protected FileChooserDescriptor getFileChooserDescriptor() {
        return FILE_CHOOSER_DESCRIPTOR;
    }

    //
    // Helper methods
    //

    private static Collection<VirtualFile> getDependencyList(final Map<String, Map<PersistenceApi, List<VirtualFile>>> dependencies,
                                                             final String enhancerSupportId,
                                                             final PersistenceApi persistenceApi) {

        Collection<VirtualFile> ret = null;
        final Map<PersistenceApi, List<VirtualFile>> byEnhSupp = dependencies.get(enhancerSupportId);
        if (byEnhSupp != null && !byEnhSupp.isEmpty()) {
            final List<VirtualFile> virtualFiles = byEnhSupp.get(persistenceApi);
            if (virtualFiles != null && !virtualFiles.isEmpty()) {
                ret = virtualFiles;
            }
        }

        return ret;
    }

    @SuppressWarnings("MapReplaceableByEnumMap")
    public void storeDependenciesForPreviousGuiState() {
        final List<VirtualFile> listItems = this.getListItems();
        Map<PersistenceApi, List<VirtualFile>> byEnhSupp = this.dependencies.get(this.enhancerSupportId);

        if (listItems.isEmpty()) {
            if (byEnhSupp != null) {
                byEnhSupp.remove(this.persistenceApi);
                if (byEnhSupp.isEmpty()) {
                    this.dependencies.remove(this.enhancerSupportId);
                }
            }
        } else {
            if (byEnhSupp == null) {
                byEnhSupp = new LinkedHashMap<PersistenceApi, List<VirtualFile>>();
            }
            byEnhSupp.put(this.persistenceApi, listItems);
            this.dependencies.put(this.enhancerSupportId, byEnhSupp);
        }
    }

}
