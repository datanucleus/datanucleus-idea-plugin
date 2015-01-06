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

import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.ClassPostProcessingCompiler;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.compiler.CompilerPaths;
import com.intellij.openapi.compiler.FileProcessingCompiler;
import com.intellij.openapi.compiler.TimestampValidityState;
import com.intellij.openapi.compiler.ValidityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;

import org.datanucleus.ide.idea.integration.EnhancerContext;
import org.datanucleus.ide.idea.integration.EnhancerProxy;
import org.datanucleus.ide.idea.integration.EnhancerSupport;
import org.datanucleus.ide.idea.util.ExceptionUtils;
import org.datanucleus.ide.idea.util.VirtualFileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Enhances class files with xml- or annotation based metadata in
 * all build-cycle affected project modules where xml metadata or
 * annotated classes could be found.<br>
 * <br>
 * Activated enhancer integration dependencies must be in the project module's classpath!<br>
 * If not, although activated, the module will be ignored and a warning will be logged (Idea messages).<br>
 * <br>
 * This implementation always tries to enhance all files in a module at once, hence a failing
 * class can prevent all others (also whole modules) from being processed.<br>
 * <br>
 * Failure stacktraces are transformed into strings and written to the ideaX messages output.
 */
class DNEComputable implements ClassPostProcessingCompiler {

    private static final Logger IDEA_LOGGER  = Logger.getInstance(DNEComputable.class);

    private static final DNEComputableLoggerWrapper LOGGER = new DNEComputableLoggerWrapper(IDEA_LOGGER);

    private static final FileProcessingCompiler.ProcessingItem[] EMPTY_PROCESSING_ITEMS = new FileProcessingCompiler.ProcessingItem[0];

    private static final char SEP = '/';

    //
    // Members
    //

    /**
     * Current project
     */
    private final Project project;

    /**
     * Plugin shared configuration
     */
    private final DNEState state;

    //
    // Constructor
    //

    DNEComputable(final Project project, final DNEState state) {
        this.project = project;
        this.state = state;
    }

    //
    // ClassPostProcessingCompiler interface implementation
    //

    @NotNull
    public FileProcessingCompiler.ProcessingItem[] getProcessingItems(final CompileContext cCtx) {
        LOGGER.update(this.state, cCtx, null);

        final Set<String> enabledModules = this.state.getEnabledModules();
        if (this.state.isEnhancerEnabled() && enabledModules != null && !enabledModules.isEmpty()) {
            // get metadata files of affected modules
            final Map<Module, List<VirtualMetadataFile>> moduleBasedMetadataFiles =
                    this.getMetadataFiles(cCtx.getCompileScope());

            // get annotated class files of affected modules
            final Map<Module, List<VirtualMetadataFile>> moduleBasedAnnotatedClasses =
                    this.getAnnotatedClassFiles(cCtx.getCompileScope());

            final Collection<FileProcessingCompiler.ProcessingItem> processingItems =
                    new LinkedHashSet<FileProcessingCompiler.ProcessingItem>();
            for (final List<VirtualMetadataFile> metadataFileList : moduleBasedMetadataFiles.values()) {
                for (final VirtualMetadataFile virtualMetadataFile : metadataFileList) {
                    final Collection<EnhancerItem> enhancerItems = virtualMetadataFile.toEnhancerItems();
                    for (final EnhancerItem enhancerItem : enhancerItems) {
                        processingItems.add(enhancerItem);
                    }
                }
            }
            for (final List<VirtualMetadataFile> annotatedClassesFileList : moduleBasedAnnotatedClasses.values()) {
                for (final VirtualMetadataFile virtualMetadataFile : annotatedClassesFileList) {
                    final Collection<EnhancerItem> enhancerItems = virtualMetadataFile.toEnhancerItems();
                    for (final EnhancerItem enhancerItem : enhancerItems) {
                        processingItems.add(enhancerItem);
                    }
                }
            }

            if (processingItems.isEmpty()) {
                LOGGER.warn("No metadata- or annotated class-files found");
            }

            LOGGER.debug("Enhancer: Processing items to check for changes: " + toString(processingItems));
            return processingItems.toArray(new FileProcessingCompiler.ProcessingItem[processingItems.size()]);
        } else {
            return EMPTY_PROCESSING_ITEMS;
        }
    }

