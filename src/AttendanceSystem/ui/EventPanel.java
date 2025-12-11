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
            
            // Delete Event Button for Teachers
            JButton deleteEventBtn = DesignSystem.createButton("Delete Event", DesignSystem.DANGER);
            deleteEventBtn.addActionListener(e -> deleteSelectedEvent());
            toolbar.add(Box.createHorizontalStrut(10));
            toolbar.add(deleteEventBtn);
            
        } else if ("ADMIN".equals(currentUser.getRole())) {
            JButton approveBtn = DesignSystem.createButton("Approve Event", DesignSystem.SUCCESS);
            JButton rejectBtn = DesignSystem.createButton("Reject Event", DesignSystem.DANGER);
            JButton deleteEventBtn = DesignSystem.createButton("Delete Event", DesignSystem.DANGER);
            
            approveBtn.addActionListener(e -> processEvent("APPROVED"));
            rejectBtn.addActionListener(e -> processEvent("REJECTED"));
            deleteEventBtn.addActionListener(e -> deleteSelectedEvent());
            
            toolbar.add(approveBtn);
            toolbar.add(rejectBtn);
            toolbar.add(Box.createHorizontalStrut(10));
            toolbar.add(deleteEventBtn);
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
            eventCols = new String[]{"ID", "Title", "Date", "Location", "Passcode", "Status"};
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
        splitPane.setDividerLocation(600);
        splitPane.setResizeWeight(0.6);
        
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
                    e.getId(), e.getTitle(), e.getDate(), e.getLocation(), e.getPasscode(), e.getStatus()
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
        if ("TEACHER".equals(currentUser.getRole())) {
            JPanel btnPanel = new JPanel(new GridLayout(1, 3, 5, 0));
            btnPanel.setBackground(Color.WHITE);
            btnPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            
            JButton approve = new JButton("✔");
            approve.setForeground(DesignSystem.SUCCESS);
            approve.setToolTipText("Approve");
            approve.addActionListener(e -> updatePhotoStatus(p, "APPROVED"));
            
            JButton reject = new JButton("✖");
            reject.setForeground(DesignSystem.DANGER);
            reject.setToolTipText("Reject");
            reject.addActionListener(e -> updatePhotoStatus(p, "REJECTED"));
            
            JButton delete = new JButton("🗑");
            delete.setForeground(DesignSystem.TEXT_SECONDARY);
            delete.setToolTipText("Delete Photo");
            delete.addActionListener(e -> deletePhoto(p));
            
            btnPanel.add(approve);
            btnPanel.add(reject);
            btnPanel.add(delete);
            
            JPanel bottomWrapper = new JPanel(new BorderLayout());
            bottomWrapper.add(infoPanel, BorderLayout.CENTER);
            bottomWrapper.add(btnPanel, BorderLayout.SOUTH);
            card.add(bottomWrapper, BorderLayout.SOUTH);
        } else {
            card.add(infoPanel, BorderLayout.SOUTH);
        }
        
        return card;
    }
    
    private void deleteSelectedEvent() {
        int row = eventTable.getSelectedRow();
        if (row == -1) {
            showError("Please select an event to delete.");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this event? All associated photos will be removed.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
        if (confirm == JOptionPane.YES_OPTION) {
            String eventId = (String) eventModel.getValueAt(row, 0);
            system.deleteEvent(eventId);
            refreshData();
            photoGridPanel.removeAll(); // Clear detail view
            photoGridPanel.revalidate();
            photoGridPanel.repaint();
            showSuccess("Event deleted.");
        }
    }
    
    private void deletePhoto(EventPhoto p) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete this photo?", "Confirm", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            system.deleteEventPhoto(p.getEventId(), p.getStudentId(), p.getFilePath());
            loadPhotosForSelectedEvent();
        }
    }
    
    private void updatePhotoStatus(EventPhoto p, String status) {
        p.setStatus(status);
        system.updateEventPhoto(p);
        
        // If Approved, Mark Attendance for affected sessions
        if ("APPROVED".equals(status)) {
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
                    
                    // Mark attendance using the synchronized method
                    system.markEventAttendance(p.getStudentId(), fullKey, true);
                }
                JOptionPane.showMessageDialog(this, "Attendance marked for " + sessions.length + " sessions!");
            }
        }
        
        loadPhotosForSelectedEvent(); // Refresh UI
    }

    private void showCreateDialog() {
        JTextField titleField = new JTextField();
        JTextField descField = new JTextField();
        JTextField timeField = new JTextField("10:00 AM");
        JTextField locField = new JTextField("Auditorium");
        
        // Date Selection
        JLabel dateLabel = new JLabel("Click to select date");
        dateLabel.setForeground(DesignSystem.TEXT_PRIMARY);
        JButton dateBtn = new JButton("Select Date");
        final String[] selectedDateStr = {""}; // Wrapper for lambda access
        
        // Session Selector
        DefaultListModel<String> sessionModel = new DefaultListModel<>();
        JList<String> sessionList = new JList<>(sessionModel);
        sessionList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane sessionScroll = new JScrollPane(sessionList);
        sessionScroll.setPreferredSize(new Dimension(350, 150));
        sessionScroll.setBorder(BorderFactory.createTitledBorder("Sessions to Replace (Auto-loaded)"));
        
        dateBtn.addActionListener(e -> {
            DatePickerDialog picker = new DatePickerDialog(SwingUtilities.getWindowAncestor(this), null);
            picker.setVisible(true);
            if (picker.isConfirmed()) {
                java.time.LocalDate date = picker.getSelectedDate();
                selectedDateStr[0] = date.toString();
                dateLabel.setText(selectedDateStr[0]);
                
                // Auto-load sessions
                sessionModel.clear();
                String dayStr = date.getDayOfWeek().toString().substring(0, 3);
                for (ClassSession s : system.getSessionsByDay(dayStr, "All Courses")) {
                    sessionModel.addElement(s.getTimeSlot() + "#" + s.getSubject() + " (" + s.getTeacher() + ")");
                }
            }
        });

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel fieldsPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        
        fieldsPanel.add(new JLabel("Title:")); fieldsPanel.add(titleField);
        fieldsPanel.add(new JLabel("Description:")); fieldsPanel.add(descField);
        fieldsPanel.add(new JLabel("Date:")); 
        
        JPanel datePanel = new JPanel(new BorderLayout(5, 0));
        datePanel.add(dateLabel, BorderLayout.CENTER);
        datePanel.add(dateBtn, BorderLayout.EAST);
        fieldsPanel.add(datePanel);
        
        fieldsPanel.add(new JLabel("Time:")); fieldsPanel.add(timeField);
        fieldsPanel.add(new JLabel("Location:")); fieldsPanel.add(locField);
        
        panel.add(fieldsPanel, BorderLayout.NORTH);
        panel.add(sessionScroll, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, panel, "Create Event", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            if (titleField.getText().isEmpty()) {
                showError("Title is required.");
                return;
            }
            if (selectedDateStr[0].isEmpty()) {
                showError("Date is required.");
                return;
            }
            
            // Collect selected sessions
            List<String> selected = sessionList.getSelectedValuesList();
            StringBuilder sb = new StringBuilder();
            for (String s : selected) {
                // Parse out "TIME#SUBJECT" from "TIME#SUBJECT (TEACHER)"
                int idx = s.indexOf(" (");
                String key = (idx != -1) ? s.substring(0, idx) : s;
                sb.append(key).append(",");
            }
            String affected = sb.toString();
            if (affected.length() > 0) affected = affected.substring(0, affected.length() - 1);
            
            Event event = new Event(
                titleField.getText(),
                descField.getText(),
                selectedDateStr[0],
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
        Event targetEvent = null;
        for(Event e : system.getAllEvents()) {
            if(e.getId().equals(eventId)) {
                targetEvent = e;
                break;
            }
        }
        
        if (targetEvent == null) return;
        
        // Passcode Verification
        String inputCode = JOptionPane.showInputDialog(this, 
            "Enter Event Passcode (provided by teacher):", "Verify Presence", JOptionPane.QUESTION_MESSAGE);
            
        if (inputCode == null || !inputCode.equals(targetEvent.getPasscode())) {
            showError("Invalid Passcode! You cannot upload a photo.");
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            EventPhoto photo = new EventPhoto(
                eventId,
                currentUser.getStudentId(),
                currentUser.getUsername(),
                file.getAbsolutePath()
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
