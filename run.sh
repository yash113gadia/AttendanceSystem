#!/bin/bash
# Run script for Attendance Management System

echo "🚀 Starting Attendance Management System..."
echo ""

# Check if bin directory exists
if [ ! -d "bin" ]; then
    echo "⚠️  No build found. Running build first..."
    ./build.sh
fi

# Run the application
java -cp bin AttendanceSystem.MainGUI
