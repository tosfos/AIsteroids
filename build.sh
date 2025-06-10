#!/bin/bash

# Simple build script for AIsteroids
# Compiles Java source files from src/ directory

echo "Building AIsteroids..."

# Create bin directory if it doesn't exist
mkdir -p bin

# Compile all Java files
javac -d bin -cp "lib/*" src/*.java

if [ $? -eq 0 ]; then
    echo "Build successful! Run with: java -cp bin:lib/* Main"
else
    echo "Build failed!"
    exit 1
fi
