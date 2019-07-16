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
package net.ssehub.kernel_haven.srcml_old.transformation.rules;

import net.ssehub.kernel_haven.srcml_old.transformation.ExceptionUtil;
import net.ssehub.kernel_haven.srcml_old.transformation.ITranslationUnit;
import net.ssehub.kernel_haven.srcml_old.transformation.TranslationUnit;
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
