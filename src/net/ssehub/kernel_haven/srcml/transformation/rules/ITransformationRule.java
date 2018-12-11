package net.ssehub.kernel_haven.srcml.transformation.rules;

import net.ssehub.kernel_haven.code_model.ast.ISyntaxElement;
import net.ssehub.kernel_haven.srcml.transformation.ITranslationUnit;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Transformation rule how to adapt the {@link ITranslationUnit}-structure to converge to the final
 * {@link ISyntaxElement}-structure to simplify parsing.
 * 
 * @author El-Sharkawy
 */
public interface ITransformationRule {
    
    /**
     * Will change the {@link ITranslationUnit}s as side effect.
     * 
     * @param base The root element, representing the whole parsed file.
     * 
     * @throws FormatException If this transformation rule detects an invalid format.
     */
    public void transform(@NonNull ITranslationUnit base) throws FormatException;

}
