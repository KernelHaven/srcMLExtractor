package net.ssehub.kernel_haven.srcml.transformation.testing.ast;

import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

public class Function extends SyntaxElementWithChildreen {

    private SyntaxElement header;
    
    public Function(@NonNull Formula presenceCondition, SyntaxElement header) {
        super(presenceCondition);
        this.header = header;
    }

    @Override
    protected String elementToString() {
        return "Function\n" + (header == null ? "\t\t\t\tnull" : header.toString("\t\t\t\t")); // TODO
    }

}
