package AttendanceSystem.ui;

import AttendanceSystem.AttendanceSystem;
import AttendanceSystem.Event;
import AttendanceSystem.EventPhoto;
import AttendanceSystem.User;
import AttendanceSystem.ClassSession;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javax.imageio.ImageIO;

public class EventPanel extends BasePanel {
    private JTable eventTable;
    private DefaultTableModel eventModel;
    
    // Components for Photo Management
    private JPanel photoGridPanel;
    private JScrollPane photoScroll;
    private JSplitPane splitPane;

    public EventPanel(AttendanceSystem system, User currentUser) {
        super(system, currentUser);
        initComponents();
    }

    private void initComponents() {
        removeAll();
        setLayout(new BorderLayout(DesignSystem.SPACING_MD, DesignSystem.SPACING_MD));
        setBackground(DesignSystem.BACKGROUND);
        
        System.out.println("DEBUG: EventPanel initialized for user: " + currentUser.getUsername() + ", Role: " + currentUser.getRole());

        // Top Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setOpaque(false);
        
        JLabel title = new JLabel("Events");
        title.setFont(DesignSystem.FONT_TITLE);
        toolbar.add(title);
        
        toolbar.add(Box.createHorizontalStrut(20));

        if ("TEACHER".equals(currentUser.getRole())) {
            JButton createBtn = DesignSystem.createButton("Create Event", DesignSystem.PRIMARY);
            createBtn.addActionListener(e -> showCreateDialog());
            toolbar.add(createBtn);
        } else if ("ADMIN".equals(currentUser.getRole())) {
            JButton approveBtn = DesignSystem.createButton("Approve Event", DesignSystem.SUCCESS);
            JButton rejectBtn = DesignSystem.createButton("Reject Event", DesignSystem.DANGER);
            
            approveBtn.addActionListener(e -> processEvent("APPROVED"));
            rejectBtn.addActionListener(e -> processEvent("REJECTED"));
            
            toolbar.add(approveBtn);
            toolbar.add(rejectBtn);
        } else if ("STUDENT".equals(currentUser.getRole())) {
            JButton uploadBtn = DesignSystem.createButton("Upload Photo", DesignSystem.PRIMARY);
            uploadBtn.addActionListener(e -> showUploadDialog());
            toolbar.add(uploadBtn);
        }

        add(toolbar, BorderLayout.NORTH);

        // --- SPLIT PANE SETUP ---
        
        // LEFT: Event List
        String[] eventCols;
        if ("STUDENT".equals(currentUser.getRole())) {
            eventCols = new String[]{"ID", "Title", "Date", "Location", "Organizer"}; 
        } else {
            eventCols = new String[]{"ID", "Title", "Date", "Location", "Organizer", "Status"};
        }
        
        eventModel = new DefaultTableModel(eventCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        eventTable = new JTable(eventModel);
        DesignSystem.styleTable(eventTable);
        
        // Listener for event selection -> load photos
        eventTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadPhotosForSelectedEvent();
            }
        });

        JScrollPane eventScroll = new JScrollPane(eventTable);
        eventScroll.setBorder(BorderFactory.createTitledBorder("Events List"));
        eventScroll.getViewport().setBackground(Color.WHITE);

        // RIGHT: Photo Grid (Approvals/View)
        photoGridPanel = new JPanel();
        photoGridPanel.setLayout(new GridLayout(0, 2, 10, 10)); // 2 Columns, dynamic rows
        photoGridPanel.setBackground(Color.WHITE);
        photoGridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        photoScroll = new JScrollPane(photoGridPanel);
        photoScroll.setBorder(BorderFactory.createTitledBorder("Event Photos Gallery"));
        photoScroll.getViewport().setBackground(Color.WHITE);
        photoScroll.getVerticalScrollBar().setUnitIncrement(16);
        
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, eventScroll, photoScroll);
        splitPane.setDividerLocation(500);
        splitPane.setResizeWeight(0.4);
        
        add(splitPane, BorderLayout.CENTER);
    }

    @Override
    public void onShow() {
        refreshData();
    }

    private void refreshData() {
        eventModel.setRowCount(0);
        List<Event> list;
        
        if ("STUDENT".equals(currentUser.getRole())) {
            list = system.getApprovedEvents();
            for (Event e : list) {
                eventModel.addRow(new Object[]{
                    e.getId(), e.getTitle(), e.getDate(), e.getLocation(), e.getOrganizer()
                });
            }
        } else {
            list = system.getAllEvents();
            for (Event e : list) {
                eventModel.addRow(new Object[]{
                    e.getId(), e.getTitle(), e.getDate(), e.getLocation(), e.getOrganizer(), e.getStatus()
                });
            }
        }
        
        // Clear grid when list refreshes
        photoGridPanel.removeAll();
        photoGridPanel.revalidate();
        photoGridPanel.repaint();
    }
    
    private void loadPhotosForSelectedEvent() {
        int row = eventTable.getSelectedRow();
        if (row == -1) {
            photoGridPanel.removeAll();
            photoGridPanel.revalidate();
            photoGridPanel.repaint();
            return;
        }
        
        String eventId = (String) eventModel.getValueAt(row, 0);
        List<EventPhoto> photos = system.getPhotosForEvent(eventId);
        
        photoGridPanel.removeAll();
        
        boolean hasPhotos = false;
        
        for (EventPhoto p : photos) {
            boolean show = false;
            if ("TEACHER".equals(currentUser.getRole()) || "ADMIN".equals(currentUser.getRole())) {
                show = true;
            } else if ("STUDENT".equals(currentUser.getRole())) {
                // Show if it's mine OR it's APPROVED
                if (p.getStudentId().equals(currentUser.getStudentId()) || "APPROVED".equals(p.getStatus())) {
                    show = true;
                }
            }
            
            if (show) {
                hasPhotos = true;
                photoGridPanel.add(createPhotoCard(p));
            }
        }
        
        if (!hasPhotos) {
            JLabel noPhotos = new JLabel("No photos to display");
            noPhotos.setHorizontalAlignment(SwingConstants.CENTER);
            noPhotos.setForeground(Color.GRAY);
            photoGridPanel.add(noPhotos);
        }
        
        photoGridPanel.revalidate();
        photoGridPanel.repaint();
    }
    
    private JPanel createPhotoCard(EventPhoto p) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(new LineBorder(new Color(220, 220, 220), 1, true));
        card.setPreferredSize(new Dimension(200, 250)); // Fixed size for grid
        
        // 1. Image Thumbnail
        JLabel imgLabel = new JLabel();
        imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imgLabel.setBackground(new Color(245, 245, 245));
        imgLabel.setOpaque(true);
        
        // Load image async-ish (just direct for now, it's a prototype)
        try {
            File imgFile = new File(p.getFilePath());
            if (imgFile.exists() && !imgFile.isDirectory()) {
                ImageIcon icon = new ImageIcon(new ImageIcon(imgFile.getAbsolutePath()).getImage().getScaledInstance(180, 140, Image.SCALE_SMOOTH));
                imgLabel.setIcon(icon);
            } else {
                imgLabel.setText("Image not found");
            }
        } catch (Exception e) {
            imgLabel.setText("Error loading image");
        }
        
        card.add(imgLabel, BorderLayout.CENTER);
        
        // 2. Info Panel (Name + Status)
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
        
        JLabel nameLbl = new JLabel(p.getStudentName());
        nameLbl.setFont(new Font(DesignSystem.FONT_FAMILY, Font.BOLD, 12));
        
        JLabel statusLbl = new JLabel(p.getStatus());
        statusLbl.setFont(DesignSystem.FONT_SMALL);
        if ("APPROVED".equals(p.getStatus())) statusLbl.setForeground(DesignSystem.SUCCESS);
        else if ("REJECTED".equals(p.getStatus())) statusLbl.setForeground(DesignSystem.DANGER);
        else statusLbl.setForeground(Color.GRAY);
        
        infoPanel.add(nameLbl);
        infoPanel.add(statusLbl);
        
        // 3. Actions (Teacher only)
        if ("TEACHER".equals(currentUser.getRole()) && "PENDING".equals(p.getStatus())) {
            JPanel btnPanel = new JPanel(new GridLayout(1, 2, 5, 0));
            btnPanel.setBackground(Color.WHITE);
            btnPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            
            JButton approve = new JButton("✔"); // Compact check
            approve.setForeground(DesignSystem.SUCCESS);
            approve.addActionListener(e -> updatePhotoStatus(p, "APPROVED"));
            
            JButton reject = new JButton("✖"); // Compact x
            reject.setForeground(DesignSystem.DANGER);
            reject.addActionListener(e -> updatePhotoStatus(p, "REJECTED"));
            
            btnPanel.add(approve);
            btnPanel.add(reject);
            
            JPanel bottomWrapper = new JPanel(new BorderLayout());
            bottomWrapper.add(infoPanel, BorderLayout.CENTER);
            bottomWrapper.add(btnPanel, BorderLayout.SOUTH);
            card.add(bottomWrapper, BorderLayout.SOUTH);
        } else {
            card.add(infoPanel, BorderLayout.SOUTH);
        }
        
        return card;
    }
    
    private void updatePhotoStatus(EventPhoto p, String status) {
        p.setStatus(status);
        system.updateEventPhoto(p);
        
        // If Approved, Mark Attendance for affected sessions
        if ("APPROVED".equals(status)) {
            // Get event to find affected sessions
            Event event = null;
            for (Event e : system.getAllEvents()) {
                if (e.getId().equals(p.getEventId())) {
                    event = e;
                    break;
                }
            }
            
            if (event != null && event.getAffectedSessions() != null && !event.getAffectedSessions().isEmpty()) {
                String[] sessions = event.getAffectedSessions().split(",");
                for (String sKey : sessions) {
                    // Key format: TIME#SUBJECT
                    // Full key for attendance: DATE#TIME#SUBJECT
                    String fullKey = event.getDate() + "#" + sKey;
                    
                    // Mark attendance
                    system.markAttendanceForSession(p.getStudentId(), fullKey, true);
                }
                JOptionPane.showMessageDialog(this, "Attendance marked for " + sessions.length + " sessions!");
            }
        }
        
        loadPhotosForSelectedEvent(); // Refresh UI
    }

    private void showCreateDialog() {
        JTextField titleField = new JTextField();
        JTextField descField = new JTextField();
        JTextField dateField = new JTextField("2025-12-");
        JTextField timeField = new JTextField("10:00 AM");
        JTextField locField = new JTextField("Auditorium");
        
        // Session Selector
        DefaultListModel<String> sessionModel = new DefaultListModel<>();
        JList<String> sessionList = new JList<>(sessionModel);
        sessionList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane sessionScroll = new JScrollPane(sessionList);
        sessionScroll.setPreferredSize(new Dimension(200, 100));
        sessionScroll.setBorder(BorderFactory.createTitledBorder("Select Sessions to Replace"));
        
        // Populate sessions when date changes (simplified: load all first, filter later?)
        // Or just load generic "Day" sessions. 
        // For prototype: Add a "Load Sessions" button
        JButton loadSessionsBtn = new JButton("Load Sessions for Date");
        loadSessionsBtn.addActionListener(e -> {
            String date = dateField.getText();
            sessionModel.clear();
            try {
                java.time.LocalDate ld = java.time.LocalDate.parse(date);
                String dayStr = ld.getDayOfWeek().toString().substring(0, 3);
                for (ClassSession s : system.getSessionsByDay(dayStr, "All Courses")) {
                    // Display: TIME - SUBJECT (TEACHER)
                    sessionModel.addElement(s.getTimeSlot() + "#" + s.getSubject() + " (" + s.getTeacher() + ")");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid Date Format (YYYY-MM-DD)");
            }
        });

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        JPanel fieldsPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        fieldsPanel.add(new JLabel("Title:")); fieldsPanel.add(titleField);
        fieldsPanel.add(new JLabel("Description:")); fieldsPanel.add(descField);
        fieldsPanel.add(new JLabel("Date (YYYY-MM-DD):")); fieldsPanel.add(dateField);
        fieldsPanel.add(new JLabel("Time:")); fieldsPanel.add(timeField);
        fieldsPanel.add(new JLabel("Location:")); fieldsPanel.add(locField);
        fieldsPanel.add(new JLabel("")); fieldsPanel.add(loadSessionsBtn);
        
        panel.add(fieldsPanel, BorderLayout.NORTH);
        panel.add(sessionScroll, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, panel, "Create Event", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            if (titleField.getText().isEmpty()) {
                showError("Title is required.");
                return;
            }
            
            // Collect selected sessions
            List<String> selected = sessionList.getSelectedValuesList();
            StringBuilder sb = new StringBuilder();
            for (String s : selected) {
                // Parse out "TIME#SUBJECT" from "TIME#SUBJECT (TEACHER)"
                // Actually we just stored "TIME#SUBJECT..." in the model value display.
                // We'll store the exact string needed for the key: "TIME#SUBJECT"
                // Model element: "10:00-11:00#Math (Mr. X)"
                String key = s.split(" \\(")[0]; // "10:00-11:00#Math"
                sb.append(key).append(",");
            }
            String affected = sb.toString();
            if (affected.length() > 0) affected = affected.substring(0, affected.length() - 1);
            
            Event event = new Event(
                titleField.getText(),
                descField.getText(),
                dateField.getText(),
                timeField.getText(),
                locField.getText(),
                currentUser.getUsername(),
                affected
            );
            system.addEvent(event);
            refreshData();
            showSuccess("Event Created! Waiting for Admin approval.");
        }
    }
    
    private void showUploadDialog() {
        int row = eventTable.getSelectedRow();
        if (row == -1) {
            showError("Please select an event first.");
            return;
        }
        
        String eventId = (String) eventModel.getValueAt(row, 0);
        
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            EventPhoto photo = new EventPhoto(
                eventId,
                currentUser.getStudentId(),
                currentUser.getUsername(),
                file.getAbsolutePath() // Changed to Absolute Path for image loading
            );
            system.addEventPhoto(photo);
            loadPhotosForSelectedEvent(); // Refresh photo list
            showSuccess("Photo uploaded for approval!");
        }
    }
    
    private void processEvent(String newStatus) {
        int row = eventTable.getSelectedRow();
        if (row == -1) {
            showError("Please select an event.");
            return;
        }
        
        String id = (String) eventModel.getValueAt(row, 0);
        for (Event e : system.getAllEvents()) {
            if (e.getId().equals(id)) {
                e.setStatus(newStatus);
                system.addEvent(e);
                break;
            }
        }
        refreshData();
    }
}
