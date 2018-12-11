package net.ssehub.kernel_haven.srcml.transformation.rules;

import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.ssehub.kernel_haven.srcml.transformation.ITranslationUnit;
import net.ssehub.kernel_haven.srcml.transformation.PreprocessorBlock;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Replace all variables in PreprocessorBlock conditions that have no surrounding "defined()" with "0"
 * Removes the spaces between the elements, e.g. "defined ( A ) & & defined ( B )" -> "defined(A)&&defined(B)"
 */
public class MissingDefined implements ITransformationRule {

    private static final @NonNull Pattern VARIABLE_PATTERN = notNull(Pattern.compile("[A-Za-z0-9_]+"));
    
    @Override
    public void transform(@NonNull ITranslationUnit unit) throws FormatException {
        if (unit instanceof PreprocessorBlock) {
            PreprocessorBlock block = (PreprocessorBlock) unit;
            String condition = block.getCondition();
            
            if (condition != null) {
                String[] parts = condition.split(" ");
                
                StringBuilder newCondition = new StringBuilder();
                for (int i = 0; i < parts.length; i++) {
                    
                    // skip fields that are "defined" followed by a "(", since these aren't variables
                    if (!(parts[i].equals("defined") && i + 1 < parts.length && parts[i + 1].equals("("))) {
                        Matcher m = VARIABLE_PATTERN.matcher(parts[i]);
                        if (m.matches()) {
                            // we found a variable, check if there is a "defined()" call around it
                            if (i < 2 || !parts[i - 2].equals("defined") || !parts[i - 1].equals("(")
                                    || i + 1 >= parts.length || !parts[i + 1].equals(")")) {
                                
                                // we found a variable without a defined() call around it; replace with false
                                parts[i] =  "0";
                            }
                            
                        }
                    }
                    
                    newCondition.append(parts[i]);
                }
                
                block.setCondition(newCondition.toString());
            }
        }
        
        // Recursive part
        for (int i = 0; i < unit.size(); i++) {
            transform(unit.getNestedElement(i));
        }
    }

}
