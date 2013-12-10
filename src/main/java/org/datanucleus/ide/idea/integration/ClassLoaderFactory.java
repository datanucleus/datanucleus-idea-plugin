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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.regex.Pattern;

import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ProjectRootsTraversing;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathsList;
import com.intellij.util.lang.UrlClassLoader;

import org.datanucleus.ide.idea.util.DNEFileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Factory for creating ClassLoaders restricted to each provided module's dependency scope.
 */
public abstract class ClassLoaderFactory {

    private static final Logger LOGGER = Logger.getInstance(ClassLoaderFactory.class.getName());

    private static final Pattern VERSION_PATTERN1 = Pattern.compile("-[0-9][a-zA-Z0-9]+$");
    private static final Pattern VERSION_PATTERN2 = Pattern.compile("\\.[0-9][a-zA-Z0-9]+$");
    private static final Pattern VERSION_PATTERN3 = Pattern.compile("-([0-9]\\.?)+");

    private static final Pattern[] VERSION_PATTERNS = {VERSION_PATTERN1, VERSION_PATTERN2, VERSION_PATTERN3};


    private ClassLoaderFactory() {
        // prohibit instantiation
    }

    /**
     * Convenience method for
     * {@link #newClassLoader(com.intellij.openapi.compiler.CompileContext, com.intellij.openapi.module.Module, Class, java.util.Collection, java.util.Collection)}
     */
    public static ClassLoader newClassLoader(final CompileContext compileContext, final Module module, final Class<?> proxyClass) throws IOException {
        return newClassLoader(compileContext, module, proxyClass, null, null);
    }

    /**
     * Creates a new {@link ClassLoader} that includes only the dependencies
     * and output dirs in the current module's compile context (includes module dependencies
     * and external jar dependencies).
     *
     * @param compileContext       .
     * @param module               .
     * @param proxyClass           the class of the proxy instantiating a new ClassLoader
     * @param excludedDependencies manually excluded dependencies
     * @param enhancerDependencies enhancer specific dependencies (equivalents in module dependencies will be excluded by simple base name approach)
     * @return .
     * @throws java.io.IOException .
     */
    @SuppressWarnings("deprecation") // want to stay backwards compatible at any cost
    public static ClassLoader newClassLoader(@NotNull final CompileContext compileContext,
                                             @NotNull final Module module,
                                             @Nullable final Class<?> proxyClass,
                                             @Nullable final Collection<String> excludedDependencies,
                                             @Nullable final Collection<String> enhancerDependencies) throws IOException {

        final Set<String> excludedDeps = createExcludedDependencies(excludedDependencies, enhancerDependencies);
        final Set<String> excludedEnhancerDepBaseNames = createExcludedEnhancerDepBaseNames(enhancerDependencies);

        final Collection<URL> urls = new LinkedList<URL>();

        // get urls from actual class loader to be able to instantiate executors
        final UrlClassLoader loader = (UrlClassLoader) (proxyClass == null ? ClassLoaderFactory.class.getClassLoader() : proxyClass.getClassLoader());
        urls.addAll(loader.getUrls());

        final PathsList paths = ProjectRootsTraversing.collectRoots(module, ProjectRootsTraversing.PROJECT_LIBRARIES);

        for (final VirtualFile vf : paths.getVirtualFiles()) {
            final File f = new File(vf.getPath());

            final String normalized = DNEFileUtils.normalizePath(f.getAbsolutePath());
            final String baseNameWithoutVersion = toBasenameWithoutVersion(normalized);
            if (!excludedDeps.contains(normalized) && !excludedEnhancerDepBaseNames.contains(baseNameWithoutVersion)) {
                final URI uri = f.toURI();
                final URL url = uri.toURL();
                urls.add(url);
            }
        }

        for (final VirtualFile vf : compileContext.getAllOutputDirectories()) {
            final File file = new File(vf.getPath());
            final File canonicalFile = file.getCanonicalFile();
            final URI uri = canonicalFile.toURI();
            final URL url = uri.toURL();
            urls.add(url);
        }

        if (enhancerDependencies != null && !enhancerDependencies.isEmpty()) {
            for (final String enhancerDependency : enhancerDependencies) {
                final File file = new File(enhancerDependency);
                final File canonicalFile = file.getCanonicalFile();
                final URI uri = canonicalFile.toURI();
                final URL url = uri.toURL();
                urls.add(url);
            }
        }

        LOGGER.info("ClassLoaderFactory: creating ClassLoader with classpath: " + urls);
        return new URLClassLoader(urls.toArray(new URL[urls.size()]));
    }

    //
    // Helper methods
    //

    private static Set<String> createExcludedDependencies(final Collection<String> excludedDependencies,
                                                          final Collection<String> enhancerDependencies) {
        Set<String> excluded = null;

        if (excludedDependencies != null && !excludedDependencies.isEmpty()) {
            excluded = new HashSet<String>();
            for (final String excludedDependency : excludedDependencies) {
                excluded.add(DNEFileUtils.normalizePath(excludedDependency));
            }
        }
        if (enhancerDependencies != null && !enhancerDependencies.isEmpty()) {
            if (excluded == null) {
                excluded = new HashSet<String>();
            }
            for (final String enhancerDependency : enhancerDependencies) {
                excluded.add(DNEFileUtils.normalizePath(enhancerDependency));
            }
        }
        if (excluded == null) {
            excluded = Collections.emptySet();
        }

        return excluded;
    }

    private static Set<String> createExcludedEnhancerDepBaseNames(final Collection<String> enhancerDependencies) {
        Set<String> excluded = null;

        if (enhancerDependencies != null && !enhancerDependencies.isEmpty()) {
            excluded = new HashSet<String>(enhancerDependencies.size());
            for (final String enhancerDependency : enhancerDependencies) {
                final String baseNameWithoutVersion = toBasenameWithoutVersion(enhancerDependency);

                excluded.add(baseNameWithoutVersion);
            }
        }

        if (excluded == null) {
            excluded = Collections.emptySet();
        }

        return excluded;
    }

    private static String toBasenameWithoutVersion(final String enhancerDependency) {
        final String baseName = DNEFileUtils.getFilenameBase(enhancerDependency);
        return removeVersion(baseName);
    }

    public static String removeVersion(final String baseName) {
        String reduced = baseName;
        if (baseName.toLowerCase().contains("datanucleus")) {
            // TODO: this is a pure hack!
            reduced = "datanucleus";
        } else if (baseName.toLowerCase().contains("openjpa")) {
            // TODO: this is a pure hack (OJ plugin)!
            reduced = "openjpa";
        } else {
            reduced = reduced.replace("-RELEASE", "");
            reduced = reduced.replace("-release", "");
            reduced = reduced.replace("-SNAPSHOT", "");
            reduced = reduced.replace("-snapshot", "");
            reduced = reduced.replace(".RELEASE", "");
            reduced = reduced.replace(".release", "");
            reduced = reduced.replace(".SNAPSHOT", "");
            reduced = reduced.replace(".snapshot", "");
            for (final Pattern versionPattern : VERSION_PATTERNS) {
                reduced = versionPattern.matcher(reduced).replaceAll("");
            }
        }
        return reduced;
    }

}
