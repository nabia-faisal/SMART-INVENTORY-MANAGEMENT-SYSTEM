package screens;

import javax.swing.SwingUtilities;

/**
 * Application entry point.
 * Run this class to start SISMS.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(login::new);
    }
}
