#!/bin/bash

# Test script for AIsteroids
# Builds and runs all tests

echo "Running AIsteroids Test Suite..."

# Build everything
echo "Building source and test files..."
javac -d bin -cp "lib/*" src/*.java test/*.java

if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi

echo "Running basic test suite..."
java -ea -cp bin:lib/* TestRunner

if [ $? -ne 0 ]; then
    echo "Basic tests failed!"
    exit 1
fi

echo "Running advanced test suite..."
java -ea -cp bin:lib/* AdvancedTestRunner

if [ $? -ne 0 ]; then
    echo "Advanced tests failed!"
    exit 1
fi

echo "All tests passed! ✅"
