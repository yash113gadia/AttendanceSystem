package AttendanceSystem.ui;

import AttendanceSystem.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

// Ensure DatePickerDialog is visible to this class
import AttendanceSystem.ui.DatePickerDialog;

/**
 * Panel for marking student attendance.
 * Date-dependent attendance marking with history support.
 */
public class AttendancePanel extends BasePanel {
    private LocalDate currentDate;
    private final LocalDate START_DATE = LocalDate.of(2025, 12, 1);
    private JScrollPane contentScrollPane;
    private JLabel dateLabel;
    private JButton prevBtn;
    private JButton nextBtn;
    
    public AttendancePanel(AttendanceSystem system, User currentUser) {
        super(system, currentUser);
        this.currentDate = LocalDate.now();
        if (this.currentDate.isBefore(START_DATE)) {
            this.currentDate = START_DATE;
        }
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
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = DesignSystem.createHeading("Mark Attendance");
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitleLabel = new JLabel("Mark or edit attendance for specific dates");
        subtitleLabel.setFont(DesignSystem.FONT_BODY);
        subtitleLabel.setForeground(DesignSystem.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(DesignSystem.SPACING_XS));
        titlePanel.add(subtitleLabel);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(createLegendPanel(), BorderLayout.EAST);
        
        card.add(headerPanel, BorderLayout.NORTH);
        
        // Date Selector
        JPanel controlsPanel = createDateControls();
        
        // Content area
        JPanel contentCard = new JPanel(new BorderLayout());
        contentCard.setOpaque(false);
        contentCard.add(controlsPanel, BorderLayout.NORTH);
        
        contentScrollPane = new JScrollPane();
        contentScrollPane.setBorder(BorderFactory.createEmptyBorder());
        contentScrollPane.setBackground(DesignSystem.BACKGROUND);
        contentScrollPane.getViewport().setBackground(DesignSystem.BACKGROUND);
        contentScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        contentScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        contentCard.add(contentScrollPane, BorderLayout.CENTER);
        card.add(contentCard, BorderLayout.CENTER);
        
        add(card, BorderLayout.CENTER);
        
        // Initial load
        refreshAttendanceGrid();
    }
    
