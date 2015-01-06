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

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.BooleanTableCellEditor;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import org.apache.commons.lang.Validate;
import org.datanucleus.ide.idea.EnhancerSupportRegistry;
import org.datanucleus.ide.idea.PersistenceApi;
import org.datanucleus.ide.idea.integration.EnhancerSupport;
import org.datanucleus.ide.idea.integration.EnhancerSupportVersion;
import org.datanucleus.ide.idea.ui.idea.AbstractAddDeletePanel;
import org.datanucleus.ide.idea.ui.idea.DependenciesAddDeletePanel;
import org.datanucleus.ide.idea.ui.swing.AffectedModulesRowModel;
import org.datanucleus.ide.idea.ui.swing.ColumnAdjuster;
import org.datanucleus.ide.idea.ui.swing.DeChatteringRadioButtonChangeListener;
import org.datanucleus.ide.idea.ui.swing.JHintingTextField;
import org.datanucleus.ide.idea.ui.swing.MetadataOrClassFilesRowModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 */
public class DNEConfigForm implements ConfigForm {

    private static final String METADATA_FILE_DISABLED = "<METADATA FILE SEARCH DISABLED>";

    private static final String[] METADATA_FS_DISABLED_TOKENS = {"<", ">", "metadata", "file", "search", "disabled"};

    private GuiState guiStateBeforeChanges = null;

    // Root Pane

    private final JPanel parentPanel;

    //
    // Settings Panel (Tab)

    private JPanel generalPanel;
    private JTabbedPane configTabbedPane;
    private JPanel configPanel;

    // General enhancer settings

    private JCheckBox enableEnhancerCheckBox;

    private JComboBox persistenceImplComboBox;

    private JRadioButton jDORadioButton;

    private JRadioButton jPARadioButton;

    private JCheckBox includeTestClassesCheckBox;

    private JTextField metadataExtensionTextField;

    private JCheckBox addToCompilerResourceCheckBox;

    // Project Module selection

    private JPanel indexNotReadyPanel;

    private JPanel contentPanel;

    private JTable affectedModulesTable;

    // Enhancement Info

    private JPanel infoPanel;

    private JScrollPane metaDataAndClassesScrollPane;

    private JTable metadataAndClassesTable;

    //
    // Dependencies Panel (Tab)

    private JPanel dependenciesPanel;

    private JRadioButton depProjectModuleRadioButton;

    private JRadioButton depManualRadioButton;
    private JLabel depManualUnsupportedLabel;

    private JPanel manualDependenciesDisabledInfoPanel;
    private final DependenciesAddDeletePanel dependenciesAddDeletePanel;
    private JPanel modifiersPanel;

    public DNEConfigForm(@NotNull final GuiState guiState, @Nullable final VirtualFile projectRootDir) {
        this.guiStateBeforeChanges = guiState;

        final String enhancerSupportId = guiState.getEnhancerSupport().getId();
        final PersistenceApi persistenceApi = guiState.getEnhancerSupport().getDefaultPersistenceApi();
        final Map<String, Map<PersistenceApi, List<VirtualFile>>> dependenciesFromGuiState = guiState.getDependencies();
        final Map<String, Map<PersistenceApi, List<VirtualFile>>> dependencies = dependenciesFromGuiState == null
                ? new LinkedHashMap<String, Map<PersistenceApi, List<VirtualFile>>>() : dependenciesFromGuiState;

        this.parentPanel = new JPanel();
        this.dependenciesAddDeletePanel = new DependenciesAddDeletePanel(enhancerSupportId,
                persistenceApi,
                dependencies,
                this.parentPanel,
                projectRootDir);

        // moved IDEA component initializer to constructor to be able to set initial state
        $$$setupUI$$$();
    }

//
    // Interface with DNEProjectComponent
    //

    @NotNull
    public JComponent getRootComponent() {
        return this.parentPanel;
    }

    //
    // Gui methods
    //

