package net.ssehub.kernel_haven.srcml.transformation.rules;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.ssehub.kernel_haven.srcml.transformation.ITranslationUnit;
import net.ssehub.kernel_haven.srcml.transformation.PreprocessorBlock;
import net.ssehub.kernel_haven.srcml.transformation.PreprocessorElse;
import net.ssehub.kernel_haven.srcml.transformation.PreprocessorEndIf;
import net.ssehub.kernel_haven.srcml.transformation.PreprocessorIf;

/**
 * Moves {@link ITransformationRule}s which are encapsulated by CPP statements into the CPP statement as a nested
 * element.
 * @author El-Sharkawy
 *
 */
public class PreprocessorBlockStructure implements ITransformationRule {
    
    /**
     * Simple data structure/container. Stores the current, obsolete parent and its child (which shall be moved).
     * @author El-Sharkawy
     *
     */
    private static class NestedElement {
        ITranslationUnit parent;
        ITranslationUnit child;
    }
    
    private static class BlockParent {
        ITranslationUnit parent;
        PreprocessorBlock child;
    }
    
    /**
     * Stores for each {@link PreprocessorBlock} the encapsulated translation units, that are the nodes which are on
     * the same layer but should be nested inside the {@link PreprocessorBlock}
     */
    private Map<PreprocessorBlock, List<NestedElement>> encapsulatedElementsMap = new HashMap<>();
    
    private Deque<BlockParent> parentblocks = new ArrayDeque<>();

    @Override
    public void transform(ITranslationUnit base) {
        // Identify elements but do not change elements to avoid concurrent modification exceptions
        for (int i = 0; i < base.size(); i++) {
            identifyStructure(base, base.getNestedElement(i));
        }
        
        // Apply changes
        for (Map.Entry<PreprocessorBlock, List<NestedElement>> entry : encapsulatedElementsMap.entrySet()) {
            reorderElements(entry.getKey());
        }
    }
    
    /**
     * Returns the list of elements, which shall be nested below the specified {@link PreprocessorBlock}.
     * @param cppBlock The specified {@link PreprocessorBlock} under which the elements shall be nested, may be
     *     <tt>null</tt> in case of an {@link PreprocessorEndIf} shall be removed from the top level.
     * @return
     */
    private List<NestedElement> getEncapsulatedElements(PreprocessorBlock cppBlock) {
        List<NestedElement> list = encapsulatedElementsMap.get(cppBlock);
        if (null == list) {
            list = new ArrayList<>();
            encapsulatedElementsMap.put(cppBlock, list);
        }
        
        return list;
    }

    /**
     * First part, will recursively identify all encapsulated elements and the target {@link PreprocessorBlock} under
     * which they shall be nested to.
     * @param parent The current (potentially obsolete) parent.
     * @param child The child, which may be moved.
     */
    private void identifyStructure(ITranslationUnit parent, ITranslationUnit child) {
        if (child instanceof PreprocessorIf) {
            markForReordering(parent, child);
            // Create block, but don't do anything
            PreprocessorIf currentBlock = (PreprocessorIf) child;
           BlockParent block = new BlockParent();
           block.parent = parent;
           block.child = currentBlock;
            getEncapsulatedElements(currentBlock);
            parentblocks.addFirst(block);
        } else if (child instanceof PreprocessorElse) {
            // From now on collect elements for else(if) block
            parentblocks.removeFirst();
            markForReordering(parent, child);
            PreprocessorBlock currentBlock = (PreprocessorBlock) child;
            getEncapsulatedElements(currentBlock);
           BlockParent block = new BlockParent();
           block.parent = parent;
           block.child = currentBlock;
            parentblocks.addFirst(block);
        } else if (child instanceof PreprocessorEndIf) {
            // Stop collection
            parentblocks.removeFirst();
            
            markForReordering(parent, child);
        } else {
            markForReordering(parent, child);
        }
       
        for (int i = 0; i < child.size(); i++) {
            // Recursive part
            identifyStructure(child, child.getNestedElement(i));
        }
    }

    /**
     * Marks an element for moving. 
     * @param parent The current, obsolete parent
     * @param child The child to be moved.
     */
    private void markForReordering(ITranslationUnit parent, ITranslationUnit child) {
//        PreprocessorBlock currentblock = parentblocks.peekFirst();
        BlockParent currentblock = parentblocks.peekFirst();
        
        /*
         * Mark elements for reordering if their exist a target CPP block
         * Mark PreprocessorEndIfs for removal in any case
         */
        if (null != currentblock && null != currentblock.parent) {
            boolean isNested = (parent != currentblock.child);
            boolean isNotSubNested = (currentblock.parent == parent);
            if (isNested && isNotSubNested) {
                NestedElement element = new NestedElement();
                element.parent = parent;
                element.child = child;
                getEncapsulatedElements(currentblock.child).add(element);
            }
        } else if (child instanceof PreprocessorEndIf) {
            NestedElement element = new NestedElement();
            element.parent = parent;
            element.child = child;
            PreprocessorBlock oldParent = currentblock != null ? currentblock.child : null;
            getEncapsulatedElements(oldParent).add(element);
        }
    }

    /**
     * Moves all identified elements to the block.
     * @param block The {@link PreprocessorBlock} for which the action shall be performed.
     */
    private void reorderElements(PreprocessorBlock block) {
        // Move elements to CPP block
        List<NestedElement> list = getEncapsulatedElements(block);
        for (NestedElement oldNesting : list) {
            oldNesting.parent.removeNested(oldNesting.child);
            
            if (!(oldNesting.child instanceof PreprocessorEndIf)) {
                block.add(oldNesting.child);
            }
        }
    }

}
