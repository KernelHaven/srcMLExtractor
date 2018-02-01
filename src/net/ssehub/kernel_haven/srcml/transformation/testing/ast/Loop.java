package net.ssehub.kernel_haven.srcml.transformation.testing.ast;

import java.io.File;

import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Represents a loop.
 * @author El-Sharkawy
 *
 */
public class Loop extends SyntaxElementWithChildreen {
    
    public static enum LoopType {
        WHILE, DO_WHILE, FOR;
    }

    private SyntaxElement condition;
    private LoopType type;
    
    public Loop(@NonNull Formula presenceCondition, File sourceFile, SyntaxElement condition, LoopType type) {
        super(presenceCondition, sourceFile);
        this.condition = condition;
        this.type = type;
    }

    @Override
    protected String elementToString() {
        return type.name() + "-loop\n" + (condition == null ? "\t\t\t\tnull" : condition.toString("\t\t\t\t")); // TODO
    }

}
