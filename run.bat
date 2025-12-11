@echo off
REM Run script for Attendance Management System

echo Starting Attendance Management System...
echo.

REM Check if bin directory exists
if not exist "bin" (
    echo No build found. Running build first...
    call build.bat
)

REM Run the application
java -cp bin AttendanceSystem.AttendanceManagementGUI
