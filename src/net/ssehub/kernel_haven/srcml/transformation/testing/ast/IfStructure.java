package net.ssehub.kernel_haven.srcml.transformation.testing.ast;

import java.io.File;

import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

public class IfStructure extends SyntaxElementWithChildreen {
    
    private SyntaxElement condition;
    
    public IfStructure(@NonNull Formula presenceCondition, File sourceFile, SyntaxElement condition) {
        super(presenceCondition, sourceFile);
        this.condition = condition;
    }

    @Override
    protected String elementToString() {
        return "if\n" + (condition == null ? "\t\t\t\tnull" : condition.toString("\t\t\t\t")); // TODO
    }

}
