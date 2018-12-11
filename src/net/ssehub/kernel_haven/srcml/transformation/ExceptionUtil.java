package net.ssehub.kernel_haven.srcml.transformation;

import net.ssehub.kernel_haven.util.FormatException;

/**
 * Utility class for creating exceptions.
 * 
 * @author Adam
 */
public class ExceptionUtil {

    /**
     * No intstances.
     */
    private ExceptionUtil() {
    }

    /**
     * Creates an exception with the given message.
     * 
     * @param message The message describing the exception.
     * @param unit The unit where the exception occurred.
     * 
     * @return An exception with the given message.
     */
    public static FormatException makeException(String message, ITranslationUnit unit) {
        return new FormatException("Line " + unit.getStartLine() + ": " + message);
    }
    
    /**
     * Creates an exception with the given message and cause.
     * 
     * @param message The message describing the exception.
     * @param cause The exception that caused this exception.
     * @param unit The unit where the exception occurred.
     * 
     * @return An exception with the given message.
     */
    public static FormatException makeException(String message, Throwable cause, ITranslationUnit unit) {
        return (FormatException) new FormatException("Line " + unit.getStartLine() + ": " + message).initCause(cause);
    }
    
}
