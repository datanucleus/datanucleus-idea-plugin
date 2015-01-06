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

package org.datanucleus.ide.idea.ui.v10x;

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
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;
import java.util.Set;

import com.intellij.ui.BooleanTableCellEditor;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import org.apache.commons.lang.Validate;
import org.datanucleus.ide.idea.EnhancerSupportRegistry;
import org.datanucleus.ide.idea.PersistenceApi;
import org.datanucleus.ide.idea.integration.EnhancerSupport;
import org.datanucleus.ide.idea.ui.AffectedModule;
import org.datanucleus.ide.idea.ui.ConfigForm;
import org.datanucleus.ide.idea.ui.GuiState;
import org.datanucleus.ide.idea.ui.swing.AffectedModulesRowModel;
import org.datanucleus.ide.idea.ui.swing.ColumnAdjuster;
import org.datanucleus.ide.idea.ui.swing.JHintingTextField;
import org.datanucleus.ide.idea.ui.swing.MetadataOrClassFilesRowModel;
import org.jetbrains.annotations.NotNull;

/**
 */
public class DNEConfigFormV10x implements ConfigForm {

    private static final String METADATA_FILE_DISABLED = "<METADATA FILE SEARCH DISABLED>";

    private static final String[] METADATA_FS_DISABLED_TOKENS = {"<", ">", "metadata", "file", "search", "disabled"};

    private GuiState guiState = null;

    private JPanel configPanel;

    private JCheckBox enableEnhancerCheckBox;

    private JTextField metadataExtensionTextField;

    private JTable affectedModulesTable;

    private JTable metadataAndClassesTable;

    private JCheckBox addToCompilerResourceCheckBox;

    private JPanel contentPanel;

    private JPanel infoPanel;

    private JCheckBox includeTestClassesCheckBox;

    private JScrollPane metaDataAndClassesScrollPane;

    private JPanel indexNotReadyPanel;

    private JRadioButton jDORadioButton;

    private JRadioButton jPARadioButton;

    private JComboBox persistenceImplComboBox;

    //
    // Interface with DNEProjectComponent
    //

    @NotNull
    public JComponent getRootComponent() {
        return this.configPanel;
    }

    //
    // Gui methods
    //

