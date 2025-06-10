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

echo "Running integration test suite..."
java -ea -cp bin:lib/* IntegrationTestSuite

if [ $? -ne 0 ]; then
    echo "Integration tests failed!"
    exit 1
fi

echo "Running edge case test suite..."
java -ea -cp bin:lib/* EdgeCaseTestSuite

if [ $? -ne 0 ]; then
    echo "Edge case tests failed!"
    exit 1
fi

echo "Running focused component tests..."
java -ea -cp bin:lib/* ScoreCalculatorTest
if [ $? -ne 0 ]; then
    echo "ScoreCalculator tests failed!"
    exit 1
fi

java -ea -cp bin:lib/* CollisionTest
if [ $? -ne 0 ]; then
    echo "Collision tests failed!"
    exit 1
fi

java -ea -cp bin:lib/* PowerUpTest
if [ $? -ne 0 ]; then
    echo "PowerUp tests failed!"
    exit 1
fi

java -ea -cp bin:lib/* WaveSystemTest
if [ $? -ne 0 ]; then
    echo "WaveSystem tests failed!"
    exit 1
fi

echo "All tests passed! ✅"
