# Attendance Management System

A Java Swing-based GUI application for managing student attendance with role-based access control.

## Features

- **Role-Based Access Control**
  - Admin: Full access (add/remove students, import CSV, mark attendance)
  - Teacher: Limited access (view students, mark attendance for assigned subjects)

- **Student Management**
  - Add students manually
  - Import students from CSV files
  - Remove students (Admin only)
  - Search students by ID

- **Attendance Tracking**
  - Visual attendance marking system
  - Session-wise attendance tracking
  - Color-coded attendance status

- **Reports**
  - Generate attendance reports
  - View low attendance students
  - Export low attendance report to CSV

- **Timetable View**
  - Weekly class schedule display

---

## Project Structure

```
AttendanceManagementGUI/
├── src/
│   └── AttendanceSystem/
│       ├── AttendanceManagementGUI.java   # Main application & UI
│       ├── AttendanceSystem.java          # Business logic & data management
│       ├── AuthenticationManager.java     # User authentication
│       ├── ClassSession.java              # Timetable session model
│       ├── LoginDialog.java               # Login window
│       ├── Person.java                    # Base class for people
│       ├── Student.java                   # Student model
│       └── User.java                      # User model
├── bin/                                   # Compiled .class files
├── data/                                  # Data files (auto-created)
├── build.sh                               # Build script (macOS/Linux)
├── run.sh                                 # Run script (macOS/Linux)
├── build.bat                              # Build script (Windows)
├── run.bat                                # Run script (Windows)
└── README.md                              # This file
```

---

## Quick Start

### Prerequisites
- Java JDK 11 or higher

### Build & Run (macOS/Linux)

```bash
# Make scripts executable (first time only)
chmod +x build.sh run.sh

# Build the project
./build.sh

# Run the application
./run.sh
```

### Build & Run (Windows)

```cmd
:: Build the project
build.bat

:: Run the application
run.bat
```

### Manual Build & Run

```bash
# Compile
javac -d bin src/AttendanceSystem/*.java

# Run
java -cp bin AttendanceSystem.AttendanceManagementGUI
```

---

## Login Credentials

### Admin Accounts
| Username     | Password    |
|--------------|-------------|
| YASHGADIA    | *********   |
| SWETAKUMARI  | **********  |

### Teacher Accounts
| Username | Password | Subjects                |
|----------|----------|-------------------------|
| SHM      | shm123   | DSA-I, DSA-I LAB, R&P   |
| RSB      | rsb123   | OS, OS LAB              |
| DRV      | drv123   | AIC                     |
| RSP      | rsp123   | AI, AI LAB              |

---

## CSV Import Format

To import students, use a CSV file with the following format:

```csv
StudentID,StudentName,Course
STU001,John Doe,B.Tech (Computer Science & Engineering)
STU002,Jane Smith,M.Tech (Artificial Intelligence)
```

---

## Data Storage

- **attendance_data.txt**: Stores all student and attendance data
- **attendance_report.txt**: Generated attendance reports

---

## Color Coding (Attendance Table)

| Color        | Attendance % | Status    |
|--------------|--------------|-----------|
| 🟢 Green     | > 80%        | Excellent |
| 🟡 Yellow    | 75-80%       | Good      |
| 🟠 Orange    | 70-75%       | Warning   |
| 🔴 Red       | < 70%        | Critical  |

---

## License

This project is for educational purposes.
