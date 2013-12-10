@rem complete your IDEA Home folder and version here
set IDEA_HOME=C:\Program Files (x86)\JetBrains\IntelliJ IDEA 11.1.1
set IDEA_VERSION=IC-117.798
set IDEA_GROUP_ID=com.intellij

@rem also set the following paths correctly
set JAVA_HOME=C:\Program Files (x86)\Java\jdk1.6.0_35

call mvn --help > NUL 2> NUL
if errorlevel 0 goto mvnInPath
set PATH=%PATH%;C:\Program Files (x86)\apache-maven-3\bin

:mvnInPath
call mvn install:install-file -e -B -ff -DgroupId=%IDEA_GROUP_ID% -Dpackaging=jar -Dfile="%IDEA_HOME%\lib\openapi.jar" -DartifactId=openapi -Dversion=%IDEA_VERSION%
call mvn install:install-file -e -B -ff -DgroupId=%IDEA_GROUP_ID% -Dpackaging=jar -Dfile="%IDEA_HOME%\lib\annotations.jar" -DartifactId=annotations -Dversion=%IDEA_VERSION%
call mvn install:install-file -e -B -ff -DgroupId=%IDEA_GROUP_ID% -Dpackaging=jar -Dfile="%IDEA_HOME%\lib\extensions.jar" -DartifactId=extensions -Dversion=%IDEA_VERSION%
call mvn install:install-file -e -B -ff -DgroupId=%IDEA_GROUP_ID% -Dpackaging=jar -Dfile="%IDEA_HOME%\lib\util.jar" -DartifactId=util -Dversion=%IDEA_VERSION%
call mvn install:install-file -e -B -ff -DgroupId=%IDEA_GROUP_ID% -Dpackaging=jar -Dfile="%IDEA_HOME%\redist\forms_rt.jar" -DartifactId=forms_rt -Dversion=%IDEA_VERSION%
call mvn install:install-file -e -B -ff -DgroupId=%IDEA_GROUP_ID% -Dpackaging=jar -Dfile="%IDEA_HOME%\redist\javac2.jar" -DartifactId=javac2 -Dversion=%IDEA_VERSION%
