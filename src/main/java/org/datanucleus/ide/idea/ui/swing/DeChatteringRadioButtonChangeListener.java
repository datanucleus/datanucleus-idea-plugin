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

import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * User: geri
 * Date: 09.09.12
 * Time: 23:17
 */
public abstract class DeChatteringRadioButtonChangeListener implements ChangeListener {

    private Boolean last = null;

    @Override
    public void stateChanged(final ChangeEvent e) {
        final JRadioButton source = (JRadioButton) e.getSource();
        final boolean selected = source.isSelected();
        if (this.last == null || this.last != selected) {
            this.last = selected;
            this.changed(e, source, selected);
        }
    }

    protected abstract void changed(ChangeEvent e, JRadioButton source, boolean selected);

}
