package net.ssehub.kernel_haven.srcml.transformation.testing;

import net.ssehub.kernel_haven.srcml.transformation.testing.rules.ITransformationRule;
import net.ssehub.kernel_haven.srcml.transformation.testing.rules.PreprocessorBlockStructure;
import net.ssehub.kernel_haven.srcml.transformation.testing.rules.PreprocessorTranslation;

public class Converter {
    
    public void convert(ITranslationUnit baseUnit) {
        ITransformationRule rule = new PreprocessorTranslation();
        rule.transform(baseUnit);
        rule = new PreprocessorBlockStructure();
        rule.transform(baseUnit);
    }

}
