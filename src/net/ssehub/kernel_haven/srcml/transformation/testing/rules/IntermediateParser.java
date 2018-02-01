package net.ssehub.kernel_haven.srcml.transformation.testing.rules;

import net.ssehub.kernel_haven.srcml.model.SrcMlSyntaxElement;
import net.ssehub.kernel_haven.srcml.transformation.testing.ITranslationUnit;
import net.ssehub.kernel_haven.srcml.transformation.testing.PreprocessorBlock;
import net.ssehub.kernel_haven.srcml.transformation.testing.PreprocessorEndIf;

/**
 * Restructures the {@link ITranslationUnit}s to be more close to the target {@link SrcMlSyntaxElement}-structure and to
 * simplify further processing.
 * More precisely it will do the following steps (in the given order):
 * <ol>
 *     <li>Translations of {@link ITranslationUnit}s into {@link PreprocessorBlock}s</li>
 *     <li>Moves/nests elements, which are encapsulated between two {@link PreprocessorBlock}s into the 
 *         responsible {@link PreprocessorBlock}. Will also delete {@link PreprocessorEndIf}'s as they are no longer
 *         needed after this processing.</li>
 *     <li>Computation and parsing of the {@link PreprocessorBlock#getEffectiveCondition()}s</li>
 * </ol>
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
