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

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.datanucleus.ide.idea.ui.MetaDataOrClassFile;

/**
 */
public class MetadataOrClassFilesRowModel implements TableModel {

    private final List<MetaDataOrClassFile> files = new ArrayList<MetaDataOrClassFile>();

    public MetadataOrClassFilesRowModel(final Collection<MetaDataOrClassFile> metadataFiles,
                                        final Collection<MetaDataOrClassFile> annotatedFiles) {
        if (metadataFiles != null && !metadataFiles.isEmpty()) {
            this.files.addAll(metadataFiles);
        }
        if (annotatedFiles != null && !annotatedFiles.isEmpty()) {
            this.files.addAll(annotatedFiles);
        }
    }

    @Override
    public int getRowCount() {
        return this.files.size();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public String getColumnName(final int columnIndex) {
        final String columnName;
        switch (columnIndex) {
            case (0):
                columnName = "Module";
                break;
            case (1):
                columnName = "Class";
                break;
            case (2):
                columnName = "File";
                break;
            case (3):
                columnName = "Path";
                break;
            default:
                throw new IllegalArgumentException("invalid column index for retrieving name: " + columnIndex);
        }
        return columnName;
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return false;
    }

    @SuppressWarnings("FeatureEnvy")
    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        final MetaDataOrClassFile moc = this.files.get(rowIndex);

        final String ret;
        switch (columnIndex) {
            case (0):
                ret = moc.getModuleName();
                break;
            case (1):
                ret = moc.getClassName();
                break;
            case (2):
                ret = moc.getFileName();
                break;
            case (3):
                ret = moc.getPath();
                break;
            default:
                throw new IllegalArgumentException("invalid column index for retrieving value: " + columnIndex);
        }
        return ret;
    }

    @Override
    public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
        // do nothing
    }

    @Override
    public void addTableModelListener(final TableModelListener l) {
        // do nothing
    }

    @Override
    public void removeTableModelListener(final TableModelListener l) {
        // do nothing
    }

}
