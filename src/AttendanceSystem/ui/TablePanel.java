package AttendanceSystem.ui;

import AttendanceSystem.AttendanceSystem;
import AttendanceSystem.User;
import AttendanceSystem.Student;
import AttendanceSystem.Assignment;
import AttendanceSystem.AssignmentSubmission;
import AttendanceSystem.Event; // Explicit import to fix ambiguity
import AttendanceSystem.EventPhoto;
import AttendanceSystem.MainGUI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;

/**
 * Panel for displaying all students in a table view.
 * Uses modern card-based design with color-coded attendance status.
 */
public class TablePanel extends BasePanel {
    private JTable studentTable;
    private DefaultTableModel tableModel;
    
    public TablePanel(AttendanceSystem system, User currentUser) {
        super(system, currentUser);
        setBackground(DesignSystem.BACKGROUND);
        initComponents();
    }
    
    private void initComponents() {
        if ("STUDENT".equals(currentUser.getRole())) {
            initStudentDashboard();
            return;
        }
        
        setLayout(new BorderLayout(0, DesignSystem.SPACING_MD));
        
        // Card container for the table
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

        // Header with title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = DesignSystem.createHeading("Student Attendance Records");
        JLabel subtitleLabel = new JLabel("View and monitor all student attendance data");
        subtitleLabel.setFont(DesignSystem.FONT_BODY);
        subtitleLabel.setForeground(DesignSystem.TEXT_SECONDARY);
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(DesignSystem.SPACING_XS));
        titlePanel.add(subtitleLabel);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        card.add(headerPanel, BorderLayout.NORTH);
        
        // Table
        String[] columns = {"ID", "Name", "Course", "Sessions", "Attended", "Attendance", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        studentTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? DesignSystem.SURFACE : new Color(248, 250, 252));
                    
