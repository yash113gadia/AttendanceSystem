package AttendanceSystem;

import AttendanceSystem.ui.DesignSystem;
import javax.swing.*;
import java.awt.*;

/**
 * Modern login dialog with clean design.
 */
public class LoginDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private User loggedInUser = null;
    
    public LoginDialog(JFrame parent) {
        super(parent, "AttendEase - Login", true);
        setSize(420, 600);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        
        initLoginComponents();
    }
    
    private void initLoginComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(DesignSystem.BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        
        // Logo - custom drawn icon
        JPanel logoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                
                // Draw a graduation cap / book icon
                g2.setColor(DesignSystem.PRIMARY);
                g2.fillRoundRect(cx - 24, cy - 8, 48, 32, 8, 8);
                g2.setColor(DesignSystem.PRIMARY_DARK);
                g2.fillRoundRect(cx - 20, cy - 4, 40, 4, 2, 2);
                g2.fillRoundRect(cx - 20, cy + 4, 40, 4, 2, 2);
                g2.fillRoundRect(cx - 20, cy + 12, 40, 4, 2, 2);
                
                // Checkmark overlay
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(cx - 8, cy + 2, cx - 2, cy + 8);
                g2.drawLine(cx - 2, cy + 8, cx + 10, cy - 6);
                
                g2.dispose();
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(80, 60);
            }
        };
        logoPanel.setOpaque(false);
        logoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(logoPanel);
        
        mainPanel.add(Box.createVerticalStrut(16));
        
        JLabel titleLabel = new JLabel("AttendEase");
        titleLabel.setFont(new Font(DesignSystem.FONT_FAMILY, Font.BOLD, 28));
        titleLabel.setForeground(DesignSystem.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        
        JLabel subtitleLabel = new JLabel("Attendance Management System");
        subtitleLabel.setFont(DesignSystem.FONT_BODY);
        subtitleLabel.setForeground(DesignSystem.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(subtitleLabel);
        
        mainPanel.add(Box.createVerticalStrut(40));
        
        // Login card
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
        card.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.setMaximumSize(new Dimension(340, 240));
        
        // Username field
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(DesignSystem.FONT_BODY_BOLD);
        userLabel.setForeground(DesignSystem.TEXT_PRIMARY);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(userLabel);
        
        card.add(Box.createVerticalStrut(8));
        
        usernameField = createStyledTextField();
        card.add(usernameField);
        
        card.add(Box.createVerticalStrut(16));
        
        // Password field
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(DesignSystem.FONT_BODY_BOLD);
        passLabel.setForeground(DesignSystem.TEXT_PRIMARY);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(passLabel);
        
        card.add(Box.createVerticalStrut(8));
        
        passwordField = createStyledPasswordField();
        card.add(passwordField);
        
        card.add(Box.createVerticalStrut(24));
        
        // Login button
        JButton loginButton = new JButton("Sign In") {
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
        loginButton.setFont(DesignSystem.FONT_BUTTON);
        loginButton.setForeground(Color.WHITE);
        loginButton.setContentAreaFilled(false);
        loginButton.setBorderPainted(false);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        loginButton.setPreferredSize(new Dimension(300, 44));
        loginButton.addActionListener(e -> performLogin());
        card.add(loginButton);
        
        mainPanel.add(card);
        
        mainPanel.add(Box.createVerticalStrut(24));
        
        // Footer hint
        JLabel hintLabel = new JLabel("Contact admin for credentials");
        hintLabel.setFont(DesignSystem.FONT_SMALL);
        hintLabel.setForeground(DesignSystem.TEXT_MUTED);
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(hintLabel);
        
        // Key listeners
        passwordField.addActionListener(e -> performLogin());
        usernameField.addActionListener(e -> passwordField.requestFocus());
        
        add(mainPanel);
    }
    
    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(DesignSystem.FONT_BODY);
        field.setForeground(DesignSystem.TEXT_PRIMARY);
        field.setBackground(DesignSystem.SURFACE);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DesignSystem.BORDER, 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        return field;
    }
    
    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(DesignSystem.FONT_BODY);
        field.setForeground(DesignSystem.TEXT_PRIMARY);
        field.setBackground(DesignSystem.SURFACE);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DesignSystem.BORDER, 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        return field;
    }
    
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        loggedInUser = AuthenticationManager.authenticate(username, password);
        
        if (loggedInUser != null) {
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Invalid username or password!", 
                "Login Failed", 
                JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
            usernameField.requestFocus();
        }
    }
    
    public User getLoggedInUser() {
        return loggedInUser;
    }
}
