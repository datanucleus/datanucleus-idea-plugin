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

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.datanucleus.ide.idea.integration.EnhancerSupport;
import org.datanucleus.ide.idea.integration.datanuculeus.EnhancerSupportDatanucleus;
import org.jetbrains.annotations.NotNull;

/**
 */
class EnhancerSupportRegistryDefault implements EnhancerSupportRegistry {

    private static final EnhancerSupportRegistry instance = new EnhancerSupportRegistryDefault();

    public static final EnhancerSupport DEFAULT_ENHANCER_SUPPORT = new EnhancerSupportDatanucleus();

    private final Map<String, EnhancerSupport> supported = new HashMap<String, EnhancerSupport>();

    public static EnhancerSupportRegistry getInstance() {
        return instance;
    }

    @Override
    @SuppressWarnings("MagicCharacter")
    @NotNull
    public EnhancerSupport getEnhancerSupportById(@NotNull final String id) {
        final EnhancerSupport enhancerSupport = this.supported.get(id);
        Validate.notNull(enhancerSupport, "no enhancer support for id '" + id + '\'');
        return enhancerSupport;
    }

    @Override
    public boolean isRegistered(@NotNull final String id) {
        return this.supported.get(id) != null;
    }

    @Override
    @NotNull
    public EnhancerSupport getDefaultEnhancerSupport() {
        return DEFAULT_ENHANCER_SUPPORT;
    }

    @Override
    @NotNull
    public Set<EnhancerSupport> getSupportedEnhancers() {
        return new LinkedHashSet<EnhancerSupport>(this.supported.values());
    }

    @Override
    public void registerEnhancerSupport(@NotNull final EnhancerSupport enhancerSupport) {
        final String id = enhancerSupport.getId();
        this.supported.put(id, enhancerSupport);
    }

    @Override
    public void unRegisterEnhanderSupport(@NotNull final EnhancerSupport enhancerSupport) {
        final String id = enhancerSupport.getId();
        this.supported.remove(id);
    }

}
