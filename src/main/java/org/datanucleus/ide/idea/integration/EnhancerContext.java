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

package org.datanucleus.ide.idea.integration;

import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.module.Module;

import org.datanucleus.ide.idea.PersistenceApi;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: geri
 * Date: 10.09.12
 * Time: 15:22
 */
public final class EnhancerContext {

    private final PersistenceApi persistenceApi;

    private final Module module;

    private final CompileContext compileContext;

    private final String persistenceUnitName;

    private final ClassLoader classLoader;

    public EnhancerContext(@NotNull final PersistenceApi persistenceApi,
                           @NotNull final Module module,
                           @NotNull final CompileContext compileContext,
                           @Nullable final String persistenceUnitName,
                           @NotNull final ClassLoader classLoader) {

        this.compileContext = compileContext;
        this.persistenceApi = persistenceApi;
        this.module = module;
        this.persistenceUnitName = persistenceUnitName;
        this.classLoader = classLoader;
    }

    public PersistenceApi getPersistenceApi() {
        return this.persistenceApi;
    }

    public Module getModule() {
        return this.module;
    }

    public CompileContext getCompileContext() {
        return this.compileContext;
    }

    public String getPersistenceUnitName() {
        return this.persistenceUnitName;
    }

    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

}
