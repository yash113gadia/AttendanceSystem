#!/bin/bash
# Build script for Attendance Management System

echo "🔨 Building Attendance Management System..."

# Create bin directory if it doesn't exist
mkdir -p bin

# Compile all Java files
javac -d bin -sourcepath src src/AttendanceSystem/*.java src/AttendanceSystem/ui/*.java

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    echo ""
    echo "Run the application with: ./run.sh"
else
    echo "❌ Build failed!"
    exit 1
fi
