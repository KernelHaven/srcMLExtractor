package net.ssehub.kernel_haven.srcml.transformation.rules;

import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import net.ssehub.kernel_haven.srcml.transformation.ITranslationUnit;
import net.ssehub.kernel_haven.srcml.transformation.PreprocessorIf;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Workaround to avoid parsing exceptions: Will remove all elements surrounded by if 0.
 * @author El-Sharkawy
 *
 */
public class IfZeroWorkaround implements ITransformationRule {

    @Override
    public void transform(@NonNull ITranslationUnit unit) throws FormatException {
        // getCondition() can't be null, since this is an if
        if (unit instanceof PreprocessorIf && notNull(((PreprocessorIf)unit).getCondition()).equals("0")) {
            for (int i = unit.size() - 1; i >= 0; i--) {
                unit.removeNested(i);
            }
        }
        
        // Recursive part
        for (int i = 0; i < unit.size(); i++) {
            transform(unit.getNestedElement(i));
        }
    }

}
