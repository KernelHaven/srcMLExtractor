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
     * Includes all headers like {@link #INCLUDE}. Tries to find declarations of functions in the headers and uses
     * their presence condition to expand the presence condition of the function implementation.
     */
    EXPAND_FUNCTION_CONDITION,
    
    /**
     * Parse the headers and include their content instead of the #include directive.
     */
    INCLUDE,
    
}
