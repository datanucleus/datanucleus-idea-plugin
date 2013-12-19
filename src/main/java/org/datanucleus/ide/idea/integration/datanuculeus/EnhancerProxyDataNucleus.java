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

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.datanucleus.ide.idea.PersistenceApi;
import org.datanucleus.ide.idea.integration.AbstractEnhancerProxy;
import org.datanucleus.ide.idea.integration.EnhancerContext;
import org.datanucleus.ide.idea.util.InternalReflectionHelper;

/**
 * Proxy for the DataNucleus enhancer.
 */
public class EnhancerProxyDataNucleus extends AbstractEnhancerProxy {

    private static final Class[] NO_PARAMETER_TYPES = {};

    public static final String NUCLEUS_ENHANCER_CLASS            = "JDODataNucleusEnhancer";

    public static final String NUCLEUS_ENHANCER_CLASS_FQ         = "org.datanucleus.api.jdo." + NUCLEUS_ENHANCER_CLASS;

    public static final String NUCLEUS_GENERIC_ENHANCER_CLASS    = "DataNucleusEnhancer";

    public static final String NUCLEUS_GENERIC_ENHANCER_CLASS_FQ = "org.datanucleus.enhancer." + NUCLEUS_GENERIC_ENHANCER_CLASS;

    private final Object enhancer;

    //
    // Constructor
    //

    // Using V1.1.x constructor
    public EnhancerProxyDataNucleus(final EnhancerContext enhancerContext)
            throws IOException,
                   ClassNotFoundException,
                   IllegalAccessException,
                   InstantiationException,
                   InvocationTargetException,
                   NoSuchMethodException {

        super(enhancerContext);

        final ClassLoader classLoader = enhancerContext.getClassLoader();
        final PersistenceApi persistenceApi = enhancerContext.getPersistenceApi();

        final Class<?> enhancerClass = Class.forName(NUCLEUS_GENERIC_ENHANCER_CLASS_FQ, true, classLoader);
        final boolean is31 = is31(enhancerClass);
        if (is31) {
            final Constructor<?> constructor = enhancerClass.getConstructor(String.class);
            this.enhancer = constructor.newInstance(persistenceApi.name());
        } else {
            final Constructor<?> constructor = enhancerClass.getConstructor(String.class, String.class);
            this.enhancer = constructor.newInstance(persistenceApi.name(), "ASM");
        }
        // log to system out
        this.invokeMethod("setVerbose", new Class[] {Boolean.TYPE}, true);

        this.invokeMethod("setSystemOut", new Class[] {Boolean.TYPE}, true);
        this.invokeMethod("setClassLoader", new Class[] {ClassLoader.class}, classLoader);
    }

    //
    // Method implementation
    //

    @Override
    public void addClasses(final String... classNames) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        final Object parameter = convertVarargsParameter(classNames);
        final Object parameterClassesArray = Array.newInstance(String.class, 0);

        invokeMethod("addClasses", new Class[]{parameterClassesArray.getClass()}, parameter);
    }

    @Override
    public void addMetadataFiles(final String... metadataFiles) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        final Object parameter = convertVarargsParameter(metadataFiles);
        final Object parameterClassesArray = Array.newInstance(String.class, 0);

        invokeMethod("addFiles", new Class[]{parameterClassesArray.getClass()}, parameter);
    }

    @Override
    public int enhance() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        return (Integer) invokeMethod("enhance", NO_PARAMETER_TYPES);
    }

    @Override
    public String toString() {
        return "EnhancerProxyDataNucleus";
    }

    //
    // Helper methods
    //

    private static boolean is31(final Class<?> enhancerClass) {
        final Constructor<?>[] constructors = enhancerClass.getConstructors();
        for (final Constructor<?> constructor : constructors) {
            final Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length == 1 && String.class.isAssignableFrom(parameterTypes[0])) {
                return true;
            }
        }
        return false;
    }

    private Object invokeMethod(final String methodName, final Class[] parameterTypes, final Object... parameters)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        return InternalReflectionHelper.invokeMethod(this.enhancer, methodName, parameterTypes, parameters);
    }

    private static Object convertVarargsParameter(final String[] metadataFiles) {
        final Object parameter = Array.newInstance(String.class, metadataFiles.length);
        for (int i = 0; i < metadataFiles.length; ++i) {
            Array.set(parameter, i, metadataFiles[i]);
        }
        return parameter;
    }

}
