package dao;

import java.io.File;

/**
 * Utility constants for the application.
 * billPath is set to a "sisms_bills" folder inside the user's home directory,
 * making it cross-platform (Windows, macOS, Linux).
 */
public class InventoryUtils {

    public static final String billPath;

    static {
        String home = System.getProperty("user.home");
        String path = home + File.separator + "sisms_bills" + File.separator;
        // Create the folder if it doesn't exist
        new File(path).mkdirs();
        billPath = path;
    }
}
