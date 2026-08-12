package ui;

import javax.swing.*;
import java.awt.*;

/**
 * A JPanel with rounded corners and optional shadow effect.
 * Used for creating modern card-like components.
 */
public class RoundedPanel extends JPanel {
    private int cornerRadius;
    private Color shadowColor;
    private boolean hasShadow;
    
    public RoundedPanel() {
        this(12, true);
    }
    
    public RoundedPanel(int cornerRadius, boolean hasShadow) {
        this.cornerRadius = cornerRadius;
        this.hasShadow = hasShadow;
        this.shadowColor = new Color(0, 0, 0, 30); // Semi-transparent black
        setOpaque(false);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        
        // Draw shadow if enabled
        if (hasShadow) {
            g2.setColor(shadowColor);
            g2.fillRoundRect(2, 2, width - 4, height - 4, cornerRadius, cornerRadius);
        }
        
        // Draw rounded panel background
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, width - (hasShadow ? 2 : 0), height - (hasShadow ? 2 : 0), 
                        cornerRadius, cornerRadius);
        
        g2.dispose();
        super.paintComponent(g);
    }
    
    @Override
    protected void paintBorder(Graphics g) {
        // No border painting - handled by paintComponent
    }
}

