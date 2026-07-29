@echo off
setlocal
set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_491
set MAVEN_OPTS=-Dexec.mainClass=com.javaforge.ui.JavaForgeUI
echo Starting Java Forge IDE...
call "C:\Program Files\NetBeans-12.6\netbeans\java\maven\bin\mvn.cmd" -pl forge-ui exec:java -Dexec.mainClass=com.javaforge.ui.JavaForgeUI -f "%~dp0pom.xml"
pause
