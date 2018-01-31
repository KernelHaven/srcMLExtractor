package net.ssehub.kernel_haven.srcml.transformation.testing.rules;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.ssehub.kernel_haven.srcml.transformation.testing.ITranslationUnit;
import net.ssehub.kernel_haven.srcml.transformation.testing.PreprocessorBlock;
import net.ssehub.kernel_haven.srcml.transformation.testing.PreprocessorElse;
import net.ssehub.kernel_haven.srcml.transformation.testing.PreprocessorEndIf;
import net.ssehub.kernel_haven.srcml.transformation.testing.PreprocessorIf;

public class PreprocessorBlockStructure implements ITransformationRule {
    
    private static class NestedElement {
        ITranslationUnit parent;
        ITranslationUnit child;
    }
    
    /**
     * Stores for each {@link PreprocessorBlock} the encapsulated translation units, that are the nodes which are on
     * the same layer but should be nested inside the {@link PreprocessorBlock}
     */
    private Map<PreprocessorBlock, List<NestedElement>> encapsulatedElementsMap = new HashMap<>();
    
    private Deque<PreprocessorBlock> parentblocks = new ArrayDeque<>();

    @Override
    public void transform(ITranslationUnit base) {
        for (ITranslationUnit nested : base) {
            createStructure(base, nested);
        }
        
        for (Map.Entry<PreprocessorBlock, List<NestedElement>> entry : encapsulatedElementsMap.entrySet()) {
            reorderElements(entry.getKey());
        }
    }
    
    private List<NestedElement> getEncapsulatedElements(PreprocessorBlock cppBlock) {
        List<NestedElement> list = encapsulatedElementsMap.get(cppBlock);
        if (null == list) {
            list = new ArrayList<>();
            encapsulatedElementsMap.put(cppBlock, list);
        }
        
        return list;
    }

    private void createStructure(ITranslationUnit parent, ITranslationUnit child) {
        if (child instanceof PreprocessorIf) {
            // Create block, but don't do anything
            PreprocessorIf currentBlock = (PreprocessorIf) child;
            getEncapsulatedElements(currentBlock);
            parentblocks.addFirst(currentBlock);
        } else if (child instanceof PreprocessorElse) {
            // From now on collect elements for else(if) block
            parentblocks.removeFirst();
            PreprocessorBlock currentBlock = (PreprocessorBlock) child;
            getEncapsulatedElements(currentBlock);
            parentblocks.addFirst(currentBlock);
        } else if (child instanceof PreprocessorEndIf) {
            // Stop collection
            parentblocks.removeFirst();
            
            // And mark endif block for removal (is no longer needed)
            NestedElement element = new NestedElement();
            element.parent = parent;
            element.child = child;
            getEncapsulatedElements(parentblocks.peekFirst()).add(element);
        } else {
            NestedElement element = new NestedElement();
            element.parent = parent;
            element.child = child;
            getEncapsulatedElements(parentblocks.peekFirst()).add(element);
        }
       
        for (ITranslationUnit nested : child) {
            
            // Recursive part
            createStructure(child, nested);
        }
    }

    private void reorderElements(PreprocessorBlock block) {
        // Move elements to cpp block
        List<NestedElement> list = getEncapsulatedElements(block);
        for (NestedElement oldNesting : list) {
            oldNesting.parent.removeNested(oldNesting.child);
            
            if (!(oldNesting.child instanceof PreprocessorEndIf)) {
                block.add(oldNesting.child);
            }
        }
    }

}
