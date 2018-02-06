package net.ssehub.kernel_haven.srcml.transformation.rules;

import net.ssehub.kernel_haven.srcml.transformation.ITranslationUnit;
import net.ssehub.kernel_haven.srcml.transformation.TranslationUnit;

/**
 * Nests statements belonging to a <tt>case</tt> or a <tt>default</tt> into the unit, i.e., create a hierarchy out of
 * a slat structure.
 * @author El-Sharkawy
 *
 */
public class SwitchCaseStructure implements ITransformationRule {

    @Override
    public void transform(ITranslationUnit unit) {
        // Check if this element is a switch statement, if yes restructure nested elements
        if ("switch".equals(unit.getType())) {
            determineSwitchBlock(unit);
        }
        
        // Recursive part
        for (int i = 0; i < unit.size(); i++) {
            transform(unit.getNestedElement(i));
        }
    }

    /**
     * Searches for block-statements directly nested inside a switch to reorder elements only in this block and not
     * also in recursively nested blocks. Will also consider CPP-blocks.
     * @param parent
     */
    private void determineSwitchBlock(ITranslationUnit parent) {
        // Determine recursively first layer of block-statements (probably there is more than 1 due to CPP).
        for (int i = 0; i < parent.size(); i++) {
            ITranslationUnit nested = parent.getNestedElement(i);
            if ("block".equals(nested.getType())) {
                reorderElementsInSwitchBlock(nested);
            } else {
                determineSwitchBlock(nested);
            }
        }
    }
    
    /**
     * Reorders statements belonging to a case or default statement into this case or default statement, i.e., creating
     * the hierarchy.
     * @param switchBlock A block which is directly nested inside a switch-statement.
     */
    private void reorderElementsInSwitchBlock(ITranslationUnit switchBlock) {
        // TODO SE: This won't consider if complete content of block is surrounded by one big CPP-block
        // Walk from back to front to avoid ConcurrentModification / IndexOutOfBound exceptions
        for (int i = switchBlock.size() - 1; i >= 0; i--) {
            ITranslationUnit nested = switchBlock.getNestedElement(i);
            if (isCaseStatement(nested)) {
                TranslationUnit caseStatement = (TranslationUnit) nested;
                int startIndex = i + 1;
                boolean nextCaseReached = false;
                // Very last element is closing bracket of the block, this shall be skipped
                while (startIndex < switchBlock.size() - 1 && !nextCaseReached) {
                    ITranslationUnit elementToMove = switchBlock.getNestedElement(startIndex);
                    if (!isCaseStatement(elementToMove)) {
                        caseStatement.add(elementToMove);
                        switchBlock.removeNested(elementToMove);
                    } else {
                        nextCaseReached = true;
                    }
                }
            }
        }
    }
    
    /**
     * Checks if the current elements represents a <tt>case</tt> or <tt>default</tt> statement.
     * @param unit The element to check.
     * @return <tt>true</tt> if this is a <tt>case</tt> or <tt>default</tt> statement, <tt>false</tt> otherwise.
     */
    private boolean isCaseStatement(ITranslationUnit unit) {
        return "case".equals(unit.getType()) || "default".equals(unit.getType());
    }
}
