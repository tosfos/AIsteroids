#!/bin/bash

# Run script for AIsteroids
# Builds and runs the game

echo "Starting AIsteroids..."

# Build first if needed
if [ ! -d "bin" ] || [ ! -f "bin/Main.class" ]; then
    echo "Building project..."
    ./build.sh
    if [ $? -ne 0 ]; then
        echo "Build failed, cannot run game"
        exit 1
    fi
fi

# Run the game
java -cp bin:lib/* Main