    private JPanel createDateControls() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, DesignSystem.SPACING_MD, 0));
        
        // Center group for arrows + label
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
        centerPanel.setOpaque(false);
        
        prevBtn = createNavButton("<", () -> navigateDate(-1));
        nextBtn = createNavButton(">", () -> navigateDate(1));
        
        dateLabel = new JLabel();
        dateLabel.setFont(DesignSystem.FONT_TITLE);
        dateLabel.setForeground(DesignSystem.PRIMARY);
        dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dateLabel.setBorder(BorderFactory.createEmptyBorder(0, DesignSystem.SPACING_MD, 0, DesignSystem.SPACING_MD));
        
        JPanel dateSwitcher = new JPanel(new BorderLayout());
        dateSwitcher.setOpaque(false);
        dateSwitcher.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DesignSystem.BORDER, 1),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        dateSwitcher.setMaximumSize(new Dimension(420, 40));
        
        dateSwitcher.add(prevBtn, BorderLayout.WEST);
        dateSwitcher.add(dateLabel, BorderLayout.CENTER);
        dateSwitcher.add(nextBtn, BorderLayout.EAST);
        
        centerPanel.add(dateSwitcher);
        
        // Right side buttons (Today + calendar)
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, DesignSystem.SPACING_SM, 0));
        rightPanel.setOpaque(false);
        
        JButton todayBtn = DesignSystem.createButton("Today", DesignSystem.PRIMARY_LIGHT);
        todayBtn.setForeground(DesignSystem.PRIMARY);
        todayBtn.setPreferredSize(new Dimension(90, 36));
        todayBtn.addActionListener(e -> {
            currentDate = LocalDate.now();
            refreshAttendanceGrid();
        });
        
        // Custom drawn calendar icon button (no emoji)
        JButton calendarBtn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2.setColor(DesignSystem.BORDER);
                } else if (getModel().isRollover()) {
                    g2.setColor(DesignSystem.PRIMARY_LIGHT);
                } else {
                    g2.setColor(DesignSystem.SURFACE);
                }
                
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), DesignSystem.RADIUS_SM, DesignSystem.RADIUS_SM);
                g2.setColor(DesignSystem.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, DesignSystem.RADIUS_SM, DesignSystem.RADIUS_SM);
                
                // Draw calendar glyph
                int w = getWidth();
                int h = getHeight();
                int cx = w / 2;
                int cy = h / 2;
                int calW = 18;
                int calH = 16;
                int x = cx - calW / 2;
                int y = cy - calH / 2;
                
                g2.setColor(DesignSystem.TEXT_PRIMARY);
                g2.drawRoundRect(x, y, calW, calH, 4, 4);
                g2.drawLine(x, y + 4, x + calW, y + 4); // header line
                g2.drawLine(x + 4, y, x + 4, y - 3);     // left ring
                g2.drawLine(x + calW - 4, y, x + calW - 4, y - 3); // right ring
                
                // Simple date dots
                g2.fillOval(x + 4, y + 7, 2, 2);
                g2.fillOval(x + 9, y + 7, 2, 2);
                g2.fillOval(x + 14, y + 7, 2, 2);
                g2.fillOval(x + 4, y + 11, 2, 2);
                g2.fillOval(x + 9, y + 11, 2, 2);
                g2.fillOval(x + 14, y + 11, 2, 2);
                
                g2.dispose();
            }
        };
        calendarBtn.setPreferredSize(new Dimension(40, 36));
        calendarBtn.setBorderPainted(false);
        calendarBtn.setContentAreaFilled(false);
        calendarBtn.setFocusPainted(false);
        calendarBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        calendarBtn.setToolTipText("Select Date");
        calendarBtn.addActionListener(e -> {
            DatePickerDialog dialog = new DatePickerDialog((JFrame) SwingUtilities.getWindowAncestor(this), currentDate);
            dialog.setVisible(true);
            if (dialog.isConfirmed()) {
                LocalDate selected = dialog.getSelectedDate();
                // Allow viewing any date, but prevent marking before START_DATE in toggle handler
                currentDate = selected;
                refreshAttendanceGrid();
            }
        });
        
        rightPanel.add(todayBtn);
        rightPanel.add(calendarBtn);
        
        wrapper.add(centerPanel, BorderLayout.CENTER);
        wrapper.add(rightPanel, BorderLayout.EAST);
        return wrapper;
    }
    
    private JButton createNavButton(String text, Runnable action) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2.setColor(DesignSystem.BORDER);
                else if (getModel().isRollover()) g2.setColor(DesignSystem.SURFACE);
                else g2.setColor(DesignSystem.BACKGROUND);
                
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), DesignSystem.RADIUS_SM, DesignSystem.RADIUS_SM);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(DesignSystem.FONT_BODY_BOLD);
        btn.setForeground(DesignSystem.TEXT_PRIMARY);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(40, 36));
        btn.addActionListener(e -> action.run());
        return btn;
    }
    
    private void navigateDate(int days) {
        LocalDate newDate = currentDate.plusDays(days);
        currentDate = newDate;
        refreshAttendanceGrid();
    }
    
    private JPanel createLegendPanel() {
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, DesignSystem.SPACING_MD, 0));
        legendPanel.setOpaque(false);
        
        legendPanel.add(createLegendItem("Present", DesignSystem.SUCCESS));
        legendPanel.add(createLegendItem("Absent", DesignSystem.DANGER));
        
        return legendPanel;
    }
    
    private JPanel createLegendItem(String text, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, DesignSystem.SPACING_XS, 0));
        item.setOpaque(false);
        
        JLabel dot = new JLabel("●");
        dot.setForeground(color);
        
        JLabel label = new JLabel(text);
        label.setFont(DesignSystem.FONT_SMALL);
        label.setForeground(DesignSystem.TEXT_SECONDARY);
        
        item.add(dot);
        item.add(label);
        return item;
    }
    
    private void refreshAttendanceGrid() {
        // Update Label
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy");
        dateLabel.setText(currentDate.format(formatter));
        
        // Disable prev button if at start date
        prevBtn.setEnabled(!currentDate.equals(START_DATE));
        
        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new BoxLayout(gridPanel, BoxLayout.Y_AXIS));
        gridPanel.setBackground(DesignSystem.BACKGROUND);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(DesignSystem.SPACING_SM, 0, DesignSystem.SPACING_SM, 0));
        
        String dayOfWeek = getDayString(currentDate);
        ArrayList<ClassSession> sessions = system.getSessionsByDay(dayOfWeek);
        Student[] allStudents = system.getAllStudents();
        
        // Filter students
        String currentFilter = "All Courses";
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof MainGUI) {
            currentFilter = ((MainGUI) window).getCurrentCourseFilter();
        }
        
        java.util.List<Student> filteredList = new ArrayList<>();
        for (Student s : allStudents) {
            if (currentFilter.equals("All Courses") || s.getCourse().equals(currentFilter)) {
                filteredList.add(s);
            }
        }
        Student[] students = filteredList.toArray(new Student[0]);
        
        if (students.length == 0) {
            JPanel emptyPanel = new JPanel(new GridBagLayout());
            emptyPanel.setOpaque(false);
            emptyPanel.setPreferredSize(new Dimension(400, 200));
            
            JPanel emptyCard = new JPanel();
            emptyCard.setLayout(new BoxLayout(emptyCard, BoxLayout.Y_AXIS));
            emptyCard.setOpaque(false);
            
            JLabel emptyIcon = new JLabel("📭");
            emptyIcon.setFont(new Font(DesignSystem.FONT_FAMILY, Font.PLAIN, 48));
            emptyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JLabel emptyLabel = new JLabel("No students found");
            emptyLabel.setFont(DesignSystem.FONT_BODY);
            emptyLabel.setForeground(DesignSystem.TEXT_MUTED);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            emptyCard.add(emptyIcon);
            emptyCard.add(Box.createVerticalStrut(DesignSystem.SPACING_SM));
            emptyCard.add(emptyLabel);
            
            emptyPanel.add(emptyCard);
            gridPanel.add(emptyPanel);
            contentScrollPane.setViewportView(gridPanel);
            return;
        }
        
        boolean hasContent = false;
        for (ClassSession session : sessions) {
            if (!currentUser.canAccessSubject(session.getSubject())) {
                continue;
            }
            hasContent = true;
            
            JPanel sessionCard = createSessionCard(session, students);
            gridPanel.add(sessionCard);
            gridPanel.add(Box.createVerticalStrut(DesignSystem.SPACING_MD));
        }
        
        if (!hasContent) {
            JPanel emptyPanel = new JPanel(new GridBagLayout());
            emptyPanel.setOpaque(false);
            emptyPanel.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));
            JLabel emptyLabel = new JLabel("No sessions scheduled for " + dayOfWeek);
            emptyLabel.setFont(DesignSystem.FONT_BODY);
            emptyLabel.setForeground(DesignSystem.TEXT_MUTED);
            emptyPanel.add(emptyLabel);
            gridPanel.add(emptyPanel);
        }
        
        contentScrollPane.setViewportView(gridPanel);
        revalidate();
        repaint();
    }
    
    private JPanel createSessionCard(ClassSession session, Student[] students) {
        JPanel card = new JPanel(new BorderLayout(0, DesignSystem.SPACING_MD)) {
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
        card.setBorder(BorderFactory.createEmptyBorder(
            DesignSystem.SPACING_MD, DesignSystem.SPACING_MD, 
            DesignSystem.SPACING_MD, DesignSystem.SPACING_MD));
            
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JPanel titleArea = new JPanel();
        titleArea.setLayout(new BoxLayout(titleArea, BoxLayout.Y_AXIS));
        titleArea.setOpaque(false);
        
        JLabel subjectLabel = new JLabel(session.getSubject());
        subjectLabel.setFont(DesignSystem.FONT_SUBHEADING);
        subjectLabel.setForeground(DesignSystem.TEXT_PRIMARY);
        
        JLabel detailsLabel = new JLabel(session.getTimeSlot() + " • " + session.getFacultyRoom());
        detailsLabel.setFont(DesignSystem.FONT_SMALL);
        detailsLabel.setForeground(DesignSystem.TEXT_MUTED);
        
        titleArea.add(subjectLabel);
        titleArea.add(detailsLabel);
        headerPanel.add(titleArea, BorderLayout.WEST);
        
        // UNIQUE KEY GENERATION INCLUDING DATE
        String sessionKey = generateSessionKey(session);
        
        int presentCount = 0;
        for (Student s : students) {
            if (s.getSessionAttendance().containsKey(sessionKey) && s.getAttendanceForSession(sessionKey)) {
                presentCount++;
            }
        }
        
        // Student boxes grid - declared before button to be accessible
        JPanel studentGrid = new JPanel(new GridLayout(0, 5, DesignSystem.SPACING_SM, DesignSystem.SPACING_SM));
        studentGrid.setOpaque(false);
        
        for (Student student : students) {
            studentGrid.add(createStudentBox(student, session, sessionKey));
        }

        // Stats Panel
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, DesignSystem.SPACING_MD, 0));
        statsPanel.setOpaque(false);
        
        JLabel presentLabel = new JLabel("✓ " + presentCount);
        presentLabel.setFont(DesignSystem.FONT_BODY_BOLD);
        presentLabel.setForeground(DesignSystem.SUCCESS);
        
        JLabel absentLabel = new JLabel("✗ " + (students.length - presentCount));
        absentLabel.setFont(DesignSystem.FONT_BODY_BOLD);
        absentLabel.setForeground(DesignSystem.DANGER);
        
        JButton markAllBtn = new JButton("Mark All Present");
        markAllBtn.setFont(DesignSystem.FONT_SMALL);
        markAllBtn.setForeground(DesignSystem.PRIMARY);
        markAllBtn.setBackground(DesignSystem.SURFACE);
        markAllBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        markAllBtn.addActionListener(e -> {
            for (Student s : students) {
                system.markAttendanceForSession(s.getId(), sessionKey, true);
            }
            notifyDataChanged();
            
            // Optimistic UI Update for all boxes
            for (Component comp : studentGrid.getComponents()) {
                if (comp instanceof JPanel) {
                    JPanel box = (JPanel) comp;
                    box.setBackground(new Color(220, 252, 231)); // Success color
                    
                    // Find the status label (last component added to box)
                    Component[] boxComps = box.getComponents();
                    if (boxComps.length > 0) {
                        Component lastComp = boxComps[boxComps.length - 1];
                        if (lastComp instanceof JLabel) {
                            JLabel status = (JLabel) lastComp;
                            status.setText("✓");
                            status.setForeground(DesignSystem.SUCCESS);
                        }
                    }
                    box.repaint();
                }
            }
            
            // Update stats labels
            presentLabel.setText("✓ " + students.length);
            absentLabel.setText("✗ 0");
        });
        
        statsPanel.add(markAllBtn);
        statsPanel.add(presentLabel);
        statsPanel.add(absentLabel);
        headerPanel.add(statsPanel, BorderLayout.EAST);
        
        card.add(headerPanel, BorderLayout.NORTH);
        
        card.add(studentGrid, BorderLayout.CENTER);
        
        return card;
    }
    
    private String generateSessionKey(ClassSession session) {
        // Key format: YYYY-MM-DD#TIMESLOT#SUBJECT
        // We use # to be safe with colon in time, matching Student.java changes
        return currentDate.toString() + "#" + session.getTimeSlot() + "#" + session.getSubject();
    }
    
    private JPanel createStudentBox(Student student, ClassSession session, String sessionKey) {
        boolean isPresent = student.getSessionAttendance().containsKey(sessionKey) 
            && student.getAttendanceForSession(sessionKey);
        
        Color bgColor = isPresent ? new Color(220, 252, 231) : new Color(254, 226, 226);
        
        JPanel box = new JPanel(new BorderLayout(DesignSystem.SPACING_XS, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), DesignSystem.RADIUS_SM, DesignSystem.RADIUS_SM);
                g2.dispose();
            }
        };
        box.setOpaque(false);
        box.setBackground(bgColor);
        box.setBorder(BorderFactory.createEmptyBorder(
            DesignSystem.SPACING_SM, DesignSystem.SPACING_SM, 
            DesignSystem.SPACING_SM, DesignSystem.SPACING_SM));
        box.setCursor(new Cursor(Cursor.HAND_CURSOR));
        box.setPreferredSize(new Dimension(150, 50));
        
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        
        JLabel nameLabel = new JLabel(truncateName(student.getName(), 20));
        nameLabel.setFont(DesignSystem.FONT_SMALL);
        nameLabel.setForeground(DesignSystem.TEXT_PRIMARY);
        
        JLabel idLabel = new JLabel(student.getId());
        idLabel.setFont(new Font(DesignSystem.FONT_FAMILY, Font.PLAIN, 10));
        idLabel.setForeground(DesignSystem.TEXT_MUTED);
        
        infoPanel.add(nameLabel);
        infoPanel.add(idLabel);
        box.add(infoPanel, BorderLayout.CENTER);
        
        JLabel statusLabel = new JLabel(isPresent ? "✓" : "✗");
        statusLabel.setFont(new Font(DesignSystem.FONT_FAMILY, Font.BOLD, 18));
        statusLabel.setForeground(isPresent ? DesignSystem.SUCCESS : DesignSystem.DANGER);
        box.add(statusLabel, BorderLayout.EAST);
        
        box.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                boolean currentStatus = student.getSessionAttendance().containsKey(sessionKey) 
                    && student.getAttendanceForSession(sessionKey);
                boolean newStatus = !currentStatus;
                
                system.markAttendanceForSession(student.getId(), sessionKey, newStatus);
                
                // Visual update (optimistic)
                box.setBackground(newStatus ? new Color(220, 252, 231) : new Color(254, 226, 226));
                statusLabel.setText(newStatus ? "✓" : "✗");
                statusLabel.setForeground(newStatus ? DesignSystem.SUCCESS : DesignSystem.DANGER);
                box.repaint();
                
                notifyDataChanged();
                // Avoid full refresh to keep scroll position
                // refreshAttendanceGrid(); 
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                box.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(DesignSystem.PRIMARY, 2),
                    BorderFactory.createEmptyBorder(
                        DesignSystem.SPACING_SM - 2, DesignSystem.SPACING_SM - 2, 
                        DesignSystem.SPACING_SM - 2, DesignSystem.SPACING_SM - 2)
                ));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                box.setBorder(BorderFactory.createEmptyBorder(
                    DesignSystem.SPACING_SM, DesignSystem.SPACING_SM, 
                    DesignSystem.SPACING_SM, DesignSystem.SPACING_SM));
            }
        });
        
        return box;
    }
    
    private String truncateName(String name, int maxLen) {
        if (name.length() <= maxLen) return name;
        return name.substring(0, maxLen - 2) + "..";
    }
    
    private String getDayString(LocalDate date) {
        switch (date.getDayOfWeek()) {
            case MONDAY: return "MON";
            case TUESDAY: return "TUE";
            case WEDNESDAY: return "WED";
            case THURSDAY: return "THU";
            case FRIDAY: return "FRI";
            case SATURDAY: return "SAT";
            case SUNDAY: return "SUN";
            default: return "";
        }
    }
    
    @Override
    public void onShow() {
        refreshAttendanceGrid();
    }
}