    public FileProcessingCompiler.ProcessingItem[] process(final CompileContext cCtx, final FileProcessingCompiler.ProcessingItem[] processingItems) {
        final long startTimestamp = System.currentTimeMillis();
        LOGGER.update(this.state, cCtx, null);
        LOGGER.debug("Processing items selected for enhancement: " + toString(processingItems));

        FileProcessingCompiler.ProcessingItem[] ret = EMPTY_PROCESSING_ITEMS;

        // shortcut if disabled
        final Set<String> enabledModules = this.state.getEnabledModules();
        if (!this.state.isEnhancerEnabled() || enabledModules == null || enabledModules.isEmpty()) {

        } else {

            // just to be sure: backup of classloader
            final ClassLoader previousCL = Thread.currentThread().getContextClassLoader();

            // for displaying progress messages
            final ProgressIndicator progressIndicator = cCtx.getProgressIndicator();

            try {
                // GUI State display init
                progressIndicator.pushState();
                progressIndicator.setFraction(0);

                final String enhancerName = this.state.getEnhancerSupport().getName();
                progressIndicator.setText(enhancerName + " Enhancer starting for api " + this.state.getApi());

                final LinkedHashMap<Module, List<VirtualMetadataFile>> moduleBasedMetadataFiles =
                        new LinkedHashMap<Module, List<VirtualMetadataFile>>();

                final LinkedHashMap<Module, List<VirtualMetadataFile>> moduleBasedAnnotatedClasses =
                        new LinkedHashMap<Module, List<VirtualMetadataFile>>();

                for (final FileProcessingCompiler.ProcessingItem processingItem : processingItems) {
                    final EnhancerItem enhancerItem = (EnhancerItem) processingItem;
                    final VirtualMetadataFile virtualMetadata = enhancerItem.getVirtualMetadata();
                    final Module module = virtualMetadata.getModule();
                    if (virtualMetadata.isAnnotationBasedOnly()) {
                        List<VirtualMetadataFile> annotatedClassesList = moduleBasedAnnotatedClasses.get(module);
                        if (annotatedClassesList == null) {
                            annotatedClassesList = new ArrayList<VirtualMetadataFile>();
                            moduleBasedAnnotatedClasses.put(module, annotatedClassesList);
                        }
                        annotatedClassesList.add(virtualMetadata);
                    } else {
                        List<VirtualMetadataFile> metadataFileList = moduleBasedMetadataFiles.get(module);
                        if (metadataFileList == null) {
                            metadataFileList = new ArrayList<VirtualMetadataFile>();
                            moduleBasedMetadataFiles.put(module, metadataFileList);
                        }
                        if (!metadataFileList.contains(virtualMetadata)) {
                            metadataFileList.add(virtualMetadata);
                        }
                    }
                }

                // detect all modules affected
                final List<Module> affectedModules = getAffectedModules(cCtx, moduleBasedMetadataFiles, moduleBasedAnnotatedClasses);


                // no metadata or annotated classes -> no enhancement
                final int count;
                if (!affectedModules.isEmpty()) {

                    // TODO: manually excluded dependencies
                    final Collection<String> excludedDependencies = null;

                    // start enhancer per module
                    count = enhanceInModules(cCtx, affectedModules, moduleBasedMetadataFiles, moduleBasedAnnotatedClasses, excludedDependencies);
                    // success message
                    final long endTimestamp = System.currentTimeMillis();
                    final long duration = endTimestamp - startTimestamp;
                    final String minutes = String.format("%d min %d sec",
                            TimeUnit.MILLISECONDS.toMinutes(duration),
                            TimeUnit.MILLISECONDS.toSeconds(duration) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
                    );
                    final String msg = "Successfully enhanced " + count + " class(es) in " + duration + " ms: " + minutes;
                    LOGGER.info(msg);
                } else {
                    final String msg = "no JDO/JPA metadata or annotated class files found";
                    LOGGER.warn(msg);
                    count = 0;
                }

                // only return all items if completed successfully (clicking the 'build' button would not have any effect otherwise)
                ret = processingItems.length == count ? processingItems : EMPTY_PROCESSING_ITEMS;

            } catch (Throwable t) {
                LOGGER.error("Error while enhancing.", t);
            } finally {
                Thread.currentThread().setContextClassLoader(previousCL);
                progressIndicator.popState();
                LOGGER.update(this.state, null, null);
            }
        }

        return ret;
    }

