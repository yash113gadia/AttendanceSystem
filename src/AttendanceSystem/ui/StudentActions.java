package AttendanceSystem.ui;

import AttendanceSystem.*;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Handles student-related actions like search, remove, import, and reports.
 * These are dialog-based actions that can be triggered from the main GUI.
 */
public class StudentActions {
    private final AttendanceSystem system;
    private final User currentUser;
    private final Component parentComponent;
    private Runnable onDataChanged;
    
    public StudentActions(AttendanceSystem system, User currentUser, Component parentComponent) {
        this.system = system;
        this.currentUser = currentUser;
        this.parentComponent = parentComponent;
    }
    
    public void setOnDataChanged(Runnable callback) {
        this.onDataChanged = callback;
    }
    
    private void notifyDataChanged() {
        if (onDataChanged != null) {
            onDataChanged.run();
        }
    }
    
    private boolean isAdmin() {
        return currentUser.getRole().equals("ADMIN");
    }
    
    /**
     * Search for a student by ID and display their details
     */
    public void searchStudent() {
        String id = JOptionPane.showInputDialog(parentComponent, 
            "Enter Student ID:", "Search Student", JOptionPane.QUESTION_MESSAGE);
        
        if (id == null || id.trim().isEmpty()) {
            return;
        }
        
        Student student = system.findStudent(id.trim());
        if (student == null) {
            JOptionPane.showMessageDialog(parentComponent, 
                "Student not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        StringBuilder info = new StringBuilder();
        info.append("Student ID: ").append(student.getId()).append("\n");
        info.append("Name: ").append(student.getName()).append("\n");
        info.append("Course: ").append(student.getCourse()).append("\n");
        info.append("Total Sessions: ").append(student.getTotalSessions()).append("\n");
        info.append("Sessions Attended: ").append(student.getTotalSessionsAttended()).append("\n");
        info.append(String.format("Attendance: %.2f%%\n\n", student.getAttendancePercentage()));
        
        if (student.getTotalSessions() > 0) {
            info.append("Session-wise Attendance:\n");
            Map<String, Boolean> sessionAttendance = student.getSessionAttendance();
            for (Map.Entry<String, Boolean> entry : sessionAttendance.entrySet()) {
                String status = entry.getValue() ? "Present" : "Absent";
                info.append("  ").append(entry.getKey()).append(": ").append(status).append("\n");
            }
        }
        
        JScrollPane scrollPane = UIUtils.createScrollableTextArea(info.toString(), 500, 400);
        JOptionPane.showMessageDialog(parentComponent, scrollPane, "Student Details", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Remove a student (Admin only)
     */
    public void removeStudent() {
        if (!isAdmin()) {
            JOptionPane.showMessageDialog(parentComponent, 
                "Only admins can remove students!", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String id = JOptionPane.showInputDialog(parentComponent, 
            "Enter Student ID to remove:", "Remove Student", JOptionPane.QUESTION_MESSAGE);
        
        if (id == null || id.trim().isEmpty()) {
            return;
        }
        
        Student student = system.findStudent(id.trim());
        if (student == null) {
            JOptionPane.showMessageDialog(parentComponent, 
                "Student not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(parentComponent, 
            "Are you sure you want to remove:\n" + 
            student.getName() + " (" + student.getId() + ")\n" +
            "Course: " + student.getCourse() + "?",
            "Confirm Removal", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (system.removeStudent(id.trim())) {
                JOptionPane.showMessageDialog(parentComponent, 
                    "Student removed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                notifyDataChanged();
            } else {
                JOptionPane.showMessageDialog(parentComponent, 
                    "Failed to remove student!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Import students from CSV (Admin only)
     */
    public void importStudentsFromCSV() {
        if (!isAdmin()) {
            JOptionPane.showMessageDialog(parentComponent, 
                "Only admins can import students!", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select CSV File");
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
            }
            
            @Override
            public String getDescription() {
                return "CSV Files (*.csv)";
            }
        });
        
        int result = fileChooser.showOpenDialog(parentComponent);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        
        File selectedFile = fileChooser.getSelectedFile();
        int successCount = 0;
        int skipCount = 0;
        int errorCount = 0;
        StringBuilder errorLog = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
            String line;
            boolean isFirstLine = true;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                try {
                    String[] parts = line.split(",");
                    
                    if (parts.length < 3) {
                        errorLog.append("Line ").append(lineNumber).append(": Invalid format (missing columns)\n");
                        errorCount++;
                        continue;
                    }
                    
                    String studentId = parts[0].trim();
                    String studentName = parts[1].trim();
                    String course = parts[2].trim();
                    
                    if (studentId.isEmpty() || studentName.isEmpty() || course.isEmpty()) {
                        errorLog.append("Line ").append(lineNumber).append(": Empty field(s) detected\n");
                        errorCount++;
                        continue;
                    }
                    
                    if (system.findStudent(studentId) != null) {
                        errorLog.append("Line ").append(lineNumber).append(": Student ID ").append(studentId).append(" already exists\n");
                        skipCount++;
                        continue;
                    }
                    
                    system.addStudent(studentName, studentId, course);
                    successCount++;
                    
                } catch (Exception e) {
                    errorLog.append("Line ").append(lineNumber).append(": ").append(e.getMessage()).append("\n");
                    errorCount++;
                }
            }
            
            StringBuilder summary = new StringBuilder();
            summary.append("CSV Import Complete!\n\n");
            summary.append("Successfully imported: ").append(successCount).append(" students\n");
            summary.append("Skipped (duplicates): ").append(skipCount).append(" students\n");
            summary.append("Errors: ").append(errorCount).append(" lines\n");
            
            if (errorLog.length() > 0) {
                summary.append("\n--- Error Details ---\n");
                summary.append(errorLog.toString());
            }
            
            JScrollPane scrollPane = UIUtils.createScrollableTextArea(summary.toString(), 500, 400);
            JOptionPane.showMessageDialog(parentComponent, scrollPane, "Import Summary", 
                errorCount > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
            
            notifyDataChanged();
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parentComponent, 
                "Error reading CSV file:\n" + e.getMessage(), 
                "Import Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Generate attendance report
     */
    public void generateReport() {
        system.generateReport();
        JOptionPane.showMessageDialog(parentComponent, 
            "Report generated successfully!\nSaved to: attendance_report.txt\n\n" +
            "Includes:\n- Overall summary\n- Individual student details\n- Session-wise attendance", 
            "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Show students with low attendance
     */
    public void showLowAttendance() {
        String input = JOptionPane.showInputDialog(parentComponent, 
            "Enter threshold percentage (e.g., 75):", "Low Attendance", JOptionPane.QUESTION_MESSAGE);
        
        if (input == null || input.trim().isEmpty()) {
            return;
        }
        
        try {
            double threshold = Double.parseDouble(input);
            Student[] students = system.getAllStudents();
            StringBuilder result = new StringBuilder("Students with attendance below " + threshold + "%:\n\n");
            final ArrayList<Student> lowAttendanceStudents = new ArrayList<>();
            
            for (Student s : students) {
                if (s.getAttendancePercentage() < threshold) {
                    result.append(String.format("%s - %s\n  Course: %s\n  Attendance: %.2f%% (%d/%d sessions)\n\n", 
                        s.getId(), s.getName(), s.getCourse(), s.getAttendancePercentage(), 
                        s.getTotalSessionsAttended(), s.getTotalSessions()));
                    lowAttendanceStudents.add(s);
                }
            }
            
            if (lowAttendanceStudents.isEmpty()) {
                result.append("No students found with low attendance.");
                JOptionPane.showMessageDialog(parentComponent, result.toString(), 
                    "Low Attendance Report", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            JPanel dialogPanel = new JPanel(new BorderLayout(10, 10));
            dialogPanel.add(UIUtils.createScrollableTextArea(result.toString(), 500, 350), BorderLayout.CENTER);
            
            JButton exportButton = UIUtils.createStyledButton("Export to CSV", UIUtils.SUCCESS_GREEN);
            exportButton.setPreferredSize(new Dimension(150, 35));
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.add(exportButton);
            dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
            
            final double finalThreshold = threshold;
            exportButton.addActionListener(e -> exportLowAttendanceToCSV(lowAttendanceStudents, finalThreshold));
            
            JOptionPane.showMessageDialog(parentComponent, dialogPanel, 
                "Low Attendance Report", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(parentComponent, 
                "Invalid threshold value!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void exportLowAttendanceToCSV(ArrayList<Student> students, double threshold) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Low Attendance Report");
        fileChooser.setSelectedFile(new File("Low_Attendance_Report_" + 
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".csv"));
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
            }
            
            @Override
            public String getDescription() {
                return "CSV Files (*.csv)";
            }
        });
        
        int result = fileChooser.showSaveDialog(parentComponent);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        
        File selectedFile = fileChooser.getSelectedFile();
        if (!selectedFile.getName().toLowerCase().endsWith(".csv")) {
            selectedFile = new File(selectedFile.getAbsolutePath() + ".csv");
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(selectedFile))) {
            writer.println("Low Attendance Report - Below " + threshold + "%");
            writer.println("Generated: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
            writer.println();
            writer.println("Student_ID,Student_Name,Course,Total_Sessions,Sessions_Attended,Attendance_Percentage,Status");
            
            for (Student s : students) {
                double percentage = s.getAttendancePercentage();
                String status;
                if (percentage > 80) {
                    status = "Excellent";
                } else if (percentage >= 70) {
                    status = "Good";
                } else if (percentage >= 60) {
                    status = "Warning";
                } else {
                    status = "Critical";
                }
                
                writer.printf("%s,%s,\"%s\",%d,%d,%.2f%%,%s\n",
                    s.getId(),
                    s.getName(),
                    s.getCourse(),
                    s.getTotalSessions(),
                    s.getTotalSessionsAttended(),
                    percentage,
                    status
                );
            }
            
            writer.println();
            writer.println("Total Students with Low Attendance: " + students.size());
            
            JOptionPane.showMessageDialog(parentComponent, 
                "Low attendance report exported successfully!\n" +
                "File: " + selectedFile.getName() + "\n" +
                "Location: " + selectedFile.getParent() + "\n" +
                "Students: " + students.size(), 
                "Export Successful", JOptionPane.INFORMATION_MESSAGE);
                
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parentComponent, 
                "Error exporting report:\n" + e.getMessage(), 
                "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
