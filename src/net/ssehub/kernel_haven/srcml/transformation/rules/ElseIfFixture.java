package net.ssehub.kernel_haven.srcml.transformation.rules;

import net.ssehub.kernel_haven.srcml.transformation.ExceptionUtil;
import net.ssehub.kernel_haven.srcml.transformation.ITranslationUnit;
import net.ssehub.kernel_haven.srcml.transformation.TranslationUnit;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Else-If Statement hold a nested if statement. This rule will pull up all the nested statements and eliminate the
 * nested if statement.
 * 
 * @author El-Sharkawy
 */
public class ElseIfFixture implements ITransformationRule {

    @Override
    public void transform(@NonNull ITranslationUnit unit) throws FormatException {
        // Check if this element is an elseif block, if yes restructure nested elements
        if ("elseif".equals(unit.getType())) {
            fixElseIfBlock(unit);
        }
        
        // Recursive part
        for (int i = 0; i < unit.size(); i++) {
            transform(unit.getNestedElement(i));
        }
    }

    /**
     * Fixes the given else-if block. Pulls up the nested if statement.
     * 
     * @param elseIfBlock The else-if block to fix.
     * 
     * @throws FormatException If fixing the block fails.
     */
    private void fixElseIfBlock(@NonNull ITranslationUnit elseIfBlock) throws FormatException {
        if (elseIfBlock.size() == 2 && elseIfBlock.getNestedElement(1) instanceof TranslationUnit
            && "if".equals(elseIfBlock.getNestedElement(1).getType()) && elseIfBlock instanceof TranslationUnit) {
            
            TranslationUnit elseifUnit = (TranslationUnit) elseIfBlock;
            ITranslationUnit nestedIf = elseIfBlock.getNestedElement(1);
            elseIfBlock.removeNested(1);
            for (int i = 0; i < nestedIf.size(); i++) {
                elseifUnit.add(nestedIf.getNestedElement(i));
            }
        } else {
            throw ExceptionUtil.makeException("Unexpected element nested inside elseif-statement at index 1: "
                + elseIfBlock.toString(), elseIfBlock);
        }
        
    }

}
