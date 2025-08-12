#!/bin/bash

ASM_HOME=/home/dan/Documents/ODBJ-Tomcat/servlet-be/ODBjfinal/asm
ASM_JARS=$ASM_HOME/asm-9.7.1.jar:$ASM_HOME/asm-analysis-9.7.1.jar:$ASM_HOME/asm-commons-9.7.1.jar:$ASM_HOME/asm-test-9.7.1.jar:$ASM_HOME/asm-tree-9.7.1.jar:$ASM_HOME/asm-util-9.7.1.jar:$TOMCAT_HOME/lib/*
cd /home/dan/Documents/ODBJ-Tomcat/servlet-be/ODBjfinal/bin
#rm -rf out
#mkdir out
echo 1
java -cp $ASM_JARS:. pack.CodeParserExceptions app.BackendServer
echo 2
cp -r pack out
cp -r odbj out
#rm -rf out/app
mkdir -p out/app
cp out/BackendServer.class out/app/
cd -
echo "---------------"

