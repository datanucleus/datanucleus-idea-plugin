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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.searches.AllClassesSearch;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.intellij.util.Query;

import org.datanucleus.ide.idea.integration.EnhancerSupport;

/**
 * Utility method collection for IDEA projects API.
 */
final class IdeaProjectUtils {

    //
    // Private constructor
    //

    private IdeaProjectUtils() {
        // no instantiation allowed
    }

    //
    // Utility methods
    //

    /**
     * Find modules containing the appropriate enhancer.
     *
     * @param enhancerSupport enum containing persistence implementation information, especially enhancer class names.
     * @param project         the project to search in
     * @return List of modules containing appropriate enhancers
     */
    static List<Module> getDefaultAffectedModules(final EnhancerSupport enhancerSupport, final Project project, final boolean manualDependencies) {
        final CompilerManager compilerManager = CompilerManager.getInstance(project);
        final CompileScope projectCompileScope = compilerManager.createProjectCompileScope(project);
        final Module[] compileScopeAffectedModules = projectCompileScope.getAffectedModules();
        final List<Module> affectedModules = new ArrayList<Module>(compileScopeAffectedModules.length);

        //
        // find enhancer class in module dependencies
        if (compileScopeAffectedModules.length > 0) {
            for (final Module affectedModule : compileScopeAffectedModules) {

                if (manualDependencies) {
                    affectedModules.add(affectedModule);
                } else {
                    // query for enhancer class
                    final Query<PsiClass> psiClassQuery = createEnhancerClassQuery(enhancerSupport, affectedModule);

                    // query returns results? -> enhancer present
                    if (psiClassQuery.findFirst() != null) {
                        affectedModules.add(affectedModule);
                    }
                }
            }
        }
        return affectedModules;
    }

    /**
     * Find virtual files by extension by traversing the directory tree.
     *
     * @param rootDir   the directory to start with
     * @param extension the extension to search for
     * @return List of virtual files matching the file extension
     */
    static List<VirtualFile> findFilesByExtension(final VirtualFile rootDir,
                                                  final String extension) {

        final List<VirtualFile> children = new LinkedList<VirtualFile>();

        // simply iterate directory for finding files
        for (final VirtualFile entry : rootDir.getChildren()) {
            if (entry.isDirectory()) {
                //
                // recursive invocation for directories
                children.addAll(findFilesByExtension(entry, extension));

            } else if (extension.equals(entry.getExtension())) {
                //
                // file with correct extension found
                children.add(entry);
            }
        }

        return children;
    }

    /**
     * Find classes annotated with persistence relevant annotations (depends on {@link org.datanucleus.ide.idea.integration.datanuculeus.EnhancerSupportDatanucleus}).
     *
     * @param enhancerSupport Enhancer integration to search classes for
     * @param module          Module to search in
     * @return List of virtual classes annotated with corresponding annotations
     */
    static List<PsiClass> findPersistenceAnnotatedClasses(final EnhancerSupport enhancerSupport, final Module module) {
        final List<PsiClass> annotatedClasses = new ArrayList<PsiClass>();

        for (final String annotationName : enhancerSupport.getAnnotationNames()) {
            final Collection<PsiClass> psiClasses = findAnnotatedClasses(module, annotationName);
            if (!psiClasses.isEmpty()) {
                annotatedClasses.addAll(psiClasses);
            }
        }

        return annotatedClasses;
    }

    /**
     * Find classes annotated with provided annotation.
     *
     * @param module              Module to search in
     * @param annotationClassName Fully qualified annotation class name
     * @return List of virtual classes annotated with corresponding annotation
     */
    static Collection<PsiClass> findAnnotatedClasses(final Module module, final String annotationClassName) {
        final PsiClass annotationPsiClass = findClassInModule(module, annotationClassName);
        final Collection<PsiClass> found;
        if (annotationPsiClass == null || !annotationPsiClass.isAnnotationType()) {
            found = Collections.emptyList();
        } else {
            found = findAnnotatedClasses(module, annotationPsiClass);
        }
        return found;
    }

    /**
     * Find classes annotated with provided annotation.
     *
     * @param module             Module to search in
     * @param annotationPsiClass Virtual class for annotation to search for
     * @return List of virtual classes annotated with corresponding annotation
     */
    static Collection<PsiClass> findAnnotatedClasses(final Module module, final PsiClass annotationPsiClass) {
        final Query<PsiClass> pcQuery = AnnotatedElementsSearch.searchPsiClasses(annotationPsiClass, module.getModuleScope());
        return pcQuery.findAll();
    }

    /**
     * Find a class via fully qualified name in module.
     *
     * @param module    Module to search in
     * @param className Fully qualified name of the class to search for
     * @return Corresponding class or null
     */
    static PsiClass findClassInModule(final Module module, final String className) {
        final JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(module.getProject());
        return javaPsiFacade.findClass(className, module.getModuleWithDependenciesAndLibrariesScope(true));
    }

    /**
     * Convert a java package name to a path.
     *
     * @param packageName the package
     * @return the path
     */
    public static String packageToPath(final String packageName) {
        return packageName.replace('.', '/');
    }

    //
    // Helper methods
    //

    /**
     * Create query for enhancer classes.
     *
     * @param enhancerSupport  Determines which classes to search for
     * @param module           Module to search in
     * @return the query
     */
    private static Query<PsiClass> createEnhancerClassQuery(final EnhancerSupport enhancerSupport, final Module module) {
        return AllClassesSearch.search(module.getModuleWithLibrariesScope(), module.getProject(), new Condition<String>() {
            public boolean value(final String s) {
                final String[] enhancerClassNames = enhancerSupport.getEnhancerClassNames();
                boolean equals = false;

                if (s != null) {
                    for (final String enhancerClassName : enhancerClassNames) {
                        equals = equals || s.equals(enhancerClassName);
                        if (equals) {
                            break;
                        }
                    }
                }

                return equals;
            }
        });
    }

}
