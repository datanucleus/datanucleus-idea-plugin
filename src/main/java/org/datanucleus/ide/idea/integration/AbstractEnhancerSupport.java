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

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.module.Module;

import org.datanucleus.ide.idea.PersistenceApi;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 */
public abstract class AbstractEnhancerSupport implements EnhancerSupport {

    @Override
    @NotNull
    public EnhancerSupportVersion getVersion() {
        // V1.0.x as default
        return EnhancerSupportVersion.V1_0_X;
    }

    @Override
    public boolean isSupported(final PersistenceApi persistenceApi) {
        final List<PersistenceApi> supported = Arrays.asList(this.getPersistenceApis());
        return supported.contains(persistenceApi);
    }

    @Override
    @NotNull
    public List<String> getAnnotationNames() {
        final List<String> annotationNames = new ArrayList<String>(5);

        for (final PersistenceApi persistenceApi : this.getPersistenceApis()) {
            annotationNames.addAll(Arrays.asList(persistenceApi.getAnnotationClassNames()));
        }

        return annotationNames;
    }

    @Override
    @NotNull
    public PersistenceApi getDefaultPersistenceApi() {
        return this.getPersistenceApis()[0];
    }

    @Deprecated
    @Override
    @NotNull
    public EnhancerProxy newEnhancerProxy(final PersistenceApi api,
                                          final CompileContext compileCtx,
                                          final Module module,
                                          @Nullable final String persistenceUnitName)
            throws NoSuchMethodException,
            InvocationTargetException,
            IllegalAccessException,
            InstantiationException {

        final Class<?> enhancerProxyClass = this.getEnhancerProxyClass();
        final Constructor<?> constructor = enhancerProxyClass
                .getConstructor(new Class[]{PersistenceApi.class, CompileContext.class, Module.class, String.class});

        return (EnhancerProxy) constructor.newInstance(api, compileCtx, module, persistenceUnitName);
    }

    @Override
    @NotNull
    public EnhancerProxy newEnhancerProxy(final EnhancerContext enhancerContext)
            throws NoSuchMethodException,
                   InvocationTargetException,
                   IllegalAccessException,
                   InstantiationException {

        final Class<?> enhancerProxyClass = this.getEnhancerProxyClass();
        final Constructor<?> constructor = enhancerProxyClass
                .getConstructor(new Class[]{EnhancerContext.class});

        return (EnhancerProxy) constructor.newInstance(enhancerContext);
    }

    @NotNull
    @Override
    public ClassLoader newClassLoader(@NotNull final CompileContext compileContext,
                                      @NotNull final Module module,
                                      @Nullable final Collection<String> excludedDependencies,
                                      @Nullable final Collection<String> enhancerDependencies) throws IOException {

        return ClassLoaderFactory.newClassLoader(compileContext,
                                                 module,
                                                 this.getEnhancerProxyClass(),
                                                 excludedDependencies,
                                                 enhancerDependencies);
    }

}
