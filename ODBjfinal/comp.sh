#!/bin/bash

TOMCAT_HOME=/home/sepia/ws/apache-tomcat-11.0.1
ASM_HOME=/home/sepia/Downloads/StageINP/TPInitiationServlet/servlet-be/ODBjfinal/asm
ASM_JARS=$ASM_HOME/asm-9.7.1.jar:$ASM_HOME/asm-analysis-9.7.1.jar:$ASM_HOME/asm-commons-9.7.1.jar:$ASM_HOME/asm-test-9.7.1.jar:$ASM_HOME/asm-tree-9.7.1.jar:$ASM_HOME/asm-util-9.7.1.jar:$TOMCAT_HOME/lib/*
cd /home/sepia/Downloads/StageINP/TPInitiationServlet/servlet-be/ODBjfinal/bin
#rm -rf out
#mkdir out
echo 1
java -cp $ASM_JARS:. pack.CodeParserExceptions $1
echo 2
cp -r pack out
cp -r odbj out
#rm -rf out/app
mkdir -p out/app
cp out/BackendServer.class out/app/
cd -
echo "---------------"