    public void setData(@NotNull final GuiState data) {
        this.guiStateBeforeChanges = new GuiState(data);

        //
        // Basic panels
        this.indexNotReadyPanel.setVisible(!data.isIndexReady());
        this.contentPanel.setVisible(data.isIndexReady());

        //
        // Enable enhancer checkbox
        this.enableEnhancerCheckBox.setSelected(data.isEnhancerEnabled());

        //
        // Persistence implementation selection
        final EnhancerSupportRegistry enhancerSupportRegistry = data.getEnhancerSupportRegistry();
        final Set<EnhancerSupport> supportedEnhancers = enhancerSupportRegistry.getSupportedEnhancers();
        this.persistenceImplComboBox.removeAllItems();
        for (final EnhancerSupport support : supportedEnhancers) {
            this.persistenceImplComboBox.addItem(support.getName());
        }
        final EnhancerSupport enhancerSupport = data.getEnhancerSupport();
        this.persistenceImplComboBox.setSelectedItem(enhancerSupport.getName());
        if (supportedEnhancers.size() <= 1) {
            this.persistenceImplComboBox.setVisible(false);
        } else {
            this.persistenceImplComboBox.setVisible(true);
        }

        // just to be sure -> validate persistence settings from config file
        PersistenceApi persistenceApi = data.getApi();
        if (!enhancerSupport.isSupported(persistenceApi)) {
            persistenceApi = enhancerSupport.getDefaultPersistenceApi();
        }

        this.jDORadioButton.setSelected(PersistenceApi.JDO == persistenceApi);
        this.jPARadioButton.setSelected(PersistenceApi.JPA == persistenceApi);
        this.jDORadioButton.setEnabled(enhancerSupport.isSupported(PersistenceApi.JDO));
        this.jPARadioButton.setEnabled(enhancerSupport.isSupported(PersistenceApi.JPA));

        //
        // Metadata file extensions text field
        this.metadataExtensionTextField.setText(data.getMetaDataExtensions().trim());

        //
        // Compiler resource file extensions control
        final boolean metadataExtensionsEnabled = data.getMetaDataExtensions() != null && !data.getMetaDataExtensions().trim().isEmpty();
        this.addToCompilerResourceCheckBox.setSelected(data.isAddToCompilerResourcePatterns());
        this.addToCompilerResourceCheckBox.setEnabled(metadataExtensionsEnabled);

        //
        // Test classes inclusion
        this.includeTestClassesCheckBox.setSelected(data.isIncludeTestClasses());

        //
        // Panel displaying an info message if enhancer is not initialized

        this.infoPanel.setVisible(!data.isEnhancerInitialized());
        this.infoPanel.setEnabled(!data.isEnhancerInitialized());

        //
        // Table displaying affected modules if enhancer is initialized

        final TableModel affectedModulesRowModel = new AffectedModulesRowModel(data.getAffectedModules());
        // modules affected by class enhancement
        this.affectedModulesTable.setModel(affectedModulesRowModel);
        // set column appearance
        final TableColumnModel columnModel = this.affectedModulesTable.getColumnModel();
        final TableColumn firstColumn = columnModel.getColumn(0);
        firstColumn.setMinWidth(50);
        firstColumn.setMaxWidth(50);
        firstColumn.setPreferredWidth(50);
        this.affectedModulesTable.setDefaultEditor(Boolean.class, new BooleanTableCellEditor(false));
        setPreferredTableHeight(this.affectedModulesTable, this.affectedModulesTable.getRowCount());

        //
        // Table displaying affected files/classes/.. if enhancer is initialized

        final TableModel metadataOrClassFilesRowModel =
                new MetadataOrClassFilesRowModel(data.getMetadataFiles(), data.getAnnotatedClassFiles());
        // files affected by class enhancement
        this.metadataAndClassesTable.setModel(metadataOrClassFilesRowModel);
        // set column appearance
        this.metadataAndClassesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // adjust column sizes (after being rendered the first time - necessary for ColumnAdjuster to work)
        final ColumnAdjuster columnAdjuster = new ColumnAdjuster(this.metadataAndClassesTable);
        //columnAdjuster.setOnlyAdjustLarger(false);
        columnAdjuster.setDynamicAdjustment(true);
        columnAdjuster.adjustColumns();
        setPreferredTableHeight(this.metadataAndClassesTable, this.metadataAndClassesTable.getRowCount());

        this.metadataAndClassesTable.setVisible(data.isEnhancerInitialized());

        // only display detected classes if initialized
        this.metaDataAndClassesScrollPane.setVisible(data.isEnhancerInitialized());

        if (enhancerSupport.getVersion() == EnhancerSupportVersion.V1_1_X) {
            this.depProjectModuleRadioButton.setSelected(!data.isDependenciesManual());
            this.depManualRadioButton.setSelected(data.isDependenciesManual());
            this.depManualRadioButton.setEnabled(true);
            this.depManualUnsupportedLabel.setVisible(false);
        } else {
            this.depProjectModuleRadioButton.setSelected(true);
            this.depManualRadioButton.setSelected(false);
            this.depManualRadioButton.setEnabled(false);
            this.depManualUnsupportedLabel.setVisible(true);
        }

        this.dependenciesAddDeletePanel.resetDependencyList(enhancerSupport.getId(), persistenceApi, data.getDependencies());
    }