                    // Color the status column
                    if (column == 6) {
                        String status = getModel().getValueAt(row, 6).toString();
                        JLabel label = (JLabel) c;
                        label.setHorizontalAlignment(SwingConstants.CENTER);
                        
                        if (status.equals("Excellent")) {
                            c.setBackground(new Color(220, 252, 231));
                            c.setForeground(new Color(22, 101, 52));
                        } else if (status.equals("Good")) {
                            c.setBackground(new Color(254, 249, 195));
                            c.setForeground(new Color(133, 77, 14));
                        } else if (status.equals("Warning")) {
                            c.setBackground(new Color(255, 237, 213));
                            c.setForeground(new Color(194, 65, 12));
                        } else if (status.equals("N/A")) {
                            c.setBackground(new Color(241, 245, 249));
                            c.setForeground(new Color(100, 116, 139));
                        } else {
                            c.setBackground(new Color(254, 226, 226));
                            c.setForeground(new Color(153, 27, 27));
                        }
                    } else {
                        c.setForeground(DesignSystem.TEXT_PRIMARY);
                    }
                } else {
                    c.setBackground(DesignSystem.PRIMARY_LIGHT);
                    c.setForeground(DesignSystem.PRIMARY_DARK);
                }
                
                return c;
            }
        };
        
        studentTable.setFont(DesignSystem.FONT_BODY);
        studentTable.setRowHeight(48);
        studentTable.setShowGrid(false);
        studentTable.setIntercellSpacing(new Dimension(0, 0));
        studentTable.getTableHeader().setFont(DesignSystem.FONT_BODY_BOLD);
        studentTable.getTableHeader().setBackground(new Color(248, 250, 252));
        studentTable.getTableHeader().setForeground(DesignSystem.TEXT_SECONDARY);
        studentTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, DesignSystem.BORDER));
        studentTable.getTableHeader().setPreferredSize(new Dimension(0, 48));
        studentTable.setSelectionBackground(DesignSystem.PRIMARY_LIGHT);
        studentTable.setSelectionForeground(DesignSystem.PRIMARY_DARK);
        
        // Center align header
        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) studentTable.getTableHeader().getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Center align cells
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < studentTable.getColumnCount(); i++) {
            studentTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(DesignSystem.BORDER, 1));
        scrollPane.getViewport().setBackground(DesignSystem.SURFACE);
        card.add(scrollPane, BorderLayout.CENTER);
        
        // Legend
        card.add(createLegendPanel(), BorderLayout.SOUTH);
        
        add(card, BorderLayout.CENTER);
    }
    
    private void initStudentDashboard() {
        setLayout(new BorderLayout(DesignSystem.SPACING_MD, DesignSystem.SPACING_MD));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        Student me = system.findStudent(currentUser.getStudentId());
        if (me == null) {
            add(new JLabel("Student Record Not Found"), BorderLayout.CENTER);
            return;
        }
        
        // --- 1. Header (Welcome) ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel welcomeLabel = new JLabel("Hello, " + me.getName().split(" ")[0] + "!");
        welcomeLabel.setFont(DesignSystem.FONT_TITLE);
        welcomeLabel.setForeground(DesignSystem.TEXT_PRIMARY);
        
        JLabel subLabel = new JLabel("Here's your academic overview for today.");
        subLabel.setFont(DesignSystem.FONT_BODY);
        subLabel.setForeground(DesignSystem.TEXT_SECONDARY);
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(welcomeLabel);
        textPanel.add(subLabel);
        
        headerPanel.add(textPanel, BorderLayout.WEST);
        
        // --- 2. Stats Row ---
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setOpaque(false);
        statsPanel.setPreferredSize(new Dimension(0, 120));
        
        double att = me.getAttendancePercentage();
        Color attColor;
        if (att > 80) attColor = new Color(22, 101, 52);
        else if (att >= 75) attColor = new Color(133, 77, 14);
        else if (att >= 70) attColor = new Color(194, 65, 12);
        else attColor = new Color(153, 27, 27);
        
        statsPanel.add(createDashboardStatCard("Attendance", String.format("%.1f%%", att), "chart", attColor));
        statsPanel.add(createDashboardStatCard("Total Sessions", String.valueOf(me.getTotalSessions()), "calendar", DesignSystem.PRIMARY));
        
        // Count pending tasks
        int pendingCount = 0;
        for (Assignment a : system.getAllAssignments()) {
            AssignmentSubmission sub = system.getStudentSubmission(a.getId(), me.getId());
            if (sub == null || "PENDING".equals(sub.getStatus())) pendingCount++;
        }
        for (Event ev : system.getApprovedEvents()) {
            boolean hasPhoto = false;
            for (EventPhoto p : system.getPhotosForEvent(ev.getId())) {
                if (p.getStudentId().equals(me.getId())) { hasPhoto = true; break; }
            }
            if (!hasPhoto) pendingCount++;
        }
        
        statsPanel.add(createDashboardStatCard("Pending Tasks", String.valueOf(pendingCount), "file", DesignSystem.WARNING));
        
        // --- 3. Content Split (Actions & List) ---
        JPanel contentPanel = new JPanel(new BorderLayout(20, 0));
        contentPanel.setOpaque(false);
        
        // Left: Quick Actions
        JPanel actionsPanel = DesignSystem.createCard();
        actionsPanel.setLayout(new BoxLayout(actionsPanel, BoxLayout.Y_AXIS));
        actionsPanel.setPreferredSize(new Dimension(250, 0));
        
        JLabel actionTitle = DesignSystem.createHeading("Quick Actions");
        actionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JButton markBtn = DesignSystem.createButton("Mark Attendance", DesignSystem.PRIMARY);
        markBtn.setMaximumSize(new Dimension(250, 45));
        markBtn.addActionListener(e -> {
            Window win = SwingUtilities.getWindowAncestor(this);
            if (win instanceof MainGUI) ((MainGUI)win).showPanel(MainGUI.PANEL_ATTENDANCE);
        });
        
        JButton viewEventsBtn = DesignSystem.createButton("View Events", DesignSystem.INFO);
        viewEventsBtn.setMaximumSize(new Dimension(250, 45));
        viewEventsBtn.addActionListener(e -> {
            Window win = SwingUtilities.getWindowAncestor(this);
            if (win instanceof MainGUI) ((MainGUI)win).showPanel(MainGUI.PANEL_EVENTS);
        });
        
        actionsPanel.add(actionTitle);
        actionsPanel.add(Box.createVerticalStrut(20));
        actionsPanel.add(markBtn);
        actionsPanel.add(Box.createVerticalStrut(10));
        actionsPanel.add(viewEventsBtn);
        actionsPanel.add(Box.createVerticalGlue());
        
        // Right: Pending Tasks List
        JPanel taskListPanel = DesignSystem.createCard();
        taskListPanel.setLayout(new BorderLayout(0, 10));
        
        JLabel listTitle = DesignSystem.createHeading("To-Do List");
        taskListPanel.add(listTitle, BorderLayout.NORTH);
        
        DefaultListModel<String> taskModel = new DefaultListModel<>();
        JList<String> taskList = new JList<>(taskModel);
        taskList.setFont(DesignSystem.FONT_BODY);
        taskList.setFixedCellHeight(50);
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskList.setBackground(Color.WHITE);
        
        // Populate
        for (Assignment a : system.getAllAssignments()) {
            AssignmentSubmission sub = system.getStudentSubmission(a.getId(), me.getId());
            if (sub == null) {
                taskModel.addElement("📝 Submit Assignment: " + a.getTitle());
            }
        }
        for (Event ev : system.getApprovedEvents()) {
            boolean hasPhoto = false;
            for (EventPhoto p : system.getPhotosForEvent(ev.getId())) {
                if (p.getStudentId().equals(me.getId())) { hasPhoto = true; break; }
            }
            if (!hasPhoto) {
                taskModel.addElement("📸 Upload Photo: " + ev.getTitle());
            }
        }
        if (taskModel.isEmpty()) {
            taskModel.addElement("🎉 All caught up! No pending tasks.");
        }
        
        JScrollPane scroll = new JScrollPane(taskList);
        DesignSystem.styleScrollPane(scroll);
        taskListPanel.add(scroll, BorderLayout.CENTER);
        
        contentPanel.add(actionsPanel, BorderLayout.WEST);
        contentPanel.add(taskListPanel, BorderLayout.CENTER);
        
        // Assemble Main Layout
        JPanel mainWrapper = new JPanel(new BorderLayout(0, 20));
        mainWrapper.setOpaque(false);
        mainWrapper.add(headerPanel, BorderLayout.NORTH);
        mainWrapper.add(statsPanel, BorderLayout.CENTER);
        
        add(mainWrapper, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private JPanel createDashboardStatCard(String title, String value, String iconType, Color color) {
        JPanel card = DesignSystem.createCard();
        card.setLayout(new BorderLayout(15, 0));
        
        // Icon Circle
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30)); // Transparent
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        iconPanel.setPreferredSize(new Dimension(60, 60));
        iconPanel.setOpaque(false);
        // (Icon drawing omitted for brevity, using simple label)
        JLabel iconLbl = new JLabel(iconType.equals("chart") ? "📊" : (iconType.equals("calendar") ? "📅" : "⚡"));
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        iconLbl.setForeground(color);
        iconPanel.add(iconLbl);
        
        JPanel textInfo = new JPanel(new GridLayout(2, 1));
        textInfo.setOpaque(false);
        
        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font(DesignSystem.FONT_FAMILY, Font.BOLD, 28));
        valLbl.setForeground(DesignSystem.TEXT_PRIMARY);
        
        JLabel titleLbl = new JLabel(title.toUpperCase());
        titleLbl.setFont(new Font(DesignSystem.FONT_FAMILY, Font.BOLD, 11));
        titleLbl.setForeground(DesignSystem.TEXT_SECONDARY);
        
        textInfo.add(valLbl);
        textInfo.add(titleLbl);
        
        card.add(iconPanel, BorderLayout.WEST);
        card.add(textInfo, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createLegendPanel() {
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, DesignSystem.SPACING_LG, DesignSystem.SPACING_SM));
        legendPanel.setOpaque(false);
        legendPanel.setBorder(BorderFactory.createEmptyBorder(DesignSystem.SPACING_MD, 0, 0, 0));
        
        legendPanel.add(createLegendItem("Excellent (>80%)", new Color(22, 101, 52), new Color(220, 252, 231)));
        legendPanel.add(createLegendItem("Good (70-80%)", new Color(133, 77, 14), new Color(254, 249, 195)));
        legendPanel.add(createLegendItem("Warning (60-70%)", new Color(194, 65, 12), new Color(255, 237, 213)));
        legendPanel.add(createLegendItem("Critical (<60%)", new Color(153, 27, 27), new Color(254, 226, 226)));
        
        return legendPanel;
    }
    
    private JPanel createLegendItem(String text, Color textColor, Color bgColor) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, DesignSystem.SPACING_XS, 0));
        item.setOpaque(false);
        
        JLabel dot = new JLabel("●");
        dot.setForeground(textColor);
        dot.setFont(new Font(DesignSystem.FONT_FAMILY, Font.PLAIN, 10));
        
        JLabel label = new JLabel(text);
        label.setFont(DesignSystem.FONT_SMALL);
        label.setForeground(DesignSystem.TEXT_SECONDARY);
        
        item.add(dot);
        item.add(label);
        
        return item;
    }
    
    @Override
    public void onShow() {
        refreshData();
    }
    
    public void refreshData() {
        if ("STUDENT".equals(currentUser.getRole())) {
            removeAll();
            initStudentDashboard();
            revalidate();
            repaint();
            return;
        }
        
        tableModel.setRowCount(0);
        Student[] students = system.getAllStudents();
        String currentFilter = "All Courses";
        
        // Get filter from parent if possible
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof MainGUI) {
            currentFilter = ((MainGUI) window).getCurrentCourseFilter();
        }
        
        for (Student s : students) {
            // Apply filter
            if (!currentFilter.equals("All Courses") && !s.getCourse().equals(currentFilter)) {
                continue;
            }
            
            double percentage = s.getAttendancePercentage();
            String status;
            if (s.getTotalSessions() == 0) {
                status = "N/A";
            } else if (percentage > 80) status = "Excellent";
            else if (percentage >= 70) status = "Good";
            else if (percentage >= 60) status = "Warning";
            else status = "Critical";
            
            Object[] row = {
                s.getId(),
                s.getName(),
                truncateText(s.getCourse(), 35),
                s.getTotalSessions(),
                s.getTotalSessionsAttended(),
                String.format("%.1f%%", percentage),
                status
            };
            
            tableModel.addRow(row);
        }
    }
    
    private String truncateText(String text, int maxLength) {
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}