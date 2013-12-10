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

package org.datanucleus.ide.idea.integration.datanuculeus;

import org.datanucleus.ide.idea.PersistenceApi;
import org.datanucleus.ide.idea.integration.AbstractEnhancerSupport;
import org.datanucleus.ide.idea.integration.EnhancerSupportVersion;
import org.jetbrains.annotations.NotNull;

/**
 */
public class EnhancerSupportDatanucleus extends AbstractEnhancerSupport {

    private static final String ID = "DATANUCLEUS";

    private static final String NAME = "DataNucleus";

    //
    // Interface implementation
    //

    @SuppressWarnings("RefusedBequest")
    @Override
    @NotNull
    public EnhancerSupportVersion getVersion() {
        return EnhancerSupportVersion.V1_1_X;
    }

    /**
     * The name to display in the configuration dialog enhancer support drop-down.
     *
     * @return Enhancer support name
     */
    @Override
    @NotNull
    public String getId() {
        return ID;
    }

    /**
     * The name to display in the configuration dialog enhancer support drop-down.
     *
     * @return Enhancer support name
     */
    @Override
    @NotNull
    public String getName() {
        return NAME;
    }

    @Override
    @NotNull
    public String[] getEnhancerClassNames() {
        return new String[] {EnhancerProxyDataNucleus.NUCLEUS_ENHANCER_CLASS, EnhancerProxyDataNucleus.NUCLEUS_GENERIC_ENHANCER_CLASS};
    }

    @Override
    @NotNull
    public PersistenceApi[] getPersistenceApis() {
        return new PersistenceApi[] {PersistenceApi.JPA, PersistenceApi.JDO};
    }

    @Override
    @NotNull
    public Class<?> getEnhancerProxyClass() {
        return EnhancerProxyDataNucleus.class;
    }

}
