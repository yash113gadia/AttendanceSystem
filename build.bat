@echo off
REM Build script for Attendance Management System

echo Building Attendance Management System...

REM Create bin directory if it doesn't exist
if not exist "bin" mkdir bin

REM Compile all Java files
javac -d bin src\AttendanceSystem\*.java

if %ERRORLEVEL% EQU 0 (
    echo Build successful!
    echo.
    echo Run the application with: run.bat
) else (
    echo Build failed!
    exit /b 1
)
