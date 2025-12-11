package AttendanceSystem.ui;

import AttendanceSystem.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class AssignmentPanel extends BasePanel {
    private JTable assignmentTable;
    private DefaultTableModel assignmentModel;
    private JTable submissionTable; // Teacher only
    private DefaultTableModel submissionModel; // Teacher only
    private JSplitPane splitPane; // Teacher only

    public AssignmentPanel(AttendanceSystem system, User currentUser) {
        super(system, currentUser);
        initComponents();
    }

    private void initComponents() {
        removeAll();
        setLayout(new BorderLayout(DesignSystem.SPACING_MD, DesignSystem.SPACING_MD));
        setBackground(DesignSystem.BACKGROUND);

        // Top Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setOpaque(false);
        
        JLabel title = new JLabel("Assignments");
        title.setFont(DesignSystem.FONT_TITLE);
        toolbar.add(title);
        
        toolbar.add(Box.createHorizontalStrut(20));

        if ("TEACHER".equals(currentUser.getRole())) {
            JButton createBtn = DesignSystem.createButton("Create Assignment", DesignSystem.PRIMARY);
            createBtn.addActionListener(e -> showCreateDialog());
            toolbar.add(createBtn);
        } else if ("STUDENT".equals(currentUser.getRole())) {
            JButton uploadBtn = DesignSystem.createButton("Upload Submission", DesignSystem.PRIMARY);
            uploadBtn.addActionListener(e -> showUploadDialog());
            toolbar.add(uploadBtn);
        }

        add(toolbar, BorderLayout.NORTH);

        // Center Content
        if ("TEACHER".equals(currentUser.getRole())) {
            setupTeacherView();
        } else {
            setupStudentView();
        }
    }

    private void setupTeacherView() {
        // Left: Assignment List
        String[] assignCols = {"ID", "Subject", "Title", "Deadline"};
        assignmentModel = new DefaultTableModel(assignCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        assignmentTable = new JTable(assignmentModel);
        DesignSystem.styleTable(assignmentTable);
        
        // Selection listener to load submissions
        assignmentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSubmissionsForSelected();
            }
        });

        JScrollPane assignScroll = new JScrollPane(assignmentTable);
        assignScroll.setBorder(BorderFactory.createTitledBorder("Assignments"));
        assignScroll.getViewport().setBackground(Color.WHITE);

        // Right: Submissions List
        String[] subCols = {"Student ID", "Name", "Status", "File"};
        submissionModel = new DefaultTableModel(subCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        submissionTable = new JTable(submissionModel);
        DesignSystem.styleTable(submissionTable);

        JScrollPane subScroll = new JScrollPane(submissionTable);
        subScroll.setBorder(BorderFactory.createTitledBorder("Submissions"));
        subScroll.getViewport().setBackground(Color.WHITE);
        
        // Action Panel for submissions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(Color.WHITE);
        JButton approveBtn = DesignSystem.createButton("Approve", DesignSystem.SUCCESS);
        JButton rejectBtn = DesignSystem.createButton("Reject", DesignSystem.DANGER);
        
        approveBtn.addActionListener(e -> processSubmission("APPROVED"));
        rejectBtn.addActionListener(e -> processSubmission("REJECTED"));
        
        actionPanel.add(approveBtn);
        actionPanel.add(rejectBtn);
        
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(subScroll, BorderLayout.CENTER);
        rightPanel.add(actionPanel, BorderLayout.SOUTH);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, assignScroll, rightPanel);
        splitPane.setDividerLocation(500);
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);
    }

    private void setupStudentView() {
        String[] assignCols = {"Subject", "Title", "Description", "Deadline", "My Status"};
        assignmentModel = new DefaultTableModel(assignCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        assignmentTable = new JTable(assignmentModel);
        DesignSystem.styleTable(assignmentTable);

        JScrollPane scroll = new JScrollPane(assignmentTable);
        add(scroll, BorderLayout.CENTER);
    }

    @Override
    public void onShow() {
        refreshData();
    }

    private void refreshData() {
        assignmentModel.setRowCount(0);
        List<Assignment> list = system.getAllAssignments();
        
        if ("TEACHER".equals(currentUser.getRole())) {
            for (Assignment a : list) {
                // Teachers see assignments they created OR match their subjects
                if (currentUser.canAccessSubject(a.getSubject())) {
                    assignmentModel.addRow(new Object[]{
                        a.getId(), a.getSubject(), a.getTitle(), a.getDeadline()
                    });
                }
            }
        } else if ("STUDENT".equals(currentUser.getRole())) {
            // Students see assignments for their course (assumed logic: subject matches their course subjects)
            // Or simpler: Show all for now, as Student object has Course string ("B.Tech..."). 
            // Matching "Subject" (e.g. "DSA-I") to "Course" is hard without a mapping.
            // WORKAROUND: Show ALL assignments that match the subjects in the student's implicit list.
            // But Student object doesn't have a list of subjects easily accessible here.
            // We'll show ALL assignments for now or filter by what the student is enrolled in.
            // Actually, User object for student has subjects[] populated with "Course" name. 
            // Wait, AuthenticationManager put "Course" into subjects[0].
            // But Assignment is linked to "Subject" (e.g. "DSA-I").
            // Mismatch: Student Course="B.Tech" vs Assignment Subject="DSA-I".
            // We'll show ALL assignments.
            
            for (Assignment a : list) {
                 AssignmentSubmission sub = system.getStudentSubmission(a.getId(), currentUser.getStudentId());
                 String status = (sub == null) ? "Not Submitted" : sub.getStatus();
                 assignmentModel.addRow(new Object[]{
                     a.getSubject(), a.getTitle(), a.getDescription(), a.getDeadline(), status
                 });
            }
        }
    }

    private void loadSubmissionsForSelected() {
        if (submissionModel == null) return;
        submissionModel.setRowCount(0);
        int row = assignmentTable.getSelectedRow();
        if (row == -1) return;

        String id = (String) assignmentModel.getValueAt(row, 0);
        List<AssignmentSubmission> subs = system.getSubmissionsForAssignment(id);
        for (AssignmentSubmission s : subs) {
            submissionModel.addRow(new Object[]{
                s.getStudentId(), s.getStudentName(), s.getStatus(), s.getFilePath()
            });
        }
    }

    private void showCreateDialog() {
        JTextField titleField = new JTextField();
        JTextField descField = new JTextField();
        JTextField deadlineField = new JTextField(); // Simple string for now
        JComboBox<String> subjectBox = new JComboBox<>(currentUser.getSubjects());

        JPanel panel = new JPanel(new GridLayout(4, 2));
        panel.add(new JLabel("Subject:")); panel.add(subjectBox);
        panel.add(new JLabel("Title:")); panel.add(titleField);
        panel.add(new JLabel("Description:")); panel.add(descField);
        panel.add(new JLabel("Deadline (YYYY-MM-DD):")); panel.add(deadlineField);

        int result = JOptionPane.showConfirmDialog(this, panel, "New Assignment", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            Assignment a = new Assignment(
                (String) subjectBox.getSelectedItem(),
                titleField.getText(),
                descField.getText(),
                deadlineField.getText(),
                currentUser.getUsername()
            );
            system.addAssignment(a);
            refreshData();
            showSuccess("Assignment Created!");
        }
    }

    private void showUploadDialog() {
        int row = assignmentTable.getSelectedRow();
        if (row == -1) {
            showError("Please select an assignment first.");
            return;
        }

        // Get Assignment ID. In student view, cols are Subject, Title... No ID col visible?
        // Ah, I didn't add ID column to student view. I need to find the assignment.
        // I should have stored the ID or object.
        // Let's rely on Title + Subject for lookup or rebuild the student table to include hidden ID.
        // Better: Fix student table model to include ID at col 5 (hidden) or just finding it.
        // I will iterate list again to find match (inefficient but safe).
        String subject = (String) assignmentTable.getValueAt(row, 0);
        String title = (String) assignmentTable.getValueAt(row, 1);
        
        Assignment target = null;
        for (Assignment a : system.getAllAssignments()) {
            if (a.getSubject().equals(subject) && a.getTitle().equals(title)) {
                target = a;
                break;
            }
        }
        
        if (target == null) return;

        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            AssignmentSubmission sub = new AssignmentSubmission(
                target.getId(),
                currentUser.getStudentId(),
                currentUser.getUsername(), // Using username (Student Name)
                file.getName() // Just storing filename for simulation
            );
            system.addSubmission(sub);
            refreshData();
            showSuccess("File Uploaded: " + file.getName());
        }
    }
    
    private void processSubmission(String newStatus) {
        int row = submissionTable.getSelectedRow();
        if (row == -1) return;
        
        int assignRow = assignmentTable.getSelectedRow();
        String assignId = (String) assignmentModel.getValueAt(assignRow, 0);
        String studentId = (String) submissionModel.getValueAt(row, 0);
        
        AssignmentSubmission sub = system.getStudentSubmission(assignId, studentId);
        if (sub != null) {
            sub.setStatus(newStatus);
            system.addSubmission(sub); // Saves changes
            loadSubmissionsForSelected(); // Refresh list
        }
    }
}