    public void getData(@NotNull final GuiState data) {
        data.setEnhancerEnabled(this.enableEnhancerCheckBox.isSelected());

        if (containsDisabledTokens(this.metadataExtensionTextField.getText())) {
            data.setMetaDataExtensions("");
        } else {
            data.setMetaDataExtensions(this.metadataExtensionTextField.getText());
        }
        data.setAddToCompilerResourcePatterns(this.addToCompilerResourceCheckBox.isSelected());
        data.setIncludeTestClasses(this.includeTestClassesCheckBox.isSelected());

        final EnhancerSupport enhancerSupport = getByEnhancerSupportName(data, (String) this.persistenceImplComboBox.getSelectedItem());
        data.setEnhancerSupport(enhancerSupport);

        final PersistenceApi selectedApi = this.jDORadioButton.isSelected() ? PersistenceApi.JDO : PersistenceApi.JPA;

        final boolean apiSupported = enhancerSupport.isSupported(selectedApi);
        final PersistenceApi supportedApi = apiSupported ? selectedApi : enhancerSupport.getDefaultPersistenceApi();
        data.setApi(supportedApi);
        data.setAffectedModules(((AffectedModulesRowModel) this.affectedModulesTable.getModel()).getAffectedModules());

        data.setDependenciesManual(this.depManualRadioButton.isSelected());

        data.setDependencies(this.dependenciesAddDeletePanel.getDependencies());
    }

    public boolean isModified() {
        final GuiState before = this.guiStateBeforeChanges;
        if (this.enableEnhancerCheckBox.isSelected() != before.isEnhancerEnabled()) {
            return true;
        }
        final String metadataExtensionTextFieldText = this.metadataExtensionTextField.getText();
        if (metadataExtensionTextFieldText != null ?
                !metadataExtensionTextFieldText.trim().equals(before.getMetaDataExtensions())
                        && !containsDisabledTokens(metadataExtensionTextFieldText)
                : before.getMetaDataExtensions() != null && !before.getMetaDataExtensions().trim().isEmpty()) {
            return true;
        }
        if (this.addToCompilerResourceCheckBox.isSelected() != before.isAddToCompilerResourcePatterns()) {
            return true;
        }
        if (this.includeTestClassesCheckBox.isSelected() != before.isIncludeTestClasses()) {
            return true;
        }
        if (!this.jDORadioButton.isSelected() && PersistenceApi.JDO == before.getApi()) {
            return true;
        }
        if (!this.jPARadioButton.isSelected() && PersistenceApi.JPA == before.getApi()) {
            return true;
        }

        final EnhancerSupport enhancerSupport =
                getByEnhancerSupportName(before, (String) this.persistenceImplComboBox.getSelectedItem());
        if (!before.getEnhancerSupport().getId().equals(enhancerSupport.getId())) {
            return true;
        }

        final PersistenceApi selectedApi = this.jDORadioButton.isSelected() ? PersistenceApi.JDO : PersistenceApi.JPA;
        if (before.getApi() != selectedApi) {
            return true;
        }

        if (before.isDependenciesManual() != this.depManualRadioButton.isSelected()) {
            return true;
        }

        if (!before.getDependencies().equals(this.dependenciesAddDeletePanel.getDependencies())) {
            return true;
        }

        final AffectedModulesRowModel affectedModulesRowModel =
                (AffectedModulesRowModel) this.affectedModulesTable.getModel();
        final List<AffectedModule> affectedModules = affectedModulesRowModel.getAffectedModules();
        return affectedModules != null
                ? !affectedModules.equals(before.getAffectedModules()) : before.getAffectedModules() != null;
    }

