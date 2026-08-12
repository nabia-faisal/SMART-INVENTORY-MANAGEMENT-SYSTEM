package common;

import dao.InventoryUtils;
import java.awt.Desktop;
import java.io.File;
import javax.swing.JOptionPane;

/**
 * Utility to open a generated PDF bill by its order ID.
 * Uses java.awt.Desktop for cross-platform support (Windows, macOS, Linux).
 */
public class OpenPdf {

    public static void OpenById(String id) {
        try {
            File file = new File(InventoryUtils.billPath + id + ".pdf");
            if (file.exists()) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                } else {
                    JOptionPane.showMessageDialog(null,
                        "Cannot open PDF: Desktop API not supported on this system.\nFile is at: " + file.getAbsolutePath());
                }
            } else {
                JOptionPane.showMessageDialog(null,
                    "File does not exist: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error opening PDF: " + e.getMessage());
        }
    }
}
