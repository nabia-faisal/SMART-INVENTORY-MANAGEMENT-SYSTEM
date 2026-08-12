package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Modern styled button with hover effects and rounded corners.
 */
public class ModernButton extends JButton {
    private static final Color PRIMARY_COLOR = new Color(74, 163, 200);
    private static final Color PRIMARY_HOVER = new Color(47, 156, 219);
    private static final Color SECONDARY_COLOR = new Color(232, 237, 242);
    private static final Color SECONDARY_HOVER = new Color(220, 225, 230);
    private Color currentBg;
    private boolean isPrimary;
    
    public ModernButton(String text) {
        this(text, true);
    }
    
    public ModernButton(String text, boolean isPrimary) {
        super(text);
        this.isPrimary = isPrimary;
        this.currentBg = isPrimary ? PRIMARY_COLOR : SECONDARY_COLOR;
        
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setFont(getFont().deriveFont(Font.BOLD, 13f));
        setForeground(isPrimary ? Color.WHITE : new Color(43, 43, 43));
        setPreferredSize(new Dimension(100, 36));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                currentBg = isPrimary ? PRIMARY_HOVER : SECONDARY_HOVER;
                repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                currentBg = isPrimary ? PRIMARY_COLOR : SECONDARY_COLOR;
                repaint();
            }
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2.setColor(currentBg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
        
        g2.dispose();
        super.paintComponent(g);
    }
}

