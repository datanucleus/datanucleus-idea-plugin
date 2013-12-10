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

package org.datanucleus.ide.idea.ui.swing;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Taken from http://tips4java.wordpress.com/2008/11/10/table-column-adjuster/
 *
 *	Class to manage the widths of colunmns in a table.
 *
 *  Various properties control how the width of the column is calculated.
 *  Another property controls whether column width calculation should be dynamic.
 *  Finally, various Actions will be added to the table to allow the user
 *  to customize the functionality.
 *
 *  This class was designed to be used with tables that use an auto resize mode
 *  of AUTO_RESIZE_OFF. With all other modes you are constrained as the width
 *  of the columns must fit inside the table. So if you increase one column, one
 *  or more of the other columns must decrease. Because of this the resize mode
 *  of RESIZE_ALL_COLUMNS will work the best.
 */
public class ColumnAdjuster implements PropertyChangeListener, TableModelListener, Serializable {

    private static final long serialVersionUID = 1L;

    private final JTable table;

    private final int spacing;

    private boolean isColumnHeaderIncluded = false;

    private boolean isColumnDataIncluded = false;

    private boolean isOnlyAdjustLarger = false;

    private boolean isDynamicAdjustment = false;

    private final Map<TableColumn, Integer> columnSizes = new HashMap<TableColumn, Integer>();

    /*
      *  Specify the table and use default spacing
      */
    public ColumnAdjuster(final JTable table) {
        this(table, 6);
    }

    /*
      *  Specify the table and spacing
      */
    public ColumnAdjuster(final JTable table, final int spacing) {
        this.table = table;
        this.spacing = spacing;
        setColumnHeaderIncluded(true);
        setColumnDataIncluded(true);
        setOnlyAdjustLarger(true);
        setDynamicAdjustment(false);
        installActions();
    }

    /*
      *  Adjust the widths of all the columns in the table
      */
    public void adjustColumns() {
        final TableColumnModel tcm = this.table.getColumnModel();

        for (int i = 0; i < tcm.getColumnCount(); i++) {
            adjustColumn(i);
        }
    }

    /*
      *  Adjust the width of the specified column in the table
      */
    public void adjustColumn(final int column) {
        final TableColumn tableColumn = this.table.getColumnModel().getColumn(column);

        if (!tableColumn.getResizable()) { return; }

        final int columnHeaderWidth = getColumnHeaderWidth(column);
        final int columnDataWidth = getColumnDataWidth(column);
        final int preferredWidth = Math.max(columnHeaderWidth, columnDataWidth);

        updateTableColumn(column, preferredWidth);
    }

    /*
      *  Calculated the width based on the column name
      */
    private int getColumnHeaderWidth(final int column) {
        if (!this.isColumnHeaderIncluded) { return 0; }

        final TableColumn tableColumn = this.table.getColumnModel().getColumn(column);
        final Object value = tableColumn.getHeaderValue();
        TableCellRenderer renderer = tableColumn.getHeaderRenderer();

        if (renderer == null) {
            renderer = this.table.getTableHeader().getDefaultRenderer();
        }

        final Component c = renderer.getTableCellRendererComponent(this.table, value, false, false, -1, column);
        return c.getPreferredSize().width;
    }

    /*
      *  Calculate the width based on the widest cell renderer for the
      *  given column.
      */
    private int getColumnDataWidth(final int column) {
        if (!this.isColumnDataIncluded) { return 0; }

        int preferredWidth = 0;
        final int maxWidth = this.table.getColumnModel().getColumn(column).getMaxWidth();

        for (int row = 0; row < this.table.getRowCount(); row++) {
            preferredWidth = Math.max(preferredWidth, getCellDataWidth(row, column));

            //  We've exceeded the maximum width, no need to check other rows

            if (preferredWidth >= maxWidth) { break; }
        }

        return preferredWidth;
    }

    /*
      *  Get the preferred width for the specified cell
      */
    private int getCellDataWidth(final int row, final int column) {
        //  Inovke the renderer for the cell to calculate the preferred width

        final TableCellRenderer cellRenderer = this.table.getCellRenderer(row, column);
        final Component c = this.table.prepareRenderer(cellRenderer, row, column);
        final int width = c.getPreferredSize().width + this.table.getIntercellSpacing().width;

        return width;
    }

    /*
      *  Update the TableColumn with the newly calculated width
      */
    private void updateTableColumn(final int column, final int width) {
        int width1 = width;
        final TableColumn tableColumn = this.table.getColumnModel().getColumn(column);

        if (!tableColumn.getResizable()) { return; }

        width1 += this.spacing;

        //  Don't shrink the column width

        if (this.isOnlyAdjustLarger) {
            width1 = Math.max(width1, tableColumn.getPreferredWidth());
        }

        this.columnSizes.put(tableColumn, tableColumn.getWidth());
        this.table.getTableHeader().setResizingColumn(tableColumn);
        tableColumn.setWidth(width1);
    }

    /*
      *  Restore the widths of the columns in the table to its previous width
      */
    public void restoreColumns() {
        final TableColumnModel tcm = this.table.getColumnModel();

        for (int i = 0; i < tcm.getColumnCount(); i++) {
            restoreColumn(i);
        }
    }

    /*
      *  Restore the width of the specified column to its previous width
      */
    private void restoreColumn(final int column) {
        final TableColumn tableColumn = this.table.getColumnModel().getColumn(column);
        final Integer width = this.columnSizes.get(tableColumn);

        if (width != null) {
            this.table.getTableHeader().setResizingColumn(tableColumn);
            tableColumn.setWidth(width);
        }
    }

