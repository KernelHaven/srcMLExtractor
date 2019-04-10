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

import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

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
import net.ssehub.kernel_haven.util.FormatException;
import net.ssehub.kernel_haven.util.null_checks.NonNull;
import net.ssehub.kernel_haven.util.null_checks.Nullable;

/**
 * Moves {@link ITransformationRule}s which are encapsulated by CPP statements into the CPP statement as a nested
 * element.
 * @author El-Sharkawy
 *
 */
public class PreprocessorBlockStructure implements ITransformationRule {
    
    /**
     * Simple data structure/container. Stores the current, obsolete parent and its child (which shall be moved).
     */
    private static class NestedElement {
        private @NonNull ITranslationUnit parent;
        private @NonNull ITranslationUnit child;
        
        /**
         * Creates a {@link NestedElement}.
         * 
         * @param parent The parent element.
         * @param child The child element.
         */
        public NestedElement(@NonNull ITranslationUnit parent, @NonNull ITranslationUnit child) {
            this.parent = parent;
            this.child = child;
        }
        
    }
    
    /**
     * Simple data structure/container. Stores the current, obsolete parent and its child (which shall be moved).
     * @author El-Sharkawy
     *
     */
    private static class BlockParent {
        private @NonNull ITranslationUnit parent;
        private @NonNull PreprocessorBlock child;

        /**
         * Creates a {@link BlockParent}.
         * 
         * @param parent The parent element.
         * @param child The child element.
         */
        public BlockParent(@NonNull ITranslationUnit parent, @NonNull PreprocessorBlock child) {
            this.parent = parent;
            this.child = child;
        }
        
    }
    
    /**
     * Stores for each {@link PreprocessorBlock} the encapsulated translation units, that are the nodes which are on
     * the same layer but should be nested inside the {@link PreprocessorBlock}.
     */
    private Map<@Nullable PreprocessorBlock, List<@NonNull NestedElement>> encapsulatedElementsMap
            = new HashMap<>();
    
    private Deque<BlockParent> parentblocks = new ArrayDeque<>();

    @Override
    public void transform(@NonNull ITranslationUnit base) throws FormatException {
        // Identify elements but do not change elements to avoid concurrent modification exceptions
        for (int i = 0; i < base.size(); i++) {
            identifyStructure(base, base.getNestedElement(i));
        }
        
        // Apply changes
        for (Map.Entry<@Nullable PreprocessorBlock, List<@NonNull NestedElement>> entry
                : encapsulatedElementsMap.entrySet()) {
            reorderElements(entry.getKey());
        }
    }
    
    /**
     * Returns the list of elements, which shall be nested below the specified {@link PreprocessorBlock}.
     * 
     * @param cppBlock The specified {@link PreprocessorBlock} under which the elements shall be nested, may be
     *     <tt>null</tt> in case of an {@link PreprocessorEndIf} shall be removed from the top level.
     *     
     * @return The list of encapsulated elements.
     */
    private @NonNull List<@NonNull NestedElement> getEncapsulatedElements(@Nullable PreprocessorBlock cppBlock) {
        List<@NonNull NestedElement> list = encapsulatedElementsMap.get(cppBlock);
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
    private void identifyStructure(@NonNull ITranslationUnit parent, @NonNull ITranslationUnit child) {
        if (child instanceof PreprocessorIf) {
            markForReordering(parent, child);
            // Create block, but don't do anything
            PreprocessorIf currentBlock = (PreprocessorIf) child;
            BlockParent block = new BlockParent(parent, currentBlock);
            getEncapsulatedElements(currentBlock);
            parentblocks.addFirst(block);
        } else if (child instanceof PreprocessorElse) {
            // From now on collect elements for else(if) block
            parentblocks.removeFirst();
            markForReordering(parent, child);
            PreprocessorBlock currentBlock = (PreprocessorBlock) child;
            getEncapsulatedElements(currentBlock);
            BlockParent block = new BlockParent(parent, currentBlock);
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
    private void markForReordering(@NonNull ITranslationUnit parent, @NonNull ITranslationUnit child) {
        BlockParent currentblock = parentblocks.peekFirst();
        
        /*
         * Mark elements for reordering if their exist a target CPP block
         * Mark PreprocessorEndIfs for removal in any case
         */
        boolean processed = false;
        if (null != currentblock) {
            boolean isNested = (parent != currentblock.child);
            boolean isNotSubNested = (currentblock.parent == parent);
            if (isNested && isNotSubNested) {
                NestedElement element = new NestedElement(parent, child);
                getEncapsulatedElements(currentblock.child).add(element);
                processed = true;
            }
        } 
        
        if (!processed && child instanceof PreprocessorEndIf) {
            NestedElement element = new NestedElement(parent, child);
            PreprocessorBlock oldParent = currentblock != null ? currentblock.child : null;
            getEncapsulatedElements(oldParent).add(element);
        }
    }

    /**
     * Moves all identified elements to the block.
     * @param block The {@link PreprocessorBlock} for which the action shall be performed.
     */
    private void reorderElements(@Nullable PreprocessorBlock block) {
        int blockEndIndex = null != block ? block.getEndLine() : -1;
        
        // Move elements to CPP block
        List<@NonNull NestedElement> list = getEncapsulatedElements(block);
        for (NestedElement oldNesting : list) {
            oldNesting.parent.removeNested(oldNesting.child);
            
            // Temporarily store the end of the block (consider also the endif statement)
            if (oldNesting.child.getEndLine() > blockEndIndex) {
                blockEndIndex = oldNesting.child.getEndLine();
            }
            
            if (!(oldNesting.child instanceof PreprocessorEndIf)) {
                // block is only null if its an PreprocessorEndi
                notNull(block).add(oldNesting.child);
            }
        }
        
        // Compute/Update (for the first time) the end of the preprocessor block
        if (null != block) {
            block.setEndLine(blockEndIndex);
        }
    }

}
