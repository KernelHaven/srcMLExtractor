/*
 * Copyright 2017-2019 University of Hildesheim, Software Systems Engineering
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.ssehub.kernel_haven.srcml.transformation.rules;

import net.ssehub.kernel_haven.srcml.transformation.ITranslationUnit;
import net.ssehub.kernel_haven.srcml.transformation.PreprocessorBlock;
import net.ssehub.kernel_haven.srcml.transformation.TranslationUnit;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Nests statements belonging to a <tt>case</tt> or a <tt>default</tt> into the unit, i.e., create a hierarchy out of
 * a slat structure.
 * @author El-Sharkawy
 *
 */
public class SwitchCaseStructure implements ITransformationRule {

    @Override
    public void transform(@NonNull ITranslationUnit unit) throws FormatException {
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
     * 
     * @param parent The parent element to search in.
     */
    private void determineSwitchBlock(@NonNull ITranslationUnit parent) {
        // Determine recursively first layer of block-statements (probably there is more than 1 due to CPP).
        for (int i = 0; i < parent.size(); i++) {
            ITranslationUnit nested = parent.getNestedElement(i);
            if ("block".equals(nested.getType())) {
                reorderElementsInSwitchBlock(nested, true);
            } else {
                determineSwitchBlock(nested);
            }
        }
    }
    
    /**
     * Reorders statements belonging to a case or default statement into this case or default statement, i.e., creating
     * the hierarchy.
     * 
     * @param switchBlock A block which is directly nested inside a switch-statement.
     * @param blockEndsWithBracket Whether the block ends with a curly bracket.
     */
    private void reorderElementsInSwitchBlock(@NonNull ITranslationUnit switchBlock, boolean blockEndsWithBracket) {
        // TODO SE: This won't consider if complete content of block is surrounded by one big CPP-block
        // Walk from back to front to avoid ConcurrentModification / IndexOutOfBound exceptions
        for (int i = switchBlock.size() - 1; i >= 0; i--) {
            ITranslationUnit nested = switchBlock.getNestedElement(i);
            if (isCaseStatement(nested)) {
                if (nested instanceof TranslationUnit) {
                    int endIndex = blockEndsWithBracket ? 1 : 0;
                    moveNestedCases(switchBlock, nested, i + 1, endIndex);
                } else if (nested instanceof PreprocessorBlock) {
                    reorderElementsInSwitchBlock(nested, false);
                }
            }
        }
    }

    /**
     * Reorders statements belonging to a case or default statement into this case or default statement, i.e., creating
     * the hierarchy.
     * 
     * @param switchBlock A block which is directly nested inside a switch-statement.
     * @param caseStatement The case which is currently re-ordered.
     * @param startIndex The index of the very first element of <tt>switchBlock</tt> after <tt>caseStatement</tt>
     * @param endOffset 1 if <tt>switchBlock</tt> is terminated with a curly bracket, which should <b>not</b> copied
     *     to the new structure, 0 otherwise.
     */
    private void moveNestedCases(@NonNull ITranslationUnit switchBlock, @NonNull ITranslationUnit caseStatement,
        int startIndex, int endOffset) {
        
        boolean nextCaseReached = false;
        // Very last element is closing bracket of the block, this shall be skipped
        while (startIndex < switchBlock.size() - endOffset && !nextCaseReached) {
            ITranslationUnit elementToMove = switchBlock.getNestedElement(startIndex);
            if (!isCaseStatement(elementToMove)) {
                caseStatement.add(elementToMove);
                switchBlock.removeNested(startIndex);
            } else {
                nextCaseReached = true;
            }
        }
    }
    
    /**
     * Checks if the current elements represents a <tt>case</tt> or <tt>default</tt> statement.
     * @param unit The element to check.
     * @return <tt>true</tt> if this is a <tt>case</tt> or <tt>default</tt> statement, <tt>false</tt> otherwise.
     */
    private boolean isCaseStatement(@NonNull ITranslationUnit unit) {
        return ("case".equals(unit.getType()) || "default".equals(unit.getType()) || unit instanceof PreprocessorBlock);
    }
}