    @NotNull
    public String getDescription() {
        return "DataNucleus Enhancer";
    }

    public boolean validateConfiguration(final CompileScope compileScope) {
        return true;
    }

    public ValidityState createValidityState(final DataInput dataInput) throws IOException {
        return TimestampValidityState.load(dataInput);
    }

    //
    // Helper methods
    //

    private int enhanceInModules(final CompileContext cCtx,
                                 final Collection<Module> affectedModules,
                                 final Map<Module, List<VirtualMetadataFile>> moduleBasedMetadataFiles,
                                 final Map<Module, List<VirtualMetadataFile>> moduleBasedAnnotatedClasses,
                                 final Collection<String> excludedDependencies)
            throws IllegalAccessException,
                   InvocationTargetException,
                   IOException {

        int iteration = 0;
        int count = 0;
        for (final Module module : affectedModules) {
            LOGGER.update(this.state, cCtx, module);

            // exclude disabled modules
            final Set<String> enabledModules = this.state.getEnabledModules();
            if (enabledModules != null && enabledModules.contains(module.getName()) && !cCtx.getProgressIndicator().isCanceled()) {

                // get modules output folders
                final VirtualFile outputDirectory = cCtx.getModuleOutputDirectory(module);
                final VirtualFile testOutputDirectory = cCtx.getModuleOutputDirectoryForTests(module);

                // only enhance in modules that have an output folder
                if (outputDirectory == null) {
                    final String msg = "No output directory for module: " + module.getName();
                    LOGGER.warn(msg);

                } else if (this.state.isIncludeTestClasses() && testOutputDirectory == null){
                    final String msg = "No test output directory for module: " + module.getName();
                    LOGGER.warn(msg);

                } else {

                    final ProgressIndicator progressIndicator = cCtx.getProgressIndicator();
                    final EnhancerSupport enhancerSupport = this.state.getEnhancerSupport();
                    LOGGER.info("Initializing enhancing process");

                    // update progress text
                    progressIndicator.setText(enhancerSupport.getName() + " Enhancer enhancing in " + module.getName());
                    // metadata files for module
                    final List<VirtualMetadataFile> metadataFiles = moduleBasedMetadataFiles.get(module);
                    // metadata files for module
                    final List<VirtualMetadataFile> annotatedClassFiles = moduleBasedAnnotatedClasses.get(module);

                    // manual compiler dependencies
                    List<String> dependencies = null;
                    final boolean dependenciesManual = this.state.isDependenciesManual();
                    if (dependenciesManual) {
                        final Map<String, Map<PersistenceApi, List<VirtualFile>>> allDeps = this.state.getDependencies();
                        if (allDeps != null && !allDeps.isEmpty()) {
                            final Map<PersistenceApi, List<VirtualFile>> bySupp = allDeps.get(enhancerSupport.getId());
                            if (bySupp != null && !bySupp.isEmpty()) {
                                final List<VirtualFile> deps = bySupp.get(this.state.getApi());
                                if (deps != null && !deps.isEmpty()) {
                                    dependencies = new ArrayList<String>(deps.size());
                                    for (final VirtualFile dep : deps) {
                                        final String path = VirtualFileUtils.toPathString(dep);
                                        dependencies.add(path);
                                    }
                                }
                            }
                        }
                        // idea inspection bug?
                        //noinspection ConstantConditions
                        if (dependencies == null || dependencies.isEmpty()) {
                            LOGGER.error("Enhancer dependencies set to 'manual' but none provided!");
                            final String msg = "Enhancer dependencies for " + enhancerSupport.getName()
                                    + " [" + this.state.getApi() + "] set to 'manual' but none provided!";
                            throw new IllegalArgumentException(msg);
                        }
                    }

                    final EnhancerModuleContext moduleContext = new EnhancerModuleContext(module,
                                                                                          outputDirectory,
                                                                                          testOutputDirectory,
                                                                                          metadataFiles,
                                                                                          annotatedClassFiles,
                                                                                          excludedDependencies,
                                                                                          dependencies);

                    try {
                        // do class enhancement in module
                        count += enhancePerModule(enhancerSupport, this.state.getApi(), cCtx, moduleContext);

                    } catch (ClassNotFoundException e) {
                        LOGGER.error("No enhancer found in classpath", e);
                    } catch (NoSuchMethodException e) {
                        LOGGER.error("Enhancer calling error", e);
                    } catch (EnhancerProxyCreationException e) {
                        LOGGER.error("Could not load enhancer proxy", e);
                    }
                }
            } else {
                LOGGER.debug("Omitting disabled module");
            }
            ++iteration;
            final double progress = (1.0 / affectedModules.size()) * iteration;
            cCtx.getProgressIndicator().setFraction(progress);
        }
        cCtx.getProgressIndicator().setFraction(1.0);
        return count;
    }

