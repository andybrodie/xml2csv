@echo off

rem Runs the command line application from the compiled binaries.

setlocal

set lib=lib
set bin=bin
set cp=-.;%bin%;%lib%\commons-cli-1.2.jar;%lib%/logback-classic-1.1.2.jar;%lib%/logback-core-1.1.2.jar;%lib%/Saxon-HE-9.5.1-6.jar;%lib%/slf4j-api-1.7.6.jar;%lib%/xercesImpl-2.11.0.jar;%lib%/xml-apis-1.4.01.jar

java -cp %cp% com.locima.xml2csv.cmdline.Program %*