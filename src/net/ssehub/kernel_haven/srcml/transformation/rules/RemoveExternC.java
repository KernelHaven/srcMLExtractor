package net.ssehub.kernel_haven.srcml.transformation.rules;

import net.ssehub.kernel_haven.srcml.transformation.CodeUnit;
import net.ssehub.kernel_haven.srcml.transformation.ITranslationUnit;
import net.ssehub.kernel_haven.srcml.transformation.TranslationUnit;
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Removes <code>extern "C" { ... }</code> blocks from the top level. The whole extern statement is replaced by the
 * contents of its nested block.
 * 
 * @author Adam
 */
public class RemoveExternC implements ITransformationRule {

    @Override
    public void transform(@NonNull ITranslationUnit base) throws FormatException {
        for (int i = 0; i < base.size(); i++) {
            ITranslationUnit child = base.getNestedElement(i);
            
            if (child instanceof CodeUnit) {
                CodeUnit unit = (CodeUnit) child;
                
                TranslationUnit block = checkExternC(unit, i, base);
                
                if (block != null) {
                    int externIndex = i;
                    int cIndex = i + 1;
                    int blockIndex = i + 2;
                    
                    // remove extern, "C", and block
                    base.removeNested(blockIndex);
                    base.removeNested(cIndex);
                    base.removeNested(externIndex);
                    
                    // add the elements nested inside the block to the top level element
                    // from the front to the back, since we insert at the same index and it pushes to the right
                    // skip first and last element (opening and closing curly braces: {}).
                    for (int j = block.size() - 2; j > 0; j--) {
                        base.insertNested(externIndex, block.getNestedElement(j));
                    }
                }
                
            }
            
        }
        
        // no recursive part; extern "C" blocks are always at the top level
    }
    
    /**
     * Checks if the given unit in the given parent is an 'extern "C" { ... }' block. The unit has to have the code
     * "extern". The next sibling has to be a {@link CodeUnit} with code "\"C\"". The sibling after that has to be a
     * block.
     * 
     * @param unit The unit that may be an "extern".
     * @param unitIndex The index of the unit in the parent.
     * @param parent The parent that the unit is nested in. This is most likely the top level element of the whole file.
     * 
     * @return The block of the extern, or <code>null</code> if the given unit is not an extern "C" block.
     */
    private TranslationUnit checkExternC(@NonNull CodeUnit unit, int unitIndex, @NonNull ITranslationUnit parent) {
        TranslationUnit block = null;
        
        if (unit.getCode().equals("extern") && (unitIndex + 2) < parent.size()
                && parent.getNestedElement(unitIndex + 1) instanceof CodeUnit
                && parent.getNestedElement(unitIndex + 2) instanceof TranslationUnit) {
            
            CodeUnit type = (CodeUnit) parent.getNestedElement(unitIndex + 1);
            block = (TranslationUnit) parent.getNestedElement(unitIndex + 2);
            
            if (!type.getCode().equals("\"C\"") || !block.getType().equals("block")) {
                block = null;
            }
            
        }
        
        return block;
    }

}
