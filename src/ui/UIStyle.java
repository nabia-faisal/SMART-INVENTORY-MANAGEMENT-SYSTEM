package ui;

import java.awt.*;

/**
 * Centralized style constants for the modernized UI.
 * All colors, fonts, spacing, and styling values are defined here.
 */
public class UIStyle {
    // Colors
    public static final Color PRIMARY = new Color(0, 30, 60);         // Very dark blue accent
    public static final Color PRIMARY_DARK = new Color(0, 20, 40);    // Even darker blue
    public static final Color BACKGROUND = Color.WHITE;                // #FFFFFF - Base background (White)
    public static final Color CARD_BG = new Color(230, 240, 255);     // Very light blue - Card background
    public static final Color SEPARATOR = new Color(200, 220, 240);   // Light blue separators
    public static final Color TEXT_DARK = new Color(43, 43, 43);      // #2B2B2B - Dark text
    public static final Color TEXT_LIGHT = new Color(128, 128, 128);  // #808080 - Light text
    public static final Color SUCCESS = new Color(76, 175, 80);       // Green for success
    public static final Color ERROR = new Color(139, 0, 0);           // Very dark red for errors/delete
    public static final Color WARNING = new Color(255, 152, 0);       // Orange for warnings
    
    // Sidebar colors
    public static final Color SIDEBAR_BG = PRIMARY;                   // Very dark blue accent for sidebar
    public static final Color SIDEBAR_HOVER = PRIMARY_DARK;
    public static final Color SIDEBAR_ACTIVE = PRIMARY_DARK;
    
    // User info card color
    public static final Color USER_CARD_BG = new Color(230, 240, 255); // Very light blue
    
    // Fonts
    public static Font getFont(int style, int size) {
        // Try to use system fonts that approximate Roboto/Inter
        String fontName = "Segoe UI"; // Windows default, close to Inter
        try {
            return new Font(fontName, style, size);
        } catch (Exception e) {
            return new Font(Font.SANS_SERIF, style, size);
        }
    }
    
    public static final Font FONT_HEADING = getFont(Font.BOLD, 24);
    public static final Font FONT_SUBHEADING = getFont(Font.BOLD, 18);
    public static final Font FONT_BODY = getFont(Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = getFont(Font.BOLD, 13);
    public static final Font FONT_SMALL = getFont(Font.PLAIN, 11);
    
    // Spacing
    public static final int PADDING_SMALL = 8;
    public static final int PADDING_MEDIUM = 12;
    public static final int PADDING_LARGE = 16;
    public static final int PADDING_XLARGE = 20;
    public static final int GAP_SMALL = 8;
    public static final int GAP_MEDIUM = 12;
    public static final int GAP_LARGE = 20;
    
    // Border radius
    public static final int RADIUS_SMALL = 8;
    public static final int RADIUS_MEDIUM = 12;
    public static final int RADIUS_LARGE = 18;
    
    // Component sizes
    public static final int BUTTON_HEIGHT = 36;
    public static final int INPUT_HEIGHT = 36;
    public static final int TABLE_ROW_HEIGHT = 40;
    public static final int SIDEBAR_WIDTH = 260;
    public static final int SIDEBAR_COLLAPSED_WIDTH = 70;
    
    // Shadows
    public static final Color SHADOW_COLOR = new Color(0, 0, 0, 30);
}

