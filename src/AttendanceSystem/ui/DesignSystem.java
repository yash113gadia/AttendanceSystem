package AttendanceSystem.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Design System - Unified design language for the entire application.
 * Inspired by modern dashboard designs with clean, light aesthetics.
 * 
 * To modify the app's appearance, update the constants here.
 */
public class DesignSystem {
    
    // ==================== COLORS ====================
    
    // Primary Colors
    public static final Color PRIMARY = new Color(55, 114, 255);      // Bright blue
    public static final Color PRIMARY_LIGHT = new Color(232, 240, 254);
    public static final Color PRIMARY_DARK = new Color(40, 84, 200);
    
    // Accent Colors
    public static final Color SUCCESS = new Color(34, 197, 94);       // Green
    public static final Color WARNING = new Color(245, 158, 11);      // Orange
    public static final Color DANGER = new Color(239, 68, 68);        // Red
    public static final Color INFO = new Color(99, 102, 241);         // Indigo
    
    // Neutral Colors
    public static final Color BACKGROUND = new Color(241, 245, 249);  // Light gray-blue bg
    public static final Color SURFACE = Color.WHITE;                   // Card/panel background
    public static final Color SIDEBAR_BG = Color.WHITE;                // Sidebar background
    public static final Color BORDER = new Color(226, 232, 240);       // Subtle borders
    
    // Text Colors
    public static final Color TEXT_PRIMARY = new Color(30, 41, 59);    // Dark slate
    public static final Color TEXT_SECONDARY = new Color(100, 116, 139); // Medium slate
    public static final Color TEXT_MUTED = new Color(148, 163, 184);   // Light slate
    public static final Color TEXT_ON_PRIMARY = Color.WHITE;
    
    // ==================== TYPOGRAPHY ====================
    
    public static final String FONT_FAMILY = "Segoe UI";
    
    public static final Font FONT_TITLE = new Font(FONT_FAMILY, Font.BOLD, 24);
    public static final Font FONT_HEADING = new Font(FONT_FAMILY, Font.BOLD, 18);
    public static final Font FONT_SUBHEADING = new Font(FONT_FAMILY, Font.BOLD, 14);
    public static final Font FONT_BODY = new Font(FONT_FAMILY, Font.PLAIN, 14);
    public static final Font FONT_BODY_BOLD = new Font(FONT_FAMILY, Font.BOLD, 14);
    public static final Font FONT_SMALL = new Font(FONT_FAMILY, Font.PLAIN, 12);
    public static final Font FONT_BUTTON = new Font(FONT_FAMILY, Font.BOLD, 13);
    public static final Font FONT_NAV = new Font(FONT_FAMILY, Font.PLAIN, 14);
    public static final Font FONT_NAV_ACTIVE = new Font(FONT_FAMILY, Font.BOLD, 14);
    
    // ==================== SPACING ====================
    
    public static final int SPACING_XS = 4;
    public static final int SPACING_SM = 8;
    public static final int SPACING_MD = 16;
    public static final int SPACING_LG = 24;
    public static final int SPACING_XL = 32;
    
    // ==================== BORDER RADIUS ====================
    
    public static final int RADIUS_SM = 6;
    public static final int RADIUS_MD = 10;
    public static final int RADIUS_LG = 16;
    
    // ==================== SHADOWS (simulated with borders) ====================
    