    private void createUIComponents() {
        //
        // ComboBox for selecting persistence implementation

        this.persistenceImplComboBox = new JComboBox();
        this.persistenceImplComboBox.addItem(this.guiStateBeforeChanges.getEnhancerSupport().getName());
        this.persistenceImplComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {

                if ("comboBoxChanged".equals(e.getActionCommand())) {
                    final Object selectedItem = DNEConfigForm.this.persistenceImplComboBox.getSelectedItem();

                    if (selectedItem != null) {
                        final String selectedEnhancerSupportName = (String) selectedItem;
                        final EnhancerSupport selectedEnhancerSupport =
                                getByEnhancerSupportName(DNEConfigForm.this.guiStateBeforeChanges,
                                        selectedEnhancerSupportName);

                        final PersistenceApi selectedApi =
                                DNEConfigForm.this.jDORadioButton.isSelected() ? PersistenceApi.JDO : PersistenceApi.JPA;
                        final PersistenceApi supportedSelectedApi =
                                selectedEnhancerSupport.isSupported(selectedApi) ? selectedApi
                                        : selectedEnhancerSupport.getDefaultPersistenceApi();

                        if (selectedApi != supportedSelectedApi) {
                            JOptionPane.showMessageDialog(null, "Selected persistence implementation does not support "
                                    + selectedApi
                                    + ','
                                    + "\nreverting to " + supportedSelectedApi);
                        }

                        DNEConfigForm.this.jDORadioButton.setSelected(PersistenceApi.JDO == supportedSelectedApi);
                        DNEConfigForm.this.jPARadioButton.setSelected(PersistenceApi.JPA == supportedSelectedApi);
                        DNEConfigForm.this.jDORadioButton.setEnabled(selectedEnhancerSupport.isSupported(PersistenceApi.JDO));
                        DNEConfigForm.this.jPARadioButton.setEnabled(selectedEnhancerSupport.isSupported(PersistenceApi.JPA));
                        DNEConfigForm.this.persistenceImplComboBox.setSelectedItem(selectedEnhancerSupport.getName());

                        if (selectedEnhancerSupport.getVersion() == EnhancerSupportVersion.V1_1_X) {
                            DNEConfigForm.this.depProjectModuleRadioButton.setSelected(!DNEConfigForm.this.guiStateBeforeChanges.isDependenciesManual());
                            DNEConfigForm.this.depManualRadioButton.setSelected(DNEConfigForm.this.guiStateBeforeChanges.isDependenciesManual());
                            DNEConfigForm.this.depManualRadioButton.setEnabled(true);
                            DNEConfigForm.this.depManualUnsupportedLabel.setVisible(false);
                        } else {
                            DNEConfigForm.this.depProjectModuleRadioButton.setSelected(true);
                            DNEConfigForm.this.depManualRadioButton.setSelected(false);
                            DNEConfigForm.this.depManualRadioButton.setEnabled(false);
                            DNEConfigForm.this.depManualUnsupportedLabel.setVisible(true);
                        }

                        DNEConfigForm.this.configPanel.repaint();

                        DNEConfigForm.this.dependenciesAddDeletePanel.updateDependencyList(selectedEnhancerSupport.getId(),
                                supportedSelectedApi);

                    }
                }
            }
        });

        this.jDORadioButton = new JRadioButton();
        this.jDORadioButton.addChangeListener(new DeChatteringRadioButtonChangeListener() {
            @Override
            protected void changed(final ChangeEvent e, final JRadioButton source, final boolean selected) {
                final String selectedEnhSuppName = (String) DNEConfigForm.this.persistenceImplComboBox.getSelectedItem();
                final PersistenceApi selectedApi = selected ? PersistenceApi.JDO : PersistenceApi.JPA;
                final EnhancerSupport enhSuppId =
                        getByEnhancerSupportName(DNEConfigForm.this.guiStateBeforeChanges, selectedEnhSuppName);

                DNEConfigForm.this.configPanel.repaint();

                DNEConfigForm.this.dependenciesAddDeletePanel.updateDependencyList(enhSuppId.getId(), selectedApi);
            }
        });


        //
        // TextBox for metadata-file extensions

        this.metadataExtensionTextField = new JHintingTextField();
        ((JHintingTextField) this.metadataExtensionTextField).setEmptyTextHint(METADATA_FILE_DISABLED);
        this.metadataExtensionTextField.addKeyListener(new KeyListener() {
            public void keyTyped(final KeyEvent e) {
                // do nothing
            }

            public void keyPressed(final KeyEvent e) {
                // do nothing
            }

            public void keyReleased(final KeyEvent e) {
                final String text = DNEConfigForm.this.metadataExtensionTextField.getText();
                final String trimmedText = text.trim();
                final boolean isEmpty = trimmedText.isEmpty();
                DNEConfigForm.this.addToCompilerResourceCheckBox.setEnabled(!isEmpty);
            }
        });

        //
        // Dependencies type selection and List

        // dependency list

        this.dependenciesAddDeletePanel.setVisible(false);
        this.dependenciesAddDeletePanel.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                final String actionCommand = e.getActionCommand();
                if (AbstractAddDeletePanel.EVENT_ADD.equals(actionCommand)
                        || AbstractAddDeletePanel.EVENT_REMOVE.equals(actionCommand)) {

                    DNEConfigForm.this.parentPanel.firePropertyChange("dependenciesAddDeletePanel", 0, 1);
                }
            }
        });

        this.manualDependenciesDisabledInfoPanel = new JPanel();
        this.manualDependenciesDisabledInfoPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        this.manualDependenciesDisabledInfoPanel.setVisible(true);

        // dependencies type selection

        this.depProjectModuleRadioButton = new JRadioButton();
        this.depProjectModuleRadioButton.setSelected(true);
        this.depProjectModuleRadioButton.addChangeListener(new DeChatteringRadioButtonChangeListener() {
            @Override
            protected void changed(final ChangeEvent e, final JRadioButton source, final boolean selected) {
                DNEConfigForm.this.dependenciesAddDeletePanel.setVisible(!selected);
                DNEConfigForm.this.manualDependenciesDisabledInfoPanel.setVisible(selected);
            }
        });

        this.depManualRadioButton = new JRadioButton();
        this.depManualRadioButton.setSelected(false);
    }

    //
    // Utility methods
    //

    private static void setPreferredTableHeight(final JTable table, final int rows) {
        final int width = table.getPreferredSize().width;
        final int height = rows * table.getRowHeight();
        table.setPreferredSize(new Dimension(width, height));
    }

    private static EnhancerSupport getByEnhancerSupportName(final GuiState guiState, final String selectedItem) {
        EnhancerSupport ret = null;
        final EnhancerSupportRegistry enhancerSupportRegistry = guiState.getEnhancerSupportRegistry();
        for (final EnhancerSupport enhancerSupport : enhancerSupportRegistry.getSupportedEnhancers()) {
            final String enhancerSupportName = enhancerSupport.getName();
            if (enhancerSupportName.equals(selectedItem)) {
                ret = enhancerSupport;
                break;
            }
        }
        Validate.notNull(ret, "EnhancerSupport value is not supported! value=" + selectedItem);
        return ret;
    }

    private static boolean containsDisabledTokens(final String extensionsString) {
        boolean contains = false;

        for (final String metadataFsDisabledToken : METADATA_FS_DISABLED_TOKENS) {
            contains = contains || extensionsString != null && extensionsString.toLowerCase().contains(metadataFsDisabledToken);
        }

        return contains;
    }

    //
    // IDEA UI-Designer code (automatically generated, so do not touch!)
    //

// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        parentPanel.setLayout(new GridLayoutManager(2, 1, new Insets(1, 0, 0, 0), -1, -1));
        configTabbedPane = new JTabbedPane();
        parentPanel.add(configTabbedPane, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        configPanel = new JPanel();
        configPanel.setLayout(new GridLayoutManager(5, 3, new Insets(6, 2, 2, 2), -1, -1));
        configTabbedPane.addTab("Enhancer", configPanel);
        indexNotReadyPanel = new JPanel();
        indexNotReadyPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        configPanel.add(indexNotReadyPanel, new GridConstraints(3, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Please wait until indexing is finished");
        indexNotReadyPanel.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        configPanel.add(contentPanel, new GridConstraints(4, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPanel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-3355444)), "Affected Modules"));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        affectedModulesTable = new JTable();
        affectedModulesTable.setEnabled(true);
        affectedModulesTable.setFillsViewportHeight(false);
        affectedModulesTable.setPreferredScrollableViewportSize(new Dimension(450, 30));
        scrollPane1.setViewportView(affectedModulesTable);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPanel.add(panel2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-3355444)), "Metadata and annotated classes for enhancement", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, new Color(-16777216)));
        infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(infoPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Please click 'Make Project' to see affected files");
        infoPanel.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        metaDataAndClassesScrollPane = new JScrollPane();
        panel2.add(metaDataAndClassesScrollPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        metadataAndClassesTable = new JTable();
        metadataAndClassesTable.setFillsViewportHeight(false);
        metadataAndClassesTable.setFont(new Font(metadataAndClassesTable.getFont().getName(), metadataAndClassesTable.getFont().getStyle(), metadataAndClassesTable.getFont().getSize()));
        metadataAndClassesTable.setPreferredScrollableViewportSize(new Dimension(450, 100));
        metaDataAndClassesScrollPane.setViewportView(metadataAndClassesTable);
        modifiersPanel = new JPanel();
        modifiersPanel.setLayout(new GridLayoutManager(2, 3, new Insets(0, 2, 0, 0), -1, -1));
        configPanel.add(modifiersPanel, new GridConstraints(1, 0, 2, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText(" Metadata file extensions (use ';' to separate)");
        modifiersPanel.add(label3, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        includeTestClassesCheckBox = new JCheckBox();
        includeTestClassesCheckBox.setText("Include Test classes");
        modifiersPanel.add(includeTestClassesCheckBox, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        metadataExtensionTextField.setAlignmentX(0.5f);
        metadataExtensionTextField.setAutoscrolls(true);
        metadataExtensionTextField.setMargin(new Insets(1, 1, 1, 1));
        modifiersPanel.add(metadataExtensionTextField, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        addToCompilerResourceCheckBox = new JCheckBox();
        addToCompilerResourceCheckBox.setText("Add to compiler resource patterns");
        modifiersPanel.add(addToCompilerResourceCheckBox, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        dependenciesPanel = new JPanel();
        dependenciesPanel.setLayout(new GridLayoutManager(3, 1, new Insets(6, 2, 2, 2), -1, -1));
        configTabbedPane.addTab("Dependencies", dependenciesPanel);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), 5, 5));
        dependenciesPanel.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        depProjectModuleRadioButton.setText("Project Module Dependencies");
        panel3.add(depProjectModuleRadioButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        depManualRadioButton.setText("Manual Dependencies");
        panel3.add(depManualRadioButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel3.add(spacer1, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        depManualUnsupportedLabel = new JLabel();
        depManualUnsupportedLabel.setText("(Not supported by plugin extension)");
        panel3.add(depManualUnsupportedLabel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        manualDependenciesDisabledInfoPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        dependenciesPanel.add(manualDependenciesDisabledInfoPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setHorizontalAlignment(0);
        label4.setHorizontalTextPosition(0);
        label4.setText("Using Enhancer and it's Dependencies from Project Module");
        manualDependenciesDisabledInfoPanel.add(label4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        dependenciesPanel.add(dependenciesAddDeletePanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        generalPanel = new JPanel();
        generalPanel.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        parentPanel.add(generalPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        enableEnhancerCheckBox = new JCheckBox();
        enableEnhancerCheckBox.setText("Enable Enhancer");
        generalPanel.add(enableEnhancerCheckBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        generalPanel.add(panel4, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        jDORadioButton.setText("JDO");
        panel4.add(jDORadioButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jPARadioButton = new JRadioButton();
        jPARadioButton.setText("JPA");
        panel4.add(jPARadioButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel4.add(spacer2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        generalPanel.add(persistenceImplComboBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(jDORadioButton);
        buttonGroup.add(jPARadioButton);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(depProjectModuleRadioButton);
        buttonGroup.add(depManualRadioButton);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return parentPanel;
    }
}
