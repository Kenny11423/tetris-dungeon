#!/bin/bash
# run.sh - Auto download dependencies, compile and run Tetris Dungeon

echo "Checking for MariaDB JDBC driver..."
mkdir -p bin
mkdir -p lib

if [ ! -f "lib/mariadb-java-client.jar" ]; then
    echo "Downloading MariaDB JDBC driver..."
    curl -L -o lib/mariadb-java-client.jar https://repo1.maven.org/maven2/org/mariadb/jdbc/mariadb-java-client/3.1.4/mariadb-java-client-3.1.4.jar
fi

echo "Compiling Java source code..."
javac -d bin src/com/tetris/*.java

if [ $? -eq 0 ]; then
    echo "Compilation successful. Extracting JDBC driver for runtime..."
    cd bin
    jar xf ../lib/mariadb-java-client.jar
    cd ..
    
    echo "Running Tetris Dungeon..."
    java -cp bin com.tetris.Tetris
else
    echo "Compilation failed."
fi
