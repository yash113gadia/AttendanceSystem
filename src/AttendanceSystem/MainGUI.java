package AttendanceSystem;

import AttendanceSystem.ui.*;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Main GUI class for the Attendance Management System.
 * Uses modern dashboard-inspired design language.
 */
public class MainGUI extends JFrame {
    private AttendanceSystem system;
    private User currentUser;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private Map<String, BasePanel> panels;
    private StudentActions studentActions;
    private JPanel navButtonsPanel;
    private JLabel avgAttendanceLabel;
    private javax.swing.Timer statsTimer;
    private JComboBox<String> courseFilter;
    private String currentCourseFilter = "All Courses";

    // Panel identifiers
    public static final String PANEL_TABLE = "table";
    public static final String PANEL_ADD_STUDENT = "addStudent";
    public static final String PANEL_ATTENDANCE = "attendance";
    public static final String PANEL_TIMETABLE = "timetable";
    public static final String PANEL_ASSIGNMENT = "assignment";
    public static final String PANEL_EVENTS = "events";

    public MainGUI(User user) {
        this.currentUser = user;
        this.system = new AttendanceSystem();
        this.panels = new HashMap<>();
        
        setTitle("Attendance Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 800);
        setLocationRelativeTo(null);
        getContentPane().setBackground(DesignSystem.BACKGROUND);

        // Initialize student actions
        studentActions = new StudentActions(system, currentUser, this);
        studentActions.setOnDataChanged(() -> {
            updateCourseDropdown();
            refreshAllPanels();
        });

        initComponents();

        // Start stats update timer (every 5 seconds)
        statsTimer = new javax.swing.Timer(5000, e -> {
            updateHeaderStats();
            system.reloadData(); // Refresh data from file
            
            // Refresh current panel if visible
            for (BasePanel panel : panels.values()) {
                if (panel.isVisible()) {
                    panel.onShow(); // This triggers refresh
                }
            }
        });
        statsTimer.start();
    }
    
    public String getCurrentCourseFilter() {
        return currentCourseFilter;
    }
    
    private void updateCourseDropdown() {
        if (courseFilter == null) return;
        
        String selected = (String) courseFilter.getSelectedItem();
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("All Courses");
        
        String[] courses = system.getAllCourses();
        for (String c : courses) {
            model.addElement(c);
        }
        
        courseFilter.setModel(model);
        
        // Restore selection if possible
        if (selected != null) {
            courseFilter.setSelectedItem(selected);
        } else {
            courseFilter.setSelectedItem("All Courses");
        }
        
        // Update current filter immediately in case selection changed implicitly
        currentCourseFilter = (String) courseFilter.getSelectedItem();
        if (currentCourseFilter == null) currentCourseFilter = "All Courses";
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(DesignSystem.BACKGROUND);

        // Sidebar on the left
        mainPanel.add(createSidebar(), BorderLayout.WEST);

        // Main content area
        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setBackground(DesignSystem.BACKGROUND);
        contentWrapper.setBorder(BorderFactory.createEmptyBorder(
            DesignSystem.SPACING_LG, DesignSystem.SPACING_LG, 
            DesignSystem.SPACING_LG, DesignSystem.SPACING_LG));

        // Header with greeting
        contentWrapper.add(createHeader(), BorderLayout.NORTH);

        // Card panel for content
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);
        initializePanels();
        contentWrapper.add(cardPanel, BorderLayout.CENTER);

        mainPanel.add(contentWrapper, BorderLayout.CENTER);
        add(mainPanel);

        // Show default panel
        showPanel(PANEL_TABLE);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(DesignSystem.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, DesignSystem.BORDER));

        // Logo/Brand area
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        brandPanel.setBackground(DesignSystem.SIDEBAR_BG);
        brandPanel.setBorder(BorderFactory.createEmptyBorder(20, 16, 20, 16));
        brandPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        brandPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Logo square
        JPanel logoSquare = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(DesignSystem.PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font(DesignSystem.FONT_FAMILY, Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                String text = "A";
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(text, x, y);
                g2.dispose();
            }
        };
        logoSquare.setPreferredSize(new Dimension(30, 30));
        logoSquare.setOpaque(false);
        
