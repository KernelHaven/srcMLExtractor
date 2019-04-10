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
package net.ssehub.kernel_haven.srcml.transformation;

import static net.ssehub.kernel_haven.util.null_checks.NullHelpers.notNull;

import java.util.ArrayList;
import java.util.List;

import net.ssehub.kernel_haven.srcml.xml.SrcMlConditionGrammar;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Represents the starting elements of an preprocessor if, it represents one of
 * <tt>&#35;if, &#35;ifdef, &#35;ifndef</tt>. Further, this class holds a reference to all of its alternatives, which
 * are represented by {@link PreprocessorElse}s.
 * @author El-Sharkawy
 *
 */
public class PreprocessorIf extends PreprocessorBlock {
    
    private @NonNull List<@NonNull PreprocessorElse> siblings = new ArrayList<>();
    
    /**
     * Sole constructor for this class.
     * @param type Denotes which kind of <i>if</i> this object represents (one of {@link Type#IF}, {@link Type#IFDEF},
     *     or {@link Type#IFNDEF})
     * @param condition The condition of the if, should be in form that the {@link SrcMlConditionGrammar} can handle it.
     */
    public PreprocessorIf(@NonNull Type type, @NonNull String condition) {
        super(type, condition);
    }
    
    /**
     * Adds an associated {@link PreprocessorElse}-block. These block must be inserted in the correct order!
     * @param sibling An associated {@link PreprocessorElse}-block
     */
    public void addSibling(@NonNull PreprocessorElse sibling) {
        siblings.add(sibling);
    }
    
    /**
     * Returns the number of associated {@link PreprocessorElse}-blocks.
     * @return the number of associated {@link PreprocessorElse}-blocks (&ge; 0).
     */
    public int getNumberOfElseBlocks() {
        return siblings.size();
    }
    
    /**
     * Returns the specified {@link PreprocessorElse}.
     * @param index The 0-based index for the {@link PreprocessorElse}-blocks to return. 
     * @return The specified {@link PreprocessorElse}-blocks, which belongs to this if-block.
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt;= getNumberOfElseBlocks()</tt>)
     * @see #getNumberOfElseBlocks()
     */
    public @NonNull PreprocessorElse getElseBlock(int index) {
        return notNull(siblings.get(index));
    }

    @Override
    public PreprocessorIf getStartingIf() {
        return this;
    }
}
