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

import javax.swing.JList;

import com.intellij.ide.presentation.VirtualFilePresentation;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;

import org.datanucleus.ide.idea.util.VirtualFileUtils;

public class EnhancedFileListRenderer extends ColoredListCellRenderer {

    @SuppressWarnings("MagicCharacter")
    @Override
    protected void customizeCellRenderer(final JList list, final Object value, final int index, final boolean selected, final boolean hasFocus) {
        // paint selection only as a focus rectangle
        this.mySelected = false;
        setBackground(null);
        final VirtualFile vf = (VirtualFile) value;
        setIcon(VirtualFilePresentation.getIcon(vf));
        if (VirtualFileUtils.existsOnFilesystem(vf)) {
            append(vf.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        } else {
            append(vf.getName() + "  [File does not exist]", SimpleTextAttributes.ERROR_ATTRIBUTES);
        }
        final VirtualFile parent = vf.getParent();
        if (parent != null) {
            append(" (" + FileUtil.toSystemDependentName(parent.getPath()) + ')', SimpleTextAttributes.GRAY_ATTRIBUTES);
        }
    }

}
