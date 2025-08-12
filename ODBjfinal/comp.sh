#!/bin/bash

TOMCAT_HOME=/home/sepia/ws/apache-tomcat-11.0.1
ASM_HOME=/home/dan/Documents/ODBJ-Tomcat/servlet-is/ODBjfinal/asm
ASM_JARS=$ASM_HOME/asm-9.7.1.jar:$ASM_HOME/asm-analysis-9.7.1.jar:$ASM_HOME/asm-commons-9.7.1.jar:$ASM_HOME/asm-test-9.7.1.jar:$ASM_HOME/asm-tree-9.7.1.jar:$ASM_HOME/asm-util-9.7.1.jar:$TOMCAT_HOME/lib/*
cd /home/dan/Documents/ODBJ-Tomcat/servlet-is/ODBjfinal/bin
#rm -rf out
#mkdir out
echo 1
java -cp $ASM_JARS:. pack.CodeParserExceptions app.InterServer
echo 2
cp -r pack out
cp -r odbj out
#rm -rf out/app
mkdir -p out/app
cp out/InterServer.class out/app/
cd -
echo "---------------"

