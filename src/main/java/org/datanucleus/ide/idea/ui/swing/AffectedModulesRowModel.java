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

import javax.swing.table.AbstractTableModel;
import java.util.List;

import org.datanucleus.ide.idea.ui.AffectedModule;

/**
 */
public class AffectedModulesRowModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private final List<AffectedModule> affectedModules;

    public AffectedModulesRowModel(final List<AffectedModule> affectedModules) {
        this.affectedModules = affectedModules;
    }

    public List<AffectedModule> getAffectedModules() {
        return this.affectedModules;
    }

    public int getRowCount() {
        return this.affectedModules.size();
    }

    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(final int columnIndex) {
        return columnIndex == 0 ? "Enabled" : "Affected Project Module";
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        return columnIndex == 0 ? Boolean.class : String.class;
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return columnIndex == 0;
    }

    public Object getValueAt(final int rowIndex, final int columnIndex) {
        final AffectedModule am = this.affectedModules.get(rowIndex);
        return columnIndex == 0 ? am.isEnabled() : am.getName();
    }

    @Override
    public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
        if (columnIndex == 0) {
            final AffectedModule am = this.affectedModules.get(rowIndex);
            am.setEnabled((Boolean) aValue);
            this.fireTableCellUpdated(rowIndex, columnIndex);
        }
    }

}