    private static int enhancePerModule(final EnhancerSupport enhancerSupport,
                                        final PersistenceApi api,
                                        final CompileContext cCtx,
                                        final EnhancerModuleContext mCtx)
            throws ClassNotFoundException,
                   IllegalAccessException,
                   InvocationTargetException,
                   NoSuchMethodException,
                   IOException,
                   EnhancerProxyCreationException {

        final Module module = mCtx.getModule();

        //
        // what to enhance

        final Collection<VirtualMetadataFile> metadataFiles = mCtx.getMetadataFiles();
        final Collection<VirtualMetadataFile> annotatedClassFiles = mCtx.getAnnotatedClassFiles();

        // metadata based classes
        final boolean metadataBased = !metadataFiles.isEmpty();
        // annotation based classes
        final boolean annotationBased = !annotatedClassFiles.isEmpty();

        //
        // only enhance if metadata or annotation based class files present

        final boolean doEnhance = metadataBased || annotationBased;

        //
        // create enhancer instance

        final EnhancerProxy enhancer;
        final List<String> inputClasses; // for logging
        final List<String> inputMetadataFiles; // for logging
        if (doEnhance) {
            LOGGER.debug("Creating enhancer proxy");
            final EnhancerContext eCtx = createEnhancerContext(enhancerSupport, api, cCtx, mCtx);

            inputClasses = new ArrayList<String>();
            inputMetadataFiles = new ArrayList<String>();
            enhancer = createEnhancerProxy(eCtx, enhancerSupport);
        } else {
            LOGGER.info("Nothing to enhance");
            inputClasses = Collections.emptyList();
            inputMetadataFiles = Collections.emptyList();
            enhancer = null;
        }

        //
        // add metadata and classes

        // add metadata based classes to enhancer list
        if (doEnhance && metadataBased) {

            // iterate modules and enhance classes in corresponding output folders
            for (final VirtualMetadataFile metadataFile : metadataFiles) {

                //ctx.addMessage(CompilerMessageCategory.INFORMATION, "DataNucleus Enhancer: found metadata file: " + metadataFile.getPath(), null, -1, -1);
                // get metadata file url
                final VirtualFile metadataVirtualFile = metadataFile.getFile();
                final String metadataFilePath = metadataVirtualFile.getPath();
                // add metadata file url to enhancer
                inputMetadataFiles.add(metadataVirtualFile.getName());
                enhancer.addMetadataFiles(metadataFilePath);

                // parse package and class names
                final Collection<String> classNames = metadataFile.getClassNames();

                // add xml metadata based classes
                for (final String className : classNames) {
                    final VirtualFile outputDirectory = mCtx.getOutputDirectory();
                    final VirtualFile testOutputDirectory = mCtx.getTestOutputDirectory();

                    final String classNameAsPath = IdeaProjectUtils.packageToPath(className);
                    final String outputPath = outputDirectory.getPath() + SEP + classNameAsPath + ".class";
                    final String testOutputPath = testOutputDirectory == null
                            ? null : testOutputDirectory.getPath() + SEP + classNameAsPath + ".class";

                    final String fullPath;
                    if (new File(outputPath).exists()) {
                        fullPath = outputPath;
                    } else if (testOutputPath != null && new File(testOutputPath).exists()) {
                        fullPath = testOutputPath;
                    } else {
                        throw new IllegalArgumentException("No output file can be found for " + className + " in module " + module.getName());
                    }

                    LOGGER.debug("Adding xml metadata based class for enhancement: " + fullPath);
                    inputClasses.add(className);
                    enhancer.addClasses(fullPath);
                }
            }
        }

        // add annotated classes to enhancer list
        if (doEnhance && annotationBased) {

            for (final VirtualMetadataFile annotatedClassFile : annotatedClassFiles) {
                //compileContext.addMessage(CompilerMessageCategory.INFORMATION,
                //                          "DataNucleus Enhancer: found class: " + annotatedClassFile.getPath(), null, -1, -1);
                final VirtualFile annotatedClassVirtualFile = annotatedClassFile.getFile();
                final String path = annotatedClassVirtualFile.getPath();
                LOGGER.debug("Adding annotated class for enhancement: " + path);
                inputClasses.add(annotatedClassFile.getClassNames().iterator().next());
                enhancer.addClasses(path);
            }
        }

        //
        // finally enhance classes

        // count nr of enhanced classes
        final int enhancedCount;

        if (doEnhance && !inputClasses.isEmpty()) {
            LOGGER.info("Metadata files input: " + inputMetadataFiles);
            LOGGER.info("Classes input: " + inputClasses);

            // finally enhance all found classes in module
            enhancedCount = enhancer.enhance();
        } else {
            // nothing to enhance
            enhancedCount = 0;
        }

        return enhancedCount;
    }

