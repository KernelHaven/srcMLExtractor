package net.ssehub.kernel_haven.srcml.transformation.rules;

import java.util.ArrayList;
import java.util.List;

import net.ssehub.kernel_haven.srcml.transformation.ITranslationUnit;
import net.ssehub.kernel_haven.srcml.transformation.TranslationUnit;

/**
 * Ternary operators will be translated into a statement and an else expression, this rule will merge both together.
 * @author El-Sharkawy
 *
 */
public class TernaryOperatorStructure implements ITransformationRule {

    @Override
    public void transform(ITranslationUnit unit) {
        int index;
        if ("expr_stmt".equals(unit.getType()) && (index = nestedElsePosition(unit)) != -1) {
            ITranslationUnit nestedElse = unit.getNestedElement(index);
            List<ITranslationUnit> newElements = new ArrayList<>();
            
            // Move elements from else to expression
            for (int i = 0; i < nestedElse.size(); i++) {
                newElements.add(nestedElse.getNestedElement(i));
            }
            
            // Move last elements
            for (int i = index + 1; i < unit.size(); i++) {
                newElements.add(unit.getNestedElement(i));
            }
            
            // Remove touched elements
            boolean allElementsRemoved = false;
            while(!allElementsRemoved) {
                ITranslationUnit lastElement = unit.getNestedElement(unit.size() - 1);
                unit.removeNested(lastElement);
                allElementsRemoved = lastElement == nestedElse;
            }
            
            // Add gathered elements to the end of the unit
            TranslationUnit statement = (TranslationUnit) unit;
            for (ITranslationUnit movedElement : newElements) {
                statement.add(movedElement);
            }
        }
        
        // Recursive part
        for (int i = 0; i < unit.size(); i++) {
            transform(unit.getNestedElement(i));
        }
    }
    
    private int nestedElsePosition(ITranslationUnit statement) {
        int index = -1;
        
        for (int i = statement.size() - 1; i >= 0 && index == -1; i--) {
            if ("else".equals(statement.getNestedElement(i).getType())) {
                index = i;
            }
        }
        
        return index;
    }

}
