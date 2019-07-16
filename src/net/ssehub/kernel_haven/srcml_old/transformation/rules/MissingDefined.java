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

import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.ssehub.kernel_haven.srcml_old.transformation.ITranslationUnit;
import net.ssehub.kernel_haven.srcml_old.transformation.PreprocessorBlock;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Replace all variables in PreprocessorBlock conditions that have no surrounding "defined()" with "0"
 * Removes the spaces between the elements, e.g. {@code "defined ( A ) & & defined ( B )" -> "defined(A)&&defined(B)"}.
 */
public class MissingDefined implements ITransformationRule {

    private static final @NonNull Pattern VARIABLE_PATTERN = notNull(Pattern.compile("[A-Za-z0-9_]+"));
    
    @Override
    public void transform(@NonNull ITranslationUnit unit) throws FormatException {
        if (unit instanceof PreprocessorBlock) {
            PreprocessorBlock block = (PreprocessorBlock) unit;
            String condition = block.getCondition();
            
            if (condition != null) {
                block.setCondition(transformCondition(condition));
            }
        }
        
        // Recursive part
        for (int i = 0; i < unit.size(); i++) {
            transform(unit.getNestedElement(i));
        }
    }

    /**
     * Transforms the given condition string.
     * 
     * @param condition The condition to transform.
     * 
     * @return The transformed condition.
     */
    private String transformCondition(String condition) {
        String[] parts = condition.split(" ");
        
        StringBuilder newCondition = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            
            // skip fields that are "defined" followed by a "(", since these aren't variables
            if (!(parts[i].equals("defined") && i + 1 < parts.length && parts[i + 1].equals("("))) {
                Matcher m = VARIABLE_PATTERN.matcher(parts[i]);
                if (m.matches()) {
                    // we found a variable, check if there is a "defined()" call around it
                    if (!hasDefined(parts, i)) {
                        
                        // we found a variable without a defined() call around it; replace with false
                        parts[i] =  "0";
                    }
                    
                }
            }
            
            newCondition.append(parts[i]);
        }
        return newCondition.toString();
    }
    
    /**
     * Checks whether the variable at the given index has a "defined()" call around it.
     * 
     * @param parts The tokens of the expression.
     * @param index The index of the variable.
     * 
     * @return Whether the variable has a defined call around it.
     */
    private boolean hasDefined(String[] parts, int index) {
        boolean result;
        
        // we need two tokens in front ("defined", "("), and one token after the index (")")
        
        // -> check that this is in the given bounds
        if (index < 2 || index + 1 >= parts.length) {
            result = false;
        
        // check that the right tokens are present
        } else if (parts[index - 2].equals("defined") && parts[index - 1].equals("(") && parts[index + 1].equals(")")) {
            result = true;
            
        } else {
            result = false;
        }
        
        return result;
    }

}
