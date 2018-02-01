package net.ssehub.kernel_haven.srcml.transformation.testing;

import net.ssehub.kernel_haven.srcml.xml.SrcMlConditionGrammar;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Representation of an <tt>&#35;else</tt> or <tt>&#35;elif</tt> block.
 * @author El-Sharkawy
 *
 */
public class PreprocessorElse extends PreprocessorBlock {

    private @NonNull PreprocessorIf startElement = null;
    
    /**
     * Sole constructor for this class.
     * @param type Denotes which kind of <i>else</i>
     *     this object represents (one of {@link Type#ELSEIF}, or {@link Type#ELSE})
     * @param condition The condition of the if, should be in form that the {@link SrcMlConditionGrammar} can handle it,
     *     or <tt>null</tt> in case of an <tt>&#35;else</tt>-block.
     * @param startElement The starting block, must not be <tt>null</tt>.
     */
    public PreprocessorElse(Type type, String condition, @NonNull PreprocessorIf startElement) {
        super(type, condition);
        this.startElement = startElement;
    }

    /**
     * Returns the first block of the if-elif-else structure, the <tt>&#35;if</tt> block.
     * @return The <tt>&#35;if</tt> block to which this else block belongs to.
     */
    public @NonNull PreprocessorIf getStartingIf() {
        return startElement;
    }
}
