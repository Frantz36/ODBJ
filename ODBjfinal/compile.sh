#!/bin/bash

# Définition des chemins
ASM_HOME=/home/sepia/Downloads/StageINP/TPInitiationServlet/servlet-is/ODBjfinal/asm
ASM_JARS=$ASM_HOME/asm-9.7.1.jar:$ASM_HOME/asm-analysis-9.7.1.jar:$ASM_HOME/asm-commons-9.7.1.jar:$ASM_HOME/asm-test-9.7.1.jar:$ASM_HOME/asm-tree-9.7.1.jar:$ASM_HOME/asm-util-9.7.1.jar

# Répertoire des fichiers source
SRC_DIR=/home/sepia/Downloads/StageINP/TPInitiationServlet/servlet-is/ODBjfinal/src/pack

# Répertoire des fichiers odbj
SRC_DIR_ODBJ=/home/sepia/Downloads/StageINP/TPInitiationServlet/servlet-is/ODBjfinal/src/odbj

# Répertoire des fichiers app 
SRC_DIR_APP=/home/sepia/Downloads/StageINP/TPInitiationServlet/servlet-is/ODBjfinal/src/app

# Répertoire de destination des .class
DEST_DIR=/home/sepia/Downloads/StageINP/TPInitiationServlet/servlet-is/ODBjfinal/bin

# Répertoire de destination des .class d'odbj 
DEST_DIR_ODBJ=/home/sepia/Downloads/StageINP/TPInitiationServlet/servlet-is/ODBjfinal/bin/odbj

# Répertoire de destination des .class d'odbj
DEST_DIR_APP=/home/sepia/Downloads/StageINP/TPInitiationServlet/servlet-is/ODBjfinal/bin/app



# Compilation des fichiers Java
javac --release 17 -cp $ASM_JARS -d $DEST_DIR $SRC_DIR/TryCatchBlockNodeStruct.java \
                                      $SRC_DIR/Inst.java \
                                      $SRC_DIR/OneSlotInst.java \
                                      $SRC_DIR/TwoSlotInst.java \
                                      $SRC_DIR/InstructionStack.java \
                                      $SRC_DIR/CodeParserExceptions.java

                                      

javac  --release 17 -cp "/home/sepia/Downloads/StageINP/TPInitiationServlet/servlet-is/ODBjfinal/asm/jakarta.servlet-api-5.0.0.jar" -d $DEST_DIR $SRC_DIR_ODBJ/Downloader.java \
                                      $SRC_DIR_ODBJ/MyFileInputStream.java \
                                      $SRC_DIR_ODBJ/MyInputStream.java \
                                      $SRC_DIR_ODBJ/MyOutputStream.java \
                                      $SRC_DIR_ODBJ/MySocket.java \
                                      $SRC_DIR_ODBJ/MyServerSocket.java \
                                      $SRC_DIR_ODBJ/RealDescriptor.java \
                                      $SRC_DIR_ODBJ/VirtualDescriptor.java\
                                      $SRC_DIR_ODBJ/Handler.java


javac -Xlint --release 17 -cp "/home/sepia/Downloads/StageINP/TPInitiationServlet/servlet-is/ODBjfinal/asm/jakarta.servlet-api-5.0.0.jar" -d $DEST_DIR $SRC_DIR_ODBJ/Downloader.java \
                                      $SRC_DIR_ODBJ/MyHttpServletRequest.java \
                                      $SRC_DIR_ODBJ/MyHttpServletResponse.java \
                                      $SRC_DIR_ODBJ/MyServletInputStream.java \
                                      $SRC_DIR_ODBJ/MyServletOutputStream.java \
                                      $SRC_DIR_ODBJ/RealDescriptor.java \
                                      $SRC_DIR_ODBJ/VirtualDescriptor.java

javac -g -cp "/home/sepia/Downloads/StageINP/TPInitiationServlet/servlet-is/ODBjfinal/asm/jakarta.servlet-api-5.0.0.jar" -d $DEST_DIR/app $SRC_DIR_APP/InterServer.java


echo "Compilation terminée. Les fichiers .class sont dans : $DEST_DIR"
echo "Compilation terminée. Les fichiers .class sont dans : $DEST_DIR_ODBJ"
echo "Compilation terminée. Les fichiers .class sont dans : $DEST_DIR_APP"