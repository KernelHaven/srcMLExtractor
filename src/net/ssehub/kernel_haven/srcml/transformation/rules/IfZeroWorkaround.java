package net.ssehub.kernel_haven.srcml.transformation.rules;

import net.ssehub.kernel_haven.srcml.transformation.ITranslationUnit;
import net.ssehub.kernel_haven.srcml.transformation.PreprocessorIf;

/**
 * Workaround to avoid parsing exceptions: Will remove all elements surrounded by if 0.
 * @author El-Sharkawy
 *
 */
public class IfZeroWorkaround implements ITransformationRule {

    @Override
    public void transform(ITranslationUnit unit) {
        if (unit instanceof PreprocessorIf && ((PreprocessorIf)unit).getCondition().equals("0")) {
            for (int i = unit.size() - 1; i >= 0; i--) {
                ITranslationUnit nestedElement = unit.getNestedElement(i);
                unit.removeNested(nestedElement);
            }
        }
        
        
        // Recursive part
        for (int i = 0; i < unit.size(); i++) {
            transform(unit.getNestedElement(i));
        }
        
    }

}
