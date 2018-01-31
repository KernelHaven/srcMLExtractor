package net.ssehub.kernel_haven.srcml.transformation.testing.ast;

import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

public class SingleStatement extends SyntaxElement {

    private SyntaxElement code;
    
    public SingleStatement(@NonNull Formula presenceCondition, SyntaxElement code) {
        super(presenceCondition);
        this.code = code;
    }
    
    public SyntaxElement getCode() {
        return code;
    }
    
    @Override
    protected String elementToString() {
        return "Statement:\n" + code.toString("\t\t\t\t");
    }

}