        JLabel brandName = new JLabel("AttendEase");
        brandName.setFont(new Font(DesignSystem.FONT_FAMILY, Font.BOLD, 16));
        brandName.setForeground(DesignSystem.TEXT_PRIMARY);
        
        brandPanel.add(logoSquare);
        brandPanel.add(brandName);
        sidebar.add(brandPanel);

        // Navigation buttons container
        navButtonsPanel = new JPanel();
        navButtonsPanel.setLayout(new BoxLayout(navButtonsPanel, BoxLayout.Y_AXIS));
        navButtonsPanel.setBackground(DesignSystem.SIDEBAR_BG);
        navButtonsPanel.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        navButtonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Main section
        navButtonsPanel.add(createSectionHeader("MAIN"));
        
        if ("STUDENT".equals(currentUser.getRole())) {
            // Student View: Dashboard, Assignments
            addNavButton("Dashboard", "grid", PANEL_TABLE); // Reuse table (maybe modify table to show just their attendance?)
            // Actually TablePanel likely shows ALL students.
            // Students shouldn't see all students. 
            // We'll address that later or let them see the class list.
            addNavButton("Assignments", "file", PANEL_ASSIGNMENT);
            addNavButton("Events", "calendar", PANEL_EVENTS);
            
            navButtonsPanel.add(Box.createVerticalStrut(16));
        } else {
            // Teacher/Admin View
            addNavButton("Dashboard", "grid", PANEL_TABLE);
            addNavButton("Timetable", "calendar", PANEL_TIMETABLE);
            addNavButton("Assignments", "file", PANEL_ASSIGNMENT);
            addNavButton("Events", "calendar", PANEL_EVENTS);
    
            navButtonsPanel.add(Box.createVerticalStrut(16));
    
            // Actions section  
            navButtonsPanel.add(createSectionHeader("ACTIONS"));
            addNavButton("Mark Attendance", "check", PANEL_ATTENDANCE);
            addActionNavButton("Search Student", "search", () -> studentActions.searchStudent());
            addActionNavButton("Generate Report", "file", () -> studentActions.generateReport());
            addActionNavButton("Low Attendance", "alert", () -> studentActions.showLowAttendance());
    
            // Admin section (only for admins)
            if (currentUser.getRole().equals("ADMIN")) {
                navButtonsPanel.add(Box.createVerticalStrut(16));
                navButtonsPanel.add(createSectionHeader("ADMIN"));
                addNavButton("Add Student", "plus", PANEL_ADD_STUDENT);
                addActionNavButton("Remove Student", "trash", () -> studentActions.removeStudent());
                addActionNavButton("Import CSV", "download", () -> studentActions.importStudentsFromCSV());
            }
        }

        sidebar.add(navButtonsPanel);

        // Spacer
        sidebar.add(Box.createVerticalGlue());