    public static Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(SPACING_MD, SPACING_MD, SPACING_MD, SPACING_MD)
        );
    }
    
    public static Border createSectionBorder() {
        return BorderFactory.createEmptyBorder(SPACING_MD, SPACING_MD, SPACING_MD, SPACING_MD);
    }
    
    // ==================== COMPONENT FACTORIES ====================
    
    /**
     * Create a styled navigation button for the sidebar
     */
    public static JButton createNavButton(String text, String icon, boolean isActive, Runnable action) {
        JButton button = new JButton(icon + "  " + text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed() || isActive) {
                    g2.setColor(PRIMARY_LIGHT);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(248, 250, 252));
                } else {
                    g2.setColor(getBackground());
                }
                
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS_SM, RADIUS_SM);
                g2.dispose();
                
                super.paintComponent(g);
            }
        };
        
        button.setFont(isActive ? FONT_NAV_ACTIVE : FONT_NAV);
        button.setForeground(isActive ? PRIMARY : TEXT_PRIMARY);
        button.setBackground(isActive ? PRIMARY_LIGHT : SURFACE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(200, 44));
        button.setPreferredSize(new Dimension(200, 44));
        button.setBorder(BorderFactory.createEmptyBorder(0, SPACING_MD, 0, SPACING_MD));
        
        if (action != null) {
            button.addActionListener(e -> action.run());
        }
        
        return button;
    }
    
    /**
     * Create a styled action button
     */
    public static JButton createButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bgColor.brighter());
                } else {
                    g2.setColor(bgColor);
                }
                
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS_MD, RADIUS_MD);
                g2.dispose();
                
                super.paintComponent(g);
            }
        };
        
        button.setFont(FONT_BUTTON);
        button.setForeground(TEXT_ON_PRIMARY);
        button.setBackground(bgColor);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(140, 40));
        
        return button;
    }
    
    /**
     * Create a styled text field
     */
    public static JTextField createTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(TEXT_MUTED);
                    g2.setFont(FONT_BODY);
                    g2.drawString(placeholder, getInsets().left, 
                        getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 2);
                    g2.dispose();
                }
            }
        };
        
        field.setFont(FONT_BODY);
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(SURFACE);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(SPACING_SM, SPACING_MD, SPACING_SM, SPACING_MD)
        ));
        field.setPreferredSize(new Dimension(250, 40));
        
        return field;
    }
    
    /**
     * Create a styled combo box
     */
    public static <T> JComboBox<T> createComboBox(T[] items) {
        JComboBox<T> combo = new JComboBox<>(items);
        combo.setFont(FONT_BODY);
        combo.setBackground(SURFACE);
        combo.setForeground(TEXT_PRIMARY);
        combo.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        combo.setPreferredSize(new Dimension(250, 40));
        return combo;
    }
    
    /**
     * Create a section label (category header)
     */
    public static JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text.toUpperCase());
        label.setFont(FONT_SMALL);
        label.setForeground(TEXT_MUTED);
        label.setBorder(BorderFactory.createEmptyBorder(SPACING_MD, SPACING_MD, SPACING_SM, SPACING_MD));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
    
    /**
     * Create a heading label
     */
    public static JLabel createHeading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_HEADING);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }
    
    /**
     * Create a card panel with white background and subtle border
     */
    public static JPanel createCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS_LG, RADIUS_LG);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIUS_LG, RADIUS_LG);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(SPACING_LG, SPACING_LG, SPACING_LG, SPACING_LG));
        return card;
    }
    
    /**
     * Create a stat card (for dashboard-style displays)
     */
    public static JPanel createStatCard(String title, String value, String icon, Color accentColor) {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(SPACING_MD, SPACING_SM));
        card.setPreferredSize(new Dimension(200, 100));
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font(FONT_FAMILY, Font.PLAIN, 32));
        iconLabel.setForeground(accentColor);
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_SMALL);
        titleLabel.setForeground(TEXT_SECONDARY);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(FONT_TITLE);
        valueLabel.setForeground(TEXT_PRIMARY);
        
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(SPACING_XS));
        textPanel.add(valueLabel);
        
        card.add(iconLabel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        
        return card;
    }
    
    /**
     * Style a JTable with the design system
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setForeground(TEXT_PRIMARY);
        table.setBackground(SURFACE);
        table.setGridColor(BORDER);
        table.setRowHeight(44);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(PRIMARY_DARK);
        
        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BODY_BOLD);
        header.setForeground(TEXT_SECONDARY);
        header.setBackground(new Color(248, 250, 252));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 48));
        
        // Center align cells
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }
    
    /**
     * Style a JScrollPane
     */
    public static void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        scrollPane.getViewport().setBackground(SURFACE);
    }
}
