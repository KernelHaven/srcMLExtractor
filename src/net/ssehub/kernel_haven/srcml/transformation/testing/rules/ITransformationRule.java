package net.ssehub.kernel_haven.srcml.transformation.testing.rules;

import net.ssehub.kernel_haven.srcml.model.SrcMlSyntaxElement;
import net.ssehub.kernel_haven.srcml.transformation.testing.ITranslationUnit;

/**
 * Transformation rule how to adapt the {@link ITranslationUnit}-structure to converge to the final
 * {@link SrcMlSyntaxElement}-structure to simplify parsing.
 * @author El-Sharkawy
 *
 */
public interface ITransformationRule {
    
    /**
     * Will change the {@link ITranslationUnit}s as side effect.
     * @param base The root element, representing the whole parsed file.
     */
    public void transform(ITranslationUnit base);

}