    public void setData(@NotNull final GuiState data) {
        this.guiState = new GuiState(data);

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
        if (this.persistenceImplComboBox.getItemCount() == 0) {
            for (final EnhancerSupport support : supportedEnhancers) {
                this.persistenceImplComboBox.addItem(support.getName());
            }
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
        data.setDependenciesManual(false);
    }

    public boolean isModified() {
        final GuiState data = this.guiState;
        if (this.enableEnhancerCheckBox.isSelected() != data.isEnhancerEnabled()) {
            return true;
        }
        final String metadataExtensionTextFieldText = this.metadataExtensionTextField.getText();
        if (metadataExtensionTextFieldText != null ?
                !metadataExtensionTextFieldText.trim().equals(data.getMetaDataExtensions())
                && !containsDisabledTokens(metadataExtensionTextFieldText)
                : data.getMetaDataExtensions() != null && !data.getMetaDataExtensions().trim().isEmpty()) {
            return true;
        }
        if (this.addToCompilerResourceCheckBox.isSelected() != data.isAddToCompilerResourcePatterns()) {
            return true;
        }
        if (this.includeTestClassesCheckBox.isSelected() != data.isIncludeTestClasses()) {
            return true;
        }
        if (!this.jDORadioButton.isSelected() && PersistenceApi.JDO == data.getApi()) {
            return true;
        }
        if (!this.jPARadioButton.isSelected() && PersistenceApi.JPA == data.getApi()) {
            return true;
        }

        final EnhancerSupport enhancerSupport = getByEnhancerSupportName(data, (String) this.persistenceImplComboBox.getSelectedItem());
        if (!data.getEnhancerSupport().getId().equals(enhancerSupport.getId())) {
            return true;
        }

        final PersistenceApi selectedApi = this.jDORadioButton.isSelected() ? PersistenceApi.JDO : PersistenceApi.JPA;
        if (data.getApi() != selectedApi) {
            return true;
        }

        final AffectedModulesRowModel affectedModulesRowModel = (AffectedModulesRowModel) this.affectedModulesTable.getModel();
        final List<AffectedModule> affectedModules = affectedModulesRowModel.getAffectedModules();
        return affectedModules != null ? !affectedModules.equals(data.getAffectedModules()) : data.getAffectedModules() != null;
    }

    private void createUIComponents() {

        //
        // ComboBox for selecting persistence implementation

        this.persistenceImplComboBox = new JComboBox();
        this.persistenceImplComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {

                if ("comboBoxChanged".equals(e.getActionCommand())) {
                    final EnhancerSupport selectedEnhancerSupport =
                            getByEnhancerSupportName(DNEConfigFormV10x.this.guiState,
                                                     (String) DNEConfigFormV10x.this.persistenceImplComboBox.getSelectedItem());

                    final PersistenceApi selectedApi =
                            DNEConfigFormV10x.this.jDORadioButton.isSelected() ? PersistenceApi.JDO : PersistenceApi.JPA;
                    final PersistenceApi supportedSelectedApi =
                            selectedEnhancerSupport.isSupported(selectedApi) ? selectedApi
                                    : selectedEnhancerSupport.getDefaultPersistenceApi();

                    if (selectedApi != supportedSelectedApi) {
                        JOptionPane.showMessageDialog(null, "Selected persistence implementation does not support "
                                                            + selectedApi
                                                            + ','
                                                            + "\nreverting to " + supportedSelectedApi);
                    }

                    DNEConfigFormV10x.this.jDORadioButton.setSelected(PersistenceApi.JDO == supportedSelectedApi);
                    DNEConfigFormV10x.this.jPARadioButton.setSelected(PersistenceApi.JPA == supportedSelectedApi);
                    DNEConfigFormV10x.this.jDORadioButton.setEnabled(selectedEnhancerSupport.isSupported(PersistenceApi.JDO));
                    DNEConfigFormV10x.this.jPARadioButton.setEnabled(selectedEnhancerSupport.isSupported(PersistenceApi.JPA));
                    DNEConfigFormV10x.this.persistenceImplComboBox.setSelectedItem(selectedEnhancerSupport.getName());

                    DNEConfigFormV10x.this.configPanel.repaint();
                }
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
                final String text = DNEConfigFormV10x.this.metadataExtensionTextField.getText();
                final String trimmedText = text.trim();
                final boolean isEmpty = trimmedText.isEmpty();
                DNEConfigFormV10x.this.addToCompilerResourceCheckBox
                        .setEnabled(!isEmpty);
            }
        });
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

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        configPanel = new JPanel();
        configPanel.setLayout(new GridLayoutManager(5, 3, new Insets(0, 0, 0, 0), -1, -1));
        enableEnhancerCheckBox = new JCheckBox();
        enableEnhancerCheckBox.setText("Enable Enhancer");
        configPanel.add(enableEnhancerCheckBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                                    GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                    | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                                                    null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        configPanel.add(panel1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null,
                                                    null, null, 0, false));
        jDORadioButton = new JRadioButton();
        jDORadioButton.setText("JDO");
        panel1.add(jDORadioButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                       GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                       GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jPARadioButton = new JRadioButton();
        jPARadioButton.setText("JPA");
        panel1.add(jPARadioButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                       GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                       GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                                GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                                false));
        configPanel.add(persistenceImplComboBox,
                        new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                                            GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                            false));
        final JLabel label1 = new JLabel();
        label1.setText(" Metadata file extensions (use ';' to separate)");
        configPanel.add(label1, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                    GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                                    false));
        includeTestClassesCheckBox = new JCheckBox();
        includeTestClassesCheckBox.setText("Include Test classes");
        configPanel.add(includeTestClassesCheckBox, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                                                        GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                        | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                        GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        metadataExtensionTextField.setAlignmentX(0.5f);
        metadataExtensionTextField.setAutoscrolls(true);
        metadataExtensionTextField.setMargin(new Insets(1, 1, 1, 1));
        configPanel.add(metadataExtensionTextField,
                        new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                                            GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
                                            new Dimension(150, -1), null, 0, false));
        addToCompilerResourceCheckBox = new JCheckBox();
        addToCompilerResourceCheckBox.setText("Add to compiler resource patterns");
        configPanel.add(addToCompilerResourceCheckBox,
                        new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                            GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        indexNotReadyPanel = new JPanel();
        indexNotReadyPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        configPanel.add(indexNotReadyPanel, new GridConstraints(3, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Please wait until indexing is finished");
        indexNotReadyPanel.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                                                           GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null,
                                                           null, 0, false));
        contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        configPanel.add(contentPanel, new GridConstraints(4, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null,
                                                          null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPanel.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                     GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                     GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null,
                                                     null, null, 0, false));
        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Affected Modules"));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel2.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null,
                                                    null, 0, false));
        affectedModulesTable = new JTable();
        affectedModulesTable.setEnabled(true);
        affectedModulesTable.setFillsViewportHeight(false);
        affectedModulesTable.setPreferredScrollableViewportSize(new Dimension(450, 30));
        scrollPane1.setViewportView(affectedModulesTable);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPanel.add(panel3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                     GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                     GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null,
                                                     null, null, 0, false));
        panel3.setBorder(BorderFactory.createTitledBorder("Metadata and annotated classes for enhancement"));
        infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(infoPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                  GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                  GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null,
                                                  null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Please click 'Make Project' to see affected files");
        infoPanel.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                                                  GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0,
                                                  false));
        metaDataAndClassesScrollPane = new JScrollPane();
        panel3.add(metaDataAndClassesScrollPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                                                     GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                     | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                                     GridConstraints.SIZEPOLICY_CAN_SHRINK
                                                                     | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        metadataAndClassesTable = new JTable();
        metadataAndClassesTable.setFillsViewportHeight(false);
        metadataAndClassesTable.setFont(new Font(metadataAndClassesTable.getFont().getName(), metadataAndClassesTable.getFont().getStyle(),
                                                 metadataAndClassesTable.getFont().getSize()));
        metadataAndClassesTable.setPreferredScrollableViewportSize(new Dimension(450, 100));
        metaDataAndClassesScrollPane.setViewportView(metadataAndClassesTable);
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(jDORadioButton);
        buttonGroup.add(jPARadioButton);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() { return configPanel; }
}