        // User info at bottom - full width, no gaps
        JPanel userPanel = new JPanel(new BorderLayout(8, 0));
        userPanel.setBackground(new Color(248, 250, 252));
        userPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, DesignSystem.BORDER),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        userPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        userPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel userInfo = new JPanel();
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));
        userInfo.setOpaque(false);

        JLabel userName = new JLabel(currentUser.getUsername());
        userName.setFont(DesignSystem.FONT_BODY_BOLD);
        userName.setForeground(DesignSystem.TEXT_PRIMARY);

        String roleDisplay = currentUser.getRole().equals("ADMIN") ? "Administrator" : "Teacher";
        JLabel userRole = new JLabel(roleDisplay);
        userRole.setFont(DesignSystem.FONT_SMALL);
        userRole.setForeground(DesignSystem.TEXT_SECONDARY);

        userInfo.add(userName);
        userInfo.add(userRole);

        // Buttons panel (right side)
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setOpaque(false);

        // Change Password button
        JButton changePassBtn = new JButton("Change Password");
        changePassBtn.setFont(DesignSystem.FONT_SMALL);
        changePassBtn.setForeground(DesignSystem.PRIMARY);
        changePassBtn.setBackground(DesignSystem.PRIMARY_LIGHT);
        changePassBtn.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        changePassBtn.setFocusPainted(false);
        changePassBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        changePassBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        changePassBtn.addActionListener(e -> showChangePasswordDialog());

        // Logout button
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(DesignSystem.FONT_SMALL);
        logoutBtn.setForeground(DesignSystem.DANGER);
        logoutBtn.setBackground(new Color(254, 226, 226));
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.addActionListener(e -> logout());

        buttonsPanel.add(changePassBtn);
        buttonsPanel.add(Box.createVerticalStrut(4));
        buttonsPanel.add(logoutBtn);

        userPanel.add(userInfo, BorderLayout.CENTER);
        userPanel.add(buttonsPanel, BorderLayout.EAST);

        sidebar.add(userPanel);

        return sidebar;
    }
    
    private void showChangePasswordDialog() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPasswordField currentPassField = new JPasswordField(15);
        JPasswordField newPassField = new JPasswordField(15);
        JPasswordField confirmPassField = new JPasswordField(15);
        
        panel.add(new JLabel("Current Password:"));
        panel.add(currentPassField);
        panel.add(new JLabel("New Password:"));
        panel.add(newPassField);
        panel.add(new JLabel("Confirm Password:"));
        panel.add(confirmPassField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Change Password", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String currentPass = new String(currentPassField.getPassword());
            String newPass = new String(newPassField.getPassword());
            String confirmPass = new String(confirmPassField.getPassword());
            
            if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!newPass.equals(confirmPass)) {
                JOptionPane.showMessageDialog(this, "New passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (newPass.length() < 4) {
                JOptionPane.showMessageDialog(this, "Password must be at least 4 characters.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            boolean success = AuthenticationManager.changePassword(currentUser.getUsername(), currentPass, newPass);
            
            if (success) {
                JOptionPane.showMessageDialog(this, "Password changed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Current password is incorrect.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private JLabel createSectionHeader(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(DesignSystem.FONT_FAMILY, Font.BOLD, 11));
        label.setForeground(DesignSystem.TEXT_MUTED);
        label.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void addNavButton(String text, String icon, String panelId) {
        JButton btn = createSidebarButton(text, icon, false);
        btn.addActionListener(e -> showPanel(panelId));
        btn.setName(panelId);
        navButtonsPanel.add(btn);
        navButtonsPanel.add(Box.createVerticalStrut(DesignSystem.SPACING_XS));
    }

    private void addActionNavButton(String text, String icon, Runnable action) {
        JButton btn = createSidebarButton(text, icon, false);
        btn.addActionListener(e -> action.run());
        navButtonsPanel.add(btn);
        navButtonsPanel.add(Box.createVerticalStrut(DesignSystem.SPACING_XS));
    }

    private JButton createSidebarButton(String text, String iconType, boolean isActive) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Boolean active = (Boolean) getClientProperty("active");
                boolean isActive = active != null && active;
                
                Color bgColor, fgColor, iconColor;
                if (isActive) {
                    bgColor = DesignSystem.PRIMARY_LIGHT;
                    fgColor = DesignSystem.PRIMARY;
                    iconColor = DesignSystem.PRIMARY;
                } else if (getModel().isRollover()) {
                    bgColor = new Color(248, 250, 252);
                    fgColor = DesignSystem.TEXT_PRIMARY;
                    iconColor = DesignSystem.TEXT_PRIMARY;
                } else {
                    bgColor = DesignSystem.SIDEBAR_BG;
                    fgColor = DesignSystem.TEXT_SECONDARY;
                    iconColor = DesignSystem.TEXT_MUTED;
                }
                
                // Draw background
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                
                // Draw active indicator
                if (isActive) {
                    g2.setColor(DesignSystem.PRIMARY);
                    g2.fillRoundRect(0, 6, 3, getHeight() - 12, 2, 2);
                }
                
                // Draw icon at left
                g2.setColor(iconColor);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int iconX = 16;
                int iconY = getHeight() / 2;
                drawIcon(g2, iconType, iconX, iconY);
                
                // Draw text after icon
                g2.setColor(fgColor);
                g2.setFont(isActive ? DesignSystem.FONT_NAV_ACTIVE : DesignSystem.FONT_NAV);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text, 36, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                
                g2.dispose();
            }
            
            private void drawIcon(Graphics2D g2, String type, int x, int y) {
                int s = 7; // icon half-size
                switch (type) {
                    case "grid": // Dashboard - 4 squares
                        g2.fillRoundRect(x - s, y - s, s - 2, s - 2, 2, 2);
                        g2.fillRoundRect(x + 2, y - s, s - 2, s - 2, 2, 2);
                        g2.fillRoundRect(x - s, y + 2, s - 2, s - 2, 2, 2);
                        g2.fillRoundRect(x + 2, y + 2, s - 2, s - 2, 2, 2);
                        break;
                    case "calendar": // Calendar
                        g2.drawRoundRect(x - s, y - s + 2, s * 2, s * 2 - 2, 3, 3);
                        g2.drawLine(x - s + 3, y - s + 2, x - s + 3, y - s - 1);
                        g2.drawLine(x + s - 3, y - s + 2, x + s - 3, y - s - 1);
                        g2.drawLine(x - s, y - 2, x + s, y - 2);
                        break;
                    case "check": // Checkmark in box
                        g2.drawRoundRect(x - s, y - s, s * 2, s * 2, 3, 3);
                        g2.drawLine(x - 4, y, x - 1, y + 3);
                        g2.drawLine(x - 1, y + 3, x + 5, y - 4);
                        break;
                    case "search": // Magnifying glass
                        g2.drawOval(x - s + 1, y - s + 1, s + 4, s + 4);
                        g2.drawLine(x + 4, y + 4, x + s, y + s);
                        break;
                    case "file": // Document
                        g2.drawRoundRect(x - s + 2, y - s, s * 2 - 4, s * 2, 2, 2);
                        g2.drawLine(x - 3, y - 3, x + 3, y - 3);
                        g2.drawLine(x - 3, y, x + 3, y);
                        g2.drawLine(x - 3, y + 3, x + 1, y + 3);
                        break;
                    case "alert": // Warning triangle
                        int[] xPoints = {x, x - s, x + s};
                        int[] yPoints = {y - s + 2, y + s - 2, y + s - 2};
                        g2.drawPolygon(xPoints, yPoints, 3);
                        g2.drawLine(x, y - 2, x, y + 1);
                        g2.fillOval(x - 1, y + 3, 3, 3);
                        break;
                    case "plus": // Plus sign
                        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2.drawLine(x, y - s + 2, x, y + s - 2);
                        g2.drawLine(x - s + 2, y, x + s - 2, y);
                        break;
                    case "trash": // Trash can
                        g2.drawLine(x - s + 2, y - s + 2, x + s - 2, y - s + 2);
                        g2.drawRoundRect(x - s + 3, y - s + 2, s * 2 - 6, s * 2 - 2, 2, 2);
                        g2.drawLine(x - 2, y - 2, x - 2, y + 4);
                        g2.drawLine(x + 2, y - 2, x + 2, y + 4);
                        break;
                    case "download": // Download arrow
                        g2.drawLine(x, y - s + 2, x, y + 2);
                        g2.drawLine(x - 4, y - 2, x, y + 2);
                        g2.drawLine(x + 4, y - 2, x, y + 2);
                        g2.drawLine(x - s + 2, y + s - 2, x + s - 2, y + s - 2);
                        break;
                    default:
                        g2.fillOval(x - 3, y - 3, 6, 6);
                }
            }
        };
        
        button.putClientProperty("active", isActive);
        button.putClientProperty("iconType", iconType);
        button.setFont(DesignSystem.FONT_NAV);
        button.setForeground(DesignSystem.TEXT_SECONDARY);
        button.setBackground(DesignSystem.SIDEBAR_BG);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        button.setPreferredSize(new Dimension(196, 36));
        button.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        
        return button;
    }

    private void updateNavButtonStates(String activePanelId) {
        for (Component comp : navButtonsPanel.getComponents()) {
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                String name = btn.getName();
                boolean isActive = name != null && name.equals(activePanelId);
                
                // Update font and trigger repaint
                btn.setFont(isActive ? DesignSystem.FONT_NAV_ACTIVE : DesignSystem.FONT_NAV);
                btn.putClientProperty("active", isActive);
                btn.repaint();
            }
        }
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, DesignSystem.SPACING_LG, 0));

        // Greeting
        String displayName = currentUser.getUsername();
        String role = currentUser.getRole().equals("ADMIN") ? "Administrator" : "Teacher";
        JLabel greeting = new JLabel("Welcome back, " + displayName + "!");
        greeting.setFont(DesignSystem.FONT_TITLE);
        greeting.setForeground(DesignSystem.TEXT_PRIMARY);

        // Date and role
        JLabel dateLabel = new JLabel(role + " • " + java.time.LocalDate.now().format(
            java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        dateLabel.setFont(DesignSystem.FONT_BODY);
        dateLabel.setForeground(DesignSystem.TEXT_SECONDARY);
        
        // Course Filter
        courseFilter = new JComboBox<>();
        courseFilter.setFont(DesignSystem.FONT_SMALL);
        courseFilter.setBackground(DesignSystem.SURFACE);
        courseFilter.setFocusable(false);
        courseFilter.setMaximumSize(new Dimension(200, 30));
        courseFilter.addActionListener(e -> {
            String selected = (String) courseFilter.getSelectedItem();
            if (selected != null && !selected.equals(currentCourseFilter)) {
                currentCourseFilter = selected;
                refreshAllPanels();
            }
        });
        updateCourseDropdown();

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        leftPanel.add(greeting);
        leftPanel.add(Box.createVerticalStrut(DesignSystem.SPACING_XS));
        leftPanel.add(dateLabel);
        leftPanel.add(Box.createVerticalStrut(DesignSystem.SPACING_SM));
        leftPanel.add(courseFilter);

        header.add(leftPanel, BorderLayout.WEST);

        // Stats cards on the right
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, DesignSystem.SPACING_MD, 0));
        statsPanel.setOpaque(false);

        int studentCount = system.getStudentCount();
        statsPanel.add(createMiniStatCard("Total Students", new JLabel(String.valueOf(studentCount)), "users", DesignSystem.PRIMARY));
        
        // Calculate average attendance
        Student[] students = system.getAllStudents();
        double avgAttendance = 0;
        if (students.length > 0) {
            for (Student s : students) {
                avgAttendance += s.getAttendancePercentage();
            }
            avgAttendance /= students.length;
        }
        
        avgAttendanceLabel = new JLabel(String.format("%.1f%%", avgAttendance));
        statsPanel.add(createMiniStatCard("Avg Attendance", avgAttendanceLabel, "chart", DesignSystem.SUCCESS));

        header.add(statsPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createMiniStatCard(String title, JLabel valueLabel, String iconType, Color color) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(DesignSystem.SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), DesignSystem.RADIUS_MD, DesignSystem.RADIUS_MD);
                g2.setColor(DesignSystem.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, DesignSystem.RADIUS_MD, DesignSystem.RADIUS_MD);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(DesignSystem.SPACING_SM, 0));
        card.setPreferredSize(new Dimension(160, 56));
        card.setBorder(BorderFactory.createEmptyBorder(DesignSystem.SPACING_SM, DesignSystem.SPACING_MD, 
            DesignSystem.SPACING_SM, DesignSystem.SPACING_MD));

        // Icon panel with drawn icon
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                
                if (iconType.equals("users")) {
                    // Two people icon
                    g2.drawOval(cx - 8, cy - 8, 8, 8);  // head 1
                    g2.drawArc(cx - 10, cy + 2, 12, 10, 0, 180); // body 1
                    g2.drawOval(cx + 2, cy - 6, 7, 7);  // head 2
                    g2.drawArc(cx + 1, cy + 3, 10, 8, 0, 180); // body 2
                } else if (iconType.equals("chart")) {
                    // Chart trending up
                    g2.drawLine(cx - 10, cy + 8, cx - 10, cy - 8);
                    g2.drawLine(cx - 10, cy + 8, cx + 10, cy + 8);
                    g2.drawLine(cx - 6, cy + 2, cx - 2, cy - 2);
                    g2.drawLine(cx - 2, cy - 2, cx + 2, cy + 1);
                    g2.drawLine(cx + 2, cy + 1, cx + 8, cy - 6);
                }
                g2.dispose();
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(32, 32);
            }
        };
        iconPanel.setOpaque(false);
        card.add(iconPanel, BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(DesignSystem.FONT_SMALL);
        titleLabel.setForeground(DesignSystem.TEXT_MUTED);

        valueLabel.setFont(new Font(DesignSystem.FONT_FAMILY, Font.BOLD, 16));
        valueLabel.setForeground(color);

        textPanel.add(titleLabel);
        textPanel.add(valueLabel);

        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }
    
    private void updateHeaderStats() {
        if (avgAttendanceLabel == null) return;

        Student[] students = system.getAllStudents();
        double avgAttendance = 0;
        if (students.length > 0) {
            for (Student s : students) {
                avgAttendance += s.getAttendancePercentage();
            }
            avgAttendance /= students.length;
        }
        avgAttendanceLabel.setText(String.format("%.1f%%", avgAttendance));
    }

    private void initializePanels() {
        TablePanel tablePanel = new TablePanel(system, currentUser);
        registerPanel(PANEL_TABLE, tablePanel);

        AddStudentPanel addStudentPanel = new AddStudentPanel(system, currentUser);
        addStudentPanel.setOnDataChanged(() -> refreshAllPanels());
        registerPanel(PANEL_ADD_STUDENT, addStudentPanel);

        AttendancePanel attendancePanel = new AttendancePanel(system, currentUser);
        registerPanel(PANEL_ATTENDANCE, attendancePanel);

        TimetablePanel timetablePanel = new TimetablePanel(system, currentUser);
        registerPanel(PANEL_TIMETABLE, timetablePanel);
        
        AssignmentPanel assignmentPanel = new AssignmentPanel(system, currentUser);
        registerPanel(PANEL_ASSIGNMENT, assignmentPanel);
        
        EventPanel eventPanel = new EventPanel(system, currentUser);
        registerPanel(PANEL_EVENTS, eventPanel);
    }

    private void registerPanel(String id, BasePanel panel) {
        panels.put(id, panel);
        cardPanel.add(panel, id);
    }

    public void showPanel(String panelId) {
        cardLayout.show(cardPanel, panelId);
        updateNavButtonStates(panelId);
        BasePanel panel = panels.get(panelId);
        if (panel != null) {
            panel.onShow();
        }
    }

    private void refreshAllPanels() {
        updateHeaderStats();
        for (BasePanel panel : panels.values()) {
            panel.onShow();
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?",
            "Confirm Logout", JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginDialog loginDialog = new LoginDialog(null);
                loginDialog.setVisible(true);
                User user = loginDialog.getLoggedInUser();
                if (user != null) {
                    new MainGUI(user).setVisible(true);
                } else {
                    System.exit(0);
                }
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            LoginDialog loginDialog = new LoginDialog(null);
            loginDialog.setVisible(true);

            User user = loginDialog.getLoggedInUser();
            if (user != null) {
                new MainGUI(user).setVisible(true);
            } else {
                System.exit(0);
            }
        });
    }
}
