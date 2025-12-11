package AttendanceSystem.ui;

import AttendanceSystem.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for displaying the weekly timetable.
 * Modern card-based layout with clean aesthetics.
 */
public class TimetablePanel extends BasePanel {
    private JPanel timetableContent;
    private JComboBox<String> courseSelector;
    
    public TimetablePanel(AttendanceSystem system, User currentUser) {
        super(system, currentUser);
        setBackground(DesignSystem.BACKGROUND);
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(0, DesignSystem.SPACING_MD));
        
        // Main card
        JPanel card = new JPanel(new BorderLayout(0, DesignSystem.SPACING_MD)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(DesignSystem.SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), DesignSystem.RADIUS_LG, DesignSystem.RADIUS_LG);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(
            DesignSystem.SPACING_LG, DesignSystem.SPACING_LG, 
            DesignSystem.SPACING_LG, DesignSystem.SPACING_LG));
        
        // Header with Toolbar
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = DesignSystem.createHeading("Weekly Timetable");
        JLabel subtitleLabel = new JLabel("View your class schedule for the week");
        subtitleLabel.setFont(DesignSystem.FONT_BODY);
        subtitleLabel.setForeground(DesignSystem.TEXT_SECONDARY);
        
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(DesignSystem.SPACING_XS));
        titlePanel.add(subtitleLabel);
        
        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, DesignSystem.SPACING_SM, 0));
        toolbar.setOpaque(false);
        
        courseSelector = new JComboBox<>();
        courseSelector.addItem("All Courses");
        for (String c : system.getAllCourses()) {
            courseSelector.addItem(c);
        }
        courseSelector.setFont(DesignSystem.FONT_SMALL);
        courseSelector.addActionListener(e -> refreshTimetable());
        
        JButton uploadBtn = DesignSystem.createButton("Upload CSV", DesignSystem.PRIMARY);
        uploadBtn.setPreferredSize(new Dimension(120, 32));
        uploadBtn.setFont(DesignSystem.FONT_SMALL);
        uploadBtn.addActionListener(e -> handleUpload());
        
        toolbar.add(new JLabel("Filter:"));
        toolbar.add(courseSelector);
        
        // Only show upload if admin? Or anyone? Assuming teacher/admin can upload.
        // if (currentUser.getRole().equals("ADMIN")) {
            toolbar.add(uploadBtn);
        // }
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(toolbar, BorderLayout.EAST);
        
        card.add(headerPanel, BorderLayout.NORTH);
        
        // Timetable content
        timetableContent = new JPanel();
        timetableContent.setLayout(new BoxLayout(timetableContent, BoxLayout.Y_AXIS));
        timetableContent.setOpaque(false);
        
        JScrollPane scrollPane = new JScrollPane(timetableContent);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        card.add(scrollPane, BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);
        
        refreshTimetable();
    }
    
    private void handleUpload() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Timetable CSV");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
            // Determine course name
            String selectedCourse = (String) courseSelector.getSelectedItem();
            String targetCourse = selectedCourse;
            
            if (selectedCourse == null || selectedCourse.equals("All Courses")) {
                targetCourse = JOptionPane.showInputDialog(this, "Enter Course Name for this Timetable:", "Course Name", JOptionPane.QUESTION_MESSAGE);
                if (targetCourse == null || targetCourse.trim().isEmpty()) return;
            } else {
                int confirm = JOptionPane.showConfirmDialog(this, "Overwrite timetable for " + targetCourse + "?", "Confirm Upload", JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) return;
            }
            
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                system.clearCourseTimetable(targetCourse);
                int count = 0;
                java.util.Set<String> newTeachers = new java.util.HashSet<>();
                
                while ((line = reader.readLine()) != null) {
                    // Format: DAY,TIME,SUBJECT,TEACHER,ROOM
                    String[] parts = line.split(",");
                    if (parts.length >= 5) {
                        String teacherName = parts[3].trim();
                        // Check if teacher exists before adding session
                        boolean teacherExisted = AuthenticationManager.teacherExists(teacherName);
                        
                        ClassSession session = new ClassSession(parts[0].trim(), parts[1].trim(), parts[2].trim(), teacherName, parts[4].trim(), targetCourse);
                        system.addSessionToCourse(targetCourse, session);
                        count++;
                        
                        // Track newly created teachers
                        if (!teacherExisted && !teacherName.isEmpty()) {
                            newTeachers.add(teacherName.toUpperCase());
                        }
                    }
                }
                
                // Build success message
                StringBuilder message = new StringBuilder();
                message.append("Successfully imported ").append(count).append(" sessions for ").append(targetCourse);
                
                if (!newTeachers.isEmpty()) {
                    message.append("\n\nNew teacher accounts created:\n");
                    for (String teacher : newTeachers) {
                        message.append("  • ").append(teacher).append(" / ").append(teacher.toLowerCase()).append("123\n");
                    }
                }
                
                JOptionPane.showMessageDialog(this, message.toString(), "Import Complete", JOptionPane.INFORMATION_MESSAGE);
                
                // Update dropdown if new course
                boolean exists = false;
                for (int i=0; i<courseSelector.getItemCount(); i++) {
                    if (courseSelector.getItemAt(i).equals(targetCourse)) exists = true;
                }
                if (!exists) courseSelector.addItem(targetCourse);
                
                courseSelector.setSelectedItem(targetCourse);
                refreshTimetable();
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error reading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void refreshTimetable() {
        timetableContent.removeAll();
        
        String[] days = system.getUniqueDays();
        // If no days (e.g. empty system), show standard weekdays
        if (days.length == 0) days = new String[]{"MON", "TUE", "WED", "THU", "FRI"};
        
        String selectedCourse = (String) courseSelector.getSelectedItem();
        if (selectedCourse == null) selectedCourse = "All Courses";
        
        boolean hasContent = false;
        
        for (String day : days) {
            ArrayList<ClassSession> sessions = system.getSessionsByDay(day, selectedCourse);
            if (!sessions.isEmpty()) {
                JPanel dayCard = createDayCard(day, sessions);
                timetableContent.add(dayCard);
                timetableContent.add(Box.createVerticalStrut(DesignSystem.SPACING_MD));
                hasContent = true;
            }
        }
        
        if (!hasContent) {
            JLabel emptyLabel = new JLabel("No classes scheduled for " + selectedCourse);
            emptyLabel.setFont(DesignSystem.FONT_BODY);
            emptyLabel.setForeground(DesignSystem.TEXT_MUTED);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            timetableContent.add(Box.createVerticalStrut(50));
            timetableContent.add(emptyLabel);
        }
        
        timetableContent.revalidate();
        timetableContent.repaint();
    }
    
    private JPanel createDayCard(String day, ArrayList<ClassSession> sessions) {
        JPanel dayCard = new JPanel(new BorderLayout(0, DesignSystem.SPACING_SM)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(248, 250, 252));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), DesignSystem.RADIUS_MD, DesignSystem.RADIUS_MD);
                g2.setColor(DesignSystem.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, DesignSystem.RADIUS_MD, DesignSystem.RADIUS_MD);
                g2.dispose();
            }
        };
        dayCard.setOpaque(false);
        dayCard.setBorder(BorderFactory.createEmptyBorder(
            DesignSystem.SPACING_MD, DesignSystem.SPACING_MD, 
            DesignSystem.SPACING_MD, DesignSystem.SPACING_MD));
        
        // Day header
        JPanel dayHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, DesignSystem.SPACING_SM, 0));
        dayHeader.setOpaque(false);
        
        JLabel dayIcon = new JLabel(getDayIcon(day));
        dayIcon.setFont(new Font(DesignSystem.FONT_FAMILY, Font.PLAIN, 20));
        
        JLabel dayLabel = new JLabel(getFullDayName(day));
        dayLabel.setFont(DesignSystem.FONT_SUBHEADING);
        dayLabel.setForeground(DesignSystem.PRIMARY);
        
        JLabel countLabel = new JLabel(sessions.size() + " classes");
        countLabel.setFont(DesignSystem.FONT_SMALL);
        countLabel.setForeground(DesignSystem.TEXT_MUTED);
        
        dayHeader.add(dayIcon);
        dayHeader.add(dayLabel);
        dayHeader.add(Box.createHorizontalStrut(DesignSystem.SPACING_SM));
        dayHeader.add(countLabel);
        
        dayCard.add(dayHeader, BorderLayout.NORTH);
        
        // Sessions list
        JPanel sessionsPanel = new JPanel();
        sessionsPanel.setLayout(new BoxLayout(sessionsPanel, BoxLayout.Y_AXIS));
        sessionsPanel.setOpaque(false);
        sessionsPanel.setBorder(BorderFactory.createEmptyBorder(DesignSystem.SPACING_SM, 0, 0, 0));
        
        for (ClassSession session : sessions) {
            sessionsPanel.add(createSessionItem(session));
            sessionsPanel.add(Box.createVerticalStrut(DesignSystem.SPACING_XS));
        }
        
        dayCard.add(sessionsPanel, BorderLayout.CENTER);
        return dayCard;
    }
    
    private JPanel createSessionItem(ClassSession session) {
        JPanel item = new JPanel(new BorderLayout(DesignSystem.SPACING_MD, 0));
        item.setOpaque(false);
        item.setBorder(BorderFactory.createEmptyBorder(
            DesignSystem.SPACING_XS, DesignSystem.SPACING_MD, 
            DesignSystem.SPACING_XS, DesignSystem.SPACING_MD));
        
        // Time badge
        JLabel timeLabel = new JLabel(session.getTimeSlot()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(DesignSystem.PRIMARY_LIGHT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), DesignSystem.RADIUS_SM, DesignSystem.RADIUS_SM);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        timeLabel.setFont(DesignSystem.FONT_SMALL);
        timeLabel.setForeground(DesignSystem.PRIMARY);
        timeLabel.setOpaque(false);
        timeLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        timeLabel.setPreferredSize(new Dimension(120, 26));
        
        // Subject and room
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        
        JLabel subjectLabel = new JLabel(session.getSubject());
        subjectLabel.setFont(DesignSystem.FONT_BODY);
        subjectLabel.setForeground(DesignSystem.TEXT_PRIMARY);
        
        // Teacher
        JLabel teacherLabel = new JLabel(session.getTeacher());
        teacherLabel.setFont(DesignSystem.FONT_SMALL);
        teacherLabel.setForeground(DesignSystem.TEXT_SECONDARY);
        
        JLabel roomLabel = new JLabel(session.getRoom());
        roomLabel.setFont(DesignSystem.FONT_SMALL);
        roomLabel.setForeground(DesignSystem.TEXT_MUTED);
        
        infoPanel.add(subjectLabel);
        if (!session.getTeacher().isEmpty()) {
            infoPanel.add(teacherLabel);
        }
        infoPanel.add(roomLabel);
        
        item.add(timeLabel, BorderLayout.WEST);
        item.add(infoPanel, BorderLayout.CENTER);
        
        return item;
    }
    
    private String getDayIcon(String day) {
        switch (day) {
            case "MON": return "📅";
            case "TUE": return "📆";
            case "WED": return "🗓️";
            case "THU": return "📋";
            case "FRI": return "📝";
            default: return "📅";
        }
    }
    
    private String getFullDayName(String day) {
        switch (day) {
            case "MON": return "Monday";
            case "TUE": return "Tuesday";
            case "WED": return "Wednesday";
            case "THU": return "Thursday";
            case "FRI": return "Friday";
            default: return day;
        }
    }
    
    @Override
    public void onShow() {
        // Refresh dropdown in case courses changed
        String selected = (String) courseSelector.getSelectedItem();
        courseSelector.removeAllItems();
        courseSelector.addItem("All Courses");
        for (String c : system.getAllCourses()) {
            if (!c.equals("Default")) courseSelector.addItem(c);
        }
        
        if (selected != null) {
            // Restore selection if exists
             for (int i=0; i<courseSelector.getItemCount(); i++) {
                if (courseSelector.getItemAt(i).equals(selected)) {
                    courseSelector.setSelectedIndex(i);
                    break;
                }
            }
        }
        
        refreshTimetable();
    }
}
