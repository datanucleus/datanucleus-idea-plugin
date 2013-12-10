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

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.Project;

/**
 * Action to be shown in and triggered by IDEA's 'Build' dialogue.<br/>
 * Build->DataNucleus Enhancer
 */
public class DNEToggleEnableAction extends ToggleAction {

    @Override
    public boolean isSelected(final AnActionEvent anActionEvent) {
        final DNEProjectComponent dNEProjectComponent = getDataNucleusEnhancerComponent(anActionEvent);
        final DNEPersistentState dNEProjectComponentState = dNEProjectComponent == null ? null : dNEProjectComponent.getState();
        return dNEProjectComponentState != null && dNEProjectComponentState.isEnhancerEnabled();
    }

    @Override
    public void setSelected(final AnActionEvent anActionEvent, final boolean b) {
        final DNEProjectComponent dNEProjectComponent = getDataNucleusEnhancerComponent(anActionEvent);
        if (dNEProjectComponent != null) {
            dNEProjectComponent.setEnhancerEnabled(b);
        }
    }

    private static DNEProjectComponent getDataNucleusEnhancerComponent(final AnActionEvent anActionEvent) {
        final Project project = getProject(anActionEvent);
        return project == null ? null : project.getComponent(DNEProjectComponent.class);
    }

    private static Project getProject(final AnActionEvent anActionEvent) {
        final Project project = PlatformDataKeys.PROJECT.getData(anActionEvent.getDataContext());
        return project;
    }

}
