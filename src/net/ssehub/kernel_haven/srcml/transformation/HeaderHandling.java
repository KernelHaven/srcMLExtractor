package net.ssehub.kernel_haven.srcml.transformation;

/**
 * How #include statements should be handled.
 * 
 * @author Adam
 */
public enum HeaderHandling {

    /**
     * Ignore the #include; just leave the preprocessor statement there.
     */
    IGNORE,
    
    /**
     * Searches for (quote) header files relative to the source file being parsed. Tries to find declarations of
     * functions in the header(s) and uses their condition to expand the condition of the function implementation.
     */
    EXPAND_FUNCTION_CONDITION,
    
    /**
     * Parse the headers and include their content instead of the #include directive.
     */
    INCLUDE,
    
}
