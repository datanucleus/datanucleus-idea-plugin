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

import java.lang.reflect.InvocationTargetException;

/**
 * Classes implementing this interface have to ensure that an invoked enhancer is instantiated
 * inside it's own {@link java.lang.ClassLoader}<br/>
 * <br/>
 * DO NOT USE EXTERNAL LIBRARIES LIKE APACHE BEANUTILS OR IDEA's OWN REFLECTION/PROPERTY HELPERS because this can/will lead to ClassLoader
 * memory leaks due to PermGen Space exhaustion, as those implementations tend to cache class references even if they're not related
 * to their own {@link java.lang.ClassLoader} hierarchy.<br/>
 * <br/>
 * A pre-generated classloader is provided via {@link org.datanucleus.ide.idea.integration.EnhancerContext#getClassLoader()}.
 * If however needed, override {@link AbstractEnhancerSupport#newClassLoader(com.intellij.openapi.compiler.CompileContext, com.intellij.openapi.module.Module, java.util.Collection, java.util.Collection)} and use
 * {@link ClassLoaderFactory#newClassLoader(com.intellij.openapi.compiler.CompileContext, com.intellij.openapi.module.Module, Class, java.util.Collection, java.util.Collection)}
 * to instantiate new ClassLoaders, as they're strictly project-module-related and do not include other dependencies, which ensures
 * project-module autonomous enhancement.<br/>
 * <br/>
 * EVERY implementing class also has to provide a constructor defined by
 * {@link org.datanucleus.ide.idea.integration.AbstractEnhancerProxy#AbstractEnhancerProxy(org.datanucleus.ide.idea.PersistenceApi, com.intellij.openapi.compiler.CompileContext, com.intellij.openapi.module.Module, String)},
 * if this is not feasible {@link AbstractEnhancerSupport#newEnhancerProxy(EnhancerContext)} has to be overridden.
 */
public interface EnhancerProxy {

    /**
     * Add names of classes annotated by persistence related annotations (e.g. javax.jdo.annotations.PersistenceCapable,
     * javax.persistence.Entity javax.jdo.annotations.PersistenceAware,...)
     *
     * @param classNames Fully qualified names of classes to be enhanced
     * @throws InvocationTargetException .
     * @throws IllegalAccessException    .
     * @throws NoSuchMethodException     .
     */
    public void addClasses(String... classNames) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException;

    /**
     * Add paths (full path) of files containing xml metadata for classes to
     * be enhanced (Also add related classes via {@link #addClasses(String...)}).
     *
     * @param metadataFiles Full path names of files containing xml metadata.
     * @throws InvocationTargetException .
     * @throws IllegalAccessException    .
     * @throws NoSuchMethodException     .
     */
    public void addMetadataFiles(String... metadataFiles) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException;

    /**
     * Start enhancement of added classes and metadata files.<br/>
     * <br/>
     * Be sure to have added class- and metadata file names via {@link #addClasses(String...)} and {@link #addMetadataFiles(String...)}
     *
     * @return Number of classes enhanced in this process.
     * @throws InvocationTargetException .
     * @throws IllegalAccessException    .
     * @throws NoSuchMethodException     .
     * @throws ClassNotFoundException    .
     */
    public int enhance() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException;

}