    /*
      *	Indicates whether to include the header in the width calculation
      */
    public void setColumnHeaderIncluded(final boolean isColumnHeaderIncluded) {
        this.isColumnHeaderIncluded = isColumnHeaderIncluded;
    }

    /*
      *	Indicates whether to include the model data in the width calculation
      */
    public void setColumnDataIncluded(final boolean isColumnDataIncluded) {
        this.isColumnDataIncluded = isColumnDataIncluded;
    }

    /*
      *	Indicates whether columns can only be increased in size
      */
    public void setOnlyAdjustLarger(final boolean isOnlyAdjustLarger) {
        this.isOnlyAdjustLarger = isOnlyAdjustLarger;
    }

    /*
      *  Indicate whether changes to the model should cause the width to be
      *  dynamically recalculated.
      */
    public void setDynamicAdjustment(final boolean isDynamicAdjustment) {
        //  May need to add or remove the TableModelListener when changed

        if (this.isDynamicAdjustment != isDynamicAdjustment) {
            if (isDynamicAdjustment) {
                this.table.addPropertyChangeListener(this);
                this.table.getModel().addTableModelListener(this);
            } else {
                this.table.removePropertyChangeListener(this);
                this.table.getModel().removeTableModelListener(this);
            }
        }

        this.isDynamicAdjustment = isDynamicAdjustment;
    }

    //
    //  PropertyChangeListener Interface
    //

    @Override
    public void propertyChange(final PropertyChangeEvent e) {
        //  When the TableModel changes we need to update the listeners
        //  and column widths

        if ("model".equals(e.getPropertyName())) {
            TableModel model = (TableModel) e.getOldValue();
            model.removeTableModelListener(this);

            model = (TableModel) e.getNewValue();
            model.addTableModelListener(this);
            adjustColumns();
        }
    }

    //
    //  TableModelListener interface
    //

    @Override
    public void tableChanged(final TableModelEvent e) {
        if (!this.isColumnDataIncluded) { return; }

        //  A cell has been updated

        if (e.getType() == TableModelEvent.UPDATE) {
            final int column = this.table.convertColumnIndexToView(e.getColumn());

            //  Only need to worry about an increase in width for this cell

            if (this.isOnlyAdjustLarger) {
                final int row = e.getFirstRow();
                final TableColumn tableColumn = this.table.getColumnModel().getColumn(column);

                if (tableColumn.getResizable()) {
                    final int width = getCellDataWidth(row, column);
                    updateTableColumn(column, width);
                }
            }

            //	Could be an increase of decrease so check all rows

            else {
                adjustColumn(column);
            }
        }

        //  The update affected more than one column so adjust all columns

        else {
            adjustColumns();
        }
    }

    /*
      *  Install Actions to give user control of certain functionality.
      */
    private void installActions() {
        installColumnAction(true, true, "adjustColumn", "control ADD");
        installColumnAction(false, true, "adjustColumns", "control shift ADD");
        installColumnAction(true, false, "restoreColumn", "control SUBTRACT");
        installColumnAction(false, false, "restoreColumns", "control shift SUBTRACT");

        installToggleAction(true, false, "toggleDynamic", "control MULTIPLY");
        installToggleAction(false, true, "toggleLarger", "control DIVIDE");
    }

    /*
      *  Update the input and action maps with a new ColumnAction
      */
    private void installColumnAction(
            final boolean isSelectedColumn, final boolean isAdjust, final String key, final String keyStroke) {
        final Action action = new ColumnAdjuster.ColumnAction(isSelectedColumn, isAdjust);
        final KeyStroke ks = KeyStroke.getKeyStroke(keyStroke);
        this.table.getInputMap().put(ks, key);
        this.table.getActionMap().put(key, action);
    }

    /*
      *  Update the input and action maps with new ToggleAction
      */
    private void installToggleAction(
            final boolean isToggleDynamic, final boolean isToggleLarger, final String key, final String keyStroke) {
        final Action action = new ColumnAdjuster.ToggleAction(isToggleDynamic, isToggleLarger);
        final KeyStroke ks = KeyStroke.getKeyStroke(keyStroke);
        this.table.getInputMap().put(ks, key);
        this.table.getActionMap().put(key, action);
    }

    /*
      *  Action to adjust or restore the width of a single column or all columns
      */
    @SuppressWarnings("CloneableClassInSecureContext")
    class ColumnAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        private final boolean isSelectedColumn;

        private final boolean isAdjust;

        ColumnAction(final boolean isSelectedColumn, final boolean isAdjust) {
            this.isSelectedColumn = isSelectedColumn;
            this.isAdjust = isAdjust;
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            //  Handle selected column(s) width change actions

            if (this.isSelectedColumn) {
                final int[] columns = ColumnAdjuster.this.table.getSelectedColumns();

                for (final int column : columns) {
                    if (this.isAdjust) { adjustColumn(column); } else { restoreColumn(column); }
                }
            } else {
                if (this.isAdjust) { adjustColumns(); } else { restoreColumns(); }
            }
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

    }

    /*
      *  Toggle properties of the TableColumnAdjuster so the user can
      *  customize the functionality to their preferences
      */
    @SuppressWarnings("CloneableClassInSecureContext")
    class ToggleAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        private final boolean isToggleDynamic;

        private final boolean isToggleLarger;

        ToggleAction(final boolean isToggleDynamic, final boolean isToggleLarger) {
            this.isToggleDynamic = isToggleDynamic;
            this.isToggleLarger = isToggleLarger;
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            if (this.isToggleDynamic) {
                setDynamicAdjustment(!ColumnAdjuster.this.isDynamicAdjustment);
                return;
            }

            if (this.isToggleLarger) {
                setOnlyAdjustLarger(!ColumnAdjuster.this.isOnlyAdjustLarger);
            }
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

    }

}