    //
    // Utility methods
    //

    private static EnhancerContext createEnhancerContext(final EnhancerSupport enhancerSupport,
                                                         final PersistenceApi api,
                                                         final CompileContext cCtx,
                                                         final EnhancerModuleContext mCtx) throws IOException {
        final Module module = mCtx.getModule();
        final ClassLoader cl = enhancerSupport.newClassLoader(cCtx,
                module,
                mCtx.getExcludedDependencies(),
                mCtx.getEnhancerDependencies());

        return new EnhancerContext(api, module, cCtx, null, cl);
    }

    /**
     * Create enhancer proxy in backwards compatible fashion.
     *
     * @param eCtx            .
     * @return                .
     */
    @SuppressWarnings({"deprecation"})
    private static EnhancerProxy createEnhancerProxy(final EnhancerContext eCtx, final EnhancerSupport enhancerSupport)
            throws EnhancerProxyCreationException {

        final CompileContext cCtx = eCtx.getCompileContext();

        EnhancerProxy proxy = null;

        // First try the V1.0.x creation (as this is the latest stable version, and most people will still have that)
        Exception firstException = null;
        try {
            proxy = enhancerSupport.newEnhancerProxy(eCtx);
        } catch (Exception e) {
            firstException = e;
        }

        if (proxy == null) {
            LOGGER.info("V1.1.x enhancer proxy not found, trying V1.0.x");
            try {
                final Module module = eCtx.getModule();
                final String persistenceUnitName = eCtx.getPersistenceUnitName();

                proxy = enhancerSupport.newEnhancerProxy(eCtx.getPersistenceApi(), cCtx, module, persistenceUnitName);
            } catch (Exception e) {
                final String msg_postfix = "enhancer proxy instantiation exception:";
                final StringBuilder msg = new StringBuilder("Neither V1.1.x nor V1.0.x enhancer proxy found!");
                msg.append("\n\nV1.1.x ")
                        .append(msg_postfix)
                        .append("\n\n")
                        .append(ExceptionUtils.stackTraceToString(firstException))
                        .append("\n\nMaybe v1.0.x error:\n\n")
                        .append("\n\nV1.0.x ")
                        .append(msg_postfix)
                        .append("\n\n")
                        .append(ExceptionUtils.stackTraceToString(e));
                LOGGER.error(msg.toString());
                throw new EnhancerProxyCreationException(e);
            }
        }

        return proxy;
    }

