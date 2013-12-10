IntelliJ IDEA DataNucleus enhancer plugin
=========================================================================================
This project is licensed by the Apache 2 license which you should have received with this
file
=========================================================================================


IntelliJ IDEA Plugin Project with maven:
====

If you already checked out the project, you can directly start with 6.

1. Project pom is included with both the binary and the source distribution
    Binary distribution: /META-INF/maven/org.datanucleus.ide.idea/datanucleus-idea-plugin/pom.xml
    Source distribution: /pom.xml
    You can use 7-Zip for extracting files from a jar (http://www.7-zip.org/).

2. Create maven project structure and put the sources into src/main/java/
    See http://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html.
    and ignore information about the location of README.txt, LICENSE.txt and
    NOTICE.txt (will be put to META-INF in this project - see 3).

3. Copy META-INF directory (including content) to src/main/resources

4. Copy pom.xml to project root
    You can uncomment the repositories section and/or copy it's content to your
    maven settings.xml (DataNucleus repository).

5. Resulting project structure must look like this:

    <Project Root>
    |   src
    |   |   main
    |   |   |    java
    |   |   |    |   com
    |   |   |        |   ..
    |   |   |    resources
    |   |   |    |   META-INF
    |   |   |        |   ..
    |   pom.xml

6. IntelliJ IDEA Plugin project dependencies have to be installed manually to the local maven repository
    There are only outdated packages on public maven repositories, hence you have to
    have IDEA installed.
    You can use the example script (provided in source package jar) as basis
    (see "META-INF/install_maven_idea_deps.bat" - sorry no u*ix/linux shell script so far).

    To make things even easier, here's it's content:

    <code>
        @rem complete your IDEA Home folder and version here
        set IDEA_HOME=C:\Program Files\JetBrains\IntelliJ IDEA 9.0.1
        set IDEA_VERSION=IU-95.429
        set IDEA_GROUP_ID=com.intellij

        @rem also set the following paths correctly
        set JAVA_HOME=C:\Program Files\Java\jdk1.6.0_18
        set PATH=%PATH%;C:\Program Files\apache-maven-3.0-beta-3\bin

        call mvn install:install-file -DgroupId=%IDEA_GROUP_ID% -Dpackaging=jar -Dfile="%IDEA_HOME%\lib\openapi.jar" -DartifactId=openapi -Dversion=%IDEA_VERSION%
        call mvn install:install-file -DgroupId=%IDEA_GROUP_ID% -Dpackaging=jar -Dfile="%IDEA_HOME%\lib\annotations.jar" -DartifactId=annotations -Dversion=%IDEA_VERSION%
        call mvn install:install-file -DgroupId=%IDEA_GROUP_ID% -Dpackaging=jar -Dfile="%IDEA_HOME%\lib\extensions.jar" -DartifactId=extensions -Dversion=%IDEA_VERSION%
        call mvn install:install-file -DgroupId=%IDEA_GROUP_ID% -Dpackaging=jar -Dfile="%IDEA_HOME%\lib\util.jar" -DartifactId=util -Dversion=%IDEA_VERSION%
        call mvn install:install-file -DgroupId=%IDEA_GROUP_ID% -Dpackaging=jar -Dfile="%IDEA_HOME%\redist\forms_rt.jar" -DartifactId=forms_rt -Dversion=%IDEA_VERSION%
        call mvn install:install-file -DgroupId=%IDEA_GROUP_ID% -Dpackaging=jar -Dfile="%IDEA_HOME%\redist\javac2.jar" -DartifactId=javac2 -Dversion=%IDEA_VERSION%
    </code>

    IMPORTANT: OMIT <code> TAGS WHEN copying

7. Create new project within IDEA

    IDEA 10 project files are part of the project, hence "File->Open Project" will suffice.
    If you encounter problems with IDEA loosing the plugin flavour (most likely with 9.x) for the Project, continue to 8.

    If you want to recreate the project config read the following steps.

    Select 'Import project from external model' and choose 'Maven' afterwards.
    Provide the maven home directory during the next step (Environment settings button).

8. Manually modifying IDEA project module config
    (to be able to debug the plugin inside of IDEA without redeploying)

    Replace the following line from datanucleus-idea-plugin.iml

    <code>
        <module org.jetbrains.idea.maven.project.MavenProjectsManager.isMavenModule="true" type="JAVA_MODULE" version="4">
    </code>

    with

    <code>
        <module org.jetbrains.idea.maven.project.MavenProjectsManager.isMavenModule="true" type="PLUGIN_MODULE" version="4">
          <component name="DevKit.ModuleBuildProperties" url="file://$MODULE_DIR$/src/main/resources/META-INF/plugin.xml" />
    </code>

    to let maven and IDEA play together in a plugin project.

    IMPORTANT: OMIT <code> TAGS WHEN copying
    IMPORTANT: Whenever the pom changes and IDEA updates the module config, this procedure has to be repeated (only IDEA 9.x).

9. Import IDEA codestyle settings (File -> Import Settings)
    File: idea-codestyle-settings.jar
