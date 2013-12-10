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
import java.util.Collection;
import java.util.List;

import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.module.Module;

import org.datanucleus.ide.idea.PersistenceApi;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface to implement for every new enhancer to support.<br/>
 * See {@link AbstractEnhancerSupport}.
 */
public interface EnhancerSupport {

    public static final String EXTENSION_POINT_NAME = "DataNucleusIntegration.datanucleusEnhancerExtension";

    /**
     * Version of enhancer support.<br/>
     * <br/>
     * Used to determine which features are supported.
     *
     * @return .
     */
    @NotNull
    public EnhancerSupportVersion getVersion();

    /**
     * The name to display in the configuration dialog enhancer support drop-down.
     *
     * @return Enhancer support name
     */
    @NotNull
    public String getId();

    /**
     * The name to display in the configuration dialog enhancer support drop-down.
     *
     * @return Enhancer support name
     */
    @NotNull
    public String getName();

    /**
     * API's supported by this enhancer integration, also see {@link org.datanucleus.ide.idea.PersistenceApi}.
     *
     * @return supported API's
     */
    @NotNull
    public PersistenceApi[] getPersistenceApis();

    /**
     * Checks if provided persistence api is supported by actual enhancer integration.
     *
     * @param persistenceApi the api to check support for
     * @return true if supported
     */
    public boolean isSupported(PersistenceApi persistenceApi);

    /**
     * Get the class name of the enhancer proxy.<br/>
     * <br/>
     * Used to instantiate the proxies.
     *
     * @return the proxy class
     */
    @NotNull
    public Class<?> getEnhancerProxyClass();

    /**
     * Get the default persistence api.
     *
     * @return .
     */
    @NotNull
    public PersistenceApi getDefaultPersistenceApi();

    /**
     * Persistence implementations may use different enhancer classes per API.<br/>
     * This method delivers all fully qualified class names of enhancer classes to be used for enhancement.<br/>
     * <br/>
     * By now this classes are only used to find enhancer support in project modules.<br/>
     *
     * @return Array of fully qualified enhancer class names
     */
    @NotNull
    public String[] getEnhancerClassNames();

    /**
     * Annotations this enhancer implementation supports.
     *
     * @return List of fully qualified annotation class names
     */
    @NotNull
    public List<String> getAnnotationNames();

    /**
     * Interface every V1.1.x enhancer proxy has to implement, see {@link org.datanucleus.ide.idea.integration.EnhancerProxy}.
     *
     * @param enhancerContext Actual enhancer context
     * @return Proxy to the selected enhancer
     * @throws NoSuchMethodException  .
     * @throws java.lang.reflect.InvocationTargetException
     *                                .
     * @throws IllegalAccessException .
     * @throws InstantiationException .
     */
    @NotNull
    public EnhancerProxy newEnhancerProxy(EnhancerContext enhancerContext)
            throws NoSuchMethodException,
                   InvocationTargetException,
                   IllegalAccessException,
                   InstantiationException;

    /**
     * Interface every V1.0.x enhancer proxy has to implement, see {@link org.datanucleus.ide.idea.integration.EnhancerProxy}.
     *
     * @param api                 Persistence API used to enhance classes with
     * @param compileContext      IntelliJ IDEA compile context
     * @param module              Module to enhance in
     * @param persistenceUnitName Optional persistence unit name (if null is provided, all persistence capable classes have to be enhanced)
     * @return Proxy to the selected enhancer
     * @throws NoSuchMethodException  .
     * @throws java.lang.reflect.InvocationTargetException
     *                                .
     * @throws IllegalAccessException .
     * @throws InstantiationException .
     * @deprecated use {@link #newEnhancerProxy(EnhancerContext)} instead
     */
    @Deprecated
    @NotNull
    public EnhancerProxy newEnhancerProxy(PersistenceApi api,
                                          CompileContext compileContext,
                                          Module module,
                                          @Nullable String persistenceUnitName)
            throws NoSuchMethodException,
                   InvocationTargetException,
                   IllegalAccessException,
                   InstantiationException;

    /**
     *
     * @param compileContext       IntelliJ IDEA compile context
     * @param module               Module to enhance in
     * @param excludedDependencies Dependencies to be excluded
     * @param enhancerDependencies Manually set enhancer dependencies
     * @return .
     * @throws IOException
     */
    @NotNull
    public ClassLoader newClassLoader(@NotNull final CompileContext compileContext,
                                      @NotNull final Module module,
                                      @Nullable final Collection<String> excludedDependencies,
                                      @Nullable final Collection<String> enhancerDependencies) throws IOException;

}