    /**
     * Retrieve annotated class files.
     *
     * @param compileScope compile scope to use (null for default - can lead to invalid file list due to refactoring)
     * @return .
     */
    // TODO: cleanup, as this seems to be very hacky
    Map<Module, List<VirtualMetadataFile>> getAnnotatedClassFiles(@Nullable final CompileScope compileScope) {
        final LinkedHashMap<Module, List<VirtualMetadataFile>> moduleBasedFiles = new LinkedHashMap<Module, List<VirtualMetadataFile>>();

        final Application application = ApplicationManager.getApplication();
        application.runReadAction(new Runnable() {

            public void run() {
                final CompileScope projectCompileScope = compileScope == null
                        ? CompilerManager.getInstance(DNEComputable.this.project).createProjectCompileScope(DNEComputable.this.project)
                        : compileScope;

                for (final Module module : projectCompileScope.getAffectedModules()) {
                    if (DNEComputable.this.state.getEnabledModules() != null && DNEComputable.this.state.getEnabledModules()
                                                                                                        .contains(module.getName())) {


                        final List<PsiClass> annotatedClasses = IdeaProjectUtils.findPersistenceAnnotatedClasses(
                                DNEComputable.this.state.getEnhancerSupport(), module);
                        final Collection<VirtualFile> outputDirectories = new ArrayList<VirtualFile>(2);
                        outputDirectories.add(CompilerPaths.getModuleOutputDirectory(module, false));
                        if (DNEComputable.this.state.isIncludeTestClasses()) {
                            outputDirectories.add(CompilerPaths.getModuleOutputDirectory(module, true));
                        }

                        for (final VirtualFile outputDirectory : outputDirectories) {
                            // convert to class files in output directory and add to map
                            if (!annotatedClasses.isEmpty()) {
                                if (outputDirectory == null) {
                                    // just care about empty directory when annotated classes are found
//                            this.ctx.addMessage(CompilerMessageCategory.WARNING, "DataNucleus Enhancer: annotated " +
//                                                                                 (count == 1 ? "test " : "") +
//                                                                                 "classes found but no output directory for module: " +
//                                                                                 module.getName(), null, -1, -1);
                                } else {
                                    final List<VirtualMetadataFile> moduleFiles = new LinkedList<VirtualMetadataFile>();
                                    // convert psi classes to class files in output path
                                    for (final PsiClass annotatedClass : annotatedClasses) {
                                        final String pcClassName = annotatedClass.getQualifiedName();
                                        // convert to path
                                        final String pcClassPath = IdeaProjectUtils.packageToPath(pcClassName) + ".class";
                                        // find file in output path
                                        final VirtualFile pcClassFile = outputDirectory.findFileByRelativePath(pcClassPath);
                                        if (pcClassFile != null && pcClassFile.exists()) {

                                            moduleFiles
                                                    .add(new VirtualMetadataFile(module, true, pcClassFile,
                                                                                 Collections.singletonList(pcClassName),
                                                                                 Collections.singletonList(pcClassFile)));
                                        }
                                    }
                                    if (!moduleFiles.isEmpty()) {
                                        final List<VirtualMetadataFile> storedModuleFiles = moduleBasedFiles.get(module);
                                        // if collection already exists, just add content
                                        if (storedModuleFiles == null) {
                                            moduleBasedFiles.put(module, moduleFiles);
                                        } else {
                                            storedModuleFiles.addAll(moduleFiles);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });

        return moduleBasedFiles;
    }

    /**
     * Retrieve metadata files.
     *
     * @param compileScope compile scope to use (null for default - can lead to invalid file list due to refactoring)
     * @return .
     */
    // TODO: cleanup, as this seems to be very hacky
    Map<Module, List<VirtualMetadataFile>> getMetadataFiles(@Nullable final CompileScope compileScope) {
        final Set<String> extensions;
        if (this.state.getMetaDataExtensions() == null || this.state.getMetaDataExtensions().isEmpty()) {
            extensions = Collections.emptySet(); // DNEState.DEFAULT_METADATA_EXTENSIONS; // no extensions provided -> disable search
        } else {
            extensions = this.state.getMetaDataExtensions();
        }

        final CompileScope projectCompileScope = compileScope == null
                ? CompilerManager.getInstance(this.project).createProjectCompileScope(this.project)
                : compileScope;

        final Map<Module, List<VirtualMetadataFile>> metadataFiles = new LinkedHashMap<Module, List<VirtualMetadataFile>>();

        final Module[] affectedModules = projectCompileScope.getAffectedModules();

        final Application application = ApplicationManager.getApplication();
        application.runReadAction(new Runnable() {
            public void run() {
                if (affectedModules.length > 0) {

                    for (final Module module : affectedModules) {
                        if (DNEComputable.this.state.getEnabledModules() != null && DNEComputable.this.state.getEnabledModules()
                                                                                                            .contains(module.getName())) {

                            final Collection<VirtualFile> outputDirectories = new ArrayList<VirtualFile>(2);
                            outputDirectories.add(CompilerPaths.getModuleOutputDirectory(module, false));
                            if (DNEComputable.this.state.isIncludeTestClasses()) {
                                outputDirectories.add(CompilerPaths.getModuleOutputDirectory(module, true));
                            }

                            for (final VirtualFile outputDirectory : outputDirectories) {

                                if (outputDirectory == null) {
//                            this.ctx
//                                    .addMessage(CompilerMessageCategory.WARNING, "DataNucleus Enhancer: no " +
//                                                                                 (count == 1 ? "test " : "") +
//                                                                                 "output directory for module: " +
//                                                                                 module.getName(), null, -1, -1);
                                } else {

                                    final List<VirtualMetadataFile> moduleFiles = new LinkedList<VirtualMetadataFile>();
                                    for (final String extension : extensions) {
                                        final List<VirtualFile> metadataFilesPerExtension =
                                                IdeaProjectUtils.findFilesByExtension(outputDirectory, extension);

                                        // remove non-parseable files
                                        for (final VirtualFile vf : metadataFilesPerExtension) {
                                            final Set<String> classNames;
                                            try {
                                                classNames = MetadataParser.parseQualifiedClassNames(vf);
                                            } catch (Exception e) {
                                                throw new IllegalArgumentException("parsing metadata error", e);
                                            }
                                            if (classNames != null && !classNames.isEmpty()) {
                                                final List<VirtualFile> classFiles = new ArrayList<VirtualFile>(classNames.size());
                                                for (final String className : classNames) {
                                                    final String classNameAsPath = IdeaProjectUtils.packageToPath(className);
                                                    final VirtualFile classFile =
                                                            outputDirectory.findFileByRelativePath(classNameAsPath + ".class");
                                                    classFiles.add(classFile);
                                                }

                                                moduleFiles.add(new VirtualMetadataFile(module, false, vf, classNames, classFiles));
                                            }
                                        }
                                    }
                                    if (!moduleFiles.isEmpty()) {

                                        metadataFiles.put(module, moduleFiles);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
        return metadataFiles;
    }

    private static List<Module> getAffectedModules(final CompileContext cCtx,
                                           final Map<Module, List<VirtualMetadataFile>> moduleBasedMetadataFiles,
                                           final Map<Module, List<VirtualMetadataFile>> moduleBasedAnnotatedClasses) {
        // list of affected modules
        final List<Module> affectedModules = new ArrayList<Module>();

        // combine affected module lists (preserving order)
        final CompileScope compileScope = cCtx.getCompileScope();
        final Module[] cSAffectedModules = compileScope.getAffectedModules();
        if (cSAffectedModules.length > 0) {
            for (final Module cSAffectedModule : cSAffectedModules) {
                if (moduleBasedMetadataFiles.containsKey(cSAffectedModule) || moduleBasedAnnotatedClasses.containsKey(cSAffectedModule)) {

                    affectedModules.add(cSAffectedModule);
                }
            }
        }
        return affectedModules;
    }

    //
    // Logging helpers
    //

    private static Collection<String> toString(final FileProcessingCompiler.ProcessingItem[] processingItems) {
        return toString(Arrays.asList(processingItems));
    }

    private static Collection<String> toString(final Collection<FileProcessingCompiler.ProcessingItem> processingItems) {
        final Collection<String> asStringItems;
        if (processingItems == null || processingItems.isEmpty()) {
            asStringItems = Collections.emptyList();
        } else {
            asStringItems = new ArrayList<String>(processingItems.size());
            for (final FileProcessingCompiler.ProcessingItem processingItem : processingItems) {
                final VirtualFile file = processingItem.getFile();
                final String path = file.getPath();
                asStringItems.add(path);
            }
        }
        return asStringItems;
    }

}
