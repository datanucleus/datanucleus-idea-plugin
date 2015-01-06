package org.datanucleus.ide.idea.ui.idea;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.intellij.CommonBundle;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.PanelWithButtons;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.ComponentWithEmptyText;
import com.intellij.util.ui.StatusText;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Panel with "Add" and "Delete" File buttons on the right side.<br/>
 * <br/>
 * Base implementation copied from {@link com.intellij.ui.AddDeleteListPanel}
 */
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

public abstract class AbstractAddDeletePanel<T> extends PanelWithButtons implements ComponentWithEmptyText {

    private static final long serialVersionUID = -7740893424756192196L;
    public static final String EVENT_INIT = "AddDeletePanelINIT";
    public static final String EVENT_ADD = "AddDeletePanelADD";
    public static final String EVENT_REMOVE = "AddDeletePanelREMOVE";

    //
    // Members
    //

    private final String myTitle;
    
    private final AbstractAddDeletePanel.ListSelectionEventTranslator translator;


    protected JButton myAddButton = new JButton(CommonBundle.message("button.add"));
    protected JButton myDeleteButton = new JButton(CommonBundle.message("button.delete"));

    protected DefaultListModel listModel;
    protected final JBList listComponent;

    //
    // Constructors
    //

    protected AbstractAddDeletePanel(@Nullable final String title,
                                     @Nullable final Collection<T> initialList) {
        super();
        this.listModel = this.createListModel();
        this.listComponent = new JBList(this.listModel);
        this.myTitle = title;
        this.resetListModel(initialList);
        this.listComponent.setCellRenderer(this.createListCellRenderer());
        this.initPanel();
        this.translator = new AbstractAddDeletePanel.ListSelectionEventTranslator(this.listModel);
    }

    //
    // Implementation
    //

    @Override
    protected void initPanel() {
        final ToolbarDecorator decorator = ToolbarDecorator.createDecorator(this.listComponent).disableUpAction().disableDownAction()
                .setAddAction(new AnActionButtonRunnable() {
                    public void run(final AnActionButton button) {
                        final List<T> itemsToAdd = findItemsToAdd();
                        AbstractAddDeletePanel.this.addElements(itemsToAdd);
                    }
                });
        customizeToolbarDecorator(decorator);
        setLayout(new BorderLayout());
        add(decorator.createPanel(), BorderLayout.CENTER);
        this.listComponent.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(final ListSelectionEvent e) {
                AbstractAddDeletePanel.this.translator.translate(e);
            }
        });
    }

    public StatusText getEmptyText() {
        return this.listComponent.getEmptyText();
    }

    @Nullable
    @Override
    protected String getLabelText() {
        return this.myTitle;
    }

    @NotNull
    @Override
    protected JButton[] createButtons() {
        return new JButton[]{this.myAddButton, this.myDeleteButton};
    }

    @NotNull
    @Override
    protected JComponent createMainComponent() {
        if (!this.listModel.isEmpty()) {
            this.listComponent.setSelectedIndex(0);
        }
        return ScrollPaneFactory.createScrollPane(this.listComponent);
    }

    public void clear() {
        this.listModel.clear();
    }

    public void resetListModel(@Nullable final Collection<T> newList) {
        this.clear();
        this.addElements(newList);
    }

    /**
     * Following events are emitted:<br/>
     * <br/>
     * {@link #EVENT_INIT}<br/>
     * {@link #EVENT_ADD}<br/>
     * {@link #EVENT_REMOVE}<br/>
     *
     * @param actionListener .
     */
    public void addActionListener(final ActionListener actionListener) {
        this.translator.addActionListener(actionListener);
    }

    public void removeActionListener(final ActionListener actionListener) {
        this.translator.removeActionListener(actionListener);
    }

    /**
     * Adds items in a null save manner (null entries are omitted)
     *
     * @param itemsToAdd .
     */
    protected void addElements(@Nullable final Collection<T> itemsToAdd) {
        if (itemsToAdd != null && !itemsToAdd.isEmpty()) {
            for (final T virtualFile : itemsToAdd) {
                this.addElement(virtualFile);
            }
        }
    }

    /**
     * Adds an item in a null save manner (null entries are omitted)
     *
     * @param itemToAdd .
     */
    protected void addElement(@Nullable final T itemToAdd) {
        if (itemToAdd != null) {
            this.listModel.addElement(itemToAdd);
            this.listComponent.setSelectedValue(itemToAdd, true);
        }
    }

    protected void customizeToolbarDecorator(@NotNull final ToolbarDecorator decorator) {
    }

    //
    // Methods that must be implemented by deriving classes
    //

    /**
     * Create the ListModel used for {@link JBList} initialization.<br/>
     * <br/>
     * This method is called only once on instance creation.
     *
     * @return a ListModel derived from {@link DefaultListModel}
     */
    @NotNull
    protected abstract DefaultListModel createListModel();

    /**
     * Create the CellRenderer used for {@link JBList} initialization.<br/>
     * <br/>
     * This method is called only once on instance creation.<br/>
     *
     * @return a cell renderer derived from {@link ListCellRenderer}
     */
    @NotNull
    protected abstract ListCellRenderer createListCellRenderer();

    /**
     * This method is called anytime the "add" button is pressed.<br/>
     * <br/>
     * Returned items must conform to the ListModel provided by {@link #createListModel()} and the
     * ListCellRenderer provided by {@link #createListCellRenderer()}!<br/>
     * HINT: you could open a file chooser or the like to retrieve items to add.
     *
     * @return {@link List} of items to add
     */
    @Nullable
    protected abstract List<T> findItemsToAdd();

    //
    // Helper methods (for deriving classes)
    //

    @SuppressWarnings("unchecked")
    @NotNull
    public List<T> getListItems() {
        final List<T> items = new ArrayList<T>(this.listModel.size());
        for (int i = 0; i < this.listModel.size(); i++) {
            items.add((T) this.listModel.getElementAt(i));
        }
        return items;
    }

    private static class ListSelectionEventTranslator {

        private final Collection<ActionListener> actionListeners = new ArrayList<ActionListener>();

        private final ListModel listModel;

        private Integer lastSize = null;

        private int evId = 0;

        private ListSelectionEventTranslator(final ListModel listModel) {
            this.listModel = listModel;
        }

        private void translate(final ListSelectionEvent actEvent) {
            final int actSize = this.listModel.getSize();
            if (this.lastSize == null && actSize == 0) {
                this.lastSize = actSize;
                this.fireEvent(actEvent, EVENT_INIT);
            } else if (this.lastSize == null || actSize > this.lastSize) {
                this.lastSize = actSize;
                this.fireEvent(actEvent, EVENT_ADD);
            } else if (actSize < this.lastSize) {
                this.lastSize = actSize;
                this.fireEvent(actEvent, EVENT_REMOVE);
            }
            if (this.lastSize < 0) {
                this.lastSize = 0;
            }
        }

        private void addActionListener(final ActionListener actionListener) {
            if (!this.actionListeners.contains(actionListener)) {
                this.actionListeners.add(actionListener);
            }
        }

        private void removeActionListener(final ActionListener actionListener) {
            if (this.actionListeners.contains(actionListener)) {
                this.actionListeners.remove(actionListener);
            }
        }

        private void removeAllActionListeners() {
            this.actionListeners.clear();
        }

        private void fireEvent(final ListSelectionEvent actEvent, final String command) {
            final ActionEvent ev = new ActionEvent(actEvent.getSource(), this.evId++, command);
            for (final ActionListener actionListener : this.actionListeners) {
                actionListener.actionPerformed(ev);
            }
        }
    }

}
