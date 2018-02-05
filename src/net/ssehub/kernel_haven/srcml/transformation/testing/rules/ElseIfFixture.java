package net.ssehub.kernel_haven.srcml.transformation.testing.rules;

import net.ssehub.kernel_haven.srcml.transformation.testing.ITranslationUnit;
import net.ssehub.kernel_haven.srcml.transformation.testing.TranslationUnit;
import net.ssehub.kernel_haven.util.Logger;

/**
 * Else-If Statement hold a nested if statement. This rule will pull up all the nested statements and eleminate the
 * nested if statement.
 * @author El-Sharkawy
 *
 */
public class ElseIfFixture implements ITransformationRule {

    @Override
    public void transform(ITranslationUnit unit) {
        // Check if this element is an elseif block, if yes restructure nested elements
        if ("elseif".equals(unit.getType())) {
            fixElseIfBlock(unit);
        }
        
        // Recursive part
        for (int i = 0; i < unit.size(); i++) {
            transform(unit.getNestedElement(i));
        }
    }

    private void fixElseIfBlock(ITranslationUnit elseIfBlock) {
        if (elseIfBlock.size() == 2 && elseIfBlock.getNestedElement(1) instanceof TranslationUnit
            && "if".equals(elseIfBlock.getNestedElement(1).getType()) && elseIfBlock instanceof TranslationUnit) {
            
            TranslationUnit elseifUnit = (TranslationUnit) elseIfBlock;
            ITranslationUnit nestedIf = elseIfBlock.getNestedElement(1);
            elseIfBlock.removeNested(nestedIf);
            for (int i = 0; i < nestedIf.size(); i++) {
                elseifUnit.add(nestedIf.getNestedElement(i));
            }
        } else {
            Logger.get().logError("Unexpected element nested inside elseif-statement at index 1: "
                + elseIfBlock.toString());
        }
        
    }

}
