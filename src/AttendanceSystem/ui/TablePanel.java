package AttendanceSystem.ui;

import AttendanceSystem.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

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
    
    private JPanel createLegendPanel() {
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, DesignSystem.SPACING_LG, DesignSystem.SPACING_SM));
        legendPanel.setOpaque(false);
        legendPanel.setBorder(BorderFactory.createEmptyBorder(DesignSystem.SPACING_MD, 0, 0, 0));
        
        legendPanel.add(createLegendItem("Excellent (>80%)", new Color(22, 101, 52), new Color(220, 252, 231)));
        legendPanel.add(createLegendItem("Good (75-80%)", new Color(133, 77, 14), new Color(254, 249, 195)));
        legendPanel.add(createLegendItem("Warning (70-75%)", new Color(194, 65, 12), new Color(255, 237, 213)));
        legendPanel.add(createLegendItem("Critical (<70%)", new Color(153, 27, 27), new Color(254, 226, 226)));
        
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
            if (percentage > 80) status = "Excellent";
            else if (percentage >= 75) status = "Good";
            else if (percentage >= 70) status = "Warning";
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
