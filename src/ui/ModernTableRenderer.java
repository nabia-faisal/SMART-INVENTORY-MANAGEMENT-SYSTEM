package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Custom table cell renderer for modern table styling.
 * Provides alternating row colors and proper text alignment.
 */
public class ModernTableRenderer extends DefaultTableCellRenderer {
    private static final Color ROW_COLOR_EVEN = new Color(255, 255, 255);
    private static final Color ROW_COLOR_ODD = new Color(230, 240, 255); // Very light blue
    private static final Color SELECTION_COLOR = new Color(0, 30, 60, 100); // Very dark blue with transparency
    private static final Color HEADER_BG = new Color(0, 30, 60); // Very dark blue
    private static final Color HEADER_FG = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(43, 43, 43);
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        // Header styling
        if (table.getTableHeader() != null && row == -1) {
            setBackground(HEADER_BG);
            setForeground(HEADER_FG);
            setFont(getFont().deriveFont(Font.BOLD, 12f));
            setHorizontalAlignment(SwingConstants.LEFT);
            return this;
        }
        
        // Row styling
        if (isSelected) {
            setBackground(SELECTION_COLOR);
            setForeground(TEXT_COLOR);
        } else {
            setBackground(row % 2 == 0 ? ROW_COLOR_EVEN : ROW_COLOR_ODD);
            setForeground(TEXT_COLOR);
        }
        
        setFont(getFont().deriveFont(12f));
        setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        setHorizontalAlignment(SwingConstants.CENTER);
        
        return this;
    }
}

