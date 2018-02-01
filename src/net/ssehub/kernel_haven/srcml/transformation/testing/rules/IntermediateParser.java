package net.ssehub.kernel_haven.srcml.transformation.testing.rules;

import net.ssehub.kernel_haven.srcml.model.SrcMlSyntaxElement;
import net.ssehub.kernel_haven.srcml.transformation.testing.ITranslationUnit;

/**
 * Restructures the {@link ITranslationUnit}s to be more close to the target {@link SrcMlSyntaxElement}-structure and to
 * simplify further processing.
 * @author El-Sharkawy
 *
 */
public class IntermediateParser {
    
    /**
     * Restructures the {@link ITranslationUnit}s.
     * @param baseUnit The root element of the AST, which represents to complete parsed file.
     */
    public void convert(ITranslationUnit baseUnit) {
        ITransformationRule rule = new PreprocessorTranslation();
        rule.transform(baseUnit);
        rule = new PreprocessorBlockStructure();
        rule.transform(baseUnit);
        rule = new PreprocessorConditionComputationRule();
        rule.transform(baseUnit);
    }

}
