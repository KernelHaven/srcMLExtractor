package net.ssehub.kernel_haven.srcml.transformation;

import java.util.ArrayList;
import java.util.List;

import net.ssehub.kernel_haven.srcml.xml.SrcMlConditionGrammar;

/**
 * Represents the starting elements of an preprocessor if, it represents one of
 * <tt>&#35;if, &#35;ifdef, &#35;ifndef</tt>. Further, this class holds a reference to all of its alternatives, which
 * are represented by {@link PreprocessorElse}s.
 * @author El-Sharkawy
 *
 */
public class PreprocessorIf extends PreprocessorBlock {
    
    private List<PreprocessorElse> siblings = new ArrayList<>();
    
    /**
     * Sole constructor for this class.
     * @param type Denotes which kind of <i>if</i> this object represents (one of {@link Type#IF}, {@link Type#IFDEF},
     *     or {@link Type#IFNDEF})
     * @param condition The condition of the if, should be in form that the {@link SrcMlConditionGrammar} can handle it.
     */
    public PreprocessorIf(Type type, String condition) {
        super(type, condition);
    }
    
    /**
     * Adds an associated {@link PreprocessorElse}-block. These block must be inserted in the correct order!
     * @param sibling An associated {@link PreprocessorElse}-block
     */
    public void addSibling(PreprocessorElse sibling) {
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
    public PreprocessorElse getElseBlock(int index) {
        return siblings.get(index);
    }
}