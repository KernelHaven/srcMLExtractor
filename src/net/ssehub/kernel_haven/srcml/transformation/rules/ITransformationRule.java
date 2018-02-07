package net.ssehub.kernel_haven.srcml.transformation.rules;

import net.ssehub.kernel_haven.code_model.ast.ISyntaxElement;
import net.ssehub.kernel_haven.srcml.transformation.ITranslationUnit;
import net.ssehub.kernel_haven.util.FormatException;

/**
 * Transformation rule how to adapt the {@link ITranslationUnit}-structure to converge to the final
 * {@link ISyntaxElement}-structure to simplify parsing.
 * @author El-Sharkawy
 *
 */
public interface ITransformationRule {
    
    /**
     * Will change the {@link ITranslationUnit}s as side effect.
     * @param base The root element, representing the whole parsed file.
     */
    public void transform(ITranslationUnit base) throws FormatException;

}
