package AttendanceSystem.ui;

import AttendanceSystem.*;
import javax.swing.*;
import java.awt.*;

/**
 * Panel for adding new students.
 * Admin-only feature with modern card-based design.
 */
public class AddStudentPanel extends BasePanel {
    private JTextField nameField;
    private JTextField idField;
    private JComboBox<String> courseComboBox;
    
    private static final String[] COURSES = {
        "M.Tech (Computer Science & Engineering)",
        "M.Tech (Mechanical Engineering, Working Professionals)",
        "M.Tech (Artificial Intelligence)",
        "M.Tech (VLSI Design)",
        "M.Tech (Biotechnology)",
        "Master of Computer Applications (MCA)",
        "MBA (Master of Business Administration)",
        "M.Tech (Computer Science & Engineering, Working Professionals)",
        "MBA (Innovation, Entrepreneurship & Venture Development)",
        "M.Tech (Electronics & Communication Engineering, Working Professionals)",
        "MBA (Marketing and Finance)",
        "M.Tech (Mechanical Engineering)",
        "Master of Integrated Technology in Computer Science and Engineering",
        "BBA + MBA (Integrated)",
        "B.Tech (Computer Science & Engineering)",
        "B.Tech (Computer Science & Engineering, Regional)",
        "B.Tech (Computer Science)",
        "B.Tech (Information Technology)",
        "B.Tech (Electronics & Communication Engineering)",
        "B.Tech (Electronics Engineering – VLSI Design and Technology)",
        "B.Tech (Mechanical Engineering)",
        "B.Tech (Biotechnology)",
        "B.Tech CSE (Data Science)",
        "B.Tech CSE (Artificial Intelligence and Machine Learning)",
        "B.Tech CSE (Cyber-Security)",
        "B.Tech (Computer Science & Business Systems)",
        "B.Tech CSE (Internet of Things)",
        "B.Tech CSE (Artificial Intelligence)",
        "B.Tech (Mathematics and Computing)",
        "Bachelor of Computer Applications (BCA)",
        "BBA (Bachelor of Business Administration)",
        "B.Tech CSE (Artificial Intelligence/International Twinning)",
        "B.Tech CSE (AI & Machine Learning/International Twinning)",
        "B.Tech IT (International Twinning Program)",
        "B.Tech CSE (International Twinning Program)",
        "Minor Degree in AIML",
        "Advanced Diploma in Automotive Mechatronics (ADAM)",
        "International Twinning Program"
    };
    
    public AddStudentPanel(AttendanceSystem system, User currentUser) {
        super(system, currentUser);
        setBackground(DesignSystem.BACKGROUND);
        setLayout(new BorderLayout());
        initComponents();
    }
    
