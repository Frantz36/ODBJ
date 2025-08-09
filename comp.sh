#!/bin/bash

ASM_HOME=/home/sepia/Documents/ODBjv/asm
ASM_JARS=$ASM_HOME/asm-9.7.1.jar:$ASM_HOME/asm-analysis-9.7.1.jar:$ASM_HOME/asm-commons-9.7.1.jar:$ASM_HOME/asm-test-9.7.1.jar:$ASM_HOME/asm-tree-9.7.1.jar:$ASM_HOME/asm-util-9.7.1.jar
cd bin
rm -rf out
mkdir out
java -cp $ASM_JARS:. pack.CodeParserExceptions $1
cp -r pack out 
cp -r odbj out 
cp app/Server\$Role.class out/app
cd -
echo "---------------"
