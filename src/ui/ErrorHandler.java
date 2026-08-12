package ui;

import javax.swing.JOptionPane;

/**
 * Utility class for converting Java exceptions into human-readable error messages.
 * All UI classes should use this instead of showing raw exceptions.
 */
public class ErrorHandler {
    
    /**
     * Converts an exception to a human-readable message and displays it to the user.
     * 
     * @param e The exception to handle
     * @param context A brief description of what operation was being performed (e.g., "saving product")
     */
    public static void showError(Exception e, String context) {
        String message = getHumanReadableMessage(e, context);
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Converts an exception to a human-readable message without displaying it.
     * 
     * @param e The exception
     * @param context The operation context
     * @return Human-readable error message
     */
    public static String getHumanReadableMessage(Exception e, String context) {
        String errorMessage = e.getMessage();
        String className = e.getClass().getSimpleName();
        
        // Database connection errors
        if (className.contains("SQLException") || className.contains("CommunicationsException")) {
            if (errorMessage != null && errorMessage.contains("Communications link failure")) {
                return "Unable to connect to the database. Please check:\n" +
                       "• Database server is running\n" +
                       "• Network connection is active\n" +
                       "• Database credentials are correct";
            }
            if (errorMessage != null && errorMessage.contains("Access denied")) {
                return "Database access denied. Please check your username and password.";
            }
            if (errorMessage != null && errorMessage.contains("Unknown database")) {
                return "Database not found. Please ensure the database exists and the name is correct.";
            }
            return "Database error occurred while " + context + ".\n" +
                   "Please check your database connection and try again.";
        }
        
        // SQL syntax or constraint errors
        if (className.contains("SQLException")) {
            if (errorMessage != null && errorMessage.contains("Duplicate entry")) {
                return "This record already exists. Please use a unique value.";
            }
            if (errorMessage != null && errorMessage.contains("foreign key constraint")) {
                return "Cannot perform this operation. Related records exist that must be removed first.";
            }
            if (errorMessage != null && errorMessage.contains("cannot be null")) {
                return "Required fields cannot be empty. Please fill in all required information.";
            }
            return "Database operation failed while " + context + ".\n" +
                   "Please verify your data and try again.";
        }
        
        // Number format errors
        if (className.contains("NumberFormatException")) {
            return "Invalid number format. Please enter a valid numeric value.";
        }
        
        // Null pointer errors
        if (className.contains("NullPointerException")) {
            return "An unexpected error occurred. Please try again or contact support.";
        }
        
        // Array index errors
        if (className.contains("ArrayIndexOutOfBoundsException") || 
            className.contains("IndexOutOfBoundsException")) {
            return "Please select an item from the list before performing this action.";
        }
        
        // File not found errors
        if (className.contains("FileNotFoundException")) {
            return "Required file not found. Please ensure all application files are present.";
        }
        
        // Generic fallback
        if (errorMessage != null && !errorMessage.isEmpty()) {
            return "Error while " + context + ":\n" + errorMessage;
        }
        
        return "An unexpected error occurred while " + context + ".\n" +
               "Please try again or contact support if the problem persists.";
    }
}

