package net.ssehub.kernel_haven.srcml.transformation.rules;

import java.util.ArrayList;
import java.util.List;

import net.ssehub.kernel_haven.srcml.transformation.ITranslationUnit;
import net.ssehub.kernel_haven.srcml.transformation.TranslationUnit;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Some operators are parsed to multiple elements, which will be fixed by this rule:
 * <ul>
 *   <li> Ternary operators will be translated into a statement and an else expression</li>
 *   <li> Array declarations may be translated into a statement and a block</li>
 * </ul>
 * .
 * 
 * @author El-Sharkawy
 *
 */
public class SingleStatementStructures implements ITransformationRule {

    @Override
    public void transform(@NonNull ITranslationUnit unit) throws FormatException {
        int index;
        if (unit instanceof TranslationUnit && (index = nestedStatementPosition(unit)) != -1) {
            
            // Designed as a loop, because elements may further contain a comment
            boolean furtherProcessingNeeded;
            do {
                furtherProcessingNeeded = false;
                if ("expr_stmt".equals(unit.getType()) || "decl_stmt".equals(unit.getType())
                    || "return".equals(unit.getType())) {
                    
                    fixTranslationUnit((TranslationUnit) unit, index);
                    index = nestedStatementPosition(unit);
                    furtherProcessingNeeded = (-1 != index);
                }
            } while (furtherProcessingNeeded);
        }
        
        // Recursive part
        for (int i = 0; i < unit.size(); i++) {
            transform(unit.getNestedElement(i));
        }
    }

    /**
     * Fixes a single statement, containing nested statements.
     * @param unit The single statement (parent) to fix
     * @param index The index of the nested statement
     */
    private void fixTranslationUnit(@NonNull TranslationUnit unit, int index) {
        ITranslationUnit nestedStructure = unit.getNestedElement(index);
        List<@NonNull ITranslationUnit> newElements = new ArrayList<>();
        
        // Move elements from nested statement to expression
        for (int i = 0; i < nestedStructure.size(); i++) {
            newElements.add(nestedStructure.getNestedElement(i));
        }
        
        // Move last elements
        for (int i = index + 1; i < unit.size(); i++) {
            newElements.add(unit.getNestedElement(i));
        }
        
        // Remove touched elements
        boolean allElementsRemoved = false;
        while (!allElementsRemoved) {
            int removalIndex = unit.size() - 1;
            ITranslationUnit lastElement = unit.getNestedElement(removalIndex);
            unit.removeNested(removalIndex);
            allElementsRemoved = lastElement == nestedStructure;
        }
        
        // Add gathered elements to the end of the unit
        for (ITranslationUnit movedElement : newElements) {
            unit.add(movedElement);
        }
    }
    
    /**
     * Checks if a statement contains nested statements.
     * @param statement The statement to check.
     * @return The index of a nested statement or -1 if there isn't a nested statement.
     */
    private int nestedStatementPosition(@NonNull ITranslationUnit statement) {
        int index = -1;
        
        for (int i = statement.size() - 1; i >= 0 && index == -1; i--) {
            if (statement.getNestedElement(i) instanceof TranslationUnit) {
                index = i;
            }
        }
        
        return index;
    }

}
