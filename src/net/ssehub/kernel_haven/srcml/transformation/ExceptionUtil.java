package net.ssehub.kernel_haven.srcml.transformation;

import net.ssehub.kernel_haven.util.FormatException;

public class ExceptionUtil {

    /**
     * No intstances.
     */
    private ExceptionUtil() {
    }
    
    public static FormatException makeException(String message, ITranslationUnit unit) {
        return new FormatException("Line " + unit.getStartLine() + ": " + message);
    }
    
    public static FormatException makeException(String message, Throwable casue, ITranslationUnit unit) {
        return (FormatException) new FormatException("Line " + unit.getStartLine() + ": " + message).initCause(casue);
    }
    
}