    private void initComponents() {
        // Create a centered card for the form
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        
        JPanel card = new JPanel() {
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
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(
            DesignSystem.SPACING_XL, DesignSystem.SPACING_XL, 
            DesignSystem.SPACING_XL, DesignSystem.SPACING_XL));
        card.setPreferredSize(new Dimension(500, 450));
        card.setMaximumSize(new Dimension(500, 450));
        
        // Icon - drawn plus in circle
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                int r = 24;
                
                // Circle
                g2.setColor(DesignSystem.PRIMARY_LIGHT);
                g2.fillOval(cx - r, cy - r, r * 2, r * 2);
                
                // Plus sign
                g2.setColor(DesignSystem.PRIMARY);
                g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(cx, cy - 12, cx, cy + 12);
                g2.drawLine(cx - 12, cy, cx + 12, cy);
                
                g2.dispose();
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(60, 60);
            }
        };
        iconPanel.setOpaque(false);
        iconPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(iconPanel);
        
        card.add(Box.createVerticalStrut(DesignSystem.SPACING_MD));
        
        JLabel titleLabel = new JLabel("Add New Student");
        titleLabel.setFont(DesignSystem.FONT_TITLE);
        titleLabel.setForeground(DesignSystem.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLabel);
        
        JLabel subtitleLabel = new JLabel("Enter student details below");
        subtitleLabel.setFont(DesignSystem.FONT_BODY);
        subtitleLabel.setForeground(DesignSystem.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subtitleLabel);
        
        card.add(Box.createVerticalStrut(DesignSystem.SPACING_XL));
        
        // Form fields
        card.add(createFormField("Student Name", nameField = createStyledTextField()));
        card.add(Box.createVerticalStrut(DesignSystem.SPACING_MD));
        
        card.add(createFormField("Student ID", idField = createStyledTextField()));
        card.add(Box.createVerticalStrut(DesignSystem.SPACING_MD));
        
        courseComboBox = new JComboBox<>(COURSES);
        courseComboBox.setFont(DesignSystem.FONT_BODY);
        courseComboBox.setBackground(DesignSystem.SURFACE);
        courseComboBox.setMaximumRowCount(10);
        card.add(createFormField("Course", courseComboBox));
        
        card.add(Box.createVerticalStrut(DesignSystem.SPACING_XL));
        
        // Add button
        JButton addButton = new JButton("Add Student") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2.setColor(DesignSystem.PRIMARY_DARK);
                } else if (getModel().isRollover()) {
                    g2.setColor(DesignSystem.PRIMARY.brighter());
                } else {
                    g2.setColor(DesignSystem.PRIMARY);
                }
                
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), DesignSystem.RADIUS_MD, DesignSystem.RADIUS_MD);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        addButton.setFont(DesignSystem.FONT_BUTTON);
        addButton.setForeground(Color.WHITE);
        addButton.setContentAreaFilled(false);
        addButton.setBorderPainted(false);
        addButton.setFocusPainted(false);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addButton.setMaximumSize(new Dimension(200, 45));
        addButton.setPreferredSize(new Dimension(200, 45));
        addButton.addActionListener(e -> addStudent());
        card.add(addButton);
        
        centerWrapper.add(card);
        add(centerWrapper, BorderLayout.CENTER);
    }
    
    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(DesignSystem.FONT_BODY);
        field.setForeground(DesignSystem.TEXT_PRIMARY);
        field.setBackground(DesignSystem.SURFACE);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DesignSystem.BORDER, 1),
            BorderFactory.createEmptyBorder(
                DesignSystem.SPACING_SM, DesignSystem.SPACING_MD, 
                DesignSystem.SPACING_SM, DesignSystem.SPACING_MD)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setPreferredSize(new Dimension(400, 44));
        return field;
    }
    
    private JPanel createFormField(String label, JComponent field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.setMaximumSize(new Dimension(400, 80));
        
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(DesignSystem.FONT_BODY_BOLD);
        labelComp.setForeground(DesignSystem.TEXT_PRIMARY);
        labelComp.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (field instanceof JComboBox) {
            field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            ((JComboBox<?>)field).setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DesignSystem.BORDER, 1),
                BorderFactory.createEmptyBorder(DesignSystem.SPACING_XS, DesignSystem.SPACING_SM, 
                    DesignSystem.SPACING_XS, DesignSystem.SPACING_SM)
            ));
        }
        
        panel.add(labelComp);
        panel.add(Box.createVerticalStrut(DesignSystem.SPACING_XS));
        panel.add(field);
        
        return panel;
    }
    
    private void addStudent() {
        if (!isAdmin()) {
            showError("Only admins can add students!");
            return;
        }
        
        String name = nameField.getText().trim();
        String id = idField.getText().trim();
        String course = (String) courseComboBox.getSelectedItem();
        
        if (name.isEmpty() || id.isEmpty()) {
            showError("Please fill all fields!");
            return;
        }
        
        if (system.findStudent(id) != null) {
            showError("Student ID already exists!");
            return;
        }
        
        system.addStudent(name, id, course);
        showSuccess("Student added successfully!\n" +
            "Name: " + name + "\nID: " + id + "\nCourse: " + course);
        
        nameField.setText("");
        idField.setText("");
        courseComboBox.setSelectedIndex(0);
        
        notifyDataChanged();
    }
}
