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
import java.lang.reflect.InvocationTargetException;

import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.module.Module;

import org.datanucleus.ide.idea.PersistenceApi;

/**
 */
public abstract class AbstractEnhancerProxy implements EnhancerProxy {

    private final EnhancerContext enhancerContext;

    // keeping, to stay backwards compatible with V1.0.x
    private final PersistenceApi api;

    // keeping, to stay backwards compatible with V1.0.x
    private final CompileContext compileContext;

    // keeping, to stay backwards compatible with V1.0.x
    private final Module module;

    // keeping, to stay backwards compatible with V1.0.x
    private final String persistenceUnitName;

    //
    // Constructor(s)
    //

    // V1.1.x Constructor
    @SuppressWarnings({"RedundantThrowsDeclaration", "FeatureEnvy"})
    protected AbstractEnhancerProxy(final EnhancerContext enhancerContext)
            throws IOException,
                   ClassNotFoundException,
                   IllegalAccessException,
                   InstantiationException,
                   InvocationTargetException,
                   NoSuchMethodException {

        this.enhancerContext = enhancerContext;
        this.api = null;
        this.compileContext = null;
        this.module = null;
        this.persistenceUnitName = null;
    }

    /**
     * @deprecated  V1.0.x Constructor, kept for backwards compatibility only!
     */
    @Deprecated
    @SuppressWarnings("RedundantThrowsDeclaration")
    protected AbstractEnhancerProxy(final PersistenceApi api,
                                    final CompileContext compileContext,
                                    final Module module,
                                    final String persistenceUnitName)
            throws IOException,
            ClassNotFoundException,
            IllegalAccessException,
            InstantiationException,
            InvocationTargetException,
            NoSuchMethodException {

        this.enhancerContext = null;
        this.api = api;
        this.compileContext = compileContext;
        this.module = module;
        this.persistenceUnitName = persistenceUnitName;
    }

    //
    // Accessor(s)
    //

    public final EnhancerContext getEnhancerContext() {
        if (this.enhancerContext == null) {
            throw new UnsupportedOperationException("This method is not supported by V1.0.x Enhancer Proxies");
        }
        return this.enhancerContext;
    }

    /**
     * @deprecated  V1.0.x Method, kept for backwards compatibility only!
     * @return .
     */
    @Deprecated
    public final PersistenceApi getApi() {
        if (this.api == null) {
            throw new UnsupportedOperationException("This method is not supported by V1.1.x Enhancer Proxies");
        }
        return this.api;
    }

    /**
     * @deprecated  V1.0.x Method, kept for backwards compatibility only!
     * @return .
     */
    @Deprecated
    public final Module getModule() {
        if (this.module == null) {
            throw new UnsupportedOperationException("This method is not supported by V1.1.x Enhancer Proxies");
        }
        return this.module;
    }

    /**
     * @deprecated  V1.0.x Method, kept for backwards compatibility only!
     * @return .
     */
    @Deprecated
    public final String getPersistenceUnitName() {
        if (this.persistenceUnitName == null) {
            throw new UnsupportedOperationException("This method is not supported by V1.1.x Enhancer Proxies");
        }
        return this.persistenceUnitName;
    }

}
